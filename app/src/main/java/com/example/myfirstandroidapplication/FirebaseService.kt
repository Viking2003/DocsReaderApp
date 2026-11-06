package com.example.myfirstandroidapplication

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseService {
    // Values taken from app/google-services.json (Firebase project: docs-reader-5c789).
    // The app configures Firebase manually, so the com.google.gms.google-services
    // Gradle plugin is not applied and FirebaseOptions must be supplied explicitly.
    private const val APPLICATION_ID = "1:241382234742:android:d7a2473fc08eed7cde9d57"
    private const val API_KEY = "AIzaSyDHxU32Aol_X5snZcpIPHbL9y2t20cKgl8"
    private const val PROJECT_ID = "docs-reader-5c789"
    private const val STORAGE_BUCKET = "docs-reader-5c789.firebasestorage.app"
    private const val GCM_SENDER_ID = "241382234742"

    @Volatile
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            val appContext = context.applicationContext
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(APPLICATION_ID)
                    .setApiKey(API_KEY)
                    .setProjectId(PROJECT_ID)
                    .setStorageBucket(STORAGE_BUCKET)
                    .setGcmSenderId(GCM_SENDER_ID)
                    .build()
                FirebaseApp.initializeApp(appContext, options)
            }
            // Only report availability once a default FirebaseApp really exists.
            isInitialized = FirebaseApp.getApps(appContext).isNotEmpty()
        } catch (e: Exception) {
            isInitialized = false
            e.printStackTrace()
        }
    }

    fun getAnalytics(context: Context): FirebaseAnalytics? {
        if (!isInitialized) return null
        return try {
            FirebaseAnalytics.getInstance(context)
        } catch (e: Exception) {
            null
        }
    }

    fun getAuth(): FirebaseAuth? {
        if (!isInitialized) return null
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    fun getFirestore(): FirebaseFirestore? {
        if (!isInitialized) return null
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    fun isFirebaseAvailable(): Boolean {
        return isInitialized
    }
}
