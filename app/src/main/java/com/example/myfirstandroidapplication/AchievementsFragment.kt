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

    private var achievementsScrollView: View? = null
    private var cardProfile: View? = null
    private lateinit var tvProgressHeader: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvXP: TextView
    private lateinit var progressXP: View
    private lateinit var tvStreak: TextView
    private lateinit var rvAchievements: RecyclerView
    private lateinit var tvAchievementsHeader: TextView
    private lateinit var tvUnlockedLabel: TextView
    private lateinit var tvUnlockedCount: TextView
    private lateinit var tvTotalLabel: TextView
    private lateinit var tvTotalCount: TextView

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
        achievementsScrollView = view.findViewById(R.id.achievementsScrollView)
        cardProfile = view.findViewById(R.id.cardProfile)
        tvProgressHeader = view.findViewById(R.id.tvProgressHeader)
        tvLevel = view.findViewById(R.id.tvLevel)
        tvXP = view.findViewById(R.id.tvXP)
        progressXP = view.findViewById(R.id.progressXP)
        tvStreak = view.findViewById(R.id.tvStreak)
        rvAchievements = view.findViewById(R.id.rvAchievements)
        tvAchievementsHeader = view.findViewById(R.id.tvAchievementsHeader)
        tvUnlockedLabel = view.findViewById(R.id.tvUnlockedLabel)
        tvUnlockedCount = view.findViewById(R.id.tvUnlockedCount)
        tvTotalLabel = view.findViewById(R.id.tvTotalLabel)
        tvTotalCount = view.findViewById(R.id.tvTotalCount)
    }

    private fun setupRecyclerView() {
        achievementAdapter = AchievementAdapter(emptyList(), currentTheme)
        rvAchievements.layoutManager = LinearLayoutManager(requireContext())
        rvAchievements.isNestedScrollingEnabled = false
        rvAchievements.adapter = achievementAdapter
    }

    private fun observeData() {
        lifecycleScope.launch {
            achievementManager.userProfile.collect { profile ->
                tvLevel.text = "Level ${profile.level}"
                tvXP.text = "${profile.xp} / ${profile.xpToNextLevel()} XP"
                tvStreak.text = "${profile.currentStreak} Day Streak"

                // Update progress bar safely
                progressXP.post {
                    val params = progressXP.layoutParams ?: return@post
                    params.width = (profile.progressToNextLevel() * 1000).toInt()
                    progressXP.layoutParams = params
                }
            }
        }

        lifecycleScope.launch {
            achievementManager.achievements.collect { achievements ->
                achievementAdapter.updateAchievements(achievements)

                val unlocked = achievements.count { it.isUnlocked }
                tvUnlockedCount.text = unlocked.toString()
                tvTotalCount.text = achievements.size.toString()
            }
        }

        lifecycleScope.launch {
            achievementManager.dailyStreak.collect { streak ->
                tvStreak.text = "$streak Day Streak"
            }
        }
    }

    fun applyTheme(theme: ReaderTheme) {
        currentTheme = theme
        if (::achievementAdapter.isInitialized) {
            achievementAdapter.setTheme(theme)
        }

        val ctx = context ?: return
        val root = achievementsScrollView ?: return

        val bgRes = when (theme) {
            ReaderTheme.SEPIA -> R.color.kindle_sepia_bg
            ReaderTheme.LIGHT -> R.color.kindle_light_bg
            ReaderTheme.DARK -> R.color.kindle_dark_bg
        }

        val cardBg = when (theme) {
            ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_card)
            ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_card)
            ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_card)
        }

        val textColor = when (theme) {
            ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_text)
            ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_text)
            ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_text)
        }

        val subtextColor = when (theme) {
            ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_subtext)
            ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_subtext)
            ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_subtext)
        }

        root.setBackgroundColor(ContextCompat.getColor(ctx, bgRes))
        cardProfile?.setBackgroundColor(cardBg)

        if (::tvProgressHeader.isInitialized) {
            tvProgressHeader.setTextColor(textColor)
            tvLevel.setTextColor(textColor)
            tvXP.setTextColor(subtextColor)
            tvAchievementsHeader.setTextColor(textColor)
            tvUnlockedLabel.setTextColor(subtextColor)
            tvUnlockedCount.setTextColor(textColor)
            tvTotalLabel.setTextColor(subtextColor)
            tvTotalCount.setTextColor(subtextColor)
        }
    }
}