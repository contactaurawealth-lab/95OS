package com.os95.app.core.csv

class CsvValidator {

    private val validDifficulties = setOf("easy", "medium", "hard", "mixed")
    private val validQuestionTypes = setOf(
        "mcq",
        "very_short_answer",
        "short_answer",
        "long_answer",
        "numerical",
        "case_study",
        "assertion_reason",
        "true_false",
        "fill_in_the_blank"
    )

    fun getRequiredColumns(datasetType: CsvDatasetType): List<List<String>> {
        // Returns list of required column aliases (any alias matches)
        return when (datasetType) {
            CsvDatasetType.QUESTIONS -> listOf(
                listOf("subject", "subjectname", "subject_name", "subject_id"),
                listOf("chapter", "chaptername", "chapter_name", "chapter_id"),
                listOf("question_text", "questiontext", "question"),
                listOf("marks", "mark", "max_marks")
            )
            CsvDatasetType.SUBJECTS -> listOf(
                listOf("name", "subject_name", "subjectname")
            )
            CsvDatasetType.CHAPTERS -> listOf(
                listOf("subject", "subject_id", "subjectname", "subject_name"),
                listOf("name", "chapter_name", "chaptername")
            )
            CsvDatasetType.TOPICS -> listOf(
                listOf("chapter", "chapter_id", "chaptername", "chapter_name"),
                listOf("name", "topic_name", "topicname")
            )
            CsvDatasetType.SYLLABUS -> listOf(
                listOf("subject", "subjectname", "subject_name"),
                listOf("chapter", "chaptername", "chapter_name"),
                listOf("topic", "topicname", "topic_name")
            )
            CsvDatasetType.RECALL_CARDS -> listOf(
                listOf("subject", "subjectname", "subject_name"),
                listOf("chapter", "chaptername", "chapter_name"),
                listOf("prompt", "question"),
                listOf("expected_answer", "expectedanswer", "answer")
            )
            CsvDatasetType.PAPERS -> listOf(
                listOf("title", "paper_title"),
                listOf("subject", "subject_name", "subjectname", "subject_id"),
                listOf("total_marks", "totalmarks", "marks"),
                listOf("duration_minutes", "duration", "durationminutes")
            )
            CsvDatasetType.RESULTS -> listOf(
                listOf("paper_id", "paper", "paper_title"),
                listOf("marks_obtained", "marksobtained", "score"),
                listOf("total_marks", "totalmarks", "max_marks")
            )
            CsvDatasetType.MISTAKES -> listOf(
                listOf("subject", "subjectname", "subject_name"),
                listOf("chapter", "chaptername", "chapter_name"),
                listOf("question", "question_text"),
                listOf("correct_answer", "correctanswer", "answer")
            )
            CsvDatasetType.STUDY_SESSIONS -> listOf(
                listOf("subject", "subjectname", "subject_name"),
                listOf("duration_minutes", "durationminutes", "duration")
            )
        }
    }

    fun checkMissingColumns(datasetType: CsvDatasetType, headers: List<String>): List<String> {
        val normalizedHeaders = headers.map { normalizeHeader(it) }
        val required = getRequiredColumns(datasetType)
        val missing = mutableListOf<String>()

        for (aliasGroup in required) {
            val matched = aliasGroup.any { alias -> normalizedHeaders.contains(normalizeHeader(alias)) }
            if (!matched) {
                missing.add(aliasGroup.first())
            }
        }
        return missing
    }

    fun normalizeHeader(raw: String): String {
        return raw.trim().lowercase().replace(" ", "").replace("_", "").replace("-", "")
    }

    fun validateRowData(
        datasetType: CsvDatasetType,
        lineNumber: Int,
        rowMap: Map<String, String>,
        rawSnippet: String
    ): List<CsvValidationError> {
        val errors = mutableListOf<CsvValidationError>()

        when (datasetType) {
            CsvDatasetType.QUESTIONS -> {
                // Validate Marks
                val marksVal = getField(rowMap, listOf("marks", "mark", "max_marks"))
                if (marksVal.isNullOrBlank()) {
                    errors.add(
                        CsvValidationError(
                            lineNumber = lineNumber,
                            column = "marks",
                            rawSnippet = rawSnippet,
                            message = "Marks must exist and cannot be empty"
                        )
                    )
                } else {
                    val floatMarks = marksVal.toFloatOrNull()
                    if (floatMarks == null) {
                        errors.add(
                            CsvValidationError(
                                lineNumber = lineNumber,
                                column = "marks",
                                rawSnippet = rawSnippet,
                                message = "Marks \"$marksVal\" is not numeric"
                            )
                        )
                    } else if (floatMarks <= 0f) {
                        errors.add(
                            CsvValidationError(
                                lineNumber = lineNumber,
                                column = "marks",
                                rawSnippet = rawSnippet,
                                message = "Marks must be greater than zero, found $floatMarks"
                            )
                        )
                    }
                }

                // Validate Difficulty
                val diffVal = getField(rowMap, listOf("difficulty"))
                if (!diffVal.isNullOrBlank()) {
                    val normalizedDiff = diffVal.trim().lowercase()
                    if (normalizedDiff !in validDifficulties) {
                        errors.add(
                            CsvValidationError(
                                lineNumber = lineNumber,
                                column = "difficulty",
                                rawSnippet = rawSnippet,
                                message = "Invalid difficulty: \"$diffVal\". Allowed: Easy, Medium, Hard, Mixed"
                            )
                        )
                    }
                }

                // Validate Question Type
                val typeVal = getField(rowMap, listOf("question_type", "questiontype", "type"))
                if (!typeVal.isNullOrBlank()) {
                    val normalizedType = typeVal.trim().lowercase().replace(" ", "_").replace("-", "_")
                    if (normalizedType !in validQuestionTypes) {
                        errors.add(
                            CsvValidationError(
                                lineNumber = lineNumber,
                                column = "question_type",
                                rawSnippet = rawSnippet,
                                message = "Invalid question type: \"$typeVal\""
                            )
                        )
                    }
                }

                // Validate Question Text
                val qText = getField(rowMap, listOf("question_text", "questiontext", "question"))
                if (qText.isNullOrBlank()) {
                    errors.add(
                        CsvValidationError(
                            lineNumber = lineNumber,
                            column = "question_text",
                            rawSnippet = rawSnippet,
                            message = "Question text cannot be blank"
                        )
                    )
                }
            }

            CsvDatasetType.PAPERS -> {
                val totalMarks = getField(rowMap, listOf("total_marks", "totalmarks", "marks"))?.toFloatOrNull()
                if (totalMarks == null || totalMarks <= 0f) {
                    errors.add(
                        CsvValidationError(
                            lineNumber = lineNumber,
                            column = "total_marks",
                            rawSnippet = rawSnippet,
                            message = "Paper total marks must be greater than zero"
                        )
                    )
                }
                val duration = getField(rowMap, listOf("duration_minutes", "duration"))?.toIntOrNull()
                if (duration == null || duration <= 0) {
                    errors.add(
                        CsvValidationError(
                            lineNumber = lineNumber,
                            column = "duration_minutes",
                            rawSnippet = rawSnippet,
                            message = "Paper duration must be a positive integer in minutes"
                        )
                    )
                }
            }

            CsvDatasetType.RESULTS -> {
                val obtained = getField(rowMap, listOf("marks_obtained", "marksobtained", "score"))?.toFloatOrNull()
                val total = getField(rowMap, listOf("total_marks", "totalmarks", "max_marks"))?.toFloatOrNull()
                if (obtained == null || obtained < 0f) {
                    errors.add(
                        CsvValidationError(
                            lineNumber = lineNumber,
                            column = "marks_obtained",
                            rawSnippet = rawSnippet,
                            message = "Marks obtained must be non-negative"
                        )
                    )
                }
                if (total == null || total <= 0f) {
                    errors.add(
                        CsvValidationError(
                            lineNumber = lineNumber,
                            column = "total_marks",
                            rawSnippet = rawSnippet,
                            message = "Total marks must be greater than zero"
                        )
                    )
                }
            }

            CsvDatasetType.STUDY_SESSIONS -> {
                val duration = getField(rowMap, listOf("duration_minutes", "durationminutes", "duration"))?.toIntOrNull()
                if (duration == null || duration <= 0) {
                    errors.add(
                        CsvValidationError(
                            lineNumber = lineNumber,
                            column = "duration_minutes",
                            rawSnippet = rawSnippet,
                            message = "Study duration must be greater than zero minutes"
                        )
                    )
                }
            }

            else -> {
                // Basic check for empty primary fields
                val primaryField = when (datasetType) {
                    CsvDatasetType.SUBJECTS -> getField(rowMap, listOf("name", "subject_name"))
                    CsvDatasetType.CHAPTERS -> getField(rowMap, listOf("name", "chapter_name"))
                    CsvDatasetType.TOPICS -> getField(rowMap, listOf("name", "topic_name"))
                    CsvDatasetType.RECALL_CARDS -> getField(rowMap, listOf("prompt", "question"))
                    CsvDatasetType.MISTAKES -> getField(rowMap, listOf("question", "question_text"))
                    else -> null
                }
                if (primaryField != null && primaryField.isBlank()) {
                    errors.add(
                        CsvValidationError(
                            lineNumber = lineNumber,
                            rawSnippet = rawSnippet,
                            message = "Primary name or content field cannot be empty"
                        )
                    )
                }
            }
        }

        return errors
    }

    fun getField(map: Map<String, String>, aliases: List<String>): String? {
        for (alias in aliases) {
            val normalizedAlias = normalizeHeader(alias)
            for ((key, value) in map) {
                if (normalizeHeader(key) == normalizedAlias) {
                    return value
                }
            }
        }
        return null
    }
}
