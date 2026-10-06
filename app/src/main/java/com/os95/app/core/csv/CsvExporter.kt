package com.os95.app.core.csv

import java.util.Locale
import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.ExamResultEntity
import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.QuestionBankEntity
import com.os95.app.core.database.entity.RecallCardEntity
import com.os95.app.core.database.entity.StudySessionEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity

class CsvExporter {

    fun formatValue(value: String): String {
        val trimmed = value.trim()
        val needsQuotes = trimmed.contains(',') || trimmed.contains('\"') || trimmed.contains('\n') || trimmed.contains('\r')
        return if (needsQuotes) {
            "\"" + trimmed.replace("\"", "\"\"") + "\""
        } else {
            trimmed
        }
    }

    fun formatRow(fields: List<String>): String {
        return fields.joinToString(",") { formatValue(it) }
    }

    fun getTemplate(datasetType: CsvDatasetType): String {
        val sb = StringBuilder()
        when (datasetType) {
            CsvDatasetType.QUESTIONS -> {
                sb.append("question_id,subject,chapter,topic,question_text,question_type,difficulty,marks,answer,source\n")
                sb.append("Q001,Mathematics,Algebra,Linear Equations,\"Solve 2x + 5 = 15 for x.\",SHORT_ANSWER,EASY,2.0,\"x = 5\",School\n")
                sb.append("Q002,Physics,Mechanics,Newton's Laws,\"State and prove Newton's second law of motion.\",LONG_ANSWER,MEDIUM,5.0,\"F = dp/dt = ma\",Textbook\n")
            }
            CsvDatasetType.SUBJECTS -> {
                sb.append("subject_id,name,color\n")
                sb.append("SUB-01,Mathematics,#B8781E\n")
                sb.append("SUB-02,Physics,#2E7D32\n")
            }
            CsvDatasetType.CHAPTERS -> {
                sb.append("chapter_id,subject,name,order\n")
                sb.append("CHP-01,Mathematics,Algebra,1\n")
                sb.append("CHP-02,Mathematics,Calculus,2\n")
            }
            CsvDatasetType.TOPICS -> {
                sb.append("topic_id,chapter,name,order,mastery,relevance\n")
                sb.append("TOP-01,Algebra,Linear Equations,1,LEARNING,HIGH\n")
                sb.append("TOP-02,Algebra,Quadratic Equations,2,MASTERED,MEDIUM\n")
            }
            CsvDatasetType.SYLLABUS -> {
                sb.append("subject,chapter,topic,mastery,relevance\n")
                sb.append("Mathematics,Calculus,Derivatives,LEARNING,HIGH\n")
                sb.append("Physics,Mechanics,Newton's Laws,MASTERED,HIGH\n")
            }
            CsvDatasetType.RECALL_CARDS -> {
                sb.append("subject,chapter,topic,prompt,expected_answer,explanation\n")
                sb.append("Physics,Mechanics,Newton's Laws,\"What is the SI unit of force?\",Newton,\"1 N = 1 kg·m/s²\"\n")
            }
            CsvDatasetType.PAPERS -> {
                sb.append("paper_id,title,subject,total_marks,duration_minutes,status\n")
                sb.append("PAP-01,\"Mathematics Mock 1\",Mathematics,50.0,60,READY\n")
            }
            CsvDatasetType.RESULTS -> {
                sb.append("result_id,paper_id,subject,marks_obtained,total_marks,percentage,time_taken,date\n")
                sb.append("RES-01,PAP-01,Mathematics,43.0,50.0,86.0,55,2026-10-06\n")
            }
            CsvDatasetType.MISTAKES -> {
                sb.append("mistake_id,result_id,question_id,subject,chapter,topic,category,marks_lost,question,correct_answer\n")
                sb.append("MST-01,,Q001,Mathematics,Algebra,Linear Equations,CARELESS_MISTAKE,1.0,\"Solve 2x + 5 = 15\",\"x = 5\"\n")
            }
            CsvDatasetType.STUDY_SESSIONS -> {
                sb.append("subject,chapter,duration_minutes\n")
                sb.append("Mathematics,Calculus,60\n")
            }
        }
        return sb.toString()
    }

    fun exportQuestions(
        questions: List<QuestionBankEntity>,
        subjectMap: Map<String, String>,
        chapterMap: Map<String, String>,
        topicMap: Map<String, String>
    ): String {
        val sb = StringBuilder()
        sb.append("question_id,subject,chapter,topic,question_text,question_type,difficulty,marks,answer,source,usage_count,last_used\n")
        for (q in questions) {
            val subName = subjectMap[q.subjectId] ?: q.subjectId
            val chpName = chapterMap[q.chapterId] ?: q.chapterId
            val topName = q.topicId?.let { topicMap[it] ?: it } ?: ""
            sb.append(
                formatRow(
                    listOf(
                        q.id,
                        subName,
                        chpName,
                        topName,
                        q.questionText,
                        q.questionType,
                        q.difficulty,
                        q.marks.toString(),
                        q.markingScheme,
                        q.source,
                        q.timesTested.toString(),
                        q.lastUsedAt?.toString() ?: ""
                    )
                )
            ).append("\n")
        }
        return sb.toString()
    }

    fun exportResults(
        results: List<ExamResultEntity>,
        paperMap: Map<String, PaperEntity>,
        subjectMap: Map<String, String>
    ): String {
        val sb = StringBuilder()
        sb.append("result_id,paper_id,subject,marks_obtained,total_marks,percentage,time_taken,date\n")
        for (r in results) {
            val paper = paperMap[r.paperId]
            val subName = paper?.let { subjectMap[it.subjectId] ?: it.subjectId } ?: ""
            val pct = if (r.totalMarks > 0f) (r.marksObtained / r.totalMarks * 100f) else 0f
            sb.append(
                formatRow(
                    listOf(
                        r.id,
                        paper?.title ?: r.paperId,
                        subName,
                        r.marksObtained.toString(),
                        r.totalMarks.toString(),
                        String.format(Locale.US, "%.1f", pct),
                        r.timeTakenMinutes.toString(),
                        r.completedAt.toString()
                    )
                )
            ).append("\n")
        }
        return sb.toString()
    }

    fun exportMistakes(
        mistakes: List<MistakeEntity>,
        subjectMap: Map<String, String>,
        chapterMap: Map<String, String>,
        topicMap: Map<String, String>
    ): String {
        val sb = StringBuilder()
        sb.append("mistake_id,result_id,question_id,subject,chapter,topic,category,marks_lost,question,correct_answer\n")
        for (m in mistakes) {
            val subName = subjectMap[m.subjectId] ?: m.subjectId
            val chpName = chapterMap[m.chapterId] ?: m.chapterId
            val topName = m.topicId?.let { topicMap[it] ?: it } ?: ""
            sb.append(
                formatRow(
                    listOf(
                        m.id,
                        "",
                        "",
                        subName,
                        chpName,
                        topName,
                        m.lossCategory,
                        m.marksLost.toString(),
                        m.question,
                        m.correctAnswer
                    )
                )
            ).append("\n")
        }
        return sb.toString()
    }

    fun exportSyllabus(
        subjects: List<SubjectEntity>,
        chapters: List<ChapterEntity>,
        topics: List<TopicEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("subject,chapter,topic,mastery,relevance\n")
        val subMap = subjects.associateBy { it.id }
        val chpMap = chapters.associateBy { it.id }

        for (topic in topics) {
            val chapter = chpMap[topic.chapterId]
            if (chapter != null) {
                val subject = subMap[chapter.subjectId]
                if (subject != null) {
                    sb.append(
                        formatRow(
                            listOf(
                                subject.name,
                                chapter.name,
                                topic.name,
                                topic.masteryState,
                                topic.examRelevance
                            )
                        )
                    ).append("\n")
                }
            }
        }
        return sb.toString()
    }

    fun exportRecallCards(
        cards: List<RecallCardEntity>,
        subjectMap: Map<String, String>,
        chapterMap: Map<String, String>,
        topicMap: Map<String, String>
    ): String {
        val sb = StringBuilder()
        sb.append("subject,chapter,topic,prompt,expected_answer,explanation\n")
        for (c in cards) {
            val subName = subjectMap[c.subjectId] ?: c.subjectId
            val chpName = chapterMap[c.chapterId] ?: c.chapterId
            val topName = c.topicId?.let { topicMap[it] ?: it } ?: ""
            sb.append(
                formatRow(
                    listOf(
                        subName,
                        chpName,
                        topName,
                        c.prompt,
                        c.expectedAnswer,
                        c.explanation
                    )
                )
            ).append("\n")
        }
        return sb.toString()
    }
}
