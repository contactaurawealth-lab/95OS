package com.os95.app.domain.model

import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.QuestionBankEntity

enum class PaperDifficultyMode(val displayName: String, val easyPct: Float, val mediumPct: Float, val hardPct: Float) {
    EASY("Easy (Foundation)", 0.70f, 0.30f, 0.00f),
    MEDIUM("Standard Medium", 0.20f, 0.60f, 0.20f),
    HARD("High Difficulty", 0.00f, 0.30f, 0.70f),
    MIXED("Balanced Exam (30/50/20)", 0.30f, 0.50f, 0.20f)
}

enum class RepetitionPolicy(val displayName: String, val description: String) {
    AVOID_RECENT("Avoid recently tested", "Deprioritize questions used in recent practice papers"),
    ALLOW_REPETITION("Allow repetition", "Treat all questions equally regardless of prior usage"),
    STRICTLY_NEW("Strictly new questions", "Only select questions that have never been tested before")
}

data class PaperBlueprintRequest(
    val title: String,
    val subjectId: String,
    val chapterIds: List<String> = emptyList(),
    val topicIds: List<String> = emptyList(),
    val totalMarks: Float = 50.0f,
    val durationMinutes: Int = 60,
    val difficultyMode: PaperDifficultyMode = PaperDifficultyMode.MIXED,
    val allowedQuestionTypes: Set<String> = emptySet(),
    val repetitionPolicy: RepetitionPolicy = RepetitionPolicy.AVOID_RECENT,
    val adaptiveWeakTopicWeighting: Boolean = true
)

data class PaperSection(
    val name: String,
    val description: String,
    val questions: List<QuestionBankEntity>,
    val sectionMarks: Float
)

data class GeneratedPaper(
    val paper: PaperEntity,
    val sections: List<PaperSection>,
    val questions: List<QuestionBankEntity>,
    val chapterMarksCoverage: Map<String, Float>,
    val difficultyBreakdown: Map<String, Float>,
    val adaptiveRationale: String? = null
)

sealed class PaperGenerationResult {
    data class Success(val generatedPaper: GeneratedPaper) : PaperGenerationResult()
    data class Failure(
        val reason: String,
        val recoverySuggestions: List<String>
    ) : PaperGenerationResult()
}

data class PaperValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)

data class QuestionResultInput(
    val questionId: String?,
    val questionText: String,
    val chapterId: String,
    val topicId: String?,
    val marksAllocated: Float,
    val marksLost: Float,
    val lossCategory: String = "CARELESS_MISTAKE",
    val notes: String = ""
) {
    val isMistake: Boolean get() = marksLost > 0f
}
