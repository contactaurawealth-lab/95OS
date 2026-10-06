package com.os95.app.domain.recovery

import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.ExamResultEntity
import com.os95.app.core.database.entity.LostMarksEntity
import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.PaperQuestionEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.domain.engine.MarksRecoveryEngine
import com.os95.app.domain.model.PaperTrendDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PreviousPaperAnalyzerTest {

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
    fun testZeroPapers_returnsEmptyAnalysis() {
        val analysis = engine.analyzePreviousPapers(
            papers = emptyList(),
            results = emptyList(),
            paperQuestions = emptyList(),
            lostMarks = emptyList(),
            mistakes = emptyList(),
            topics = emptyList(),
            chapters = emptyList(),
            subjects = emptyList()
        )

        assertEquals(0, analysis.totalPapers)
        assertFalse(analysis.hasEnoughDataForTrend)
        assertEquals(0f, analysis.averagePercentage, 0.001f)
        assertEquals(PaperTrendDirection.INSUFFICIENT_DATA, analysis.trendDirection)
    }

    @Test
    fun testSinglePaper_establishesBaselineWithoutTrend() {
        val paper = PaperEntity(id = "p1", subjectId = "sub_math", title = "Mock Paper #1", totalMarks = 50f, durationMinutes = 60)
        val result = ExamResultEntity(id = "r1", paperId = "p1", marksObtained = 40f, totalMarks = 50f, timeTakenMinutes = 60, completedAt = 1000L)

        val analysis = engine.analyzePreviousPapers(
            papers = listOf(paper),
            results = listOf(result),
            paperQuestions = emptyList(),
            lostMarks = emptyList(),
            mistakes = emptyList(),
            topics = listOf(linearEqTopic),
            chapters = listOf(algebraChapter),
            subjects = listOf(mathSubject)
        )

        assertEquals(1, analysis.totalPapers)
        assertFalse(analysis.hasEnoughDataForTrend)
        assertEquals(80f, analysis.averagePercentage, 0.001f)
        assertEquals(80f, analysis.bestPercentage, 0.001f)
        assertEquals(PaperTrendDirection.INSUFFICIENT_DATA, analysis.trendDirection)
    }

    @Test
    fun testTwoPapers_improvingTrendDirection() {
        val p1 = PaperEntity(id = "p1", subjectId = "sub_math", title = "Paper 1", totalMarks = 50f, durationMinutes = 60)
        val p2 = PaperEntity(id = "p2", subjectId = "sub_math", title = "Paper 2", totalMarks = 50f, durationMinutes = 60)

        // Paper 1: 35/50 (70%), Paper 2: 44/50 (88%)
        val r1 = ExamResultEntity(id = "r1", paperId = "p1", marksObtained = 35f, totalMarks = 50f, timeTakenMinutes = 60, completedAt = 1000L)
        val r2 = ExamResultEntity(id = "r2", paperId = "p2", marksObtained = 44f, totalMarks = 50f, timeTakenMinutes = 55, completedAt = 2000L)

        val analysis = engine.analyzePreviousPapers(
            papers = listOf(p1, p2),
            results = listOf(r1, r2),
            paperQuestions = emptyList(),
            lostMarks = emptyList(),
            mistakes = emptyList(),
            topics = listOf(linearEqTopic),
            chapters = listOf(algebraChapter),
            subjects = listOf(mathSubject)
        )

        assertEquals(2, analysis.totalPapers)
        assertTrue(analysis.hasEnoughDataForTrend)
        assertEquals(79f, analysis.averagePercentage, 0.001f)
        assertEquals(88f, analysis.bestPercentage, 0.001f)
        assertEquals(18f, analysis.improvementPercentage, 0.001f)
        assertEquals(PaperTrendDirection.IMPROVING, analysis.trendDirection)
    }

    @Test
    fun testTwoPapers_decliningTrendDirection() {
        val p1 = PaperEntity(id = "p1", subjectId = "sub_math", title = "Paper 1", totalMarks = 50f, durationMinutes = 60)
        val p2 = PaperEntity(id = "p2", subjectId = "sub_math", title = "Paper 2", totalMarks = 50f, durationMinutes = 60)

        // Paper 1: 45/50 (90%), Paper 2: 38/50 (76%)
        val r1 = ExamResultEntity(id = "r1", paperId = "p1", marksObtained = 45f, totalMarks = 50f, timeTakenMinutes = 50, completedAt = 1000L)
        val r2 = ExamResultEntity(id = "r2", paperId = "p2", marksObtained = 38f, totalMarks = 50f, timeTakenMinutes = 60, completedAt = 2000L)

        val analysis = engine.analyzePreviousPapers(
            papers = listOf(p1, p2),
            results = listOf(r1, r2),
            paperQuestions = emptyList(),
            lostMarks = emptyList(),
            mistakes = emptyList(),
            topics = listOf(linearEqTopic),
            chapters = listOf(algebraChapter),
            subjects = listOf(mathSubject)
        )

        assertTrue(analysis.hasEnoughDataForTrend)
        assertEquals(PaperTrendDirection.DECLINING, analysis.trendDirection)
    }

    @Test
    fun testRepeatedWeaknessDetection_identifiesLossAcrossMultiplePapers() {
        val p1 = PaperEntity(id = "p1", subjectId = "sub_math", title = "Mid-Term", totalMarks = 50f, durationMinutes = 60)
        val p2 = PaperEntity(id = "p2", subjectId = "sub_math", title = "Pre-Board", totalMarks = 50f, durationMinutes = 60)

        val r1 = ExamResultEntity(id = "r1", paperId = "p1", marksObtained = 42f, totalMarks = 50f, timeTakenMinutes = 60, completedAt = 1000L)
        val r2 = ExamResultEntity(id = "r2", paperId = "p2", marksObtained = 44f, totalMarks = 50f, timeTakenMinutes = 55, completedAt = 2000L)

        // Linear equations lost marks in BOTH paper 1 and paper 2
        val lm1 = LostMarksEntity(id = "lm1", examResultId = "r1", topicId = "top_lin", marksLost = 4f, lossCategory = "CARELESS")
        val lm2 = LostMarksEntity(id = "lm2", examResultId = "r2", topicId = "top_lin", marksLost = 3f, lossCategory = "CALCULATION_ERROR")

        // Quadratic equations lost marks ONLY in paper 1
        val lm3 = LostMarksEntity(id = "lm3", examResultId = "r1", topicId = "top_quad", marksLost = 2f, lossCategory = "DIDNT_KNOW")

        val analysis = engine.analyzePreviousPapers(
            papers = listOf(p1, p2),
            results = listOf(r1, r2),
            paperQuestions = emptyList(),
            lostMarks = listOf(lm1, lm2, lm3),
            mistakes = emptyList(),
            topics = listOf(linearEqTopic, quadraticTopic),
            chapters = listOf(algebraChapter),
            subjects = listOf(mathSubject)
        )

        // Only linear equations appeared in >= 2 distinct papers!
        assertEquals(1, analysis.repeatedWeaknesses.size)
        val repeated = analysis.repeatedWeaknesses.first()
        assertEquals("top_lin", repeated.topicId)
        assertEquals("Linear Equations", repeated.topicName)
        assertEquals(7f, repeated.totalLostMarks, 0.001f)
        assertEquals(2, repeated.papersAffectedCount)
    }

    @Test
    fun testChapterAccuracyAndQuestionTypePerformance() {
        val p1 = PaperEntity(id = "p1", subjectId = "sub_math", title = "Algebra Test", totalMarks = 20f, durationMinutes = 30)
        val r1 = ExamResultEntity(id = "r1", paperId = "p1", marksObtained = 16f, totalMarks = 20f, timeTakenMinutes = 30, completedAt = 1000L)

        val pq1 = PaperQuestionEntity(
            id = "pq1",
            paperId = "p1",
            questionId = "q1",
            snapshotQuestionText = "Solve 2x+1=5",
            snapshotQuestionType = "MCQ",
            snapshotDifficulty = "EASY",
            snapshotMarks = 4f,
            snapshotChapterId = "ch_alg",
            snapshotTopicId = "top_lin",
            orderIndex = 1
        )
        val pq2 = PaperQuestionEntity(
            id = "pq2",
            paperId = "p1",
            questionId = "q2",
            snapshotQuestionText = "Find roots",
            snapshotQuestionType = "SHORT_ANSWER",
            snapshotDifficulty = "MEDIUM",
            snapshotMarks = 16f,
            snapshotChapterId = "ch_alg",
            snapshotTopicId = "top_quad",
            orderIndex = 2
        )

        val lm = LostMarksEntity(id = "lm1", examResultId = "r1", topicId = "top_lin", marksLost = 4f, lossCategory = "CALCULATION_ERROR")

        val analysis = engine.analyzePreviousPapers(
            papers = listOf(p1),
            results = listOf(r1),
            paperQuestions = listOf(pq1, pq2),
            lostMarks = listOf(lm),
            mistakes = emptyList(),
            topics = listOf(linearEqTopic, quadraticTopic),
            chapters = listOf(algebraChapter),
            subjects = listOf(mathSubject)
        )

        assertEquals(1, analysis.chapterPerformance.size)
        val chPerf = analysis.chapterPerformance.first()
        assertEquals("ch_alg", chPerf.chapterId)
        assertEquals(4f, chPerf.marksLostTotal, 0.001f)
        // Total marks in chapter was 20f, lost was 4f, accuracy = (16/20)*100 = 80%
        assertEquals(80f, chPerf.accuracyPercentage, 0.001f)
    }
}
