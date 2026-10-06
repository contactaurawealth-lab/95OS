package com.os95.app.data.repository

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
import com.os95.app.core.database.entity.QuestionBankEntity
import com.os95.app.core.database.entity.RecallCardEntity
import com.os95.app.core.database.entity.RecallReviewEntity
import com.os95.app.core.database.entity.StudentProfileEntity
import com.os95.app.core.database.entity.StudyPreferencesEntity
import com.os95.app.core.database.entity.StudySessionEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.domain.engine.SM2Engine
import com.os95.app.domain.model.RecallRating
import com.os95.app.domain.repository.MistakeRepository
import com.os95.app.domain.repository.PaperRepository
import com.os95.app.domain.repository.RecallRepository
import com.os95.app.domain.repository.StudentRepository
import com.os95.app.domain.repository.SyllabusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class OfflineSyllabusRepository(
    private val dao: SyllabusDao
) : SyllabusRepository {

    override fun getAllSubjects(): Flow<List<SubjectEntity>> = dao.getAllSubjects()

    override suspend fun getSubjectById(id: String): SubjectEntity? = dao.getSubjectById(id)

    override suspend fun createSubject(name: String, colorHex: String): SubjectEntity {
        val subject = SubjectEntity(name = name, colorHex = colorHex)
        dao.insertSubject(subject)
        return subject
    }

    override suspend fun deleteSubject(subject: SubjectEntity) = dao.deleteSubject(subject)

    override fun getChaptersForSubject(subjectId: String): Flow<List<ChapterEntity>> =
        dao.getChaptersForSubject(subjectId)

    override suspend fun createChapter(subjectId: String, name: String): ChapterEntity {
        val chapter = ChapterEntity(subjectId = subjectId, name = name)
        dao.insertChapter(chapter)
        return chapter
    }

    override fun getTopicsForChapter(chapterId: String): Flow<List<TopicEntity>> =
        dao.getTopicsForChapter(chapterId)

    override fun getAllTopics(): Flow<List<TopicEntity>> = dao.getAllTopics()

    override suspend fun createTopic(chapterId: String, name: String, relevance: String): TopicEntity {
        val topic = TopicEntity(chapterId = chapterId, name = name, examRelevance = relevance)
        dao.insertTopic(topic)
        return topic
    }

    override suspend fun updateTopic(topic: TopicEntity) = dao.updateTopic(topic)

    override fun getMasteredTopicsCount(): Flow<Int> = dao.getMasteredTopicsCount()

    override fun getTotalTopicsCount(): Flow<Int> = dao.getTotalTopicsCount()
}

class OfflineRecallRepository(
    private val dao: RecallDao
) : RecallRepository {

    override fun getDueCards(now: Long): Flow<List<RecallCardEntity>> = dao.getDueCards(now)

    override fun getAllCards(): Flow<List<RecallCardEntity>> = dao.getAllCards()

    override suspend fun createCard(
        subjectId: String,
        chapterId: String,
        topicId: String?,
        prompt: String,
        answer: String
    ): RecallCardEntity {
        val card = RecallCardEntity(
            subjectId = subjectId,
            chapterId = chapterId,
            topicId = topicId,
            prompt = prompt,
            expectedAnswer = answer
        )
        dao.insertCard(card)
        return card
    }

    override suspend fun createCardFromMistake(mistake: MistakeEntity): RecallCardEntity {
        val card = RecallCardEntity(
            subjectId = mistake.subjectId,
            chapterId = mistake.chapterId,
            topicId = mistake.topicId,
            prompt = mistake.question,
            expectedAnswer = mistake.correctAnswer,
            explanation = "Recovered from mistake (lost ${mistake.marksLost} marks)",
            intervalDays = 1,
            easeFactor = 2.3f, // slightly lower initial ease factor for errors
            repetitions = 0
        )
        dao.insertCard(card)
        return card
    }

    override suspend fun deleteCard(card: RecallCardEntity) = dao.deleteCard(card)

    override suspend fun submitReview(card: RecallCardEntity, rating: RecallRating) {
        val now = System.currentTimeMillis()
        val sm2 = SM2Engine.calculateNextReview(
            currentIntervalDays = card.intervalDays,
            currentEaseFactor = card.easeFactor,
            currentRepetitions = card.repetitions,
            rating = rating,
            nowMs = now
        )

        val updatedCard = card.copy(
            intervalDays = sm2.nextIntervalDays,
            easeFactor = sm2.nextEaseFactor,
            repetitions = sm2.nextRepetitions,
            dueDate = sm2.nextDueTimestamp
        )
        dao.updateCard(updatedCard)

        dao.insertReviewLog(
            RecallReviewEntity(
                cardId = card.id,
                rating = rating.grade,
                reviewTimestamp = now
            )
        )
    }

    override fun getRecentReviews(): Flow<List<RecallReviewEntity>> = dao.getRecentReviews()
}

class OfflinePaperRepository(
    private val dao: PaperPilotDao
) : PaperRepository {

    override fun getAllPapers(): Flow<List<PaperEntity>> = dao.getAllPapers()

    override suspend fun getPaperById(id: String): PaperEntity? = dao.getPaperById(id)

    override suspend fun createPaper(
        subjectId: String,
        title: String,
        totalMarks: Float,
        durationMinutes: Int
    ): PaperEntity {
        val paper = PaperEntity(
            subjectId = subjectId,
            title = title,
            totalMarks = totalMarks,
            durationMinutes = durationMinutes
        )
        dao.insertPaper(paper)
        return paper
    }

    override suspend fun deletePaper(paper: PaperEntity) = dao.deletePaper(paper)

    override fun getAllQuestions(): Flow<List<QuestionBankEntity>> = dao.getAllQuestions()

    override fun getQuestionsForSubject(subjectId: String): Flow<List<QuestionBankEntity>> =
        dao.getQuestionsForSubject(subjectId)

    override suspend fun addQuestion(
        subjectId: String,
        chapterId: String,
        topicId: String?,
        text: String,
        marks: Float,
        difficulty: String,
        questionType: String
    ): QuestionBankEntity {
        val question = QuestionBankEntity(
            subjectId = subjectId,
            chapterId = chapterId,
            topicId = topicId,
            questionText = text,
            marks = marks,
            difficulty = difficulty,
            questionType = questionType
        )
        dao.insertQuestion(question)
        return question
    }

    override suspend fun updateQuestion(question: QuestionBankEntity) = dao.updateQuestion(question)

    override suspend fun deleteQuestion(question: QuestionBankEntity) = dao.deleteQuestion(question)

    override fun getQuestionsForPaper(paperId: String): Flow<List<QuestionBankEntity>> =
        dao.getQuestionsForPaper(paperId)

    override suspend fun generatePaperBlueprint(
        subjectId: String,
        title: String,
        targetMarks: Float,
        durationMinutes: Int
    ): PaperEntity {
        val paper = PaperEntity(
            subjectId = subjectId,
            title = title,
            totalMarks = targetMarks,
            durationMinutes = durationMinutes,
            status = "READY"
        )
        dao.insertPaper(paper)

        // Select available questions for this subject
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val questions = dao.getQuestionsForSubject(subjectId).first()
            var currentTotal = 0f
            var order = 1
            for (q in questions) {
                if (currentTotal + q.marks <= targetMarks || currentTotal == 0f) {
                    dao.insertPaperQuestion(
                        com.os95.app.core.database.entity.PaperQuestionEntity(
                            paperId = paper.id,
                            questionId = q.id,
                            orderIndex = order++
                        )
                    )
                    currentTotal += q.marks
                    if (currentTotal >= targetMarks) break
                }
            }
        }
        return paper
    }

    override fun getAllResults(): Flow<List<ExamResultEntity>> = dao.getAllResults()

    override suspend fun recordResult(
        paperId: String,
        marksObtained: Float,
        totalMarks: Float,
        timeTakenMinutes: Int,
        lostMarks: List<LostMarksEntity>
    ) {
        val result = ExamResultEntity(
            paperId = paperId,
            marksObtained = marksObtained,
            totalMarks = totalMarks,
            timeTakenMinutes = timeTakenMinutes
        )
        dao.insertResult(result)
        lostMarks.forEach { dao.insertLostMarks(it.copy(examResultId = result.id)) }
    }
}

class OfflineMistakeRepository(
    private val dao: MistakeDao
) : MistakeRepository {

    override fun getActiveMistakes(): Flow<List<MistakeEntity>> = dao.getActiveMistakes()

    override fun getAllMistakes(): Flow<List<MistakeEntity>> = dao.getAllMistakes()

    override suspend fun recordMistake(
        subjectId: String,
        chapterId: String,
        topicId: String?,
        question: String,
        studentAnswer: String,
        correctAnswer: String,
        category: String,
        marksLost: Float
    ): MistakeEntity {
        val mistake = MistakeEntity(
            subjectId = subjectId,
            chapterId = chapterId,
            topicId = topicId,
            question = question,
            studentAnswer = studentAnswer,
            correctAnswer = correctAnswer,
            lossCategory = category,
            marksLost = marksLost
        )
        dao.insertMistake(mistake)
        return mistake
    }

    override suspend fun resolveMistake(mistake: MistakeEntity) {
        dao.updateMistake(mistake.copy(isResolved = true))
    }
}

class OfflineStudentRepository(
    private val studentDao: StudentDao,
    private val preferencesDao: StudyPreferencesDao,
    private val sessionDao: StudySessionDao
) : StudentRepository {

    override fun getProfileFlow(): Flow<StudentProfileEntity?> = studentDao.getProfileFlow()

    override suspend fun getProfile(): StudentProfileEntity? = studentDao.getProfile()

    override suspend fun saveProfile(profile: StudentProfileEntity) = studentDao.saveProfile(profile)

    override fun getPreferencesFlow(): Flow<StudyPreferencesEntity?> = preferencesDao.getPreferencesFlow()

    override suspend fun getPreferences(): StudyPreferencesEntity? = preferencesDao.getPreferences()

    override suspend fun savePreferences(prefs: StudyPreferencesEntity) = preferencesDao.savePreferences(prefs)

    override fun getAllSessions(): Flow<List<StudySessionEntity>> = sessionDao.getAllSessions()

    override suspend fun logSession(subjectId: String, chapterId: String?, durationMinutes: Int) {
        sessionDao.insertSession(
            StudySessionEntity(
                subjectId = subjectId,
                chapterId = chapterId,
                durationMinutes = durationMinutes
            )
        )
    }
}
