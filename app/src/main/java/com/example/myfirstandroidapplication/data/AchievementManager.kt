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
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AchievementManager private constructor(context: Context) {

    private val context = context.applicationContext
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

    init {
        // Sync disabled - XP is added immediately when activities happen
        // syncXPWithAchievements()
    }

    private fun loadUserProfile(): UserProfile {
        val xp = prefs.getInt("user_xp", 0)
        val level = calculateLevel(xp)
        val quizzes = prefs.getInt("quizzes_completed", 0)
        val notes = prefs.getInt("notes_created", 0)
        val bookmarks = prefs.getInt("bookmarks_created", 0)
        val readingMinutes = prefs.getLong("reading_minutes", 0L)
        val streak = prefs.getInt("daily_streak", 1)
        val longestStreak = prefs.getInt("longest_streak", 1)
        val lastActiveDate = prefs.getLong("last_active_date", 0L).takeIf { it > 0L }

        return UserProfile(
            userId = "local_user",
            displayName = "Reader",
            level = level,
            xp = xp,
            currentStreak = streak,
            longestStreak = longestStreak,
            totalReadingTimeMinutes = readingMinutes,
            totalQuizzesCompleted = quizzes,
            totalBookmarks = bookmarks,
            totalNotes = notes,
            lastActiveDate = lastActiveDate
        )
    }

    fun syncXPWithAchievements() {
        val currentProfile = _userProfile.value
        val unlockedIds = prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()
        val achievements = loadAchievements()

        var calculatedXP = 0

        // XP from achievements ONLY (activities already add XP when they happen)
        // We only add achievement XP to catch cases where achievements were unlocked
        // but the XP wasn't awarded (e.g., app crash during unlock)
        for (achievement in achievements) {
            if (achievement.isUnlocked) {
                calculatedXP += achievement.xpReward
            }
        }

        android.util.Log.d("AchievementManager", "XP Sync - Current: ${currentProfile.xp}, Calculated from achievements: $calculatedXP, Unlocked: ${achievements.count { it.isUnlocked }}")

        // Only update if stored XP is less than calculated (prevent overwriting legitimate XP)
        if (currentProfile.xp < calculatedXP) {
            val newLevel = calculateLevel(calculatedXP)
            val updatedProfile = currentProfile.copy(
                xp = calculatedXP,
                level = newLevel
            )
            _userProfile.value = updatedProfile
            saveUserProfile(updatedProfile)
            android.util.Log.d("AchievementManager", "XP updated from ${currentProfile.xp} to $calculatedXP")
        } else {
            android.util.Log.d("AchievementManager", "XP not updated (current XP is already correct or higher)")
        }
    }

    private fun saveUserProfile(profile: UserProfile) {
        prefs.edit()
            .putInt("user_xp", profile.xp)
            .putInt("quizzes_completed", profile.totalQuizzesCompleted)
            .putInt("notes_created", profile.totalNotes)
            .putInt("bookmarks_created", profile.totalBookmarks)
            .putLong("reading_minutes", profile.totalReadingTimeMinutes)
            .putInt("daily_streak", profile.currentStreak)
            .putInt("longest_streak", profile.longestStreak)
            .putLong("last_active_date", profile.lastActiveDate ?: 0L)
            .apply()
    }

    private fun loadAchievements(): List<Achievement> {
        val unlockedIds = prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()
        val baseList = initializeAchievements()

        val pagesRead = (prefs.getLong("reading_minutes", 0L) / 5).toInt()
        val quizzes = prefs.getInt("quizzes_completed", 0)
        val perfectQuizzes = prefs.getInt("perfect_quizzes", 0)
        val bookmarks = prefs.getInt("bookmarks_created", 0)
        val notes = prefs.getInt("notes_created", 0)
        val streak = prefs.getInt("daily_streak", 1)
        val categoriesExplored = prefs.getStringSet("categories_explored", emptySet())?.size ?: 0

        return baseList.map { achievement ->
            val currentValue = when (achievement.requirement.type) {
                "pages_read" -> pagesRead
                "quizzes_completed" -> quizzes
                "perfect_quizzes" -> perfectQuizzes
                "bookmarks_created" -> bookmarks
                "notes_created" -> notes
                "daily_streak" -> streak
                "categories_explored" -> categoriesExplored
                else -> 0
            }
            // Only unlock if explicitly saved in unlocked_achievements
            // Auto-unlocking via currentValue is disabled to ensure XP and toast are always shown
            val isUnlocked = unlockedIds.contains(achievement.id)
            achievement.copy(
                isUnlocked = isUnlocked,
                requirement = achievement.requirement.copy(current = currentValue.coerceAtMost(achievement.requirement.target))
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
                requirement = AchievementRequirement("categories_explored", 3, 0)
            )
        )
    }

    fun trackPageRead(minutes: Int = 5) {
        val currentProfile = _userProfile.value
        val newMinutes = currentProfile.totalReadingTimeMinutes + minutes
        val pagesRead = (newMinutes / 5).toInt()

        val updatedProfile = currentProfile.copy(totalReadingTimeMinutes = newMinutes)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)

        checkAchievement("pages_read", pagesRead)
        addXP(10, "Reading progress")
    }

    fun trackCategoryExplored(categoryName: String) {
        val currentSet = prefs.getStringSet("categories_explored", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (!currentSet.contains(categoryName)) {
            currentSet.add(categoryName)
            prefs.edit().putStringSet("categories_explored", currentSet).apply()
            checkAchievement("categories_explored", currentSet.size)
        }
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
        addXP(scorePercent, "Quiz completed ($scorePercent%)")
    }

    fun trackBookmarkCreated() {
        val currentProfile = _userProfile.value
        val newBookmarks = currentProfile.totalBookmarks + 1

        val updatedProfile = currentProfile.copy(totalBookmarks = newBookmarks)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)

        checkAchievement("bookmarks_created", newBookmarks)
        addXP(25, "Bookmark created")
    }

    fun trackNoteCreated() {
        val currentProfile = _userProfile.value
        val newNotes = currentProfile.totalNotes + 1

        val updatedProfile = currentProfile.copy(totalNotes = newNotes)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)

        checkAchievement("notes_created", newNotes)
        addXP(25, "Note created")
    }

    fun trackDailyOpen() {
        val currentProfile = _userProfile.value
        val currentTime = System.currentTimeMillis()
        val lastActiveTime = currentProfile.lastActiveDate

        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        val lastCalendar = Calendar.getInstance()
        lastActiveTime?.let {
            lastCalendar.timeInMillis = it
        }
        val lastDay = lastCalendar.get(Calendar.DAY_OF_YEAR)
        val lastYear = lastCalendar.get(Calendar.YEAR)

        val newStreak = when {
            lastActiveTime == null -> {
                1
            }
            (currentYear == lastYear && today == lastDay) -> {
                currentProfile.currentStreak
            }
            isConsecutiveDay(lastCalendar, calendar) -> {
                currentProfile.currentStreak + 1
            }
            else -> {
                1
            }
        }

        val newLongestStreak = maxOf(newStreak, currentProfile.longestStreak)

        val updatedProfile = currentProfile.copy(
            currentStreak = newStreak,
            longestStreak = newLongestStreak,
            lastActiveDate = currentTime
        )
        _userProfile.value = updatedProfile
        _dailyStreak.value = newStreak
        saveUserProfile(updatedProfile)

        checkAchievement("daily_streak", newStreak)
    }

    private fun isConsecutiveDay(lastCalendar: Calendar, currentCalendar: Calendar): Boolean {
        val diffMillis = currentCalendar.timeInMillis - lastCalendar.timeInMillis
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
        return diffDays == 1L
    }

    private fun checkAchievement(requirementType: String, currentValue: Int) {
        val updatedAchievements = _achievements.value.map { achievement ->
            if (achievement.requirement.type == requirementType) {
                val shouldUnlock = !achievement.isUnlocked && currentValue >= achievement.requirement.target
                if (shouldUnlock) {
                    saveUnlockedAchievement(achievement.id)
                    addXP(achievement.xpReward, "Achievement: ${achievement.title}")
                    achievement.copy(
                        isUnlocked = true,
                        unlockedAt = System.currentTimeMillis(),
                        requirement = achievement.requirement.copy(current = currentValue.coerceAtMost(achievement.requirement.target))
                    )
                } else {
                    achievement.copy(
                        requirement = achievement.requirement.copy(current = currentValue.coerceAtMost(achievement.requirement.target))
                    )
                }
            } else {
                achievement
            }
        }

        _achievements.value = updatedAchievements
    }

    private fun addXP(amount: Int, reason: String = "") {
        val currentProfile = _userProfile.value
        val newXP = currentProfile.xp + amount
        val newLevel = calculateLevel(newXP)

        val updatedProfile = currentProfile.copy(
            xp = newXP,
            level = newLevel
        )
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)

        // Show toast notification
        val message = if (reason.isNotEmpty()) {
            "+$amount XP: $reason"
        } else {
            "+$amount XP"
        }
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun calculateLevel(xp: Int): Int {
        var level = 1
        var xpNeeded = 1000
        var remainingXP = xp

        while (remainingXP >= xpNeeded) {
            remainingXP -= xpNeeded
            level++
            xpNeeded = 1000
        }

        return level
    }
}