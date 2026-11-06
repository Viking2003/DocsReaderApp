package com.example.myfirstandroidapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.example.myfirstandroidapplication.data.AuthManager
import com.example.myfirstandroidapplication.data.PreferencesManager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var rootContainer: LinearLayout
    lateinit var mainNavTabs: TabLayout
    private lateinit var btnFontDecrease: ImageButton
    private lateinit var btnFontIncrease: ImageButton
    private lateinit var btnThemeToggle: ImageButton

    var currentTheme: ReaderTheme = ReaderTheme.SEPIA
    private var currentFontSizeSp: Float = 16f
    private lateinit var preferencesManager: PreferencesManager

    /**
     * Set while we move the Quiz tab to the front ourselves. Selecting that tab
     * would otherwise replace the player with a fresh [QuizFragment] and drop
     * the community quiz that was just handed to it.
     */
    private var ignoreQuizTabSwitch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        FirebaseService.initialize(this)
        preferencesManager = PreferencesManager(this)

        initViews()
        setupHeaderListeners()
        setupMainNavTabs()

        // Auto-switch theme based on time of day
        applyAutoTheme()
        
        // Check if user has already chosen auth mode (Google sign-in or guest)
        lifecycleScope.launch {
            val isGoogle = AuthManager.isGoogleSignedIn()
            val completedChoice = preferencesManager.authChoiceCompleted.first()

            if (isGoogle || completedChoice) {
                // Ensure auth user id is loaded
                AuthManager.ensureUserId(this@MainActivity)
                switchFragment("READER")
            } else {
                showAuthStartup()
            }
        }
    }

    private fun showAuthStartup() {
        mainNavTabs.visibility = android.view.View.GONE
        findViewById<android.view.View>(R.id.topToolbar)?.visibility = android.view.View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, AuthStartupFragment(), "AUTH_STARTUP")
            .commit()
    }

    fun onAuthCompleted() {
        mainNavTabs.visibility = android.view.View.VISIBLE
        findViewById<android.view.View>(R.id.topToolbar)?.visibility = android.view.View.VISIBLE
        mainNavTabs.getTabAt(0)?.select()
        switchFragment("READER")
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
                    1 -> if (!ignoreQuizTabSwitch) switchFragment("QUIZ")
                    2 -> switchFragment("STATS")
                    3 -> switchFragment("ACHIEVEMENTS")
                    4 -> switchFragment("SETTINGS")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        mainNavTabs.addTab(mainNavTabs.newTab().setText("📖 Reader"))
        mainNavTabs.addTab(mainNavTabs.newTab().setText("🧪 Quiz"))
        mainNavTabs.addTab(mainNavTabs.newTab().setText("📊 Stats"))
        mainNavTabs.addTab(mainNavTabs.newTab().setText("🏆 Achievements"))
        mainNavTabs.addTab(mainNavTabs.newTab().setText("⚙️ Settings"))
    }

    fun switchFragment(tag: String) {
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer)

        val targetFragment = when (tag) {
            "QUIZ" -> fragmentManager.findFragmentByTag("QUIZ") ?: QuizFragment()
            "STATS" -> fragmentManager.findFragmentByTag("STATS") ?: StatsFragment()
            "ACHIEVEMENTS" -> fragmentManager.findFragmentByTag("ACHIEVEMENTS") ?: AchievementsFragment()
            "SETTINGS" -> fragmentManager.findFragmentByTag("SETTINGS") ?: SettingsFragment()
            "COMMUNITY_LIST" -> fragmentManager.findFragmentByTag("COMMUNITY_LIST") ?: CommunityQuizListFragment()
            "QUIZ_EDITOR" -> fragmentManager.findFragmentByTag("QUIZ_EDITOR") ?: QuizEditorFragment()
            "MY_QUIZZES" -> fragmentManager.findFragmentByTag("MY_QUIZZES") ?: MyQuizzesFragment()
            else -> fragmentManager.findFragmentByTag("READER") ?: ReaderFragment()
        }

        if (currentFragment != targetFragment) {
            val transaction = fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, targetFragment, tag)
            
            // Add to backstack if it's not a main tab
            if (tag !in listOf("READER", "QUIZ", "STATS", "ACHIEVEMENTS", "SETTINGS")) {
                transaction.addToBackStack(tag)
            }
            
            transaction.commit()

            // Update theme for new fragment
            applyThemeToFragment(targetFragment, currentTheme)
        }
        updateFontButtonsVisibility(tag == "READER")
    }

    private fun updateFontButtonsVisibility(isVisible: Boolean) {
        val alpha = if (isVisible) 1.0f else 0.3f
        btnFontDecrease.isEnabled = isVisible
        btnFontIncrease.isEnabled = isVisible
        btnFontDecrease.alpha = alpha
        btnFontIncrease.alpha = alpha
    }

    private fun applyThemeToFragment(fragment: androidx.fragment.app.Fragment, theme: ReaderTheme) {
        when (fragment) {
            is ReaderFragment -> fragment.applyTheme(theme)
            is QuizFragment -> fragment.applyTheme(theme)
            is StatsFragment -> fragment.applyTheme(theme)
            is AchievementsFragment -> fragment.applyTheme(theme)
            is SettingsFragment -> fragment.applyTheme(theme)
            is CommunityQuizListFragment -> fragment.applyTheme(theme)
            is QuizEditorFragment -> fragment.applyTheme(theme)
            is MyQuizzesFragment -> fragment.applyTheme(theme)
            is AuthStartupFragment -> fragment.applyTheme(theme)
        }
    }

    fun startCommunityQuiz(quiz: com.example.myfirstandroidapplication.models.CommunityQuiz) {
        // Community list is opened on top of the Quiz tab, so that tab is already
        // selected and selecting it again never brings the player back. The quiz
        // fragment is also stopped on the back stack, with its view destroyed.
        // Hand it the quiz first so the rebuilt view plays those questions instead
        // of the built-in category set.
        val existing = supportFragmentManager.findFragmentByTag("QUIZ") as? QuizFragment
        existing?.prepareCommunityQuiz(quiz)

        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        var player = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? QuizFragment
        if (player == null) {
            switchFragment("QUIZ")
            supportFragmentManager.executePendingTransactions()
            player = supportFragmentManager.findFragmentByTag("QUIZ") as? QuizFragment
            player?.prepareCommunityQuiz(quiz)
        }

        if (mainNavTabs.selectedTabPosition != 1) {
            ignoreQuizTabSwitch = true
            mainNavTabs.getTabAt(1)?.select()
            ignoreQuizTabSwitch = false
        }
    }

    fun navigateToCreateQuiz(quizId: String? = null) {
        // Pass quizId for editing
        val fragment = QuizEditorFragment()
        quizId?.let {
            val args = android.os.Bundle()
            args.putString("quizId", it)
            fragment.arguments = args
        }
        
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, "QUIZ_EDITOR")
            .addToBackStack(null)
            .commit()
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

    private fun getActiveAchievementsFragment(): AchievementsFragment? {
        return supportFragmentManager.findFragmentByTag("ACHIEVEMENTS") as? AchievementsFragment
    }

    private fun getActiveSettingsFragment(): SettingsFragment? {
        return supportFragmentManager.findFragmentByTag("SETTINGS") as? SettingsFragment
    }

    private fun getActiveCommunityListFragment(): CommunityQuizListFragment? {
        return supportFragmentManager.findFragmentByTag("COMMUNITY_LIST") as? CommunityQuizListFragment
    }

    private fun getActiveQuizEditorFragment(): QuizEditorFragment? {
        return supportFragmentManager.findFragmentByTag("QUIZ_EDITOR") as? QuizEditorFragment
    }

    private fun getActiveMyQuizzesFragment(): MyQuizzesFragment? {
        return supportFragmentManager.findFragmentByTag("MY_QUIZZES") as? MyQuizzesFragment
    }

    private fun getActiveAuthStartupFragment(): AuthStartupFragment? {
        return supportFragmentManager.findFragmentByTag("AUTH_STARTUP") as? AuthStartupFragment
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

    fun applyThemeFromSettings(theme: ReaderTheme) {
        applyTheme(theme, showToast = true)
    }

    private fun applyTheme(theme: ReaderTheme, showToast: Boolean = false) {
        currentTheme = theme
        getActiveReaderFragment()?.applyTheme(theme)
        getActiveQuizFragment()?.applyTheme(theme)
        getActiveStatsFragment()?.applyTheme(theme)
        getActiveAchievementsFragment()?.applyTheme(theme)
        getActiveSettingsFragment()?.applyTheme(theme)
        getActiveCommunityListFragment()?.applyTheme(theme)
        getActiveQuizEditorFragment()?.applyTheme(theme)
        getActiveMyQuizzesFragment()?.applyTheme(theme)
        getActiveAuthStartupFragment()?.applyTheme(theme)

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