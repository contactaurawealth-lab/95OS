package com.os95.app.domain.engine

import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.PaperQuestionEntity
import com.os95.app.core.database.entity.QuestionBankEntity
import com.os95.app.domain.model.PaperValidationResult

class PaperValidator {

    fun validatePaper(
        paper: PaperEntity,
        questions: List<QuestionBankEntity>
    ): PaperValidationResult {
        val errors = mutableListOf<String>()

        if (questions.isEmpty()) {
            errors.add("Paper has no questions.")
            return PaperValidationResult(isValid = false, errors = errors)
        }

        // 1. Exact marks check
        val sumMarks = questions.sumOf { it.marks.toDouble() }.toFloat()
        if (Math.abs(sumMarks - paper.totalMarks) > 0.01f) {
            errors.add("Marks mismatch: Sum of question marks ($sumMarks) does not equal paper total marks (${paper.totalMarks}).")
        }

        // 2. Structural & Entity integrity
        for ((idx, q) in questions.withIndex()) {
            if (q.questionText.isBlank()) {
                errors.add("Question #${idx + 1} has blank text.")
            }
            if (q.marks <= 0f) {
                errors.add("Question #${idx + 1} has non-positive marks (${q.marks}).")
            }
            if (q.subjectId != paper.subjectId) {
                errors.add("Question #${idx + 1} does not belong to paper's subject.")
            }
        }

        // 3. Duplicates check
        val seenIds = mutableSetOf<String>()
        val seenTexts = mutableSetOf<String>()
        for (q in questions) {
            if (seenIds.contains(q.id)) {
                errors.add("Duplicate question ID in paper: ${q.id}")
            }
            seenIds.add(q.id)

            val normText = q.questionText.trim().lowercase()
            if (seenTexts.contains(normText)) {
                errors.add("Identical question content repeated in same paper: \"${q.questionText.take(30)}...\"")
            }
            seenTexts.add(normText)
        }

        return PaperValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    fun validatePaperQuestions(
        paper: PaperEntity,
        paperQuestions: List<PaperQuestionEntity>
    ): PaperValidationResult {
        val errors = mutableListOf<String>()

        if (paperQuestions.isEmpty()) {
            errors.add("Paper has no snapshot questions.")
            return PaperValidationResult(isValid = false, errors = errors)
        }

        val sumMarks = paperQuestions.sumOf { it.snapshotMarks.toDouble() }.toFloat()
        if (Math.abs(sumMarks - paper.totalMarks) > 0.01f) {
            errors.add("Marks mismatch: Sum of question marks ($sumMarks) does not equal paper total marks (${paper.totalMarks}).")
        }

        for ((idx, pq) in paperQuestions.withIndex()) {
            if (pq.snapshotQuestionText.isBlank()) {
                errors.add("Snapshot question #${idx + 1} has blank text.")
            }
            if (pq.snapshotMarks <= 0f) {
                errors.add("Snapshot question #${idx + 1} has non-positive marks (${pq.snapshotMarks}).")
            }
            if (pq.sectionName.isBlank()) {
                errors.add("Question #${idx + 1} is missing a section assignment.")
            }
        }

        return PaperValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}
