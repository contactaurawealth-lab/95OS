package com.os95.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE id = 'student_profile' LIMIT 1")
    fun getProfileFlow(): Flow<StudentProfileEntity?>

    @Query("SELECT * FROM students WHERE id = 'student_profile' LIMIT 1")
    suspend fun getProfile(): StudentProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: StudentProfileEntity)
}

@Dao
interface StudyPreferencesDao {
    @Query("SELECT * FROM study_preferences WHERE id = 'study_preferences' LIMIT 1")
    fun getPreferencesFlow(): Flow<StudyPreferencesEntity?>

    @Query("SELECT * FROM study_preferences WHERE id = 'study_preferences' LIMIT 1")
    suspend fun getPreferences(): StudyPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(prefs: StudyPreferencesEntity)
}

@Dao
interface SyllabusDao {
    // Subjects
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    // Chapters
    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY orderIndex ASC, name ASC")
    fun getChaptersForSubject(subjectId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterById(id: String): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)

    // Topics
    @Query("SELECT * FROM topics WHERE chapterId = :chapterId ORDER BY orderIndex ASC, name ASC")
    fun getTopicsForChapter(chapterId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics ORDER BY name ASC")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: String): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity)

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Delete
    suspend fun deleteTopic(topic: TopicEntity)

    @Query("SELECT COUNT(*) FROM topics WHERE masteryState = 'MASTERED'")
    fun getMasteredTopicsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM topics")
    fun getTotalTopicsCount(): Flow<Int>
}

@Dao
interface PaperPilotDao {
    // Question Bank
    @Query("SELECT * FROM question_bank WHERE subjectId = :subjectId")
    fun getQuestionsForSubject(subjectId: String): Flow<List<QuestionBankEntity>>

    @Query("SELECT * FROM question_bank WHERE chapterId = :chapterId")
    fun getQuestionsForChapter(chapterId: String): Flow<List<QuestionBankEntity>>

    @Query("SELECT * FROM question_bank")
    fun getAllQuestions(): Flow<List<QuestionBankEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionBankEntity)

    // Papers
    @Query("SELECT * FROM papers ORDER BY createdAt DESC")
    fun getAllPapers(): Flow<List<PaperEntity>>

    @Query("SELECT * FROM papers WHERE id = :id LIMIT 1")
    suspend fun getPaperById(id: String): PaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaper(paper: PaperEntity)

    @Update
    suspend fun updatePaper(paper: PaperEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaperQuestion(join: PaperQuestionEntity)

    // Exam Results
    @Query("SELECT * FROM exam_results ORDER BY completedAt DESC")
    fun getAllResults(): Flow<List<ExamResultEntity>>

    @Query("SELECT * FROM exam_results WHERE paperId = :paperId LIMIT 1")
    suspend fun getResultForPaper(paperId: String): ExamResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ExamResultEntity)

    // Lost Marks
    @Query("SELECT * FROM lost_marks WHERE examResultId = :resultId")
    fun getLostMarksForResult(resultId: String): Flow<List<LostMarksEntity>>

    @Query("SELECT * FROM lost_marks")
    fun getAllLostMarks(): Flow<List<LostMarksEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLostMarks(lostMarks: LostMarksEntity)
}

@Dao
interface MistakeDao {
    @Query("SELECT * FROM mistakes WHERE isResolved = 0 ORDER BY createdAt DESC")
    fun getActiveMistakes(): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistakes ORDER BY createdAt DESC")
    fun getAllMistakes(): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistakes WHERE subjectId = :subjectId")
    fun getMistakesBySubject(subjectId: String): Flow<List<MistakeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: MistakeEntity)

    @Update
    suspend fun updateMistake(mistake: MistakeEntity)

    @Delete
    suspend fun deleteMistake(mistake: MistakeEntity)
}

@Dao
interface RecallDao {
    @Query("SELECT * FROM recall_cards WHERE dueDate <= :currentTimestamp ORDER BY dueDate ASC")
    fun getDueCards(currentTimestamp: Long): Flow<List<RecallCardEntity>>

    @Query("SELECT * FROM recall_cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<RecallCardEntity>>

    @Query("SELECT * FROM recall_cards WHERE subjectId = :subjectId")
    fun getCardsBySubject(subjectId: String): Flow<List<RecallCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: RecallCardEntity)

    @Update
    suspend fun updateCard(card: RecallCardEntity)

    @Delete
    suspend fun deleteCard(card: RecallCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewLog(review: RecallReviewEntity)

    @Query("SELECT * FROM recall_reviews ORDER BY reviewTimestamp DESC LIMIT 50")
    fun getRecentReviews(): Flow<List<RecallReviewEntity>>
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity)

    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE completedAt >= :sinceTimestamp")
    fun getTotalMinutesSince(sinceTimestamp: Long): Flow<Int?>
}
