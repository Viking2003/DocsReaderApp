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
        return level * 1000
    }

    fun progressToNextLevel(): Float {
        return (xp.toFloat() / xpToNextLevel().toFloat()).coerceAtMost(1f)
    }
}
