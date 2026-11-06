package com.example.myfirstandroidapplication.models

data class CloudSyncData(
    val userId: String,
    val lastSyncTimestamp: Long,
    val readingProgress: ReadingProgressData,
    val bookmarks: List<Int>,
    val notes: Map<Int, String>,
    val quizProgress: QuizProgressData,
    val userProfile: UserProfile
)

data class ReadingProgressData(
    val categoryIndex: Int,
    val pageIndex: Int,
    val totalReadingTimeMinutes: Long
)

data class QuizProgressData(
    val completedQuizzes: List<Int>,
    val averageScore: Float,
    val totalQuizzesTaken: Int
)
