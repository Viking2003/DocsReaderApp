package com.example.myfirstandroidapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reader_preferences")

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
}
