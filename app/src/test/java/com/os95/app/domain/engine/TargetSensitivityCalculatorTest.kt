package com.os95.app.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetSensitivityCalculatorTest {

    @Test
    fun testSensitivityCalculation_computesAccurateCompensatoryMarks() {
        val subjects = listOf(
            SubjectScoreInput("math", "Mathematics", 95f, 100f),
            SubjectScoreInput("phy", "Physics", 95f, 100f),
            SubjectScoreInput("chem", "Chemistry", 95f, 100f),
            SubjectScoreInput("eng", "English", 95f, 100f)
        )

        // Simulate Physics dropping from 95% to 85% (-10 marks)
        val result = Target95Engine.calculateSensitivity(
            subjects = subjects,
            simulatedSubjectId = "phy",
            simulatedPercentage = 85f,
            targetOverallPercentage = 95.0f
        )

        assertEquals(95.0f, result.baselineOverallPercentage, 0.01f)
        assertEquals(92.5f, result.simulatedOverallPercentage, 0.01f)
        assertEquals(2.5f, result.simulatedGapPercentage, 0.01f)
        assertEquals(10.0f, result.totalCompensatoryMarksNeeded, 0.01f)
        assertTrue(result.isTargetAchievable)

        // Remaining subjects: Math (headroom 5m), Chem (headroom 5m), Eng (headroom 5m) -> total headroom 15m >= 10m
        assertEquals(3, result.subjectCompensations.size)
        val mathComp = result.subjectCompensations.find { it.subjectId == "math" }!!
        assertEquals(3.33f, mathComp.requiredAdditionalMarks, 0.1f)
        assertEquals(98.33f, mathComp.targetPercentage, 0.1f)
    }

    @Test
    fun testSensitivityCalculation_handlesUnachievableTargetGracefully() {
        // All other subjects already near ceiling (99%), cannot make up a 20-mark drop
        val subjects = listOf(
            SubjectScoreInput("math", "Mathematics", 90f, 100f),
            SubjectScoreInput("phy", "Physics", 99f, 100f),
            SubjectScoreInput("chem", "Chemistry", 99f, 100f)
        )

        // Math drops to 70% (needs 15 marks compensation across total 300 marks for 95% = 285 total marks)
        val result = Target95Engine.calculateSensitivity(
            subjects = subjects,
            simulatedSubjectId = "math",
            simulatedPercentage = 70f,
            targetOverallPercentage = 95.0f
        )

        assertFalse(result.isTargetAchievable)
        assertTrue(result.totalCompensatoryMarksNeeded > 2.0f)
    }
}
