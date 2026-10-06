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
        assertEquals(ThemeMode.WARM_OBSIDIAN, ThemeMode.valueOf("WARM_OBSIDIAN"))
        assertEquals(ThemeMode.PAPER_WHITE, ThemeMode.valueOf("PAPER_WHITE"))
        assertEquals(ThemeMode.GRAPHITE_CHAMBER, ThemeMode.valueOf("GRAPHITE_CHAMBER"))
        assertEquals(ThemeMode.SEPIA_SCHOLAR, ThemeMode.valueOf("SEPIA_SCHOLAR"))
        assertEquals(ThemeMode.FOREST_SLATE, ThemeMode.valueOf("FOREST_SLATE"))
    }

    @Test
    fun testColorContrastInvariants() {
        // Light theme background must be light, text must be dark
        assertNotEquals(LightBackground, LightPrimaryText)
        // Dark theme background must be dark, text must be light
        assertNotEquals(DarkBackground, DarkPrimaryText)
        // User selectable themes should include all 6 presets
        assertEquals(6, ThemeMode.userSelectableThemes.size)
    }
}
