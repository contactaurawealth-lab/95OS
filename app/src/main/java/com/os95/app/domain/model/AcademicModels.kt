package com.os95.app.domain.model

enum class TopicMasteryState(val label: String) {
    NOT_STARTED("Not Started"),
    LEARNING("Learning"),
    REVISED("Revised"),
    MASTERED("Mastered")
}

enum class ExamRelevance(val label: String) {
    LOW("Low Weight"),
    MEDIUM("Medium Weight"),
    HIGH("High Weight")
}

enum class LossCategory(val label: String) {
    DIDNT_KNOW("Didn't know concept"),
    FORGOT("Forgot formula / fact"),
    CONCEPT_ERROR("Concept misunderstanding"),
    CALCULATION_ERROR("Calculation error"),
    MISREAD("Misread question"),
    CARELESS_MISTAKE("Careless mistake"),
    POOR_PRESENTATION("Poor presentation / steps"),
    TIME_SHORTAGE("Time shortage"),
    INCOMPLETE_ANSWER("Incomplete answer"),
    OTHER("Other")
}

enum class QuestionType(val label: String) {
    MCQ("Multiple Choice"),
    SHORT_ANSWER("Short Answer"),
    LONG_ANSWER("Long Answer"),
    NUMERICAL("Numerical")
}

enum class QuestionDifficulty(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard")
}

enum class PaperStatus {
    DRAFT,
    READY,
    IN_PROGRESS,
    COMPLETED
}

enum class RecallRating(val grade: Int) {
    AGAIN(1), // Total blackout
    HARD(2),  // Correct with serious difficulty
    GOOD(3),  // Correct with slight hesitation
    EASY(4)   // Instant perfect recall
}

data class StudentProfile(
    val id: String = "student_profile",
    val name: String,
    val targetPercentage: Float = 95.0f,
    val gradeLevel: String = "",
    val division: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class StudyPreferences(
    val id: String = "study_preferences",
    val dailyTargetMinutes: Int = 120,
    val defaultExamDurationMinutes: Int = 90,
    val examModeLockdownEnabled: Boolean = true,
    val warningAtTenMinutes: Boolean = true
)
