package com.example.myfirstandroidapplication.models

enum class AchievementType(val displayName: String) {
    READING("Reading"),
    QUIZ("Quiz Master"),
    BOOKMARK("Bookmarker"),
    NOTE("Note Taker"),
    STREAK("Streak Master"),
    EXPLORER("Explorer")
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val type: AchievementType,
    val iconRes: Int,
    val xpReward: Int,
    val requirement: AchievementRequirement,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

data class AchievementRequirement(
    val type: String, // "pages_read", "quizzes_completed", "bookmarks_created", etc.
    val target: Int,
    val current: Int = 0
)
