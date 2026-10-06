package com.os95.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.os95.app.core.database.dao.MistakeDao
import com.os95.app.core.database.dao.PaperPilotDao
import com.os95.app.core.database.dao.RecallDao
import com.os95.app.core.database.dao.StudentDao
import com.os95.app.core.database.dao.StudyPreferencesDao
import com.os95.app.core.database.dao.StudySessionDao
import com.os95.app.core.database.dao.SyllabusDao
import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.ExamResultEntity
import com.os95.app.core.database.entity.LostMarksEntity
import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.PaperQuestionEntity
import com.os95.app.core.database.entity.QuestionBankEntity
import com.os95.app.core.database.entity.RecallCardEntity
import com.os95.app.core.database.entity.RecallReviewEntity
import com.os95.app.core.database.entity.StudentProfileEntity
import com.os95.app.core.database.entity.StudyPreferencesEntity
import com.os95.app.core.database.entity.StudySessionEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity

interface DatabaseProvider {
    fun studentDao(): StudentDao
    fun studyPreferencesDao(): StudyPreferencesDao
    fun syllabusDao(): SyllabusDao
    fun paperPilotDao(): PaperPilotDao
    fun mistakeDao(): MistakeDao
    fun recallDao(): RecallDao
    fun studySessionDao(): StudySessionDao
    suspend fun <R> runInTransaction(block: suspend () -> R): R
}

@Database(
    entities = [
        StudentProfileEntity::class,
        StudyPreferencesEntity::class,
        SubjectEntity::class,
        ChapterEntity::class,
        TopicEntity::class,
        QuestionBankEntity::class,
        PaperEntity::class,
        PaperQuestionEntity::class,
        ExamResultEntity::class,
        LostMarksEntity::class,
        MistakeEntity::class,
        RecallCardEntity::class,
        RecallReviewEntity::class,
        StudySessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OS95Database : RoomDatabase(), DatabaseProvider {
    abstract override fun studentDao(): StudentDao
    abstract override fun studyPreferencesDao(): StudyPreferencesDao
    abstract override fun syllabusDao(): SyllabusDao
    abstract override fun paperPilotDao(): PaperPilotDao
    abstract override fun mistakeDao(): MistakeDao
    abstract override fun recallDao(): RecallDao
    abstract override fun studySessionDao(): StudySessionDao

    override suspend fun <R> runInTransaction(block: suspend () -> R): R = withTransaction(block)

    companion object {
        @Volatile
        private var INSTANCE: OS95Database? = null

        fun getInstance(context: Context): OS95Database {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OS95Database::class.java,
                    "95os_offline.db"
                )
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("PRAGMA foreign_keys = ON;")
                    }
                })
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}
