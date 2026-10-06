package com.os95.app.core.di

import android.content.Context
import com.os95.app.core.csv.DefaultUniversalCsvProcessor
import com.os95.app.core.csv.UniversalCsvProcessor
import com.os95.app.core.database.OS95Database
import com.os95.app.core.datastore.PreferencesManager
import com.os95.app.data.repository.OfflineMistakeRepository
import com.os95.app.data.repository.OfflinePaperRepository
import com.os95.app.data.repository.OfflineRecallRepository
import com.os95.app.data.repository.OfflineStudentRepository
import com.os95.app.data.repository.OfflineSyllabusRepository
import com.os95.app.domain.repository.MistakeRepository
import com.os95.app.domain.repository.PaperRepository
import com.os95.app.domain.repository.RecallRepository
import com.os95.app.domain.repository.StudentRepository
import com.os95.app.domain.repository.SyllabusRepository

class OS95AppContainer(context: Context) {
    val database: OS95Database by lazy {
        OS95Database.getInstance(context)
    }

    val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(context)
    }

    val syllabusRepository: SyllabusRepository by lazy {
        OfflineSyllabusRepository(database.syllabusDao())
    }

    val recallRepository: RecallRepository by lazy {
        OfflineRecallRepository(database.recallDao())
    }

    val paperRepository: PaperRepository by lazy {
        OfflinePaperRepository(
            dao = database.paperPilotDao(),
            syllabusDao = database.syllabusDao(),
            mistakeDao = database.mistakeDao()
        )
    }

    val mistakeRepository: MistakeRepository by lazy {
        OfflineMistakeRepository(database.mistakeDao())
    }

    val studentRepository: StudentRepository by lazy {
        OfflineStudentRepository(
            studentDao = database.studentDao(),
            preferencesDao = database.studyPreferencesDao(),
            sessionDao = database.studySessionDao()
        )
    }

    val marksRecoveryEngine: com.os95.app.domain.engine.MarksRecoveryEngine by lazy {
        com.os95.app.domain.engine.MarksRecoveryEngine()
    }

    val marksRecoveryRepository: com.os95.app.domain.repository.MarksRecoveryRepository by lazy {
        com.os95.app.data.repository.OfflineMarksRecoveryRepository(
            engine = marksRecoveryEngine,
            studentDao = database.studentDao(),
            syllabusDao = database.syllabusDao(),
            paperDao = database.paperPilotDao(),
            mistakeDao = database.mistakeDao(),
            recallDao = database.recallDao(),
            sessionDao = database.studySessionDao()
        )
    }

    val universalCsvEngine: com.os95.app.core.csv.UniversalCsvEngine by lazy {
        com.os95.app.core.csv.UniversalCsvEngine()
    }

    val csvProcessor: UniversalCsvProcessor by lazy {
        DefaultUniversalCsvProcessor(universalCsvEngine)
    }
}
