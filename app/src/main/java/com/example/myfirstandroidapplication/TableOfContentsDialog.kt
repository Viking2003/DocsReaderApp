package com.example.myfirstandroidapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstandroidapplication.adapters.ReaderAdapter
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.example.myfirstandroidapplication.data.DocRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TableOfContentsDialog : BottomSheetDialogFragment() {

    private var onChapterClickListener: ((Int) -> Unit)? = null
    private var currentTheme: ReaderTheme = ReaderTheme.SEPIA

    fun setOnChapterClickListener(listener: (Int) -> Unit) {
        onChapterClickListener = listener
    }

    fun setTheme(theme: ReaderTheme) {
        currentTheme = theme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_table_of_contents, container, false)

        // Sync theme from parent activity
        (activity as? MainActivity)?.let {
            currentTheme = it.currentTheme
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.tocRecyclerView)
        val tvDialogTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val adapter = TocAdapter(DocRepository.getAllPages(), currentTheme) { pageIndex ->
            onChapterClickListener?.invoke(pageIndex)
            dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        applyTheme(view, tvDialogTitle)

        return view
    }

    private fun applyTheme(view: View, tvDialogTitle: TextView) {
        val bgRes = when (currentTheme) {
            ReaderTheme.SEPIA -> R.color.kindle_sepia_bg
            ReaderTheme.LIGHT -> R.color.kindle_light_bg
            ReaderTheme.DARK -> R.color.kindle_dark_bg
        }
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), bgRes))

        val textColor = when (currentTheme) {
            ReaderTheme.SEPIA -> R.color.kindle_sepia_text
            ReaderTheme.LIGHT -> R.color.kindle_light_text
            ReaderTheme.DARK -> R.color.kindle_dark_text
        }
        tvDialogTitle.setTextColor(ContextCompat.getColor(requireContext(), textColor))
    }
}

class TocAdapter(
    private val pages: List<com.example.myfirstandroidapplication.models.DocPage>,
    private var theme: ReaderTheme,
    private val onChapterClick: (Int) -> Unit
) : RecyclerView.Adapter<TocAdapter.TocViewHolder>() {

    fun setTheme(newTheme: ReaderTheme) {
        theme = newTheme
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TocViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_toc_chapter, parent, false)
        return TocViewHolder(view)
    }

    override fun onBindViewHolder(holder: TocViewHolder, position: Int) {
        holder.bind(pages[position], theme, onChapterClick)
    }

    override fun getItemCount(): Int = pages.size

    class TocViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvChapterNumber: TextView = itemView.findViewById(R.id.tvChapterNumber)
        private val tvChapterTitle: TextView = itemView.findViewById(R.id.tvChapterTitle)
        private val tvChapterSubtitle: TextView = itemView.findViewById(R.id.tvChapterSubtitle)

        fun bind(
            page: com.example.myfirstandroidapplication.models.DocPage,
            theme: ReaderTheme,
            onClick: (Int) -> Unit
        ) {
            val ctx = itemView.context
            val position = adapterPosition

            tvChapterNumber.text = "${position + 1}."
            tvChapterTitle.text = page.chapterTitle
            tvChapterSubtitle.text = page.subtitle

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

            tvChapterNumber.setTextColor(textColor)
            tvChapterTitle.setTextColor(textColor)
            tvChapterSubtitle.setTextColor(subtextColor)

            itemView.setOnClickListener {
                onClick(position)
            }
        }
    }
}
