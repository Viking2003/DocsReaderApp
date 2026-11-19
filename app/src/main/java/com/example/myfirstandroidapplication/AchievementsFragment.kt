package com.example.myfirstandroidapplication

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstandroidapplication.adapters.AchievementAdapter
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.example.myfirstandroidapplication.data.AchievementManager
import kotlinx.coroutines.launch

class AchievementsFragment : Fragment(R.layout.fragment_achievements) {

    private lateinit var achievementManager: AchievementManager
    private lateinit var achievementAdapter: AchievementAdapter
    private var currentTheme: ReaderTheme = ReaderTheme.SEPIA

    private lateinit var rvAchievements: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        achievementManager = AchievementManager.getInstance(requireContext())
        initViews(view)
        setupRecyclerView()
        observeData()

        (activity as? MainActivity)?.let {
            currentTheme = it.currentTheme
        }
        applyTheme(currentTheme)
    }

    private fun initViews(view: View) {
        rvAchievements = view.findViewById(R.id.rvAchievements)
    }

    private fun setupRecyclerView() {
        achievementAdapter = AchievementAdapter(emptyList(), currentTheme)
        rvAchievements.layoutManager = LinearLayoutManager(requireContext())
        rvAchievements.adapter = achievementAdapter
    }

    private fun observeData() {
        lifecycleScope.launch {
            achievementManager.userProfile.collect { profile ->
                val profileData = AchievementAdapter.ProfileData(
                    level = profile.level,
                    xp = profile.xp,
                    xpToNext = profile.xpToNextLevel(),
                    streak = profile.currentStreak,
                    progressPercent = profile.progressToNextLevel(),
                    unlockedCount = 0,
                    totalCount = 0
                )
                achievementAdapter.updateProfileData(profileData)
            }
        }

        lifecycleScope.launch {
            achievementManager.achievements.collect { achievements ->
                achievementAdapter.updateAchievements(achievements)

                val unlocked = achievements.count { it.isUnlocked }
                val profile = achievementManager.userProfile.value
                val profileData = AchievementAdapter.ProfileData(
                    level = profile.level,
                    xp = profile.xp,
                    xpToNext = profile.xpToNextLevel(),
                    streak = profile.currentStreak,
                    progressPercent = profile.progressToNextLevel(),
                    unlockedCount = unlocked,
                    totalCount = achievements.size
                )
                achievementAdapter.updateProfileData(profileData)
            }
        }

        lifecycleScope.launch {
            achievementManager.dailyStreak.collect { streak ->
                val profile = achievementManager.userProfile.value
                val profileData = AchievementAdapter.ProfileData(
                    level = profile.level,
                    xp = profile.xp,
                    xpToNext = profile.xpToNextLevel(),
                    streak = streak,
                    progressPercent = profile.progressToNextLevel(),
                    unlockedCount = achievementManager.achievements.value.count { it.isUnlocked },
                    totalCount = achievementManager.achievements.value.size
                )
                achievementAdapter.updateProfileData(profileData)
            }
        }
    }

    fun applyTheme(theme: ReaderTheme) {
        currentTheme = theme
        if (::achievementAdapter.isInitialized) {
            achievementAdapter.setTheme(theme)
        }

        val ctx = context ?: return
        val root = view ?: return

        val bgRes = when (theme) {
            ReaderTheme.SEPIA -> R.color.kindle_sepia_bg
            ReaderTheme.LIGHT -> R.color.kindle_light_bg
            ReaderTheme.DARK -> R.color.kindle_dark_bg
        }

        root.setBackgroundColor(ContextCompat.getColor(ctx, bgRes))
    }
}