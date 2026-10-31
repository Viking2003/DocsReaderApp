package com.example.myfirstandroidapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.myfirstandroidapplication.adapters.ReaderTheme
import com.example.myfirstandroidapplication.data.PreferencesManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesDialog : DialogFragment() {

    private var pageId: Int = 0
    private var currentTheme: ReaderTheme = ReaderTheme.SEPIA
    private var onNoteSavedListener: (() -> Unit)? = null
    private lateinit var preferencesManager: PreferencesManager

    fun setPageId(id: Int) {
        pageId = id
    }

    fun setTheme(theme: ReaderTheme) {
        currentTheme = theme
    }

    fun setOnNoteSavedListener(listener: () -> Unit) {
        onNoteSavedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_notes, container, false)

        preferencesManager = PreferencesManager(requireContext())

        // Sync theme from parent activity
        (activity as? MainActivity)?.let {
            currentTheme = it.currentTheme
        }

        val etNote = view.findViewById<EditText>(R.id.etNote)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)
        val btnDelete = view.findViewById<MaterialButton>(R.id.btnDelete)
        val tvDialogTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val btnClose = view.findViewById<ImageButton>(R.id.btnClose)

        tvDialogTitle.text = if (pageId > 0) "Notes for Page $pageId" else "Add Note"

        btnClose.setOnClickListener {
            dismiss()
        }

        // Load existing note
        CoroutineScope(Dispatchers.Main).launch {
            val existingNote = preferencesManager.getNote(pageId)
            etNote.setText(existingNote)
            btnDelete.visibility = if (existingNote.isNotEmpty()) View.VISIBLE else View.GONE
        }

        btnSave.setOnClickListener {
            val noteText = etNote.text.toString()
            CoroutineScope(Dispatchers.Main).launch {
                preferencesManager.saveNote(pageId, noteText)
                onNoteSavedListener?.invoke()
                dismiss()
            }
        }

        btnDelete.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                preferencesManager.deleteNote(pageId)
                etNote.setText("")
                btnDelete.visibility = View.GONE
                onNoteSavedListener?.invoke()
            }
        }

        applyTheme(view)

        return view
    }

    private fun applyTheme(view: View) {
        val ctx = requireContext()
        val bgRes = when (currentTheme) {
            ReaderTheme.SEPIA -> R.color.kindle_sepia_bg
            ReaderTheme.LIGHT -> R.color.kindle_light_bg
            ReaderTheme.DARK -> R.color.kindle_dark_bg
        }

        val textColor = when (currentTheme) {
            ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_text)
            ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_text)
            ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_text)
        }

        val subtextColor = when (currentTheme) {
            ReaderTheme.SEPIA -> ContextCompat.getColor(ctx, R.color.kindle_sepia_subtext)
            ReaderTheme.LIGHT -> ContextCompat.getColor(ctx, R.color.kindle_light_subtext)
            ReaderTheme.DARK -> ContextCompat.getColor(ctx, R.color.kindle_dark_subtext)
        }

        view.setBackgroundColor(ContextCompat.getColor(ctx, bgRes))
        val etNote = view.findViewById<EditText>(R.id.etNote)
        val tvDialogTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val btnClose = view.findViewById<ImageButton>(R.id.btnClose)

        tvDialogTitle.setTextColor(textColor)
        etNote.setTextColor(textColor)
        etNote.setHintTextColor(subtextColor)
        btnClose.setColorFilter(textColor)
    }
}