package com.os95.app.features.syllabus

import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.data.repository.OfflineSyllabusRepository
import com.os95.app.testutil.FakeSyllabusDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SyllabusBatchMasteryTest {

    private lateinit var dao: FakeSyllabusDao
    private lateinit var repository: OfflineSyllabusRepository

    @Before
    fun setUp() {
        dao = FakeSyllabusDao()
        repository = OfflineSyllabusRepository(dao)
    }

    @Test
    fun testUpdateChapterTopicsMastery_updatesAllTopicsInTargetChapter() = runBlocking {
        val sub = SubjectEntity(id = "sub_math", name = "Mathematics")
        val ch1 = ChapterEntity(id = "ch_calc", subjectId = "sub_math", name = "Calculus")
        val ch2 = ChapterEntity(id = "ch_alg", subjectId = "sub_math", name = "Algebra")

        val t1 = TopicEntity(id = "t1", chapterId = "ch_calc", name = "Limits", masteryState = "NOT_STARTED")
        val t2 = TopicEntity(id = "t2", chapterId = "ch_calc", name = "Derivatives", masteryState = "LEARNING")
        val t3 = TopicEntity(id = "t3", chapterId = "ch_alg", name = "Matrices", masteryState = "NOT_STARTED")

        dao.insertSubject(sub)
        dao.insertChapters(listOf(ch1, ch2))
        dao.insertTopics(listOf(t1, t2, t3))

        // Update all topics in Calculus to REVISED
        repository.updateChapterTopicsMastery("ch_calc", "REVISED")

        val calcTopics = dao.getTopicsForChapterSync("ch_calc")
        assertEquals(2, calcTopics.size)
        assertEquals("REVISED", calcTopics[0].masteryState)
        assertEquals("REVISED", calcTopics[1].masteryState)

        // Topic in Algebra should remain NOT_STARTED
        val algTopics = dao.getTopicsForChapterSync("ch_alg")
        assertEquals(1, algTopics.size)
        assertEquals("NOT_STARTED", algTopics[0].masteryState)

        // Now update all Calculus to MASTERED
        repository.updateChapterTopicsMastery("ch_calc", "MASTERED")
        val calcTopicsMastered = dao.getTopicsForChapterSync("ch_calc")
        assertEquals("MASTERED", calcTopicsMastered[0].masteryState)
        assertEquals("MASTERED", calcTopicsMastered[1].masteryState)
    }
}
