package com.os95.app.domain

import com.os95.app.domain.engine.SM2Engine
import com.os95.app.domain.model.RecallRating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SM2EngineTest {

    @Test
    fun testAgainRatingResetsRepetitionsAndInterval() {
        val now = 1_000_000L
        val result = SM2Engine.calculateNextReview(
            currentIntervalDays = 10,
            currentEaseFactor = 2.5f,
            currentRepetitions = 3,
            rating = RecallRating.AGAIN,
            nowMs = now
        )

        assertEquals(1, result.nextIntervalDays)
        assertEquals(0, result.nextRepetitions)
        assertEquals(2.3f, result.nextEaseFactor, 0.01f)
        assertEquals(now + 86_400_000L, result.nextDueTimestamp)
    }

    @Test
    fun testGoodRatingAdvancesInterval() {
        val now = 1_000_000L
        // First repetition
        val first = SM2Engine.calculateNextReview(
            currentIntervalDays = 1,
            currentEaseFactor = 2.5f,
            currentRepetitions = 0,
            rating = RecallRating.GOOD,
            nowMs = now
        )
        assertEquals(1, first.nextIntervalDays)
        assertEquals(1, first.nextRepetitions)

        // Second repetition
        val second = SM2Engine.calculateNextReview(
            currentIntervalDays = 1,
            currentEaseFactor = 2.5f,
            currentRepetitions = 1,
            rating = RecallRating.GOOD,
            nowMs = now
        )
        assertEquals(6, second.nextIntervalDays)
        assertEquals(2, second.nextRepetitions)

        // Third repetition scales by ease factor
        val third = SM2Engine.calculateNextReview(
            currentIntervalDays = 6,
            currentEaseFactor = 2.5f,
            currentRepetitions = 2,
            rating = RecallRating.GOOD,
            nowMs = now
        )
        assertEquals(15, third.nextIntervalDays)
        assertEquals(3, third.nextRepetitions)
    }

    @Test
    fun testEaseFactorFloorIsRespected() {
        var ease = 1.4f
        for (i in 1..5) {
            val res = SM2Engine.calculateNextReview(
                currentIntervalDays = 1,
                currentEaseFactor = ease,
                currentRepetitions = 0,
                rating = RecallRating.AGAIN
            )
            ease = res.nextEaseFactor
        }
        assertTrue("Ease factor must never drop below 1.3", ease >= 1.3f)
    }
}
