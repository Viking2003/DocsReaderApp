package com.example.myfirstandroidapplication

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.example.myfirstandroidapplication.data.AuthManager
import com.example.myfirstandroidapplication.data.CommunityQuizRepository
import com.example.myfirstandroidapplication.data.PreferencesManager
import com.example.myfirstandroidapplication.models.CommunityQuestion
import com.example.myfirstandroidapplication.models.CommunityQuiz
import com.example.myfirstandroidapplication.models.DocCategory
import com.example.myfirstandroidapplication.models.QuizDifficulty
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class QuizEditorFragment : Fragment(R.layout.fragment_quiz_editor) {

    private lateinit var etQuizTitle: EditText
    private lateinit var etQuizDescription: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etQuestionText: EditText
    private lateinit var etOption0: EditText
    private lateinit var etOption1: EditText
    private lateinit var etOption2: EditText
    private lateinit var etOption3: EditText
    private lateinit var etExplanation: EditText
    private lateinit var btnAddQuestion: MaterialButton
    private lateinit var btnUpload: MaterialButton
    private lateinit var uploadProgress: ProgressBar
    private lateinit var tvAddedCount: TextView
    private lateinit var editorRoot: View
    private lateinit var tvEditorTitle: TextView
    private lateinit var tvLabelCategory: TextView
    private lateinit var tvLabelQuestions: TextView
    private lateinit var tvQuestionCountLabel: TextView
    private lateinit var questionEditorCard: View
    private lateinit var tilQuizTitle: com.google.android.material.textfield.TextInputLayout
    private lateinit var tilQuizDescription: com.google.android.material.textfield.TextInputLayout
    private lateinit var tvQuestionCategoryDisplay: TextView
    private lateinit var tvAuthorPreview: TextView

    private val addedQuestions = mutableListOf<CommunityQuestion>()
    private var currentTheme: ReaderTheme = ReaderTheme.SEPIA
    private lateinit var preferencesManager: PreferencesManager
    private var editingQuizId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferencesManager = PreferencesManager(requireContext())
        initViews(view)
        setupSpinner()
        setupListeners()
        
        viewLifecycleOwner.lifecycleScope.launch {
            val author = AuthManager.getAuthorName(requireContext())
            val isGoogle = AuthManager.isGoogleSignedIn()
            tvAuthorPreview.text = if (isGoogle) {
                "Publishing as: $author (Google Account)"
            } else {
                "Publishing as: $author (Anonymous - sign in in Settings to show your name)"
            }
        }

        (activity as? MainActivity)?.let {
            currentTheme = it.currentTheme
        }
        applyTheme(currentTheme)
        
        // Check if we're editing an existing quiz
        editingQuizId = arguments?.getString("quizId")
        if (editingQuizId != null) {
            loadQuizForEditing(editingQuizId!!)
        }
    }

    private fun initViews(view: View) {
        editorRoot = view.findViewById(R.id.editorRoot)
        tvEditorTitle = view.findViewById(R.id.tvEditorTitle)
        tvAuthorPreview = view.findViewById(R.id.tvAuthorPreview)
        etQuizTitle = view.findViewById(R.id.etQuizTitle)
        etQuizDescription = view.findViewById(R.id.etQuizDescription)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        etQuestionText = view.findViewById(R.id.etQuestionText)
        etOption0 = view.findViewById(R.id.etOption0)
        etOption1 = view.findViewById(R.id.etOption1)
        etOption2 = view.findViewById(R.id.etOption2)
        etOption3 = view.findViewById(R.id.etOption3)
        etExplanation = view.findViewById(R.id.etExplanation)
        btnAddQuestion = view.findViewById(R.id.btnAddQuestionToList)
        btnUpload = view.findViewById(R.id.btnUploadQuiz)
        uploadProgress = view.findViewById(R.id.uploadProgress)
        tvAddedCount = view.findViewById(R.id.tvAddedQuestionsCount)
        tvLabelCategory = view.findViewById(R.id.tvLabelCategory)
        tvLabelQuestions = view.findViewById(R.id.tvLabelQuestions)
        tvQuestionCountLabel = view.findViewById(R.id.tvQuestionCountLabel)
        questionEditorCard = view.findViewById(R.id.questionEditorCard)
        tilQuizTitle = view.findViewById(R.id.tilQuizTitle)
        tilQuizDescription = view.findViewById(R.id.tilQuizDescription)
        tvQuestionCategoryDisplay = view.findViewById(R.id.tvQuestionCategoryDisplay)
    }

    private fun setupSpinner() {
        val categories = DocCategory.values().map { it.displayName }
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as? TextView)?.setTextColor(getThemeTextColor())
                return v
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                val cardBg = when (currentTheme) {
                    ReaderTheme.SEPIA -> ContextCompat.getColor(context, R.color.kindle_sepia_card)
                    ReaderTheme.LIGHT -> ContextCompat.getColor(context, R.color.kindle_light_card)
                    ReaderTheme.DARK -> ContextCompat.getColor(context, R.color.kindle_dark_card)
                }
                v.setBackgroundColor(cardBg)
                (v as? TextView)?.setTextColor(getThemeTextColor())
                return v
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        
        // Update question category display when main category changes
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = DocCategory.values()[position]
                tvQuestionCategoryDisplay.text = "Category: ${selectedCategory.displayName}"
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Initialize with first category
        if (categories.isNotEmpty()) {
            tvQuestionCategoryDisplay.text = "Category: ${categories[0]}"
        }
    }

    private fun getThemeTextColor(): Int {
        val ctx = context ?: return android.graphics.Color.BLACK
        return when (currentTheme) {
            ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_text)
            ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_text)
            ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_text)
        }
    }

    private fun getThemeSubtextColor(): Int {
        val ctx = context ?: return android.graphics.Color.GRAY
        return when (currentTheme) {
            ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_subtext)
            ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_subtext)
            ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_subtext)
        }
    }

    private fun setupListeners() {
        btnAddQuestion.setOnClickListener {
            addCurrentQuestion()
        }

        btnUpload.setOnClickListener {
            uploadQuiz()
        }
    }

    private fun addCurrentQuestion() {
        val qText = etQuestionText.text.toString()
        val opt0 = etOption0.text.toString()
        val opt1 = etOption1.text.toString()
        val opt2 = etOption2.text.toString()
        val opt3 = etOption3.text.toString()
        val expl = etExplanation.text.toString()
        // Use main quiz category for consistency
        val category = DocCategory.values()[spinnerCategory.selectedItemPosition]

        if (qText.isBlank() || opt0.isBlank() || opt1.isBlank()) {
            Toast.makeText(requireContext(), "Please fill question and at least 2 options", Toast.LENGTH_SHORT).show()
            return
        }

        val question = CommunityQuestion(
            category = category,
            questionText = qText,
            options = listOf(opt0, opt1, opt2, opt3).filter { it.isNotBlank() },
            correctAnswerIndex = 0, // Simplified: First option is always correct for now
            explanation = expl,
            difficulty = QuizDifficulty.MEDIUM
        )

        addedQuestions.add(question)
        tvAddedCount.text = "Questions added: ${addedQuestions.size}"
        
        // Clear question fields
        etQuestionText.text.clear()
        etOption0.text.clear()
        etOption1.text.clear()
        etOption2.text.clear()
        etOption3.text.clear()
        etExplanation.text.clear()
        
        Toast.makeText(requireContext(), "Question added!", Toast.LENGTH_SHORT).show()
    }

    private fun loadQuizForEditing(quizId: String) {
        tvEditorTitle.text = "Edit Quiz"
        btnUpload.text = "Update Quiz"
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = CommunityQuizRepository.fetchQuizById(quizId)
                result.onSuccess { quiz ->
                    if (quiz != null) {
                        etQuizTitle.setText(quiz.title)
                        etQuizDescription.setText(quiz.description)
                        addedQuestions.clear()
                        addedQuestions.addAll(quiz.questions)
                        tvAddedCount.text = "Questions added: ${addedQuestions.size}"
                    } else {
                        Toast.makeText(requireContext(), "Quiz not found", Toast.LENGTH_SHORT).show()
                    }
                }.onFailure { error ->
                    Toast.makeText(requireContext(), "Failed to load quiz: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadQuiz() {
        val title = etQuizTitle.text.toString()
        val desc = etQuizDescription.text.toString()

        if (title.isBlank() || addedQuestions.isEmpty()) {
            Toast.makeText(requireContext(), "Title and at least 1 question required", Toast.LENGTH_SHORT).show()
            return
        }

        // Check Firebase availability first
        if (!FirebaseService.isFirebaseAvailable()) {
            Toast.makeText(requireContext(), "Firebase not available. Check your internet connection.", Toast.LENGTH_LONG).show()
            return
        }

        // Disable all input fields during upload
        disableAllInputs(true)
        
        // Disable button and show progress immediately
        btnUpload.isEnabled = false
        btnUpload.text = "Uploading..."
        uploadProgress.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val uid = AuthManager.ensureUserId(requireContext())
                val authorName = AuthManager.getAuthorName(requireContext())
                
                val categories = addedQuestions.map { it.category }.distinct()

                val quiz = CommunityQuiz(
                    quizId = editingQuizId ?: "", // Empty for new quiz, will be generated by Firestore
                    authorId = uid,
                    authorName = authorName,
                    title = title,
                    description = desc,
                    categories = categories,
                    questions = addedQuestions,
                    createdAt = System.currentTimeMillis(),
                    likedBy = emptyList()
                )

                val result = if (editingQuizId != null) {
                    CommunityQuizRepository.updateQuiz(quiz.copy(quizId = editingQuizId!!))
                } else {
                    CommunityQuizRepository.uploadQuiz(quiz)
                }
                
                if (isAdded) {
                    uploadProgress.visibility = View.GONE
                    btnUpload.isEnabled = true
                    btnUpload.text = if (editingQuizId != null) "Update Quiz" else "Upload to Community 🚀"

                    result.onSuccess {
                        val message = if (editingQuizId != null) "Quiz updated successfully!" else "Quiz uploaded successfully!"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        // Navigate to My Quizzes after successful upload/update
                        parentFragmentManager.popBackStack()
                        (activity as? MainActivity)?.switchFragment("MY_QUIZZES")
                    }.onFailure { error ->
                        disableAllInputs(false)
                        val errorMsg = if (error is kotlinx.coroutines.TimeoutCancellationException) {
                            "Upload timed out. Check your internet connection."
                        } else {
                            "Operation failed: ${error.message}"
                        }
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                if (isAdded) {
                    uploadProgress.visibility = View.GONE
                    btnUpload.isEnabled = true
                    btnUpload.text = if (editingQuizId != null) "Update Quiz" else "Upload to Community 🚀"
                    disableAllInputs(false)
                    val errorMsg = if (e is kotlinx.coroutines.TimeoutCancellationException) {
                        "Upload timed out. Check your internet connection."
                    } else {
                        "Operation failed: ${e.message}"
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun disableAllInputs(disable: Boolean) {
        etQuizTitle.isEnabled = !disable
        etQuizDescription.isEnabled = !disable
        spinnerCategory.isEnabled = !disable
        etQuestionText.isEnabled = !disable
        etOption0.isEnabled = !disable
        etOption1.isEnabled = !disable
        etOption2.isEnabled = !disable
        etOption3.isEnabled = !disable
        etExplanation.isEnabled = !disable
        btnAddQuestion.isEnabled = !disable
        // btnUpload is handled separately
    }

    fun applyTheme(theme: ReaderTheme) {
        currentTheme = theme
        val ctx = context ?: return
        if (!::editorRoot.isInitialized) return

        val bgRes = when (theme) {
            ReaderTheme.SEPIA -> R.color.kindle_sepia_bg
            ReaderTheme.LIGHT -> R.color.kindle_light_bg
            ReaderTheme.DARK -> R.color.kindle_dark_bg
        }
        val textColor = getThemeTextColor()
        val subtextColor = getThemeSubtextColor()
        val cardBg = when (theme) {
            ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_card)
            ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_card)
            ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_card)
        }

        editorRoot.setBackgroundColor(ContextCompat.getColor(ctx, bgRes))
        tvEditorTitle.setTextColor(textColor)
        if (::tvAuthorPreview.isInitialized) {
            tvAuthorPreview.setTextColor(subtextColor)
        }
        tvLabelCategory.setTextColor(subtextColor)
        tvLabelQuestions.setTextColor(textColor)
        tvAddedCount.setTextColor(subtextColor)
        tvQuestionCountLabel.setTextColor(ContextCompat.getColor(ctx, R.color.accent))
        questionEditorCard.setBackgroundColor(cardBg)

        // Apply to all EditTexts
        val editTexts = listOf(
            etQuizTitle, etQuizDescription, etQuestionText,
            etOption0, etOption1, etOption2, etOption3, etExplanation
        )
        for (et in editTexts) {
            et.setTextColor(textColor)
            et.setHintTextColor(subtextColor)
        }

        val hintColorStateList = android.content.res.ColorStateList.valueOf(subtextColor)
        tilQuizTitle.defaultHintTextColor = hintColorStateList
        tilQuizTitle.hintTextColor = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.accent))
        tilQuizDescription.defaultHintTextColor = hintColorStateList
        tilQuizDescription.hintTextColor = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.accent))

        btnAddQuestion.setTextColor(ContextCompat.getColor(ctx, R.color.accent))
        
        // Refresh spinner
        (spinnerCategory.adapter as? BaseAdapter)?.notifyDataSetChanged()
    }
}
