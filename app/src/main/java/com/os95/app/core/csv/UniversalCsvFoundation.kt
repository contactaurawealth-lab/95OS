package com.os95.app.core.csv

import java.io.BufferedReader
import java.io.StringReader

enum class UniversalCsvEntity(val displayName: String) {
    SYLLABUS("Syllabus (Subjects, Chapters, Topics)"),
    QUESTION_BANK("Question Bank"),
    RECALL_CARDS("Recall & Flashcards"),
    MISTAKES("Mistake Bank"),
    STUDY_SESSIONS("Study Sessions")
}

data class CsvRowError(
    val lineNumber: Int,
    val rawLine: String,
    val message: String
)

data class CsvParseResult(
    val entityType: UniversalCsvEntity,
    val totalRows: Int,
    val validRows: List<Map<String, String>>,
    val errors: List<CsvRowError>
)

interface UniversalCsvProcessor {
    fun getTemplate(entity: UniversalCsvEntity): String
    fun parseAndValidate(entity: UniversalCsvEntity, content: String): CsvParseResult
}

class DefaultUniversalCsvProcessor(
    private val engine: UniversalCsvEngine = UniversalCsvEngine()
) : UniversalCsvProcessor {

    override fun getTemplate(entity: UniversalCsvEntity): String {
        return engine.getTemplate(entity)
    }

    override fun parseAndValidate(entity: UniversalCsvEntity, content: String): CsvParseResult {
        return engine.parseAndValidate(entity, content)
    }
}
