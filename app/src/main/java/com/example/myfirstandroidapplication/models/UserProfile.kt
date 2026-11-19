package com.example.myfirstandroidapplication.models

data class UserProfile(
    val userId: String,
    val displayName: String,
    val level: Int = 1,
    val xp: Int = 0,
    val totalReadingTimeMinutes: Long = 0,
    val totalQuizzesCompleted: Int = 0,
    val totalBookmarks: Int = 0,
    val totalNotes: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: Long? = null,
    val unlockedAchievements: List<String> = emptyList(),
    val cloudSyncEnabled: Boolean = false
) {
    fun xpToNextLevel(): Int {
        return (level + 1) * 1000
    }

    fun xpForCurrentLevel(): Int {
        return (level - 1) * 1000
    }

    fun progressToNextLevel(): Float {
        val currentLevelThreshold = (level - 1) * 1000
        val nextLevelThreshold = level * 1000
        val xpInRange = nextLevelThreshold - currentLevelThreshold
        val xpProgress = (xp - currentLevelThreshold).coerceAtLeast(0).coerceAtMost(xpInRange)
        return if (xpInRange > 0) {
            (xpProgress.toFloat() / xpInRange.toFloat()).coerceAtMost(1f)
        } else {
            0f
        }
    }
}
