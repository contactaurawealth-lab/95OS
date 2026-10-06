package com.os95.app.features.settings

import com.os95.app.core.csv.CsvDatasetType
import com.os95.app.core.csv.UniversalMarkdownEngine
import com.os95.app.core.ui.theme.ThemeMode
import com.os95.app.features.onboarding.OnboardingViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsAndDataPortabilityTest {

    private lateinit var markdownEngine: UniversalMarkdownEngine

    @Before
    fun setUp() {
        markdownEngine = UniversalMarkdownEngine()
    }

    @Test
    fun testMarkdownEngineProvidesTemplatesForDatasets() {
        val syllabusTmpl = markdownEngine.getMarkdownTemplate(CsvDatasetType.SYLLABUS)
        assertTrue(syllabusTmpl.contains("# Mathematics"))
        assertTrue(syllabusTmpl.contains("## Real Numbers"))

        val questionsTmpl = markdownEngine.getMarkdownTemplate(CsvDatasetType.QUESTIONS)
        assertTrue(questionsTmpl.contains("### Question:"))
        assertTrue(questionsTmpl.contains("- Subject:"))

        val recallTmpl = markdownEngine.getMarkdownTemplate(CsvDatasetType.RECALL_CARDS)
        assertTrue(recallTmpl.contains("## Card:"))
        assertTrue(recallTmpl.contains("- Back:"))

        val mistakesTmpl = markdownEngine.getMarkdownTemplate(CsvDatasetType.MISTAKES)
        assertTrue(mistakesTmpl.contains("### Mistake:"))
        assertTrue(mistakesTmpl.contains("- Category:"))
    }

    @Test
    fun testDataPortabilityFormatEnumDefinitions() {
        val formats = DataPortabilityFormat.values()
        assertEquals(2, formats.size)
        assertEquals(DataPortabilityFormat.CSV, formats[0])
        assertEquals(DataPortabilityFormat.MARKDOWN, formats[1])
        assertEquals(".csv", DataPortabilityFormat.CSV.extension)
        assertEquals(".md", DataPortabilityFormat.MARKDOWN.extension)
    }

    @Test
    fun testAllSubjectsCatalogInOnboarding_contains18PlusSubjects() {
        val subjects = OnboardingViewModel.ALL_SUBJECTS
        assertTrue("Must contain at least 13+ subjects", subjects.size >= 13)
        assertEquals(18, subjects.size)

        // Verify key subjects from all 4 streams exist
        val names = subjects.map { it.name }.toSet()
        assertTrue(names.contains("Mathematics"))
        assertTrue(names.contains("Physics"))
        assertTrue(names.contains("Chemistry"))
        assertTrue(names.contains("Biology"))
        assertTrue(names.contains("Computer Science"))
        assertTrue(names.contains("History"))
        assertTrue(names.contains("Political Science"))
        assertTrue(names.contains("Economics"))
        assertTrue(names.contains("Accountancy"))
        assertTrue(names.contains("Business Studies"))
        assertTrue(names.contains("Psychology"))
        assertTrue(names.contains("English Literature"))

        // Check each has valid color
        subjects.forEach { sub ->
            assertTrue(sub.colorHex.startsWith("#"))
            assertTrue(sub.stream.isNotBlank())
        }
    }

    @Test
    fun testThemeModeCustomThemes() {
        assertEquals(6, ThemeMode.userSelectableThemes.size)
        assertTrue(ThemeMode.userSelectableThemes.contains(ThemeMode.WARM_OBSIDIAN))
        assertTrue(ThemeMode.userSelectableThemes.contains(ThemeMode.PAPER_WHITE))
        assertTrue(ThemeMode.userSelectableThemes.contains(ThemeMode.GRAPHITE_CHAMBER))
        assertTrue(ThemeMode.userSelectableThemes.contains(ThemeMode.SEPIA_SCHOLAR))
        assertTrue(ThemeMode.userSelectableThemes.contains(ThemeMode.FOREST_SLATE))
        assertTrue(ThemeMode.userSelectableThemes.contains(ThemeMode.SYSTEM))
    }
}
