package com.example.myfirstandroidapplication

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.example.myfirstandroidapplication.data.PreferencesManager
import com.example.myfirstandroidapplication.data.QuizRepository
import com.example.myfirstandroidapplication.models.DocCategory
import com.example.myfirstandroidapplication.models.QuizDifficulty
import com.example.myfirstandroidapplication.models.QuizQuestion
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class QuizFragment : Fragment(R.layout.fragment_quiz) {

    private lateinit var quizScrollView: ScrollView
    private lateinit var quizCategoryTabs: TabLayout
    private lateinit var quizProgressBar: LinearProgressIndicator
    private lateinit var tvQuizProgressCounter: TextView
    private lateinit var questionCard: LinearLayout
    private lateinit var tvQuestionText: TextView
    private lateinit var optionsRadioGroup: RadioGroup
    private lateinit var rbOption0: RadioButton
    private lateinit var rbOption1: RadioButton
    private lateinit var rbOption2: RadioButton
    private lateinit var rbOption3: RadioButton
    private lateinit var btnSubmitAnswer: MaterialButton

    private lateinit var explanationContainer: LinearLayout
    private lateinit var tvFeedbackTitle: TextView
    private lateinit var tvExplanationText: TextView
    private lateinit var btnNextQuestion: MaterialButton

    private lateinit var scoreSummaryContainer: LinearLayout
    private lateinit var tvSummaryTitle: TextView
    private lateinit var tvFinalScore: TextView
    private lateinit var tvScoreMessage: TextView
    private lateinit var btnRetryQuiz: MaterialButton
    private lateinit var btnReviewMistakes: MaterialButton
    private lateinit var difficultyRadioGroup: RadioGroup
    private lateinit var rbEasy: RadioButton
    private lateinit var rbMedium: RadioButton
    private lateinit var rbHard: RadioButton
    private lateinit var tvDifficultyLabel: TextView

    private var currentQuestions: List<QuizQuestion> = QuizRepository.jniQuestions.shuffled()
    private var currentQuestionIndex = 0
    private var score = 0
    private var isAnswerSubmitted = false
    private var incorrectQuestionIndices: MutableList<Int> = mutableListOf()
    private var isReviewMode = false
    private lateinit var preferencesManager: PreferencesManager
    private var currentDifficulty: QuizDifficulty = QuizDifficulty.MEDIUM
    var currentTheme: ReaderTheme = ReaderTheme.SEPIA

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        initViews(view)
        setupCategoryTabs()
        setupListeners()

        // Sync active theme from MainActivity
        (activity as? MainActivity)?.let {
            currentTheme = it.currentTheme
        }

        applyTheme(currentTheme)
        resetQuiz()
    }

    private fun initViews(view: View) {
        quizScrollView = view.findViewById(R.id.quizScrollView)
        quizCategoryTabs = view.findViewById(R.id.quizCategoryTabs)
        quizProgressBar = view.findViewById(R.id.quizProgressBar)
        tvQuizProgressCounter = view.findViewById(R.id.tvQuizProgressCounter)
        questionCard = view.findViewById(R.id.questionCard)
        tvQuestionText = view.findViewById(R.id.tvQuestionText)
        optionsRadioGroup = view.findViewById(R.id.optionsRadioGroup)
        rbOption0 = view.findViewById(R.id.rbOption0)
        rbOption1 = view.findViewById(R.id.rbOption1)
        rbOption2 = view.findViewById(R.id.rbOption2)
        rbOption3 = view.findViewById(R.id.rbOption3)
        btnSubmitAnswer = view.findViewById(R.id.btnSubmitAnswer)

        explanationContainer = view.findViewById(R.id.explanationContainer)
        tvFeedbackTitle = view.findViewById(R.id.tvFeedbackTitle)
        tvExplanationText = view.findViewById(R.id.tvExplanationText)
        btnNextQuestion = view.findViewById(R.id.btnNextQuestion)

        scoreSummaryContainer = view.findViewById(R.id.scoreSummaryContainer)
        tvSummaryTitle = view.findViewById(R.id.tvSummaryTitle)
        tvFinalScore = view.findViewById(R.id.tvFinalScore)
        tvScoreMessage = view.findViewById(R.id.tvScoreMessage)
        btnRetryQuiz = view.findViewById(R.id.btnRetryQuiz)
        btnReviewMistakes = view.findViewById(R.id.btnReviewMistakes)

        difficultyRadioGroup = view.findViewById(R.id.difficultyRadioGroup)
        rbEasy = view.findViewById(R.id.rbEasy)
        rbMedium = view.findViewById(R.id.rbMedium)
        rbHard = view.findViewById(R.id.rbHard)
        tvDifficultyLabel = view.findViewById(R.id.tvDifficultyLabel)
    }

    fun applyTheme(theme: ReaderTheme) {
        currentTheme = theme
        val ctx = context ?: return
        if (!::quizScrollView.isInitialized) return

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

        val cardBg = when (theme) {
            ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_card)
            ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_card)
            ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_card)
        }

        quizScrollView.setBackgroundColor(ContextCompat.getColor(ctx, bgRes))
        tvQuizProgressCounter.setTextColor(subtextColor)
        tvQuestionText.setTextColor(textColor)
        tvExplanationText.setTextColor(textColor)
        tvSummaryTitle.setTextColor(textColor)
        tvScoreMessage.setTextColor(subtextColor)
        tvDifficultyLabel.setTextColor(subtextColor)

        rbEasy.setTextColor(textColor)
        rbMedium.setTextColor(textColor)
        rbHard.setTextColor(textColor)

        questionCard.setBackgroundColor(cardBg)
        explanationContainer.setBackgroundColor(cardBg)
        scoreSummaryContainer.setBackgroundColor(cardBg)

        val optionButtons = listOf(rbOption0, rbOption1, rbOption2, rbOption3)
        for (rb in optionButtons) {
            if (!isAnswerSubmitted) {
                rb.setTextColor(textColor)
            }
        }
    }

    private fun setupCategoryTabs() {
        quizCategoryTabs.addTab(quizCategoryTabs.newTab().setText("1. JNI Quiz"))
        quizCategoryTabs.addTab(quizCategoryTabs.newTab().setText("2. Android Principles"))

        quizCategoryTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                currentQuestions = filterQuestionsByDifficulty(
                    if (position == 0) QuizRepository.jniQuestions else QuizRepository.androidQuestions
                ).shuffled()
                resetQuiz()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        difficultyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            currentDifficulty = when (checkedId) {
                R.id.rbEasy -> QuizDifficulty.EASY
                R.id.rbHard -> QuizDifficulty.HARD
                else -> QuizDifficulty.MEDIUM
            }
            val pos = quizCategoryTabs.selectedTabPosition
            currentQuestions = filterQuestionsByDifficulty(
                if (pos == 1) QuizRepository.androidQuestions else QuizRepository.jniQuestions
            ).shuffled()
            resetQuiz()
        }
    }

    private fun filterQuestionsByDifficulty(questions: List<QuizQuestion>): List<QuizQuestion> {
        val filtered = questions.filter { it.difficulty == currentDifficulty }
        return if (filtered.isNotEmpty()) filtered else questions
    }

    private fun setupListeners() {
        btnSubmitAnswer.setOnClickListener {
            if (isAnswerSubmitted) return@setOnClickListener
            val selectedId = optionsRadioGroup.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(context, "Please select an answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedIndex = when (selectedId) {
                R.id.rbOption0 -> 0
                R.id.rbOption1 -> 1
                R.id.rbOption2 -> 2
                R.id.rbOption3 -> 3
                else -> -1
            }

            submitAnswer(selectedIndex)
        }

        btnNextQuestion.setOnClickListener {
            if (currentQuestionIndex < currentQuestions.size - 1) {
                currentQuestionIndex++
                loadQuestion(currentQuestionIndex)
            } else {
                showScoreSummary()
            }
        }

        btnRetryQuiz.setOnClickListener {
            resetQuiz()
        }

        btnReviewMistakes.setOnClickListener {
            startReviewMode()
        }
    }

    private fun resetQuiz() {
        currentQuestionIndex = 0
        score = 0
        incorrectQuestionIndices.clear()
        isReviewMode = false
        scoreSummaryContainer.visibility = View.GONE
        questionCard.visibility = View.VISIBLE
        val pos = quizCategoryTabs.selectedTabPosition
        val baseList = if (pos == 1) QuizRepository.androidQuestions else QuizRepository.jniQuestions
        currentQuestions = filterQuestionsByDifficulty(baseList).shuffled()
        loadQuestion(0)
    }

    private fun loadQuestion(index: Int) {
        isAnswerSubmitted = false
        explanationContainer.visibility = View.GONE
        optionsRadioGroup.clearCheck()

        val question = currentQuestions[index]
        tvQuestionText.text = question.questionText

        val optionButtons = listOf(rbOption0, rbOption1, rbOption2, rbOption3)
        for (i in 0 until 4) {
            val rb = optionButtons[i]
            rb.text = question.options[i]
            rb.isEnabled = true
        }

        applyTheme(currentTheme)

        val total = currentQuestions.size
        tvQuizProgressCounter.text = "Question ${index + 1} of $total"
        val progressPercent = (((index + 1).toFloat() / total.toFloat()) * 100).toInt()
        quizProgressBar.progress = progressPercent
    }

    private fun submitAnswer(selectedIndex: Int) {
        isAnswerSubmitted = true
        val question = currentQuestions[currentQuestionIndex]
        val isCorrect = selectedIndex == question.correctAnswerIndex

        if (isCorrect) {
            score++
            tvFeedbackTitle.text = "Correct! 🎉"
            tvFeedbackTitle.setTextColor(Color.parseColor("#276749"))
        } else {
            tvFeedbackTitle.text = "Incorrect ❌"
            tvFeedbackTitle.setTextColor(Color.parseColor("#C53030"))
            if (!isReviewMode) {
                incorrectQuestionIndices.add(currentQuestionIndex)
            }
        }

        tvExplanationText.text = question.explanation
        explanationContainer.visibility = View.VISIBLE

        if (currentQuestionIndex == currentQuestions.size - 1) {
            btnNextQuestion.text = "Finish Quiz ➔"
        } else {
            btnNextQuestion.text = "Next Question ➔"
        }

        val optionButtons = listOf(rbOption0, rbOption1, rbOption2, rbOption3)
        for (i in 0 until 4) {
            val rb = optionButtons[i]
            rb.isEnabled = false
            if (i == question.correctAnswerIndex) {
                rb.setTextColor(Color.parseColor("#276749"))
            } else if (i == selectedIndex && !isCorrect) {
                rb.setTextColor(Color.parseColor("#C53030"))
            }
        }
    }

    private fun showScoreSummary() {
        questionCard.visibility = View.GONE
        explanationContainer.visibility = View.GONE
        scoreSummaryContainer.visibility = View.VISIBLE

        val total = currentQuestions.size
        val percent = ((score.toFloat() / total.toFloat()) * 100).toInt()
        tvFinalScore.text = "You scored $score / $total ($percent%)"

        tvScoreMessage.text = when {
            percent >= 80 -> "Outstanding! You have mastered these core concepts."
            percent >= 60 -> "Good job! Review the documentation to perfect your knowledge."
            else -> "Keep learning! Re-read the chapters and try again."
        }

        // Show Review Mistakes button if there are incorrect answers and not in review mode
        if (!isReviewMode && incorrectQuestionIndices.isNotEmpty()) {
            btnReviewMistakes.visibility = View.VISIBLE
        } else {
            btnReviewMistakes.visibility = View.GONE
        }

        // Record quiz result if not in review mode
        if (!isReviewMode) {
            viewLifecycleOwner.lifecycleScope.launch {
                preferencesManager.recordQuizResult(score, total)
            }
        }
    }

    private fun startReviewMode() {
        isReviewMode = true
        currentQuestionIndex = 0
        score = 0
        scoreSummaryContainer.visibility = View.GONE
        questionCard.visibility = View.VISIBLE

        // Filter questions to only show incorrect questions
        val reviewQuestions = incorrectQuestionIndices.map { currentQuestions[it] }
        currentQuestions = reviewQuestions

        loadQuestion(0)
    }

    fun filterQuestionsByChapter(category: DocCategory) {
        val tabPosition = if (category == DocCategory.JNI) 0 else 1
        quizCategoryTabs.selectTab(quizCategoryTabs.getTabAt(tabPosition))
        resetQuiz()
    }
}