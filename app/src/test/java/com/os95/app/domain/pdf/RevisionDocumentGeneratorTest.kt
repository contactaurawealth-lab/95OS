package com.os95.app.domain.pdf

import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.domain.model.RetentionRiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RevisionDocumentGeneratorTest {

    private lateinit var generator: RevisionDocumentGenerator

    @Before
    fun setUp() {
        generator = RevisionDocumentGenerator()
    }

    @Test
    fun testGenerateMistakeRemediationMarkdown_formatsMistakeDetailsProperly() {
        val mistakes = listOf(
            MistakeEntity(
                id = "m1",
                subjectId = "sub_math",
                chapterId = "ch_calc",
                topicId = "top1",
                question = "Find the derivative of cos(2x)",
                studentAnswer = "-sin(2x)",
                correctAnswer = "-2sin(2x)",
                lossCategory = "CALCULATION_ERROR",
                marksLost = 2.0f,
                missedCount = 2
            ),
            MistakeEntity(
                id = "m2",
                subjectId = "sub_phy",
                chapterId = "ch_optics",
                topicId = "top2",
                question = "State Snell's Law",
                studentAnswer = "",
                correctAnswer = "n1 * sin(theta1) = n2 * sin(theta2)",
                lossCategory = "CARELESS_MISTAKE",
                marksLost = 1.0f
            )
        )

        val subjectNames = mapOf("sub_math" to "Mathematics", "sub_phy" to "Physics")
        val markdown = generator.generateMistakeRemediationMarkdown(mistakes, subjectNames)

        assertTrue(markdown.contains("# 95OS Mistake Remediation & Recovery Sheet"))
        assertTrue(markdown.contains("Mathematics — Find the derivative of cos(2x)"))
        assertTrue(markdown.contains("CALCULATION ERROR"))
        assertTrue(markdown.contains("2.0 marks"))
        assertTrue(markdown.contains("-2sin(2x)"))
        assertTrue(markdown.contains("Physics — State Snell's Law"))
    }

    @Test
    fun testGenerateMistakeRemediationMarkdown_handlesEmptyMistakes() {
        val markdown = generator.generateMistakeRemediationMarkdown(emptyList(), emptyMap())
        assertTrue(markdown.contains("No unmastered mistakes logged!"))
    }

    @Test
    fun testGenerateForgettingFlashMarkdown_formatsCriticalRetentionCards() {
        val topics = listOf(
            com.os95.app.domain.model.TopicRetentionRisk(
                topicId = "top1",
                topicName = "Entropy & Laws",
                chapterId = "ch1",
                chapterName = "Thermodynamics",
                subjectId = "sub1",
                subjectName = "Chemistry",
                riskLevel = RetentionRiskLevel.CRITICAL,
                cardsDueCount = 6,
                totalCards = 10,
                averageRecallScore = 45f,
                daysSinceLastReview = 5,
                recommendedCardCount = 6
            )
        )

        val markdown = generator.generateForgettingFlashMarkdown(topics)
        assertTrue(markdown.contains("# 95OS Forgetting Radar — Critical Review Flash Sheet"))
        assertTrue(markdown.contains("Chemistry — Thermodynamics / Entropy & Laws [CRITICAL]"))
        assertTrue(markdown.contains("**Recall Performance:** 45%"))
        assertTrue(markdown.contains("**Last Reviewed:** 5 days ago"))
        assertTrue(markdown.contains("6 cards due for active review"))
    }
}
