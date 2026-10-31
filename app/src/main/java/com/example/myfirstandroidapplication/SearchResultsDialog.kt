package com.example.myfirstandroidapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.example.myfirstandroidapplication.models.DocPage
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SearchResultsDialog : BottomSheetDialogFragment() {

    private var onResultClickListener: ((DocPage) -> Unit)? = null
    private var currentTheme: ReaderTheme = ReaderTheme.SEPIA
    private var searchQuery: String = ""

    fun setOnResultClickListener(listener: (DocPage) -> Unit) {
        onResultClickListener = listener
    }

    fun setTheme(theme: ReaderTheme) {
        currentTheme = theme
    }

    fun setSearchQuery(query: String) {
        searchQuery = query
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_search_results, container, false)

        // Sync theme from parent activity
        (activity as? MainActivity)?.let {
            currentTheme = it.currentTheme
        }

        val tvSearchTitle = view.findViewById<TextView>(R.id.tvSearchTitle)
        val tvSearchQuery = view.findViewById<TextView>(R.id.tvSearchQuery)
        val tvNoResults = view.findViewById<TextView>(R.id.tvNoResults)
        val recyclerView = view.findViewById<RecyclerView>(R.id.searchResultsRecyclerView)

        tvSearchQuery.text = "Searching for: \"$searchQuery\""

        val matchingPages = performSearch(searchQuery)

        if (matchingPages.isEmpty()) {
            tvNoResults.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvNoResults.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            val adapter = SearchResultsListAdapter(matchingPages, currentTheme) { page ->
                onResultClickListener?.invoke(page)
                dismiss()
            }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
        }

        applyTheme(view, tvSearchTitle, tvSearchQuery, tvNoResults)

        return view
    }

    private fun performSearch(query: String): List<DocPage> {
        val allPages = com.example.myfirstandroidapplication.data.DocRepository.jniPages +
                com.example.myfirstandroidapplication.data.DocRepository.androidRulesPages
        return allPages.filter { page ->
            page.bodyText.contains(query, ignoreCase = true) ||
            page.chapterTitle.contains(query, ignoreCase = true) ||
            page.subtitle.contains(query, ignoreCase = true)
        }
    }

    private fun applyTheme(view: View, tvSearchTitle: TextView, tvSearchQuery: TextView, tvNoResults: TextView) {
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
        tvSearchTitle.setTextColor(ContextCompat.getColor(requireContext(), textColor))
        tvSearchQuery.setTextColor(ContextCompat.getColor(requireContext(), textColor))
        tvNoResults.setTextColor(ContextCompat.getColor(requireContext(), textColor))
    }
}

class SearchResultsListAdapter(
    private val pages: List<com.example.myfirstandroidapplication.models.DocPage>,
    private var theme: ReaderTheme,
    private val onResultClick: (DocPage) -> Unit
) : RecyclerView.Adapter<SearchResultsListAdapter.SearchResultViewHolder>() {

    fun setTheme(newTheme: ReaderTheme) {
        theme = newTheme
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(pages[position], theme, onResultClick)
    }

    override fun getItemCount(): Int = pages.size

    class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvResultNumber: TextView = itemView.findViewById(R.id.tvResultNumber)
        private val tvResultTitle: TextView = itemView.findViewById(R.id.tvResultTitle)
        private val tvResultSubtitle: TextView = itemView.findViewById(R.id.tvResultSubtitle)

        fun bind(
            page: com.example.myfirstandroidapplication.models.DocPage,
            theme: ReaderTheme,
            onClick: (DocPage) -> Unit
        ) {
            val ctx = itemView.context
            val position = adapterPosition

            tvResultNumber.text = "${position + 1}."
            tvResultTitle.text = page.chapterTitle
            tvResultSubtitle.text = page.subtitle

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

            tvResultNumber.setTextColor(textColor)
            tvResultTitle.setTextColor(textColor)
            tvResultSubtitle.setTextColor(subtextColor)

            itemView.setOnClickListener {
                onClick(page)
            }
        }
    }
}
