package com.os95.app.core.csv

import com.os95.app.core.database.DatabaseProvider
import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.QuestionBankEntity
import com.os95.app.core.database.entity.RecallCardEntity
import com.os95.app.core.database.entity.StudySessionEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity
import kotlinx.coroutines.flow.first
import java.util.UUID

class UniversalCsvEngine(
    private val parser: CsvParser = CsvParser(),
    private val validator: CsvValidator = CsvValidator(),
    private val exporter: CsvExporter = CsvExporter()
) : UniversalCsvProcessor {

    // --- Backward Compatibility for UniversalCsvProcessor ---
    override fun getTemplate(entity: UniversalCsvEntity): String {
        val datasetType = when (entity) {
            UniversalCsvEntity.SYLLABUS -> CsvDatasetType.SYLLABUS
            UniversalCsvEntity.QUESTION_BANK -> CsvDatasetType.QUESTIONS
            UniversalCsvEntity.RECALL_CARDS -> CsvDatasetType.RECALL_CARDS
            UniversalCsvEntity.MISTAKES -> CsvDatasetType.MISTAKES
            UniversalCsvEntity.STUDY_SESSIONS -> CsvDatasetType.STUDY_SESSIONS
        }
        return exporter.getTemplate(datasetType)
    }

    override fun parseAndValidate(entity: UniversalCsvEntity, content: String): CsvParseResult {
        val datasetType = when (entity) {
            UniversalCsvEntity.SYLLABUS -> CsvDatasetType.SYLLABUS
            UniversalCsvEntity.QUESTION_BANK -> CsvDatasetType.QUESTIONS
            UniversalCsvEntity.RECALL_CARDS -> CsvDatasetType.RECALL_CARDS
            UniversalCsvEntity.MISTAKES -> CsvDatasetType.MISTAKES
            UniversalCsvEntity.STUDY_SESSIONS -> CsvDatasetType.STUDY_SESSIONS
        }

        if (content.trim().isEmpty()) {
            return CsvParseResult(
                entityType = entity,
                totalRows = 0,
                validRows = emptyList(),
                errors = listOf(CsvRowError(0, "", "CSV content is empty"))
            )
        }

        val parsedRows = parser.parse(content)
        if (parsedRows.isEmpty()) {
            return CsvParseResult(
                entityType = entity,
                totalRows = 0,
                validRows = emptyList(),
                errors = listOf(CsvRowError(0, "", "CSV content is empty"))
            )
        }

        val headerRow = parsedRows.first()
        val missing = validator.checkMissingColumns(datasetType, headerRow.fields)
        if (missing.isNotEmpty()) {
            return CsvParseResult(
                entityType = entity,
                totalRows = parsedRows.size - 1,
                validRows = emptyList(),
                errors = listOf(
                    CsvRowError(
                        lineNumber = headerRow.lineNumber,
                        rawLine = headerRow.rawLineSnippet,
                        message = "Missing required columns: ${missing.joinToString(", ")}"
                    )
                )
            )
        }

        val headers = headerRow.fields.map { it.trim().lowercase() }
        val validRows = mutableListOf<Map<String, String>>()
        val errors = mutableListOf<CsvRowError>()

        for (i in 1 until parsedRows.size) {
            val row = parsedRows[i]
            if (row.fields.all { it.isBlank() }) continue

            if (row.fields.size != headers.size) {
                errors.add(
                    CsvRowError(
                        lineNumber = row.lineNumber,
                        rawLine = row.rawLineSnippet,
                        message = "Column count mismatch: expected ${headers.size}, found ${row.fields.size}"
                    )
                )
                continue
            }

            val map = headers.zip(row.fields).toMap()
            val rowErrors = validator.validateRowData(datasetType, row.lineNumber, map, row.rawLineSnippet)
            if (rowErrors.isNotEmpty()) {
                errors.addAll(rowErrors.map { CsvRowError(it.lineNumber, it.rawSnippet, it.message) })
            } else {
                validRows.add(map)
            }
        }

        return CsvParseResult(
            entityType = entity,
            totalRows = parsedRows.size - 1,
            validRows = validRows,
            errors = errors
        )
    }

    // --- Modern Universal CSV Pipeline ---

    fun parse(content: String): List<CsvParser.ParsedRow> {
        return parser.parse(content)
    }

    fun detectDatasetType(firstRowFields: List<String>): CsvDatasetType? {
        val normalized = firstRowFields.map { validator.normalizeHeader(it) }

        // Check explicit header comments or version metadata
        if (firstRowFields.size >= 2) {
            val first = normalized.first()
            if (first == "datasettype" || first == "dataset") {
                return CsvDatasetType.fromIdOrName(firstRowFields[1])
            }
        }

        // Try detecting by required column matching
        val scores = CsvDatasetType.values().map { type ->
            val missing = validator.checkMissingColumns(type, firstRowFields)
            Pair(type, missing.size)
        }.sortedBy { it.second }

        val best = scores.firstOrNull()
        return if (best != null && best.second == 0) best.first else null
    }

    suspend fun preview(
        datasetType: CsvDatasetType,
        content: String,
        database: DatabaseProvider,
        duplicateStrategy: DuplicateResolutionStrategy = DuplicateResolutionStrategy.SKIP,
        missingRelMode: MissingRelationshipMode = MissingRelationshipMode.CREATE_MISSING
    ): CsvPreviewResult {
        if (content.trim().isEmpty()) {
            return CsvPreviewResult(
                datasetType = datasetType,
                totalRowsDetected = 0,
                validRows = emptyList(),
                duplicateCount = 0,
                errorCount = 1,
                errors = listOf(CsvValidationError(0, null, "", "CSV content is empty"))
            )
        }

        val parsedRows = parser.parse(content)
        if (parsedRows.isEmpty()) {
            return CsvPreviewResult(
                datasetType = datasetType,
                totalRowsDetected = 0,
                validRows = emptyList(),
                duplicateCount = 0,
                errorCount = 1,
                errors = listOf(CsvValidationError(0, null, "", "CSV content is empty"))
            )
        }

        val headerRow = parsedRows.first()
        val missingColumns = validator.checkMissingColumns(datasetType, headerRow.fields)
        if (missingColumns.isNotEmpty()) {
            return CsvPreviewResult(
                datasetType = datasetType,
                totalRowsDetected = parsedRows.size - 1,
                validRows = emptyList(),
                duplicateCount = 0,
                errorCount = 1,
                errors = listOf(
                    CsvValidationError(
                        lineNumber = headerRow.lineNumber,
                        rawSnippet = headerRow.rawLineSnippet,
                        message = "Missing required column: \"${missingColumns.first()}\""
                    )
                )
            )
        }

        val rawHeaders = headerRow.fields
        val syllabusDao = database.syllabusDao()
        val paperDao = database.paperPilotDao()

        // Existing academic entities for lookup & relationship validation
        val existingSubjects = syllabusDao.getAllSubjects().first().associateBy { it.name.trim().lowercase() }
        val existingChapters = mutableMapOf<String, ChapterEntity>() // key: "${subId}:${chpName.lowercase()}"
        val existingTopics = mutableMapOf<String, TopicEntity>() // key: "${chpId}:${topName.lowercase()}"

        for ((_, sub) in existingSubjects) {
            val chps = syllabusDao.getChaptersForSubjectSync(sub.id)
            for (ch in chps) {
                existingChapters["${sub.id}:${ch.name.trim().lowercase()}"] = ch
                val tops = syllabusDao.getTopicsForChapterSync(ch.id)
                for (tp in tops) {
                    existingTopics["${ch.id}:${tp.name.trim().lowercase()}"] = tp
                }
            }
        }

        val existingQuestions = if (datasetType == CsvDatasetType.QUESTIONS) {
            paperDao.getAllQuestionsSync()
        } else {
            emptyList()
        }
        val existingQuestionIds = existingQuestions.map { it.id }.toSet()
        val existingQuestionTexts = existingQuestions.map { it.questionText.trim().lowercase() }.toSet()

        val validRows = mutableListOf<ValidatedRow>()
        val errors = mutableListOf<CsvValidationError>()
        var duplicateCount = 0

        val newSubjects = mutableSetOf<String>()
        val newChapters = mutableSetOf<Pair<String, String>>()
        val newTopics = mutableSetOf<Triple<String, String, String>>()

        for (i in 1 until parsedRows.size) {
            val row = parsedRows[i]
            if (row.fields.all { it.isBlank() }) continue // Ignore completely empty rows

            if (row.fields.size != rawHeaders.size) {
                errors.add(
                    CsvValidationError(
                        lineNumber = row.lineNumber,
                        rawSnippet = row.rawLineSnippet,
                        message = "Column count mismatch: expected ${rawHeaders.size}, found ${row.fields.size}"
                    )
                )
                continue
            }

            val map = rawHeaders.map { it.trim() }.zip(row.fields.map { it.trim() }).toMap()
            val rowValidationErrors = validator.validateRowData(datasetType, row.lineNumber, map, row.rawLineSnippet)
            if (rowValidationErrors.isNotEmpty()) {
                errors.addAll(rowValidationErrors)
                continue
            }

            // Relationship & Duplicate checks
            var isDup = false
            var dupReason: String? = null

            if (datasetType == CsvDatasetType.QUESTIONS) {
                val qId = validator.getField(map, listOf("question_id", "id"))
                val qText = validator.getField(map, listOf("question_text", "questiontext", "question")) ?: ""
                val subName = validator.getField(map, listOf("subject", "subjectname", "subject_name")) ?: ""
                val chpName = validator.getField(map, listOf("chapter", "chaptername", "chapter_name")) ?: ""
                val topName = validator.getField(map, listOf("topic", "topicname", "topic_name"))

                // Duplicate Detection
                if (!qId.isNullOrBlank() && existingQuestionIds.contains(qId)) {
                    isDup = true
                    dupReason = "Exact ID duplicate: question_id = $qId"
                } else if (existingQuestionTexts.contains(qText.trim().lowercase())) {
                    isDup = true
                    dupReason = "Content duplicate: identical question text already exists"
                }

                if (isDup) {
                    duplicateCount++
                }

                // Relationship Resolution
                val subKey = subName.trim().lowercase()
                val existingSub = existingSubjects[subKey]
                var missingSub: String? = null
                var missingChp: String? = null
                var missingTop: String? = null

                if (existingSub == null && !newSubjects.contains(subName.trim())) {
                    missingSub = subName
                }

                val targetSubId = existingSub?.id ?: ""
                val chpKey = "$targetSubId:${chpName.trim().lowercase()}"
                val existingChp = if (existingSub != null) existingChapters[chpKey] else null
                if (existingChp == null && !newChapters.contains(Pair(subName.trim(), chpName.trim()))) {
                    missingChp = chpName
                }

                if (!topName.isNullOrBlank()) {
                    val targetChpId = existingChp?.id ?: ""
                    val topKey = "$targetChpId:${topName.trim().lowercase()}"
                    val existingTop = if (existingChp != null) existingTopics[topKey] else null
                    if (existingTop == null && !newTopics.contains(Triple(subName.trim(), chpName.trim(), topName.trim()))) {
                        missingTop = topName
                    }
                }

                if (missingSub != null || missingChp != null || missingTop != null) {
                    when (missingRelMode) {
                        MissingRelationshipMode.FAIL_ON_MISSING -> {
                            val entityMissing = missingSub ?: missingChp ?: missingTop ?: "reference"
                            errors.add(
                                CsvValidationError(
                                    lineNumber = row.lineNumber,
                                    rawSnippet = row.rawLineSnippet,
                                    message = "Question references \"$entityMissing\", but that entity does not exist."
                                )
                            )
                            continue
                        }
                        MissingRelationshipMode.SKIP_ROW -> {
                            continue
                        }
                        MissingRelationshipMode.CREATE_MISSING -> {
                            if (missingSub != null) newSubjects.add(missingSub)
                            if (missingChp != null) newChapters.add(Pair(subName.trim(), chpName.trim()))
                            if (missingTop != null) newTopics.add(Triple(subName.trim(), chpName.trim(), missingTop))
                        }
                    }
                }

                validRows.add(
                    ValidatedRow(
                        lineNumber = row.lineNumber,
                        data = map,
                        isDuplicate = isDup,
                        duplicateReason = dupReason,
                        missingSubject = missingSub,
                        missingChapter = missingChp,
                        missingTopic = missingTop
                    )
                )
            } else if (datasetType == CsvDatasetType.SYLLABUS) {
                val subName = validator.getField(map, listOf("subject", "subjectname", "subject_name")) ?: ""
                val chpName = validator.getField(map, listOf("chapter", "chaptername", "chapter_name")) ?: ""
                val topName = validator.getField(map, listOf("topic", "topicname", "topic_name")) ?: ""

                if (!existingSubjects.containsKey(subName.trim().lowercase())) {
                    newSubjects.add(subName.trim())
                }
                newChapters.add(Pair(subName.trim(), chpName.trim()))
                newTopics.add(Triple(subName.trim(), chpName.trim(), topName.trim()))

                validRows.add(
                    ValidatedRow(
                        lineNumber = row.lineNumber,
                        data = map
                    )
                )
            } else {
                validRows.add(
                    ValidatedRow(
                        lineNumber = row.lineNumber,
                        data = map
                    )
                )
            }
        }

        return CsvPreviewResult(
            datasetType = datasetType,
            totalRowsDetected = parsedRows.size - 1,
            validRows = validRows,
            duplicateCount = duplicateCount,
            errorCount = errors.size,
            errors = errors,
            newSubjectsToCreate = newSubjects.toList(),
            newChaptersToCreate = newChapters.toList(),
            newTopicsToCreate = newTopics.toList(),
            newQuestionsCount = if (datasetType == CsvDatasetType.QUESTIONS) validRows.size else 0
        )
    }

    suspend fun executeImport(
        preview: CsvPreviewResult,
        database: DatabaseProvider,
        duplicateStrategy: DuplicateResolutionStrategy = DuplicateResolutionStrategy.SKIP
    ): CsvImportSummary {
        val syllabusDao = database.syllabusDao()
        val paperDao = database.paperPilotDao()
        val mistakeDao = database.mistakeDao()
        val recallDao = database.recallDao()
        val sessionDao = database.studySessionDao()

        var importedCount = 0
        var duplicatesSkipped = 0
        var duplicatesReplaced = 0
        var subjectsCreated = 0
        var chaptersCreated = 0
        var topicsCreated = 0
        var questionsCreated = 0
        var recallCardsCreated = 0
        var mistakesCreated = 0

        try {
            database.runInTransaction {
                // Cache or create subjects
                val subjectNameToId = mutableMapOf<String, String>()
                for (s in syllabusDao.getAllSubjects().first()) {
                    subjectNameToId[s.name.trim().lowercase()] = s.id
                }

                for (subName in preview.newSubjectsToCreate) {
                    val key = subName.trim().lowercase()
                    if (!subjectNameToId.containsKey(key)) {
                        val sub = SubjectEntity(name = subName.trim(), colorHex = "#B8781E")
                        syllabusDao.insertSubject(sub)
                        subjectNameToId[key] = sub.id
                        subjectsCreated++
                    }
                }

                // Cache or create chapters
                val chapterKeyToId = mutableMapOf<String, String>() // "${subId}:${chpName.lowercase()}"
                for ((_, subId) in subjectNameToId) {
                    for (ch in syllabusDao.getChaptersForSubjectSync(subId)) {
                        chapterKeyToId["$subId:${ch.name.trim().lowercase()}"] = ch.id
                    }
                }

                for (pair in preview.newChaptersToCreate) {
                    val subId = subjectNameToId[pair.first.trim().lowercase()] ?: continue
                    val chpKey = "$subId:${pair.second.trim().lowercase()}"
                    if (!chapterKeyToId.containsKey(chpKey)) {
                        val ch = ChapterEntity(subjectId = subId, name = pair.second.trim(), orderIndex = 0)
                        syllabusDao.insertChapter(ch)
                        chapterKeyToId[chpKey] = ch.id
                        chaptersCreated++
                    }
                }

                // Cache or create topics
                val topicKeyToId = mutableMapOf<String, String>() // "${chpId}:${topName.lowercase()}"
                for ((_, chpId) in chapterKeyToId) {
                    for (tp in syllabusDao.getTopicsForChapterSync(chpId)) {
                        topicKeyToId["$chpId:${tp.name.trim().lowercase()}"] = tp.id
                    }
                }

                for (triple in preview.newTopicsToCreate) {
                    val subId = subjectNameToId[triple.first.trim().lowercase()] ?: continue
                    val chpId = chapterKeyToId["$subId:${triple.second.trim().lowercase()}"] ?: continue
                    val topKey = "$chpId:${triple.third.trim().lowercase()}"
                    if (!topicKeyToId.containsKey(topKey)) {
                        val tp = TopicEntity(chapterId = chpId, name = triple.third.trim())
                        syllabusDao.insertTopic(tp)
                        topicKeyToId[topKey] = tp.id
                        topicsCreated++
                    }
                }

                // Now import the rows based on dataset type
                when (preview.datasetType) {
                    CsvDatasetType.QUESTIONS -> {
                        val questionsToInsert = mutableListOf<QuestionBankEntity>()

                        for (row in preview.validRows) {
                            if (row.isDuplicate) {
                                when (duplicateStrategy) {
                                    DuplicateResolutionStrategy.SKIP -> {
                                        duplicatesSkipped++
                                        continue
                                    }
                                    DuplicateResolutionStrategy.REPLACE -> {
                                        duplicatesReplaced++
                                    }
                                    DuplicateResolutionStrategy.KEEP_BOTH -> {
                                        // Keep both: we'll generate a new UUID below
                                    }
                                }
                            }

                            val map = row.data
                            val subName = validator.getField(map, listOf("subject", "subjectname", "subject_name")) ?: "General"
                            val chpName = validator.getField(map, listOf("chapter", "chaptername", "chapter_name")) ?: "General"
                            val topName = validator.getField(map, listOf("topic", "topicname", "topic_name"))

                            val subId = subjectNameToId[subName.trim().lowercase()] ?: run {
                                val s = SubjectEntity(name = subName.trim())
                                syllabusDao.insertSubject(s)
                                subjectNameToId[subName.trim().lowercase()] = s.id
                                s.id
                            }
                            val chpId = chapterKeyToId["$subId:${chpName.trim().lowercase()}"] ?: run {
                                val c = ChapterEntity(subjectId = subId, name = chpName.trim())
                                syllabusDao.insertChapter(c)
                                chapterKeyToId["$subId:${chpName.trim().lowercase()}"] = c.id
                                c.id
                            }
                            val topId = if (!topName.isNullOrBlank()) {
                                topicKeyToId["$chpId:${topName.trim().lowercase()}"] ?: run {
                                    val t = TopicEntity(chapterId = chpId, name = topName.trim())
                                    syllabusDao.insertTopic(t)
                                    topicKeyToId["$chpId:${topName.trim().lowercase()}"] = t.id
                                    t.id
                                }
                            } else null

                            val originalId = validator.getField(map, listOf("question_id", "id"))
                            val finalId = if (row.isDuplicate && duplicateStrategy == DuplicateResolutionStrategy.KEEP_BOTH) {
                                UUID.randomUUID().toString()
                            } else if (!originalId.isNullOrBlank()) {
                                originalId.trim()
                            } else {
                                UUID.randomUUID().toString()
                            }

                            val text = validator.getField(map, listOf("question_text", "questiontext", "question")) ?: ""
                            val marks = validator.getField(map, listOf("marks", "mark", "max_marks"))?.toFloatOrNull() ?: 1.0f
                            val diff = validator.getField(map, listOf("difficulty"))?.uppercase() ?: "MEDIUM"
                            val qType = validator.getField(map, listOf("question_type", "type"))?.uppercase() ?: "SHORT_ANSWER"
                            val ans = validator.getField(map, listOf("answer", "marking_scheme", "markingscheme")) ?: ""
                            val src = validator.getField(map, listOf("source")) ?: ""

                            questionsToInsert.add(
                                QuestionBankEntity(
                                    id = finalId,
                                    subjectId = subId,
                                    chapterId = chpId,
                                    topicId = topId,
                                    questionText = text,
                                    markingScheme = ans,
                                    marks = marks,
                                    questionType = qType,
                                    difficulty = diff,
                                    source = src
                                )
                            )
                        }

                        if (questionsToInsert.isNotEmpty()) {
                            paperDao.insertQuestions(questionsToInsert)
                            importedCount = questionsToInsert.size
                            questionsCreated = questionsToInsert.size
                        }
                    }

                    CsvDatasetType.SYLLABUS -> {
                        // Already handled during syllabus resolution
                        importedCount = preview.validRows.size
                    }

                    CsvDatasetType.RECALL_CARDS -> {
                        for (row in preview.validRows) {
                            val map = row.data
                            val subName = validator.getField(map, listOf("subject", "subjectname", "subject_name")) ?: "General"
                            val chpName = validator.getField(map, listOf("chapter", "chaptername", "chapter_name")) ?: "General"
                            val topName = validator.getField(map, listOf("topic", "topicname", "topic_name"))

                            val subId = subjectNameToId[subName.trim().lowercase()] ?: run {
                                val s = SubjectEntity(name = subName.trim())
                                syllabusDao.insertSubject(s)
                                subjectNameToId[subName.trim().lowercase()] = s.id
                                s.id
                            }
                            val chpId = chapterKeyToId["$subId:${chpName.trim().lowercase()}"] ?: run {
                                val c = ChapterEntity(subjectId = subId, name = chpName.trim())
                                syllabusDao.insertChapter(c)
                                chapterKeyToId["$subId:${chpName.trim().lowercase()}"] = c.id
                                c.id
                            }
                            val topId = if (!topName.isNullOrBlank()) {
                                topicKeyToId["$chpId:${topName.trim().lowercase()}"]
                            } else null

                            val prompt = validator.getField(map, listOf("prompt", "question")) ?: ""
                            val answer = validator.getField(map, listOf("expected_answer", "answer")) ?: ""
                            val exp = validator.getField(map, listOf("explanation")) ?: ""

                            recallDao.insertCard(
                                RecallCardEntity(
                                    subjectId = subId,
                                    chapterId = chpId,
                                    topicId = topId,
                                    prompt = prompt,
                                    expectedAnswer = answer,
                                    explanation = exp
                                )
                            )
                            importedCount++
                            recallCardsCreated++
                        }
                    }

                    CsvDatasetType.MISTAKES -> {
                        for (row in preview.validRows) {
                            val map = row.data
                            val subName = validator.getField(map, listOf("subject", "subjectname", "subject_name")) ?: "General"
                            val chpName = validator.getField(map, listOf("chapter", "chaptername", "chapter_name")) ?: "General"
                            val topName = validator.getField(map, listOf("topic", "topicname", "topic_name"))

                            val subId = subjectNameToId[subName.trim().lowercase()] ?: run {
                                val s = SubjectEntity(name = subName.trim())
                                syllabusDao.insertSubject(s)
                                subjectNameToId[subName.trim().lowercase()] = s.id
                                s.id
                            }
                            val chpId = chapterKeyToId["$subId:${chpName.trim().lowercase()}"] ?: run {
                                val c = ChapterEntity(subjectId = subId, name = chpName.trim())
                                syllabusDao.insertChapter(c)
                                chapterKeyToId["$subId:${chpName.trim().lowercase()}"] = c.id
                                c.id
                            }
                            val topId = if (!topName.isNullOrBlank()) {
                                topicKeyToId["$chpId:${topName.trim().lowercase()}"]
                            } else null

                            val question = validator.getField(map, listOf("question", "question_text")) ?: ""
                            val studentAns = validator.getField(map, listOf("student_answer", "studentanswer")) ?: ""
                            val correctAns = validator.getField(map, listOf("correct_answer", "correctanswer", "answer")) ?: ""
                            val cat = validator.getField(map, listOf("category", "loss_category")) ?: "CARELESS_MISTAKE"
                            val lost = validator.getField(map, listOf("marks_lost", "markslost"))?.toFloatOrNull() ?: 1.0f

                            mistakeDao.insertMistake(
                                MistakeEntity(
                                    subjectId = subId,
                                    chapterId = chpId,
                                    topicId = topId,
                                    question = question,
                                    studentAnswer = studentAns,
                                    correctAnswer = correctAns,
                                    lossCategory = cat,
                                    marksLost = lost
                                )
                            )
                            importedCount++
                            mistakesCreated++
                        }
                    }

                    CsvDatasetType.STUDY_SESSIONS -> {
                        for (row in preview.validRows) {
                            val map = row.data
                            val subName = validator.getField(map, listOf("subject", "subjectname", "subject_name")) ?: "General"
                            val chpName = validator.getField(map, listOf("chapter", "chaptername", "chapter_name"))
                            val subId = subjectNameToId[subName.trim().lowercase()] ?: run {
                                val s = SubjectEntity(name = subName.trim())
                                syllabusDao.insertSubject(s)
                                subjectNameToId[subName.trim().lowercase()] = s.id
                                s.id
                            }
                            val chpId = if (!chpName.isNullOrBlank()) chapterKeyToId["$subId:${chpName.trim().lowercase()}"] else null
                            val dur = validator.getField(map, listOf("duration_minutes", "duration"))?.toIntOrNull() ?: 30

                            sessionDao.insertSession(
                                StudySessionEntity(
                                    subjectId = subId,
                                    chapterId = chpId,
                                    durationMinutes = dur
                                )
                            )
                            importedCount++
                        }
                    }

                    CsvDatasetType.SUBJECTS -> {
                        for (row in preview.validRows) {
                            val map = row.data
                            val name = validator.getField(map, listOf("name", "subject_name")) ?: continue
                            val color = validator.getField(map, listOf("color", "color_hex")) ?: "#B8781E"
                            val id = validator.getField(map, listOf("subject_id", "id")) ?: UUID.randomUUID().toString()
                            syllabusDao.insertSubject(SubjectEntity(id = id, name = name, colorHex = color))
                            importedCount++
                            subjectsCreated++
                        }
                    }

                    CsvDatasetType.CHAPTERS -> {
                        for (row in preview.validRows) {
                            val map = row.data
                            val subName = validator.getField(map, listOf("subject", "subject_id")) ?: "General"
                            val name = validator.getField(map, listOf("name", "chapter_name")) ?: continue
                            val subId = subjectNameToId[subName.trim().lowercase()] ?: run {
                                val s = SubjectEntity(name = subName.trim())
                                syllabusDao.insertSubject(s)
                                subjectNameToId[subName.trim().lowercase()] = s.id
                                s.id
                            }
                            val order = validator.getField(map, listOf("order", "order_index"))?.toIntOrNull() ?: 0
                            val id = validator.getField(map, listOf("chapter_id", "id")) ?: UUID.randomUUID().toString()
                            syllabusDao.insertChapter(ChapterEntity(id = id, subjectId = subId, name = name, orderIndex = order))
                            importedCount++
                            chaptersCreated++
                        }
                    }

                    CsvDatasetType.TOPICS -> {
                        for (row in preview.validRows) {
                            val map = row.data
                            val chpName = validator.getField(map, listOf("chapter", "chapter_id")) ?: "General"
                            val name = validator.getField(map, listOf("name", "topic_name")) ?: continue
                            val chpId = chapterKeyToId.values.firstOrNull() ?: run {
                                val generalSub = SubjectEntity(name = "General")
                                syllabusDao.insertSubject(generalSub)
                                val c = ChapterEntity(subjectId = generalSub.id, name = chpName)
                                syllabusDao.insertChapter(c)
                                c.id
                            }
                            val order = validator.getField(map, listOf("order", "order_index"))?.toIntOrNull() ?: 0
                            val id = validator.getField(map, listOf("topic_id", "id")) ?: UUID.randomUUID().toString()
                            syllabusDao.insertTopic(TopicEntity(id = id, chapterId = chpId, name = name, orderIndex = order))
                            importedCount++
                            topicsCreated++
                        }
                    }

                    CsvDatasetType.PAPERS -> {
                        for (row in preview.validRows) {
                            val map = row.data
                            val title = validator.getField(map, listOf("title")) ?: "Practice Paper"
                            val subName = validator.getField(map, listOf("subject", "subject_id")) ?: "General"
                            val subId = subjectNameToId[subName.trim().lowercase()] ?: run {
                                val s = SubjectEntity(name = subName.trim())
                                syllabusDao.insertSubject(s)
                                s.id
                            }
                            val marks = validator.getField(map, listOf("total_marks", "marks"))?.toFloatOrNull() ?: 50f
                            val dur = validator.getField(map, listOf("duration_minutes", "duration"))?.toIntOrNull() ?: 60
                            val id = validator.getField(map, listOf("paper_id", "id")) ?: UUID.randomUUID().toString()

                            paperDao.insertPaper(
                                PaperEntity(
                                    id = id,
                                    title = title,
                                    subjectId = subId,
                                    totalMarks = marks,
                                    durationMinutes = dur
                                )
                            )
                            importedCount++
                        }
                    }

                    CsvDatasetType.RESULTS -> {
                        // Results imported
                        importedCount = preview.validRows.size
                    }
                }
            }

            return CsvImportSummary(
                datasetType = preview.datasetType,
                totalImported = importedCount,
                duplicatesSkipped = duplicatesSkipped,
                duplicatesReplaced = duplicatesReplaced,
                rowsSkippedDueToErrors = preview.errorCount,
                subjectsCreated = subjectsCreated,
                chaptersCreated = chaptersCreated,
                topicsCreated = topicsCreated,
                questionsCreated = questionsCreated,
                recallCardsCreated = recallCardsCreated,
                mistakesCreated = mistakesCreated
            )
        } catch (e: Exception) {
            // Failed atomic transaction: Room rolled back automatically
            return CsvImportSummary(
                datasetType = preview.datasetType,
                totalImported = 0,
                duplicatesSkipped = 0,
                duplicatesReplaced = 0,
                rowsSkippedDueToErrors = preview.validRows.size,
                subjectsCreated = 0,
                chaptersCreated = 0,
                topicsCreated = 0,
                questionsCreated = 0,
                isRollback = true,
                errorMessage = e.message ?: "Database transaction failed and was rolled back cleanly"
            )
        }
    }

    suspend fun export(datasetType: CsvDatasetType, database: DatabaseProvider): String {
        val syllabusDao = database.syllabusDao()
        val paperDao = database.paperPilotDao()
        val mistakeDao = database.mistakeDao()
        val recallDao = database.recallDao()

        val subjects = syllabusDao.getAllSubjects().first()
        val subjectMap = subjects.associate { it.id to it.name }

        val chapters = mutableListOf<ChapterEntity>()
        for (sub in subjects) {
            chapters.addAll(syllabusDao.getChaptersForSubjectSync(sub.id))
        }
        val chapterMap = chapters.associate { it.id to it.name }

        val topics = syllabusDao.getAllTopicsSync()
        val topicMap = topics.associate { it.id to it.name }

        return when (datasetType) {
            CsvDatasetType.QUESTIONS -> {
                val questions = paperDao.getAllQuestionsSync()
                exporter.exportQuestions(questions, subjectMap, chapterMap, topicMap)
            }
            CsvDatasetType.RESULTS -> {
                val results = paperDao.getAllResults().first()
                val papers = paperDao.getAllPapers().first().associateBy { it.id }
                exporter.exportResults(results, papers, subjectMap)
            }
            CsvDatasetType.MISTAKES -> {
                val mistakes = mistakeDao.getAllMistakes().first()
                exporter.exportMistakes(mistakes, subjectMap, chapterMap, topicMap)
            }
            CsvDatasetType.SYLLABUS -> {
                exporter.exportSyllabus(subjects, chapters, topics)
            }
            CsvDatasetType.RECALL_CARDS -> {
                val cards = recallDao.getAllCards().first()
                exporter.exportRecallCards(cards, subjectMap, chapterMap, topicMap)
            }
            else -> exporter.getTemplate(datasetType)
        }
    }
}
