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

class DefaultUniversalCsvProcessor : UniversalCsvProcessor {

    override fun getTemplate(entity: UniversalCsvEntity): String {
        return when (entity) {
            UniversalCsvEntity.SYLLABUS ->
                "SubjectName,ChapterName,TopicName,MasteryState,ExamRelevance\n" +
                "Mathematics,Calculus,Derivatives,LEARNING,HIGH\n" +
                "Physics,Mechanics,Newton's Laws,MASTERED,HIGH\n"

            UniversalCsvEntity.QUESTION_BANK ->
                "SubjectName,ChapterName,TopicName,QuestionText,MarkingScheme,Marks,QuestionType,Difficulty\n" +
                "Physics,Mechanics,Newton's Laws,\"State Newton's second law.\",\"Formula (1m) + Statement (1m)\",2.0,SHORT_ANSWER,MEDIUM\n"

            UniversalCsvEntity.RECALL_CARDS ->
                "SubjectName,ChapterName,TopicName,Prompt,ExpectedAnswer,Explanation\n" +
                "Physics,Mechanics,Newton's Laws,\"What is the SI unit of force?\",Newton,\"1 N = 1 kg·m/s²\"\n"

            UniversalCsvEntity.MISTAKES ->
                "SubjectName,ChapterName,Question,StudentAnswer,CorrectAnswer,LossCategory,MarksLost\n" +
                "Mathematics,Calculus,\"Integrate sin(x)\",\"cos(x)\",\"-cos(x) + C\",CARELESS_MISTAKE,1.0\n"

            UniversalCsvEntity.STUDY_SESSIONS ->
                "SubjectName,ChapterName,DurationMinutes\n" +
                "Mathematics,Calculus,60\n"
        }
    }

    override fun parseAndValidate(entity: UniversalCsvEntity, content: String): CsvParseResult {
        val lines = splitCsvLines(content)
        if (lines.isEmpty()) {
            return CsvParseResult(
                entityType = entity,
                totalRows = 0,
                validRows = emptyList(),
                errors = listOf(CsvRowError(0, "", "CSV content is empty"))
            )
        }

        val headers = lines.first().map { it.trim().lowercase() }
        val requiredHeaders = when (entity) {
            UniversalCsvEntity.SYLLABUS -> listOf("subjectname", "chaptername", "topicname")
            UniversalCsvEntity.QUESTION_BANK -> listOf("subjectname", "chaptername", "questiontext", "marks")
            UniversalCsvEntity.RECALL_CARDS -> listOf("subjectname", "chaptername", "prompt", "expectedanswer")
            UniversalCsvEntity.MISTAKES -> listOf("subjectname", "chaptername", "question", "correctanswer")
            UniversalCsvEntity.STUDY_SESSIONS -> listOf("subjectname", "durationminutes")
        }

        val missing = requiredHeaders.filter { it !in headers }
        if (missing.isNotEmpty()) {
            return CsvParseResult(
                entityType = entity,
                totalRows = lines.size - 1,
                validRows = emptyList(),
                errors = listOf(
                    CsvRowError(
                        lineNumber = 1,
                        rawLine = lines.first().joinToString(","),
                        message = "Missing required columns: ${missing.joinToString(", ")}"
                    )
                )
            )
        }

        val validRows = mutableListOf<Map<String, String>>()
        val errors = mutableListOf<CsvRowError>()

        for (i in 1 until lines.size) {
            val row = lines[i]
            if (row.all { it.isBlank() }) continue // Skip empty line

            if (row.size != headers.size) {
                errors.add(
                    CsvRowError(
                        lineNumber = i + 1,
                        rawLine = row.joinToString(","),
                        message = "Column count mismatch: expected ${headers.size}, found ${row.size}"
                    )
                )
                continue
            }

            val map = headers.zip(row).toMap()
            validRows.add(map)
        }

        return CsvParseResult(
            entityType = entity,
            totalRows = lines.size - 1,
            validRows = validRows,
            errors = errors
        )
    }

    private fun splitCsvLines(content: String): List<List<String>> {
        val reader = BufferedReader(StringReader(content))
        val result = mutableListOf<List<String>>()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            val currentLine = line ?: break
            if (currentLine.trim().isEmpty()) continue

            val tokens = mutableListOf<String>()
            val sb = java.lang.StringBuilder()
            var inQuotes = false

            for (ch in currentLine) {
                when {
                    ch == '\"' -> inQuotes = !inQuotes
                    ch == ',' && !inQuotes -> {
                        tokens.add(sb.toString().trim())
                        sb.setLength(0)
                    }
                    else -> sb.append(ch)
                }
            }
            tokens.add(sb.toString().trim())
            result.add(tokens)
        }

        return result
    }
}
