package com.os95.app.domain.engine

import com.os95.app.domain.model.RecallRating
import kotlin.math.ceil
import kotlin.math.max

data class SM2Result(
    val nextIntervalDays: Int,
    val nextEaseFactor: Float,
    val nextRepetitions: Int,
    val nextDueTimestamp: Long
)

object SM2Engine {
    private const val ONE_DAY_MS = 86_400_000L
    private const val MIN_EASE_FACTOR = 1.3f

    fun calculateNextReview(
        currentIntervalDays: Int,
        currentEaseFactor: Float,
        currentRepetitions: Int,
        rating: RecallRating,
        nowMs: Long = System.currentTimeMillis()
    ): SM2Result {
        return when (rating) {
            RecallRating.AGAIN -> {
                val newEase = max(MIN_EASE_FACTOR, currentEaseFactor - 0.2f)
                SM2Result(
                    nextIntervalDays = 1,
                    nextEaseFactor = newEase,
                    nextRepetitions = 0,
                    nextDueTimestamp = nowMs + ONE_DAY_MS
                )
            }
            RecallRating.HARD -> {
                val newReps = currentRepetitions + 1
                val newInterval = when (newReps) {
                    1 -> 1
                    2 -> 2
                    else -> max(2, ceil(currentIntervalDays * 1.2f).toInt())
                }
                val newEase = max(MIN_EASE_FACTOR, currentEaseFactor - 0.15f)
                SM2Result(
                    nextIntervalDays = newInterval,
                    nextEaseFactor = newEase,
                    nextRepetitions = newReps,
                    nextDueTimestamp = nowMs + (newInterval * ONE_DAY_MS)
                )
            }
            RecallRating.GOOD -> {
                val newReps = currentRepetitions + 1
                val newInterval = when (newReps) {
                    1 -> 1
                    2 -> 6
                    else -> max(6, ceil(currentIntervalDays * currentEaseFactor).toInt())
                }
                SM2Result(
                    nextIntervalDays = newInterval,
                    nextEaseFactor = currentEaseFactor,
                    nextRepetitions = newReps,
                    nextDueTimestamp = nowMs + (newInterval * ONE_DAY_MS)
                )
            }
            RecallRating.EASY -> {
                val newReps = currentRepetitions + 1
                val newEase = currentEaseFactor + 0.15f
                val newInterval = when (newReps) {
                    1 -> 4
                    2 -> 10
                    else -> max(10, ceil(currentIntervalDays * newEase * 1.3f).toInt())
                }
                SM2Result(
                    nextIntervalDays = newInterval,
                    nextEaseFactor = newEase,
                    nextRepetitions = newReps,
                    nextDueTimestamp = nowMs + (newInterval * ONE_DAY_MS)
                )
            }
        }
    }
}
