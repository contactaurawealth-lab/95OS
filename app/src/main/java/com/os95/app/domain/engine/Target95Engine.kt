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

    /**
     * Target Score Sensitivity Calculator ("What-If" Analysis).
     * Simulates what happens if a subject score drops or rises, and deterministically
     * computes the exact compensatory marks needed in remaining subjects to maintain >= 95%.
     */
    fun calculateSensitivity(
        subjects: List<SubjectScoreInput>,
        simulatedSubjectId: String,
        simulatedPercentage: Float,
        targetOverallPercentage: Float = 95.0f
    ): TargetSensitivityResult {
        if (subjects.isEmpty()) {
            return TargetSensitivityResult(
                baselineOverallPercentage = 0f,
                simulatedOverallPercentage = 0f,
                targetPercentage = targetOverallPercentage,
                simulatedGapPercentage = targetOverallPercentage,
                totalCompensatoryMarksNeeded = 0f,
                isTargetAchievable = false,
                subjectCompensations = emptyList()
            )
        }

        val totalPossibleMarks = subjects.sumOf { it.totalExamMarks.toDouble() }.toFloat()
        val baselineObtained = subjects.sumOf { (it.currentPercentage * it.totalExamMarks / 100.0) }.toFloat()
        val baselineOverall = if (totalPossibleMarks > 0f) (baselineObtained / totalPossibleMarks) * 100f else 0f

        // Simulated marks
        val simulatedObtained = subjects.sumOf { s ->
            val pct = if (s.subjectId == simulatedSubjectId) simulatedPercentage else s.currentPercentage
            (pct * s.totalExamMarks / 100.0)
        }.toFloat()

        val simulatedOverall = if (totalPossibleMarks > 0f) (simulatedObtained / totalPossibleMarks) * 100f else 0f
        val simulatedGap = (targetOverallPercentage - simulatedOverall).coerceAtLeast(0f)
        val targetTotalMarks = (targetOverallPercentage * totalPossibleMarks / 100f)
        val compensatoryMarksNeeded = (targetTotalMarks - simulatedObtained).coerceAtLeast(0f)

        // Distribute compensation across remaining subjects proportionally to their available headroom (100% - current%)
        val remainingSubjects = subjects.filter { it.subjectId != simulatedSubjectId }
        val totalAvailableHeadroom = remainingSubjects.sumOf {
            val headroomPct = (100f - it.currentPercentage).coerceAtLeast(0f)
            (headroomPct * it.totalExamMarks / 100.0)
        }.toFloat()

        val compensations = remainingSubjects.map { s ->
            val headroomMarks = ((100f - s.currentPercentage).coerceAtLeast(0f) * s.totalExamMarks / 100f)
            val assignedMarks = if (totalAvailableHeadroom > 0f) {
                ((headroomMarks / totalAvailableHeadroom) * compensatoryMarksNeeded).coerceAtMost(headroomMarks)
            } else {
                0f
            }
            val newPct = s.currentPercentage + if (s.totalExamMarks > 0f) (assignedMarks / s.totalExamMarks * 100f) else 0f
            SubjectCompensation(
                subjectId = s.subjectId,
                subjectName = s.subjectName,
                currentPercentage = s.currentPercentage,
                targetPercentage = newPct.coerceAtMost(100f),
                requiredAdditionalMarks = assignedMarks
            )
        }

        val isAchievable = totalAvailableHeadroom >= compensatoryMarksNeeded

        return TargetSensitivityResult(
            baselineOverallPercentage = baselineOverall,
            simulatedOverallPercentage = simulatedOverall,
            targetPercentage = targetOverallPercentage,
            simulatedGapPercentage = simulatedGap,
            totalCompensatoryMarksNeeded = compensatoryMarksNeeded,
            isTargetAchievable = isAchievable,
            subjectCompensations = compensations
        )
    }
}

data class SubjectScoreInput(
    val subjectId: String,
    val subjectName: String,
    val currentPercentage: Float,
    val totalExamMarks: Float = 100f
)

data class SubjectCompensation(
    val subjectId: String,
    val subjectName: String,
    val currentPercentage: Float,
    val targetPercentage: Float,
    val requiredAdditionalMarks: Float
)

data class TargetSensitivityResult(
    val baselineOverallPercentage: Float,
    val simulatedOverallPercentage: Float,
    val targetPercentage: Float,
    val simulatedGapPercentage: Float,
    val totalCompensatoryMarksNeeded: Float,
    val isTargetAchievable: Boolean,
    val subjectCompensations: List<SubjectCompensation>
)

