package com.example.myfirstandroidapplication.data

import com.example.myfirstandroidapplication.FirebaseService
import com.example.myfirstandroidapplication.models.CommunityQuestion
import com.example.myfirstandroidapplication.models.CommunityQuiz
import com.example.myfirstandroidapplication.models.DocCategory
import com.example.myfirstandroidapplication.models.QuizDifficulty
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.withTimeout

/**
 * Single point of access for the "community_quizzes" Firestore collection.
 *
 * Every operation returns a [Result] so the UI can tell the difference between
 * "there is nothing here" and "the call failed" (offline, missing rules,
 * permission denied, misconfigured Firebase, ...).
 */
object CommunityQuizRepository {

    const val COLLECTION = "community_quizzes"
    const val DEFAULT_PAGE_SIZE = 20L
    const val MAX_QUESTIONS_PER_QUIZ = 20

    /**
     * Loads one page of quizzes, newest first. Pass the [createdAt] of the last
     * item you already have to fetch the next page.
     */
    suspend fun fetchQuizzes(
        pageSize: Long = DEFAULT_PAGE_SIZE,
        startAfterCreatedAt: Long? = null
    ): Result<List<CommunityQuiz>> = runCatching {
        val firestore = firestoreOrThrow()
        var query: Query = firestore.collection(COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(pageSize)
        if (startAfterCreatedAt != null) {
            query = query.startAfter(startAfterCreatedAt)
        }
        withTimeout(10000) { // 10 second timeout
            query.get().awaitTask().documents.map { it.toCommunityQuiz() }
        }
    }

    suspend fun fetchMyQuizzes(authorId: String): Result<List<CommunityQuiz>> = runCatching {
        val firestore = firestoreOrThrow()
        withTimeout(10000) { // 10 second timeout
            firestore.collection(COLLECTION)
                .whereEqualTo("authorId", authorId)
                .get()
                .awaitTask()
                .documents
                .map { it.toCommunityQuiz() }
                .sortedByDescending { it.createdAt }
        }
    }

    suspend fun fetchQuizById(quizId: String): Result<CommunityQuiz?> = runCatching {
        val firestore = firestoreOrThrow()
        val doc = firestore.collection(COLLECTION).document(quizId).get().awaitTask()
        if (doc.exists()) {
            doc.toCommunityQuiz()
        } else {
            null
        }
    }

    suspend fun uploadQuiz(quiz: CommunityQuiz): Result<String> = runCatching {
        val firestore = firestoreOrThrow()
        validate(quiz)
        val docId = quiz.quizId.ifBlank { firestore.collection(COLLECTION).document().id }
        val payload = quiz.copy(quizId = docId, createdAt = System.currentTimeMillis()).toMap()
        
        // Add timeout to prevent indefinite hanging
        withTimeout(10000) { // 10 second timeout
            firestore.collection(COLLECTION).document(docId).set(payload).awaitTask()
        }
        docId
    }

    suspend fun updateQuiz(quiz: CommunityQuiz): Result<Unit> = runCatching {
        val firestore = firestoreOrThrow()
        require(quiz.quizId.isNotBlank()) { "Cannot update a quiz that was never uploaded" }
        validate(quiz)
        withTimeout(10000) { // 10 second timeout
            firestore.collection(COLLECTION)
                .document(quiz.quizId)
                .set(quiz.toMap(), SetOptions.merge())
                .awaitTask()
        }
        Unit
    }

    suspend fun deleteQuiz(quizId: String): Result<Unit> = runCatching {
        val firestore = firestoreOrThrow()
        firestore.collection(COLLECTION).document(quizId).delete().awaitTask()
        Unit
    }

    suspend fun likeQuiz(quizId: String, userId: String): Result<Unit> = runCatching {
        val firestore = firestoreOrThrow()
        val docRef = firestore.collection(COLLECTION).document(quizId)
        
        // First check if user already liked this quiz
        val snapshot = docRef.get().awaitTask()
        @Suppress("UNCHECKED_CAST")
        val likedBy = (snapshot.get("likedBy") as? List<String>) ?: emptyList()
        
        if (userId in likedBy) {
            throw IllegalStateException("You have already liked this quiz")
        }
        
        // Add user to likedBy list and increment likes
        firestore.runTransaction { transaction ->
            val freshSnapshot = transaction.get(docRef)
            @Suppress("UNCHECKED_CAST")
            val freshLikedBy = (freshSnapshot.get("likedBy") as? List<String>) ?: emptyList()
            
            if (userId in freshLikedBy) {
                throw IllegalStateException("You have already liked this quiz")
            }
            
            transaction.update(docRef, mapOf(
                "likedBy" to FieldValue.arrayUnion(userId),
                "likes" to FieldValue.increment(1)
            ))
        }.awaitTask()
        Unit
    }

    suspend fun incrementDownloads(quizId: String): Result<Unit> = incrementField(quizId, "downloads")

    private suspend fun incrementField(quizId: String, field: String): Result<Unit> = runCatching {
        val firestore = firestoreOrThrow()
        firestore.collection(COLLECTION)
            .document(quizId)
            .update(field, FieldValue.increment(1))
            .awaitTask()
        Unit
    }

    private fun validate(quiz: CommunityQuiz) {
        require(quiz.title.isNotBlank()) { "Quiz title cannot be empty" }
        require(quiz.questions.isNotEmpty()) { "A quiz needs at least one question" }
        require(quiz.questions.size <= MAX_QUESTIONS_PER_QUIZ) {
            "A quiz can contain at most $MAX_QUESTIONS_PER_QUIZ questions"
        }
        quiz.questions.forEachIndexed { index, question ->
            require(question.questionText.isNotBlank()) { "Question ${index + 1} is missing its text" }
            require(question.options.size >= 2) { "Question ${index + 1} needs at least two options" }
            require(question.correctAnswerIndex in question.options.indices) {
                "Question ${index + 1} has an invalid correct answer"
            }
        }
    }

    private fun firestoreOrThrow() = FirebaseService.getFirestore()
        ?: throw IllegalStateException(
            "Firebase is unavailable. Check your internet connection and Firebase configuration."
        )

    private fun DocumentSnapshot.toCommunityQuiz(): CommunityQuiz {
        @Suppress("UNCHECKED_CAST")
        val categoryNames = (get("categories") as? List<String>) ?: listOf(getString("category") ?: DocCategory.JNI.name)
        val categories = categoryNames.map { name ->
            runCatching { DocCategory.valueOf(name) }.getOrDefault(DocCategory.JNI)
        }.distinct()

        @Suppress("UNCHECKED_CAST")
        val rawQuestions = get("questions") as? List<Map<String, Any>> ?: emptyList()

        @Suppress("UNCHECKED_CAST")
        val tags = (get("tags") as? List<String>) ?: emptyList()

        @Suppress("UNCHECKED_CAST")
        val likedBy = (get("likedBy") as? List<String>) ?: emptyList()

        return CommunityQuiz(
            quizId = getString("quizId") ?: id,
            authorId = getString("authorId") ?: "",
            authorName = getString("authorName") ?: "Anonymous",
            title = getString("title") ?: "Untitled Quiz",
            description = getString("description") ?: "",
            categories = categories,
            questions = rawQuestions.map { it.toCommunityQuestion() },
            likes = getLong("likes")?.toInt() ?: 0,
            downloads = getLong("downloads")?.toInt() ?: 0,
            createdAt = getLong("createdAt") ?: 0L,
            tags = tags,
            likedBy = likedBy
        )
    }

    private fun Map<String, Any>.toCommunityQuestion(): CommunityQuestion {
        @Suppress("UNCHECKED_CAST")
        val options = (this["options"] as? List<String>) ?: emptyList()
        val difficulty = runCatching { QuizDifficulty.valueOf(this["difficulty"] as? String ?: "") }
            .getOrDefault(QuizDifficulty.MEDIUM)
        val category = runCatching { DocCategory.valueOf(this["category"] as? String ?: "") }
            .getOrDefault(DocCategory.JNI)

        return CommunityQuestion(
            category = category,
            questionText = this["questionText"] as? String ?: "",
            options = options,
            correctAnswerIndex = (this["correctAnswerIndex"] as? Number)?.toInt() ?: 0,
            explanation = this["explanation"] as? String ?: "",
            difficulty = difficulty
        )
    }

    private fun CommunityQuiz.toMap(): Map<String, Any> = mapOf(
        "quizId" to quizId,
        "authorId" to authorId,
        "authorName" to authorName,
        "title" to title,
        "description" to description,
        "categories" to categories.map { it.name },
        "likes" to likes,
        "downloads" to downloads,
        "createdAt" to createdAt,
        "tags" to tags,
        "likedBy" to likedBy,
        "questions" to questions.map { q ->
            mapOf(
                "category" to q.category.name,
                "questionText" to q.questionText,
                "options" to q.options,
                "correctAnswerIndex" to q.correctAnswerIndex,
                "explanation" to q.explanation,
                "difficulty" to q.difficulty.name
            )
        }
    )
}
