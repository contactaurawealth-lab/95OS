package com.os95.app.domain.recovery

import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.ExamResultEntity
import com.os95.app.core.database.entity.LostMarksEntity
import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.domain.engine.MarksRecoveryEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecoveryScoreTest {

    private lateinit var engine: MarksRecoveryEngine

    private val mathSubject = SubjectEntity(id = "sub_math", name = "Mathematics")
    private val algebraChapter = ChapterEntity(id = "ch_alg", subjectId = "sub_math", name = "Algebra", orderIndex = 1)
    private val linearEqTopic = TopicEntity(id = "top_lin", chapterId = "ch_alg", name = "Linear Equations", orderIndex = 1)
    private val quadraticTopic = TopicEntity(id = "top_quad", chapterId = "ch_alg", name = "Quadratic Equations", orderIndex = 2)

    @Before
    fun setUp() {
        engine = MarksRecoveryEngine()
    }

    @Test
    fun testLessThanTwoResults_cannotComputeRecovery() {
        val paper = PaperEntity(id = "p1", subjectId = "sub_math", title = "Mock 1", totalMarks = 50f)
        val result = ExamResultEntity(id = "r1", paperId = "p1", marksObtained = 40f, totalMarks = 50f, timeTakenMinutes = 60)

        val report = engine.calculateRecoveryScore(
            papers = listOf(paper),
            results = listOf(result),
            lostMarks = emptyList(),
            mistakes = emptyList(),
            topics = emptyList(),
            chapters = emptyList(),
            subjects = emptyList()
        )

        assertFalse(report.hasComparablePapers)
        assertEquals(0f, report.totalRecoveredMarks, 0.001f)
    }

    @Test
    fun testPositiveRecovery_identicalPaperMarks() {
        val p1 = PaperEntity(id = "p1", subjectId = "sub_math", title = "Paper 1", totalMarks = 50f)
        val p2 = PaperEntity(id = "p2", subjectId = "sub_math", title = "Paper 2", totalMarks = 50f)

        // Paper 1: Scored 32/50 -> Lost 18 marks
        val r1 = ExamResultEntity(id = "r1", paperId = "p1", marksObtained = 32f, totalMarks = 50f, timeTakenMinutes = 60, completedAt = 1000L)
        // Paper 2: Scored 41/50 -> Lost 9 marks
        val r2 = ExamResultEntity(id = "r2", paperId = "p2", marksObtained = 41f, totalMarks = 50f, timeTakenMinutes = 55, completedAt = 2000L)

        val report = engine.calculateRecoveryScore(
            papers = listOf(p1, p2),
            results = listOf(r1, r2),
            lostMarks = emptyList(),
            mistakes = emptyList(),
            topics = emptyList(),
            chapters = emptyList(),
            subjects = listOf(mathSubject)
        )

        assertTrue(report.hasComparablePapers)
        assertEquals(18f, report.previousLostMarks, 0.001f)
        assertEquals(9f, report.currentLostMarks, 0.001f)
        // 18 - 9 = +9 marks recovered
        assertEquals(9.0f, report.totalRecoveredMarks, 0.001f)
    }

    @Test
    fun testNegativeRecovery_regressionOnRetest() {
        val p1 = PaperEntity(id = "p1", subjectId = "sub_math", title = "Paper 1", totalMarks = 50f)
        val p2 = PaperEntity(id = "p2", subjectId = "sub_math", title = "Paper 2", totalMarks = 50f)

        // Paper 1: Scored 41/50 -> Lost 9 marks
        val r1 = ExamResultEntity(id = "r1", paperId = "p1", marksObtained = 41f, totalMarks = 50f, timeTakenMinutes = 50, completedAt = 1000L)
        // Paper 2: Scored 38/50 -> Lost 12 marks
        val r2 = ExamResultEntity(id = "r2", paperId = "p2", marksObtained = 38f, totalMarks = 50f, timeTakenMinutes = 60, completedAt = 2000L)

        val report = engine.calculateRecoveryScore(
            papers = listOf(p1, p2),
            results = listOf(r1, r2),
            lostMarks = emptyList(),
            mistakes = emptyList(),
            topics = emptyList(),
            chapters = emptyList(),
            subjects = listOf(mathSubject)
        )

        assertTrue(report.hasComparablePapers)
        assertEquals(9f, report.previousLostMarks, 0.001f)
        assertEquals(12f, report.currentLostMarks, 0.001f)
        // 9 - 12 = -3 marks regression
        assertEquals(-3.0f, report.totalRecoveredMarks, 0.001f)
    }

    @Test
    fun testZeroRecovery_identicalScoreLoss() {
        val p1 = PaperEntity(id = "p1", subjectId = "sub_math", title = "Paper 1", totalMarks = 50f)
        val p2 = PaperEntity(id = "p2", subjectId = "sub_math", title = "Paper 2", totalMarks = 50f)

        // Both papers lost 10 marks
        val r1 = ExamResultEntity(id = "r1", paperId = "p1", marksObtained = 40f, totalMarks = 50f, timeTakenMinutes = 60, completedAt = 1000L)
        val r2 = ExamResultEntity(id = "r2", paperId = "p2", marksObtained = 40f, totalMarks = 50f, timeTakenMinutes = 60, completedAt = 2000L)

        val report = engine.calculateRecoveryScore(
            papers = listOf(p1, p2),
            results = listOf(r1, r2),
            lostMarks = emptyList(),
            mistakes = emptyList(),
            topics = emptyList(),
            chapters = emptyList(),
            subjects = listOf(mathSubject)
        )

        assertEquals(0.0f, report.totalRecoveredMarks, 0.001f)
    }

    @Test
    fun testAssessmentComparability_normalizationAcrossDifferentTotalMarks() {
        // Paper 1: 50 marks max. Student scored 35/50 (Lost 15 marks, i.e. 30% lost).
        val p1 = PaperEntity(id = "p1", subjectId = "sub_math", title = "Half Syllabus Test", totalMarks = 50f)
        val r1 = ExamResultEntity(id = "r1", paperId = "p1", marksObtained = 35f, totalMarks = 50f, timeTakenMinutes = 60, completedAt = 1000L)

        // Paper 2: 100 marks max. Student scored 85/100 (Lost 15 marks, i.e. only 15% lost).
        val p2 = PaperEntity(id = "p2", subjectId = "sub_math", title = "Full Syllabus Exam", totalMarks = 100f)
        val r2 = ExamResultEntity(id = "r2", paperId = "p2", marksObtained = 85f, totalMarks = 100f, timeTakenMinutes = 120, completedAt = 2000L)

        val report = engine.calculateRecoveryScore(
            papers = listOf(p1, p2),
            results = listOf(r1, r2),
            lostMarks = emptyList(),
            mistakes = emptyList(),
            topics = emptyList(),
            chapters = emptyList(),
            subjects = listOf(mathSubject)
        )

        // Without normalization: 15 - 15 = 0.
        // WITH normalization: (15/50) * 100 = 30 normalized previous lost marks.
        // Recovered = 30 - 15 = +15 marks!
        assertEquals(15.0f, report.totalRecoveredMarks, 0.001f)
    }

    @Test
    fun testTopicRecoveryItemBreakdown() {
        val p1 = PaperEntity(id = "p1", subjectId = "sub_math", title = "Paper 1", totalMarks = 50f)
        val p2 = PaperEntity(id = "p2", subjectId = "sub_math", title = "Paper 2", totalMarks = 50f)

        val r1 = ExamResultEntity(id = "r1", paperId = "p1", marksObtained = 38f, totalMarks = 50f, timeTakenMinutes = 60, completedAt = 1000L)
        val r2 = ExamResultEntity(id = "r2", paperId = "p2", marksObtained = 46f, totalMarks = 50f, timeTakenMinutes = 55, completedAt = 2000L)

        // In Paper 1: Lost 6 marks in Linear Equations, 6 marks in Quadratic
        val lm1 = LostMarksEntity(id = "lm1", examResultId = "r1", topicId = "top_lin", marksLost = 6f, lossCategory = "CALCULATION_ERROR")
        val lm2 = LostMarksEntity(id = "lm2", examResultId = "r1", topicId = "top_quad", marksLost = 6f, lossCategory = "FORGOTTEN")

        // In Paper 2: Lost only 1 mark in Linear Equations, 3 marks in Quadratic
        val lm3 = LostMarksEntity(id = "lm3", examResultId = "r2", topicId = "top_lin", marksLost = 1f, lossCategory = "CARELESS")
        val lm4 = LostMarksEntity(id = "lm4", examResultId = "r2", topicId = "top_quad", marksLost = 3f, lossCategory = "CARELESS")

        val report = engine.calculateRecoveryScore(
            papers = listOf(p1, p2),
            results = listOf(r1, r2),
            lostMarks = listOf(lm1, lm2, lm3, lm4),
            mistakes = emptyList(),
            topics = listOf(linearEqTopic, quadraticTopic),
            chapters = listOf(algebraChapter),
            subjects = listOf(mathSubject)
        )

        assertEquals(2, report.recoveryByTopic.size)
        // Linear Equations recovered 6 - 1 = +5 marks
        val linRec = report.recoveryByTopic.first { it.topicId == "top_lin" }
        assertEquals(5f, linRec.recoveredMarks, 0.001f)
        // Quadratic Equations recovered 6 - 3 = +3 marks
        val quadRec = report.recoveryByTopic.first { it.topicId == "top_quad" }
        assertEquals(3f, quadRec.recoveredMarks, 0.001f)
    }
}
