package com.os95.app.domain.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.domain.model.TopicRetentionRisk
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates printable physical revision documents (A4 PDF and Markdown)
 * for last-mile pre-exam review.
 * 100% offline, deterministic, zero network dependencies.
 */
class RevisionDocumentGenerator {

    companion object {
        const val PAGE_WIDTH = 595 // A4 pt
        const val PAGE_HEIGHT = 842 // A4 pt
        const val MARGIN_LEFT = 40f
        const val MARGIN_RIGHT = 40f
        const val MARGIN_TOP = 40f
        const val MARGIN_BOTTOM = 50f
        const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT
    }

    /**
     * Generates a clean Markdown Mistake Remediation Sheet suitable for
     * reading, printing, or copy-pasting into study journals.
     */
    fun generateMistakeRemediationMarkdown(
        mistakes: List<MistakeEntity>,
        subjectNames: Map<String, String>
    ): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateStr = dateFormat.format(Date())

        sb.appendLine("# 95OS Mistake Remediation & Recovery Sheet")
        sb.appendLine("> Date: $dateStr | Active Unmastered Errors: ${mistakes.size}")
        sb.appendLine("> Academic Goal: Eliminate careless & conceptual errors before exam hall.")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        if (mistakes.isEmpty()) {
            sb.appendLine("No unmastered mistakes logged! All previous errors have been resolved.")
            return sb.toString()
        }

        mistakes.forEachIndexed { idx, m ->
            val subName = subjectNames[m.subjectId] ?: "General"
            sb.appendLine("### [${idx + 1}] $subName — ${m.question}")
            sb.appendLine("- **Loss Category:** ${m.lossCategory.replace("_", " ")}")
            sb.appendLine("- **Marks Lost:** ${m.marksLost} marks | Missed Count: ${m.missedCount}")
            if (m.studentAnswer.isNotBlank()) {
                sb.appendLine("- **Student Error:** `${m.studentAnswer}`")
            }
            sb.appendLine("- **Correct Model Solution:** `${m.correctAnswer}`")
            sb.appendLine("- **Correction / Working Notes:** __________________________________________________")
            sb.appendLine()
        }

        return sb.toString()
    }

    /**
     * Generates an official, printable A4 PDF Mistake Remediation Document.
     */
    fun generateMistakeRemediationPdf(
        context: Context,
        mistakes: List<MistakeEntity>,
        subjectNames: Map<String, String>,
        targetFile: File? = null
    ): File {
        val outputFile = targetFile ?: File(
            context.cacheDir,
            "95OS_Mistake_Remediation_Sheet.pdf"
        )

        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(30, 30, 30)
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val badgePaint = Paint().apply {
            color = Color.rgb(180, 80, 0)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.8f
        }

        var y = MARGIN_TOP

        // Header
        canvas.drawText("95OS — MISTAKE REMEDIATION & RECOVERY SHEET", MARGIN_LEFT, y + 14f, titlePaint)
        y += 20f
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.US)
        canvas.drawText("Generated: ${dateFormat.format(Date())}  |  Unresolved Errors: ${mistakes.size}", MARGIN_LEFT, y + 10f, metaPaint)
        y += 18f
        canvas.drawLine(MARGIN_LEFT, y, MARGIN_LEFT + CONTENT_WIDTH, y, linePaint)
        y += 14f

        fun checkPageBreak(requiredHeight: Float) {
            if (y + requiredHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN_TOP
            }
        }

        if (mistakes.isEmpty()) {
            canvas.drawText("No unmastered mistakes logged! All exam errors have been remediated.", MARGIN_LEFT, y + 12f, bodyPaint)
        } else {
            mistakes.forEachIndexed { index, m ->
                val subName = subjectNames[m.subjectId] ?: "General"
                checkPageBreak(75f)

                canvas.drawText("[${index + 1}] $subName", MARGIN_LEFT, y + 10f, boldPaint)
                canvas.drawText("Lost: ${m.marksLost}m (${m.lossCategory.replace("_", " ")})", MARGIN_LEFT + 200f, y + 10f, badgePaint)
                y += 14f

                val qText = if (m.question.length > 85) m.question.take(82) + "..." else m.question
                canvas.drawText("Q: $qText", MARGIN_LEFT + 8f, y + 10f, bodyPaint)
                y += 13f

                if (m.studentAnswer.isNotBlank()) {
                    val sAns = if (m.studentAnswer.length > 80) m.studentAnswer.take(77) + "..." else m.studentAnswer
                    canvas.drawText("Student Error: $sAns", MARGIN_LEFT + 8f, y + 10f, metaPaint)
                    y += 13f
                }

                val cAns = if (m.correctAnswer.length > 80) m.correctAnswer.take(77) + "..." else m.correctAnswer
                canvas.drawText("Model Answer: $cAns", MARGIN_LEFT + 8f, y + 10f, boldPaint)
                y += 13f

                canvas.drawText("Correction: ____________________________________________________________________", MARGIN_LEFT + 8f, y + 10f, metaPaint)
                y += 16f
                canvas.drawLine(MARGIN_LEFT + 8f, y, MARGIN_LEFT + CONTENT_WIDTH, y, linePaint)
                y += 8f
            }
        }

        // Draw page footer
        canvas.drawText("Page $pageNumber  •  95OS Offline Academic Operating System", MARGIN_LEFT, PAGE_HEIGHT - 20f, metaPaint)
        pdfDocument.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    /**
     * Generates a printable Markdown Forgetting Radar Flash Sheet for high-decay recall cards.
     */
    fun generateForgettingFlashMarkdown(
        topics: List<TopicRetentionRisk>
    ): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sb.appendLine("# 95OS Forgetting Radar — Critical Review Flash Sheet")
        sb.appendLine("> Date: ${dateFormat.format(Date())} | Priority Topics: ${topics.size}")
        sb.appendLine("> Retention Risk Alert: High-decay concepts requiring immediate reinforcement.")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        topics.forEachIndexed { idx, t ->
            sb.appendLine("### [${idx + 1}] ${t.subjectName} — ${t.chapterName} / ${t.topicName} [${t.riskLevel.name}]")
            sb.appendLine("- **Risk Level:** ${t.riskLevel.label}")
            sb.appendLine("- **Recall Performance:** ${t.averageRecallScore.toInt()}%")
            sb.appendLine("- **Last Reviewed:** ${t.daysSinceLastReview} days ago")
            sb.appendLine("- **Recommended Action:** ${t.recommendedCardCount} cards due for active review")
            sb.appendLine()
        }

        return sb.toString()
    }
}
