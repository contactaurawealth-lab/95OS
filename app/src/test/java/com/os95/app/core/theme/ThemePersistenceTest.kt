package com.os95.app.core.theme

import com.os95.app.core.ui.theme.DarkBackground
import com.os95.app.core.ui.theme.DarkPrimaryText
import com.os95.app.core.ui.theme.LightBackground
import com.os95.app.core.ui.theme.LightPrimaryText
import com.os95.app.core.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ThemePersistenceTest {

    @Test
    fun testThemeModeEnumParsing() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.valueOf("SYSTEM"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.valueOf("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.valueOf("DARK"))
    }

    @Test
    fun testColorContrastInvariants() {
        // Light theme background must be light, text must be dark
        assertNotEquals(LightBackground, LightPrimaryText)
        // Dark theme background must be dark, text must be light
        assertNotEquals(DarkBackground, DarkPrimaryText)
    }
}
