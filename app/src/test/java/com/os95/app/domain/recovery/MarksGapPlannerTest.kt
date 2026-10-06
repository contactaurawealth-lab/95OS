package com.os95.app.domain.recovery

import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.ExamResultEntity
import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.domain.engine.MarksRecoveryEngine
import com.os95.app.domain.model.RecoveryPriorityLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MarksGapPlannerTest {

    private lateinit var engine: MarksRecoveryEngine

    private val mathSubject = SubjectEntity(id = "sub_math", name = "Mathematics")
    private val histSubject = SubjectEntity(id = "sub_hist", name = "History")
    private val engSubject = SubjectEntity(id = "sub_eng", name = "English")

    private val algebraChapter = ChapterEntity(id = "ch_alg", subjectId = "sub_math", name = "Algebra", orderIndex = 1)
    private val mauryanChapter = ChapterEntity(id = "ch_mau", subjectId = "sub_hist", name = "Mauryan Age", orderIndex = 1)
    private val writingChapter = ChapterEntity(id = "ch_wri", subjectId = "sub_eng", name = "Writing Skills", orderIndex = 1)

    private val linearEqTopic = TopicEntity(
        id = "top_lin",
        chapterId = "ch_alg",
        name = "Linear Equations",
        orderIndex = 1,
        weaknessScore = 0.8f,
        examRelevance = "HIGH"
    )
    private val ashokaTopic = TopicEntity(
        id = "top_ash",
        chapterId = "ch_mau",
        name = "Ashoka's Edicts",
        orderIndex = 1,
        weaknessScore = 0.5f,
        examRelevance = "HIGH"
    )
    private val essaysTopic = TopicEntity(
        id = "top_ess",
        chapterId = "ch_wri",
        name = "Essay Composition",
        orderIndex = 1,
        weaknessScore = 0.3f,
        examRelevance = "MEDIUM"
    )

    @Before
    fun setUp() {
        engine = MarksRecoveryEngine()
    }

    @Test
    fun testZeroExamResults_producesPendingBaseline() {
        val plan = engine.calculateMarksGap(
            targetPercentage = 95.0f,
            examResults = emptyList(),
            mistakes = emptyList(),
            topics = emptyList(),
            chapters = emptyList(),
            subjects = emptyList(),
            totalExamMarks = 500f
        )

        assertEquals(0f, plan.currentPercentage, 0.001f)
        assertEquals(95.0f, plan.targetPercentage, 0.001f)
        assertEquals(95.0f, plan.percentageGap, 0.001f)
        assertEquals(475f, plan.marksNeeded!!, 0.001f)
        assertEquals(0f, plan.potentialRecoverableMarks, 0.001f)
        assertFalse(plan.hasSufficientData)
        assertTrue(plan.topOpportunities.isEmpty())
    }

    @Test
    fun testTarget95Percent_withExamResultsAndTotalExamMarks() {
        // Exam Result: 446 / 500 = 89.2%
        val examResult = ExamResultEntity(
            id = "res_1",
            paperId = "paper_1",
            marksObtained = 446f,
            totalMarks = 500f,
            timeTakenMinutes = 60,
            completedAt = 1000L
        )

        val plan = engine.calculateMarksGap(
            targetPercentage = 95.0f,
            examResults = listOf(examResult),
            mistakes = emptyList(),
            topics = emptyList(),
            chapters = emptyList(),
            subjects = emptyList(),
            totalExamMarks = 500f
        )

        assertEquals(89.2f, plan.currentPercentage, 0.05f)
        assertEquals(95.0f, plan.targetPercentage, 0.001f)
        assertEquals(5.8f, plan.percentageGap, 0.05f)
        // 500 * 0.95 = 475. 475 - 446 = 29 marks needed.
        assertEquals(29f, plan.marksNeeded!!, 0.1f)
        assertTrue(plan.hasSufficientData)
    }

    @Test
    fun testTarget100Percent_calculatesFullGap() {
        val examResult = ExamResultEntity(
            id = "res_1",
            paperId = "paper_1",
            marksObtained = 90f,
            totalMarks = 100f,
            timeTakenMinutes = 60,
            completedAt = 1000L
        )

        val plan = engine.calculateMarksGap(
            targetPercentage = 100.0f,
            examResults = listOf(examResult),
            mistakes = emptyList(),
            topics = emptyList(),
            chapters = emptyList(),
            subjects = emptyList(),
            totalExamMarks = 100f
        )

        assertEquals(90.0f, plan.currentPercentage, 0.01f)
        assertEquals(100.0f, plan.targetPercentage, 0.01f)
        assertEquals(10.0f, plan.percentageGap, 0.01f)
        assertEquals(10.0f, plan.marksNeeded!!, 0.01f)
    }

    @Test
    fun testTargetBelowCurrentScore_clampsGapToZero() {
        val examResult = ExamResultEntity(
            id = "res_1",
            paperId = "paper_1",
            marksObtained = 96f,
            totalMarks = 100f,
            timeTakenMinutes = 60,
            completedAt = 1000L
        )

        val plan = engine.calculateMarksGap(
            targetPercentage = 95.0f,
            examResults = listOf(examResult),
            mistakes = emptyList(),
            topics = emptyList(),
            chapters = emptyList(),
            subjects = emptyList(),
            totalExamMarks = 100f
        )

        assertEquals(96.0f, plan.currentPercentage, 0.01f)
        assertEquals(0f, plan.percentageGap, 0.001f)
        assertEquals(0f, plan.marksNeeded!!, 0.001f)
    }

    @Test
    fun testPrioritizationAlgorithm_ranksByDeterministicFormula() {
        // Mathematics - Linear Equations: Lost 6 marks (Calculation Error, High Relevance, 0.8 Weakness)
        val mathMistakes = listOf(
            MistakeEntity(
                id = "m1",
                subjectId = "sub_math",
                chapterId = "ch_alg",
                topicId = "top_lin",
                question = "2x + 5 = 15",
                correctAnswer = "x = 5",
                lossCategory = "CALCULATION_ERROR",
                marksLost = 3f,
                isResolved = false
            ),
            MistakeEntity(
                id = "m2",
                subjectId = "sub_math",
                chapterId = "ch_alg",
                topicId = "top_lin",
                question = "3x - 4 = 11",
                correctAnswer = "x = 5",
                lossCategory = "CALCULATION_ERROR",
                marksLost = 3f,
                isResolved = false
            )
        )

        // History - Ashoka: Lost 4 marks (Forgotten, High Relevance, 0.5 Weakness)
        val histMistakes = listOf(
            MistakeEntity(
                id = "m3",
                subjectId = "sub_hist",
                chapterId = "ch_mau",
                topicId = "top_ash",
                question = "Kalinga Rock Edict XIII",
                correctAnswer = "Dhamma conquest",
                lossCategory = "FORGOTTEN",
                marksLost = 4f,
                isResolved = false
            )
        )

        // English - Writing: Lost 3 marks (Concept Error, Medium Relevance, 0.3 Weakness)
        val engMistakes = listOf(
            MistakeEntity(
                id = "m4",
                subjectId = "sub_eng",
                chapterId = "ch_wri",
                topicId = "top_ess",
                question = "Format of formal letter",
                correctAnswer = "Sender's address, Date, Receiver's address, Subject, Salutation",
                lossCategory = "CONCEPT_ERROR",
                marksLost = 3f,
                isResolved = false
            )
        )

        val allMistakes = mathMistakes + histMistakes + engMistakes

        val plan = engine.calculateMarksGap(
            targetPercentage = 95.0f,
            examResults = listOf(ExamResultEntity(id = "r1", paperId = "p1", marksObtained = 85f, totalMarks = 100f, timeTakenMinutes = 60)),
            mistakes = allMistakes,
            topics = listOf(linearEqTopic, ashokaTopic, essaysTopic),
            chapters = listOf(algebraChapter, mauryanChapter, writingChapter),
            subjects = listOf(mathSubject, histSubject, engSubject)
        )

        assertEquals(13f, plan.potentialRecoverableMarks, 0.001f)
        assertEquals(3, plan.topOpportunities.size)

        // 1st opportunity should be Mathematics - Linear Equations (Lost: 6 marks, score ~ 10.08)
        val top1 = plan.topOpportunities[0]
        assertEquals("top_lin", top1.topicId)
        assertEquals("Mathematics", top1.subjectName)
        assertEquals("Algebra", top1.chapterName)
        assertEquals("Linear Equations", top1.topicName)
        assertEquals(6f, top1.lostMarks, 0.001f)
        assertEquals(RecoveryPriorityLevel.VERY_HIGH, top1.priorityLevel)
        assertEquals("Calculation Error", top1.dominantMistakeCategory)

        // 2nd opportunity should be History - Ashoka's Edicts
        val top2 = plan.topOpportunities[1]
        assertEquals("top_ash", top2.topicId)
        assertEquals("History", top2.subjectName)
        assertEquals(4f, top2.lostMarks, 0.001f)

        // 3rd opportunity should be English - Essay Composition
        val top3 = plan.topOpportunities[2]
        assertEquals("top_ess", top3.topicId)
        assertEquals("English", top3.subjectName)
        assertEquals(3f, top3.lostMarks, 0.001f)
    }

    @Test
    fun testResolvedMistakes_areExcludedFromRecoverableMarks() {
        val mistakes = listOf(
            MistakeEntity(
                id = "m1",
                subjectId = "sub_math",
                chapterId = "ch_alg",
                topicId = "top_lin",
                question = "Resolved problem",
                correctAnswer = "Solved",
                lossCategory = "CARELESS",
                marksLost = 5f,
                isResolved = true // Resolved!
            ),
            MistakeEntity(
                id = "m2",
                subjectId = "sub_math",
                chapterId = "ch_alg",
                topicId = "top_lin",
                question = "Active problem",
                correctAnswer = "Solved",
                lossCategory = "CARELESS",
                marksLost = 2f,
                isResolved = false // Active
            )
        )

        val plan = engine.calculateMarksGap(
            targetPercentage = 95.0f,
            examResults = emptyList(),
            mistakes = mistakes,
            topics = listOf(linearEqTopic),
            chapters = listOf(algebraChapter),
            subjects = listOf(mathSubject)
        )

        assertEquals(2f, plan.potentialRecoverableMarks, 0.001f)
        assertEquals(1, plan.topOpportunities.size)
        assertEquals(2f, plan.topOpportunities[0].lostMarks, 0.001f)
    }
}
