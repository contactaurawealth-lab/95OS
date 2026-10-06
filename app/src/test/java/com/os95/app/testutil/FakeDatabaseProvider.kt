package com.os95.app.testutil

import com.os95.app.core.database.DatabaseProvider
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeDatabaseProvider : DatabaseProvider {

    val fakeSyllabusDao = FakeSyllabusDao()
    val fakePaperPilotDao = FakePaperPilotDao()
    val fakeMistakeDao = FakeMistakeDao()
    val fakeRecallDao = FakeRecallDao()
    val fakeStudySessionDao = FakeStudySessionDao()
    val fakeStudentDao = FakeStudentDao()
    val fakeStudyPreferencesDao = FakeStudyPreferencesDao()

    override fun studentDao(): StudentDao = fakeStudentDao
    override fun studyPreferencesDao(): StudyPreferencesDao = fakeStudyPreferencesDao
    override fun syllabusDao(): SyllabusDao = fakeSyllabusDao
    override fun paperPilotDao(): PaperPilotDao = fakePaperPilotDao
    override fun mistakeDao(): MistakeDao = fakeMistakeDao
    override fun recallDao(): RecallDao = fakeRecallDao
    override fun studySessionDao(): StudySessionDao = fakeStudySessionDao

    override suspend fun <R> runInTransaction(block: suspend () -> R): R = block()

    fun clearAll() {
        fakeSyllabusDao.clear()
        fakePaperPilotDao.clear()
        fakeMistakeDao.clear()
        fakeRecallDao.clear()
        fakeStudySessionDao.clear()
    }
}

class FakeSyllabusDao : SyllabusDao {
    private val subjects = MutableStateFlow<List<SubjectEntity>>(emptyList())
    private val chapters = MutableStateFlow<List<ChapterEntity>>(emptyList())
    private val topics = MutableStateFlow<List<TopicEntity>>(emptyList())

    fun clear() {
        subjects.value = emptyList()
        chapters.value = emptyList()
        topics.value = emptyList()
    }

    override fun getAllSubjects(): Flow<List<SubjectEntity>> = subjects.asStateFlow()

    override suspend fun getSubjectById(id: String): SubjectEntity? =
        subjects.value.firstOrNull { it.id == id }

    override suspend fun insertSubject(subject: SubjectEntity) {
        subjects.value = subjects.value.filter { it.id != subject.id } + subject
    }

    override suspend fun insertSubjects(subjectsList: List<SubjectEntity>) {
        val existingMap = subjects.value.associateBy { it.id }.toMutableMap()
        for (s in subjectsList) { existingMap[s.id] = s }
        subjects.value = existingMap.values.toList()
    }

    override suspend fun getSubjectByName(name: String): SubjectEntity? =
        subjects.value.firstOrNull { it.name.equals(name, ignoreCase = true) }

    override suspend fun deleteSubject(subject: SubjectEntity) {
        subjects.value = subjects.value.filter { it.id != subject.id }
    }

    override fun getChaptersForSubject(subjectId: String): Flow<List<ChapterEntity>> =
        chapters.map { list -> list.filter { it.subjectId == subjectId } }

    override suspend fun getChaptersForSubjectSync(subjectId: String): List<ChapterEntity> =
        chapters.value.filter { it.subjectId == subjectId }

    override suspend fun getChapterById(id: String): ChapterEntity? =
        chapters.value.firstOrNull { it.id == id }

    override suspend fun getChapterByName(subjectId: String, name: String): ChapterEntity? =
        chapters.value.firstOrNull { it.subjectId == subjectId && it.name.equals(name, ignoreCase = true) }

    override suspend fun insertChapter(chapter: ChapterEntity) {
        chapters.value = chapters.value.filter { it.id != chapter.id } + chapter
    }

    override suspend fun insertChapters(chaptersList: List<ChapterEntity>) {
        val existingMap = chapters.value.associateBy { it.id }.toMutableMap()
        for (c in chaptersList) { existingMap[c.id] = c }
        chapters.value = existingMap.values.toList()
    }

    override suspend fun deleteChapter(chapter: ChapterEntity) {
        chapters.value = chapters.value.filter { it.id != chapter.id }
    }

    override fun getTopicsForChapter(chapterId: String): Flow<List<TopicEntity>> =
        topics.map { list -> list.filter { it.chapterId == chapterId } }

    override suspend fun getTopicsForChapterSync(chapterId: String): List<TopicEntity> =
        topics.value.filter { it.chapterId == chapterId }

    override fun getAllTopics(): Flow<List<TopicEntity>> = topics.asStateFlow()

    override suspend fun getAllTopicsSync(): List<TopicEntity> = topics.value

    override suspend fun getTopicById(id: String): TopicEntity? =
        topics.value.firstOrNull { it.id == id }

    override suspend fun getTopicByName(chapterId: String, name: String): TopicEntity? =
        topics.value.firstOrNull { it.chapterId == chapterId && it.name.equals(name, ignoreCase = true) }

    override suspend fun insertTopic(topic: TopicEntity) {
        topics.value = topics.value.filter { it.id != topic.id } + topic
    }

    override suspend fun insertTopics(topicsList: List<TopicEntity>) {
        val existingMap = topics.value.associateBy { it.id }.toMutableMap()
        for (t in topicsList) { existingMap[t.id] = t }
        topics.value = existingMap.values.toList()
    }

    override suspend fun updateTopic(topic: TopicEntity) {
        topics.value = topics.value.map { if (it.id == topic.id) topic else it }
    }

    override suspend fun updateTopicWeakness(topicId: String, weaknessScore: Float) {
        topics.value = topics.value.map {
            if (it.id == topicId) it.copy(weaknessScore = weaknessScore) else it
        }
    }

    override suspend fun deleteTopic(topic: TopicEntity) {
        topics.value = topics.value.filter { it.id != topic.id }
    }

    override fun getMasteredTopicsCount(): Flow<Int> =
        topics.map { it.count { top -> top.masteryState == "MASTERED" } }

    override fun getTotalTopicsCount(): Flow<Int> =
        topics.map { it.size }
}

class FakePaperPilotDao : PaperPilotDao {
    private val questions = MutableStateFlow<List<QuestionBankEntity>>(emptyList())
    private val papers = MutableStateFlow<List<PaperEntity>>(emptyList())
    private val paperQuestions = MutableStateFlow<List<PaperQuestionEntity>>(emptyList())
    private val results = MutableStateFlow<List<ExamResultEntity>>(emptyList())
    private val lostMarks = MutableStateFlow<List<LostMarksEntity>>(emptyList())

    fun clear() {
        questions.value = emptyList()
        papers.value = emptyList()
        paperQuestions.value = emptyList()
        results.value = emptyList()
        lostMarks.value = emptyList()
    }

    override fun getQuestionsForSubject(subjectId: String): Flow<List<QuestionBankEntity>> =
        questions.map { list -> list.filter { it.subjectId == subjectId } }

    override suspend fun getQuestionsForSubjectSync(subjectId: String): List<QuestionBankEntity> =
        questions.value.filter { it.subjectId == subjectId }

    override fun getQuestionsForChapter(chapterId: String): Flow<List<QuestionBankEntity>> =
        questions.map { list -> list.filter { it.chapterId == chapterId } }

    override fun getAllQuestions(): Flow<List<QuestionBankEntity>> = questions.asStateFlow()

    override suspend fun getAllQuestionsSync(): List<QuestionBankEntity> = questions.value

    override suspend fun getQuestionById(id: String): QuestionBankEntity? =
        questions.value.firstOrNull { it.id == id }

    override suspend fun insertQuestion(question: QuestionBankEntity) {
        questions.value = questions.value.filter { it.id != question.id } + question
    }

    override suspend fun insertQuestions(questionsList: List<QuestionBankEntity>) {
        val existingMap = questions.value.associateBy { it.id }.toMutableMap()
        for (q in questionsList) { existingMap[q.id] = q }
        questions.value = existingMap.values.toList()
    }

    override suspend fun updateQuestion(question: QuestionBankEntity) {
        questions.value = questions.value.map { if (it.id == question.id) question else it }
    }

    override suspend fun recordQuestionUsage(questionId: String, testedInc: Int, failedInc: Int, lastUsedAt: Long) {
        questions.value = questions.value.map {
            if (it.id == questionId) {
                it.copy(
                    timesTested = it.timesTested + testedInc,
                    timesFailed = it.timesFailed + failedInc,
                    lastUsedAt = lastUsedAt
                )
            } else it
        }
    }

    override suspend fun deleteQuestion(question: QuestionBankEntity) {
        questions.value = questions.value.filter { it.id != question.id }
    }

    override fun getAllPapers(): Flow<List<PaperEntity>> = papers.asStateFlow()

    override suspend fun getPaperById(id: String): PaperEntity? =
        papers.value.firstOrNull { it.id == id }

    override suspend fun insertPaper(paper: PaperEntity) {
        papers.value = papers.value.filter { it.id != paper.id } + paper
    }

    override suspend fun insertPapers(papersList: List<PaperEntity>) {
        val existingMap = papers.value.associateBy { it.id }.toMutableMap()
        for (p in papersList) { existingMap[p.id] = p }
        papers.value = existingMap.values.toList()
    }

    override suspend fun updatePaper(paper: PaperEntity) {
        papers.value = papers.value.map { if (it.id == paper.id) paper else it }
    }

    override suspend fun deletePaper(paper: PaperEntity) {
        papers.value = papers.value.filter { it.id != paper.id }
    }

    override suspend fun insertPaperQuestion(join: PaperQuestionEntity) {
        paperQuestions.value = paperQuestions.value + join
    }

    override suspend fun insertPaperQuestions(joins: List<PaperQuestionEntity>) {
        paperQuestions.value = paperQuestions.value + joins
    }

    override suspend fun deletePaperQuestions(paperId: String) {
        paperQuestions.value = paperQuestions.value.filter { it.paperId != paperId }
    }

    override fun getPaperQuestions(paperId: String): Flow<List<PaperQuestionEntity>> =
        paperQuestions.map { list -> list.filter { it.paperId == paperId }.sortedBy { it.orderIndex } }

    override suspend fun getPaperQuestionsSync(paperId: String): List<PaperQuestionEntity> =
        paperQuestions.value.filter { it.paperId == paperId }.sortedBy { it.orderIndex }

    override fun getQuestionsForPaper(paperId: String): Flow<List<QuestionBankEntity>> =
        paperQuestions.map { joins ->
            val qIds = joins.filter { it.paperId == paperId }.sortedBy { it.orderIndex }.mapNotNull { it.questionId }
            val qMap = questions.value.associateBy { it.id }
            qIds.mapNotNull { qMap[it] }
        }

    override fun getAllResults(): Flow<List<ExamResultEntity>> = results.asStateFlow()

    override suspend fun getAllResultsSync(): List<ExamResultEntity> = results.value

    override suspend fun getAllPapersSync(): List<PaperEntity> = papers.value

    override suspend fun getResultForPaper(paperId: String): ExamResultEntity? =
        results.value.firstOrNull { it.paperId == paperId }

    override suspend fun insertResult(result: ExamResultEntity) {
        results.value = results.value.filter { it.id != result.id } + result
    }

    override suspend fun insertResults(resultsList: List<ExamResultEntity>) {
        val existingMap = results.value.associateBy { it.id }.toMutableMap()
        for (r in resultsList) { existingMap[r.id] = r }
        results.value = existingMap.values.toList()
    }

    override fun getLostMarksForResult(resultId: String): Flow<List<LostMarksEntity>> =
        lostMarks.map { list -> list.filter { it.examResultId == resultId } }

    override fun getAllLostMarks(): Flow<List<LostMarksEntity>> = lostMarks.asStateFlow()

    override suspend fun getAllLostMarksSync(): List<LostMarksEntity> = lostMarks.value

    override suspend fun insertLostMarks(lostMarksItem: LostMarksEntity) {
        lostMarks.value = lostMarks.value + lostMarksItem
    }

    override suspend fun insertMultipleLostMarks(lostMarksList: List<LostMarksEntity>) {
        lostMarks.value = lostMarks.value + lostMarksList
    }
}

class FakeMistakeDao : MistakeDao {
    private val mistakes = MutableStateFlow<List<MistakeEntity>>(emptyList())

    fun clear() { mistakes.value = emptyList() }

    override fun getActiveMistakes(): Flow<List<MistakeEntity>> =
        mistakes.map { list -> list.filter { !it.isResolved } }

    override fun getAllMistakes(): Flow<List<MistakeEntity>> = mistakes.asStateFlow()

    override suspend fun getAllMistakesSync(): List<MistakeEntity> = mistakes.value

    override fun getMistakesBySubject(subjectId: String): Flow<List<MistakeEntity>> =
        mistakes.map { list -> list.filter { it.subjectId == subjectId } }

    override suspend fun insertMistake(mistake: MistakeEntity) {
        mistakes.value = mistakes.value.filter { it.id != mistake.id } + mistake
    }

    override suspend fun insertMistakes(mistakesList: List<MistakeEntity>) {
        val existingMap = mistakes.value.associateBy { it.id }.toMutableMap()
        for (m in mistakesList) { existingMap[m.id] = m }
        mistakes.value = existingMap.values.toList()
    }

    override suspend fun updateMistake(mistake: MistakeEntity) {
        mistakes.value = mistakes.value.map { if (it.id == mistake.id) mistake else it }
    }

    override suspend fun deleteMistake(mistake: MistakeEntity) {
        mistakes.value = mistakes.value.filter { it.id != mistake.id }
    }
}

class FakeRecallDao : RecallDao {
    private val cards = MutableStateFlow<List<RecallCardEntity>>(emptyList())
    private val reviews = MutableStateFlow<List<RecallReviewEntity>>(emptyList())

    fun clear() {
        cards.value = emptyList()
        reviews.value = emptyList()
    }

    override fun getDueCards(currentTimestamp: Long): Flow<List<RecallCardEntity>> =
        cards.map { list -> list.filter { it.dueDate <= currentTimestamp } }

    override fun getAllCards(): Flow<List<RecallCardEntity>> = cards.asStateFlow()

    override suspend fun getAllCardsSync(): List<RecallCardEntity> = cards.value

    override fun getCardsBySubject(subjectId: String): Flow<List<RecallCardEntity>> =
        cards.map { list -> list.filter { it.subjectId == subjectId } }

    override suspend fun insertCard(card: RecallCardEntity) {
        cards.value = cards.value.filter { it.id != card.id } + card
    }

    override suspend fun updateCard(card: RecallCardEntity) {
        cards.value = cards.value.map { if (it.id == card.id) card else it }
    }

    override suspend fun deleteCard(card: RecallCardEntity) {
        cards.value = cards.value.filter { it.id != card.id }
    }

    override suspend fun insertReviewLog(review: RecallReviewEntity) {
        reviews.value = reviews.value + review
    }

    override fun getRecentReviews(): Flow<List<RecallReviewEntity>> = reviews.asStateFlow()

    override suspend fun getAllReviewsSync(): List<RecallReviewEntity> = reviews.value
}

class FakeStudySessionDao : StudySessionDao {
    private val sessions = MutableStateFlow<List<StudySessionEntity>>(emptyList())

    fun clear() { sessions.value = emptyList() }

    override fun getAllSessions(): Flow<List<StudySessionEntity>> = sessions.asStateFlow()

    override suspend fun insertSession(session: StudySessionEntity) {
        sessions.value = sessions.value.filter { it.id != session.id } + session
    }

    override fun getTotalMinutesSince(sinceTimestamp: Long): Flow<Int?> =
        sessions.map { list -> list.filter { it.completedAt >= sinceTimestamp }.sumOf { it.durationMinutes } }
}

class FakeStudentDao : StudentDao {
    private val profile = MutableStateFlow<StudentProfileEntity?>(null)

    override fun getProfileFlow(): Flow<StudentProfileEntity?> = profile.asStateFlow()
    override suspend fun getProfile(): StudentProfileEntity? = profile.value
    override suspend fun saveProfile(profileEntity: StudentProfileEntity) { profile.value = profileEntity }
}

class FakeStudyPreferencesDao : StudyPreferencesDao {
    private val preferences = MutableStateFlow<StudyPreferencesEntity?>(null)

    override fun getPreferencesFlow(): Flow<StudyPreferencesEntity?> = preferences.asStateFlow()
    override suspend fun getPreferences(): StudyPreferencesEntity? = preferences.value
    override suspend fun savePreferences(prefs: StudyPreferencesEntity) { preferences.value = prefs }
}
