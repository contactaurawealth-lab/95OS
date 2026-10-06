package com.os95.app.core.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AcousticModeTest {

    @Test
    fun testAcousticModesProperties() {
        val modes = AcousticMode.values()
        assertEquals(4, modes.size)

        assertEquals("Mute", AcousticMode.OFF.displayName)
        assertEquals("Brown Noise", AcousticMode.BROWNIAN.displayName)
        assertEquals("Pink Noise", AcousticMode.PINK.displayName)
        assertEquals("Exam Clock", AcousticMode.CLOCK_TICK.displayName)

        modes.forEach { mode ->
            assertNotNull(mode.displayName)
            assertNotNull(mode.description)
        }
    }
}
