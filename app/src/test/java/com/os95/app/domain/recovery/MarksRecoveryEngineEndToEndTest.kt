package com.os95.app.domain.recovery

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
import com.os95.app.domain.engine.MarksRecoveryEngine
import com.os95.app.domain.model.PaperTrendDirection
import com.os95.app.domain.model.RecoveryPriorityLevel
import com.os95.app.domain.model.RescueActionType
import com.os95.app.domain.model.RetentionRiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MarksRecoveryEngineEndToEndTest {

    private lateinit var engine: MarksRecoveryEngine
    private val nowMs = 1_700_000_000_000L
    private val oneDayMs = MarksRecoveryEngine.ONE_DAY_MS

    // Academic Domain Data
    private val mathSubject = SubjectEntity(id = "sub_math", name = "Mathematics")
    private val algebraChapter = ChapterEntity(id = "ch_alg", subjectId = "sub_math", name = "Algebra", orderIndex = 1)
    private val linearEqTopic = TopicEntity(
        id = "top_lin",
        chapterId = "ch_alg",
        name = "Linear Equations",
        orderIndex = 1,
        weaknessScore = 0.8f,
        examRelevance = "HIGH"
    )
    private val quadraticTopic = TopicEntity(
        id = "top_quad",
        chapterId = "ch_alg",
        name = "Quadratic Equations",
        orderIndex = 2,
        weaknessScore = 0.6f,
        examRelevance = "HIGH"
    )

    @Before
    fun setUp() {
        engine = MarksRecoveryEngine()
    }

    @Test
    fun testCompleteMarksRecoveryClosedLoopPipeline() {
        // --- PHASE 1: Initial Baseline Setup ---
        // Student target: 95%.
        val targetPercentage = 95.0f
        val subjects = listOf(mathSubject)
        val chapters = listOf(algebraChapter)
        val topics = listOf(linearEqTopic, quadraticTopic)

        // Recall card: overdue by 4 days, low ease factor -> high decay risk
        val card1 = RecallCardEntity(
            id = "card_1",
            subjectId = "sub_math",
            chapterId = "ch_alg",
            topicId = "top_lin",
            prompt = "Standard form of linear equation in two variables",
            expectedAnswer = "ax + by + c = 0",
            dueDate = nowMs - (4 * oneDayMs),
            intervalDays = 2,
            repetitions = 1,
            easeFactor = 1.4f
        )
        val cards = mutableListOf(card1)
        val reviews = mutableListOf<RecallReviewEntity>()

        // --- PHASE 2: Student Takes Exam Paper #1 ---
        val paper1 = PaperEntity(
            id = "paper_1",
            subjectId = "sub_math",
            title = "Algebra Diagnostic Test #1",
            totalMarks = 50f,
            durationMinutes = 60,
            status = "COMPLETED"
        )
        val papers = mutableListOf(paper1)

        val pq1 = PaperQuestionEntity(
            id = "pq_1",
            paperId = "paper_1",
            questionId = "q_1",
            snapshotQuestionText = "Solve 4x + 7 = 31",
            snapshotQuestionType = "SHORT_ANSWER",
            snapshotMarks = 25f,
            snapshotChapterId = "ch_alg",
            snapshotTopicId = "top_lin"
        )
        val pq2 = PaperQuestionEntity(
            id = "pq_2",
            paperId = "paper_1",
            questionId = "q_2",
            snapshotQuestionText = "Solve x^2 - 5x + 6 = 0",
            snapshotQuestionType = "SHORT_ANSWER",
            snapshotMarks = 25f,
            snapshotChapterId = "ch_alg",
            snapshotTopicId = "top_quad"
        )
        val paperQuestions = mutableListOf(pq1, pq2)

        // Student scores 38/50 (76.0%) -> Lost 12 marks
        val result1 = ExamResultEntity(
            id = "res_1",
            paperId = "paper_1",
            marksObtained = 38f,
            totalMarks = 50f,
            timeTakenMinutes = 60,
            completedAt = nowMs - (2 * oneDayMs)
        )
        val results = mutableListOf(result1)

        // Lost marks: 6 marks lost in Linear Equations, 6 marks in Quadratic
        val lm1 = LostMarksEntity(id = "lm_1", examResultId = "res_1", topicId = "top_lin", marksLost = 6f, lossCategory = "CALCULATION_ERROR")
        val lm2 = LostMarksEntity(id = "lm_2", examResultId = "res_1", topicId = "top_quad", marksLost = 6f, lossCategory = "FORGOTTEN")
        val lostMarks = mutableListOf(lm1, lm2)

        // Log active mistakes in Mistake Bank
        val m1 = MistakeEntity(
            id = "m_1",
            subjectId = "sub_math",
            chapterId = "ch_alg",
            topicId = "top_lin",
            question = "Solve 4x + 7 = 31",
            correctAnswer = "x = 6",
            lossCategory = "CALCULATION_ERROR",
            marksLost = 6f,
            isResolved = false
        )
        val m2 = MistakeEntity(
            id = "m_2",
            subjectId = "sub_math",
            chapterId = "ch_alg",
            topicId = "top_quad",
            question = "Solve x^2 - 5x + 6 = 0",
            correctAnswer = "x = 2 or x = 3",
            lossCategory = "FORGOTTEN",
            marksLost = 6f,
            isResolved = false
        )
        val mistakes = mutableListOf(m1, m2)

        // --- PHASE 3: Marks Gap Planner Evaluation ---
        val gapPlan1 = engine.calculateMarksGap(
            targetPercentage = targetPercentage,
            examResults = results,
            mistakes = mistakes,
            topics = topics,
            chapters = chapters,
            subjects = subjects,
            totalExamMarks = 50f
        )

        assertEquals(76.0f, gapPlan1.currentPercentage, 0.01f)
        assertEquals(95.0f, gapPlan1.targetPercentage, 0.01f)
        assertEquals(19.0f, gapPlan1.percentageGap, 0.01f)
        assertEquals(9.5f, gapPlan1.marksNeeded!!, 0.01f)
        assertEquals(12.0f, gapPlan1.potentialRecoverableMarks, 0.01f)

        // Top opportunity should be Linear Equations (Calculation Error has high recoverability)
        val topOpp = gapPlan1.topOpportunities.first()
        assertEquals("top_lin", topOpp.topicId)
        assertEquals("Linear Equations", topOpp.topicName)
        assertEquals(6.0f, topOpp.lostMarks, 0.01f)
        assertEquals(RecoveryPriorityLevel.HIGH, topOpp.priorityLevel)

        // --- PHASE 4: Forgetting Radar Analysis ---
        val radarSnapshot1 = engine.calculateForgettingRadar(
            cards = cards,
            reviews = reviews,
            topics = topics,
            chapters = chapters,
            subjects = subjects,
            nowMs = nowMs
        )

        assertEquals(1, radarSnapshot1.criticalCount)
        val criticalRiskTopic = radarSnapshot1.topics.first { it.topicId == "top_lin" }
        assertEquals(RetentionRiskLevel.CRITICAL, criticalRiskTopic.riskLevel)
        assertEquals(1, criticalRiskTopic.cardsDueCount)

        // --- PHASE 5: 15-Minute Rescue Mode Generation ---
        val rescuePlan = engine.generateRescuePlan(
            durationMinutes = 15,
            marksGap = gapPlan1,
            forgettingRadar = radarSnapshot1,
            mistakes = mistakes,
            topics = topics
        )

        assertEquals(15, rescuePlan.totalDurationMinutes)
        assertEquals(3, rescuePlan.blocks.size)

        // Block 1: Review overdue card in Linear Equations
        val b1 = rescuePlan.blocks[0]
        assertEquals(RescueActionType.RECALL_REVIEW, b1.actionType)
        assertEquals("Linear Equations", b1.targetName)

        // Block 2: Fix calculation mistakes
        val b2 = rescuePlan.blocks[1]
        assertEquals(RescueActionType.MISTAKE_FIX, b2.actionType)

        // Block 3: Rapid topic review on top opportunity
        val b3 = rescuePlan.blocks[2]
        assertEquals(RescueActionType.RAPID_TOPIC_READ, b3.actionType)
        assertEquals("Linear Equations", b3.targetName)

        // --- PHASE 6: Student Executes Rescue Actions ---
        // 1. Review recall card -> rating GOOD (rating = 4)
        reviews.add(
            RecallReviewEntity(
                id = "rev_1",
                cardId = "card_1",
                rating = 4,
                reviewTimestamp = nowMs
            )
        )
        // Card updated: interval 4 days, ease factor 2.5
        cards[0] = card1.copy(dueDate = nowMs + (4 * oneDayMs), intervalDays = 4, repetitions = 2, easeFactor = 2.5f)

        // 2. Resolve calculation mistake
        mistakes[0] = m1.copy(isResolved = true)

        // --- PHASE 7: Student Takes Re-Test (Paper #2) ---
        val paper2 = PaperEntity(
            id = "paper_2",
            subjectId = "sub_math",
            title = "Algebra Mastery Re-Test #2",
            totalMarks = 50f,
            durationMinutes = 60,
            status = "COMPLETED"
        )
        papers.add(paper2)

        val pq3 = PaperQuestionEntity(
            id = "pq_3",
            paperId = "paper_2",
            questionId = "q_3",
            snapshotQuestionText = "Solve 3(x + 2) = 24",
            snapshotQuestionType = "SHORT_ANSWER",
            snapshotMarks = 25f,
            snapshotChapterId = "ch_alg",
            snapshotTopicId = "top_lin"
        )
        val pq4 = PaperQuestionEntity(
            id = "pq_4",
            paperId = "paper_2",
            questionId = "q_4",
            snapshotQuestionText = "Solve 2x^2 - 8 = 0",
            snapshotQuestionType = "SHORT_ANSWER",
            snapshotMarks = 25f,
            snapshotChapterId = "ch_alg",
            snapshotTopicId = "top_quad"
        )
        paperQuestions.addAll(listOf(pq3, pq4))

        // Student scores 47/50 (94.0%)!
        val result2 = ExamResultEntity(
            id = "res_2",
            paperId = "paper_2",
            marksObtained = 47f,
            totalMarks = 50f,
            timeTakenMinutes = 55,
            completedAt = nowMs
        )
        results.add(result2)

        // Lost marks in paper 2: only 1 mark in Linear Equations, 2 marks in Quadratic
        val lm3 = LostMarksEntity(id = "lm_3", examResultId = "res_2", topicId = "top_lin", marksLost = 1f, lossCategory = "CARELESS")
        val lm4 = LostMarksEntity(id = "lm_4", examResultId = "res_2", topicId = "top_quad", marksLost = 2f, lossCategory = "CARELESS")
        lostMarks.addAll(listOf(lm3, lm4))

        // --- PHASE 8: Previous Paper Analyzer & Trend Detection ---
        val paperAnalysis = engine.analyzePreviousPapers(
            papers = papers,
            results = results,
            paperQuestions = paperQuestions,
            lostMarks = lostMarks,
            mistakes = mistakes,
            topics = topics,
            chapters = chapters,
            subjects = subjects
        )

        assertEquals(2, paperAnalysis.totalPapers)
        assertTrue(paperAnalysis.hasEnoughDataForTrend)
        assertEquals(85.0f, paperAnalysis.averagePercentage, 0.01f)
        assertEquals(94.0f, paperAnalysis.bestPercentage, 0.01f)
        assertEquals(18.0f, paperAnalysis.improvementPercentage, 0.01f)
        assertEquals(PaperTrendDirection.IMPROVING, paperAnalysis.trendDirection)

        // --- PHASE 9: Recovery Score Verification ---
        val recoveryReport = engine.calculateRecoveryScore(
            papers = papers,
            results = results,
            lostMarks = lostMarks,
            mistakes = mistakes,
            topics = topics,
            chapters = chapters,
            subjects = subjects
        )

        assertTrue(recoveryReport.hasComparablePapers)
        assertEquals(12.0f, recoveryReport.previousLostMarks, 0.001f)
        assertEquals(3.0f, recoveryReport.currentLostMarks, 0.001f)
        // 12 - 3 = +9 marks recovered!
        assertEquals(9.0f, recoveryReport.totalRecoveredMarks, 0.001f)

        // Linear equations recovered 6 - 1 = +5 marks
        val linRecovery = recoveryReport.recoveryByTopic.first { it.topicId == "top_lin" }
        assertEquals(5.0f, linRecovery.recoveredMarks, 0.001f)

        // Quadratic equations recovered 6 - 2 = +4 marks
        val quadRecovery = recoveryReport.recoveryByTopic.first { it.topicId == "top_quad" }
        assertEquals(4.0f, quadRecovery.recoveredMarks, 0.001f)

        // --- PHASE 10: Updated Marks Gap Planner ---
        val gapPlan2 = engine.calculateMarksGap(
            targetPercentage = targetPercentage,
            examResults = results,
            mistakes = mistakes,
            topics = topics,
            chapters = chapters,
            subjects = subjects,
            totalExamMarks = 50f
        )

        // Average percentage now 85.0%
        assertEquals(85.0f, gapPlan2.currentPercentage, 0.01f)
        assertEquals(10.0f, gapPlan2.percentageGap, 0.01f)
        // Only active mistake left is m_2 (6 marks)
        assertEquals(6.0f, gapPlan2.potentialRecoverableMarks, 0.01f)

        // Radar has no critical topics left
        val radarSnapshot2 = engine.calculateForgettingRadar(
            cards = cards,
            reviews = reviews,
            topics = topics,
            chapters = chapters,
            subjects = subjects,
            nowMs = nowMs
        )
        assertEquals(0, radarSnapshot2.criticalCount)
    }
}
