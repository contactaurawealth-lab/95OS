package com.os95.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "students")
data class StudentProfileEntity(
    @PrimaryKey val id: String = "student_profile",
    val name: String,
    val targetPercentage: Float = 95.0f,
    val gradeLevel: String = "",
    val division: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_preferences")
data class StudyPreferencesEntity(
    @PrimaryKey val id: String = "study_preferences",
    val dailyTargetMinutes: Int = 120,
    val defaultExamDurationMinutes: Int = 90,
    val examModeLockdownEnabled: Boolean = true,
    val warningAtTenMinutes: Boolean = true
)

@Entity(
    tableName = "subjects",
    indices = [Index(value = ["name"], unique = true)]
)
data class SubjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorHex: String = "#B8781E",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class ChapterEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val name: String,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "topics",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chapterId"), Index("masteryState")]
)
data class TopicEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val chapterId: String,
    val name: String,
    val masteryState: String = "NOT_STARTED", // NOT_STARTED, LEARNING, REVISED, MASTERED
    val examRelevance: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val weaknessScore: Float = 0.0f,
    val lastRevisedAt: Long? = null,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "question_bank",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("subjectId"), Index("chapterId"), Index("topicId"), Index("difficulty")]
)
data class QuestionBankEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val chapterId: String,
    val topicId: String?,
    val questionText: String,
    val markingScheme: String = "",
    val marks: Float = 1.0f,
    val questionType: String = "SHORT_ANSWER",
    val difficulty: String = "MEDIUM",
    val timesTested: Int = 0,
    val timesFailed: Int = 0,
    val source: String = "",
    val lastUsedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "papers",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId"), Index("status")]
)
data class PaperEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subjectId: String,
    val totalMarks: Float = 100.0f,
    val durationMinutes: Int = 90,
    val status: String = "DRAFT", // DRAFT, READY, IN_PROGRESS, COMPLETED
    val instructions: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "paper_questions",
    foreignKeys = [
        ForeignKey(
            entity = PaperEntity::class,
            parentColumns = ["id"],
            childColumns = ["paperId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionBankEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("paperId"), Index("questionId")]
)
data class PaperQuestionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val paperId: String,
    val questionId: String? = null,
    val orderIndex: Int = 0,
    val sectionName: String = "Section A",
    val snapshotQuestionText: String = "",
    val snapshotMarks: Float = 1.0f,
    val snapshotQuestionType: String = "SHORT_ANSWER",
    val snapshotDifficulty: String = "MEDIUM",
    val snapshotAnswer: String = "",
    val snapshotChapterId: String = "",
    val snapshotTopicId: String? = null
)

@Entity(
    tableName = "exam_results",
    foreignKeys = [
        ForeignKey(
            entity = PaperEntity::class,
            parentColumns = ["id"],
            childColumns = ["paperId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("paperId")]
)
data class ExamResultEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val paperId: String,
    val marksObtained: Float,
    val totalMarks: Float,
    val timeTakenMinutes: Int,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "lost_marks",
    foreignKeys = [
        ForeignKey(
            entity = ExamResultEntity::class,
            parentColumns = ["id"],
            childColumns = ["examResultId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("examResultId"), Index("topicId"), Index("lossCategory")]
)
data class LostMarksEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val examResultId: String,
    val topicId: String?,
    val marksLost: Float,
    val lossCategory: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "mistakes",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("subjectId"), Index("chapterId"), Index("topicId"), Index("isResolved")]
)
data class MistakeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val chapterId: String,
    val topicId: String?,
    val question: String,
    val studentAnswer: String = "",
    val correctAnswer: String,
    val lossCategory: String = "CARELESS_MISTAKE",
    val marksLost: Float = 1.0f,
    val missedCount: Int = 1,
    val isResolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "recall_cards",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("subjectId"), Index("chapterId"), Index("topicId"), Index("dueDate")]
)
data class RecallCardEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val chapterId: String,
    val topicId: String?,
    val prompt: String,
    val expectedAnswer: String,
    val explanation: String = "",
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val repetitions: Int = 0,
    val dueDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "recall_reviews",
    foreignKeys = [
        ForeignKey(
            entity = RecallCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cardId")]
)
data class RecallReviewEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val cardId: String,
    val rating: Int,
    val reviewTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "study_sessions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class StudySessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val chapterId: String? = null,
    val durationMinutes: Int,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "formulas",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId"), Index("chapterId"), Index("isBookmarked")]
)
data class FormulaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val chapterId: String,
    val topicId: String? = null,
    val title: String,
    val expression: String,
    val explanation: String = "",
    val examRelevance: String = "HIGH",
    val isBookmarked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

