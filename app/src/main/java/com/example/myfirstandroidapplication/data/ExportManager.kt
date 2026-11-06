package com.example.myfirstandroidapplication.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.myfirstandroidapplication.models.DocPage
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportManager(private val context: Context) {

    private val preferencesManager = PreferencesManager(context)

    data class ExportData(
        val bookmarks: List<DocPage>,
        val notes: List<Pair<DocPage, String>>
    )

    private suspend fun collectExportData(): ExportData {
        val bookmarkIds = preferencesManager.bookmarks.first()
        val allPages = DocRepository.jniPages + DocRepository.androidRulesPages

        val bookmarkedPages = allPages.filter { bookmarkIds.contains(it.id) }

        val notesList = mutableListOf<Pair<DocPage, String>>()
        for (page in allPages) {
            val note = preferencesManager.getNote(page.id)
            if (note.isNotEmpty()) {
                notesList.add(Pair(page, note))
            }
        }

        return ExportData(bookmarkedPages, notesList)
    }

    suspend fun exportData(format: String) {
        val data = collectExportData()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val (filename, content, mimeType) = when (format.lowercase(Locale.getDefault())) {
            "json" -> Triple("DocReader_Export_$timeStamp.json", generateJson(data), "application/json")
            "csv" -> Triple("DocReader_Export_$timeStamp.csv", generateCsv(data), "text/csv")
            "markdown", "md" -> Triple("DocReader_Export_$timeStamp.md", generateMarkdown(data), "text/markdown")
            else -> Triple("DocReader_Export_$timeStamp.txt", generateMarkdown(data), "text/plain")
        }

        val exportDir = File(context.getExternalFilesDir("Exports") ?: context.filesDir, "Exports").apply { mkdirs() }
        val file = File(exportDir, filename)
        file.writeText(content)

        shareExportedFile(file, mimeType)
    }

    private fun generateJson(data: ExportData): String {
        val root = JSONObject()

        val bookmarksArray = JSONArray()
        for (page in data.bookmarks) {
            val item = JSONObject().apply {
                put("id", page.id)
                put("category", page.category.name)
                put("chapter", page.chapterTitle)
                put("subtitle", page.subtitle)
            }
            bookmarksArray.put(item)
        }
        root.put("bookmarks", bookmarksArray)

        val notesArray = JSONArray()
        for ((page, noteText) in data.notes) {
            val item = JSONObject().apply {
                put("page_id", page.id)
                put("category", page.category.name)
                put("chapter", page.chapterTitle)
                put("note", noteText)
            }
            notesArray.put(item)
        }
        root.put("notes", notesArray)

        return root.toString(2)
    }

    private fun generateCsv(data: ExportData): String {
        val sb = StringBuilder()
        sb.append("Type,Page ID,Category,Chapter Title,Note Content\n")

        for (page in data.bookmarks) {
            val safeTitle = page.chapterTitle.replace("\"", "\"\"")
            sb.append("\"Bookmark\",${page.id},\"${page.category.name}\",\"$safeTitle\",\"\"\n")
        }

        for ((page, noteText) in data.notes) {
            val safeTitle = page.chapterTitle.replace("\"", "\"\"")
            val safeNote = noteText.replace("\"", "\"\"").replace("\n", " ")
            sb.append("\"Note\",${page.id},\"${page.category.name}\",\"$safeTitle\",\"$safeNote\"\n")
        }

        return sb.toString()
    }

    private fun generateMarkdown(data: ExportData): String {
        val sb = StringBuilder()
        sb.append("# 📖 DocReader Notes & Bookmarks Export\n\n")
        sb.append("_Exported on ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}_\n\n")

        sb.append("## 🔖 Bookmarks (${data.bookmarks.size})\n\n")
        if (data.bookmarks.isEmpty()) {
            sb.append("_No bookmarks saved._\n\n")
        } else {
            for (page in data.bookmarks) {
                sb.append("- **${page.chapterTitle}** (${page.category.name}) - _Page ID: ${page.id}_\n")
            }
            sb.append("\n")
        }

        sb.append("## 📝 Notes (${data.notes.size})\n\n")
        if (data.notes.isEmpty()) {
            sb.append("_No notes saved._\n\n")
        } else {
            for ((page, noteText) in data.notes) {
                sb.append("### ${page.chapterTitle} (${page.category.name})\n")
                sb.append("> $noteText\n\n")
            }
        }

        return sb.toString()
    }

    private fun shareExportedFile(file: File, mimeType: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "DocReader Export: ${file.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Save or Share Exported File")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}