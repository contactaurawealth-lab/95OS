package com.os95.app.domain.engine

import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.QuestionBankEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.domain.model.GeneratedPaper
import com.os95.app.domain.model.PaperBlueprintRequest
import com.os95.app.domain.model.PaperDifficultyMode
import com.os95.app.domain.model.PaperGenerationResult
import com.os95.app.domain.model.PaperSection
import com.os95.app.domain.model.RepetitionPolicy
import java.util.UUID

class PaperGenerationEngine {

    fun generatePaper(
        request: PaperBlueprintRequest,
        availableQuestions: List<QuestionBankEntity>,
        chapters: List<ChapterEntity> = emptyList(),
        topics: List<TopicEntity> = emptyList()
    ): PaperGenerationResult {
        // Step 1: Filter candidates matching syllabus criteria
        val chapterMap = chapters.associateBy { it.id }
        val topicMap = topics.associateBy { it.id }

        val eligibleQuestions = availableQuestions.filter { q ->
            val matchSubject = q.subjectId == request.subjectId
            val matchChapter = request.chapterIds.isEmpty() || request.chapterIds.contains(q.chapterId)
            val matchTopic = request.topicIds.isEmpty() || (q.topicId != null && request.topicIds.contains(q.topicId))
            val matchType = request.allowedQuestionTypes.isEmpty() ||
                request.allowedQuestionTypes.any { it.equals(q.questionType, ignoreCase = true) }

            val matchRepetition = when (request.repetitionPolicy) {
                RepetitionPolicy.STRICTLY_NEW -> q.timesTested == 0
                RepetitionPolicy.AVOID_RECENT, RepetitionPolicy.ALLOW_REPETITION -> true
            }

            matchSubject && matchChapter && matchTopic && matchType && matchRepetition
        }

        if (eligibleQuestions.isEmpty()) {
            return PaperGenerationResult.Failure(
                reason = "No questions in the Question Bank match the selected subject, chapters, and question types.",
                recoverySuggestions = listOf(
                    "Include all chapters in the subject",
                    "Allow all question types (MCQ, Short Answer, Long Answer)",
                    "Switch repetition policy to 'Allow repetition'",
                    "Import new questions via CSV"
                )
            )
        }

        val totalAvailableMarks = eligibleQuestions.sumOf { it.marks.toDouble() }.toFloat()
        if (totalAvailableMarks < request.totalMarks) {
            return PaperGenerationResult.Failure(
                reason = "A ${request.totalMarks.toInt()}-mark paper cannot be generated with the selected constraints because the question bank contains only ${totalAvailableMarks.toInt()} matching marks.",
                recoverySuggestions = listOf(
                    "Reduce requested paper marks to ${totalAvailableMarks.toInt()} or lower",
                    "Select additional chapters to widen the question pool",
                    "Allow all question types",
                    "Import more questions into Question Bank"
                )
            )
        }

        // Step 2: Score candidates for prioritization
        val weakTopicIds = topics.filter { it.weaknessScore > 0.2f }.map { it.id }.toSet()
        val weakChapterIds = mutableSetOf<String>()
        for (tp in topics) {
            if (tp.weaknessScore > 0.2f) {
                weakChapterIds.add(tp.chapterId)
            }
        }

        var extraWeakIncluded = false
        val identifiedWeakChapterNames = mutableSetOf<String>()

        val scoredCandidates = eligibleQuestions.map { q ->
            var score = 100.0f

            // Adaptive Weak Topic Weighting
            if (request.adaptiveWeakTopicWeighting) {
                if (q.topicId != null && weakTopicIds.contains(q.topicId)) {
                    score += 80.0f
                    extraWeakIncluded = true
                    chapterMap[q.chapterId]?.let { identifiedWeakChapterNames.add(it.name) }
                } else if (weakChapterIds.contains(q.chapterId)) {
                    score += 40.0f
                    extraWeakIncluded = true
                    chapterMap[q.chapterId]?.let { identifiedWeakChapterNames.add(it.name) }
                }
            }

            // Repetition / Recency Penalty
            if (request.repetitionPolicy == RepetitionPolicy.AVOID_RECENT) {
                score -= (q.timesTested * 20.0f)
                val lastUsed = q.lastUsedAt
                if (lastUsed != null) {
                    val daysAgo = (System.currentTimeMillis() - lastUsed) / (1000 * 60 * 60 * 24)
                    if (daysAgo < 7) {
                        score -= 30.0f
                    }
                }
            }

            // Difficulty Alignment
            val diffNorm = q.difficulty.trim().uppercase()
            when (request.difficultyMode) {
                PaperDifficultyMode.EASY -> {
                    if (diffNorm == "EASY") score += 50f
                    if (diffNorm == "MEDIUM") score += 20f
                }
                PaperDifficultyMode.MEDIUM -> {
                    if (diffNorm == "MEDIUM") score += 50f
                    if (diffNorm == "HARD") score += 20f
                }
                PaperDifficultyMode.HARD -> {
                    if (diffNorm == "HARD") score += 50f
                    if (diffNorm == "MEDIUM") score += 20f
                }
                PaperDifficultyMode.MIXED -> {
                    // Balance all
                    score += 10f
                }
            }

            ScoredQuestion(question = q, priorityScore = score)
        }.sortedByDescending { it.priorityScore }

        // Step 3: Exact-Marks Constraint Satisfaction Knapsack Solver
        val selectedQuestions = findExactMarksSubset(scoredCandidates, request.totalMarks)

        if (selectedQuestions == null || selectedQuestions.sumOf { it.marks.toDouble() }.toFloat() != request.totalMarks) {
            return PaperGenerationResult.Failure(
                reason = "A ${request.totalMarks.toInt()}-mark paper cannot be mathematically formed with the exact mark combinations available in the question bank.",
                recoverySuggestions = listOf(
                    "Try standard mark increments (e.g. 20, 40, 50, 80)",
                    "Select additional chapters or include more 1m/2m/5m questions",
                    "Add diverse question types to allow exact mark combinations",
                    "Import questions with varying mark schemes"
                )
            )
        }

        // Step 4: Section Partitioning
        val sections = partitionIntoSections(selectedQuestions)

        // Step 5: Metadata & Rationale
        val chapterCoverage = mutableMapOf<String, Float>()
        for (q in selectedQuestions) {
            chapterCoverage[q.chapterId] = (chapterCoverage[q.chapterId] ?: 0f) + q.marks
        }

        val difficultyBreakdown = mutableMapOf<String, Float>()
        for (q in selectedQuestions) {
            val d = q.difficulty.uppercase()
            difficultyBreakdown[d] = (difficultyBreakdown[d] ?: 0f) + q.marks
        }

        val rationale = if (extraWeakIncluded && identifiedWeakChapterNames.isNotEmpty()) {
            "Extra ${identifiedWeakChapterNames.joinToString(", ")} questions included because this is currently an area with recorded mark loss."
        } else null

        val paper = PaperEntity(
            id = UUID.randomUUID().toString(),
            title = request.title,
            subjectId = request.subjectId,
            totalMarks = request.totalMarks,
            durationMinutes = request.durationMinutes,
            status = "READY"
        )

        val generatedPaper = GeneratedPaper(
            paper = paper,
            sections = sections,
            questions = selectedQuestions,
            chapterMarksCoverage = chapterCoverage,
            difficultyBreakdown = difficultyBreakdown,
            adaptiveRationale = rationale
        )

        return PaperGenerationResult.Success(generatedPaper)
    }

    private data class ScoredQuestion(
        val question: QuestionBankEntity,
        val priorityScore: Float
    )

    private fun findExactMarksSubset(
        candidates: List<ScoredQuestion>,
        targetMarks: Float
    ): List<QuestionBankEntity>? {
        val target = Math.round(targetMarks * 10f) // Scale by 10 for integer arithmetic (handles 0.5, 1.0, 2.5 marks)
        val items = candidates.map { Pair(it.question, Math.round(it.question.marks * 10f)) }

        val result = mutableListOf<QuestionBankEntity>()
        var found = false

        fun search(index: Int, currentMarks: Int, selected: MutableList<QuestionBankEntity>) {
            if (found) return
            if (currentMarks == target) {
                result.addAll(selected)
                found = true
                return
            }
            if (currentMarks > target || index >= items.size) return

            // Branch 1: Include current candidate (prioritized order)
            val (q, marks) = items[index]
            if (currentMarks + marks <= target) {
                selected.add(q)
                search(index + 1, currentMarks + marks, selected)
                selected.removeAt(selected.size - 1)
            }

            // Branch 2: Exclude current candidate
            if (!found) {
                search(index + 1, currentMarks, selected)
            }
        }

        search(0, 0, mutableListOf())
        return if (found) result else null
    }

    private fun partitionIntoSections(questions: List<QuestionBankEntity>): List<PaperSection> {
        // Group by marks / question type:
        // Section A: 1 mark / MCQ / True-False
        // Section B: 2-3 marks / Short Answer
        // Section C: 4-5 marks / Long Answer / Numerical
        // Section D: >5 marks / Case Study / Comprehensive
        val secA = mutableListOf<QuestionBankEntity>()
        val secB = mutableListOf<QuestionBankEntity>()
        val secC = mutableListOf<QuestionBankEntity>()
        val secD = mutableListOf<QuestionBankEntity>()

        for (q in questions) {
            when {
                q.marks <= 1.5f || q.questionType.equals("MCQ", ignoreCase = true) -> secA.add(q)
                q.marks <= 3.5f -> secB.add(q)
                q.marks <= 5.5f -> secC.add(q)
                else -> secD.add(q)
            }
        }

        val sections = mutableListOf<PaperSection>()
        var secLetter = 'A'

        if (secA.isNotEmpty()) {
            val total = secA.sumOf { it.marks.toDouble() }.toFloat()
            sections.add(
                PaperSection(
                    name = "Section ${secLetter++}",
                    description = "Objective Questions (${total.toInt()} Marks)",
                    questions = secA,
                    sectionMarks = total
                )
            )
        }

        if (secB.isNotEmpty()) {
            val total = secB.sumOf { it.marks.toDouble() }.toFloat()
            sections.add(
                PaperSection(
                    name = "Section ${secLetter++}",
                    description = "Short Answer Questions (${total.toInt()} Marks)",
                    questions = secB,
                    sectionMarks = total
                )
            )
        }

        if (secC.isNotEmpty()) {
            val total = secC.sumOf { it.marks.toDouble() }.toFloat()
            sections.add(
                PaperSection(
                    name = "Section ${secLetter++}",
                    description = "Long Answer Questions (${total.toInt()} Marks)",
                    questions = secC,
                    sectionMarks = total
                )
            )
        }

        if (secD.isNotEmpty()) {
            val total = secD.sumOf { it.marks.toDouble() }.toFloat()
            sections.add(
                PaperSection(
                    name = "Section ${secLetter++}",
                    description = "Advanced / Case Study Questions (${total.toInt()} Marks)",
                    questions = secD,
                    sectionMarks = total
                )
            )
        }

        // If only 1 group was formed or all fell into one bucket, label cleanly
        if (sections.isEmpty() && questions.isNotEmpty()) {
            val total = questions.sumOf { it.marks.toDouble() }.toFloat()
            sections.add(
                PaperSection(
                    name = "Section A",
                    description = "General Questions (${total.toInt()} Marks)",
                    questions = questions,
                    sectionMarks = total
                )
            )
        }

        return sections
    }
}
