package com.os95.app.domain.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

class ExamPaperPdfGenerator(
    private val formatter: PrintableExamPaperFormatter = PrintableExamPaperFormatter()
) {

    companion object {
        const val PAGE_WIDTH = 595 // A4 standard pt
        const val PAGE_HEIGHT = 842 // A4 standard pt
        const val MARGIN_LEFT = 40f
        const val MARGIN_RIGHT = 40f
        const val MARGIN_TOP = 40f
        const val MARGIN_BOTTOM = 50f
        const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT
    }

    fun generatePdfFile(
        context: Context,
        formattedPaper: FormattedExamPaper,
        targetFile: File? = null
    ): File {
        val outputFile = targetFile ?: File(
            context.cacheDir,
            "95OS_${formattedPaper.title.replace("\\s+".toRegex(), "_")}_Exam.pdf"
        )

        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldHeaderPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val sectionPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val marksPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var currentY = MARGIN_TOP

        fun checkPageBreak(requiredHeight: Float) {
            if (currentY + requiredHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
                // Draw footer for current page
                val footerPaint = Paint().apply {
                    color = Color.GRAY
                    textSize = 8.5f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.drawText("Page $pageNumber", PAGE_WIDTH / 2f, PAGE_HEIGHT - 25f, footerPaint)

                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = MARGIN_TOP

                // Running header on subsequent pages
                canvas.drawText("${formattedPaper.title} • ${formattedPaper.subjectName}", MARGIN_LEFT, currentY, headerPaint)
                canvas.drawLine(MARGIN_LEFT, currentY + 5f, PAGE_WIDTH - MARGIN_RIGHT, currentY + 5f, linePaint)
                currentY += 24f
            }
        }

        // --- Cover Header (Page 1) ---
        canvas.drawText("95OS — THE OFFLINE EXAM OPERATING SYSTEM", MARGIN_LEFT, currentY, headerPaint)
        currentY += 18f

        canvas.drawText(formattedPaper.title.uppercase(), MARGIN_LEFT, currentY, titlePaint)
        currentY += 15f

        canvas.drawLine(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY, linePaint)
        currentY += 16f

        canvas.drawText("Student Name: _________________________________", MARGIN_LEFT, currentY, headerPaint)
        canvas.drawText("Date: ________________________", PAGE_WIDTH - MARGIN_RIGHT - 180f, currentY, headerPaint)
        currentY += 16f

        canvas.drawText("Subject: ${formattedPaper.subjectName}", MARGIN_LEFT, currentY, boldHeaderPaint)
        canvas.drawText("Time Allowed: ${formattedPaper.durationMinutes} Minutes", MARGIN_LEFT + 220f, currentY, headerPaint)
        canvas.drawText("Max Marks: ${formattedPaper.maxMarks.toInt()}", PAGE_WIDTH - MARGIN_RIGHT - 100f, currentY, boldHeaderPaint)
        currentY += 16f

        canvas.drawLine(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY, linePaint)
        currentY += 16f

        // --- Instructions ---
        canvas.drawText("GENERAL INSTRUCTIONS:", MARGIN_LEFT, currentY, boldHeaderPaint)
        currentY += 14f

        for (instruction in formattedPaper.instructions) {
            checkPageBreak(14f)
            canvas.drawText(instruction, MARGIN_LEFT + 8f, currentY, headerPaint)
            currentY += 13f
        }

        currentY += 8f
        canvas.drawLine(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY, linePaint)
        currentY += 18f

        // --- Sections & Questions ---
        for (section in formattedPaper.sections) {
            checkPageBreak(30f)
            canvas.drawText("${section.title.uppercase()} — ${section.description}", MARGIN_LEFT, currentY, sectionPaint)
            currentY += 16f

            for (q in section.questions) {
                val qPrefix = "Q${q.questionNumber}. "
                val prefixWidth = bodyPaint.measureText(qPrefix)
                val wrappedLines = wrapText(q.questionText, bodyPaint, CONTENT_WIDTH - prefixWidth - 55f)
                val questionHeight = wrappedLines.size * 14f + 10f

                checkPageBreak(questionHeight)

                // Draw Q prefix
                canvas.drawText(qPrefix, MARGIN_LEFT, currentY, boldHeaderPaint)

                // Draw wrapped lines
                var lineY = currentY
                for (line in wrappedLines) {
                    canvas.drawText(line, MARGIN_LEFT + prefixWidth, lineY, bodyPaint)
                    lineY += 13f
                }

                // Draw marks on right margin of the first line
                val markStr = if (q.marks == 1.0f) "[1m]" else "[${q.marks.toInt()}m]"
                canvas.drawText(markStr, PAGE_WIDTH - MARGIN_RIGHT, currentY, marksPaint)

                currentY = lineY + 8f
            }
            currentY += 8f
        }

        // Draw final page footer
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Page $pageNumber", PAGE_WIDTH / 2f, PAGE_HEIGHT - 25f, footerPaint)

        pdfDocument.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split("\\s+".toRegex())
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(candidate) <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                } else {
                    // Single word exceeds width, force split
                    lines.add(word)
                    currentLine = StringBuilder()
                }
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
}
