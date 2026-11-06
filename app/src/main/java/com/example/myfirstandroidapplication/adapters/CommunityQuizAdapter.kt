package com.example.myfirstandroidapplication.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstandroidapplication.R
import com.example.myfirstandroidapplication.models.CommunityQuiz
import com.google.android.material.button.MaterialButton

enum class QuizListMode { BROWSE, OWNED }

class CommunityQuizAdapter(
    private var theme: ReaderTheme = ReaderTheme.SEPIA,
    private val mode: QuizListMode = QuizListMode.BROWSE,
    private val onTakeQuiz: (CommunityQuiz) -> Unit = {},
    private val onLike: (CommunityQuiz) -> Unit = {},
    private val onEdit: (CommunityQuiz) -> Unit = {},
    private val onDelete: (CommunityQuiz) -> Unit = {},
    private val currentUserId: String = ""
) : RecyclerView.Adapter<CommunityQuizAdapter.QuizViewHolder>() {

    private val items = mutableListOf<CommunityQuiz>()

    fun replaceAll(newItems: List<CommunityQuiz>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun append(newItems: List<CommunityQuiz>) {
        if (newItems.isEmpty()) return
        val start = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }

    fun updateQuiz(updated: CommunityQuiz) {
        val index = items.indexOfFirst { it.quizId == updated.quizId }
        if (index >= 0) {
            items[index] = updated
            notifyItemChanged(index)
        }
    }

    fun removeQuiz(quizId: String) {
        val index = items.indexOfFirst { it.quizId == quizId }
        if (index >= 0) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun setTheme(newTheme: ReaderTheme) {
        theme = newTheme
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_quiz, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(items[position], theme, mode)
    }

    override fun getItemCount(): Int = items.size

    inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: View = itemView.findViewById(R.id.cardCommunityQuiz)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvQuizTitle)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvQuizAuthor)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvQuizCategory)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvQuizDescription)
        private val tvMeta: TextView = itemView.findViewById(R.id.tvQuizMeta)
        private val btnLike: MaterialButton = itemView.findViewById(R.id.btnQuizLike)
        private val btnTake: MaterialButton = itemView.findViewById(R.id.btnQuizTake)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnQuizEdit)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnQuizDelete)

        fun bind(quiz: CommunityQuiz, theme: ReaderTheme, mode: QuizListMode) {
            val ctx = itemView.context

            tvTitle.text = quiz.title
            tvAuthor.text = "by ${quiz.authorName.ifBlank { "Anonymous" }}"
            tvCategory.text = quiz.categories.joinToString(", ") { it.displayName }
            tvDescription.text = quiz.description.ifBlank { "No description provided." }
            tvMeta.text = "${quiz.questions.size} questions • ❤ ${quiz.likes} • ⬇ ${quiz.downloads}"

            val owner = mode == QuizListMode.OWNED
            btnLike.visibility = if (owner) View.GONE else View.VISIBLE
            btnTake.visibility = if (owner) View.GONE else View.VISIBLE
            btnEdit.visibility = if (owner) View.VISIBLE else View.GONE
            btnDelete.visibility = if (owner) View.VISIBLE else View.GONE

            // Disable like button if user has already liked
            val hasLiked = currentUserId in quiz.likedBy
            btnLike.isEnabled = !hasLiked
            btnLike.alpha = if (hasLiked) 0.5f else 1.0f
            btnLike.text = if (hasLiked) "Liked" else "Like"

            btnLike.setOnClickListener { onLike(quiz) }
            btnTake.setOnClickListener { onTakeQuiz(quiz) }
            btnEdit.setOnClickListener { onEdit(quiz) }
            btnDelete.setOnClickListener { onDelete(quiz) }

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

            card.setBackgroundColor(cardBg)
            tvTitle.setTextColor(textColor)
            tvAuthor.setTextColor(subtextColor)
            tvDescription.setTextColor(subtextColor)
            tvMeta.setTextColor(subtextColor)
            tvCategory.setTextColor(Color.WHITE) // Keep white on blue badge for now, but ensure badge is visible
            
            // If the theme is Light or Sepia, maybe use a darker badge or darker text?
            // Actually, white on blue is standard. Let's ensure the badge background is accent color.
            tvCategory.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.accent)))

            btnLike.setTextColor(ContextCompat.getColor(ctx, R.color.accent))
            btnTake.setTextColor(ContextCompat.getColor(ctx, R.color.white))
            btnEdit.setTextColor(ContextCompat.getColor(ctx, R.color.accent))
            btnDelete.setTextColor(Color.parseColor("#C53030"))
        }
    }
}
