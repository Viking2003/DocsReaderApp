package com.example.myfirstandroidapplication.data

import android.R
import android.content.Context
import android.content.SharedPreferences
import com.example.myfirstandroidapplication.models.Achievement
import com.example.myfirstandroidapplication.models.AchievementRequirement
import com.example.myfirstandroidapplication.models.AchievementType
import com.example.myfirstandroidapplication.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AchievementManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("achievements_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: AchievementManager? = null

        fun getInstance(context: Context): AchievementManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AchievementManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val _userProfile = MutableStateFlow<UserProfile>(loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(loadAchievements())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _dailyStreak = MutableStateFlow(loadUserProfile().currentStreak)
    val dailyStreak: StateFlow<Int> = _dailyStreak.asStateFlow()

    private fun loadUserProfile(): UserProfile {
        val xp = prefs.getInt("user_xp", 0)
        val level = calculateLevel(xp)
        val quizzes = prefs.getInt("quizzes_completed", 0)
        val notes = prefs.getInt("notes_created", 0)
        val bookmarks = prefs.getInt("bookmarks_created", 0)
        val readingMinutes = prefs.getLong("reading_minutes", 0L)
        val streak = prefs.getInt("daily_streak", 1)

        return UserProfile(
            userId = "local_user",
            displayName = "Reader",
            level = level,
            xp = xp,
            currentStreak = streak,
            longestStreak = streak,
            totalReadingTimeMinutes = readingMinutes,
            totalQuizzesCompleted = quizzes,
            totalBookmarks = bookmarks,
            totalNotes = notes
        )
    }

    private fun saveUserProfile(profile: UserProfile) {
        prefs.edit()
            .putInt("user_xp", profile.xp)
            .putInt("quizzes_completed", profile.totalQuizzesCompleted)
            .putInt("notes_created", profile.totalNotes)
            .putInt("bookmarks_created", profile.totalBookmarks)
            .putLong("reading_minutes", profile.totalReadingTimeMinutes)
            .putInt("daily_streak", profile.currentStreak)
            .apply()
    }

    private fun loadAchievements(): List<Achievement> {
        val unlockedIds = prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()
        val baseList = initializeAchievements()

        val pagesRead = (prefs.getLong("reading_minutes", 0L) / 5).toInt().coerceAtLeast(1)
        val quizzes = prefs.getInt("quizzes_completed", 0)
        val perfectQuizzes = prefs.getInt("perfect_quizzes", 0)
        val bookmarks = prefs.getInt("bookmarks_created", 0)
        val notes = prefs.getInt("notes_created", 0)
        val streak = prefs.getInt("daily_streak", 1)

        return baseList.map { achievement ->
            val currentValue = when (achievement.requirement.type) {
                "pages_read" -> pagesRead
                "quizzes_completed" -> quizzes
                "perfect_quizzes" -> perfectQuizzes
                "bookmarks_created" -> bookmarks
                "notes_created" -> notes
                "daily_streak" -> streak
                else -> 0
            }
            val isUnlocked = unlockedIds.contains(achievement.id) || currentValue >= achievement.requirement.target
            if (isUnlocked && !unlockedIds.contains(achievement.id)) {
                saveUnlockedAchievement(achievement.id)
            }
            achievement.copy(
                isUnlocked = isUnlocked,
                requirement = achievement.requirement.copy(current = currentValue)
            )
        }
    }

    private fun saveUnlockedAchievement(id: String) {
        val currentSet = prefs.getStringSet("unlocked_achievements", emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(id)
        prefs.edit().putStringSet("unlocked_achievements", currentSet).apply()
    }

    private fun initializeAchievements(): List<Achievement> {
        return listOf(
            Achievement(
                id = "first_read",
                title = "First Steps",
                description = "Read your first page",
                type = AchievementType.READING,
                iconRes = R.drawable.ic_menu_info_details,
                xpReward = 50,
                requirement = AchievementRequirement("pages_read", 1, 0)
            ),
            Achievement(
                id = "page_master_10",
                title = "Page Master",
                description = "Read 10 pages",
                type = AchievementType.READING,
                iconRes = R.drawable.ic_menu_agenda,
                xpReward = 100,
                requirement = AchievementRequirement("pages_read", 10, 0)
            ),
            Achievement(
                id = "page_master_50",
                title = "Avid Reader",
                description = "Read 50 pages",
                type = AchievementType.READING,
                iconRes = R.drawable.ic_menu_agenda,
                xpReward = 500,
                requirement = AchievementRequirement("pages_read", 50, 0)
            ),
            Achievement(
                id = "first_quiz",
                title = "Quiz Novice",
                description = "Complete your first quiz",
                type = AchievementType.QUIZ,
                iconRes = R.drawable.ic_menu_edit,
                xpReward = 75,
                requirement = AchievementRequirement("quizzes_completed", 1, 0)
            ),
            Achievement(
                id = "quiz_master_10",
                title = "Quiz Master",
                description = "Complete 10 quizzes",
                type = AchievementType.QUIZ,
                iconRes = R.drawable.ic_menu_sort_alphabetically,
                xpReward = 300,
                requirement = AchievementRequirement("quizzes_completed", 10, 0)
            ),
            Achievement(
                id = "perfect_score",
                title = "Perfect Score",
                description = "Get 100% on a quiz",
                type = AchievementType.QUIZ,
                iconRes = R.drawable.ic_menu_add,
                xpReward = 200,
                requirement = AchievementRequirement("perfect_quizzes", 1, 0)
            ),
            Achievement(
                id = "first_bookmark",
                title = "Bookmarker",
                description = "Create your first bookmark",
                type = AchievementType.BOOKMARK,
                iconRes = R.drawable.ic_menu_add,
                xpReward = 25,
                requirement = AchievementRequirement("bookmarks_created", 1, 0)
            ),
            Achievement(
                id = "bookmark_collector",
                title = "Bookmark Collector",
                description = "Create 10 bookmarks",
                type = AchievementType.BOOKMARK,
                iconRes = R.drawable.ic_menu_myplaces,
                xpReward = 150,
                requirement = AchievementRequirement("bookmarks_created", 10, 0)
            ),
            Achievement(
                id = "first_note",
                title = "Note Taker",
                description = "Write your first note",
                type = AchievementType.NOTE,
                iconRes = R.drawable.ic_menu_call,
                xpReward = 25,
                requirement = AchievementRequirement("notes_created", 1, 0)
            ),
            Achievement(
                id = "note_writer",
                title = "Prolific Writer",
                description = "Write 10 notes",
                type = AchievementType.NOTE,
                iconRes = R.drawable.ic_menu_edit,
                xpReward = 150,
                requirement = AchievementRequirement("notes_created", 10, 0)
            ),
            Achievement(
                id = "streak_3",
                title = "3-Day Streak",
                description = "Use the app for 3 consecutive days",
                type = AchievementType.STREAK,
                iconRes = R.drawable.ic_menu_recent_history,
                xpReward = 100,
                requirement = AchievementRequirement("daily_streak", 3, 0)
            ),
            Achievement(
                id = "streak_7",
                title = "Week Warrior",
                description = "Use the app for 7 consecutive days",
                type = AchievementType.STREAK,
                iconRes = R.drawable.ic_menu_week,
                xpReward = 300,
                requirement = AchievementRequirement("daily_streak", 7, 0)
            ),
            Achievement(
                id = "explorer",
                title = "Explorer",
                description = "Read from all categories",
                type = AchievementType.EXPLORER,
                iconRes = R.drawable.ic_menu_compass,
                xpReward = 200,
                requirement = AchievementRequirement("categories_explored", 2, 0)
            )
        )
    }

    fun trackPageRead() {
        val currentProfile = _userProfile.value
        val newMinutes = currentProfile.totalReadingTimeMinutes + 5
        val pagesRead = (newMinutes / 5).toInt()

        val updatedProfile = currentProfile.copy(totalReadingTimeMinutes = newMinutes)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)

        checkAchievement("pages_read", pagesRead)
        addXP(10)
    }

    fun trackQuizCompleted(scorePercent: Int) {
        val currentProfile = _userProfile.value
        val newQuizzes = currentProfile.totalQuizzesCompleted + 1

        val updatedProfile = currentProfile.copy(totalQuizzesCompleted = newQuizzes)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)

        checkAchievement("quizzes_completed", newQuizzes)

        if (scorePercent >= 100) {
            val currentPerfect = prefs.getInt("perfect_quizzes", 0) + 1
            prefs.edit().putInt("perfect_quizzes", currentPerfect).apply()
            checkAchievement("perfect_quizzes", currentPerfect)
        }

        // Award XP based on quiz score (e.g. 100 XP for 100%, 80 XP for 80%)
        addXP(scorePercent)
    }

    fun trackBookmarkCreated() {
        val currentProfile = _userProfile.value
        val newBookmarks = currentProfile.totalBookmarks + 1

        val updatedProfile = currentProfile.copy(totalBookmarks = newBookmarks)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)

        checkAchievement("bookmarks_created", newBookmarks)
        addXP(25)
    }

    fun trackNoteCreated() {
        val currentProfile = _userProfile.value
        val newNotes = currentProfile.totalNotes + 1

        val updatedProfile = currentProfile.copy(totalNotes = newNotes)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)

        checkAchievement("notes_created", newNotes)
        addXP(25)
    }

    private fun checkAchievement(requirementType: String, currentValue: Int) {
        val updatedAchievements = _achievements.value.map { achievement ->
            if (achievement.requirement.type == requirementType) {
                val shouldUnlock = !achievement.isUnlocked && currentValue >= achievement.requirement.target
                if (shouldUnlock) {
                    saveUnlockedAchievement(achievement.id)
                    addXP(achievement.xpReward)
                    achievement.copy(
                        isUnlocked = true,
                        unlockedAt = System.currentTimeMillis(),
                        requirement = achievement.requirement.copy(current = currentValue)
                    )
                } else {
                    achievement.copy(
                        requirement = achievement.requirement.copy(current = currentValue)
                    )
                }
            } else {
                achievement
            }
        }

        _achievements.value = updatedAchievements
    }

    private fun addXP(amount: Int) {
        val currentProfile = _userProfile.value
        val newXP = currentProfile.xp + amount
        val newLevel = calculateLevel(newXP)

        val updatedProfile = currentProfile.copy(
            xp = newXP,
            level = newLevel
        )
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }

    private fun calculateLevel(xp: Int): Int {
        var level = 1
        var xpNeeded = 1000
        var remainingXP = xp

        while (remainingXP >= xpNeeded) {
            remainingXP -= xpNeeded
            level++
            xpNeeded = level * 1000
        }

        return level
    }
}