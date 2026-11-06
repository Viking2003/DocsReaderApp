package com.example.myfirstandroidapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.myfirstandroidapplication.FirebaseService
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reader_preferences")

suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}

class PreferencesManager(private val context: Context) {

    companion object {
        private val LAST_READ_CATEGORY = intPreferencesKey("last_read_category")
        private val LAST_READ_PAGE_INDEX = intPreferencesKey("last_read_page_index")
        private val BOOKMARKS = stringPreferencesKey("bookmarks")
        private val NOTES_PREFIX = "note_"
        private val TOTAL_READING_TIME = longPreferencesKey("total_reading_time")
        private val TOTAL_QUIZZES_TAKEN = intPreferencesKey("total_quizzes_taken")
        private val TOTAL_QUIZ_SCORE = intPreferencesKey("total_quiz_score")
        private val TOTAL_QUIZ_QUESTIONS = intPreferencesKey("total_quiz_questions")
        private val AUTO_THEME_ENABLED = booleanPreferencesKey("auto_theme_enabled")
        private val THEME_SCHEDULE = stringPreferencesKey("theme_schedule")
        private val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")
        private val USER_ID = stringPreferencesKey("user_id")
        private val AUTHOR_NAME = stringPreferencesKey("author_name")
        private val AUTH_CHOICE_COMPLETED = booleanPreferencesKey("auth_choice_completed")
    }

    val authChoiceCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTH_CHOICE_COMPLETED] ?: false
    }

    suspend fun setAuthChoiceCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_CHOICE_COMPLETED] = completed
        }
    }

    data class ReadingProgress(
        val categoryIndex: Int,
        val pageIndex: Int
    )

    val readingProgress: Flow<ReadingProgress> = context.dataStore.data.map { preferences ->
        ReadingProgress(
            categoryIndex = preferences[LAST_READ_CATEGORY] ?: 0,
            pageIndex = preferences[LAST_READ_PAGE_INDEX] ?: 0
        )
    }

    suspend fun saveReadingProgress(categoryIndex: Int, pageIndex: Int) {
        context.dataStore.edit { preferences ->
            preferences[LAST_READ_CATEGORY] = categoryIndex
            preferences[LAST_READ_PAGE_INDEX] = pageIndex
        }
    }

    val bookmarks: Flow<Set<Int>> = context.dataStore.data.map { preferences ->
        val bookmarksString = preferences[BOOKMARKS] ?: ""
        if (bookmarksString.isEmpty()) {
            emptySet()
        } else {
            bookmarksString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }
    }

    suspend fun addBookmark(pageId: Int) {
        context.dataStore.edit { preferences ->
            val currentBookmarks = preferences[BOOKMARKS] ?: ""
            val bookmarkSet = if (currentBookmarks.isEmpty()) {
                setOf(pageId.toString())
            } else {
                currentBookmarks.split(",").toMutableSet().apply { add(pageId.toString()) }
            }
            preferences[BOOKMARKS] = bookmarkSet.joinToString(",")
        }
    }

    suspend fun removeBookmark(pageId: Int) {
        context.dataStore.edit { preferences ->
            val currentBookmarks = preferences[BOOKMARKS] ?: ""
            val bookmarkSet = currentBookmarks.split(",").toMutableSet()
            bookmarkSet.remove(pageId.toString())
            preferences[BOOKMARKS] = bookmarkSet.joinToString(",")
        }
    }

    suspend fun isBookmarked(pageId: Int): Boolean {
        val bookmarksFlow = context.dataStore.data.map { preferences ->
            preferences[BOOKMARKS] ?: ""
        }
        val currentBookmarks = bookmarksFlow.first()
        return if (currentBookmarks.isEmpty()) {
            false
        } else {
            currentBookmarks.split(",").contains(pageId.toString())
        }
    }

    suspend fun saveNote(pageId: Int, note: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(NOTES_PREFIX + pageId)] = note
        }
    }

    suspend fun getNote(pageId: Int): String {
        return context.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(NOTES_PREFIX + pageId)] ?: ""
        }.first()
    }

    suspend fun deleteNote(pageId: Int) {
        context.dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(NOTES_PREFIX + pageId))
        }
    }

    suspend fun hasNote(pageId: Int): Boolean {
        val note = getNote(pageId)
        return note.isNotEmpty()
    }

    data class Statistics(
        val totalReadingTimeMinutes: Long,
        val totalQuizzesTaken: Int,
        val totalQuizScore: Int,
        val totalQuizQuestions: Int,
        val averageQuizScore: Int
    )

    val statistics: Flow<Statistics> = context.dataStore.data.map { preferences ->
        val readingTime = preferences[TOTAL_READING_TIME] ?: 0L
        val quizzesTaken = preferences[TOTAL_QUIZZES_TAKEN] ?: 0
        val quizScore = preferences[TOTAL_QUIZ_SCORE] ?: 0
        val quizQuestions = preferences[TOTAL_QUIZ_QUESTIONS] ?: 0
        val avgScore = if (quizQuestions > 0) {
            ((quizScore.toFloat() / quizQuestions.toFloat()) * 100).toInt()
        } else {
            0
        }
        Statistics(readingTime, quizzesTaken, quizScore, quizQuestions, avgScore)
    }

    suspend fun addReadingTime(minutes: Long) {
        context.dataStore.edit { preferences ->
            val current = preferences[TOTAL_READING_TIME] ?: 0L
            preferences[TOTAL_READING_TIME] = current + minutes
        }
    }

    suspend fun recordQuizResult(score: Int, totalQuestions: Int) {
        context.dataStore.edit { preferences ->
            val currentQuizzes = preferences[TOTAL_QUIZZES_TAKEN] ?: 0
            val currentScore = preferences[TOTAL_QUIZ_SCORE] ?: 0
            val currentQuestions = preferences[TOTAL_QUIZ_QUESTIONS] ?: 0

            preferences[TOTAL_QUIZZES_TAKEN] = currentQuizzes + 1
            preferences[TOTAL_QUIZ_SCORE] = currentScore + score
            preferences[TOTAL_QUIZ_QUESTIONS] = currentQuestions + totalQuestions
        }
    }

    // Theme Schedule Settings
    val autoThemeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_THEME_ENABLED] ?: true
    }

    suspend fun setAutoThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_THEME_ENABLED] = enabled
        }
    }

    suspend fun saveThemeSchedule(scheduleJson: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_SCHEDULE] = scheduleJson
        }
    }

    val themeSchedule: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_SCHEDULE] ?: ""
    }

    // Cloud Sync Settings
    val cloudSyncEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CLOUD_SYNC_ENABLED] ?: false
    }

    suspend fun setCloudSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CLOUD_SYNC_ENABLED] = enabled
        }
    }

    suspend fun setUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }

    val userId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_ID] ?: ""
    }

    suspend fun setAuthorName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTHOR_NAME] = name
        }
    }

    val authorName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[AUTHOR_NAME] ?: ""
    }

    suspend fun syncToCloud() {
        if (!FirebaseService.isFirebaseAvailable()) return
        val firestore = FirebaseService.getFirestore() ?: return
        val currentUserId = userId.first().ifEmpty { "anonymous_user" }

        val progress = readingProgress.first()
        val bookmarkSet = bookmarks.first().toList()
        val allPages = DocRepository.jniPages + DocRepository.androidRulesPages

        val notesMap = mutableMapOf<String, String>()
        for (page in allPages) {
            val note = getNote(page.id)
            if (note.isNotEmpty()) {
                notesMap[page.id.toString()] = note
            }
        }

        val syncData = mapOf(
            "lastReadCategory" to progress.categoryIndex,
            "lastReadPage" to progress.pageIndex,
            "bookmarks" to bookmarkSet,
            "notes" to notesMap,
            "lastSyncedAt" to System.currentTimeMillis()
        )

        try {
            firestore.collection("user_sync_data")
                .document(currentUserId)
                .set(syncData, SetOptions.merge())
                .awaitTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncFromCloud() {
        if (!FirebaseService.isFirebaseAvailable()) return
        val firestore = FirebaseService.getFirestore() ?: return
        val currentUserId = userId.first().ifEmpty { "anonymous_user" }

        try {
            val snapshot = firestore.collection("user_sync_data")
                .document(currentUserId)
                .get()
                .awaitTask()

            if (snapshot.exists()) {
                val lastCat = snapshot.getLong("lastReadCategory")?.toInt()
                val lastPage = snapshot.getLong("lastReadPage")?.toInt()
                if (lastCat != null && lastPage != null) {
                    saveReadingProgress(lastCat, lastPage)
                }

                @Suppress("UNCHECKED_CAST")
                val cloudBookmarks = snapshot.get("bookmarks") as? List<Long>
                if (cloudBookmarks != null) {
                    for (pageId in cloudBookmarks) {
                        addBookmark(pageId.toInt())
                    }
                }

                @Suppress("UNCHECKED_CAST")
                val cloudNotes = snapshot.get("notes") as? Map<String, String>
                if (cloudNotes != null) {
                    for ((pageIdStr, noteText) in cloudNotes) {
                        pageIdStr.toIntOrNull()?.let { pageId ->
                            saveNote(pageId, noteText)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}