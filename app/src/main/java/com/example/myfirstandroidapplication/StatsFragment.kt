package com.example.myfirstandroidapplication

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.example.myfirstandroidapplication.data.PreferencesManager
import kotlinx.coroutines.launch

class StatsFragment : Fragment(R.layout.fragment_stats) {

    private var statsRootContainer: View? = null
    private var tvStatsTitle: TextView? = null
    private var cardReadingTime: View? = null
    private var tvReadingTimeLabel: TextView? = null
    private var tvTotalReadingTime: TextView? = null
    private var cardQuizzes: View? = null
    private var tvQuizzesLabel: TextView? = null
    private var tvTotalQuizzes: TextView? = null
    private var cardAverageScore: View? = null
    private var tvAverageScoreLabel: TextView? = null
    private var tvAverageScore: TextView? = null
    private var cardBookmarks: View? = null
    private var tvBookmarksLabel: TextView? = null
    private var tvTotalBookmarks: TextView? = null
    private var cardNotes: View? = null
    private var tvNotesLabel: TextView? = null
    private var tvTotalNotes: TextView? = null

    private lateinit var preferencesManager: PreferencesManager
    var currentTheme: ReaderTheme = ReaderTheme.SEPIA

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        initViews(view)

        // Sync with MainActivity's current theme
        (activity as? MainActivity)?.let {
            currentTheme = it.currentTheme
        }

        applyTheme(currentTheme)
        loadStatistics()
    }

    private fun initViews(view: View) {
        statsRootContainer = view.findViewById(R.id.statsRootContainer)
        tvStatsTitle = view.findViewById(R.id.tvStatsTitle)
        cardReadingTime = view.findViewById(R.id.cardReadingTime)
        tvReadingTimeLabel = view.findViewById(R.id.tvReadingTimeLabel)
        tvTotalReadingTime = view.findViewById(R.id.tvTotalReadingTime)
        cardQuizzes = view.findViewById(R.id.cardQuizzes)
        tvQuizzesLabel = view.findViewById(R.id.tvQuizzesLabel)
        tvTotalQuizzes = view.findViewById(R.id.tvTotalQuizzes)
        cardAverageScore = view.findViewById(R.id.cardAverageScore)
        tvAverageScoreLabel = view.findViewById(R.id.tvAverageScoreLabel)
        tvAverageScore = view.findViewById(R.id.tvAverageScore)
        cardBookmarks = view.findViewById(R.id.cardBookmarks)
        tvBookmarksLabel = view.findViewById(R.id.tvBookmarksLabel)
        tvTotalBookmarks = view.findViewById(R.id.tvTotalBookmarks)
        cardNotes = view.findViewById(R.id.cardNotes)
        tvNotesLabel = view.findViewById(R.id.tvNotesLabel)
        tvTotalNotes = view.findViewById(R.id.tvTotalNotes)
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            try {
                preferencesManager.statistics.collect { stats ->
                    tvTotalReadingTime?.text = formatReadingTime(stats.totalReadingTimeMinutes)
                    tvTotalQuizzes?.text = "${stats.totalQuizzesTaken} quizzes completed"
                    tvAverageScore?.text = "${stats.averageQuizScore}%"
                }
            } catch (e: Exception) {
                // Handle safely
            }
        }

        lifecycleScope.launch {
            try {
                preferencesManager.bookmarks.collect { bookmarks ->
                    tvTotalBookmarks?.text = "${bookmarks.size} pages bookmarked"
                }
            } catch (e: Exception) {
                // Handle safely
            }
        }

        lifecycleScope.launch {
            try {
                var noteCount = 0
                for (i in 1..10) {
                    if (preferencesManager.hasNote(i)) {
                        noteCount++
                    }
                }
                tvTotalNotes?.text = "$noteCount notes taken"
            } catch (e: Exception) {
                tvTotalNotes?.text = "0 notes taken"
            }
        }
    }

    private fun formatReadingTime(minutes: Long): String {
        return when {
            minutes < 60 -> "$minutes min"
            minutes < 1440 -> "${minutes / 60}h ${minutes % 60}m"
            else -> "${minutes / 1440}d ${(minutes % 1440) / 60}h"
        }
    }

    fun applyTheme(theme: ReaderTheme) {
        currentTheme = theme
        val ctx = context ?: return
        val root = statsRootContainer ?: return

        val bgRes = when (theme) {
            ReaderTheme.SEPIA -> R.color.kindle_sepia_bg
            ReaderTheme.LIGHT -> R.color.kindle_light_bg
            ReaderTheme.DARK -> R.color.kindle_dark_bg
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

        val cardBgRes = when (theme) {
            ReaderTheme.SEPIA -> R.drawable.bg_callout_box_sepia
            ReaderTheme.LIGHT -> R.drawable.bg_callout_box_light
            ReaderTheme.DARK -> R.drawable.bg_callout_box_dark
        }

        root.setBackgroundColor(ContextCompat.getColor(ctx, bgRes))
        tvStatsTitle?.setTextColor(textColor)
        tvReadingTimeLabel?.setTextColor(subtextColor)
        tvTotalReadingTime?.setTextColor(textColor)
        tvQuizzesLabel?.setTextColor(subtextColor)
        tvTotalQuizzes?.setTextColor(textColor)
        tvAverageScoreLabel?.setTextColor(subtextColor)
        tvAverageScore?.setTextColor(textColor)
        tvBookmarksLabel?.setTextColor(subtextColor)
        tvTotalBookmarks?.setTextColor(textColor)
        tvNotesLabel?.setTextColor(subtextColor)
        tvTotalNotes?.setTextColor(textColor)

        // Update card backgrounds
        cardReadingTime?.setBackgroundResource(cardBgRes)
        cardQuizzes?.setBackgroundResource(cardBgRes)
        cardAverageScore?.setBackgroundResource(cardBgRes)
        cardBookmarks?.setBackgroundResource(cardBgRes)
        cardNotes?.setBackgroundResource(cardBgRes)
    }
}