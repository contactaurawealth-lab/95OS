package com.os95.app.features.papers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamDayProtocolTest {

    @Test
    fun testChecklistContainsCriticalExamItems() {
        val checklist = DEFAULT_EXAM_CHECKLIST
        assertTrue("Checklist must not be empty", checklist.isNotEmpty())

        val ids = checklist.map { it.id }
        assertTrue("Must include admit card", ids.contains("admit_card"))
        assertTrue("Must include pens", ids.contains("pens"))
        assertTrue("Must include analog watch", ids.contains("watch"))
        assertTrue("Must include pouch", ids.contains("pouch"))
        assertTrue("Must include water bottle", ids.contains("water"))

        checklist.forEach { item ->
            assertFalse(item.title.isBlank())
            assertFalse(item.detail.isBlank())
        }
    }

    @Test
    fun testTimelineSequence() {
        val timeline = TIMELINE_STEPS
        assertEquals(5, timeline.size)
        assertEquals("T - 3:00 Hours", timeline[0].timeLabel)
        assertEquals("T - 2:00 Hours", timeline[1].timeLabel)
        assertEquals("T - 1:00 Hour", timeline[2].timeLabel)
        assertEquals("T - 0:30 Hour", timeline[3].timeLabel)
        assertEquals("T - 0:15 Hour", timeline[4].timeLabel)

        timeline.forEach { step ->
            assertTrue(step.instructions.isNotEmpty())
            assertFalse(step.caution.isBlank())
        }
    }

    @Test
    fun testPrimingCardsContent() {
        val cards = PRIMING_CARDS
        assertEquals(5, cards.size)
        assertTrue(cards[0].first.contains("Box Breathing"))
        assertTrue(cards[1].first.contains("First-Pass Paper Scan"))
        assertTrue(cards[2].first.contains("Time-to-Mark"))
        assertTrue(cards[3].first.contains("Blocked"))
        assertTrue(cards[4].first.contains("Final 15-Minute Audit"))
    }
}
