package com.example.myfirstandroidapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.example.myfirstandroidapplication.data.DocRepository
import com.example.myfirstandroidapplication.models.DocPage
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SearchDialog : BottomSheetDialogFragment() {

    private var onResultClickListener: ((Int) -> Unit)? = null
    private var currentTheme: ReaderTheme = ReaderTheme.SEPIA
    private var allPages: List<DocPage> = DocRepository.getAllPages()
    private var filteredPages: List<DocPage> = allPages
    private var initialQuery: String = ""

    fun setOnResultClickListener(listener: (Int) -> Unit) {
        onResultClickListener = listener
    }

    fun setTheme(theme: ReaderTheme) {
        currentTheme = theme
    }

    fun setInitialQuery(query: String) {
        initialQuery = query
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_search, container, false)

        // Sync theme from parent activity
        (activity as? MainActivity)?.let {
            currentTheme = it.currentTheme
        }

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val recyclerView = view.findViewById<RecyclerView>(R.id.searchResultsRecyclerView)
        val tvNoResults = view.findViewById<TextView>(R.id.tvNoResults)

        val adapter = SearchResultsAdapter(filteredPages, currentTheme) { pageIndex ->
            onResultClickListener?.invoke(pageIndex)
            dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Set initial query if provided
        if (initialQuery.isNotEmpty()) {
            etSearch.setText(initialQuery)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                if (query.isEmpty()) {
                    filteredPages = allPages
                    tvNoResults.visibility = View.GONE
                } else {
                    filteredPages = allPages.filter { page ->
                        page.chapterTitle.lowercase().contains(query) ||
                        page.subtitle.lowercase().contains(query) ||
                        page.bodyText.lowercase().contains(query) ||
                        (page.codeSnippet?.lowercase()?.contains(query) == true) ||
                        (page.keyTakeaway?.lowercase()?.contains(query) == true)
                    }
                    tvNoResults.visibility = if (filteredPages.isEmpty()) View.VISIBLE else View.GONE
                }
                adapter.updateData(filteredPages)
            }
        })

        applyTheme(view)

        return view
    }

    private fun applyTheme(view: View) {
        val bgRes = when (currentTheme) {
            ReaderTheme.SEPIA -> R.color.kindle_sepia_bg
            ReaderTheme.LIGHT -> R.color.kindle_light_bg
            ReaderTheme.DARK -> R.color.kindle_dark_bg
        }
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), bgRes))
    }
}

class SearchResultsAdapter(
    private var pages: List<DocPage>,
    private var theme: ReaderTheme,
    private val onResultClick: (Int) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {

    fun updateData(newPages: List<DocPage>) {
        pages = newPages
        notifyDataSetChanged()
    }

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
            page: DocPage,
            theme: ReaderTheme,
            onClick: (Int) -> Unit
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
                onClick(page.id - 1) // Convert to 0-based index
            }
        }
    }
}
