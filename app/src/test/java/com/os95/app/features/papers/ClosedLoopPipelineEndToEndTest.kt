package com.os95.app.features.papers

import com.os95.app.core.csv.CsvDatasetType
import com.os95.app.core.csv.MissingRelationshipMode
import com.os95.app.core.csv.UniversalCsvEngine
import com.os95.app.data.repository.OfflinePaperRepository
import com.os95.app.domain.engine.PaperValidator
import com.os95.app.domain.model.GeneratedPaper
import com.os95.app.domain.model.PaperBlueprintRequest
import com.os95.app.domain.model.PaperDifficultyMode
import com.os95.app.domain.model.PaperGenerationResult
import com.os95.app.domain.model.QuestionResultInput
import com.os95.app.domain.pdf.PrintableExamPaperFormatter
import com.os95.app.testutil.FakeDatabaseProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ClosedLoopPipelineEndToEndTest {

    private lateinit var database: FakeDatabaseProvider
    private lateinit var paperRepository: OfflinePaperRepository
    private val csvEngine = UniversalCsvEngine()
    private val paperValidator = PaperValidator()
    private val formatter = PrintableExamPaperFormatter()

    @Before
    fun setUp() {
        database = FakeDatabaseProvider()

        paperRepository = OfflinePaperRepository(
            dao = database.paperPilotDao(),
            syllabusDao = database.syllabusDao(),
            mistakeDao = database.mistakeDao()
        )
    }

    @After
    fun tearDown() {
        database.clearAll()
    }

    @Test
    fun testCompleteClosedLoopOfflinePipeline() = runBlocking {
        // Step 1: Import Syllabus (Mathematics -> Algebra & Geometry -> Topics)
        val syllabusCsv = "subject,chapter,topic,mastery,relevance\n" +
                          "Mathematics,Algebra,Linear Equations,LEARNING,HIGH\n" +
                          "Mathematics,Algebra,Quadratic Equations,NOT_STARTED,HIGH\n" +
                          "Mathematics,Geometry,Triangles,MASTERED,MEDIUM\n" +
                          "Mathematics,Geometry,Circles,NOT_STARTED,HIGH\n"

        val syllabusPreview = csvEngine.preview(
            datasetType = CsvDatasetType.SYLLABUS,
            content = syllabusCsv,
            database = database,
            missingRelMode = MissingRelationshipMode.CREATE_MISSING
        )
        val syllabusSummary = csvEngine.executeImport(syllabusPreview, database)
        assertEquals(4, syllabusSummary.totalImported)

        val subjects = database.syllabusDao().getAllSubjects().first()
        assertEquals(1, subjects.size)
        val mathSubject = subjects.first()
        assertEquals("Mathematics", mathSubject.name)

        val chapters = database.syllabusDao().getChaptersForSubjectSync(mathSubject.id)
        assertEquals(2, chapters.size)
        val algebraChapter = chapters.first { it.name == "Algebra" }
        val geometryChapter = chapters.first { it.name == "Geometry" }

        // Step 2: Import 50 Questions via CSV
        val qCsvBuilder = StringBuilder()
        qCsvBuilder.append("question_id,subject,chapter,topic,question_text,question_type,difficulty,marks,answer\n")

        // 10 1m MCQs
        for (i in 1..10) {
            val chp = if (i <= 5) "Algebra" else "Geometry"
            val top = if (i <= 5) "Linear Equations" else "Triangles"
            qCsvBuilder.append("Q-MCQ-$i,Mathematics,$chp,$top,\"Multiple choice question #$i\",MCQ,EASY,1.0,\"Option A\"\n")
        }
        // 15 2m Short Answer
        for (i in 1..15) {
            val chp = if (i <= 8) "Algebra" else "Geometry"
            val top = if (i <= 8) "Quadratic Equations" else "Circles"
            qCsvBuilder.append("Q-SA-$i,Mathematics,$chp,$top,\"Short answer problem #$i\",SHORT_ANSWER,MEDIUM,2.0,\"Solution step #$i\"\n")
        }
        // 6 5m Long Answer
        for (i in 1..6) {
            val chp = if (i <= 3) "Algebra" else "Geometry"
            val top = if (i <= 3) "Linear Equations" else "Triangles"
            qCsvBuilder.append("Q-LA-$i,Mathematics,$chp,$top,\"Long answer analytical question #$i\",LONG_ANSWER,HARD,5.0,\"Proof step #$i\"\n")
        }

        val qPreview = csvEngine.preview(
            datasetType = CsvDatasetType.QUESTIONS,
            content = qCsvBuilder.toString(),
            database = database,
            missingRelMode = MissingRelationshipMode.CREATE_MISSING
        )
        val qSummary = csvEngine.executeImport(qPreview, database)
        assertEquals(31, qSummary.totalImported)
        assertEquals(31, database.paperPilotDao().getAllQuestionsSync().size)

        // Step 3: Open PaperPilot & Generate Paper #1 (50 Marks, 60 Minutes)
        val blueprintRequest = PaperBlueprintRequest(
            title = "Mid-Term Exam — Mathematics",
            subjectId = mathSubject.id,
            chapterIds = listOf(algebraChapter.id, geometryChapter.id),
            totalMarks = 50.0f,
            durationMinutes = 60,
            difficultyMode = PaperDifficultyMode.MIXED
        )

        val genResult = paperRepository.generatePaper(blueprintRequest)
        assertTrue("Paper generation must succeed", genResult is PaperGenerationResult.Success)

        val generatedPaper1 = (genResult as PaperGenerationResult.Success).generatedPaper

        // Step 4: Validate Paper #1
        val validation = paperValidator.validatePaper(generatedPaper1.paper, generatedPaper1.questions)
        assertTrue("Paper structure and marks must be 100% valid", validation.isValid)
        assertEquals(50.0f, generatedPaper1.questions.sumOf { it.marks.toDouble() }.toFloat(), 0.001f)

        // Step 5: Finalize Paper #1 (Creates Snapshot in Room DB)
        val savedPaper1 = paperRepository.finalizeAndSavePaper(generatedPaper1)
        assertEquals("READY", savedPaper1.status)

        val snapshotQuestions = database.paperPilotDao().getPaperQuestionsSync(savedPaper1.id)
        assertEquals(generatedPaper1.questions.size, snapshotQuestions.size)

        // Step 6: Format Printable PDF / Exam Paper
        val formattedPaper = formatter.formatFromSnapshots(savedPaper1, "Mathematics", snapshotQuestions)
        val printableText = formatter.renderToPrintableText(formattedPaper)
        assertTrue(printableText.contains("MID-TERM EXAM — MATHEMATICS"))
        assertTrue(printableText.contains("Maximum Marks: 50 Marks"))
        assertTrue(printableText.contains("Time Allowed: 60 Minutes"))

        // Step 7: Simulate Physical Exam Mode & Result Recording (43/50 = 86%)
        // Student writes physically, gets evaluated, lost 7 marks (5m on Long Answer Algebra, 2m on Short Answer Algebra)
        val questionResults = mutableListOf<QuestionResultInput>()
        var recorded5mLoss = false
        var recorded2mLoss = false

        for (pq in snapshotQuestions) {
            if (pq.snapshotChapterId == algebraChapter.id && pq.snapshotMarks == 5.0f && !recorded5mLoss) {
                questionResults.add(
                    QuestionResultInput(
                        questionId = pq.questionId,
                        questionText = pq.snapshotQuestionText,
                        chapterId = pq.snapshotChapterId,
                        topicId = pq.snapshotTopicId,
                        marksAllocated = 5.0f,
                        marksLost = 5.0f,
                        lossCategory = "CONCEPT_ERROR",
                        notes = "Forgot matrix inversion rule"
                    )
                )
                recorded5mLoss = true
            } else if (pq.snapshotChapterId == algebraChapter.id && pq.snapshotMarks == 2.0f && !recorded2mLoss) {
                questionResults.add(
                    QuestionResultInput(
                        questionId = pq.questionId,
                        questionText = pq.snapshotQuestionText,
                        chapterId = pq.snapshotChapterId,
                        topicId = pq.snapshotTopicId,
                        marksAllocated = 2.0f,
                        marksLost = 2.0f,
                        lossCategory = "CALCULATION_ERROR",
                        notes = "Arithmetic slip in final step"
                    )
                )
                recorded2mLoss = true
            } else {
                questionResults.add(
                    QuestionResultInput(
                        questionId = pq.questionId,
                        questionText = pq.snapshotQuestionText,
                        chapterId = pq.snapshotChapterId,
                        topicId = pq.snapshotTopicId,
                        marksAllocated = pq.snapshotMarks,
                        marksLost = 0.0f
                    )
                )
            }
        }

        val resultRecord = paperRepository.recordDetailedResult(
            paperId = savedPaper1.id,
            marksObtained = 43.0f,
            totalMarks = 50.0f,
            timeTakenMinutes = 55,
            questionResults = questionResults
        )

        // Step 8: Verify Result & Metrics
        assertEquals(43.0f, resultRecord.marksObtained, 0.001f)
        val percentage = (resultRecord.marksObtained / resultRecord.totalMarks) * 100f
        assertEquals(86.0f, percentage, 0.001f)

        // Step 9: Verify Mistake Bank entries created automatically
        val activeMistakes = database.mistakeDao().getAllMistakes().first()
        assertTrue("Mistake bank must contain entries for lost marks", activeMistakes.size >= 2)
        assertTrue(activeMistakes.any { it.lossCategory == "CONCEPT_ERROR" && it.marksLost == 5.0f })
        assertTrue(activeMistakes.any { it.lossCategory == "CALCULATION_ERROR" && it.marksLost == 2.0f })

        // Step 10: Verify Topic weakness score was updated
        val linearEquationsTopic = database.syllabusDao().getAllTopicsSync().first { it.name == "Linear Equations" }
        assertTrue("Linear Equations topic weakness must increase", linearEquationsTopic.weaknessScore > 0.5f)

        // Step 11: Generate Paper #2 — Verify Adaptive Weighting uses Paper #1 history!
        val blueprint2 = PaperBlueprintRequest(
            title = "Follow-up Diagnostic Paper #2",
            subjectId = mathSubject.id,
            chapterIds = listOf(algebraChapter.id, geometryChapter.id),
            totalMarks = 40.0f,
            durationMinutes = 45,
            adaptiveWeakTopicWeighting = true
        )

        val genResult2 = paperRepository.generatePaper(blueprint2)
        assertTrue("Paper #2 generation must succeed", genResult2 is PaperGenerationResult.Success)

        val generatedPaper2 = (genResult2 as PaperGenerationResult.Success).generatedPaper
        assertNotNull(generatedPaper2.adaptiveRationale)
        assertTrue(
            "Adaptive rationale must reflect recorded mark loss in Algebra",
            generatedPaper2.adaptiveRationale!!.contains("Algebra")
        )
    }
}
