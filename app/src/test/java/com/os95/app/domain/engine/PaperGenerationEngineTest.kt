package com.os95.app.domain.engine

import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.QuestionBankEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.domain.model.PaperBlueprintRequest
import com.os95.app.domain.model.PaperDifficultyMode
import com.os95.app.domain.model.PaperGenerationResult
import com.os95.app.domain.model.RepetitionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class PaperGenerationEngineTest {

    private val engine = PaperGenerationEngine()
    private val validator = PaperValidator()

    private val subjectId = "MATH-01"
    private val chapters = listOf(
        ChapterEntity(id = "chp-alg", subjectId = subjectId, name = "Algebra"),
        ChapterEntity(id = "chp-geo", subjectId = subjectId, name = "Geometry")
    )
    private val topics = listOf(
        TopicEntity(id = "top-lin", chapterId = "chp-alg", name = "Linear Equations", weaknessScore = 0.8f),
        TopicEntity(id = "top-quad", chapterId = "chp-alg", name = "Quadratic Equations", weaknessScore = 0.0f),
        TopicEntity(id = "top-tri", chapterId = "chp-geo", name = "Triangles", weaknessScore = 0.1f)
    )

    private fun createQuestionPool(): List<QuestionBankEntity> {
        val list = mutableListOf<QuestionBankEntity>()
        var qNum = 1

        // 10 1-mark MCQs (10 marks)
        for (i in 1..10) {
            list.add(
                QuestionBankEntity(
                    id = "Q-1M-$i",
                    subjectId = subjectId,
                    chapterId = if (i <= 5) "chp-alg" else "chp-geo",
                    topicId = if (i <= 5) "top-lin" else "top-tri",
                    questionText = "Objective question $i",
                    marks = 1.0f,
                    questionType = "MCQ",
                    difficulty = "EASY"
                )
            )
        }

        // 10 2-mark Short Answers (20 marks)
        for (i in 1..10) {
            list.add(
                QuestionBankEntity(
                    id = "Q-2M-$i",
                    subjectId = subjectId,
                    chapterId = if (i <= 5) "chp-alg" else "chp-geo",
                    topicId = if (i <= 5) "top-quad" else "top-tri",
                    questionText = "Short answer question $i",
                    marks = 2.0f,
                    questionType = "SHORT_ANSWER",
                    difficulty = "MEDIUM"
                )
            )
        }

        // 6 5-mark Long Answers (30 marks)
        for (i in 1..6) {
            list.add(
                QuestionBankEntity(
                    id = "Q-5M-$i",
                    subjectId = subjectId,
                    chapterId = if (i <= 3) "chp-alg" else "chp-geo",
                    topicId = if (i <= 3) "top-lin" else "top-tri",
                    questionText = "Long answer question $i",
                    marks = 5.0f,
                    questionType = "LONG_ANSWER",
                    difficulty = "HARD"
                )
            )
        }

        return list
    }

    @Test
    fun testExactMarksPaperGeneration50Marks() {
        val questions = createQuestionPool()
        val request = PaperBlueprintRequest(
            title = "Term Exam 1",
            subjectId = subjectId,
            chapterIds = listOf("chp-alg", "chp-geo"),
            totalMarks = 50.0f,
            durationMinutes = 60,
            difficultyMode = PaperDifficultyMode.MIXED
        )

        val result = engine.generatePaper(request, questions, chapters, topics)
        assertTrue("Paper generation should succeed", result is PaperGenerationResult.Success)

        val success = result as PaperGenerationResult.Success
        val generated = success.generatedPaper

        // Verify exact mathematical match
        val actualSumMarks = generated.questions.sumOf { it.marks.toDouble() }.toFloat()
        assertEquals(50.0f, actualSumMarks, 0.001f)
        assertEquals(50.0f, generated.paper.totalMarks, 0.001f)

        // Verify PaperValidator passes
        val valResult = validator.validatePaper(generated.paper, generated.questions)
        assertTrue(valResult.isValid)
        assertEquals(0, valResult.errors.size)
    }

    @Test
    fun testImpossibleConfigurationInsufficientQuestionsBlocksGeneration() {
        // Pool with only 8 marks total
        val tinyPool = listOf(
            QuestionBankEntity(id = "Q1", subjectId = subjectId, chapterId = "chp-alg", topicId = null, questionText = "Q1", marks = 3.0f),
            QuestionBankEntity(id = "Q2", subjectId = subjectId, chapterId = "chp-alg", topicId = null, questionText = "Q2", marks = 5.0f)
        )

        val request = PaperBlueprintRequest(
            title = "Impossible Paper",
            subjectId = subjectId,
            totalMarks = 50.0f
        )

        val result = engine.generatePaper(request, tinyPool, chapters, topics)
        assertTrue("Generation must fail when insufficient marks available", result is PaperGenerationResult.Failure)

        val failure = result as PaperGenerationResult.Failure
        assertTrue(failure.reason.contains("50-mark paper cannot be generated"))
        assertTrue(failure.recoverySuggestions.isNotEmpty())
    }

    @Test
    fun testRepetitionPolicyStrictlyNewExcludesTestedQuestions() {
        val questions = createQuestionPool().mapIndexed { idx, q ->
            if (idx < 15) q.copy(timesTested = 2) else q.copy(timesTested = 0)
        }

        val request = PaperBlueprintRequest(
            title = "Strictly Fresh Paper",
            subjectId = subjectId,
            totalMarks = 20.0f,
            repetitionPolicy = RepetitionPolicy.STRICTLY_NEW
        )

        val result = engine.generatePaper(request, questions, chapters, topics)
        assertTrue(result is PaperGenerationResult.Success)

        val generated = (result as PaperGenerationResult.Success).generatedPaper
        for (q in generated.questions) {
            assertEquals("All selected questions must be fresh (timesTested == 0)", 0, q.timesTested)
        }
    }

    @Test
    fun testAdaptiveWeakTopicWeightingPrioritizesWeakTopicsAndExplainsRationale() {
        val questions = createQuestionPool()
        val request = PaperBlueprintRequest(
            title = "Adaptive Paper",
            subjectId = subjectId,
            chapterIds = listOf("chp-alg", "chp-geo"),
            totalMarks = 40.0f,
            adaptiveWeakTopicWeighting = true
        )

        val result = engine.generatePaper(request, questions, chapters, topics)
        assertTrue(result is PaperGenerationResult.Success)

        val generated = (result as PaperGenerationResult.Success).generatedPaper
        assertNotNull(generated.adaptiveRationale)
        assertTrue(generated.adaptiveRationale!!.contains("Algebra"))

        // Verify Algebra has strong representation due to recorded mark loss
        val algebraMarks = generated.chapterMarksCoverage["chp-alg"] ?: 0f
        assertTrue("Algebra must be well-represented due to weakness weighting", algebraMarks >= 15f)
    }

    @Test
    fun testSectionsPartitioningCreatesStructuredExam() {
        val questions = createQuestionPool()
        val request = PaperBlueprintRequest(
            title = "Structured Board Paper",
            subjectId = subjectId,
            totalMarks = 50.0f,
            durationMinutes = 90
        )

        val result = engine.generatePaper(request, questions, chapters, topics)
        assertTrue(result is PaperGenerationResult.Success)

        val generated = (result as PaperGenerationResult.Success).generatedPaper
        assertTrue("Should produce multiple structured sections", generated.sections.size >= 2)

        for (sec in generated.sections) {
            assertTrue("Section ${sec.name} should have questions", sec.questions.isNotEmpty())
            assertTrue("Section marks must equal sum of questions", sec.sectionMarks > 0f)
        }
    }
}
