package com.example.myfirstandroidapplication.models

import java.io.Serializable

data class CommunityQuiz(
    val quizId: String,
    val authorId: String,
    val authorName: String,
    val title: String,
    val description: String,
    val categories: List<DocCategory>,
    val questions: List<CommunityQuestion>,
    val likes: Int = 0,
    val downloads: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val likedBy: List<String> = emptyList() // Track which users have liked this quiz
) : Serializable

data class CommunityQuestion(
    val category: DocCategory,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: QuizDifficulty
) : Serializable
