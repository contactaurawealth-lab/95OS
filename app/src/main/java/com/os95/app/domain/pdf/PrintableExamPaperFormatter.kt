package com.os95.app.domain.pdf

import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.PaperQuestionEntity
import com.os95.app.domain.model.PaperSection

data class FormattedExamPaper(
    val title: String,
    val subjectName: String,
    val maxMarks: Float,
    val durationMinutes: Int,
    val instructions: List<String>,
    val sections: List<FormattedSection>,
    val totalQuestions: Int
)

data class FormattedSection(
    val title: String,
    val description: String,
    val totalMarks: Float,
    val questions: List<FormattedQuestion>
)

data class FormattedQuestion(
    val questionNumber: Int,
    val questionText: String,
    val marks: Float,
    val questionType: String,
    val markingScheme: String = ""
)

class PrintableExamPaperFormatter {

    fun format(
        paper: PaperEntity,
        subjectName: String,
        sections: List<PaperSection>
    ): FormattedExamPaper {
        var qNum = 1
        val formattedSections = mutableListOf<FormattedSection>()
        var totalQ = 0

        for (sec in sections) {
            val qList = mutableListOf<FormattedQuestion>()
            for (q in sec.questions) {
                qList.add(
                    FormattedQuestion(
                        questionNumber = qNum++,
                        questionText = q.questionText,
                        marks = q.marks,
                        questionType = q.questionType,
                        markingScheme = q.markingScheme
                    )
                )
                totalQ++
            }
            formattedSections.add(
                FormattedSection(
                    title = sec.name,
                    description = sec.description,
                    totalMarks = sec.sectionMarks,
                    questions = qList
                )
            )
        }

        val instructions = listOf(
            "1. Read all questions carefully before answering.",
            "2. All questions are compulsory unless internal choice is specified.",
            "3. Marks for each question are indicated against it.",
            "4. Write answers clearly with proper question numbering.",
            "5. Diagrams, if any, should be drawn neatly with labels."
        )

        return FormattedExamPaper(
            title = paper.title,
            subjectName = subjectName,
            maxMarks = paper.totalMarks,
            durationMinutes = paper.durationMinutes,
            instructions = instructions,
            sections = formattedSections,
            totalQuestions = totalQ
        )
    }

    fun formatFromSnapshots(
        paper: PaperEntity,
        subjectName: String,
        snapshotQuestions: List<PaperQuestionEntity>
    ): FormattedExamPaper {
        val groupedBySection = snapshotQuestions.groupBy { it.sectionName }
        var qNum = 1
        val formattedSections = mutableListOf<FormattedSection>()
        var totalQ = 0

        for ((secName, questions) in groupedBySection) {
            val qList = mutableListOf<FormattedQuestion>()
            val secTotal = questions.sumOf { it.snapshotMarks.toDouble() }.toFloat()
            for (q in questions) {
                qList.add(
                    FormattedQuestion(
                        questionNumber = qNum++,
                        questionText = q.snapshotQuestionText,
                        marks = q.snapshotMarks,
                        questionType = q.snapshotQuestionType,
                        markingScheme = q.snapshotAnswer
                    )
                )
                totalQ++
            }
            formattedSections.add(
                FormattedSection(
                    title = secName,
                    description = "(${secTotal.toInt()} Marks)",
                    totalMarks = secTotal,
                    questions = qList
                )
            )
        }

        val instructions = listOf(
            "1. Read all questions carefully before answering.",
            "2. All questions are compulsory.",
            "3. Marks for each question are indicated on the right.",
            "4. Write neatly and preserve standard exam discipline."
        )

        return FormattedExamPaper(
            title = paper.title,
            subjectName = subjectName,
            maxMarks = paper.totalMarks,
            durationMinutes = paper.durationMinutes,
            instructions = instructions,
            sections = formattedSections,
            totalQuestions = totalQ
        )
    }

    fun renderToPrintableText(formatted: FormattedExamPaper): String {
        val sb = StringBuilder()
        sb.append("================================================================================\n")
        sb.append("                                95OS EXAMINATION\n")
        sb.append("                          ${formatted.title.uppercase()}\n")
        sb.append("================================================================================\n")
        sb.append("Candidate Name: ____________________________        Date: _____________________\n")
        sb.append("Subject: ${formatted.subjectName.padEnd(35)} Time Allowed: ${formatted.durationMinutes} Minutes\n")
        sb.append("Maximum Marks: ${formatted.maxMarks.toInt()} Marks                          Total Questions: ${formatted.totalQuestions}\n")
        sb.append("--------------------------------------------------------------------------------\n")
        sb.append("GENERAL INSTRUCTIONS:\n")
        for (inst in formatted.instructions) {
            sb.append("  $inst\n")
        }
        sb.append("--------------------------------------------------------------------------------\n\n")

        for (sec in formatted.sections) {
            sb.append("--- ${sec.title.uppercase()}: ${sec.description} ---\n\n")
            for (q in sec.questions) {
                val markLabel = if (q.marks == 1.0f) "[1 Mark]" else "[${q.marks.toInt()} Marks]"
                sb.append("Q${q.questionNumber}. ${q.questionText}\n")
                sb.append("     $markLabel\n\n")
            }
        }
        sb.append("============================== END OF QUESTION PAPER ===========================\n")
        return sb.toString()
    }
}
