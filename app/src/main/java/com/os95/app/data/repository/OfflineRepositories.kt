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
import com.os95.app.core.database.entity.PaperQuestionEntity
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
    private val dao: PaperPilotDao,
    private val syllabusDao: SyllabusDao? = null,
    private val mistakeDao: MistakeDao? = null,
    private val generationEngine: com.os95.app.domain.engine.PaperGenerationEngine = com.os95.app.domain.engine.PaperGenerationEngine()
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

    override fun getPaperQuestions(paperId: String): Flow<List<PaperQuestionEntity>> =
        dao.getPaperQuestions(paperId)

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
                            orderIndex = order++,
                            sectionName = "Section A",
                            snapshotQuestionText = q.questionText,
                            snapshotMarks = q.marks,
                            snapshotQuestionType = q.questionType,
                            snapshotDifficulty = q.difficulty,
                            snapshotAnswer = q.markingScheme,
                            snapshotChapterId = q.chapterId,
                            snapshotTopicId = q.topicId
                        )
                    )
                    currentTotal += q.marks
                    if (currentTotal >= targetMarks) break
                }
            }
        }
        return paper
    }

    override suspend fun generatePaper(
        blueprint: com.os95.app.domain.model.PaperBlueprintRequest
    ): com.os95.app.domain.model.PaperGenerationResult {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val questions = dao.getQuestionsForSubjectSync(blueprint.subjectId)
            val chapters = syllabusDao?.getChaptersForSubjectSync(blueprint.subjectId) ?: emptyList()
            val topics = syllabusDao?.getAllTopicsSync() ?: emptyList()
            generationEngine.generatePaper(blueprint, questions, chapters, topics)
        }
    }

    override suspend fun finalizeAndSavePaper(
        generatedPaper: com.os95.app.domain.model.GeneratedPaper
    ): PaperEntity {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.insertPaper(generatedPaper.paper)
            val joins = mutableListOf<PaperQuestionEntity>()
            var order = 1
            val now = System.currentTimeMillis()

            for (sec in generatedPaper.sections) {
                for (q in sec.questions) {
                    joins.add(
                        PaperQuestionEntity(
                            paperId = generatedPaper.paper.id,
                            questionId = q.id,
                            orderIndex = order++,
                            sectionName = sec.name,
                            snapshotQuestionText = q.questionText,
                            snapshotMarks = q.marks,
                            snapshotQuestionType = q.questionType,
                            snapshotDifficulty = q.difficulty,
                            snapshotAnswer = q.markingScheme,
                            snapshotChapterId = q.chapterId,
                            snapshotTopicId = q.topicId
                        )
                    )
                    dao.recordQuestionUsage(q.id, testedInc = 1, failedInc = 0, lastUsedAt = now)
                }
            }
            dao.insertPaperQuestions(joins)
            generatedPaper.paper
        }
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

    override suspend fun recordDetailedResult(
        paperId: String,
        marksObtained: Float,
        totalMarks: Float,
        timeTakenMinutes: Int,
        questionResults: List<com.os95.app.domain.model.QuestionResultInput>
    ): ExamResultEntity {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val result = ExamResultEntity(
                paperId = paperId,
                marksObtained = marksObtained,
                totalMarks = totalMarks,
                timeTakenMinutes = timeTakenMinutes
            )
            dao.insertResult(result)

            val paper = dao.getPaperById(paperId)
            val subId = paper?.subjectId ?: ""
            val now = System.currentTimeMillis()

            for (qRes in questionResults) {
                if (qRes.isMistake) {
                    dao.insertLostMarks(
                        LostMarksEntity(
                            examResultId = result.id,
                            topicId = qRes.topicId,
                            marksLost = qRes.marksLost,
                            lossCategory = qRes.lossCategory,
                            notes = qRes.notes
                        )
                    )
                    mistakeDao?.insertMistake(
                        MistakeEntity(
                            subjectId = subId,
                            chapterId = qRes.chapterId,
                            topicId = qRes.topicId,
                            question = qRes.questionText,
                            studentAnswer = "",
                            correctAnswer = "Review question marking scheme",
                            lossCategory = qRes.lossCategory,
                            marksLost = qRes.marksLost
                        )
                    )
                    if (qRes.topicId != null) {
                        syllabusDao?.updateTopicWeakness(qRes.topicId, 0.85f)
                    }
                    if (qRes.questionId != null) {
                        dao.recordQuestionUsage(qRes.questionId, testedInc = 1, failedInc = 1, lastUsedAt = now)
                    }
                } else {
                    if (qRes.questionId != null) {
                        dao.recordQuestionUsage(qRes.questionId, testedInc = 1, failedInc = 0, lastUsedAt = now)
                    }
                }
            }
            result
        }
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

class OfflineMarksRecoveryRepository(
    private val engine: com.os95.app.domain.engine.MarksRecoveryEngine = com.os95.app.domain.engine.MarksRecoveryEngine(),
    private val studentDao: StudentDao,
    private val syllabusDao: SyllabusDao,
    private val paperDao: PaperPilotDao,
    private val mistakeDao: MistakeDao,
    private val recallDao: RecallDao,
    private val sessionDao: StudySessionDao
) : com.os95.app.domain.repository.MarksRecoveryRepository {

    override fun getRecoverySnapshotFlow(durationMinutes: Int): Flow<com.os95.app.domain.model.MarksRecoverySnapshot> {
        return kotlinx.coroutines.flow.combine(
            studentDao.getProfileFlow(),
            syllabusDao.getAllSubjects(),
            syllabusDao.getAllTopics(),
            paperDao.getAllResults(),
            paperDao.getAllPapers(),
            paperDao.getAllLostMarks(),
            mistakeDao.getAllMistakes(),
            recallDao.getAllCards(),
            recallDao.getRecentReviews()
        ) { args: Array<Any?> ->
            val profile = args[0] as? StudentProfileEntity
            @Suppress("UNCHECKED_CAST") val subjects = args[1] as List<SubjectEntity>
            @Suppress("UNCHECKED_CAST") val topics = args[2] as List<TopicEntity>
            @Suppress("UNCHECKED_CAST") val results = args[3] as List<ExamResultEntity>
            @Suppress("UNCHECKED_CAST") val papers = args[4] as List<PaperEntity>
            @Suppress("UNCHECKED_CAST") val lostMarks = args[5] as List<LostMarksEntity>
            @Suppress("UNCHECKED_CAST") val mistakes = args[6] as List<MistakeEntity>
            @Suppress("UNCHECKED_CAST") val cards = args[7] as List<RecallCardEntity>
            @Suppress("UNCHECKED_CAST") val reviews = args[8] as List<RecallReviewEntity>

            val chapters = mutableListOf<ChapterEntity>()
            for (sub in subjects) {
                chapters.addAll(syllabusDao.getChaptersForSubjectSync(sub.id))
            }

            val targetPercentage = profile?.targetPercentage ?: 95.0f

            val marksGap = engine.calculateMarksGap(
                targetPercentage = targetPercentage,
                examResults = results,
                mistakes = mistakes,
                topics = topics,
                chapters = chapters,
                subjects = subjects
            )

            val forgettingRadar = engine.calculateForgettingRadar(
                cards = cards,
                reviews = reviews,
                topics = topics,
                chapters = chapters,
                subjects = subjects
            )

            val paperAnalysis = engine.analyzePreviousPapers(
                papers = papers,
                results = results,
                paperQuestions = emptyList(),
                lostMarks = lostMarks,
                mistakes = mistakes,
                topics = topics,
                chapters = chapters,
                subjects = subjects
            )

            val rescuePlan = engine.generateRescuePlan(
                durationMinutes = durationMinutes,
                marksGap = marksGap,
                forgettingRadar = forgettingRadar,
                mistakes = mistakes,
                topics = topics
            )

            val recoveryScore = engine.calculateRecoveryScore(
                papers = papers,
                results = results,
                lostMarks = lostMarks,
                mistakes = mistakes,
                topics = topics,
                chapters = chapters,
                subjects = subjects
            )

            com.os95.app.domain.model.MarksRecoverySnapshot(
                marksGap = marksGap,
                forgettingRadar = forgettingRadar,
                paperAnalysis = paperAnalysis,
                rescuePlan = rescuePlan,
                recoveryScore = recoveryScore
            )
        }
    }

    override suspend fun getMarksGapPlan(): com.os95.app.domain.model.MarksGapPlan = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val profile = studentDao.getProfile()
        val subjects = syllabusDao.getAllSubjects().first()
        val topics = syllabusDao.getAllTopicsSync()
        val chapters = mutableListOf<ChapterEntity>()
        for (sub in subjects) {
            chapters.addAll(syllabusDao.getChaptersForSubjectSync(sub.id))
        }
        val results = paperDao.getAllResultsSync()
        val mistakes = mistakeDao.getAllMistakesSync()
        engine.calculateMarksGap(
            targetPercentage = profile?.targetPercentage ?: 95.0f,
            examResults = results,
            mistakes = mistakes,
            topics = topics,
            chapters = chapters,
            subjects = subjects
        )
    }

    override suspend fun getForgettingRadar(): com.os95.app.domain.model.ForgettingRadarSnapshot = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val subjects = syllabusDao.getAllSubjects().first()
        val topics = syllabusDao.getAllTopicsSync()
        val chapters = mutableListOf<ChapterEntity>()
        for (sub in subjects) {
            chapters.addAll(syllabusDao.getChaptersForSubjectSync(sub.id))
        }
        val cards = recallDao.getAllCardsSync()
        val reviews = recallDao.getAllReviewsSync()
        engine.calculateForgettingRadar(cards, reviews, topics, chapters, subjects)
    }

    override suspend fun getPaperAnalysis(): com.os95.app.domain.model.PaperAnalysis = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val subjects = syllabusDao.getAllSubjects().first()
        val topics = syllabusDao.getAllTopicsSync()
        val chapters = mutableListOf<ChapterEntity>()
        for (sub in subjects) {
            chapters.addAll(syllabusDao.getChaptersForSubjectSync(sub.id))
        }
        val papers = paperDao.getAllPapersSync()
        val results = paperDao.getAllResultsSync()
        val lostMarks = paperDao.getAllLostMarksSync()
        val mistakes = mistakeDao.getAllMistakesSync()
        engine.analyzePreviousPapers(papers, results, emptyList(), lostMarks, mistakes, topics, chapters, subjects)
    }

    override suspend fun getRescuePlan(durationMinutes: Int): com.os95.app.domain.model.RescuePlan = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val marksGap = getMarksGapPlan()
        val radar = getForgettingRadar()
        val mistakes = mistakeDao.getAllMistakesSync()
        val topics = syllabusDao.getAllTopicsSync()
        engine.generateRescuePlan(durationMinutes, marksGap, radar, mistakes, topics)
    }

    override suspend fun completeRescueSession(
        durationMinutes: Int,
        actionsCompleted: Int,
        topicsCovered: Int,
        cardsReviewed: Int,
        mistakesResolved: Int
    ) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        sessionDao.insertSession(
            StudySessionEntity(
                subjectId = "rescue_mode",
                chapterId = null,
                durationMinutes = durationMinutes
            )
        )
    }

    override suspend fun getRecoveryScore(): com.os95.app.domain.model.RecoveryScoreReport = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val subjects = syllabusDao.getAllSubjects().first()
        val topics = syllabusDao.getAllTopicsSync()
        val chapters = mutableListOf<ChapterEntity>()
        for (sub in subjects) {
            chapters.addAll(syllabusDao.getChaptersForSubjectSync(sub.id))
        }
        val papers = paperDao.getAllPapersSync()
        val results = paperDao.getAllResultsSync()
        val lostMarks = paperDao.getAllLostMarksSync()
        val mistakes = mistakeDao.getAllMistakesSync()
        engine.calculateRecoveryScore(papers, results, lostMarks, mistakes, topics, chapters, subjects)
    }
}

class OfflineAdvancedExamRepository(
    private val engine: com.os95.app.domain.engine.AdvancedExamEngine,
    private val preferencesManager: com.os95.app.core.datastore.PreferencesManager,
    private val studentDao: StudentDao,
    private val syllabusDao: SyllabusDao,
    private val paperDao: PaperPilotDao,
    private val mistakeDao: MistakeDao,
    private val recallDao: RecallDao,
    private val sessionDao: StudySessionDao
) : com.os95.app.domain.repository.AdvancedExamRepository {

    override suspend fun getTimeToMarksReport(
        availableMinutes: Int,
        targetPercentage: Float?
    ): com.os95.app.domain.model.TimeToMarksReport = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val profile = studentDao.getProfile()
        val effTarget = targetPercentage ?: profile?.targetPercentage ?: 95.0f
        val prefs = preferencesManager.preferencesFlow.first()
        val examDate = prefs.targetExamDateTimestamp
        val now = System.currentTimeMillis()
        val daysRemaining = examDate?.let { kotlin.math.max(0, ((it - now) / com.os95.app.domain.engine.AdvancedExamEngine.ONE_DAY_MS).toInt()) }

        val subjects = syllabusDao.getAllSubjectsSync()
        val chapters = syllabusDao.getAllChaptersSync()
        val topics = syllabusDao.getAllTopicsSync()
        val questions = paperDao.getAllQuestionsSync()
        val examResults = paperDao.getAllResultsSync()
        val mistakes = mistakeDao.getAllMistakesSync()
        val studySessions = sessionDao.getAllSessionsSync()

        engine.calculateTimeToMarks(
            targetPercentage = effTarget,
            availableStudyMinutes = availableMinutes,
            examDaysRemaining = daysRemaining,
            subjects = subjects,
            chapters = chapters,
            topics = topics,
            questions = questions,
            examResults = examResults,
            mistakes = mistakes,
            studySessions = studySessions
        )
    }

    override suspend fun generateAdaptiveRetest(
        config: com.os95.app.domain.model.AdaptiveRetestConfig
    ): com.os95.app.domain.model.AdaptiveRetestGenerationResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val allQuestions = paperDao.getAllQuestionsSync()
        val mistakes = mistakeDao.getAllMistakesSync()
        val topics = syllabusDao.getAllTopicsSync()
        val chapters = syllabusDao.getAllChaptersSync()
        val subjects = syllabusDao.getAllSubjectsSync()

        val result = engine.generateAdaptiveRetest(
            config = config,
            allQuestions = allQuestions,
            mistakes = mistakes,
            topics = topics,
            chapters = chapters,
            subjects = subjects
        )

        paperDao.insertPaper(result.paper)
        paperDao.insertPaperQuestions(result.questions)
        result
    }

    override suspend fun completeAdaptiveRetest(
        paperId: String,
        marksObtained: Float,
        totalMarks: Float,
        marksAwardedMap: Map<Int, Float>
    ): com.os95.app.domain.model.AdaptiveRetestCompletionSummary = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val paper = paperDao.getPaperById(paperId)
        val paperQuestions = paperDao.getPaperQuestionsSync(paperId)
        val existingMistakes = mistakeDao.getAllMistakesSync()
        val topics = syllabusDao.getAllTopicsSync()

        val summary = engine.evaluateAdaptiveRetestCompletion(
            paperId = paperId,
            marksObtained = marksObtained,
            totalMarks = totalMarks,
            paperQuestions = paperQuestions,
            marksAwardedPerQuestion = marksAwardedMap,
            existingMistakes = existingMistakes,
            topics = topics
        )

        // Record Exam Result
        val resultId = UUID.randomUUID().toString()
        paperDao.insertResult(
            ExamResultEntity(
                id = resultId,
                paperId = paperId,
                marksObtained = marksObtained,
                totalMarks = totalMarks,
                timeTakenMinutes = paper?.durationMinutes ?: 30,
                completedAt = System.currentTimeMillis()
            )
        )

        // Resolve mistakes if student scored full marks on retest questions
        paperQuestions.forEachIndexed { idx, pq ->
            val awarded = marksAwardedMap[idx] ?: 0f
            if (awarded >= pq.snapshotMarks * 0.9f) {
                val matched = existingMistakes.find {
                    !it.isResolved && (it.question.trim().equals(pq.snapshotQuestionText.trim(), ignoreCase = true) ||
                        (it.topicId != null && it.topicId == pq.snapshotTopicId))
                }
                if (matched != null) {
                    mistakeDao.updateMistake(matched.copy(isResolved = true))
                }
            }
        }

        if (paper != null) {
            paperDao.updatePaper(paper.copy(status = "COMPLETED"))
            sessionDao.insertSession(
                StudySessionEntity(
                    subjectId = paper.subjectId,
                    chapterId = null,
                    durationMinutes = paper.durationMinutes
                )
            )
        }

        summary
    }

    override suspend fun generateExamSimulationPaper(
        config: com.os95.app.domain.model.ExamSimulatorConfig
    ): PaperEntity = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val subjects = syllabusDao.getAllSubjectsSync()
        val defaultSubjectId = config.subjectId ?: subjects.firstOrNull()?.id ?: "general_subject"
        val subject = subjects.find { it.id == defaultSubjectId }
        val subjectName = subject?.name ?: "Exam"

        val bankQuestions = paperDao.getAllQuestionsSync().filter {
            (config.subjectId == null || it.subjectId == config.subjectId) &&
            (config.selectedChapterIds.isEmpty() || it.chapterId in config.selectedChapterIds) &&
            (config.allowedQuestionTypes.isEmpty() || it.questionType in config.allowedQuestionTypes)
        }

        val chapters = syllabusDao.getAllChaptersSync().filter { it.subjectId == defaultSubjectId }
        val topics = syllabusDao.getAllTopicsSync().filter { t -> chapters.any { c -> c.id == t.chapterId } }

        val paperId = UUID.randomUUID().toString()
        val paperQuestions = mutableListOf<PaperQuestionEntity>()

        var accumulatedMarks = 0f
        var order = 1

        if (bankQuestions.isNotEmpty()) {
            val sortedBank = bankQuestions.shuffled().sortedBy {
                if (config.difficulty == "HARD") -it.marks else it.marks
            }
            for (q in sortedBank) {
                if (accumulatedMarks + q.marks <= config.totalMarks + 3f) {
                    val section = when {
                        q.marks <= 1.0f -> "Section A (Objective)"
                        q.marks <= 3.0f -> "Section B (Short Answer)"
                        q.marks <= 5.0f -> "Section C (Long Answer)"
                        else -> "Section D (Comprehensive)"
                    }
                    paperQuestions.add(
                        PaperQuestionEntity(
                            id = UUID.randomUUID().toString(),
                            paperId = paperId,
                            questionId = q.id,
                            orderIndex = order++,
                            sectionName = section,
                            snapshotQuestionText = q.questionText,
                            snapshotMarks = q.marks,
                            snapshotQuestionType = q.questionType,
                            snapshotDifficulty = q.difficulty,
                            snapshotAnswer = q.markingScheme,
                            snapshotChapterId = q.chapterId,
                            snapshotTopicId = q.topicId
                        )
                    )
                    accumulatedMarks += q.marks
                }
            }
        }

        // If bank questions didn't meet the target marks or was empty, synthesize structured items from topics
        if (paperQuestions.isEmpty() || accumulatedMarks < config.totalMarks * 0.7f) {
            val availableTopics = if (topics.isNotEmpty()) topics else listOf(
                TopicEntity(chapterId = "ch_general", name = "Core Concepts", examRelevance = "HIGH")
            )

            val questionTemplates = listOf(
                Triple("Define the fundamental theorem and notation of %s.", 2.0f, "Section A (Objective)"),
                Triple("Explain the core theoretical derivation and steps for %s.", 3.0f, "Section B (Short Answer)"),
                Triple("Solve the advanced analytical problem applying principles of %s.", 5.0f, "Section C (Long Answer)"),
                Triple("Critically examine and synthesize the practical implications of %s.", 5.0f, "Section D (Comprehensive)")
            )

            var tIdx = 0
            while (accumulatedMarks < config.totalMarks && tIdx < 20) {
                val t = availableTopics[tIdx % availableTopics.size]
                val tmpl = questionTemplates[tIdx % questionTemplates.size]
                val qText = String.format(tmpl.first, t.name)
                val marks = tmpl.second

                paperQuestions.add(
                    PaperQuestionEntity(
                        id = UUID.randomUUID().toString(),
                        paperId = paperId,
                        questionId = "sim_q_${order}",
                        orderIndex = order++,
                        sectionName = tmpl.third,
                        snapshotQuestionText = qText,
                        snapshotMarks = marks,
                        snapshotQuestionType = if (marks <= 2f) "SHORT" else "LONG",
                        snapshotDifficulty = config.difficulty,
                        snapshotAnswer = "Detailed key points on ${t.name}",
                        snapshotChapterId = t.chapterId,
                        snapshotTopicId = t.id
                    )
                )
                accumulatedMarks += marks
                tIdx++
            }
        }

        val totalMarksActual = paperQuestions.sumOf { it.snapshotMarks.toDouble() }.toFloat()
        val paper = PaperEntity(
            id = paperId,
            title = "$subjectName Official Simulation",
            subjectId = defaultSubjectId,
            totalMarks = totalMarksActual,
            durationMinutes = config.durationMinutes,
            status = "READY",
            instructions = "Timed Exam Readiness Simulator: Complete all questions within ${config.durationMinutes} minutes. Strict examination conditions."
        )

        paperDao.insertPaper(paper)
        paperDao.insertPaperQuestions(paperQuestions)
        paper
    }

    override suspend fun submitExamSimulation(
        paperId: String,
        submission: com.os95.app.domain.model.ExamSimulationSubmission
    ): com.os95.app.domain.model.ExamReadinessResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val paper = paperDao.getPaperById(paperId) ?: throw IllegalArgumentException("Paper not found: $paperId")
        val questions = paperDao.getPaperQuestionsSync(paperId)
        val chapters = syllabusDao.getAllChaptersSync()
        val historicalResults = paperDao.getAllResultsSync()
        val allTopics = syllabusDao.getAllTopicsSync()
        val allMistakes = mistakeDao.getAllMistakesSync()

        val readinessResult = engine.evaluateExamReadiness(
            submission = submission,
            paper = paper,
            questions = questions,
            chapters = chapters,
            historicalResults = historicalResults,
            allTopics = allTopics,
            allMistakes = allMistakes
        )

        // Save Result
        val resultId = UUID.randomUUID().toString()
        paperDao.insertResult(
            ExamResultEntity(
                id = resultId,
                paperId = paperId,
                marksObtained = readinessResult.score,
                totalMarks = paper.totalMarks,
                timeTakenMinutes = kotlin.math.max(1, submission.timeSpentSeconds / 60),
                completedAt = System.currentTimeMillis()
            )
        )

        // Save lost marks & mistakes for incorrect items
        val lostMarksList = mutableListOf<LostMarksEntity>()
        val newMistakes = mutableListOf<MistakeEntity>()

        questions.forEachIndexed { idx, q ->
            val awarded = submission.marksAwardedMap[idx] ?: 0f
            val lost = q.snapshotMarks - awarded
            if (lost > 0f) {
                val category = when {
                    q.snapshotMarks <= 2.0f -> "CARELESS_ERROR"
                    q.snapshotDifficulty.equals("HARD", ignoreCase = true) -> "CONCEPTUAL_GAP"
                    else -> "CALCULATION_ERROR"
                }

                lostMarksList.add(
                    LostMarksEntity(
                        id = UUID.randomUUID().toString(),
                        examResultId = resultId,
                        topicId = q.snapshotTopicId,
                        marksLost = lost,
                        lossCategory = category,
                        notes = "Missed in ${paper.title}"
                    )
                )

                val chId = if (q.snapshotChapterId.isNotBlank()) q.snapshotChapterId else "${paper.subjectId}_gen"
                newMistakes.add(
                    MistakeEntity(
                        id = UUID.randomUUID().toString(),
                        subjectId = paper.subjectId,
                        chapterId = chId,
                        topicId = q.snapshotTopicId,
                        question = q.snapshotQuestionText,
                        studentAnswer = submission.answers[idx] ?: "",
                        correctAnswer = q.snapshotAnswer.ifBlank { "Complete solution" },
                        marksLost = lost,
                        lossCategory = category,
                        isResolved = false,
                        createdAt = System.currentTimeMillis()
                    )
                )

                // Update question test count
                val qId = q.questionId
                if (!qId.isNullOrBlank() && !qId.startsWith("sim_q_")) {
                    paperDao.recordQuestionUsage(
                        questionId = qId,
                        testedInc = 1,
                        failedInc = 1,
                        lastUsedAt = System.currentTimeMillis()
                    )
                }
            } else {
                val qId = q.questionId
                if (!qId.isNullOrBlank() && !qId.startsWith("sim_q_")) {
                    paperDao.recordQuestionUsage(
                        questionId = qId,
                        testedInc = 1,
                        failedInc = 0,
                        lastUsedAt = System.currentTimeMillis()
                    )
                }
            }
        }

        if (lostMarksList.isNotEmpty()) {
            paperDao.insertMultipleLostMarks(lostMarksList)
        }
        if (newMistakes.isNotEmpty()) {
            mistakeDao.insertMistakes(newMistakes)
        }

        paperDao.updatePaper(paper.copy(status = "COMPLETED"))
        sessionDao.insertSession(
            StudySessionEntity(
                subjectId = paper.subjectId,
                chapterId = null,
                durationMinutes = kotlin.math.max(1, submission.timeSpentSeconds / 60)
            )
        )

        readinessResult
    }

    override suspend fun getLast7DaysDashboard(): com.os95.app.domain.model.Last7DaysDashboard = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val prefs = preferencesManager.preferencesFlow.first()
        val examDate = prefs.targetExamDateTimestamp
        val completedTaskIds = prefs.completedLast7DaysTaskIds
        val subjects = syllabusDao.getAllSubjectsSync()
        val chapters = syllabusDao.getAllChaptersSync()
        val topics = syllabusDao.getAllTopicsSync()
        val mistakes = mistakeDao.getAllMistakesSync()
        val cards = recallDao.getAllCardsSync()

        engine.buildLast7DaysDashboard(
            targetExamDateTimestamp = examDate,
            completedTaskIds = completedTaskIds,
            subjects = subjects,
            chapters = chapters,
            topics = topics,
            mistakes = mistakes,
            recallCards = cards
        )
    }

    override suspend fun toggleLast7DaysTask(taskId: String) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        preferencesManager.toggleLast7DaysTask(taskId)
    }

    override suspend fun setTargetExamDate(timestamp: Long?) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        preferencesManager.setTargetExamDate(timestamp)
    }

    override suspend fun getCommandCenterSnapshot(): com.os95.app.domain.model.CommandCenterSnapshot = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val profile = studentDao.getProfile()
        val studentName = profile?.name ?: "Student"
        val targetPercentage = profile?.targetPercentage ?: 95.0f
        val prefs = preferencesManager.preferencesFlow.first()
        val examDate = prefs.targetExamDateTimestamp

        val subjects = syllabusDao.getAllSubjectsSync()
        val chapters = syllabusDao.getAllChaptersSync()
        val topics = syllabusDao.getAllTopicsSync()
        val mistakes = mistakeDao.getAllMistakesSync()
        val examResults = paperDao.getAllResultsSync()
        val studySessions = sessionDao.getAllSessionsSync()

        engine.build95CommandCenterSnapshot(
            studentName = studentName,
            targetPercentage = targetPercentage,
            targetExamDateTimestamp = examDate,
            subjects = subjects,
            chapters = chapters,
            topics = topics,
            mistakes = mistakes,
            examResults = examResults,
            studySessions = studySessions
        )
    }
}


