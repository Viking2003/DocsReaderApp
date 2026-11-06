package com.example.myfirstandroidapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.myfirstandroidapplication.adapters.ReaderAdapter
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.example.myfirstandroidapplication.data.AchievementManager
import com.example.myfirstandroidapplication.data.DocRepository
import com.example.myfirstandroidapplication.data.PreferencesManager
import kotlinx.coroutines.flow.first
import com.example.myfirstandroidapplication.models.DocCategory
import com.example.myfirstandroidapplication.models.DocPage
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReaderFragment : Fragment(R.layout.fragment_reader) {

    private var readerRootContainer: LinearLayout? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager2? = null
    private var readingProgress: LinearProgressIndicator? = null
    private var tvPageIndicator: TextView? = null
    private var btnPrevPage: ImageButton? = null
    private var btnNextPage: ImageButton? = null
    private var btnBookmark: ImageButton? = null
    private var btnTableOfContents: ImageButton? = null
    private var btnQuickQuiz: ImageButton? = null
    private var btnNotes: ImageButton? = null
    private var etSearch: EditText? = null
    private var btnSearch: ImageButton? = null
    private var searchBarContainer: LinearLayout? = null

    private lateinit var readerAdapter: ReaderAdapter
    private var currentCategoryIndex = 0
    private var currentPages: List<DocPage> = DocRepository.jniPages
    var currentTheme: ReaderTheme = ReaderTheme.SEPIA
    private var currentFontSizeSp: Float = 16f
    private lateinit var preferencesManager: PreferencesManager

    private var readingTimeHandler: Handler? = null
    private var pageStartTime: Long = 0L
    private val readingTimeRunnable = object : Runnable {
        override fun run() {
            val elapsedMinutes = ((System.currentTimeMillis() - pageStartTime) / 60000L).coerceAtLeast(1L)
            CoroutineScope(Dispatchers.IO).launch {
                preferencesManager.addReadingTime(elapsedMinutes)
            }
            pageStartTime = System.currentTimeMillis()
            readingTimeHandler?.postDelayed(this, 60000)
        }
    }

    private var lastSearchTime: Long = 0L
    private val SEARCH_DEBOUNCE_MS = 500L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        initViews(view)
        setupTabs()

        // Sync with MainActivity's current theme
        (activity as? MainActivity)?.let {
            currentTheme = it.currentTheme
        }

        applyTheme(currentTheme)

        // Load bookmarks and reading progress
        loadBookmarks()
        loadReadingProgress()

        // Ensure tab and data are synced when fragment becomes visible
        syncTabAndData()

        // Start reading time tracking
        pageStartTime = System.currentTimeMillis()
        readingTimeHandler = Handler(Looper.getMainLooper())
        readingTimeHandler?.postDelayed(readingTimeRunnable, 60000)

        // Post ViewPager2 setup to ensure instant initial window draw (preventing cold start ANR)
        view.post {
            if (isAdded) {
                setupViewPager()
                setupListeners()
            }
        }
    }

    private fun syncTabAndData() {
        // Ensure the tab matches the current category index
        tabLayout?.selectTab(tabLayout?.getTabAt(currentCategoryIndex))
        // Ensure the data matches the current category
        currentPages = when (currentCategoryIndex) {
            0 -> DocRepository.jniPages
            1 -> DocRepository.androidRulesPages
            2 -> DocRepository.flutterPages
            else -> DocRepository.jniPages
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        readingTimeHandler?.removeCallbacks(readingTimeRunnable)
        val elapsedMinutes = ((System.currentTimeMillis() - pageStartTime) / 60000L)
        if (elapsedMinutes > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                preferencesManager.addReadingTime(elapsedMinutes)
            }
        }
    }

    private fun initViews(view: View) {
        readerRootContainer = view.findViewById(R.id.readerRootContainer)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        readingProgress = view.findViewById(R.id.readingProgress)
        tvPageIndicator = view.findViewById(R.id.tvPageIndicator)
        btnPrevPage = view.findViewById(R.id.btnPrevPage)
        btnNextPage = view.findViewById(R.id.btnNextPage)
        btnBookmark = view.findViewById(R.id.btnBookmark)
        btnTableOfContents = view.findViewById(R.id.btnTableOfContents)
        btnQuickQuiz = view.findViewById(R.id.btnQuickQuiz)
        btnNotes = view.findViewById(R.id.btnNotes)
        etSearch = view.findViewById(R.id.etSearch)
        btnSearch = view.findViewById(R.id.btnSearch)
        searchBarContainer = view.findViewById(R.id.searchBarContainer)
    }

    private fun setupTabs() {
        tabLayout?.addTab(tabLayout!!.newTab().setText("1. JNI Documentation"))
        tabLayout?.addTab(tabLayout!!.newTab().setText("2. Android Principles"))
        tabLayout?.addTab(tabLayout!!.newTab().setText("3. Flutter Development"))

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                if (position != currentCategoryIndex) {
                    currentCategoryIndex = position
                    currentPages = when (position) {
                        0 -> DocRepository.jniPages
                        1 -> DocRepository.androidRulesPages
                        2 -> DocRepository.flutterPages
                        else -> DocRepository.jniPages
                    }
                    if (::readerAdapter.isInitialized) {
                        readerAdapter.updatePages(currentPages)
                        readerAdapter.setSearchQuery(null) // Clear search highlight when switching tabs
                    }
                    viewPager?.setCurrentItem(0, true)
                    updatePageIndicator(0)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupViewPager() {
        val viewPagerRef = viewPager ?: return
        readerAdapter = ReaderAdapter(currentPages, currentTheme, currentFontSizeSp)
        viewPagerRef.adapter = readerAdapter

        viewPagerRef.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updatePageIndicator(position)
                saveCurrentProgress(position)
                checkBookmarkStatus(position)
            }
        })

        // Intercept mouse wheel scroll events at ViewPager2 level and scroll active ScrollView
        viewPagerRef.setOnGenericMotionListener { _, event ->
            if (event.action == MotionEvent.ACTION_SCROLL && event.isFromSource(InputDevice.SOURCE_MOUSE)) {
                val verticalScroll = -event.getAxisValue(MotionEvent.AXIS_VSCROLL) * 50
                val recyclerView = viewPagerRef.getChildAt(0) as? RecyclerView
                val currentViewHolder = recyclerView?.findViewHolderForAdapterPosition(viewPagerRef.currentItem)
                val scrollView = currentViewHolder?.itemView?.findViewById<ScrollView>(R.id.pageScrollView)
                scrollView?.smoothScrollBy(0, verticalScroll.toInt())
                true
            } else {
                false
            }
        }
    }

    private fun setupListeners() {
        btnPrevPage?.setOnClickListener {
            val currentItem = viewPager?.currentItem ?: 0
            if (currentItem > 0) {
                viewPager?.setCurrentItem(currentItem - 1, true)
            }
        }

        btnNextPage?.setOnClickListener {
            val currentItem = viewPager?.currentItem ?: 0
            if (currentItem < currentPages.size - 1) {
                viewPager?.setCurrentItem(currentItem + 1, true)
            }
        }

        btnBookmark?.setOnClickListener {
            val pageIndex = viewPager?.currentItem ?: 0
            val page = currentPages[pageIndex]
            CoroutineScope(Dispatchers.Main).launch {
                if (preferencesManager.isBookmarked(page.id)) {
                    preferencesManager.removeBookmark(page.id)
                    btnBookmark?.setImageResource(R.drawable.ic_bookmark_border)
                    Toast.makeText(requireContext(), "Bookmark removed", Toast.LENGTH_SHORT).show()
                } else {
                    preferencesManager.addBookmark(page.id)
                    btnBookmark?.setImageResource(R.drawable.ic_bookmark)
                    AchievementManager.getInstance(requireContext()).trackBookmarkCreated()
                    Toast.makeText(requireContext(), "Page bookmarked", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnTableOfContents?.setOnClickListener {
            val tocDialog = TableOfContentsDialog().apply {
                setTheme(currentTheme)
                setOnChapterClickListener { pageIndex ->
                    // Determine which category the page belongs to
                    val allPages = DocRepository.jniPages + DocRepository.androidRulesPages
                    val selectedPage = allPages[pageIndex]
                    val catIndex = if (DocRepository.jniPages.contains(selectedPage)) 0 else 1

                    // Switch to the correct tab if needed
                    if (catIndex != currentCategoryIndex) {
                        tabLayout?.selectTab(tabLayout?.getTabAt(catIndex))
                    }

                    // Navigate to the selected page
                    val pageIndexInCategory = if (catIndex == 0) {
                        DocRepository.jniPages.indexOf(selectedPage)
                    } else {
                        DocRepository.androidRulesPages.indexOf(selectedPage)
                    }
                    viewPager?.setCurrentItem(pageIndexInCategory, true)
                }
            }
            tocDialog.show(parentFragmentManager, "TableOfContentsDialog")
        }

        btnQuickQuiz?.setOnClickListener {
            // Switch to quiz tab in MainActivity
            (activity as? MainActivity)?.let { main ->
                main.mainNavTabs.selectTab(main.mainNavTabs.getTabAt(1))
                // Also filter quiz by current category
                val quizFrag = main.supportFragmentManager.findFragmentByTag("QUIZ") as? QuizFragment
                val category = when (currentCategoryIndex) {
                    0 -> DocCategory.JNI
                    1 -> DocCategory.ANDROID_RULES
                    2 -> DocCategory.FLUTTER
                    else -> DocCategory.JNI
                }
                quizFrag?.filterQuestionsByChapter(category)
            }
        }

        btnNotes?.setOnClickListener {
            val pageIndex = viewPager?.currentItem ?: 0
            val page = currentPages[pageIndex]
            val notesDialog = NotesDialog().apply {
                setPageId(page.id)
                setTheme(currentTheme)
                setOnNoteSavedListener {
                    Toast.makeText(requireContext(), "Note saved successfully", Toast.LENGTH_SHORT).show()
                }
            }
            notesDialog.show(parentFragmentManager, "NotesDialog")
        }

        btnSearch?.setOnClickListener {
            performSearch()
        }

        etSearch?.setOnEditorActionListener { _, actionId, event ->
            // Only handle IME action to avoid duplicate triggers
            // This covers both soft keyboard Search/Done button and hardware Enter key
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun performSearch() {
        val currentTime = System.currentTimeMillis()
        // Debounce: prevent duplicate calls within 500ms
        if (currentTime - lastSearchTime < SEARCH_DEBOUNCE_MS) {
            return
        }
        lastSearchTime = currentTime

        val query = etSearch?.text.toString().trim()
        if (query.isNotEmpty()) {
            showSearchResultsDialog(query)
        } else {
            // Clear search highlight if search is empty
            if (::readerAdapter.isInitialized) {
                readerAdapter.setSearchQuery(null)
            }
        }
    }

    private fun showSearchResultsDialog(query: String) {
        val searchDialog = SearchResultsDialog().apply {
            setTheme(currentTheme)
            setSearchQuery(query)
            setOnResultClickListener { page ->
                // Determine which category the page belongs to
                val catIndex = if (DocRepository.jniPages.contains(page)) 0 else 1

                // Switch to the correct tab if needed
                if (catIndex != currentCategoryIndex) {
                    tabLayout?.selectTab(tabLayout?.getTabAt(catIndex))
                }

                // Navigate to the selected page
                val pageIndexInCategory = if (catIndex == 0) {
                    DocRepository.jniPages.indexOf(page)
                } else {
                    DocRepository.androidRulesPages.indexOf(page)
                }
                viewPager?.setCurrentItem(pageIndexInCategory, true)

                // Apply search highlighting to the adapter
                if (::readerAdapter.isInitialized) {
                    readerAdapter.setSearchQuery(query)
                }
            }
        }
        searchDialog.show(parentFragmentManager, "SearchResultsDialog")
    }

    private fun saveCurrentProgress(position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            preferencesManager.saveReadingProgress(currentCategoryIndex, position)
            if (preferencesManager.cloudSyncEnabled.first()) {
                preferencesManager.syncToCloud()
            }
        }
    }

    private fun loadReadingProgress() {
        CoroutineScope(Dispatchers.Main).launch {
            preferencesManager.readingProgress.collect { progress ->
                if (progress.categoryIndex != currentCategoryIndex) {
                    currentCategoryIndex = progress.categoryIndex
                    tabLayout?.selectTab(tabLayout?.getTabAt(currentCategoryIndex))
                }
                if (progress.pageIndex > 0 && progress.pageIndex < currentPages.size) {
                    viewPager?.setCurrentItem(progress.pageIndex, false)
                }
            }
        }
    }

    private fun checkBookmarkStatus(position: Int) {
        val page = currentPages[position]
        CoroutineScope(Dispatchers.Main).launch {
            val isBookmarked = preferencesManager.isBookmarked(page.id)
            if (isBookmarked) {
                btnBookmark?.setImageResource(R.drawable.ic_bookmark)
            } else {
                btnBookmark?.setImageResource(R.drawable.ic_bookmark_border)
            }
        }
    }

    private fun loadBookmarks() {
        checkBookmarkStatus(viewPager?.currentItem ?: 0)
    }

    fun applyTheme(theme: ReaderTheme) {
        currentTheme = theme
        if (::readerAdapter.isInitialized) {
            readerAdapter.setTheme(theme)
        }

        val ctx = context ?: return
        val bgRes = when (theme) {
            ReaderTheme.SEPIA -> R.color.kindle_sepia_bg
            ReaderTheme.LIGHT -> R.color.kindle_light_bg
            ReaderTheme.DARK -> R.color.kindle_dark_bg
        }
        readerRootContainer?.setBackgroundColor(ContextCompat.getColor(ctx, bgRes))
        viewPager?.let {
            updatePageIndicator(it.currentItem)
        }
    }

    fun setFontSize(sizeSp: Float) {
        currentFontSizeSp = sizeSp
        if (::readerAdapter.isInitialized) {
            readerAdapter.setFontSize(sizeSp)
        }
    }

    private fun updatePageIndicator(position: Int) {
        val totalPages = currentPages.size
        val pageNum = position + 1

        val themeLabel = when (currentTheme) {
            ReaderTheme.SEPIA -> "Sepia Mode"
            ReaderTheme.LIGHT -> "Light Mode"
            ReaderTheme.DARK -> "Night Mode"
        }

        tvPageIndicator?.text = "Page $pageNum of $totalPages • $themeLabel"

        val progressPercent = ((pageNum.toFloat() / totalPages.toFloat()) * 100).toInt()
        readingProgress?.progress = progressPercent

        btnPrevPage?.alpha = if (position > 0) 1.0f else 0.3f
        btnPrevPage?.isEnabled = position > 0

        btnNextPage?.alpha = if (position < totalPages - 1) 1.0f else 0.3f
        btnNextPage?.isEnabled = position < totalPages - 1
    }
}