package com.os95.app.domain

import com.os95.app.domain.engine.Target95Engine
import org.junit.Assert.assertEquals
import org.junit.Test

class Target95EngineTest {

    @Test
    fun testEmptyExamScoresYieldsZeroCurrentScoreAndFullGap() {
        val metrics = Target95Engine.calculateMetrics(
            latestExamScores = emptyList(),
            totalLostMarks = 0f,
            recoveredMarks = 0f,
            masteredTopicsCount = 0,
            totalTopicsCount = 10,
            totalStudyMinutes = 0,
            targetScorePercentage = 95.0f
        )

        assertEquals(0f, metrics.currentScorePercentage, 0.01f)
        assertEquals(95.0f, metrics.marksGapPercentage, 0.01f)
        assertEquals(0f, metrics.syllabusCompletionPercentage, 0.01f)
    }

    @Test
    fun testScoreCalculationAndGap() {
        // Exam 1: 40/50 (80%), Exam 2: 50/50 (100%) -> Total 90/100 = 90%
        val scores = listOf(Pair(40f, 50f), Pair(50f, 50f))
        val metrics = Target95Engine.calculateMetrics(
            latestExamScores = scores,
            totalLostMarks = 10f,
            recoveredMarks = 5f,
            masteredTopicsCount = 8,
            totalTopicsCount = 10,
            totalStudyMinutes = 120,
            targetScorePercentage = 95.0f
        )

        assertEquals(90.0f, metrics.currentScorePercentage, 0.01f)
        assertEquals(5.0f, metrics.marksGapPercentage, 0.01f) // 95 - 90 = 5
        assertEquals(80.0f, metrics.syllabusCompletionPercentage, 0.01f) // 8/10 = 80%
        assertEquals(10.0f, metrics.totalLostMarks, 0.01f)
        assertEquals(5.0f, metrics.recoveredMarks, 0.01f)
    }

    @Test
    fun testExceedingTargetYieldsZeroGap() {
        val scores = listOf(Pair(98f, 100f))
        val metrics = Target95Engine.calculateMetrics(
            latestExamScores = scores,
            totalLostMarks = 2f,
            recoveredMarks = 1f,
            masteredTopicsCount = 10,
            totalTopicsCount = 10,
            totalStudyMinutes = 200,
            targetScorePercentage = 95.0f
        )

        assertEquals(98.0f, metrics.currentScorePercentage, 0.01f)
        assertEquals(0f, metrics.marksGapPercentage, 0.01f)
        assertEquals(100.0f, metrics.syllabusCompletionPercentage, 0.01f)
    }
}
