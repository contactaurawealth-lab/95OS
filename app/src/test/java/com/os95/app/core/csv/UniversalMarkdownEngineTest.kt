package com.os95.app.core.csv

import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.core.database.entity.QuestionBankEntity
import com.os95.app.core.database.entity.RecallCardEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.testutil.FakeDatabaseProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UniversalMarkdownEngineTest {

    private lateinit var engine: UniversalMarkdownEngine
    private lateinit var fakeDb: FakeDatabaseProvider

    @Before
    fun setUp() {
        engine = UniversalMarkdownEngine()
        fakeDb = FakeDatabaseProvider()
    }

    @Test
    fun testParseMarkdownSyllabusOutline_parsesHierarchyCorrectly() {
        val markdown = """
            # Mathematics
            ## Chapter: Quadratic Equations
            - Factoring Quadratics [HIGH] [MASTERED]
            - Completing the Square [MEDIUM] [LEARNING]
            
            # Physics
            ## Laws of Motion
            - Newton's First Law [HIGH] [REVISED]
        """.trimIndent()

        val rows = engine.parseMarkdown(CsvDatasetType.SYLLABUS, markdown)
        assertEquals(3, rows.size)

        val row1 = rows[0]
        assertEquals("Mathematics", row1["subject_name"])
        assertEquals("Quadratic Equations", row1["chapter_name"])
        assertEquals("Factoring Quadratics", row1["topic_name"])
        assertEquals("HIGH", row1["exam_relevance"])
        assertEquals("MASTERED", row1["mastery_state"])

        val row3 = rows[2]
        assertEquals("Physics", row3["subject_name"])
        assertEquals("Laws of Motion", row3["chapter_name"])
        assertEquals("Newton's First Law", row3["topic_name"])
        assertEquals("HIGH", row3["exam_relevance"])
        assertEquals("REVISED", row3["mastery_state"])
    }

    @Test
    fun testParseMarkdownTable_parsesRowsCorrectly() {
        val markdown = """
            | subject_name | chapter_name | topic_name | exam_relevance | mastery_state |
            |--------------|--------------|------------|----------------|---------------|
            | Chemistry    | Organic Chem | Alkanes    | HIGH           | MASTERED      |
            | Chemistry    | Organic Chem | Alkenes    | MEDIUM         | LEARNING      |
        """.trimIndent()

        val rows = engine.parseMarkdown(CsvDatasetType.SYLLABUS, markdown)
        assertEquals(2, rows.size)
        assertEquals("Chemistry", rows[0]["subject_name"])
        assertEquals("Alkanes", rows[0]["topic_name"])
        assertEquals("HIGH", rows[0]["exam_relevance"])
    }

    @Test
    fun testParseMarkdownQuestionsOutline_parsesQuestionBankBlocks() {
        val markdown = """
            ### Question: Solve 3x + 5 = 20
            - Subject: Mathematics
            - Chapter: Linear Equations
            - Topic: One Variable
            - Marks: 2.0
            - Difficulty: EASY
            - Type: SHORT_ANSWER
            - Answer: x = 5
        """.trimIndent()

        val rows = engine.parseMarkdown(CsvDatasetType.QUESTIONS, markdown)
        assertEquals(1, rows.size)
        val q = rows[0]
        assertEquals("Solve 3x + 5 = 20", q["question_text"])
        assertEquals("Mathematics", q["subject_name"])
        assertEquals("Linear Equations", q["chapter_name"])
        assertEquals("2.0", q["marks"])
        assertEquals("EASY", q["difficulty"])
        assertEquals("x = 5", q["correct_answer"])
    }

    @Test
    fun testParseMarkdownRecallCards_parsesPromptAndAnswer() {
        val markdown = """
            ## Card: What is photosynthesis?
            - Back: Conversion of light energy into chemical energy by plants
            - Subject: Biology
            - Chapter: Plant Physiology
        """.trimIndent()

        val rows = engine.parseMarkdown(CsvDatasetType.RECALL_CARDS, markdown)
        assertEquals(1, rows.size)
        val c = rows[0]
        assertEquals("What is photosynthesis?", c["prompt"])
        assertTrue(c["answer"]!!.contains("Conversion of light energy"))
        assertEquals("Biology", c["subject_name"])
    }

    @Test
    fun testParseMarkdownMistakes_parsesErrorDiagnostics() {
        val markdown = """
            ### Mistake: Forgot sign change during transposition
            - Subject: Mathematics
            - Chapter: Linear Equations
            - Question: Solve 5 - 2x = 9
            - Student Answer: -2x = 4 -> x = 2
            - Correct Answer: x = -2
            - Marks Lost: 2.0
            - Category: CALCULATION_ERROR
        """.trimIndent()

        val rows = engine.parseMarkdown(CsvDatasetType.MISTAKES, markdown)
        assertEquals(1, rows.size)
        val m = rows[0]
        assertEquals("Forgot sign change during transposition", m["question"])
        assertEquals("2.0", m["marks_lost"])
        assertEquals("CALCULATION_ERROR", m["loss_category"])
        assertEquals("x = -2", m["correct_answer"])
    }

    @Test
    fun testExportToMarkdown_formatsSyllabusAndQuestionsProperly() = runBlocking {
        fakeDb.fakeSyllabusDao.insertSubject(SubjectEntity(id = "sub1", name = "Mathematics"))
        fakeDb.fakeSyllabusDao.insertChapter(ChapterEntity(id = "ch1", subjectId = "sub1", name = "Calculus"))
        fakeDb.fakeSyllabusDao.insertTopic(TopicEntity(id = "top1", chapterId = "ch1", name = "Derivatives", masteryState = "MASTERED", examRelevance = "HIGH"))

        fakeDb.fakePaperPilotDao.insertQuestion(
            QuestionBankEntity(
                id = "q1",
                subjectId = "sub1",
                chapterId = "ch1",
                topicId = "top1",
                questionText = "Find the derivative of sin(x)",
                markingScheme = "cos(x)",
                marks = 2f,
                difficulty = "MEDIUM"
            )
        )

        val syllabusMd = engine.exportToMarkdown(CsvDatasetType.SYLLABUS, fakeDb)
        assertTrue(syllabusMd.contains("# Mathematics"))
        assertTrue(syllabusMd.contains("## Chapter: Calculus"))
        assertTrue(syllabusMd.contains("[x] Derivatives [HIGH] [MASTERED]"))

        val questionsMd = engine.exportToMarkdown(CsvDatasetType.QUESTIONS, fakeDb)
        assertTrue(questionsMd.contains("### Question: Find the derivative of sin(x)"))
        assertTrue(questionsMd.contains("- Answer: cos(x)"))
    }
}
