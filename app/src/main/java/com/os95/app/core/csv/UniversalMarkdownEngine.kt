package com.os95.app.core.csv

import com.os95.app.core.database.DatabaseProvider
import kotlinx.coroutines.flow.first

class UniversalMarkdownEngine {

    fun getMarkdownTemplate(datasetType: CsvDatasetType): String {
        return when (datasetType) {
            CsvDatasetType.SYLLABUS -> """
# Mathematics
## Real Numbers
- Euclid's Division Lemma [HIGH] [MASTERED]
- Fundamental Theorem of Arithmetic [HIGH] [REVISED]
- Revisiting Irrational Numbers [MEDIUM] [LEARNING]

## Polynomials
- Geometrical Meaning of Zeroes [HIGH] [LEARNING]
- Relationship between Zeroes and Coefficients [HIGH] [NOT_STARTED]

# Physics
## Kinematics
- Rectilinear Motion [HIGH] [MASTERED]
- Projectile Motion [HIGH] [REVISED]
- Relative Velocity [MEDIUM] [LEARNING]
            """.trimIndent()

            CsvDatasetType.QUESTIONS -> """
### Question: Solve the quadratic equation x^2 - 5x + 6 = 0
- Subject: Mathematics
- Chapter: Quadratic Equations
- Topic: Factoring Quadratics
- Marks: 3.0
- Difficulty: MEDIUM
- Type: SHORT_ANSWER
- Answer: x = 2 or x = 3

### Question: State Newton's Second Law of Motion and derive F = ma
- Subject: Physics
- Chapter: Laws of Motion
- Topic: Second Law
- Marks: 5.0
- Difficulty: HARD
- Type: LONG_ANSWER
- Answer: Rate of change of momentum is directly proportional to applied force.
            """.trimIndent()

            CsvDatasetType.RECALL_CARDS -> """
## Card: What is the formula for the roots of a quadratic equation?
- Back: x = (-b ± √(b² - 4ac)) / (2a)
- Subject: Mathematics
- Chapter: Quadratic Equations
- Topic: Quadratic Formula

## Card: Define Ohm's Law and its mathematical formulation
- Back: Current through a conductor is directly proportional to voltage across it: V = IR
- Subject: Physics
- Chapter: Current Electricity
- Topic: Ohm's Law
            """.trimIndent()

            CsvDatasetType.MISTAKES -> """
### Mistake: Forgot negative sign in discriminant formula
- Subject: Mathematics
- Chapter: Quadratic Equations
- Topic: Quadratic Formula
- Question: Solve 2x^2 + 4x - 6 = 0
- Student Answer: b^2 + 4ac was calculated as 64
- Correct Answer: b^2 - 4ac = 16 - 4(2)(-6) = 64, but subtraction was missed
- Marks Lost: 2.0
- Category: CALCULATION_ERROR

### Mistake: Confused velocity with acceleration at the apex of projectile motion
- Subject: Physics
- Chapter: Kinematics
- Topic: Projectile Motion
- Question: What is the vertical acceleration at the maximum height?
- Student Answer: 0 m/s^2
- Correct Answer: -9.8 m/s^2 (gravity always acts downwards)
- Marks Lost: 1.0
- Category: CONCEPTUAL_ERROR
            """.trimIndent()

            else -> """
| Subject | Chapter | Topic |
| Mathematics | Algebra | Matrices |
| Physics | Mechanics | Friction |
            """.trimIndent()
        }
    }

    /**
     * Parses Markdown text (tables or structured outlines) into standardized column maps.
     */
    fun parseMarkdown(datasetType: CsvDatasetType, content: String): List<Map<String, String>> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return emptyList()

        val lines = trimmed.lines().map { it.trim() }

        // Check if content is a Markdown Table
        val isTable = lines.any { it.startsWith("|") && it.endsWith("|") }
        if (isTable) {
            return parseMarkdownTable(lines)
        }

        // Otherwise parse structured Markdown headings / bullet items
        return when (datasetType) {
            CsvDatasetType.SYLLABUS -> parseSyllabusOutline(lines)
            CsvDatasetType.QUESTIONS -> parseQuestionsOutline(lines)
            CsvDatasetType.RECALL_CARDS -> parseRecallCardsOutline(lines)
            CsvDatasetType.MISTAKES -> parseMistakesOutline(lines)
            else -> parseSyllabusOutline(lines)
        }
    }

    private fun parseMarkdownTable(lines: List<String>): List<Map<String, String>> {
        val tableLines = lines.filter { it.startsWith("|") && it.endsWith("|") }
        if (tableLines.size < 2) return emptyList()

        val headerRow = tableLines[0]
        val headers = headerRow.split("|")
            .map { it.trim().lowercase().replace(" ", "_") }
            .filter { it.isNotEmpty() }

        val result = mutableListOf<Map<String, String>>()

        for (i in 1 until tableLines.size) {
            val line = tableLines[i]
            // Skip markdown divider row e.g. |---|---|
            if (line.contains("---")) continue

            val cells = line.split("|")
                .map { it.trim() }
                .filterIndexed { index, _ -> index > 0 && index <= headers.size }

            if (cells.size >= headers.size) {
                val rowMap = headers.zip(cells).toMap()
                result.add(rowMap)
            }
        }
        return result
    }

    private fun parseSyllabusOutline(lines: List<String>): List<Map<String, String>> {
        val result = mutableListOf<Map<String, String>>()
        var currentSubject = "General"
        var currentChapter = "Overview"

        for (line in lines) {
            when {
                line.startsWith("# ") -> {
                    currentSubject = line.removePrefix("# ").trim()
                }
                line.startsWith("## ") -> {
                    var ch = line.removePrefix("## ").trim()
                    if (ch.startsWith("Chapter: ", ignoreCase = true)) {
                        ch = ch.substringAfter(":").trim()
                    }
                    currentChapter = ch
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    var topicLine = line.substring(2).trim()
                    if (topicLine.startsWith("[ ] ") || topicLine.startsWith("[x] ")) {
                        topicLine = topicLine.substring(4).trim()
                    }

                    var relevance = "MEDIUM"
                    var mastery = "NOT_STARTED"

                    if (topicLine.contains("[HIGH]", ignoreCase = true)) {
                        relevance = "HIGH"
                        topicLine = topicLine.replace("[HIGH]", "", ignoreCase = true).trim()
                    } else if (topicLine.contains("[LOW]", ignoreCase = true)) {
                        relevance = "LOW"
                        topicLine = topicLine.replace("[LOW]", "", ignoreCase = true).trim()
                    }

                    if (topicLine.contains("[MASTERED]", ignoreCase = true)) {
                        mastery = "MASTERED"
                        topicLine = topicLine.replace("[MASTERED]", "", ignoreCase = true).trim()
                    } else if (topicLine.contains("[REVISED]", ignoreCase = true)) {
                        mastery = "REVISED"
                        topicLine = topicLine.replace("[REVISED]", "", ignoreCase = true).trim()
                    } else if (topicLine.contains("[LEARNING]", ignoreCase = true)) {
                        mastery = "LEARNING"
                        topicLine = topicLine.replace("[LEARNING]", "", ignoreCase = true).trim()
                    }

                    if (topicLine.isNotBlank()) {
                        result.add(
                            mapOf(
                                "subject_name" to currentSubject,
                                "chapter_name" to currentChapter,
                                "topic_name" to topicLine,
                                "mastery_state" to mastery,
                                "exam_relevance" to relevance
                            )
                        )
                    }
                }
            }
        }
        return result
    }

    private fun parseQuestionsOutline(lines: List<String>): List<Map<String, String>> {
        val result = mutableListOf<Map<String, String>>()
        var currentQuestion: MutableMap<String, String>? = null

        for (line in lines) {
            if (line.startsWith("### Question:") || line.startsWith("### Q:") || line.startsWith("## Question:")) {
                if (currentQuestion != null && currentQuestion.containsKey("question_text")) {
                    result.add(currentQuestion)
                }
                val qText = line.substringAfter(":").trim()
                currentQuestion = mutableMapOf(
                    "question_text" to qText,
                    "marks" to "1.0",
                    "difficulty" to "MEDIUM",
                    "question_type" to "SHORT_ANSWER"
                )
            } else if (currentQuestion != null && (line.startsWith("- ") || line.startsWith("* "))) {
                val bullet = line.substring(2).trim()
                val key = bullet.substringBefore(":").trim().lowercase().replace(" ", "_")
                val value = bullet.substringAfter(":").trim()

                when (key) {
                    "subject", "subject_name" -> currentQuestion["subject_name"] = value
                    "chapter", "chapter_name" -> currentQuestion["chapter_name"] = value
                    "topic", "topic_name" -> currentQuestion["topic_name"] = value
                    "marks" -> currentQuestion["marks"] = value
                    "difficulty" -> currentQuestion["difficulty"] = value.uppercase()
                    "type", "question_type" -> currentQuestion["question_type"] = value.uppercase()
                    "answer", "correct_answer", "solution" -> currentQuestion["correct_answer"] = value
                }
            }
        }
        if (currentQuestion != null && currentQuestion.containsKey("question_text")) {
            result.add(currentQuestion)
        }
        return result
    }

    private fun parseRecallCardsOutline(lines: List<String>): List<Map<String, String>> {
        val result = mutableListOf<Map<String, String>>()
        var currentCard: MutableMap<String, String>? = null

        for (line in lines) {
            if (line.startsWith("## Card:") || line.startsWith("### Card:") || line.startsWith("# Card:")) {
                if (currentCard != null && currentCard.containsKey("prompt")) {
                    result.add(currentCard)
                }
                val prompt = line.substringAfter(":").trim()
                currentCard = mutableMapOf("prompt" to prompt)
            } else if (currentCard != null && (line.startsWith("- ") || line.startsWith("* "))) {
                val bullet = line.substring(2).trim()
                val key = bullet.substringBefore(":").trim().lowercase().replace(" ", "_")
                val value = bullet.substringAfter(":").trim()

                when (key) {
                    "back", "answer", "completion" -> currentCard["answer"] = value
                    "subject", "subject_name" -> currentCard["subject_name"] = value
                    "chapter", "chapter_name" -> currentCard["chapter_name"] = value
                    "topic", "topic_name" -> currentCard["topic_name"] = value
                }
            }
        }
        if (currentCard != null && currentCard.containsKey("prompt")) {
            result.add(currentCard)
        }
        return result
    }

    private fun parseMistakesOutline(lines: List<String>): List<Map<String, String>> {
        val result = mutableListOf<Map<String, String>>()
        var currentMistake: MutableMap<String, String>? = null

        for (line in lines) {
            if (line.startsWith("### Mistake:") || line.startsWith("## Mistake:")) {
                if (currentMistake != null && currentMistake.containsKey("question")) {
                    result.add(currentMistake)
                }
                val question = line.substringAfter(":").trim()
                currentMistake = mutableMapOf(
                    "question" to question,
                    "marks_lost" to "1.0",
                    "loss_category" to "CARELESS_MISTAKE"
                )
            } else if (currentMistake != null && (line.startsWith("- ") || line.startsWith("* "))) {
                val bullet = line.substring(2).trim()
                val key = bullet.substringBefore(":").trim().lowercase().replace(" ", "_")
                val value = bullet.substringAfter(":").trim()

                when (key) {
                    "subject", "subject_name" -> currentMistake["subject_name"] = value
                    "chapter", "chapter_name" -> currentMistake["chapter_name"] = value
                    "topic", "topic_name" -> currentMistake["topic_name"] = value
                    "question" -> {
                        if (currentMistake["question"].isNullOrBlank()) {
                            currentMistake["question"] = value
                        }
                    }
                    "student_answer" -> currentMistake["student_answer"] = value
                    "correct_answer", "solution" -> currentMistake["correct_answer"] = value
                    "marks_lost" -> currentMistake["marks_lost"] = value
                    "category", "loss_category" -> currentMistake["loss_category"] = value.uppercase()
                }
            }
        }
        if (currentMistake != null && currentMistake.containsKey("question")) {
            result.add(currentMistake)
        }
        return result
    }

    /**
     * Exports database tables into clean, readable Markdown documents.
     */
    suspend fun exportToMarkdown(datasetType: CsvDatasetType, database: DatabaseProvider): String {
        val sb = StringBuilder()

        when (datasetType) {
            CsvDatasetType.SYLLABUS -> {
                sb.appendLine("# 95OS Academic Syllabus Blueprint")
                sb.appendLine("> Deterministic offline curriculum tracking")
                sb.appendLine()

                val subjects = database.syllabusDao().getAllSubjectsSync()
                for (sub in subjects) {
                    sb.appendLine("# ${sub.name}")
                    val chapters = database.syllabusDao().getChaptersForSubjectSync(sub.id)
                    for (ch in chapters) {
                        sb.appendLine("## Chapter: ${ch.name}")
                        val topics = database.syllabusDao().getTopicsForChapterSync(ch.id)
                        for (top in topics) {
                            val check = if (top.masteryState == "MASTERED") "[x]" else "[ ]"
                            sb.appendLine("- $check ${top.name} [${top.examRelevance}] [${top.masteryState}]")
                        }
                        sb.appendLine()
                    }
                }
            }

            CsvDatasetType.QUESTIONS -> {
                sb.appendLine("# 95OS Question Bank Compilation")
                sb.appendLine("> Offline blueprint questions and marking scheme")
                sb.appendLine()

                val questions = database.paperPilotDao().getAllQuestionsSync()
                val subjects = database.syllabusDao().getAllSubjectsSync().associateBy { it.id }
                val chapters = database.syllabusDao().getAllChaptersSync().associateBy { it.id }

                for (q in questions) {
                    val subName = subjects[q.subjectId]?.name ?: "General"
                    val chName = chapters[q.chapterId]?.name ?: "Core"
                    sb.appendLine("### Question: ${q.questionText}")
                    sb.appendLine("- Subject: $subName")
                    sb.appendLine("- Chapter: $chName")
                    sb.appendLine("- Marks: ${q.marks}")
                    sb.appendLine("- Difficulty: ${q.difficulty}")
                    sb.appendLine("- Type: ${q.questionType}")
                    sb.appendLine("- Answer: ${q.markingScheme}")
                    sb.appendLine()
                }
            }

            CsvDatasetType.RECALL_CARDS -> {
                sb.appendLine("# 95OS Active Recall Spaced Repetition Deck")
                sb.appendLine("> SuperMemo SM-2 Card Catalog")
                sb.appendLine()

                val cards = database.recallDao().getAllCardsSync()
                val subjects = database.syllabusDao().getAllSubjectsSync().associateBy { it.id }
                val chapters = database.syllabusDao().getAllChaptersSync().associateBy { it.id }

                for (c in cards) {
                    val subName = subjects[c.subjectId]?.name ?: "General"
                    val chName = chapters[c.chapterId]?.name ?: "Core"
                    sb.appendLine("## Card: ${c.prompt}")
                    sb.appendLine("- Back: ${c.expectedAnswer}")
                    sb.appendLine("- Subject: $subName")
                    sb.appendLine("- Chapter: $chName")
                    sb.appendLine()
                }
            }

            CsvDatasetType.MISTAKES -> {
                sb.appendLine("# 95OS Mistake Bank Remediation Sheet")
                sb.appendLine("> Active unmastered errors for targeted score recovery")
                sb.appendLine()

                val mistakes = database.mistakeDao().getAllMistakesSync().filter { !it.isResolved }
                val subjects = database.syllabusDao().getAllSubjectsSync().associateBy { it.id }
                val chapters = database.syllabusDao().getAllChaptersSync().associateBy { it.id }

                for (m in mistakes) {
                    val subName = subjects[m.subjectId]?.name ?: "General"
                    val chName = chapters[m.chapterId]?.name ?: "Core"
                    sb.appendLine("### Mistake: ${m.question}")
                    sb.appendLine("- Subject: $subName")
                    sb.appendLine("- Chapter: $chName")
                    sb.appendLine("- Student Answer: ${m.studentAnswer}")
                    sb.appendLine("- Correct Answer: ${m.correctAnswer}")
                    sb.appendLine("- Marks Lost: ${m.marksLost}")
                    sb.appendLine("- Category: ${m.lossCategory}")
                    sb.appendLine()
                }
            }

            else -> {
                sb.appendLine("# 95OS Export")
                sb.appendLine("Dataset: ${datasetType.displayName}")
            }
        }

        return sb.toString()
    }
}
