package com.os95.app.domain.engine

import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.ExamResultEntity
import com.os95.app.core.database.entity.LostMarksEntity
import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.PaperQuestionEntity
import com.os95.app.core.database.entity.RecallCardEntity
import com.os95.app.core.database.entity.RecallReviewEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.domain.model.ChapterAnalysisItem
import com.os95.app.domain.model.ForgettingRadarSnapshot
import com.os95.app.domain.model.MarksGapPlan
import com.os95.app.domain.model.MarksRecoverySnapshot
import com.os95.app.domain.model.PaperAnalysis
import com.os95.app.domain.model.PaperScoreSummary
import com.os95.app.domain.model.PaperTrendDirection
import com.os95.app.domain.model.QuestionTypeAnalysisItem
import com.os95.app.domain.model.RecoveryOpportunity
import com.os95.app.domain.model.RecoveryPriorityLevel
import com.os95.app.domain.model.RecoveryScoreReport
import com.os95.app.domain.model.RepeatedWeaknessItem
import com.os95.app.domain.model.RescueActionType
import com.os95.app.domain.model.RescueBlock
import com.os95.app.domain.model.RescuePlan
import com.os95.app.domain.model.RetentionRiskLevel
import com.os95.app.domain.model.SubjectAnalysisItem
import com.os95.app.domain.model.TopicRecoveryItem
import com.os95.app.domain.model.TopicRetentionRisk
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class MarksRecoveryEngine {

    companion object {
        const val ONE_DAY_MS = 86_400_000L
    }

    // --- 1. Marks Gap Planner ---

    fun calculateMarksGap(
        targetPercentage: Float = 95.0f,
        examResults: List<ExamResultEntity>,
        mistakes: List<MistakeEntity>,
        topics: List<TopicEntity>,
        chapters: List<ChapterEntity>,
        subjects: List<SubjectEntity>,
        totalExamMarks: Float? = null
    ): MarksGapPlan {
        val subjectMap = subjects.associateBy { it.id }
        val chapterMap = chapters.associateBy { it.id }
        val topicMap = topics.associateBy { it.id }

        // Current Average Percentage
        val currentPercentage = if (examResults.isNotEmpty()) {
            val totalObtained = examResults.sumOf { it.marksObtained.toDouble() }
            val totalPossible = examResults.sumOf { it.totalMarks.toDouble() }
            if (totalPossible > 0.0) ((totalObtained / totalPossible) * 100.0).toFloat() else 0f
        } else {
            0f
        }

        val percentageGap = (targetPercentage - currentPercentage).coerceAtLeast(0f)

        val marksNeeded = if (totalExamMarks != null && totalExamMarks > 0f) {
            val requiredMarks = (totalExamMarks * (targetPercentage / 100f))
            val currentProjectedMarks = (totalExamMarks * (currentPercentage / 100f))
            (requiredMarks - currentProjectedMarks).coerceAtLeast(0f)
        } else {
            null
        }
        val projectedMarks = if (totalExamMarks != null && totalExamMarks > 0f) {
            (totalExamMarks * (currentPercentage / 100f))
        } else {
            null
        }

        // Unresolved Lost Marks by Category
        val activeMistakes = mistakes.filter { !it.isResolved }
        val lostByCategory = mutableMapOf<String, Float>()
        for (m in activeMistakes) {
            val cat = normalizeCategory(m.lossCategory)
            lostByCategory[cat] = (lostByCategory[cat] ?: 0f) + m.marksLost
        }

        val potentialRecoverableMarks = activeMistakes.sumOf { it.marksLost.toDouble() }.toFloat()

        // Prioritization Algorithm:
        // Priority Score = Lost Marks * Recent Frequency * Weakness * Recoverability * Exam Relevance
        val mistakesByTopic = activeMistakes.filter { it.topicId != null }.groupBy { it.topicId!! }
        val opportunities = mutableListOf<RecoveryOpportunity>()

        for ((topId, topMistakes) in mistakesByTopic) {
            val topic = topicMap[topId] ?: continue
            val chapter = chapterMap[topic.chapterId]
            val subject = chapter?.let { subjectMap[it.subjectId] } ?: continue

            val lostMarks = topMistakes.sumOf { it.marksLost.toDouble() }.toFloat()
            if (lostMarks <= 0f) continue

            val recentFreqFactor = 1.0f + 0.2f * min(topMistakes.size, 5)
            val weaknessFactor = max(0.2f, topic.weaknessScore)

            val dominantCategory = topMistakes.groupBy { normalizeCategory(it.lossCategory) }
                .maxByOrNull { it.value.sumOf { m -> m.marksLost.toDouble() } }?.key ?: "Other"

            val recoverabilityFactor = when (dominantCategory) {
                "Calculation Error", "Careless" -> 1.0f
                "Forgotten", "Didn't Know" -> 0.9f
                "Time Management" -> 0.8f
                "Concept Error" -> 0.7f
                else -> 0.75f
            }

            val relevanceFactor = when (topic.examRelevance.uppercase()) {
                "HIGH" -> 1.5f
                "MEDIUM" -> 1.0f
                "LOW" -> 0.7f
                else -> 1.0f
            }

            val score = lostMarks * recentFreqFactor * weaknessFactor * recoverabilityFactor * relevanceFactor
            val priorityLevel = when {
                score >= 10.0f -> RecoveryPriorityLevel.VERY_HIGH
                score >= 5.0f -> RecoveryPriorityLevel.HIGH
                score >= 2.0f -> RecoveryPriorityLevel.MEDIUM
                else -> RecoveryPriorityLevel.LOW
            }

            val actionDesc = when (dominantCategory) {
                "Calculation Error" -> "Eliminate arithmetic steps and verify calculation"
                "Forgotten" -> "Review formulas and trigger Active Recall review"
                "Concept Error" -> "Re-read chapter concept and rebuild mental model"
                "Careless" -> "Practice careful question stem re-reading"
                "Time Management" -> "Practice timed speed-accuracy drills"
                else -> "Review mistakes and re-test understanding"
            }

            opportunities.add(
                RecoveryOpportunity(
                    id = topId,
                    subjectId = subject.id,
                    subjectName = subject.name,
                    chapterId = chapter.id,
                    chapterName = chapter.name,
                    topicId = topic.id,
                    topicName = topic.name,
                    lostMarks = lostMarks,
                    priorityScore = score,
                    priorityLevel = priorityLevel,
                    dominantMistakeCategory = dominantCategory,
                    actionDescription = actionDesc
                )
            )
        }

        // If no topic-level mistakes, fallback to chapter/subject level opportunities if mistakes exist
        if (opportunities.isEmpty() && activeMistakes.isNotEmpty()) {
            val mistakesBySubject = activeMistakes.groupBy { it.subjectId }
            for ((subId, subMistakes) in mistakesBySubject) {
                val subject = subjectMap[subId] ?: continue
                val lost = subMistakes.sumOf { it.marksLost.toDouble() }.toFloat()
                val score = lost * 0.8f
                opportunities.add(
                    RecoveryOpportunity(
                        id = subId,
                        subjectId = subId,
                        subjectName = subject.name,
                        lostMarks = lost,
                        priorityScore = score,
                        priorityLevel = if (lost >= 10f) RecoveryPriorityLevel.HIGH else RecoveryPriorityLevel.MEDIUM,
                        dominantMistakeCategory = "General Revision",
                        actionDescription = "Review unresolved mistakes in ${subject.name}"
                    )
                )
            }
        }

        opportunities.sortByDescending { it.priorityScore }

        return MarksGapPlan(
            targetPercentage = targetPercentage,
            currentPercentage = currentPercentage,
            percentageGap = percentageGap,
            totalExamMarks = totalExamMarks,
            projectedMarks = projectedMarks,
            marksNeeded = marksNeeded,
            potentialRecoverableMarks = potentialRecoverableMarks,
            lostMarksByCategory = lostByCategory,
            topOpportunities = opportunities,
            hasSufficientData = examResults.isNotEmpty() || activeMistakes.isNotEmpty()
        )
    }

    // --- 2. Forgetting Radar ---

    fun calculateForgettingRadar(
        cards: List<RecallCardEntity>,
        reviews: List<RecallReviewEntity>,
        topics: List<TopicEntity>,
        chapters: List<ChapterEntity>,
        subjects: List<SubjectEntity>,
        nowMs: Long = System.currentTimeMillis()
    ): ForgettingRadarSnapshot {
        val subjectMap = subjects.associateBy { it.id }
        val chapterMap = chapters.associateBy { it.id }
        val cardsByTopic = cards.filter { it.topicId != null }.groupBy { it.topicId!! }
        val reviewsByCard = reviews.groupBy { it.cardId }

        val topicRisks = mutableListOf<TopicRetentionRisk>()

        for (topic in topics) {
            val chapter = chapterMap[topic.chapterId] ?: continue
            val subject = subjectMap[chapter.subjectId] ?: continue
            val topicCards = cardsByTopic[topic.id] ?: emptyList()

            if (topicCards.isNotEmpty()) {
                var criticalCards = 0
                var atRiskCards = 0
                var watchCards = 0
                var dueCards = 0
                var totalRecallRatings = 0
                var totalReviewCount = 0
                var latestReviewTime = 0L

                for (c in topicCards) {
                    if (c.dueDate <= nowMs) dueCards++
                    val deltaDays = (nowMs - c.dueDate).toFloat() / ONE_DAY_MS

                    val cardReviews = reviewsByCard[c.id] ?: emptyList()
                    val lastRating = cardReviews.maxByOrNull { it.reviewTimestamp }
                    if (lastRating != null && lastRating.reviewTimestamp > latestReviewTime) {
                        latestReviewTime = lastRating.reviewTimestamp
                    }
                    totalReviewCount += cardReviews.size
                    totalRecallRatings += cardReviews.sumOf { it.rating }

                    // Card Risk Categorization
                    when {
                        deltaDays > 3.0f || (c.repetitions == 0 && deltaDays > 0.0f) || c.easeFactor < 1.6f -> {
                            criticalCards++
                        }
                        deltaDays in 1.0f..3.0f || (deltaDays >= 0.0f && c.intervalDays <= 3) || (deltaDays >= -1.0f && topic.weaknessScore >= 0.7f) -> {
                            atRiskCards++
                        }
                        deltaDays in -3.0f..0.0f || c.easeFactor < 2.0f -> {
                            watchCards++
                        }
                    }
                }

                val avgScore = if (totalReviewCount > 0) {
                    ((totalRecallRatings.toFloat() / (totalReviewCount * 4f)) * 100.0f).coerceIn(0f, 100f)
                } else {
                    75.0f
                }

                val daysSinceReview = if (latestReviewTime > 0L) {
                    ((nowMs - latestReviewTime) / ONE_DAY_MS).toInt().coerceAtLeast(0)
                } else {
                    ((nowMs - topicCards.minOf { it.createdAt }) / ONE_DAY_MS).toInt().coerceAtLeast(0)
                }

                val topicRiskLevel = when {
                    criticalCards > 0 -> RetentionRiskLevel.CRITICAL
                    atRiskCards > 0 || topic.weaknessScore >= 0.7f -> RetentionRiskLevel.AT_RISK
                    watchCards > 0 || topic.masteryState in listOf("NOT_STARTED", "LEARNING") -> RetentionRiskLevel.WATCH
                    else -> RetentionRiskLevel.STABLE
                }

                topicRisks.add(
                    TopicRetentionRisk(
                        topicId = topic.id,
                        topicName = topic.name,
                        chapterId = chapter.id,
                        chapterName = chapter.name,
                        subjectId = subject.id,
                        subjectName = subject.name,
                        riskLevel = topicRiskLevel,
                        cardsDueCount = dueCards,
                        totalCards = topicCards.size,
                        averageRecallScore = avgScore,
                        daysSinceLastReview = daysSinceReview,
                        recommendedCardCount = if (dueCards > 0) dueCards else min(topicCards.size, 5)
                    )
                )
            } else {
                // Topic without cards: infer from academic weakness
                val risk = when {
                    topic.weaknessScore >= 0.75f -> RetentionRiskLevel.AT_RISK
                    topic.masteryState in listOf("NOT_STARTED", "LEARNING") -> RetentionRiskLevel.WATCH
                    else -> RetentionRiskLevel.STABLE
                }
                topicRisks.add(
                    TopicRetentionRisk(
                        topicId = topic.id,
                        topicName = topic.name,
                        chapterId = chapter.id,
                        chapterName = chapter.name,
                        subjectId = subject.id,
                        subjectName = subject.name,
                        riskLevel = risk,
                        cardsDueCount = 0,
                        totalCards = 0,
                        averageRecallScore = 0f,
                        daysSinceLastReview = 0,
                        recommendedCardCount = 0,
                        primaryAction = "Create Cards"
                    )
                )
            }
        }

        val criticalCount = topicRisks.count { it.riskLevel == RetentionRiskLevel.CRITICAL }
        val atRiskCount = topicRisks.count { it.riskLevel == RetentionRiskLevel.AT_RISK }
        val watchCount = topicRisks.count { it.riskLevel == RetentionRiskLevel.WATCH }
        val stableCount = topicRisks.count { it.riskLevel == RetentionRiskLevel.STABLE }

        // Sort: Critical first, then At Risk, then Watch, then Stable
        val sortedTopics = topicRisks.sortedWith(
            compareBy<TopicRetentionRisk> {
                when (it.riskLevel) {
                    RetentionRiskLevel.CRITICAL -> 0
                    RetentionRiskLevel.AT_RISK -> 1
                    RetentionRiskLevel.WATCH -> 2
                    RetentionRiskLevel.STABLE -> 3
                }
            }.thenByDescending { it.cardsDueCount }
        )

        return ForgettingRadarSnapshot(
            criticalCount = criticalCount,
            atRiskCount = atRiskCount,
            watchCount = watchCount,
            stableCount = stableCount,
            topics = sortedTopics,
            totalCardsTracked = cards.size
        )
    }

    // --- 3. Previous Paper Analyzer ---

    fun analyzePreviousPapers(
        papers: List<PaperEntity>,
        results: List<ExamResultEntity>,
        paperQuestions: List<PaperQuestionEntity>,
        lostMarks: List<LostMarksEntity>,
        mistakes: List<MistakeEntity>,
        topics: List<TopicEntity>,
        chapters: List<ChapterEntity>,
        subjects: List<SubjectEntity>
    ): PaperAnalysis {
        val paperMap = papers.associateBy { it.id }
        val subjectMap = subjects.associateBy { it.id }
        val chapterMap = chapters.associateBy { it.id }
        val topicMap = topics.associateBy { it.id }

        if (results.isEmpty()) {
            return PaperAnalysis(
                totalPapers = 0,
                hasEnoughDataForTrend = false,
                averagePercentage = 0f,
                bestPercentage = 0f,
                worstPercentage = 0f,
                recentAveragePercentage = 0f,
                improvementPercentage = 0f,
                trendDirection = PaperTrendDirection.INSUFFICIENT_DATA
            )
        }

        val sortedResults = results.sortedBy { it.completedAt }
        val paperSummaries = sortedResults.map { r ->
            val paper = paperMap[r.paperId]
            val subName = paper?.let { subjectMap[it.subjectId]?.name } ?: "General"
            val pct = if (r.totalMarks > 0f) (r.marksObtained / r.totalMarks) * 100f else 0f
            PaperScoreSummary(
                paperId = r.paperId,
                paperTitle = paper?.title ?: "Practice Paper",
                subjectName = subName,
                marksObtained = r.marksObtained,
                totalMarks = r.totalMarks,
                percentage = pct,
                dateMillis = r.completedAt
            )
        }

        val percentages = paperSummaries.map { it.percentage }
        val avgPct = percentages.average().toFloat()
        val bestPct = percentages.maxOrNull() ?: 0f
        val worstPct = percentages.minOrNull() ?: 0f

        val hasEnoughData = paperSummaries.size >= 2
        val recentPapers = paperSummaries.takeLast(3)
        val recentAvg = recentPapers.map { it.percentage }.average().toFloat()

        val improvement = if (hasEnoughData) {
            paperSummaries.last().percentage - paperSummaries.first().percentage
        } else {
            0f
        }

        val trendDirection = if (!hasEnoughData) {
            PaperTrendDirection.INSUFFICIENT_DATA
        } else {
            val delta = if (paperSummaries.size <= 3) {
                improvement
            } else {
                recentAvg - avgPct
            }
            when {
                delta >= 2.0f -> PaperTrendDirection.IMPROVING
                delta <= -2.0f -> PaperTrendDirection.DECLINING
                else -> PaperTrendDirection.STABLE
            }
        }

        // Subject Performance
        val subjectPerformance = paperSummaries.groupBy { it.subjectName }.map { (subName, pList) ->
            val subObj = subjects.firstOrNull { it.name == subName }
            SubjectAnalysisItem(
                subjectId = subObj?.id ?: subName,
                subjectName = subName,
                paperCount = pList.size,
                averagePercentage = pList.map { it.percentage }.average().toFloat(),
                bestPercentage = pList.maxOf { it.percentage }
            )
        }.sortedByDescending { it.paperCount }

        // Chapter & Question Type Performance from Snapshots
        val pQuestionsByPaper = paperQuestions.groupBy { it.paperId }
        val chapterLost = mutableMapOf<String, Float>()
        val chapterTotal = mutableMapOf<String, Float>()
        val qTypeTested = mutableMapOf<String, Int>()
        val qTypeFailed = mutableMapOf<String, Int>()

        for (r in sortedResults) {
            val qList = pQuestionsByPaper[r.paperId] ?: emptyList()
            for (q in qList) {
                val chpId = q.snapshotChapterId ?: "unknown"
                chapterTotal[chpId] = (chapterTotal[chpId] ?: 0f) + q.snapshotMarks

                val type = q.snapshotQuestionType.ifBlank { "SHORT_ANSWER" }
                qTypeTested[type] = (qTypeTested[type] ?: 0) + 1
            }
        }

        for (lm in lostMarks) {
            val top = lm.topicId?.let { topicMap[it] }
            val chpId = top?.chapterId ?: "unknown"
            chapterLost[chpId] = (chapterLost[chpId] ?: 0f) + lm.marksLost
        }

        val chapterPerformance = chapterTotal.mapNotNull { (chpId, totMarks) ->
            val chp = chapterMap[chpId] ?: return@mapNotNull null
            val subName = subjectMap[chp.subjectId]?.name ?: ""
            val lost = chapterLost[chpId] ?: 0f
            val acc = if (totMarks > 0f) (((totMarks - lost).coerceAtLeast(0f) / totMarks) * 100f) else 100f
            ChapterAnalysisItem(
                chapterId = chp.id,
                chapterName = chp.name,
                subjectName = subName,
                accuracyPercentage = acc,
                marksLostTotal = lost
            )
        }.sortedBy { it.accuracyPercentage }

        val questionTypePerformance = qTypeTested.map { (type, count) ->
            val failedCount = mistakes.count { m ->
                m.question.contains(type, ignoreCase = true) || m.lossCategory.contains(type, ignoreCase = true)
            }
            val acc = if (count > 0) (((count - min(failedCount, count)).toFloat() / count) * 100f) else 85f
            QuestionTypeAnalysisItem(
                questionType = formatQuestionTypeLabel(type),
                timesTested = count,
                accuracyPercentage = acc
            )
        }

        // Mistake Category Breakdown
        val mistakePatterns = mutableMapOf<String, Int>()
        for (m in mistakes) {
            val cat = normalizeCategory(m.lossCategory)
            mistakePatterns[cat] = (mistakePatterns[cat] ?: 0) + 1
        }

        // Repeated Weakness Detection across papers
        val lostByTopicAndPaper = mutableMapOf<String, MutableMap<String, Float>>() // topicId -> (paperId -> lost)
        for (lm in lostMarks) {
            val topId = lm.topicId ?: continue
            val paperId = lm.examResultId // result has 1:1 or map to paper
            val result = sortedResults.firstOrNull { it.id == paperId || it.paperId == paperId }
            val pId = result?.paperId ?: paperId

            val map = lostByTopicAndPaper.getOrPut(topId) { mutableMapOf() }
            map[pId] = (map[pId] ?: 0f) + lm.marksLost
        }

        val repeatedWeaknesses = mutableListOf<RepeatedWeaknessItem>()
        for ((topId, paperLosses) in lostByTopicAndPaper) {
            if (paperLosses.size >= 2) { // Appeared in 2 or more distinct papers!
                val top = topicMap[topId] ?: continue
                val chp = chapterMap[top.chapterId]
                val sub = chp?.let { subjectMap[it.subjectId] }

                val breakdown = paperLosses.map { (pId, lost) ->
                    val pTitle = paperMap[pId]?.title ?: "Paper"
                    Pair(pTitle, lost)
                }

                repeatedWeaknesses.add(
                    RepeatedWeaknessItem(
                        topicId = top.id,
                        topicName = top.name,
                        chapterName = chp?.name ?: "",
                        subjectName = sub?.name ?: "",
                        totalLostMarks = paperLosses.values.sum(),
                        papersAffectedCount = paperLosses.size,
                        paperLostBreakdown = breakdown
                    )
                )
            }
        }
        repeatedWeaknesses.sortByDescending { it.totalLostMarks }

        return PaperAnalysis(
            totalPapers = paperSummaries.size,
            hasEnoughDataForTrend = hasEnoughData,
            averagePercentage = avgPct,
            bestPercentage = bestPct,
            worstPercentage = worstPct,
            recentAveragePercentage = recentAvg,
            improvementPercentage = improvement,
            trendDirection = trendDirection,
            paperHistory = paperSummaries.reversed(),
            subjectPerformance = subjectPerformance,
            chapterPerformance = chapterPerformance,
            questionTypePerformance = questionTypePerformance,
            mistakePatternBreakdown = mistakePatterns,
            repeatedWeaknesses = repeatedWeaknesses
        )
    }

    // --- 4. 15-Minute Rescue Mode ---

    fun generateRescuePlan(
        durationMinutes: Int = 15,
        marksGap: MarksGapPlan,
        forgettingRadar: ForgettingRadarSnapshot,
        mistakes: List<MistakeEntity>,
        topics: List<TopicEntity>
    ): RescuePlan {
        val totalMins = when {
            durationMinutes <= 5 -> 5
            durationMinutes <= 10 -> 10
            durationMinutes <= 20 -> 15
            durationMinutes <= 25 -> 20
            else -> durationMinutes
        }

        // Time allocations:
        // Block 1 (Recall): 40%
        // Block 2 (Mistakes): 35%
        // Block 3 (High-Yield Topic): 25%
        val b1Mins = max(2, (totalMins * 0.40f).roundToInt())
        val b2Mins = max(2, (totalMins * 0.35f).roundToInt())
        val b3Mins = max(1, totalMins - b1Mins - b2Mins)

        val blocks = mutableListOf<RescueBlock>()
        var currentMin = 0

        // Block 1: Review High-Risk Cards from Forgetting Radar
        val criticalOrAtRiskTopic = forgettingRadar.topics.firstOrNull {
            it.riskLevel in listOf(RetentionRiskLevel.CRITICAL, RetentionRiskLevel.AT_RISK) && it.cardsDueCount > 0
        } ?: forgettingRadar.topics.firstOrNull { it.cardsDueCount > 0 }

        val b1Target = criticalOrAtRiskTopic?.topicName ?: "Active Due Cards"
        val b1CardsCount = min(criticalOrAtRiskTopic?.cardsDueCount ?: 5, max(4, b1Mins * 2))

        blocks.add(
            RescueBlock(
                startMinute = currentMin,
                endMinute = currentMin + b1Mins,
                title = "Review $b1CardsCount high-risk cards in $b1Target",
                actionType = RescueActionType.RECALL_REVIEW,
                targetEntityId = criticalOrAtRiskTopic?.topicId ?: "all_due",
                targetName = b1Target,
                itemCount = b1CardsCount,
                description = "Rapid active recall session focusing on memory decay prevention before exam exposure."
            )
        )
        currentMin += b1Mins

        // Block 2: Fix Repeated Mistakes
        val unresolvedMistakes = mistakes.filter { !it.isResolved }
        val calculationOrCareless = unresolvedMistakes.filter {
            it.lossCategory.contains("CALCULATION", ignoreCase = true) || it.lossCategory.contains("CARELESS", ignoreCase = true)
        }
        val targetMistakes = if (calculationOrCareless.isNotEmpty()) calculationOrCareless else unresolvedMistakes
        val b2Count = min(targetMistakes.size, max(2, b2Mins / 2))
        val b2Topic = targetMistakes.firstOrNull()?.question?.take(30) ?: "Calculation & Accuracy"

        blocks.add(
            RescueBlock(
                startMinute = currentMin,
                endMinute = currentMin + b2Mins,
                title = "Eliminate $b2Count critical lost-mark mistakes",
                actionType = RescueActionType.MISTAKE_FIX,
                targetEntityId = targetMistakes.firstOrNull()?.id ?: "mistakes",
                targetName = b2Topic,
                itemCount = b2Count,
                description = "Verify correct solution steps and eliminate careless execution errors."
            )
        )
        currentMin += b2Mins

        // Block 3: Rapid High-Yield Topic Review
        val topOpportunity = marksGap.topOpportunities.firstOrNull()
        val b3TopicName = topOpportunity?.topicName ?: (topics.firstOrNull { it.weaknessScore >= 0.5f }?.name ?: "Key Formulas")

        blocks.add(
            RescueBlock(
                startMinute = currentMin,
                endMinute = totalMins,
                title = "Rapid topic recall: $b3TopicName",
                actionType = RescueActionType.RAPID_TOPIC_READ,
                targetEntityId = topOpportunity?.topicId ?: "quick_read",
                targetName = b3TopicName,
                itemCount = 1,
                description = "High-leverage concept reinforcement in your highest priority recovery area."
            )
        )

        val estimatedRecovery = (topOpportunity?.lostMarks?.times(0.5f) ?: 3.0f).coerceAtLeast(1.0f)

        return RescuePlan(
            totalDurationMinutes = totalMins,
            blocks = blocks,
            estimatedRecoverableMarks = estimatedRecovery
        )
    }

    // --- 5. Recovery Score ---

    fun calculateRecoveryScore(
        papers: List<PaperEntity>,
        results: List<ExamResultEntity>,
        lostMarks: List<LostMarksEntity>,
        mistakes: List<MistakeEntity>,
        topics: List<TopicEntity>,
        chapters: List<ChapterEntity>,
        subjects: List<SubjectEntity>
    ): RecoveryScoreReport {
        if (results.size < 2) {
            val singleLost = results.firstOrNull()?.let { (it.totalMarks - it.marksObtained).coerceAtLeast(0f) } ?: 0f
            return RecoveryScoreReport(
                totalRecoveredMarks = 0f,
                hasComparablePapers = false,
                previousLostMarks = singleLost,
                currentLostMarks = singleLost,
                recoveryByCategory = emptyMap(),
                recoveryByTopic = emptyList(),
                lostMarksTrend = results.map { Pair("Paper #1", (it.totalMarks - it.marksObtained).coerceAtLeast(0f)) }
            )
        }

        val paperMap = papers.associateBy { it.id }
        val topicMap = topics.associateBy { it.id }
        val chapterMap = chapters.associateBy { it.id }
        val subjectMap = subjects.associateBy { it.id }

        val sortedResults = results.sortedBy { it.completedAt }

        // Find comparable pair (prefer same subject, or latest two consecutive papers)
        val latest = sortedResults.last()
        val latestPaper = paperMap[latest.paperId]
        val latestSubjectId = latestPaper?.subjectId

        val earlierComparable = sortedResults.dropLast(1).findLast { r ->
            val p = paperMap[r.paperId]
            p?.subjectId == latestSubjectId
        } ?: sortedResults[sortedResults.size - 2]

        val prevTotal = earlierComparable.totalMarks
        val prevObtained = earlierComparable.marksObtained
        val prevLost = (prevTotal - prevObtained).coerceAtLeast(0f)

        val currTotal = latest.totalMarks
        val currObtained = latest.marksObtained
        val currLost = (currTotal - currObtained).coerceAtLeast(0f)

        // Normalized Recovery Calculation:
        // Recovered = PrevLost - CurrLost (normalized to current paper scale)
        val recoveredMarks = if (prevTotal > 0f && currTotal > 0f) {
            val prevLostNorm = (prevLost / prevTotal) * currTotal
            (prevLostNorm - currLost)
        } else {
            prevLost - currLost
        }

        // Recovery by Category (from resolved mistakes or delta)
        val resolvedMistakes = mistakes.filter { it.isResolved }
        val recoveryByCategory = mutableMapOf<String, Float>()
        for (m in resolvedMistakes) {
            val cat = normalizeCategory(m.lossCategory)
            recoveryByCategory[cat] = (recoveryByCategory[cat] ?: 0f) + m.marksLost
        }
        if (recoveryByCategory.isEmpty() && recoveredMarks > 0f) {
            // Attribute recovered marks proportionally across common categories
            recoveryByCategory["Calculation mistakes"] = (recoveredMarks * 0.4f).roundToOneDecimal()
            recoveryByCategory["Forgotten concepts"] = (recoveredMarks * 0.3f).roundToOneDecimal()
            recoveryByCategory["Careless mistakes"] = (recoveredMarks * 0.2f).roundToOneDecimal()
            recoveryByCategory["Time management"] = (recoveredMarks * 0.1f).roundToOneDecimal()
        }

        // Recovery by Topic
        val prevResultLost = lostMarks.filter { it.examResultId == earlierComparable.id || it.examResultId == earlierComparable.paperId }
        val currResultLost = lostMarks.filter { it.examResultId == latest.id || it.examResultId == latest.paperId }

        val prevLostByTopic = prevResultLost.filter { it.topicId != null }.groupBy { it.topicId!! }
            .mapValues { it.value.sumOf { lm -> lm.marksLost.toDouble() }.toFloat() }
        val currLostByTopic = currResultLost.filter { it.topicId != null }.groupBy { it.topicId!! }
            .mapValues { it.value.sumOf { lm -> lm.marksLost.toDouble() }.toFloat() }

        val topicRecoveryItems = mutableListOf<TopicRecoveryItem>()
        val allTouchedTopics = (prevLostByTopic.keys + currLostByTopic.keys).toSet()

        for (topId in allTouchedTopics) {
            val top = topicMap[topId] ?: continue
            val chp = chapterMap[top.chapterId]
            val sub = chp?.let { subjectMap[it.subjectId] }
            val pLost = prevLostByTopic[topId] ?: 0f
            val cLost = currLostByTopic[topId] ?: 0f
            val rec = pLost - cLost

            topicRecoveryItems.add(
                TopicRecoveryItem(
                    topicId = top.id,
                    topicName = top.name,
                    chapterName = chp?.name ?: "",
                    subjectName = sub?.name ?: "",
                    previouslyLost = pLost,
                    currentlyLost = cLost,
                    recoveredMarks = rec
                )
            )
        }
        topicRecoveryItems.sortByDescending { it.recoveredMarks }

        // Lost Marks Trend over time
        val lostTrend = sortedResults.map { r ->
            val pTitle = paperMap[r.paperId]?.title ?: "Paper"
            val lost = (r.totalMarks - r.marksObtained).coerceAtLeast(0f)
            Pair(pTitle, lost)
        }

        return RecoveryScoreReport(
            totalRecoveredMarks = recoveredMarks.roundToOneDecimal(),
            hasComparablePapers = true,
            previousLostMarks = prevLost,
            currentLostMarks = currLost,
            recoveryByCategory = recoveryByCategory,
            recoveryByTopic = topicRecoveryItems,
            lostMarksTrend = lostTrend
        )
    }

    // --- Helpers ---

    private fun normalizeCategory(raw: String): String {
        val s = raw.uppercase()
        return when {
            s.contains("CALCULATION") -> "Calculation Error"
            s.contains("CARELESS") -> "Careless"
            s.contains("FORGOT") -> "Forgotten"
            s.contains("DIDNT_KNOW") -> "Didn't Know"
            s.contains("CONCEPT") -> "Concept Error"
            s.contains("MISREAD") -> "Misread Question"
            s.contains("TIME") -> "Time Management"
            else -> "Other"
        }
    }

    private fun formatQuestionTypeLabel(type: String): String {
        return when (type.uppercase()) {
            "MCQ" -> "Multiple Choice (MCQ)"
            "SHORT_ANSWER" -> "Short Answer"
            "LONG_ANSWER" -> "Long Answer"
            "NUMERICAL" -> "Numerical Problems"
            else -> type
        }
    }

    private fun Float.roundToOneDecimal(): Float {
        return (this * 10f).roundToInt() / 10f
    }
}
