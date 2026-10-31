package com.example.myfirstandroidapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var rootContainer: LinearLayout
    lateinit var mainNavTabs: TabLayout
    private lateinit var btnFontDecrease: ImageButton
    private lateinit var btnFontIncrease: ImageButton
    private lateinit var btnThemeToggle: ImageButton

    var currentTheme: ReaderTheme = ReaderTheme.SEPIA
    private var currentFontSizeSp: Float = 16f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupHeaderListeners()
        setupMainNavTabs()

        // Auto-switch theme based on time of day
        applyAutoTheme()
    }

    private fun applyAutoTheme() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val theme = when {
            hour >= 19 || hour < 7 -> ReaderTheme.DARK // 7 PM to 7 AM
            hour >= 7 && hour < 9 -> ReaderTheme.LIGHT // 7 AM to 9 AM
            else -> ReaderTheme.SEPIA // 9 AM to 7 PM
        }
        applyTheme(theme, showToast = false)
    }

    private fun initViews() {
        rootContainer = findViewById(R.id.rootContainer)
        mainNavTabs = findViewById(R.id.mainNavTabs)
        btnFontDecrease = findViewById(R.id.btnFontDecrease)
        btnFontIncrease = findViewById(R.id.btnFontIncrease)
        btnThemeToggle = findViewById(R.id.btnThemeToggle)
    }

    private fun setupMainNavTabs() {
        mainNavTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> switchFragment("READER")
                    1 -> switchFragment("QUIZ")
                    2 -> switchFragment("STATS")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        mainNavTabs.addTab(mainNavTabs.newTab().setText("📖 Reader"))
        mainNavTabs.addTab(mainNavTabs.newTab().setText("🧪 Quiz"))
        mainNavTabs.addTab(mainNavTabs.newTab().setText("📊 Stats"))
    }

    private fun switchFragment(tag: String) {
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer)

        val targetFragment = when (tag) {
            "QUIZ" -> fragmentManager.findFragmentByTag("QUIZ") ?: QuizFragment()
            "STATS" -> fragmentManager.findFragmentByTag("STATS") ?: StatsFragment()
            else -> fragmentManager.findFragmentByTag("READER") ?: ReaderFragment()
        }

        if (currentFragment != targetFragment) {
            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, targetFragment, tag)
                .commit()

            if (targetFragment is ReaderFragment) {
                targetFragment.applyTheme(currentTheme)
            } else if (targetFragment is QuizFragment) {
                targetFragment.applyTheme(currentTheme)
            } else if (targetFragment is StatsFragment) {
                targetFragment.applyTheme(currentTheme)
            }
        }
    }

    private fun getActiveReaderFragment(): ReaderFragment? {
        return supportFragmentManager.findFragmentByTag("READER") as? ReaderFragment
    }

    private fun getActiveQuizFragment(): QuizFragment? {
        return supportFragmentManager.findFragmentByTag("QUIZ") as? QuizFragment
    }

    private fun getActiveStatsFragment(): StatsFragment? {
        return supportFragmentManager.findFragmentByTag("STATS") as? StatsFragment
    }

    private fun setupHeaderListeners() {
        btnFontDecrease.setOnClickListener {
            if (currentFontSizeSp > 12f) {
                currentFontSizeSp -= 2f
                getActiveReaderFragment()?.setFontSize(currentFontSizeSp)
                Toast.makeText(this, "Font Size: ${currentFontSizeSp.toInt()}sp", Toast.LENGTH_SHORT).show()
            }
        }

        btnFontIncrease.setOnClickListener {
            if (currentFontSizeSp < 24f) {
                currentFontSizeSp += 2f
                getActiveReaderFragment()?.setFontSize(currentFontSizeSp)
                Toast.makeText(this, "Font Size: ${currentFontSizeSp.toInt()}sp", Toast.LENGTH_SHORT).show()
            }
        }

        btnThemeToggle.setOnClickListener {
            currentTheme = when (currentTheme) {
                ReaderTheme.SEPIA -> ReaderTheme.LIGHT
                ReaderTheme.LIGHT -> ReaderTheme.DARK
                ReaderTheme.DARK -> ReaderTheme.SEPIA
            }
            applyTheme(currentTheme, showToast = true)
        }
    }

    private fun applyTheme(theme: ReaderTheme, showToast: Boolean = false) {
        currentTheme = theme
        getActiveReaderFragment()?.applyTheme(theme)
        getActiveQuizFragment()?.applyTheme(theme)
        getActiveStatsFragment()?.applyTheme(theme)

        val bgRes = when (theme) {
            ReaderTheme.SEPIA -> R.color.kindle_sepia_bg
            ReaderTheme.LIGHT -> R.color.kindle_light_bg
            ReaderTheme.DARK -> R.color.kindle_dark_bg
        }
        rootContainer.setBackgroundColor(ContextCompat.getColor(this, bgRes))

        if (showToast) {
            val themeName = when (theme) {
                ReaderTheme.SEPIA -> "Sepia Mode"
                ReaderTheme.LIGHT -> "Light Mode"
                ReaderTheme.DARK -> "Night Mode"
            }
            Toast.makeText(this, "Switched to $themeName", Toast.LENGTH_SHORT).show()
        }
    }
}