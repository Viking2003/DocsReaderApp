package com.example.myfirstandroidapplication.adapters

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.InputDevice
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstandroidapplication.R
import com.example.myfirstandroidapplication.models.DocCategory
import com.example.myfirstandroidapplication.models.DocPage

enum class ReaderTheme {
    SEPIA, LIGHT, DARK
}

class ReaderAdapter(
    private var pages: List<DocPage>,
    private var currentTheme: ReaderTheme = ReaderTheme.SEPIA,
    private var fontSizeSp: Float = 16f
) : RecyclerView.Adapter<ReaderAdapter.ReaderViewHolder>() {

    private var searchQuery: String? = null

    fun updatePages(newPages: List<DocPage>) {
        pages = newPages
        notifyDataSetChanged()
    }

    fun setTheme(theme: ReaderTheme) {
        currentTheme = theme
        notifyDataSetChanged()
    }

    fun setFontSize(sizeSp: Float) {
        fontSizeSp = sizeSp
        notifyDataSetChanged()
    }

    fun setSearchQuery(query: String?) {
        if (searchQuery != query) {
            searchQuery = query
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reader_page, parent, false)
        return ReaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReaderViewHolder, position: Int) {
        holder.bind(pages[position], currentTheme, fontSizeSp, searchQuery)
    }

    override fun getItemCount(): Int = pages.size

    class ReaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val scrollView: ScrollView = itemView.findViewById(R.id.pageScrollView)
        private val pageContainer: LinearLayout = itemView.findViewById(R.id.pageContainer)
        private val tvCategoryBadge: TextView = itemView.findViewById(R.id.tvCategoryBadge)
        private val tvPageTitle: TextView = itemView.findViewById(R.id.tvPageTitle)
        private val tvPageSubtitle: TextView = itemView.findViewById(R.id.tvPageSubtitle)
        private val pageDivider: View = itemView.findViewById(R.id.pageDivider)
        private val tvPageContent: TextView = itemView.findViewById(R.id.tvPageContent)
        
        private val codeBoxContainer: LinearLayout = itemView.findViewById(R.id.codeBoxContainer)
        private val tvCodeBoxTitle: TextView = itemView.findViewById(R.id.tvCodeBoxTitle)
        private val tvCodeBoxContent: TextView = itemView.findViewById(R.id.tvCodeBoxContent)
        
        private val calloutBoxContainer: LinearLayout = itemView.findViewById(R.id.calloutBoxContainer)
        private val tvCalloutTitle: TextView = itemView.findViewById(R.id.tvCalloutTitle)
        private val tvCalloutContent: TextView = itemView.findViewById(R.id.tvCalloutContent)

        init {
            scrollView.isNestedScrollingEnabled = true
            scrollView.setOnGenericMotionListener { _, event ->
                if (event.action == MotionEvent.ACTION_SCROLL && event.isFromSource(InputDevice.SOURCE_MOUSE)) {
                    val scrollAmount = -event.getAxisValue(MotionEvent.AXIS_VSCROLL) * 48
                    scrollView.smoothScrollBy(0, scrollAmount.toInt())
                    true
                } else {
                    false
                }
            }
        }

        fun bind(page: DocPage, theme: ReaderTheme, fontSize: Float, searchQuery: String?) {
            val ctx = itemView.context

            // Badge & Titles
            tvCategoryBadge.text = when (page.category) {
                DocCategory.JNI -> "JNI DOCUMENTATION"
                DocCategory.ANDROID_RULES -> "ANDROID PRINCIPLE"
                DocCategory.FLUTTER -> "FLUTTER DEVELOPMENT"
            }
            tvPageTitle.text = page.chapterTitle
            tvPageSubtitle.text = page.subtitle

            // Apply search highlighting to body text
            if (!searchQuery.isNullOrEmpty()) {
                tvPageContent.text = highlightText(page.bodyText, searchQuery, theme, ctx)
            } else {
                tvPageContent.text = page.bodyText
            }

            // Code Snippet Box
            if (page.codeSnippet != null) {
                codeBoxContainer.visibility = View.VISIBLE
                tvCodeBoxTitle.text = page.codeTitle ?: "CODE EXAMPLE"
                tvCodeBoxContent.text = page.codeSnippet
            } else {
                codeBoxContainer.visibility = View.GONE
            }

            // Callout Box
            if (page.keyTakeaway != null) {
                calloutBoxContainer.visibility = View.VISIBLE
                tvCalloutTitle.text = "💡 KEY TAKEAWAY / BEST PRACTICE"
                tvCalloutContent.text = page.keyTakeaway
            } else {
                calloutBoxContainer.visibility = View.GONE
            }

            // Adjust Font Sizes
            tvPageTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize + 6f)
            tvPageContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
            tvCodeBoxContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, (fontSize - 2f).coerceAtLeast(12f))
            tvCalloutContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, (fontSize - 1f).coerceAtLeast(13f))

            // Apply Theme Palette
            when (theme) {
                ReaderTheme.SEPIA -> {
                    val bg = ContextCompat.getColor(ctx, R.color.kindle_sepia_bg)
                    val text = ContextCompat.getColor(ctx, R.color.kindle_sepia_text)
                    val subtext = ContextCompat.getColor(ctx, R.color.kindle_sepia_subtext)
                    val divider = ContextCompat.getColor(ctx, R.color.kindle_sepia_divider)

                    scrollView.setBackgroundColor(bg)
                    tvPageTitle.setTextColor(text)
                    tvPageSubtitle.setTextColor(subtext)
                    tvPageContent.setTextColor(text)
                    pageDivider.setBackgroundColor(divider)

                    codeBoxContainer.setBackgroundColor(ContextCompat.getColor(ctx, R.color.kindle_sepia_code_bg))
                    tvCodeBoxContent.setTextColor(text)

                    calloutBoxContainer.setBackgroundColor(ContextCompat.getColor(ctx, R.color.kindle_sepia_card))
                    tvCalloutContent.setTextColor(text)
                }
                ReaderTheme.LIGHT -> {
                    val bg = ContextCompat.getColor(ctx, R.color.kindle_light_bg)
                    val text = ContextCompat.getColor(ctx, R.color.kindle_light_text)
                    val subtext = ContextCompat.getColor(ctx, R.color.kindle_light_subtext)
                    val divider = ContextCompat.getColor(ctx, R.color.kindle_light_divider)

                    scrollView.setBackgroundColor(bg)
                    tvPageTitle.setTextColor(text)
                    tvPageSubtitle.setTextColor(subtext)
                    tvPageContent.setTextColor(text)
                    pageDivider.setBackgroundColor(divider)

                    codeBoxContainer.setBackgroundColor(ContextCompat.getColor(ctx, R.color.kindle_light_code_bg))
                    tvCodeBoxContent.setTextColor(text)

                    calloutBoxContainer.setBackgroundColor(ContextCompat.getColor(ctx, R.color.kindle_light_card))
                    tvCalloutContent.setTextColor(text)
                }
                ReaderTheme.DARK -> {
                    val bg = ContextCompat.getColor(ctx, R.color.kindle_dark_bg)
                    val text = ContextCompat.getColor(ctx, R.color.kindle_dark_text)
                    val subtext = ContextCompat.getColor(ctx, R.color.kindle_dark_subtext)
                    val divider = ContextCompat.getColor(ctx, R.color.kindle_dark_divider)

                    scrollView.setBackgroundColor(bg)
                    tvPageTitle.setTextColor(text)
                    tvPageSubtitle.setTextColor(subtext)
                    tvPageContent.setTextColor(text)
                    pageDivider.setBackgroundColor(divider)

                    codeBoxContainer.setBackgroundColor(ContextCompat.getColor(ctx, R.color.kindle_dark_code_bg))
                    tvCodeBoxContent.setTextColor(text)

                    calloutBoxContainer.setBackgroundColor(ContextCompat.getColor(ctx, R.color.kindle_dark_card))
                    tvCalloutContent.setTextColor(text)
                }
            }
        }

        private fun highlightText(text: String, query: String, theme: ReaderTheme, ctx: android.content.Context): SpannableString {
            val spannable = SpannableString(text)
            val lowerCaseText = text.lowercase()
            val lowerCaseQuery = query.lowercase()

            var startIndex = 0
            while (startIndex < lowerCaseText.length) {
                val foundIndex = lowerCaseText.indexOf(lowerCaseQuery, startIndex)
                if (foundIndex == -1) break

                val endIndex = foundIndex + query.length
                val highlightColor = when (theme) {
                    ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.search_highlight_sepia)
                    ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.search_highlight_light)
                    ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.search_highlight_dark)
                }
                spannable.setSpan(BackgroundColorSpan(highlightColor), foundIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                // Set text color to dark for highlighted portions to ensure readability
                spannable.setSpan(ForegroundColorSpan(Color.parseColor("#1A1A1A")), foundIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                startIndex = endIndex
            }

            return spannable
        }
    }
}