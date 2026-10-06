package com.os95.app.domain.repository

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
import com.os95.app.domain.model.RecallRating
import kotlinx.coroutines.flow.Flow

interface SyllabusRepository {
    fun getAllSubjects(): Flow<List<SubjectEntity>>
    suspend fun getSubjectById(id: String): SubjectEntity?
    suspend fun createSubject(name: String, colorHex: String): SubjectEntity
    suspend fun deleteSubject(subject: SubjectEntity)

    fun getChaptersForSubject(subjectId: String): Flow<List<ChapterEntity>>
    suspend fun createChapter(subjectId: String, name: String): ChapterEntity

    fun getTopicsForChapter(chapterId: String): Flow<List<TopicEntity>>
    fun getAllTopics(): Flow<List<TopicEntity>>
    suspend fun createTopic(chapterId: String, name: String, relevance: String): TopicEntity
    suspend fun updateTopic(topic: TopicEntity)

    fun getMasteredTopicsCount(): Flow<Int>
    fun getTotalTopicsCount(): Flow<Int>
}

interface RecallRepository {
    fun getDueCards(now: Long = System.currentTimeMillis()): Flow<List<RecallCardEntity>>
    fun getAllCards(): Flow<List<RecallCardEntity>>
    suspend fun createCard(subjectId: String, chapterId: String, topicId: String?, prompt: String, answer: String): RecallCardEntity
    suspend fun createCardFromMistake(mistake: MistakeEntity): RecallCardEntity
    suspend fun deleteCard(card: RecallCardEntity)
    suspend fun submitReview(card: RecallCardEntity, rating: RecallRating)
    fun getRecentReviews(): Flow<List<RecallReviewEntity>>
}

interface PaperRepository {
    fun getAllPapers(): Flow<List<PaperEntity>>
    suspend fun getPaperById(id: String): PaperEntity?
    suspend fun createPaper(subjectId: String, title: String, totalMarks: Float, durationMinutes: Int): PaperEntity
    suspend fun deletePaper(paper: PaperEntity)
    fun getAllQuestions(): Flow<List<QuestionBankEntity>>
    fun getQuestionsForSubject(subjectId: String): Flow<List<QuestionBankEntity>>
    suspend fun addQuestion(subjectId: String, chapterId: String, topicId: String?, text: String, marks: Float, difficulty: String = "MEDIUM", questionType: String = "SHORT_ANSWER"): QuestionBankEntity
    suspend fun updateQuestion(question: QuestionBankEntity)
    suspend fun deleteQuestion(question: QuestionBankEntity)
    fun getQuestionsForPaper(paperId: String): Flow<List<QuestionBankEntity>>
    fun getPaperQuestions(paperId: String): Flow<List<PaperQuestionEntity>>
    suspend fun generatePaperBlueprint(subjectId: String, title: String, targetMarks: Float, durationMinutes: Int): PaperEntity
    suspend fun generatePaper(blueprint: com.os95.app.domain.model.PaperBlueprintRequest): com.os95.app.domain.model.PaperGenerationResult
    suspend fun finalizeAndSavePaper(generatedPaper: com.os95.app.domain.model.GeneratedPaper): PaperEntity
    fun getAllResults(): Flow<List<ExamResultEntity>>
    suspend fun recordResult(paperId: String, marksObtained: Float, totalMarks: Float, timeTakenMinutes: Int, lostMarks: List<LostMarksEntity>)
    suspend fun recordDetailedResult(
        paperId: String,
        marksObtained: Float,
        totalMarks: Float,
        timeTakenMinutes: Int,
        questionResults: List<com.os95.app.domain.model.QuestionResultInput>
    ): ExamResultEntity
}

interface MistakeRepository {
    fun getActiveMistakes(): Flow<List<MistakeEntity>>
    fun getAllMistakes(): Flow<List<MistakeEntity>>
    suspend fun recordMistake(subjectId: String, chapterId: String, topicId: String?, question: String, studentAnswer: String, correctAnswer: String, category: String, marksLost: Float): MistakeEntity
    suspend fun resolveMistake(mistake: MistakeEntity)
}

interface StudentRepository {
    fun getProfileFlow(): Flow<StudentProfileEntity?>
    suspend fun getProfile(): StudentProfileEntity?
    suspend fun saveProfile(profile: StudentProfileEntity)

    fun getPreferencesFlow(): Flow<StudyPreferencesEntity?>
    suspend fun getPreferences(): StudyPreferencesEntity?
    suspend fun savePreferences(prefs: StudyPreferencesEntity)

    fun getAllSessions(): Flow<List<StudySessionEntity>>
    suspend fun logSession(subjectId: String, chapterId: String?, durationMinutes: Int)
}

interface MarksRecoveryRepository {
    fun getRecoverySnapshotFlow(durationMinutes: Int = 15): Flow<com.os95.app.domain.model.MarksRecoverySnapshot>
    suspend fun getMarksGapPlan(): com.os95.app.domain.model.MarksGapPlan
    suspend fun getForgettingRadar(): com.os95.app.domain.model.ForgettingRadarSnapshot
    suspend fun getPaperAnalysis(): com.os95.app.domain.model.PaperAnalysis
    suspend fun getRescuePlan(durationMinutes: Int = 15): com.os95.app.domain.model.RescuePlan
    suspend fun completeRescueSession(durationMinutes: Int, actionsCompleted: Int, topicsCovered: Int, cardsReviewed: Int, mistakesResolved: Int)
    suspend fun getRecoveryScore(): com.os95.app.domain.model.RecoveryScoreReport
}

interface AdvancedExamRepository {
    suspend fun getTimeToMarksReport(availableMinutes: Int = 60, targetPercentage: Float? = null): com.os95.app.domain.model.TimeToMarksReport
    suspend fun generateAdaptiveRetest(config: com.os95.app.domain.model.AdaptiveRetestConfig): com.os95.app.domain.model.AdaptiveRetestGenerationResult
    suspend fun completeAdaptiveRetest(
        paperId: String,
        marksObtained: Float,
        totalMarks: Float,
        marksAwardedMap: Map<Int, Float>
    ): com.os95.app.domain.model.AdaptiveRetestCompletionSummary
    suspend fun generateExamSimulationPaper(config: com.os95.app.domain.model.ExamSimulatorConfig): com.os95.app.core.database.entity.PaperEntity
    suspend fun submitExamSimulation(
        paperId: String,
        submission: com.os95.app.domain.model.ExamSimulationSubmission
    ): com.os95.app.domain.model.ExamReadinessResult
    suspend fun getLast7DaysDashboard(): com.os95.app.domain.model.Last7DaysDashboard
    suspend fun toggleLast7DaysTask(taskId: String)
    suspend fun setTargetExamDate(timestamp: Long?)
    suspend fun getCommandCenterSnapshot(): com.os95.app.domain.model.CommandCenterSnapshot
}

