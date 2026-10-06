package com.os95.app.domain.model

import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.PaperQuestionEntity

// =========================================================================
// 6. Time-to-Marks Intelligence Models
// =========================================================================

data class TimeToMarksChapterItem(
    val chapterId: String,
    val chapterName: String,
    val subjectId: String,
    val subjectName: String,
    val weakness: Float,                  // 0.0 to 1.0 (higher = weaker)
    val examWeightage: Float,             // e.g. 1.5 for High, 1.0 for Medium, 0.7 for Low
    val improvementPotential: Float,      // 0.0 to 1.0 based on recoverability of mistakes
    val confidenceFactor: Float,          // 0.2 to 1.0 based on test sample size
    val priorityScore: Float,             // weakness * exam_weightage * improvement_potential * confidence_factor
    val potentialGainPercentage: Float,   // e.g. 78% potential gain
    val expectedMarksImprovementPerHour: Float, // e.g. 2.4 marks / hour
    val estimatedHoursRequired: Float,    // hours needed to master this chapter
    val activeLostMarks: Float
)

data class TimeToMarksReport(
    val currentEstimatedScore: Float,
    val targetScore: Float,
    val marksGap: Float,
    val totalEstimatedStudyHoursNeeded: Float,
    val expectedMarksImprovementPerHour: Float,
    val bestUseDurationMinutes: Int,
    val bestUseTitle: String,             // e.g. "Best use of your next 60 minutes"
    val topImpactChapters: List<TimeToMarksChapterItem>,
    val disclaimer: String = "Deterministic academic estimate based on mistake history and syllabus weightage. Scores are not guaranteed."
)

// =========================================================================
// 7. Adaptive Re-Test Models
// =========================================================================

data class AdaptiveRetestConfig(
    val subjectId: String? = null,
    val targetMarks: Float = 30f,
    val durationMinutes: Int = 45,
    val title: String = "Adaptive Diagnostic Re-Test"
)

data class AdaptiveRetestGenerationResult(
    val paper: PaperEntity,
    val questions: List<PaperQuestionEntity>,
    val weakAreaCount: Int,
    val recentlyIncorrectCount: Int,
    val mixedRevisionCount: Int,
    val targetWeakTopics: List<String>
)

data class AdaptiveRetestCompletionSummary(
    val paperId: String,
    val marksObtained: Float,
    val totalMarks: Float,
    val accuracyPercentage: Float,
    val mistakesFixedCount: Int,
    val marksRecovered: Float,
    val remainingWeakTopics: List<String>,
    val masteryPercentage: Float,
    val recommendedNextAction: String
)

// =========================================================================
// 8. Exam Readiness Simulator Models
// =========================================================================

data class ExamSimulatorConfig(
    val subjectId: String? = null,
    val durationMinutes: Int = 90,
    val totalMarks: Float = 100f,
    val difficulty: String = "BALANCED", // EASY, MEDIUM, HARD, BALANCED
    val selectedChapterIds: Set<String> = emptySet(),
    val allowedQuestionTypes: Set<String> = emptySet()
)

data class ExamSimulationSubmission(
    val paperId: String,
    val answers: Map<Int, String>,             // question index -> student response text
    val marksAwardedMap: Map<Int, Float>,      // question index -> marks obtained
    val timeSpentSeconds: Int,
    val timePerQuestionSeconds: Map<Int, Int> = emptyMap(),
    val markedForReviewIndices: Set<Int> = emptySet()
)

data class ExamReadinessResult(
    val readinessScore: Float,                 // 0.0 to 100.0%
    val score: Float,
    val percentage: Float,
    val accuracy: Float,
    val avgTimePerQuestionSeconds: Float,
    val easyQuestionsMissedCount: Int,
    val weakChapterNames: List<String>,
    val conceptualMistakesCount: Int,
    val calculationMistakesCount: Int,
    val carelessMistakesCount: Int,
    val timeManagementWarning: String?,
    val readinessFactors: Map<String, Float>   // Factor breakdown (Scores, Accuracy, Coverage, Time, Retests)
)

// =========================================================================
// 9. Last-7-Days Mode Models
// =========================================================================

data class Last7DaysTask(
    val id: String,
    val dayNumber: Int,                        // 7 down to 1
    val title: String,
    val description: String,
    val category: String,                      // "PRIORITY_CHAPTER", "QUICK_REVISION", "PREVIOUS_MISTAKES", "ADAPTIVE_RETEST", "SHORT_RECALL"
    val estimatedMinutes: Int,
    val isCompleted: Boolean = false,
    val targetSubject: String? = null,
    val targetChapter: String? = null
)

data class Last7DaysDailyPlan(
    val dayNumber: Int,                        // 7 to 1
    val dayLabel: String,                      // e.g. "Day 7", "Day 1 (Final Review)"
    val isToday: Boolean,
    val priorityChapters: List<String>,
    val tasks: List<Last7DaysTask>,
    val completedTasksCount: Int,
    val totalTasksCount: Int
)

data class Last7DaysDashboard(
    val isActive: Boolean,                     // true when exam <= 7 days away
    val daysRemaining: Int,
    val targetExamDateTimestamp: Long,
    val currentDayNumber: Int,                 // 7 to 1
    val todayTopThreeTasks: List<Last7DaysTask>,
    val dailyPlans: List<Last7DaysDailyPlan>,
    val overallWeekCompletionPercentage: Float
)

// =========================================================================
// 10. 95% Command Center Models
// =========================================================================

data class CommandCenterAction(
    val title: String,                         // e.g. "Revise Mauryan Age for 35 minutes"
    val reason: String,                        // e.g. "High weightage + your accuracy is 61%"
    val actionType: String,                    // "REVISE_TOPIC", "RETEST_WEAKNESS", "RECALL_RADAR", "PRACTICE_PAPER"
    val targetSubjectId: String? = null,
    val targetChapterId: String? = null,
    val suggestedMinutes: Int = 30
)

data class CommandCenterSnapshot(
    val studentName: String,
    val currentPredictedPercentage: Float,
    val targetPercentage: Float,
    val percentageGap: Float,
    val marksGap: Float?,
    val examReadinessPercentage: Float,
    val daysRemaining: Int?,
    val primaryAction: CommandCenterAction,
    val todayStudyMinutes: Int,
    val todayTargetMinutes: Int,
    val todayRetestsTaken: Int,
    val todayTopicsRevised: Int,
    val topThreeWeakChapters: List<Pair<String, Float>>, // Chapter Name to Weakness Score
    val nextExamTitle: String?,
    val recentTestScores: List<Float>,
    val accuracyTrend: String,                 // "IMPROVING", "DECLINING", "STABLE"
    val subjectBreakdown: List<Pair<String, Float>> // Subject Name to Score %
)
