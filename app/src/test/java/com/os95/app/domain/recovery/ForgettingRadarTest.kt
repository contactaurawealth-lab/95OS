package com.os95.app.domain.recovery

import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.RecallCardEntity
import com.os95.app.core.database.entity.RecallReviewEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.domain.engine.MarksRecoveryEngine
import com.os95.app.domain.model.RetentionRiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ForgettingRadarTest {

    private lateinit var engine: MarksRecoveryEngine
    private val nowMs = 1_700_000_000_000L
    private val oneDayMs = MarksRecoveryEngine.ONE_DAY_MS

    private val subject = SubjectEntity(id = "sub_sci", name = "Science")
    private val chapter = ChapterEntity(id = "ch_chem", subjectId = "sub_sci", name = "Chemistry", orderIndex = 1)

    private val criticalTopic = TopicEntity(id = "top_crit", chapterId = "ch_chem", name = "Acids and Bases", orderIndex = 1)
    private val atRiskTopic = TopicEntity(id = "top_risk", chapterId = "ch_chem", name = "Periodic Table", orderIndex = 2)
    private val watchTopic = TopicEntity(id = "top_watch", chapterId = "ch_chem", name = "Chemical Bonding", orderIndex = 3)
    private val stableTopic = TopicEntity(id = "top_stable", chapterId = "ch_chem", name = "Metals and Non-metals", orderIndex = 4, masteryState = "MASTERED")

    @Before
    fun setUp() {
        engine = MarksRecoveryEngine()
    }

    @Test
    fun testCriticalRisk_overdueGreaterThanThreeDays() {
        // Due 5 days ago, low ease factor
        val overdueCard = RecallCardEntity(
            id = "c1",
            subjectId = "sub_sci",
            chapterId = "ch_chem",
            topicId = "top_crit",
            prompt = "What is the pH of pure water?",
            expectedAnswer = "7.0",
            dueDate = nowMs - (5 * oneDayMs),
            intervalDays = 2,
            repetitions = 2,
            easeFactor = 1.5f
        )

        val snapshot = engine.calculateForgettingRadar(
            cards = listOf(overdueCard),
            reviews = emptyList(),
            topics = listOf(criticalTopic),
            chapters = listOf(chapter),
            subjects = listOf(subject),
            nowMs = nowMs
        )

        assertEquals(1, snapshot.criticalCount)
        assertEquals(0, snapshot.atRiskCount)
        assertEquals(1, snapshot.topics.size)
        val topicRisk = snapshot.topics.first()
        assertEquals("top_crit", topicRisk.topicId)
        assertEquals(RetentionRiskLevel.CRITICAL, topicRisk.riskLevel)
        assertEquals(1, topicRisk.cardsDueCount)
    }

    @Test
    fun testAtRisk_overdueBetweenOneAndThreeDays() {
        // Due 2 days ago
        val atRiskCard = RecallCardEntity(
            id = "c2",
            subjectId = "sub_sci",
            chapterId = "ch_chem",
            topicId = "top_risk",
            prompt = "Atomic number of Carbon",
            expectedAnswer = "6",
            dueDate = nowMs - (2 * oneDayMs),
            intervalDays = 4,
            repetitions = 3,
            easeFactor = 2.1f
        )

        val snapshot = engine.calculateForgettingRadar(
            cards = listOf(atRiskCard),
            reviews = emptyList(),
            topics = listOf(atRiskTopic),
            chapters = listOf(chapter),
            subjects = listOf(subject),
            nowMs = nowMs
        )

        assertEquals(0, snapshot.criticalCount)
        assertEquals(1, snapshot.atRiskCount)
        val topicRisk = snapshot.topics.first()
        assertEquals(RetentionRiskLevel.AT_RISK, topicRisk.riskLevel)
    }

    @Test
    fun testWatchRisk_dueSoonOrInReviewWindow() {
        // Due in 1 day (deltaDays = -1.0)
        val watchCard = RecallCardEntity(
            id = "c3",
            subjectId = "sub_sci",
            chapterId = "ch_chem",
            topicId = "top_watch",
            prompt = "Define covalent bond",
            expectedAnswer = "Sharing of electron pairs",
            dueDate = nowMs + (1 * oneDayMs),
            intervalDays = 6,
            repetitions = 4,
            easeFactor = 1.9f // ease factor < 2.0 triggers watch
        )

        val snapshot = engine.calculateForgettingRadar(
            cards = listOf(watchCard),
            reviews = emptyList(),
            topics = listOf(watchTopic),
            chapters = listOf(chapter),
            subjects = listOf(subject),
            nowMs = nowMs
        )

        assertEquals(0, snapshot.criticalCount)
        assertEquals(0, snapshot.atRiskCount)
        assertEquals(1, snapshot.watchCount)
        val topicRisk = snapshot.topics.first()
        assertEquals(RetentionRiskLevel.WATCH, topicRisk.riskLevel)
    }

    @Test
    fun testStableRisk_healthyIntervalAndDueDateFarAway() {
        // Due in 10 days, strong ease factor
        val stableCard = RecallCardEntity(
            id = "c4",
            subjectId = "sub_sci",
            chapterId = "ch_chem",
            topicId = "top_stable",
            prompt = "Properties of Gold",
            expectedAnswer = "Malleable and ductile",
            dueDate = nowMs + (10 * oneDayMs),
            intervalDays = 15,
            repetitions = 6,
            easeFactor = 2.5f
        )

        val snapshot = engine.calculateForgettingRadar(
            cards = listOf(stableCard),
            reviews = emptyList(),
            topics = listOf(stableTopic),
            chapters = listOf(chapter),
            subjects = listOf(subject),
            nowMs = nowMs
        )

        assertEquals(0, snapshot.criticalCount)
        assertEquals(0, snapshot.atRiskCount)
        assertEquals(0, snapshot.watchCount)
        assertEquals(1, snapshot.stableCount)
        val topicRisk = snapshot.topics.first()
        assertEquals(RetentionRiskLevel.STABLE, topicRisk.riskLevel)
        assertEquals(0, topicRisk.cardsDueCount)
    }

    @Test
    fun testTopicWithoutCards_infersRiskFromWeaknessScore() {
        val weakTopic = TopicEntity(
            id = "top_weak_no_cards",
            chapterId = "ch_chem",
            name = "Organic Reactions",
            orderIndex = 5,
            weaknessScore = 0.85f // high weakness
        )

        val snapshot = engine.calculateForgettingRadar(
            cards = emptyList(),
            reviews = emptyList(),
            topics = listOf(weakTopic),
            chapters = listOf(chapter),
            subjects = listOf(subject),
            nowMs = nowMs
        )

        assertEquals(1, snapshot.atRiskCount)
        val item = snapshot.topics.first()
        assertEquals(RetentionRiskLevel.AT_RISK, item.riskLevel)
        assertEquals(0, item.totalCards)
        assertEquals("Create Cards", item.primaryAction)
    }

    @Test
    fun testSortingOrder_criticalBeforeAtRiskBeforeWatchBeforeStable() {
        val c1 = RecallCardEntity(
            id = "c1",
            subjectId = "sub_sci",
            chapterId = "ch_chem",
            topicId = "top_crit",
            prompt = "Acid test",
            expectedAnswer = "Turns litmus red",
            dueDate = nowMs - (5 * oneDayMs),
            easeFactor = 1.3f
        )
        val c2 = RecallCardEntity(
            id = "c2",
            subjectId = "sub_sci",
            chapterId = "ch_chem",
            topicId = "top_risk",
            prompt = "Carbon atomic mass",
            expectedAnswer = "12",
            dueDate = nowMs - (2 * oneDayMs),
            intervalDays = 4,
            repetitions = 2
        )
        val c3 = RecallCardEntity(
            id = "c3",
            subjectId = "sub_sci",
            chapterId = "ch_chem",
            topicId = "top_watch",
            prompt = "Water bond angle",
            expectedAnswer = "104.5 degrees",
            dueDate = nowMs + (1 * oneDayMs),
            easeFactor = 1.9f
        )
        val c4 = RecallCardEntity(
            id = "c4",
            subjectId = "sub_sci",
            chapterId = "ch_chem",
            topicId = "top_stable",
            prompt = "Gold symbol",
            expectedAnswer = "Au",
            dueDate = nowMs + (14 * oneDayMs),
            easeFactor = 2.6f
        )

        val snapshot = engine.calculateForgettingRadar(
            cards = listOf(c4, c2, c1, c3),
            reviews = emptyList(),
            topics = listOf(stableTopic, atRiskTopic, criticalTopic, watchTopic),
            chapters = listOf(chapter),
            subjects = listOf(subject),
            nowMs = nowMs
        )

        assertEquals(4, snapshot.topics.size)
        assertEquals(RetentionRiskLevel.CRITICAL, snapshot.topics[0].riskLevel)
        assertEquals(RetentionRiskLevel.AT_RISK, snapshot.topics[1].riskLevel)
        assertEquals(RetentionRiskLevel.WATCH, snapshot.topics[2].riskLevel)
        assertEquals(RetentionRiskLevel.STABLE, snapshot.topics[3].riskLevel)
    }
}
