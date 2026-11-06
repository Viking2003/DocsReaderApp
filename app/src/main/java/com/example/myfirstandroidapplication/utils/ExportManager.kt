package com.example.myfirstandroidapplication.utils

import android.content.Context
import android.net.Uri
import com.example.myfirstandroidapplication.data.DocRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExportData(
    val exportDate: String,
    val bookmarks: List<BookmarkExport>,
    val notes: List<NoteExport>
)

data class BookmarkExport(
    val pageId: Int,
    val chapterTitle: String,
    val category: String,
    val bookmarkedAt: String
)

data class NoteExport(
    val pageId: Int,
    val chapterTitle: String,
    val category: String,
    val noteContent: String,
    val createdAt: String
)

class ExportManager(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    suspend fun exportToJSON(
        bookmarks: List<Int>,
        notes: Map<Int, String>
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val allPages = DocRepository.getAllPages()
            val exportData = ExportData(
                exportDate = dateFormat.format(Date()),
                bookmarks = bookmarks.map { pageId ->
                    val page = allPages.find { it.id == pageId }
                    BookmarkExport(
                        pageId = pageId,
                        chapterTitle = page?.chapterTitle ?: "Unknown",
                        category = page?.category?.displayName ?: "Unknown",
                        bookmarkedAt = dateFormat.format(Date())
                    )
                },
                notes = notes.map { (pageId, noteContent) ->
                    val page = allPages.find { it.id == pageId }
                    NoteExport(
                        pageId = pageId,
                        chapterTitle = page?.chapterTitle ?: "Unknown",
                        category = page?.category?.displayName ?: "Unknown",
                        noteContent = noteContent,
                        createdAt = dateFormat.format(Date())
                    )
                }
            )

            val json = gson.toJson(exportData)
            val fileName = "docs_reader_export_${System.currentTimeMillis()}.json"
            val file = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { it.write(json.toByteArray()) }

            Result.success(Uri.fromFile(file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportToCSV(
        bookmarks: List<Int>,
        notes: Map<Int, String>
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val allPages = DocRepository.getAllPages()
            val fileName = "docs_reader_export_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)

            FileOutputStream(file).use { outputStream ->
                val writer = outputStream.bufferedWriter()
                // Write bookmarks section
                writer.write("=== BOOKMARKS ===\n")
                writer.write("Page ID,Chapter Title,Category,Bookmarked At\n")
                bookmarks.forEach { pageId ->
                    val page = allPages.find { it.id == pageId }
                    val line = "${pageId},\"${page?.chapterTitle ?: "Unknown"}\",\"${page?.category?.displayName ?: "Unknown"}\",\"${dateFormat.format(Date())}\"\n"
                    writer.write(line)
                }

                // Write notes section
                writer.write("\n=== NOTES ===\n")
                writer.write("Page ID,Chapter Title,Category,Note Content,Created At\n")
                notes.forEach { (pageId, noteContent) ->
                    val page = allPages.find { it.id == pageId }
                    val escapedNote = noteContent.replace("\"", "\"\"")
                    val line = "${pageId},\"${page?.chapterTitle ?: "Unknown"}\",\"${page?.category?.displayName ?: "Unknown"}\",\"$escapedNote\",\"${dateFormat.format(Date())}\"\n"
                    writer.write(line)
                }
                writer.flush()
            }

            Result.success(Uri.fromFile(file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportToMarkdown(
        bookmarks: List<Int>,
        notes: Map<Int, String>
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val allPages = DocRepository.getAllPages()
            val fileName = "docs_reader_export_${System.currentTimeMillis()}.md"
            val file = File(context.getExternalFilesDir(null), fileName)

            FileOutputStream(file).use { outputStream ->
                val writer = outputStream.bufferedWriter()
                writer.write("# Docs Reader Export\n\n")
                writer.write("Export Date: ${dateFormat.format(Date())}\n\n")

                // Write bookmarks section
                writer.write("## Bookmarks\n\n")
                if (bookmarks.isEmpty()) {
                    writer.write("No bookmarks\n\n")
                } else {
                    bookmarks.forEach { pageId ->
                        val page = allPages.find { it.id == pageId }
                        writer.write("- **${page?.chapterTitle ?: "Unknown"}** (${page?.category?.displayName ?: "Unknown"})\n")
                    }
                    writer.write("\n")
                }

                // Write notes section
                writer.write("## Notes\n\n")
                if (notes.isEmpty()) {
                    writer.write("No notes\n\n")
                } else {
                    notes.forEach { (pageId, noteContent) ->
                        val page = allPages.find { it.id == pageId }
                        writer.write("### ${page?.chapterTitle ?: "Unknown"} (${page?.category?.displayName ?: "Unknown"})\n\n")
                        writer.write("$noteContent\n\n")
                    }
                }
                writer.flush()
            }

            Result.success(Uri.fromFile(file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
