package com.example.myfirstandroidapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstandroidapplication.R
import com.example.myfirstandroidapplication.models.Achievement

class AchievementAdapter(
    private var achievements: List<Achievement>,
    private var theme: ReaderTheme,
    private var profileData: ProfileData? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ACHIEVEMENT = 1
    }

    data class ProfileData(
        val level: Int,
        val xp: Int,
        val xpToNext: Int,
        val streak: Int,
        val progressPercent: Float,
        val unlockedCount: Int,
        val totalCount: Int
    )

    fun updateAchievements(newAchievements: List<Achievement>) {
        achievements = newAchievements
        notifyDataSetChanged()
    }

    fun updateProfileData(data: ProfileData) {
        profileData = data
        notifyItemChanged(0)
    }

    fun setTheme(newTheme: ReaderTheme) {
        theme = newTheme
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ACHIEVEMENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_achievements_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_achievement, parent, false)
                AchievementViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(profileData, theme)
            is AchievementViewHolder -> holder.bind(achievements[position - 1], theme)
        }
    }

    override fun getItemCount(): Int = achievements.size + 1

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardProfile: View = itemView.findViewById(R.id.cardProfile)
        private val tvProgressHeader: TextView = itemView.findViewById(R.id.tvProgressHeader)
        private val tvLevel: TextView = itemView.findViewById(R.id.tvLevel)
        private val tvXP: TextView = itemView.findViewById(R.id.tvXP)
        private val progressXP: View = itemView.findViewById(R.id.progressXP)
        private val tvStreak: TextView = itemView.findViewById(R.id.tvStreak)
        private val tvAchievementsHeader: TextView = itemView.findViewById(R.id.tvAchievementsHeader)
        private val tvUnlockedLabel: TextView = itemView.findViewById(R.id.tvUnlockedLabel)
        private val tvUnlockedCount: TextView = itemView.findViewById(R.id.tvUnlockedCount)
        private val tvTotalLabel: TextView = itemView.findViewById(R.id.tvTotalLabel)
        private val tvTotalCount: TextView = itemView.findViewById(R.id.tvTotalCount)

        fun bind(data: ProfileData?, theme: ReaderTheme) {
            val ctx = itemView.context

            data?.let {
                tvLevel.text = "Level ${it.level}"
                val currentLevelThreshold = (it.level - 1) * 1000
                val xpInCurrentLevel = (it.xp - currentLevelThreshold).coerceAtLeast(0)
                val xpNeededForNextLevel = it.xpToNext - currentLevelThreshold
                tvXP.text = "$xpInCurrentLevel / $xpNeededForNextLevel XP"
                tvStreak.text = "${it.streak} Day Streak"
                tvUnlockedCount.text = it.unlockedCount.toString()
                tvTotalCount.text = it.totalCount.toString()

                progressXP.post {
                    val params = progressXP.layoutParams ?: return@post
                    params.width = (it.progressPercent * 1000).toInt()
                    progressXP.layoutParams = params
                }
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

            val cardBg = when (theme) {
                ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_card)
                ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_card)
                ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_card)
            }

            cardProfile.setBackgroundColor(cardBg)
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

    class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardAchievement: View = itemView.findViewById(R.id.cardAchievement)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivAchievementIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvAchievementTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvAchievementDescription)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvAchievementProgress)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressAchievement)
        private val tvXPReward: TextView = itemView.findViewById(R.id.tvXPReward)
        private val tvUnlocked: TextView = itemView.findViewById(R.id.tvUnlocked)

        fun bind(achievement: Achievement, theme: ReaderTheme) {
            val ctx = itemView.context

            ivIcon.setImageResource(achievement.iconRes)
            tvTitle.text = achievement.title
            tvDescription.text = achievement.description
            tvXPReward.text = "+${achievement.xpReward} XP"

            val progress = achievement.requirement.current.toFloat() / achievement.requirement.target.toFloat()
            progressBar.progress = (progress * 100).toInt()
            tvProgress.text = "${achievement.requirement.current}/${achievement.requirement.target}"

            if (achievement.isUnlocked) {
                tvUnlocked.visibility = View.VISIBLE
                tvUnlocked.text = "✓ Unlocked"
                ivIcon.alpha = 1.0f
            } else {
                tvUnlocked.visibility = View.GONE
                ivIcon.alpha = 0.5f
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

            val cardBg = when (theme) {
                ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_card)
                ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_card)
                ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_card)
            }

            cardAchievement.setBackgroundColor(cardBg)
            tvTitle.setTextColor(textColor)
            tvDescription.setTextColor(subtextColor)
            tvProgress.setTextColor(subtextColor)
            ivIcon.setColorFilter(textColor)
        }
    }
}