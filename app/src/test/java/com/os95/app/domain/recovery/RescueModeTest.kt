package com.os95.app.domain.recovery

import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.domain.engine.MarksRecoveryEngine
import com.os95.app.domain.model.ForgettingRadarSnapshot
import com.os95.app.domain.model.MarksGapPlan
import com.os95.app.domain.model.RecoveryOpportunity
import com.os95.app.domain.model.RecoveryPriorityLevel
import com.os95.app.domain.model.RescueActionType
import com.os95.app.domain.model.RetentionRiskLevel
import com.os95.app.domain.model.TopicRetentionRisk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RescueModeTest {

    private lateinit var engine: MarksRecoveryEngine

    @Before
    fun setUp() {
        engine = MarksRecoveryEngine()
    }

    private fun createSampleInputs(): Triple<MarksGapPlan, ForgettingRadarSnapshot, List<MistakeEntity>> {
        val opp = RecoveryOpportunity(
            id = "top_alg",
            subjectId = "sub_math",
            subjectName = "Mathematics",
            chapterId = "ch_alg",
            chapterName = "Algebra",
            topicId = "top_alg",
            topicName = "Quadratic Equations",
            lostMarks = 8f,
            priorityScore = 12.0f,
            priorityLevel = RecoveryPriorityLevel.VERY_HIGH,
            dominantMistakeCategory = "Calculation Error",
            actionDescription = "Practice sign consistency"
        )
        val gapPlan = MarksGapPlan(
            targetPercentage = 95.0f,
            currentPercentage = 87.0f,
            percentageGap = 8.0f,
            potentialRecoverableMarks = 8.0f,
            topOpportunities = listOf(opp),
            hasSufficientData = true
        )

        val topicRisk = TopicRetentionRisk(
            topicId = "top_recall",
            topicName = "Trigonometric Identities",
            chapterId = "ch_trig",
            chapterName = "Trigonometry",
            subjectId = "sub_math",
            subjectName = "Mathematics",
            riskLevel = RetentionRiskLevel.CRITICAL,
            cardsDueCount = 6,
            totalCards = 10,
            averageRecallScore = 60.0f,
            daysSinceLastReview = 8,
            recommendedCardCount = 6
        )
        val radarSnapshot = ForgettingRadarSnapshot(
            criticalCount = 1,
            atRiskCount = 0,
            watchCount = 0,
            stableCount = 5,
            topics = listOf(topicRisk),
            totalCardsTracked = 10
        )

        val mistakes = listOf(
            MistakeEntity(
                id = "m1",
                subjectId = "sub_math",
                chapterId = "ch_alg",
                topicId = "top_alg",
                question = "Discriminant calculation: b^2 - 4ac",
                correctAnswer = "Positive discriminant implies two distinct real roots",
                lossCategory = "CALCULATION_ERROR",
                marksLost = 4f,
                isResolved = false
            ),
            MistakeEntity(
                id = "m2",
                subjectId = "sub_math",
                chapterId = "ch_alg",
                topicId = "top_alg",
                question = "Sign error in quadratic formula",
                correctAnswer = "x = (-b +- sqrt(D)) / 2a",
                lossCategory = "CARELESS",
                marksLost = 4f,
                isResolved = false
            )
        )

        return Triple(gapPlan, radarSnapshot, mistakes)
    }

    @Test
    fun testFifteenMinuteSession_allocatesExactBlocks() {
        val (gapPlan, radarSnapshot, mistakes) = createSampleInputs()

        val plan = engine.generateRescuePlan(
            durationMinutes = 15,
            marksGap = gapPlan,
            forgettingRadar = radarSnapshot,
            mistakes = mistakes,
            topics = emptyList()
        )

        assertEquals(15, plan.totalDurationMinutes)
        assertEquals(3, plan.blocks.size)

        // Block 1: Recall review (starts at 0)
        val b1 = plan.blocks[0]
        assertEquals(0, b1.startMinute)
        assertEquals(RescueActionType.RECALL_REVIEW, b1.actionType)
        assertEquals("Trigonometric Identities", b1.targetName)
        assertTrue(b1.endMinute > b1.startMinute)

        // Block 2: Mistake fix
        val b2 = plan.blocks[1]
        assertEquals(b1.endMinute, b2.startMinute)
        assertEquals(RescueActionType.MISTAKE_FIX, b2.actionType)
        assertTrue(b2.endMinute > b2.startMinute)

        // Block 3: Rapid topic read (ends at total minutes)
        val b3 = plan.blocks[2]
        assertEquals(b2.endMinute, b3.startMinute)
        assertEquals(15, b3.endMinute)
        assertEquals(RescueActionType.RAPID_TOPIC_READ, b3.actionType)
        assertEquals("Quadratic Equations", b3.targetName)

        // Total minutes check
        val totalBlockDuration = (b1.endMinute - b1.startMinute) + (b2.endMinute - b2.startMinute) + (b3.endMinute - b3.startMinute)
        assertEquals(15, totalBlockDuration)
        assertTrue(plan.estimatedRecoverableMarks > 0f)
    }

    @Test
    fun testVariableDurations_fiveAndThirtyMinutes() {
        val (gapPlan, radarSnapshot, mistakes) = createSampleInputs()

        // 5-minute session
        val plan5 = engine.generateRescuePlan(
            durationMinutes = 5,
            marksGap = gapPlan,
            forgettingRadar = radarSnapshot,
            mistakes = mistakes,
            topics = emptyList()
        )
        assertEquals(5, plan5.totalDurationMinutes)
        assertEquals(5, plan5.blocks.last().endMinute)

        // 30-minute session
        val plan30 = engine.generateRescuePlan(
            durationMinutes = 30,
            marksGap = gapPlan,
            forgettingRadar = radarSnapshot,
            mistakes = mistakes,
            topics = emptyList()
        )
        assertEquals(30, plan30.totalDurationMinutes)
        assertEquals(30, plan30.blocks.last().endMinute)
    }

    @Test
    fun testFallbackWhenNoMistakesOrWeakTopics() {
        val emptyGap = MarksGapPlan(
            targetPercentage = 95f,
            currentPercentage = 0f,
            percentageGap = 95f,
            potentialRecoverableMarks = 0f
        )
        val emptyRadar = ForgettingRadarSnapshot(0, 0, 0, 0, emptyList(), 0)
        val fallbackTopic = TopicEntity(id = "top_fb", chapterId = "ch_1", name = "Basic Algebra", weaknessScore = 0.6f)

        val plan = engine.generateRescuePlan(
            durationMinutes = 15,
            marksGap = emptyGap,
            forgettingRadar = emptyRadar,
            mistakes = emptyList(),
            topics = listOf(fallbackTopic)
        )

        assertEquals(15, plan.totalDurationMinutes)
        assertEquals(3, plan.blocks.size)
        // Check that fallback action targets are set reasonably
        assertEquals(RescueActionType.RECALL_REVIEW, plan.blocks[0].actionType)
        assertEquals(RescueActionType.MISTAKE_FIX, plan.blocks[1].actionType)
        assertEquals(RescueActionType.RAPID_TOPIC_READ, plan.blocks[2].actionType)
        assertEquals("Basic Algebra", plan.blocks[2].targetName)
    }
}
