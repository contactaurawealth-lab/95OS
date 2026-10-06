package com.os95.app.core.csv

import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.QuestionBankEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.testutil.FakeDatabaseProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UniversalCsvEngineTest {

    private lateinit var database: FakeDatabaseProvider
    private val engine = UniversalCsvEngine()

    @Before
    fun setUp() {
        database = FakeDatabaseProvider()
    }

    @After
    fun tearDown() {
        database.clearAll()
    }

    @Test
    fun testRfc4180ParserHandlesQuotesCommasAndEscapedQuotes() {
        val parser = CsvParser()
        val csv = "question_id,subject,chapter,question_text,marks\n" +
                  "Q001,Mathematics,Algebra,\"Solve 2x + 5 = 15, where x > 0\",2.0\n" +
                  "Q002,Physics,Mechanics,\"State Newton's \"\"Second\"\" Law of Motion\",5.0\n"

        val rows = parser.parse(csv)
        assertEquals(3, rows.size)

        val row1 = rows[1].fields
        assertEquals("Q001", row1[0])
        assertEquals("Solve 2x + 5 = 15, where x > 0", row1[3])

        val row2 = rows[2].fields
        assertEquals("Q002", row2[0])
        assertEquals("State Newton's \"Second\" Law of Motion", row2[3])
    }

    @Test
    fun testRfc4180ParserHandlesMultilineQuestions() {
        val parser = CsvParser()
        val csv = "question_id,question_text,marks\n" +
                  "Q001,\"Line 1 of question.\nLine 2 of question.\nLine 3 of question.\",3.0\n"

        val rows = parser.parse(csv)
        assertEquals(2, rows.size)
        assertEquals(
            "Line 1 of question.\nLine 2 of question.\nLine 3 of question.",
            rows[1].fields[1]
        )
    }

    @Test
    fun testRfc4180ParserHandlesUnicodeAndMathematicalSymbols() {
        val parser = CsvParser()
        val csv = "question_id,question_text,marks\n" +
                  "Q001,\"Find value of θ when sin(θ) = √3/2, where 0 ≤ θ ≤ 2π\",2.0\n"

        val rows = parser.parse(csv)
        assertEquals(2, rows.size)
        assertTrue(rows[1].fields[1].contains("θ"))
        assertTrue(rows[1].fields[1].contains("√3/2"))
        assertTrue(rows[1].fields[1].contains("2π"))
    }

    @Test
    fun testValidatorRejectsMissingRequiredColumn() {
        val validator = CsvValidator()
        // Missing "question_text"
        val headers = listOf("subject", "chapter", "marks")
        val missing = validator.checkMissingColumns(CsvDatasetType.QUESTIONS, headers)

        assertEquals(1, missing.size)
        assertEquals("question_text", missing[0])
    }

    @Test
    fun testValidatorRejectsNegativeOrZeroMarks() {
        val validator = CsvValidator()
        val rowZero = mapOf(
            "subject" to "Math",
            "chapter" to "Algebra",
            "question_text" to "Sample question",
            "marks" to "0.0"
        )
        val errorsZero = validator.validateRowData(CsvDatasetType.QUESTIONS, 2, rowZero, "")
        assertTrue(errorsZero.any { it.message.contains("Marks must be greater than zero") })

        val rowNegative = mapOf(
            "subject" to "Math",
            "chapter" to "Algebra",
            "question_text" to "Sample question",
            "marks" to "-5.0"
        )
        val errorsNegative = validator.validateRowData(CsvDatasetType.QUESTIONS, 3, rowNegative, "")
        assertTrue(errorsNegative.any { it.message.contains("Marks must be greater than zero") })
    }

    @Test
    fun testValidatorRejectsInvalidDifficulty() {
        val validator = CsvValidator()
        val row = mapOf(
            "subject" to "Math",
            "chapter" to "Algebra",
            "question_text" to "Sample question",
            "marks" to "2.0",
            "difficulty" to "Impossible"
        )
        val errors = validator.validateRowData(CsvDatasetType.QUESTIONS, 2, row, "")
        assertTrue(errors.any { it.message.contains("Invalid difficulty: \"Impossible\"") })
    }

    @Test
    fun testDuplicateDetectionExactIdAndContent() = runBlocking {
        // Pre-insert a subject, chapter, and question
        val sub = SubjectEntity(id = "sub-1", name = "Mathematics")
        val chp = ChapterEntity(id = "chp-1", subjectId = "sub-1", name = "Algebra")
        val q = QuestionBankEntity(
            id = "Q001",
            subjectId = "sub-1",
            chapterId = "chp-1",
            topicId = null,
            questionText = "Solve 2x = 10",
            marks = 2.0f
        )
        database.syllabusDao().insertSubject(sub)
        database.syllabusDao().insertChapter(chp)
        database.paperPilotDao().insertQuestion(q)

        val csvWithExactId = "question_id,subject,chapter,question_text,marks\n" +
                              "Q001,Mathematics,Algebra,\"Solve 2x = 10\",2.0\n"

        val preview = engine.preview(
            datasetType = CsvDatasetType.QUESTIONS,
            content = csvWithExactId,
            database = database,
            duplicateStrategy = DuplicateResolutionStrategy.SKIP
        )

        assertEquals(1, preview.duplicateCount)
        assertTrue(preview.validRows[0].isDuplicate)
        assertTrue(preview.validRows[0].duplicateReason!!.contains("Exact ID duplicate"))
    }

    @Test
    fun testRelationshipModeFailOnMissingBlocksImport() = runBlocking {
        val csv = "question_id,subject,chapter,topic,question_text,marks\n" +
                  "Q014,Mathematics,Algebra,Linear Equations,\"Solve 2x + 5 = 15\",2.0\n"

        val preview = engine.preview(
            datasetType = CsvDatasetType.QUESTIONS,
            content = csv,
            database = database,
            missingRelMode = MissingRelationshipMode.FAIL_ON_MISSING
        )

        assertEquals(1, preview.errorCount)
        assertTrue(preview.errors.first().message.contains("does not exist"))
    }

    @Test
    fun testRelationshipModeCreateMissingAutoCreatesHierarchy() = runBlocking {
        val csv = "question_id,subject,chapter,topic,question_text,marks\n" +
                  "Q014,Mathematics,Algebra,Linear Equations,\"Solve 2x + 5 = 15\",2.0\n"

        val preview = engine.preview(
            datasetType = CsvDatasetType.QUESTIONS,
            content = csv,
            database = database,
            missingRelMode = MissingRelationshipMode.CREATE_MISSING
        )

        assertEquals(0, preview.errorCount)
        assertEquals(1, preview.newSubjectsToCreate.size)
        assertEquals("Mathematics", preview.newSubjectsToCreate.first())
        assertEquals(1, preview.newChaptersToCreate.size)
        assertEquals(1, preview.newTopicsToCreate.size)

        // Execute import
        val summary = engine.executeImport(preview, database)
        assertEquals(1, summary.totalImported)
        assertEquals(1, summary.subjectsCreated)
        assertEquals(1, summary.chaptersCreated)
        assertEquals(1, summary.topicsCreated)

        // Verify entities in database
        val subjects = database.syllabusDao().getAllSubjects().first()
        assertEquals("Mathematics", subjects.first().name)

        val questions = database.paperPilotDao().getAllQuestions().first()
        assertEquals(1, questions.size)
        assertEquals("Solve 2x + 5 = 15", questions.first().questionText)
    }

    @Test
    fun testRoundTripExportAndImportPreservesAllData() = runBlocking {
        // 1. Create data
        val sub = SubjectEntity(id = "sub-rt", name = "Physics")
        val chp = ChapterEntity(id = "chp-rt", subjectId = "sub-rt", name = "Mechanics")
        val top = TopicEntity(id = "top-rt", chapterId = "chp-rt", name = "Newton Laws")
        val q1 = QuestionBankEntity(
            id = "Q-RT1",
            subjectId = "sub-rt",
            chapterId = "chp-rt",
            topicId = "top-rt",
            questionText = "State Newton's first law of motion.",
            markingScheme = "Statement of inertia (2m)",
            marks = 2.0f,
            difficulty = "EASY",
            questionType = "SHORT_ANSWER",
            source = "NCERT"
        )
        database.syllabusDao().insertSubject(sub)
        database.syllabusDao().insertChapter(chp)
        database.syllabusDao().insertTopic(top)
        database.paperPilotDao().insertQuestion(q1)

        // 2. Export CSV
        val exportedCsv = engine.export(CsvDatasetType.QUESTIONS, database)
        assertTrue(exportedCsv.contains("State Newton's first law of motion."))
        assertTrue(exportedCsv.contains("Physics"))
        assertTrue(exportedCsv.contains("Mechanics"))

        // 3. Clear Questions
        database.paperPilotDao().deleteQuestion(q1)
        assertEquals(0, database.paperPilotDao().getAllQuestions().first().size)

        // 4. Re-import CSV
        val preview = engine.preview(
            datasetType = CsvDatasetType.QUESTIONS,
            content = exportedCsv,
            database = database,
            missingRelMode = MissingRelationshipMode.CREATE_MISSING
        )
        val summary = engine.executeImport(preview, database)
        assertEquals(1, summary.totalImported)

        // 5. Verify restored data matches
        val restoredQuestions = database.paperPilotDao().getAllQuestions().first()
        assertEquals(1, restoredQuestions.size)
        val rq = restoredQuestions.first()
        assertEquals("Q-RT1", rq.id)
        assertEquals("State Newton's first law of motion.", rq.questionText)
        assertEquals(2.0f, rq.marks, 0.001f)
        assertEquals("EASY", rq.difficulty)
        assertEquals("SHORT_ANSWER", rq.questionType)
        assertEquals("NCERT", rq.source)
    }

    @Test
    fun testLargeDatasetPerformance1000Questions() = runBlocking {
        val sb = StringBuilder()
        sb.append("question_id,subject,chapter,topic,question_text,marks,difficulty,question_type\n")
        for (i in 1..1000) {
            sb.append("Q$i,Biology,Cell Biology,Mitosis,\"Describe step $i of cell division under high magnification.\",2.0,MEDIUM,SHORT_ANSWER\n")
        }

        val startTime = System.currentTimeMillis()
        val preview = engine.preview(
            datasetType = CsvDatasetType.QUESTIONS,
            content = sb.toString(),
            database = database,
            missingRelMode = MissingRelationshipMode.CREATE_MISSING
        )
        val summary = engine.executeImport(preview, database)
        val durationMs = System.currentTimeMillis() - startTime

        assertEquals(1000, summary.totalImported)
        val questionsInDb = database.paperPilotDao().getAllQuestionsSync()
        assertEquals(1000, questionsInDb.size)
        assertTrue("Import of 1,000 questions must be fast (< 5000ms), took: ${durationMs}ms", durationMs < 5000)
    }
}
