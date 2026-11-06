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
import com.example.myfirstandroidapplication.data.AuthManager
import com.example.myfirstandroidapplication.data.CommunityQuizRepository
import kotlinx.coroutines.launch

class MyQuizzesFragment : Fragment(R.layout.fragment_my_quizzes) {

    private lateinit var rvQuizzes: RecyclerView
    private lateinit var adapter: CommunityQuizAdapter
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var tvEmptyMessage: TextView
    private lateinit var tvTitle: TextView
    private lateinit var rootContainer: View

    private var currentTheme: ReaderTheme = ReaderTheme.SEPIA
    private var currentUserId: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        
        (activity as? MainActivity)?.let {
            currentTheme = it.currentTheme
        }
        applyTheme(currentTheme)
        
        // Get user ID in coroutine
        viewLifecycleOwner.lifecycleScope.launch {
            currentUserId = AuthManager.ensureUserId(requireContext())
            if (isAdded) {
                loadMyQuizzes()
            }
        }
    }

    private fun initViews(view: View) {
        rootContainer = view.findViewById(R.id.myQuizzesRoot)
        rvQuizzes = view.findViewById(R.id.rvMyQuizzes)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage)
        tvTitle = view.findViewById(R.id.tvMyQuizzesTitle)
    }

    private fun setupRecyclerView() {
        adapter = CommunityQuizAdapter(
            theme = currentTheme,
            mode = QuizListMode.OWNED,
            onEdit = { quiz ->
                (activity as? MainActivity)?.navigateToCreateQuiz(quiz.quizId)
            },
            onDelete = { quiz ->
                deleteQuiz(quiz.quizId)
            },
            currentUserId = currentUserId
        )
        rvQuizzes.layoutManager = LinearLayoutManager(requireContext())
        rvQuizzes.adapter = adapter
    }

    private fun loadMyQuizzes() {
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
            val uid = AuthManager.ensureUserId(requireContext())
            val result = CommunityQuizRepository.fetchMyQuizzes(uid)
            loadingIndicator.visibility = View.GONE
            
            result.onSuccess { quizzes ->
                if (quizzes.isEmpty()) {
                    tvEmptyMessage.text = "No quizzes created yet. Create your first quiz!"
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

    private fun deleteQuiz(quizId: String) {
        // Show loading indicator immediately
        loadingIndicator.visibility = View.VISIBLE
        
        viewLifecycleOwner.lifecycleScope.launch {
            val result = CommunityQuizRepository.deleteQuiz(quizId)
            loadingIndicator.visibility = View.GONE
            
            result.onSuccess {
                adapter.removeQuiz(quizId)
                Toast.makeText(requireContext(), "Quiz deleted", Toast.LENGTH_SHORT).show()
                // Show empty message if no quizzes left
                if (adapter.itemCount == 0) {
                    tvEmptyMessage.visibility = View.VISIBLE
                }
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Delete failed: ${error.message}", Toast.LENGTH_SHORT).show()
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
        tvTitle.setTextColor(textColor)
        tvEmptyMessage.setTextColor(subtextColor)
        adapter.setTheme(theme)
    }
}
