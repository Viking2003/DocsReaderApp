package com.example.myfirstandroidapplication.models

enum class QuizDifficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard")
}

data class QuizQuestion(
    val id: Int,
    val category: DocCategory,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: QuizDifficulty = QuizDifficulty.MEDIUM
)
