package com.example.myfirstandroidapplication

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstandroidapplication.adapters.CommunityQuizAdapter
import com.example.myfirstandroidapplication.adapters.QuizListMode
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.example.myfirstandroidapplication.data.CommunityQuizRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class CommunityQuizListFragment : Fragment(R.layout.fragment_community_quiz_list) {

    private lateinit var rvQuizzes: RecyclerView
    private lateinit var adapter: CommunityQuizAdapter
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var tvEmptyMessage: TextView
    private lateinit var btnMyQuizzes: MaterialButton
    private lateinit var tvListTitle: TextView
    private lateinit var rootContainer: View
    
    private var currentTheme: ReaderTheme = ReaderTheme.SEPIA
    private var currentUserId: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        setupListeners()
        
        (activity as? MainActivity)?.let {
            currentTheme = it.currentTheme
        }
        applyTheme(currentTheme)
        
        // Get user ID in coroutine
        viewLifecycleOwner.lifecycleScope.launch {
            currentUserId = com.example.myfirstandroidapplication.data.AuthManager.ensureUserId(requireContext())
            if (isAdded) {
                loadQuizzes()
            }
        }
    }

    private fun initViews(view: View) {
        rootContainer = view.findViewById(R.id.communityListRoot)
        rvQuizzes = view.findViewById(R.id.rvCommunityQuizzes)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage)
        btnMyQuizzes = view.findViewById(R.id.btnMyQuizzes)
        tvListTitle = view.findViewById(R.id.tvListTitle)
    }

    private fun setupRecyclerView() {
        adapter = CommunityQuizAdapter(
            theme = currentTheme,
            mode = QuizListMode.BROWSE,
            onTakeQuiz = { quiz ->
                (activity as? MainActivity)?.startCommunityQuiz(quiz)
            },
            onLike = { quiz ->
                likeQuiz(quiz)
            },
            currentUserId = currentUserId
        )
        rvQuizzes.layoutManager = LinearLayoutManager(requireContext())
        rvQuizzes.adapter = adapter
    }

    private fun setupListeners() {
        btnMyQuizzes.setOnClickListener {
            (activity as? MainActivity)?.switchFragment("MY_QUIZZES")
        }
    }

    private fun loadQuizzes() {
        // Check Firebase availability first
        if (!FirebaseService.isFirebaseAvailable()) {
            loadingIndicator.visibility = View.GONE
            tvEmptyMessage.text = "Firebase not available. Check your internet connection."
            tvEmptyMessage.visibility = View.VISIBLE
            return
        }

        loadingIndicator.visibility = View.VISIBLE
        tvEmptyMessage.visibility = View.GONE
        
        viewLifecycleOwner.lifecycleScope.launch {
            val result = CommunityQuizRepository.fetchQuizzes()
            loadingIndicator.visibility = View.GONE
            
            result.onSuccess { quizzes ->
                if (quizzes.isEmpty()) {
                    tvEmptyMessage.text = "No quizzes found. Be the first to create one!"
                    tvEmptyMessage.visibility = View.VISIBLE
                } else {
                    adapter.replaceAll(quizzes)
                }
            }.onFailure { error ->
                val errorMsg = if (error is kotlinx.coroutines.TimeoutCancellationException) {
                    "Request timed out. Check your internet connection."
                } else {
                    "Error: ${error.message}"
                }
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                tvEmptyMessage.text = "Failed to load quizzes. Check connection."
                tvEmptyMessage.visibility = View.VISIBLE
            }
        }
    }

    private fun likeQuiz(quiz: com.example.myfirstandroidapplication.models.CommunityQuiz) {
        viewLifecycleOwner.lifecycleScope.launch {
            val uid = com.example.myfirstandroidapplication.data.AuthManager.ensureUserId(requireContext())
            val result = CommunityQuizRepository.likeQuiz(quiz.quizId, uid)
            result.onSuccess {
                adapter.updateQuiz(quiz.copy(likes = quiz.likes + 1, likedBy = quiz.likedBy + uid))
            }.onFailure { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun applyTheme(theme: ReaderTheme) {
        currentTheme = theme
        val ctx = context ?: return
        if (!::rootContainer.isInitialized) return

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

        rootContainer.setBackgroundColor(ContextCompat.getColor(ctx, bgRes))
        tvListTitle.setTextColor(textColor)
        tvEmptyMessage.setTextColor(subtextColor)
        btnMyQuizzes.setTextColor(ContextCompat.getColor(ctx, R.color.accent))
        adapter.setTheme(theme)
    }
}
