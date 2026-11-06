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
    private var theme: ReaderTheme
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    fun updateAchievements(newAchievements: List<Achievement>) {
        achievements = newAchievements
        notifyDataSetChanged()
    }

    fun setTheme(newTheme: ReaderTheme) {
        theme = newTheme
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position], theme)
    }

    override fun getItemCount(): Int = achievements.size

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

            // Apply theme colors
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