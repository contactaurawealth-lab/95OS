package com.os95.app.core.csv

enum class CsvDatasetType(val id: String, val displayName: String, val description: String) {
    QUESTIONS(
        id = "questions",
        displayName = "Questions",
        description = "Question Bank entries with marks, difficulty, type, and syllabus mapping"
    ),
    SUBJECTS(
        id = "subjects",
        displayName = "Subjects",
        description = "Academic subjects hierarchy"
    ),
    CHAPTERS(
        id = "chapters",
        displayName = "Chapters",
        description = "Curriculum chapters mapped to subjects"
    ),
    TOPICS(
        id = "topics",
        displayName = "Topics",
        description = "Granular concepts with mastery status and exam relevance"
    ),
    SYLLABUS(
        id = "syllabus",
        displayName = "Syllabus (Unified)",
        description = "Combined Subject, Chapter, and Topic structure in a single file"
    ),
    RECALL_CARDS(
        id = "recall_cards",
        displayName = "Recall Cards",
        description = "Active recall prompt/answer cards with spaced repetition metadata"
    ),
    PAPERS(
        id = "papers",
        displayName = "Exam Papers",
        description = "PaperPilot exam paper blueprints and configurations"
    ),
    RESULTS(
        id = "results",
        displayName = "Exam Results",
        description = "Historical exam attempts, scores, and duration records"
    ),
    MISTAKES(
        id = "mistakes",
        displayName = "Mistake Bank",
        description = "Lost marks and mistake diagnostics linked to topics and categories"
    ),
    STUDY_SESSIONS(
        id = "study_sessions",
        displayName = "Study Sessions",
        description = "Focus and exam practice time logs"
    );

    companion object {
        fun fromIdOrName(raw: String): CsvDatasetType? {
            val normalized = raw.trim().lowercase().replace(" ", "_").replace("-", "_")
            return values().firstOrNull {
                it.id == normalized || it.displayName.lowercase() == normalized || it.name.lowercase() == normalized
            }
        }
    }
}

enum class DuplicateResolutionStrategy(val displayName: String, val description: String) {
    SKIP("Skip duplicates", "Keep existing records and ignore duplicates from CSV"),
    REPLACE("Replace existing", "Overwrite existing records with incoming CSV data"),
    KEEP_BOTH("Keep both", "Generate new unique IDs and import as separate records")
}

enum class MissingRelationshipMode(val displayName: String, val description: String) {
    CREATE_MISSING("Create missing relationships", "Automatically generate missing subjects, chapters, or topics"),
    FAIL_ON_MISSING("Reject with error", "Block import if any referenced subject, chapter, or topic is missing"),
    SKIP_ROW("Skip invalid rows", "Skip rows referencing missing academic entities while importing the rest")
}

data class CsvValidationError(
    val lineNumber: Int,
    val column: String? = null,
    val rawSnippet: String = "",
    val message: String
)

data class ValidatedRow(
    val lineNumber: Int,
    val data: Map<String, String>,
    val isDuplicate: Boolean = false,
    val duplicateReason: String? = null,
    val missingSubject: String? = null,
    val missingChapter: String? = null,
    val missingTopic: String? = null
)

data class CsvPreviewResult(
    val datasetType: CsvDatasetType,
    val totalRowsDetected: Int,
    val validRows: List<ValidatedRow>,
    val duplicateCount: Int,
    val errorCount: Int,
    val errors: List<CsvValidationError>,
    val newSubjectsToCreate: List<String> = emptyList(),
    val newChaptersToCreate: List<Pair<String, String>> = emptyList(), // Pair(SubjectName, ChapterName)
    val newTopicsToCreate: List<Triple<String, String, String>> = emptyList(), // Triple(SubjectName, ChapterName, TopicName)
    val newQuestionsCount: Int = 0
) {
    val canProceed: Boolean get() = errors.isEmpty() || validRows.isNotEmpty()
}

data class CsvImportSummary(
    val datasetType: CsvDatasetType,
    val totalImported: Int,
    val duplicatesSkipped: Int,
    val duplicatesReplaced: Int,
    val rowsSkippedDueToErrors: Int,
    val subjectsCreated: Int,
    val chaptersCreated: Int,
    val topicsCreated: Int,
    val questionsCreated: Int,
    val recallCardsCreated: Int = 0,
    val mistakesCreated: Int = 0,
    val resultsCreated: Int = 0,
    val isRollback: Boolean = false,
    val errorMessage: String? = null
)
