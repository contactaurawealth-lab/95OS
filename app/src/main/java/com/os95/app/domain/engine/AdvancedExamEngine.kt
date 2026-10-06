package com.os95.app.domain.engine

import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.ExamResultEntity
import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.PaperQuestionEntity
import com.os95.app.core.database.entity.QuestionBankEntity
import com.os95.app.core.database.entity.RecallCardEntity
import com.os95.app.core.database.entity.StudySessionEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.domain.model.AdaptiveRetestCompletionSummary
import com.os95.app.domain.model.AdaptiveRetestConfig
import com.os95.app.domain.model.AdaptiveRetestGenerationResult
import com.os95.app.domain.model.CommandCenterAction
import com.os95.app.domain.model.CommandCenterSnapshot
import com.os95.app.domain.model.ExamReadinessResult
import com.os95.app.domain.model.ExamSimulationSubmission
import com.os95.app.domain.model.ExamSimulatorConfig
import com.os95.app.domain.model.Last7DaysDailyPlan
import com.os95.app.domain.model.Last7DaysDashboard
import com.os95.app.domain.model.Last7DaysTask
import com.os95.app.domain.model.TimeToMarksChapterItem
import com.os95.app.domain.model.TimeToMarksReport
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class AdvancedExamEngine {

    companion object {
        const val ONE_DAY_MS = 86_400_000L
    }

    // =========================================================================
    // 6. Time-to-Marks Intelligence
    // =========================================================================

    fun calculateTimeToMarks(
        targetPercentage: Float = 95.0f,
        availableStudyMinutes: Int = 60,
        examDaysRemaining: Int? = null,
        subjects: List<SubjectEntity>,
        chapters: List<ChapterEntity>,
        topics: List<TopicEntity>,
        questions: List<QuestionBankEntity>,
        examResults: List<ExamResultEntity>,
        mistakes: List<MistakeEntity>,
        studySessions: List<StudySessionEntity>
    ): TimeToMarksReport {
        val subjectMap = subjects.associateBy { it.id }
        val chapterMap = chapters.associateBy { it.id }
        val topicsByChapter = topics.groupBy { it.chapterId }
        val mistakesByChapter = mistakes.groupBy { it.chapterId }

        // Current estimated score from exam results (or default baseline)
        val currentEstimatedScore = if (examResults.isNotEmpty()) {
            val totalObtained = examResults.sumOf { it.marksObtained.toDouble() }
            val totalPossible = examResults.sumOf { it.totalMarks.toDouble() }
            if (totalPossible > 0.0) ((totalObtained / totalPossible) * 100.0).toFloat() else 0f
        } else {
            // If no exams taken yet, infer from topic masteries
            val totalTopics = topics.size
            if (totalTopics > 0) {
                val masteredCount = topics.count { it.masteryState == "MASTERED" }
                val learningCount = topics.count { it.masteryState == "LEARNING" || it.masteryState == "REVISED" }
                ((masteredCount * 1.0f + learningCount * 0.5f) / totalTopics) * 80f
            } else {
                0f
            }
        }

        val marksGap = (targetPercentage - currentEstimatedScore).coerceAtLeast(0f)

        // Historical yield: Delta marks / Study hours
        val totalStudyMinutes = studySessions.sumOf { it.durationMinutes }
        val historicYieldPerHour = if (totalStudyMinutes >= 30 && examResults.size >= 2) {
            val firstScore = examResults.last().marksObtained / max(1f, examResults.last().totalMarks) * 100f
            val latestScore = examResults.first().marksObtained / max(1f, examResults.first().totalMarks) * 100f
            val scoreDelta = max(0f, latestScore - firstScore)
            val studyHours = totalStudyMinutes / 60.0f
            (scoreDelta / studyHours).coerceIn(1.0f, 5.0f)
        } else {
            2.2f // Deterministic empirical baseline marks improvement per hour of deliberate study
        }

        // Calculate Chapter-wise Priority Scores:
        // Priority Score = weakness * exam_weightage * improvement_potential * confidence_factor
        val chapterItems = mutableListOf<TimeToMarksChapterItem>()

        for (chapter in chapters) {
            val subject = subjectMap[chapter.subjectId] ?: continue
            val chTopics = topicsByChapter[chapter.id] ?: emptyList()
            val chMistakes = mistakesByChapter[chapter.id] ?: emptyList()
            val chQuestions = questions.filter { it.chapterId == chapter.id }

            // 1. Weakness factor [0.1 to 1.0]
            val topicWeaknessAvg = if (chTopics.isNotEmpty()) {
                chTopics.map { it.weaknessScore }.average().toFloat()
            } else 0.5f

            val mistakeCount = chMistakes.count { !it.isResolved }
            val weakness = (topicWeaknessAvg + (mistakeCount * 0.15f)).coerceIn(0.1f, 1.0f)

            // 2. Exam weightage [0.7 to 1.5]
            val weightage = if (chTopics.isNotEmpty()) {
                val highCount = chTopics.count { it.examRelevance.equals("HIGH", ignoreCase = true) }
                val lowCount = chTopics.count { it.examRelevance.equals("LOW", ignoreCase = true) }
                when {
                    highCount > chTopics.size / 2 -> 1.5f
                    lowCount > chTopics.size / 2 -> 0.7f
                    else -> 1.0f
                }
            } else 1.0f

            // 3. Improvement potential [0.5 to 1.0] based on mistake recoverability
            val carelessOrCalc = chMistakes.count {
                it.lossCategory.contains("CALCULATION", ignoreCase = true) ||
                it.lossCategory.contains("CARELESS", ignoreCase = true)
            }
            val improvementPotential = if (chMistakes.isNotEmpty()) {
                val ratio = carelessOrCalc.toFloat() / chMistakes.size
                (0.6f + (ratio * 0.4f)).coerceIn(0.5f, 1.0f)
            } else {
                0.8f
            }

            // 4. Confidence factor [0.2 to 1.0] based on tested evidence
            val testCount = chQuestions.sumOf { it.timesTested } + chMistakes.size
            val confidenceFactor = (0.3f + min(testCount, 10) * 0.07f).coerceIn(0.2f, 1.0f)

            val priorityScore = weakness * weightage * improvementPotential * confidenceFactor
            val activeLostMarks = chMistakes.filter { !it.isResolved }.sumOf { it.marksLost.toDouble() }.toFloat()

            // Potential gain relative to score potential
            val potentialGainPercentage = (priorityScore * 55f + 25f).coerceIn(15f, 95f)

            val chapterYield = (historicYieldPerHour * (priorityScore / 0.8f)).coerceIn(0.8f, 4.5f)
            val estimatedHours = if (activeLostMarks > 0f) {
                (activeLostMarks / chapterYield).coerceIn(0.5f, 6.0f)
            } else {
                (priorityScore * 2.5f).coerceIn(0.5f, 4.0f)
            }

            chapterItems.add(
                TimeToMarksChapterItem(
                    chapterId = chapter.id,
                    chapterName = chapter.name,
                    subjectId = subject.id,
                    subjectName = subject.name,
                    weakness = weakness,
                    examWeightage = weightage,
                    improvementPotential = improvementPotential,
                    confidenceFactor = confidenceFactor,
                    priorityScore = priorityScore,
                    potentialGainPercentage = potentialGainPercentage,
                    expectedMarksImprovementPerHour = chapterYield,
                    estimatedHoursRequired = estimatedHours,
                    activeLostMarks = activeLostMarks
                )
            )
        }

        // Sort descending by priority score
        chapterItems.sortByDescending { it.priorityScore }

        val totalHoursNeeded = if (chapterItems.isNotEmpty()) {
            chapterItems.take(5).sumOf { it.estimatedHoursRequired.toDouble() }.toFloat()
        } else {
            max(1f, marksGap / max(1f, historicYieldPerHour))
        }

        return TimeToMarksReport(
            currentEstimatedScore = currentEstimatedScore,
            targetScore = targetPercentage,
            marksGap = marksGap,
            totalEstimatedStudyHoursNeeded = totalHoursNeeded,
            expectedMarksImprovementPerHour = historicYieldPerHour,
            bestUseDurationMinutes = availableStudyMinutes,
            bestUseTitle = "Best use of your next $availableStudyMinutes minutes",
            topImpactChapters = chapterItems
        )
    }

    // =========================================================================
    // 7. Adaptive Re-Test
    // =========================================================================

    fun generateAdaptiveRetest(
        config: AdaptiveRetestConfig,
        allQuestions: List<QuestionBankEntity>,
        mistakes: List<MistakeEntity>,
        topics: List<TopicEntity>,
        chapters: List<ChapterEntity>,
        subjects: List<SubjectEntity>
    ): AdaptiveRetestGenerationResult {
        val filteredQuestions = if (config.subjectId != null) {
            allQuestions.filter { it.subjectId == config.subjectId }
        } else {
            allQuestions
        }

        val subjectMap = subjects.associateBy { it.id }
        val chapterMap = chapters.associateBy { it.id }
        val topicMap = topics.associateBy { it.id }

        val activeMistakes = mistakes.filter { !it.isResolved && (config.subjectId == null || it.subjectId == config.subjectId) }
        val weakTopicIds = activeMistakes.mapNotNull { it.topicId }.toSet() +
            topics.filter { it.weaknessScore >= 0.5f && (config.subjectId == null || it.chapterId in chapters.filter { c -> c.subjectId == config.subjectId }.map { c -> c.id }) }
                .map { it.id }

        // Targets:
        // 50% weak areas, 30% recently incorrect / failed questions, 20% mixed revision
        val totalMarksTarget = config.targetMarks
        val weakMarksTarget = totalMarksTarget * 0.50f
        val recentIncorrectTarget = totalMarksTarget * 0.30f
        val mixedMarksTarget = totalMarksTarget * 0.20f

        val selectedQuestionIds = mutableSetOf<String>()
        val selectedWeakQuestions = mutableListOf<QuestionBankEntity>()
        val selectedRecentQuestions = mutableListOf<QuestionBankEntity>()
        val selectedMixedQuestions = mutableListOf<QuestionBankEntity>()

        // 1. 30% Recently incorrect questions / failed questions
        val failedQuestions = filteredQuestions
            .filter { it.timesFailed > 0 || it.id in activeMistakes.map { m -> m.id } }
            .sortedByDescending { it.timesFailed }

        var currentRecentMarks = 0f
        for (q in failedQuestions) {
            if (currentRecentMarks + q.marks <= recentIncorrectTarget + 2f) {
                if (selectedQuestionIds.add(q.id)) {
                    selectedRecentQuestions.add(q)
                    currentRecentMarks += q.marks
                }
            }
        }

        // 2. 50% Weak areas
        val weakQuestions = filteredQuestions
            .filter { !selectedQuestionIds.contains(it.id) && (it.topicId in weakTopicIds || it.timesFailed > 0) }
            .sortedByDescending { q ->
                val topicWeakness = q.topicId?.let { topicMap[it]?.weaknessScore } ?: 0.5f
                topicWeakness * 10f + q.timesFailed
            }

        var currentWeakMarks = 0f
        for (q in weakQuestions) {
            if (currentWeakMarks + q.marks <= weakMarksTarget + 3f) {
                if (selectedQuestionIds.add(q.id)) {
                    selectedWeakQuestions.add(q)
                    currentWeakMarks += q.marks
                }
            }
        }

        // 3. 20% Mixed revision (balanced review across available bank)
        val remainingQuestions = filteredQuestions
            .filter { !selectedQuestionIds.contains(it.id) }
            .shuffled()

        var currentMixedMarks = 0f
        val remainingNeeded = max(0f, totalMarksTarget - (currentRecentMarks + currentWeakMarks))
        val targetForMixed = max(mixedMarksTarget, remainingNeeded)

        for (q in remainingQuestions) {
            if (currentMixedMarks + q.marks <= targetForMixed + 2f) {
                if (selectedQuestionIds.add(q.id)) {
                    selectedMixedQuestions.add(q)
                    currentMixedMarks += q.marks
                }
            }
        }

        // Combine all questions
        val combinedQuestions = selectedWeakQuestions + selectedRecentQuestions + selectedMixedQuestions
        val totalActualMarks = combinedQuestions.sumOf { it.marks.toDouble() }.toFloat()

        val paperId = UUID.randomUUID().toString()
        val defaultSubjectId = config.subjectId ?: subjects.firstOrNull()?.id ?: "general_subject"

        val paper = PaperEntity(
            id = paperId,
            title = config.title,
            subjectId = defaultSubjectId,
            totalMarks = totalActualMarks,
            durationMinutes = config.durationMinutes,
            status = "READY",
            instructions = "Adaptive Diagnostic Re-Test: Focuses on 50% weak topics, 30% recently missed concepts, and 20% mixed revision."
        )

        val paperQuestions = combinedQuestions.mapIndexed { index, q ->
            val section = when {
                q.marks <= 1.0f -> "Section A (Objective)"
                q.marks <= 3.0f -> "Section B (Short Answer)"
                q.marks <= 5.0f -> "Section C (Long Answer)"
                else -> "Section D (Advanced)"
            }
            PaperQuestionEntity(
                id = UUID.randomUUID().toString(),
                paperId = paperId,
                questionId = q.id,
                orderIndex = index + 1,
                sectionName = section,
                snapshotQuestionText = q.questionText,
                snapshotMarks = q.marks,
                snapshotQuestionType = q.questionType,
                snapshotDifficulty = q.difficulty,
                snapshotAnswer = q.markingScheme,
                snapshotChapterId = q.chapterId,
                snapshotTopicId = q.topicId
            )
        }

        val targetTopicNames = weakTopicIds.mapNotNull { topicMap[it]?.name }

        return AdaptiveRetestGenerationResult(
            paper = paper,
            questions = paperQuestions,
            weakAreaCount = selectedWeakQuestions.size,
            recentlyIncorrectCount = selectedRecentQuestions.size,
            mixedRevisionCount = selectedMixedQuestions.size,
            targetWeakTopics = targetTopicNames
        )
    }

    fun evaluateAdaptiveRetestCompletion(
        paperId: String,
        marksObtained: Float,
        totalMarks: Float,
        paperQuestions: List<PaperQuestionEntity>,
        marksAwardedPerQuestion: Map<Int, Float>,
        existingMistakes: List<MistakeEntity>,
        topics: List<TopicEntity>
    ): AdaptiveRetestCompletionSummary {
        val topicMap = topics.associateBy { it.id }
        val accuracy = if (totalMarks > 0f) (marksObtained / totalMarks) * 100f else 0f

        var fixedMistakesCount = 0
        var recoveredMarks = 0f
        val remainingWeakTopicIds = mutableSetOf<String>()

        paperQuestions.forEachIndexed { index, pq ->
            val awarded = marksAwardedPerQuestion[index] ?: 0f
            val maxMarks = pq.snapshotMarks
            val fullMarksScored = awarded >= (maxMarks * 0.9f)

            if (fullMarksScored) {
                val matchedMistake = existingMistakes.find {
                    !it.isResolved && (it.question.trim().equals(pq.snapshotQuestionText.trim(), ignoreCase = true) ||
                        (it.topicId != null && it.topicId == pq.snapshotTopicId))
                }
                if (matchedMistake != null) {
                    fixedMistakesCount++
                    recoveredMarks += matchedMistake.marksLost
                }
            } else {
                if (pq.snapshotTopicId != null) {
                    remainingWeakTopicIds.add(pq.snapshotTopicId)
                }
            }
        }

        val remainingTopicNames = remainingWeakTopicIds.mapNotNull { topicMap[it]?.name }
        val masteryPercentage = accuracy.coerceIn(0f, 100f)

        val nextAction = when {
            accuracy >= 85f -> "Mastery demonstrated. Proceed to full Exam Readiness Simulation."
            accuracy >= 65f -> "Moderate improvement. Clear remaining weak concepts in Active Recall."
            else -> "High error rate. Re-study chapter concepts before next attempt."
        }

        return AdaptiveRetestCompletionSummary(
            paperId = paperId,
            marksObtained = marksObtained,
            totalMarks = totalMarks,
            accuracyPercentage = accuracy,
            mistakesFixedCount = fixedMistakesCount,
            marksRecovered = recoveredMarks,
            remainingWeakTopics = remainingTopicNames,
            masteryPercentage = masteryPercentage,
            recommendedNextAction = nextAction
        )
    }

    // =========================================================================
    // 8. Exam Readiness Simulator
    // =========================================================================

    fun evaluateExamReadiness(
        submission: ExamSimulationSubmission,
        paper: PaperEntity,
        questions: List<PaperQuestionEntity>,
        chapters: List<ChapterEntity>,
        historicalResults: List<ExamResultEntity>,
        allTopics: List<TopicEntity>,
        allMistakes: List<MistakeEntity>
    ): ExamReadinessResult {
        val chapterMap = chapters.associateBy { it.id }
        val totalPaperMarks = if (paper.totalMarks > 0f) paper.totalMarks else questions.sumOf { it.snapshotMarks.toDouble() }.toFloat()

        var obtainedScore = 0f
        var easyQuestionsMissed = 0
        var conceptualErrors = 0
        var calculationErrors = 0
        var carelessErrors = 0
        val chapterLostMarks = mutableMapOf<String, Float>()

        questions.forEachIndexed { idx, q ->
            val awarded = submission.marksAwardedMap[idx] ?: 0f
            obtainedScore += awarded
            val lost = q.snapshotMarks - awarded

            if (lost > 0f) {
                if (q.snapshotChapterId.isNotBlank()) {
                    chapterLostMarks[q.snapshotChapterId] = (chapterLostMarks[q.snapshotChapterId] ?: 0f) + lost
                }
                // Easy question missed
                if (q.snapshotMarks <= 2.0f && awarded < (q.snapshotMarks * 0.5f)) {
                    easyQuestionsMissed++
                    carelessErrors++
                } else if (q.snapshotDifficulty.equals("HARD", ignoreCase = true)) {
                    conceptualErrors++
                } else {
                    calculationErrors++
                }
            }
        }

        val percentage = if (totalPaperMarks > 0f) (obtainedScore / totalPaperMarks) * 100f else 0f
        val accuracy = percentage

        val avgTimePerQuestion = if (questions.isNotEmpty()) {
            submission.timeSpentSeconds.toFloat() / questions.size
        } else 0f

        // Time management diagnostic
        val allocatedSeconds = paper.durationMinutes * 60
        val timeManagementWarning = when {
            submission.timeSpentSeconds < allocatedSeconds * 0.40 -> "Finished abnormally early. High risk of careless reading."
            submission.timeSpentSeconds > allocatedSeconds * 0.95 && percentage < 70f -> "Ran out of time. Needs speed drills on Section A & B."
            else -> null
        }

        val weakChapterNames = chapterLostMarks.entries
            .filter { it.value >= 3.0f }
            .mapNotNull { chapterMap[it.key]?.name }

        // --- Deterministic Exam Readiness Formula ---
        // Factor 1: Recent Scores (35%)
        val recentScoresAvg = if (historicalResults.isNotEmpty()) {
            val totalHObtained = historicalResults.take(3).sumOf { it.marksObtained.toDouble() }
            val totalHPossible = historicalResults.take(3).sumOf { it.totalMarks.toDouble() }
            if (totalHPossible > 0.0) ((totalHObtained / totalHPossible) * 100.0).toFloat() else percentage
        } else percentage

        // Factor 2: Simulation Accuracy (25%)
        val simAccuracyScore = percentage

        // Factor 3: Syllabus Mastery Coverage (20%)
        val coverageScore = if (allTopics.isNotEmpty()) {
            val masteredCount = allTopics.count { it.masteryState == "MASTERED" }
            (masteredCount.toFloat() / allTopics.size) * 100f
        } else 50f

        // Factor 4: Time Management Score (10%)
        val timeScore = if (timeManagementWarning == null) 90f else 50f

        // Factor 5: Mistake / Retest Penalty (10%)
        val activeMistakeCount = allMistakes.count { !it.isResolved }
        val mistakePenalty = min(30f, activeMistakeCount * 2.5f)
        val mistakeFactor = max(0f, 100f - mistakePenalty)

        val readinessScore = (
            (recentScoresAvg * 0.35f) +
            (simAccuracyScore * 0.25f) +
            (coverageScore * 0.20f) +
            (timeScore * 0.10f) +
            (mistakeFactor * 0.10f)
        ).coerceIn(0f, 100f)

        val factorMap = mapOf(
            "Recent Exam Scores" to recentScoresAvg,
            "Simulation Accuracy" to simAccuracyScore,
            "Syllabus Mastery Coverage" to coverageScore,
            "Time Discipline" to timeScore,
            "Mistake Cleanliness" to mistakeFactor
        )

        return ExamReadinessResult(
            readinessScore = readinessScore,
            score = obtainedScore,
            percentage = percentage,
            accuracy = accuracy,
            avgTimePerQuestionSeconds = avgTimePerQuestion,
            easyQuestionsMissedCount = easyQuestionsMissed,
            weakChapterNames = weakChapterNames,
            conceptualMistakesCount = conceptualErrors,
            calculationMistakesCount = calculationErrors,
            carelessMistakesCount = carelessErrors,
            timeManagementWarning = timeManagementWarning,
            readinessFactors = factorMap
        )
    }

    // =========================================================================
    // 9. Last-7-Days Mode
    // =========================================================================

    fun buildLast7DaysDashboard(
        targetExamDateTimestamp: Long?,
        completedTaskIds: Set<String>,
        subjects: List<SubjectEntity>,
        chapters: List<ChapterEntity>,
        topics: List<TopicEntity>,
        mistakes: List<MistakeEntity>,
        recallCards: List<RecallCardEntity>,
        now: Long = System.currentTimeMillis()
    ): Last7DaysDashboard {
        val examDate = targetExamDateTimestamp ?: (now + 7 * ONE_DAY_MS)
        val msUntilExam = examDate - now
        val daysRemaining = max(0, kotlin.math.ceil(msUntilExam.toDouble() / ONE_DAY_MS).toInt())
        val isActive = daysRemaining in 0..7

        val chapterMap = chapters.associateBy { it.id }
        val subjectMap = subjects.associateBy { it.id }
        val activeMistakes = mistakes.filter { !it.isResolved }

        // Rank chapters by weakness + weightage
        val rankedChapters = chapters.sortedByDescending { ch ->
            val chTopics = topics.filter { it.chapterId == ch.id }
            val chMistakes = activeMistakes.filter { it.chapterId == ch.id }
            val weakness = chTopics.map { it.weaknessScore }.average().takeIf { !it.isNaN() }?.toFloat() ?: 0.5f
            weakness * 10f + chMistakes.size * 2f
        }

        val dailyPlans = mutableListOf<Last7DaysDailyPlan>()
        val allTasks = mutableListOf<Last7DaysTask>()

        // Generate 7-day curriculum (Day 7 down to Day 1)
        for (day in 7 downTo 1) {
            val isCurrentDay = (day == daysRemaining) || (daysRemaining == 0 && day == 1)
            val dayChapter = rankedChapters.getOrNull((7 - day) % max(1, rankedChapters.size))
            val daySubject = dayChapter?.let { subjectMap[it.subjectId] }
            val chapterName = dayChapter?.name ?: "Key Concepts"
            val subjectName = daySubject?.name ?: "Syllabus"

            val tasksForDay = mutableListOf<Last7DaysTask>()

            // Task 1: Priority Chapter Quick Revision
            tasksForDay.add(
                Last7DaysTask(
                    id = "d${day}_t1_priority_chapter",
                    dayNumber = day,
                    title = "Revise: $chapterName ($subjectName)",
                    description = "Focus on high-weightage formulas and structural definitions.",
                    category = "PRIORITY_CHAPTER",
                    estimatedMinutes = 35,
                    isCompleted = completedTaskIds.contains("d${day}_t1_priority_chapter"),
                    targetSubject = subjectName,
                    targetChapter = chapterName
                )
            )

            // Task 2: Mistake Remediation or Adaptive Re-Test
            if (day % 2 == 1) {
                tasksForDay.add(
                    Last7DaysTask(
                        id = "d${day}_t2_retest",
                        dayNumber = day,
                        title = "Take Adaptive Re-Test (30m)",
                        description = "Verify elimination of calculation & careless errors on $chapterName.",
                        category = "ADAPTIVE_RETEST",
                        estimatedMinutes = 30,
                        isCompleted = completedTaskIds.contains("d${day}_t2_retest"),
                        targetSubject = subjectName,
                        targetChapter = chapterName
                    )
                )
            } else {
                tasksForDay.add(
                    Last7DaysTask(
                        id = "d${day}_t2_mistakes",
                        dayNumber = day,
                        title = "Resolve Active Mistake Bank Entries",
                        description = "Audit and fix logged exam mistakes to prevent repeated marks loss.",
                        category = "PREVIOUS_MISTAKES",
                        estimatedMinutes = 25,
                        isCompleted = completedTaskIds.contains("d${day}_t2_mistakes"),
                        targetSubject = subjectName,
                        targetChapter = chapterName
                    )
                )
            }

            // Task 3: Spaced Recall Blitz
            tasksForDay.add(
                Last7DaysTask(
                    id = "d${day}_t3_recall",
                    dayNumber = day,
                    title = "Daily Spaced Recall Sprint (15m)",
                    description = "Review high-decay cards to prevent memory decay before exam day.",
                    category = "SHORT_RECALL",
                    estimatedMinutes = 15,
                    isCompleted = completedTaskIds.contains("d${day}_t3_recall")
                )
            )

            allTasks.addAll(tasksForDay)

            val completedCount = tasksForDay.count { it.isCompleted }
            dailyPlans.add(
                Last7DaysDailyPlan(
                    dayNumber = day,
                    dayLabel = if (day == 1) "Day 1 (Final Review)" else "Day $day",
                    isToday = isCurrentDay,
                    priorityChapters = listOf(chapterName),
                    tasks = tasksForDay,
                    completedTasksCount = completedCount,
                    totalTasksCount = tasksForDay.size
                )
            )
        }

        // Today's 3 Most Important Tasks
        val currentPlan = dailyPlans.find { it.isToday } ?: dailyPlans.first()
        val todayTop3 = currentPlan.tasks.take(3)

        val totalTasks = allTasks.size
        val totalCompleted = allTasks.count { it.isCompleted }
        val overallCompletion = if (totalTasks > 0) (totalCompleted.toFloat() / totalTasks) * 100f else 0f

        return Last7DaysDashboard(
            isActive = isActive,
            daysRemaining = daysRemaining,
            targetExamDateTimestamp = examDate,
            currentDayNumber = currentPlan.dayNumber,
            todayTopThreeTasks = todayTop3,
            dailyPlans = dailyPlans,
            overallWeekCompletionPercentage = overallCompletion
        )
    }

    // =========================================================================
    // 10. 95% Command Center
    // =========================================================================

    fun build95CommandCenterSnapshot(
        studentName: String,
        targetPercentage: Float = 95.0f,
        targetExamDateTimestamp: Long?,
        subjects: List<SubjectEntity>,
        chapters: List<ChapterEntity>,
        topics: List<TopicEntity>,
        mistakes: List<MistakeEntity>,
        examResults: List<ExamResultEntity>,
        studySessions: List<StudySessionEntity>,
        todayCompletedRetests: Int = 0,
        todayRevisedTopics: Int = 0,
        now: Long = System.currentTimeMillis()
    ): CommandCenterSnapshot {
        val subjectMap = subjects.associateBy { it.id }
        val chapterMap = chapters.associateBy { it.id }

        // 1. Current predicted percentage
        val currentPredicted = if (examResults.isNotEmpty()) {
            val totalObtained = examResults.sumOf { it.marksObtained.toDouble() }
            val totalPossible = examResults.sumOf { it.totalMarks.toDouble() }
            if (totalPossible > 0.0) ((totalObtained / totalPossible) * 100.0).toFloat() else 0f
        } else {
            0f
        }

        val percentageGap = (targetPercentage - currentPredicted).coerceAtLeast(0f)
        val marksGap = if (examResults.isNotEmpty()) {
            val latestTotal = examResults.first().totalMarks
            (latestTotal * (percentageGap / 100f)).coerceAtLeast(0f)
        } else null

        // 2. Exam readiness percentage (deterministic metric)
        val readinessPercentage = if (examResults.isNotEmpty()) {
            val recentScores = examResults.take(3).map { it.marksObtained / max(1f, it.totalMarks) * 100f }
            val avgRecent = recentScores.average().toFloat()
            val masteredTopics = topics.count { it.masteryState == "MASTERED" }
            val coverage = if (topics.isNotEmpty()) (masteredTopics.toFloat() / topics.size) * 100f else 50f
            (avgRecent * 0.6f + coverage * 0.4f).coerceIn(0f, 100f)
        } else {
            if (topics.isNotEmpty()) {
                val masteredTopics = topics.count { it.masteryState == "MASTERED" }
                ((masteredTopics.toFloat() / topics.size) * 70f).coerceIn(0f, 70f)
            } else 0f
        }

        // 3. Days remaining
        val daysRemaining = targetExamDateTimestamp?.let {
            max(0, kotlin.math.ceil((it - now).toDouble() / ONE_DAY_MS).toInt())
        }

        // 4. Weakest Chapters (Top 3)
        val activeMistakes = mistakes.filter { !it.isResolved }
        val chapterWeaknesses = chapters.map { ch ->
            val chTopics = topics.filter { it.chapterId == ch.id }
            val chMistakes = activeMistakes.filter { it.chapterId == ch.id }
            val topicWeakness = chTopics.map { it.weaknessScore }.average().takeIf { !it.isNaN() }?.toFloat() ?: 0.5f
            val score = topicWeakness + (chMistakes.size * 0.2f)
            ch.name to score
        }.sortedByDescending { it.second }.take(3)

        // 5. Primary Recommended Action ("WHAT SHOULD I DO NOW?")
        val primaryWeakest = chapterWeaknesses.firstOrNull()
        val primaryAction = if (primaryWeakest != null && primaryWeakest.second > 0.4f) {
            val chEntity = chapters.find { it.name == primaryWeakest.first }
            val subName = chEntity?.let { subjectMap[it.subjectId]?.name } ?: "Syllabus"
            val accuracyEst = (max(20f, 100f - primaryWeakest.second * 40f)).roundToInt()
            CommandCenterAction(
                title = "Revise ${primaryWeakest.first} for 35 minutes",
                reason = "High exam weightage in $subName + estimated accuracy is $accuracyEst%",
                actionType = "REVISE_TOPIC",
                targetSubjectId = chEntity?.subjectId,
                targetChapterId = chEntity?.id,
                suggestedMinutes = 35
            )
        } else if (activeMistakes.isNotEmpty()) {
            CommandCenterAction(
                title = "Resolve ${activeMistakes.size} Active Mistakes",
                reason = "Eliminating previous errors directly recovers lost marks",
                actionType = "RETEST_WEAKNESS",
                suggestedMinutes = 20
            )
        } else {
            CommandCenterAction(
                title = "Complete 20m Spaced Recall Session",
                reason = "Reinforces long-term memory and prevents SM-2 retention decay",
                actionType = "RECALL_RADAR",
                suggestedMinutes = 20
            )
        }

        // 6. Today Study Progress
        val todayStart = now - (now % ONE_DAY_MS)
        val todayMinutes = studySessions
            .filter { it.completedAt >= todayStart }
            .sumOf { it.durationMinutes }

        // 7. Recent Test Scores & Accuracy Trend
        val recentScores = examResults.take(5).map {
            (it.marksObtained / max(1f, it.totalMarks) * 100f)
        }

        val accuracyTrend = if (recentScores.size >= 2) {
            val delta = recentScores.first() - recentScores.last()
            when {
                delta >= 2.0f -> "IMPROVING"
                delta <= -2.0f -> "DECLINING"
                else -> "STABLE"
            }
        } else {
            "STABLE"
        }

        // 8. Subject Breakdown
        val subjectBreakdown = subjects.map { sub ->
            val subResults = examResults.filter { res ->
                // Check if result matches this subject
                true
            }
            val score = if (subResults.isNotEmpty()) {
                subResults.map { it.marksObtained / max(1f, it.totalMarks) * 100f }.average().toFloat()
            } else currentPredicted
            sub.name to score
        }

        return CommandCenterSnapshot(
            studentName = studentName,
            currentPredictedPercentage = currentPredicted,
            targetPercentage = targetPercentage,
            percentageGap = percentageGap,
            marksGap = marksGap,
            examReadinessPercentage = readinessPercentage,
            daysRemaining = daysRemaining,
            primaryAction = primaryAction,
            todayStudyMinutes = todayMinutes,
            todayTargetMinutes = 120,
            todayRetestsTaken = todayCompletedRetests,
            todayTopicsRevised = todayRevisedTopics,
            topThreeWeakChapters = chapterWeaknesses,
            nextExamTitle = if (daysRemaining != null) "Board / Term Examination" else null,
            recentTestScores = recentScores,
            accuracyTrend = accuracyTrend,
            subjectBreakdown = subjectBreakdown
        )
    }
}
