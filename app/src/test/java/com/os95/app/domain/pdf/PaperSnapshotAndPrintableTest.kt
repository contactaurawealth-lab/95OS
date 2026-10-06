package com.os95.app.domain.pdf

import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.PaperQuestionEntity
import com.os95.app.core.database.entity.QuestionBankEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.domain.engine.PaperValidator
import com.os95.app.testutil.FakeDatabaseProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class PaperSnapshotAndPrintableTest {

    private lateinit var database: FakeDatabaseProvider
    private val formatter = PrintableExamPaperFormatter()
    private val validator = PaperValidator()

    @Before
    fun setUp() {
        database = FakeDatabaseProvider()
    }

    @After
    fun tearDown() {
        database.clearAll()
    }

    @Test
    fun testFinalizedPaperRemainsIntactWhenQuestionIsDeletedFromBank() = runBlocking {
        // 1. Setup Subject, Chapter, Question
        val sub = SubjectEntity(id = "sub-math", name = "Mathematics")
        val chp = ChapterEntity(id = "chp-alg", subjectId = "sub-math", name = "Algebra")
        val q1 = QuestionBankEntity(
            id = "Q-DEL-1",
            subjectId = "sub-math",
            chapterId = "chp-alg",
            topicId = null,
            questionText = "Solve for x: 3x - 7 = 14",
            marks = 3.0f,
            difficulty = "MEDIUM",
            questionType = "SHORT_ANSWER"
        )
        database.syllabusDao().insertSubject(sub)
        database.syllabusDao().insertChapter(chp)
        database.paperPilotDao().insertQuestion(q1)

        // 2. Finalize a paper with snapshot of Q1
        val paper = PaperEntity(
            id = "paper-final-1",
            title = "Algebra Test 1",
            subjectId = "sub-math",
            totalMarks = 3.0f,
            durationMinutes = 30,
            status = "READY"
        )
        database.paperPilotDao().insertPaper(paper)

        val pq = PaperQuestionEntity(
            id = UUID.randomUUID().toString(),
            paperId = paper.id,
            questionId = q1.id,
            orderIndex = 1,
            sectionName = "Section A",
            snapshotQuestionText = q1.questionText,
            snapshotMarks = q1.marks,
            snapshotQuestionType = q1.questionType,
            snapshotDifficulty = q1.difficulty,
            snapshotAnswer = q1.markingScheme,
            snapshotChapterId = q1.chapterId,
            snapshotTopicId = q1.topicId
        )
        database.paperPilotDao().insertPaperQuestion(pq)

        // 3. Verify paper has question
        val snapshotQuestionsBefore = database.paperPilotDao().getPaperQuestionsSync(paper.id)
        assertEquals(1, snapshotQuestionsBefore.size)
        assertEquals("Solve for x: 3x - 7 = 14", snapshotQuestionsBefore.first().snapshotQuestionText)

        // 4. DELETE the question from Question Bank
        database.paperPilotDao().deleteQuestion(q1)
        val bankQuestions = database.paperPilotDao().getAllQuestionsSync()
        assertEquals(0, bankQuestions.size)

        // 5. Verify the Finalized Paper snapshot remains completely intact!
        val snapshotQuestionsAfter = database.paperPilotDao().getPaperQuestionsSync(paper.id)
        assertEquals(1, snapshotQuestionsAfter.size)
        assertEquals("Solve for x: 3x - 7 = 14", snapshotQuestionsAfter.first().snapshotQuestionText)
        assertEquals(3.0f, snapshotQuestionsAfter.first().snapshotMarks, 0.001f)

        // Validate Paper validation remains valid
        val valResult = validator.validatePaperQuestions(paper, snapshotQuestionsAfter)
        assertTrue("Paper validation must succeed even after original question deletion", valResult.isValid)
    }

    @Test
    fun testPrintableExamPaperFormattingRendersAcademicExamSheet() {
        val paper = PaperEntity(
            id = "p-print-1",
            title = "Final Examination — Physics",
            subjectId = "PHYS-01",
            totalMarks = 50.0f,
            durationMinutes = 60,
            status = "READY"
        )

        val snapshotQuestions = listOf(
            PaperQuestionEntity(
                id = "pq-1",
                paperId = paper.id,
                orderIndex = 1,
                sectionName = "Section A",
                snapshotQuestionText = "What is the SI unit of electric current?",
                snapshotMarks = 1.0f,
                snapshotQuestionType = "MCQ"
            ),
            PaperQuestionEntity(
                id = "pq-2",
                paperId = paper.id,
                orderIndex = 2,
                sectionName = "Section B",
                snapshotQuestionText = "State Ohm's Law and deduce the formula V = IR.",
                snapshotMarks = 3.0f,
                snapshotQuestionType = "SHORT_ANSWER"
            )
        )

        val formatted = formatter.formatFromSnapshots(paper, "Physics", snapshotQuestions)
        val printableText = formatter.renderToPrintableText(formatted)

        // Verify Header elements
        assertTrue(printableText.contains("95OS EXAMINATION"))
        assertTrue(printableText.contains("FINAL EXAMINATION — PHYSICS"))
        assertTrue(printableText.contains("Candidate Name:"))
        assertTrue(printableText.contains("Time Allowed: 60 Minutes"))
        assertTrue(printableText.contains("Maximum Marks: 50 Marks"))
        assertTrue(printableText.contains("GENERAL INSTRUCTIONS:"))

        // Verify Question formatting & numbering
        assertTrue(printableText.contains("Q1. What is the SI unit of electric current?"))
        assertTrue(printableText.contains("[1 Mark]"))
        assertTrue(printableText.contains("Q2. State Ohm's Law and deduce the formula V = IR."))
        assertTrue(printableText.contains("[3 Marks]"))
        assertTrue(printableText.contains("END OF QUESTION PAPER"))
    }
}
