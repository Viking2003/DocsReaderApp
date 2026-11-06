package com.example.myfirstandroidapplication.models

enum class DocCategory(val displayName: String) {
    JNI("JNI Documentation"),
    ANDROID_RULES("Android Principles & Rules"),
    FLUTTER("Flutter Development")
}

data class DocPage(
    val id: Int,
    val category: DocCategory,
    val chapterTitle: String,
    val subtitle: String,
    val bodyText: String,
    val codeSnippet: String? = null,
    val codeTitle: String? = "CODE EXAMPLE",
    val keyTakeaway: String? = null
)