package com.example.myfirstandroidapplication.data

import android.content.Context
import com.example.myfirstandroidapplication.FirebaseService
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * Resolves a stable author identity and authenticated display name for community quizzes.
 */
object AuthManager {

    suspend fun ensureUserId(context: Context): String {
        val preferencesManager = PreferencesManager(context)

        FirebaseService.getAuth()?.currentUser?.let { user ->
            if (user.uid.isNotEmpty()) {
                preferencesManager.setUserId(user.uid)
                user.displayName?.takeIf { it.isNotBlank() }?.let { name ->
                    preferencesManager.setAuthorName(name)
                }
                return user.uid
            }
        }

        FirebaseService.getAuth()?.let { auth ->
            try {
                val user = auth.signInAnonymously().awaitTask().user
                val uid = user?.uid
                if (!uid.isNullOrEmpty()) {
                    preferencesManager.setUserId(uid)
                    user.displayName?.takeIf { it.isNotBlank() }?.let { name ->
                        preferencesManager.setAuthorName(name)
                    }
                    return uid
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val existing = preferencesManager.userId.first()
        if (existing.isNotEmpty()) return existing

        val localId = "local_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        preferencesManager.setUserId(localId)
        return localId
    }

    suspend fun getAuthorName(context: Context): String {
        val preferencesManager = PreferencesManager(context)

        // 1. Firebase Auth currentUser's displayName (Google account name)
        val firebaseUser = FirebaseService.getAuth()?.currentUser
        val googleDisplayName = firebaseUser?.displayName?.takeIf { it.isNotBlank() }
        if (googleDisplayName != null) {
            preferencesManager.setAuthorName(googleDisplayName)
            return googleDisplayName
        }

        // 2. Email username if displayName is absent
        val googleEmailName = firebaseUser?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        if (googleEmailName != null) {
            preferencesManager.setAuthorName(googleEmailName)
            return googleEmailName
        }

        // 3. Saved authorName in PreferencesManager if customized
        val savedName = preferencesManager.authorName.first()
        if (savedName.isNotBlank() && !savedName.startsWith("User_local")) {
            return savedName
        }

        // 4. Default fallback UID
        val uid = ensureUserId(context)
        val defaultName = "User_${uid.take(5)}"
        preferencesManager.setAuthorName(defaultName)
        return defaultName
    }

    suspend fun signInWithGoogleCredential(context: Context, idToken: String): Result<String> {
        val auth = FirebaseService.getAuth() ?: return Result.failure(IllegalStateException("Firebase Auth not initialized"))
        val preferencesManager = PreferencesManager(context)
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).awaitTask()
            val user = authResult.user ?: throw IllegalStateException("Google sign in returned no user")
            preferencesManager.setUserId(user.uid)
            val name = user.displayName?.takeIf { it.isNotBlank() } ?: user.email?.substringBefore("@") ?: "User_${user.uid.take(5)}"
            preferencesManager.setAuthorName(name)
            Result.success(name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(context: Context) {
        val auth = FirebaseService.getAuth()
        auth?.signOut()
        val preferencesManager = PreferencesManager(context)
        preferencesManager.setAuthorName("")
        preferencesManager.setUserId("")
    }

    fun isGoogleSignedIn(): Boolean {
        val user = FirebaseService.getAuth()?.currentUser ?: return false
        return !user.isAnonymous && user.providerData.any { it.providerId == com.google.firebase.auth.GoogleAuthProvider.PROVIDER_ID }
    }

    fun getCurrentGoogleUser(): com.google.firebase.auth.FirebaseUser? {
        val user = FirebaseService.getAuth()?.currentUser ?: return null
        return if (isGoogleSignedIn()) user else null
    }
}