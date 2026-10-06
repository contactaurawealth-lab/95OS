package com.os95.app.domain.model

// --- Marks Gap Planner Models ---

enum class RecoveryPriorityLevel(val label: String) {
    VERY_HIGH("Very High"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

data class RecoveryOpportunity(
    val id: String, // topicId or chapterId
    val subjectId: String,
    val subjectName: String,
    val chapterId: String? = null,
    val chapterName: String? = null,
    val topicId: String? = null,
    val topicName: String? = null,
    val lostMarks: Float,
    val priorityScore: Float,
    val priorityLevel: RecoveryPriorityLevel,
    val dominantMistakeCategory: String,
    val actionDescription: String
)

data class MarksGapPlan(
    val targetPercentage: Float,
    val currentPercentage: Float,
    val percentageGap: Float,
    val totalExamMarks: Float? = null,
    val projectedMarks: Float? = null,
    val marksNeeded: Float? = null,
    val potentialRecoverableMarks: Float,
    val lostMarksByCategory: Map<String, Float> = emptyMap(),
    val topOpportunities: List<RecoveryOpportunity> = emptyList(),
    val hasSufficientData: Boolean = true
)

// --- Forgetting Radar Models ---

enum class RetentionRiskLevel(val label: String) {
    CRITICAL("Critical"),
    AT_RISK("At Risk"),
    WATCH("Watch"),
    STABLE("Stable")
}

data class TopicRetentionRisk(
    val topicId: String,
    val topicName: String,
    val chapterId: String,
    val chapterName: String,
    val subjectId: String,
    val subjectName: String,
    val riskLevel: RetentionRiskLevel,
    val cardsDueCount: Int,
    val totalCards: Int,
    val averageRecallScore: Float, // 0..100%
    val daysSinceLastReview: Int,
    val recommendedCardCount: Int,
    val primaryAction: String = "Review Now"
)

data class ForgettingRadarSnapshot(
    val criticalCount: Int,
    val atRiskCount: Int,
    val watchCount: Int,
    val stableCount: Int,
    val topics: List<TopicRetentionRisk> = emptyList(),
    val totalCardsTracked: Int = 0
)

// --- Previous Paper Analyzer Models ---

enum class PaperTrendDirection(val label: String) {
    IMPROVING("Improving"),
    DECLINING("Declining"),
    STABLE("Stable"),
    INSUFFICIENT_DATA("Insufficient Data")
}

data class PaperScoreSummary(
    val paperId: String,
    val paperTitle: String,
    val subjectName: String,
    val marksObtained: Float,
    val totalMarks: Float,
    val percentage: Float,
    val dateMillis: Long
)

data class SubjectAnalysisItem(
    val subjectId: String,
    val subjectName: String,
    val paperCount: Int,
    val averagePercentage: Float,
    val bestPercentage: Float
)

data class ChapterAnalysisItem(
    val chapterId: String,
    val chapterName: String,
    val subjectName: String,
    val accuracyPercentage: Float,
    val marksLostTotal: Float
)

data class QuestionTypeAnalysisItem(
    val questionType: String,
    val timesTested: Int,
    val accuracyPercentage: Float
)

data class RepeatedWeaknessItem(
    val topicId: String,
    val topicName: String,
    val chapterName: String,
    val subjectName: String,
    val totalLostMarks: Float,
    val papersAffectedCount: Int,
    val paperLostBreakdown: List<Pair<String, Float>> // (Paper Title, Marks Lost)
)

data class PaperAnalysis(
    val totalPapers: Int,
    val hasEnoughDataForTrend: Boolean,
    val averagePercentage: Float,
    val bestPercentage: Float,
    val worstPercentage: Float,
    val recentAveragePercentage: Float,
    val improvementPercentage: Float,
    val trendDirection: PaperTrendDirection,
    val paperHistory: List<PaperScoreSummary> = emptyList(),
    val subjectPerformance: List<SubjectAnalysisItem> = emptyList(),
    val chapterPerformance: List<ChapterAnalysisItem> = emptyList(),
    val questionTypePerformance: List<QuestionTypeAnalysisItem> = emptyList(),
    val mistakePatternBreakdown: Map<String, Int> = emptyMap(),
    val repeatedWeaknesses: List<RepeatedWeaknessItem> = emptyList()
)

// --- 15-Minute Rescue Mode Models ---

enum class RescueActionType(val label: String) {
    RECALL_REVIEW("Active Recall"),
    MISTAKE_FIX("Mistake Elimination"),
    RAPID_TOPIC_READ("High-Yield Review"),
    MINI_TEST("Mini Test")
}

data class RescueBlock(
    val startMinute: Int,
    val endMinute: Int,
    val title: String,
    val actionType: RescueActionType,
    val targetEntityId: String, // topicId or subjectId
    val targetName: String,
    val itemCount: Int,
    val description: String
)

data class RescuePlan(
    val totalDurationMinutes: Int,
    val blocks: List<RescueBlock>,
    val estimatedRecoverableMarks: Float
)

data class RescueCompletionSummary(
    val durationMinutes: Int,
    val completedActionsCount: Int,
    val topicsCoveredCount: Int,
    val cardsReviewedCount: Int,
    val mistakesResolvedCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// --- Recovery Score Models ---

data class TopicRecoveryItem(
    val topicId: String,
    val topicName: String,
    val chapterName: String,
    val subjectName: String,
    val previouslyLost: Float,
    val currentlyLost: Float,
    val recoveredMarks: Float
)

data class RecoveryScoreReport(
    val totalRecoveredMarks: Float,
    val hasComparablePapers: Boolean,
    val previousLostMarks: Float,
    val currentLostMarks: Float,
    val recoveryByCategory: Map<String, Float> = emptyMap(),
    val recoveryByTopic: List<TopicRecoveryItem> = emptyList(),
    val lostMarksTrend: List<Pair<String, Float>> = emptyList() // (Paper Title, Lost Marks)
)

// --- Unified Marks Recovery Snapshot ---

data class MarksRecoverySnapshot(
    val marksGap: MarksGapPlan,
    val forgettingRadar: ForgettingRadarSnapshot,
    val paperAnalysis: PaperAnalysis,
    val rescuePlan: RescuePlan,
    val recoveryScore: RecoveryScoreReport,
    val timestamp: Long = System.currentTimeMillis()
)
