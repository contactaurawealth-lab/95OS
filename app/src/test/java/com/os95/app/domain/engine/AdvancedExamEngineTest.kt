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
import com.os95.app.domain.model.AdaptiveRetestConfig
import com.os95.app.domain.model.ExamSimulationSubmission
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdvancedExamEngineTest {

    private lateinit var engine: AdvancedExamEngine

    private val mathSubject = SubjectEntity(id = "sub_math", name = "Mathematics")
    private val histSubject = SubjectEntity(id = "sub_hist", name = "History")

    private val quadChapter = ChapterEntity(id = "ch_quad", subjectId = "sub_math", name = "Quadratic Equations", orderIndex = 1)
    private val mauryanChapter = ChapterEntity(id = "ch_mau", subjectId = "sub_hist", name = "Mauryan Age", orderIndex = 1)

    private val quadTopic = TopicEntity(
        id = "top_quad",
        chapterId = "ch_quad",
        name = "Factoring Quadratics",
        weaknessScore = 0.8f,
        examRelevance = "HIGH"
    )
    private val mauryanTopic = TopicEntity(
        id = "top_mau",
        chapterId = "ch_mau",
        name = "Ashokan Edicts",
        weaknessScore = 0.4f,
        examRelevance = "MEDIUM"
    )

    @Before
    fun setUp() {
        engine = AdvancedExamEngine()
    }

    // =========================================================================
    // 6. Time-to-Marks Intelligence Tests
    // =========================================================================

    @Test
    fun testTimeToMarks_ranksWeakHighWeightageChapterFirst() {
        val questions = listOf(
            QuestionBankEntity(
                id = "q1",
                subjectId = "sub_math",
                chapterId = "ch_quad",
                topicId = "top_quad",
                questionText = "Solve x^2 - 5x + 6 = 0",
                marks = 3f,
                timesTested = 5,
                timesFailed = 3
            )
        )

        val mistakes = listOf(
            MistakeEntity(
                id = "m1",
                subjectId = "sub_math",
                chapterId = "ch_quad",
                topicId = "top_quad",
                question = "Solve x^2 - 5x + 6 = 0",
                studentAnswer = "x = 1",
                correctAnswer = "x = 2, 3",
                marksLost = 3f,
                lossCategory = "CALCULATION_ERROR",
                isResolved = false
            )
        )

        val report = engine.calculateTimeToMarks(
            targetPercentage = 95f,
            availableStudyMinutes = 60,
            subjects = listOf(mathSubject, histSubject),
            chapters = listOf(quadChapter, mauryanChapter),
            topics = listOf(quadTopic, mauryanTopic),
            questions = questions,
            examResults = emptyList(),
            mistakes = mistakes,
            studySessions = emptyList()
        )

        assertEquals(95f, report.targetScore, 0.01f)
        assertTrue("Gap should be greater than 0", report.marksGap > 0f)
        assertEquals(60, report.bestUseDurationMinutes)
        assertTrue(report.topImpactChapters.isNotEmpty())

        // Quadratic equations chapter should be ranked higher due to higher weakness (0.8) and HIGH relevance
        val top = report.topImpactChapters.first()
        assertEquals(quadChapter.name, top.chapterName)
        assertTrue(top.potentialGainPercentage >= 25f)
        assertTrue(top.priorityScore > 0f)
    }

    // =========================================================================
    // 7. Adaptive Re-Test Tests
    // =========================================================================

    @Test
    fun testAdaptiveRetest_generatesBalancedQuestionDistribution() {
        val qBank = listOf(
            QuestionBankEntity(
                id = "q_weak1",
                subjectId = "sub_math",
                chapterId = "ch_quad",
                topicId = "top_quad",
                questionText = "Weak Question 1",
                marks = 5f,
                timesTested = 3,
                timesFailed = 2
            ),
            QuestionBankEntity(
                id = "q_recent1",
                subjectId = "sub_math",
                chapterId = "ch_quad",
                topicId = "top_quad",
                questionText = "Recent Fail 1",
                marks = 3f,
                timesTested = 2,
                timesFailed = 2
            ),
            QuestionBankEntity(
                id = "q_mix1",
                subjectId = "sub_math",
                chapterId = "ch_quad",
                topicId = "top_quad",
                questionText = "General Practice",
                marks = 2f,
                timesTested = 1,
                timesFailed = 0
            )
        )

        val mistakes = listOf(
            MistakeEntity(
                id = "q_recent1",
                subjectId = "sub_math",
                chapterId = "ch_quad",
                topicId = "top_quad",
                question = "Recent Fail 1",
                studentAnswer = "Wrong",
                correctAnswer = "Right",
                marksLost = 3f,
                lossCategory = "CARELESS_ERROR",
                isResolved = false
            )
        )

        val config = AdaptiveRetestConfig(
            subjectId = "sub_math",
            title = "Adaptive Re-Test 1",
            durationMinutes = 30,
            targetMarks = 10f
        )

        val result = engine.generateAdaptiveRetest(
            config = config,
            allQuestions = qBank,
            mistakes = mistakes,
            topics = listOf(quadTopic),
            chapters = listOf(quadChapter),
            subjects = listOf(mathSubject)
        )

        assertNotNull(result.paper)
        assertTrue(result.questions.isNotEmpty())
        assertEquals(30, result.paper.durationMinutes)
        assertEquals("READY", result.paper.status)
    }

    @Test
    fun testAdaptiveRetest_completionResolvesMistakesOnFullMarks() {
        val paperQuestions = listOf(
            PaperQuestionEntity(
                id = "pq1",
                paperId = "p1",
                questionId = "q1",
                orderIndex = 1,
                sectionName = "Section A",
                snapshotQuestionText = "Quadratic Roots problem",
                snapshotMarks = 4f,
                snapshotQuestionType = "SHORT",
                snapshotDifficulty = "MEDIUM",
                snapshotAnswer = "x = 2, 3",
                snapshotChapterId = "ch_quad",
                snapshotTopicId = "top_quad"
            )
        )

        val mistakes = listOf(
            MistakeEntity(
                id = "m1",
                subjectId = "sub_math",
                chapterId = "ch_quad",
                topicId = "top_quad",
                question = "Quadratic Roots problem",
                studentAnswer = "Wrong",
                correctAnswer = "x = 2, 3",
                marksLost = 4f,
                lossCategory = "CARELESS_ERROR",
                isResolved = false
            )
        )

        // Award full marks (4.0f)
        val awardedMap = mapOf(0 to 4.0f)

        val summary = engine.evaluateAdaptiveRetestCompletion(
            paperId = "p1",
            marksObtained = 4.0f,
            totalMarks = 4.0f,
            paperQuestions = paperQuestions,
            marksAwardedPerQuestion = awardedMap,
            existingMistakes = mistakes,
            topics = listOf(quadTopic)
        )

        assertEquals(100f, summary.accuracyPercentage, 0.01f)
        assertEquals(1, summary.mistakesFixedCount)
        assertEquals(4.0f, summary.marksRecovered, 0.01f)
        assertTrue(summary.remainingWeakTopics.isEmpty())
        assertTrue(summary.recommendedNextAction.contains("Mastery"))
    }

    // =========================================================================
    // 8. Exam Readiness Simulator Tests
    // =========================================================================

    @Test
    fun testExamReadinessSimulator_computesReadinessScoreDeterministically() {
        val paper = PaperEntity(
            id = "sim_p1",
            title = "Math Mock Exam",
            subjectId = "sub_math",
            totalMarks = 50f,
            durationMinutes = 60,
            status = "READY"
        )

        val questions = listOf(
            PaperQuestionEntity(
                id = "pq1",
                paperId = "sim_p1",
                questionId = "q1",
                orderIndex = 1,
                sectionName = "Section A",
                snapshotQuestionText = "Easy Question",
                snapshotMarks = 2f,
                snapshotQuestionType = "MCQ",
                snapshotDifficulty = "EASY",
                snapshotAnswer = "A",
                snapshotChapterId = "ch_quad",
                snapshotTopicId = "top_quad"
            ),
            PaperQuestionEntity(
                id = "pq2",
                paperId = "sim_p1",
                questionId = "q2",
                orderIndex = 2,
                sectionName = "Section B",
                snapshotQuestionText = "Hard Analytical Question",
                snapshotMarks = 8f,
                snapshotQuestionType = "LONG",
                snapshotDifficulty = "HARD",
                snapshotAnswer = "Solution",
                snapshotChapterId = "ch_quad",
                snapshotTopicId = "top_quad"
            )
        )

        // Missed the easy question (0 marks awarded), scored full on hard question (8 marks)
        val submission = ExamSimulationSubmission(
            paperId = "sim_p1",
            answers = mapOf(0 to "Wrong", 1 to "Correct"),
            marksAwardedMap = mapOf(0 to 0f, 1 to 8f),
            timeSpentSeconds = 3000
        )

        val readinessResult = engine.evaluateExamReadiness(
            submission = submission,
            paper = paper,
            questions = questions,
            chapters = listOf(quadChapter),
            historicalResults = emptyList(),
            allTopics = listOf(quadTopic),
            allMistakes = emptyList()
        )

        assertEquals(8f, readinessResult.score, 0.01f)
        assertEquals(1, readinessResult.easyQuestionsMissedCount)
        assertTrue(readinessResult.readinessScore in 0f..100f)
        assertTrue(readinessResult.readinessFactors.containsKey("Simulation Accuracy"))
        assertTrue(readinessResult.readinessFactors.containsKey("Recent Exam Scores"))
    }

    // =========================================================================
    // 9. Last-7-Days Mode Tests
    // =========================================================================

    @Test
    fun testLast7DaysDashboard_structuresDay7DownToDay1Curriculum() {
        val now = System.currentTimeMillis()
        val targetExamDate = now + (5 * AdvancedExamEngine.ONE_DAY_MS) // 5 days remaining

        val dashboard = engine.buildLast7DaysDashboard(
            targetExamDateTimestamp = targetExamDate,
            completedTaskIds = setOf("d5_t1_priority_chapter"),
            subjects = listOf(mathSubject),
            chapters = listOf(quadChapter),
            topics = listOf(quadTopic),
            mistakes = emptyList(),
            recallCards = emptyList(),
            now = now
        )

        assertTrue(dashboard.isActive)
        assertEquals(5, dashboard.daysRemaining)
        assertEquals(7, dashboard.dailyPlans.size)
        assertEquals(3, dashboard.todayTopThreeTasks.size)

        // Verify completion calculation
        assertTrue(dashboard.overallWeekCompletionPercentage > 0f)
    }

    // =========================================================================
    // 10. 95% Command Center Tests
    // =========================================================================

    @Test
    fun testCommandCenterSnapshot_generates5SecondSituationalAwareness() {
        val results = listOf(
            ExamResultEntity(
                id = "res1",
                paperId = "p1",
                marksObtained = 88f,
                totalMarks = 100f,
                timeTakenMinutes = 60,
                completedAt = System.currentTimeMillis()
            )
        )

        val now = System.currentTimeMillis()
        val snapshot = engine.build95CommandCenterSnapshot(
            studentName = "Alex",
            targetPercentage = 95f,
            targetExamDateTimestamp = now + 6 * AdvancedExamEngine.ONE_DAY_MS,
            subjects = listOf(mathSubject),
            chapters = listOf(quadChapter),
            topics = listOf(quadTopic),
            mistakes = emptyList(),
            examResults = results,
            studySessions = listOf(
                StudySessionEntity(
                    subjectId = "sub_math",
                    durationMinutes = 45,
                    completedAt = now
                )
            ),
            now = now
        )

        assertEquals("Alex", snapshot.studentName)
        assertEquals(88f, snapshot.currentPredictedPercentage, 0.01f)
        assertEquals(95f, snapshot.targetPercentage, 0.01f)
        assertEquals(7f, snapshot.percentageGap, 0.01f)
        assertNotNull(snapshot.primaryAction)
        assertEquals(45, snapshot.todayStudyMinutes)
        assertEquals(120, snapshot.todayTargetMinutes)
        assertEquals(6, snapshot.daysRemaining)
    }
}
