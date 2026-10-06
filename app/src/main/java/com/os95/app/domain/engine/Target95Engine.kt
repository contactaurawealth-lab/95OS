package com.os95.app.domain.engine

data class Target95Metrics(
    val currentScorePercentage: Float,
    val targetScorePercentage: Float = 95.0f,
    val marksGapPercentage: Float,
    val totalLostMarks: Float,
    val recoveredMarks: Float,
    val syllabusCompletionPercentage: Float,
    val totalStudyMinutes: Int
)

object Target95Engine {
    fun calculateMetrics(
        latestExamScores: List<Pair<Float, Float>>, // Pair(marksObtained, totalMarks)
        totalLostMarks: Float,
        recoveredMarks: Float,
        masteredTopicsCount: Int,
        totalTopicsCount: Int,
        totalStudyMinutes: Int,
        targetScorePercentage: Float = 95.0f
    ): Target95Metrics {
        val currentAveragePercentage = if (latestExamScores.isNotEmpty()) {
            val totalObtained = latestExamScores.sumOf { it.first.toDouble() }
            val totalPossible = latestExamScores.sumOf { it.second.toDouble() }
            if (totalPossible > 0.0) ((totalObtained / totalPossible) * 100.0).toFloat() else 0f
        } else {
            0f
        }

        val gap = (targetScorePercentage - currentAveragePercentage).coerceAtLeast(0f)
        val syllabusProgress = if (totalTopicsCount > 0) {
            ((masteredTopicsCount.toFloat() / totalTopicsCount.toFloat()) * 100.0f).coerceIn(0f, 100f)
        } else {
            0f
        }

        return Target95Metrics(
            currentScorePercentage = currentAveragePercentage,
            targetScorePercentage = targetScorePercentage,
            marksGapPercentage = gap,
            totalLostMarks = totalLostMarks,
            recoveredMarks = recoveredMarks,
            syllabusCompletionPercentage = syllabusProgress,
            totalStudyMinutes = totalStudyMinutes
        )
    }
}
