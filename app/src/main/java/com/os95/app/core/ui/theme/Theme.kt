package com.os95.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

enum class ThemeMode(val displayName: String, val isDarkTheme: Boolean) {
    SYSTEM("System Default", false),
    WARM_OBSIDIAN("Warm Obsidian", true),
    PAPER_WHITE("Paper White", false),
    GRAPHITE_CHAMBER("Graphite Chamber", true),
    SEPIA_SCHOLAR("Sepia Scholar", false),
    FOREST_SLATE("Forest Slate", true),
    LIGHT("Paper White (Legacy)", false),
    DARK("Warm Obsidian (Legacy)", true);

    companion object {
        val userSelectableThemes = listOf(
            SYSTEM,
            WARM_OBSIDIAN,
            PAPER_WHITE,
            GRAPHITE_CHAMBER,
            SEPIA_SCHOLAR,
            FOREST_SLATE
        )
    }
}

fun createDarkObsidianColors() = OS95Colors(
    background = DarkBackground,
    surface = DarkSurface,
    cardBackground = DarkCardBackground,
    primaryText = DarkPrimaryText,
    secondaryText = DarkSecondaryText,
    mutedText = DarkMutedText,
    border = DarkBorder,
    accent = DarkAccent,
    accentCyan = DarkAccentCyan,
    success = ColorSuccess,
    warning = ColorWarning,
    error = ColorError,
    isDark = true
)

fun createLightPaperColors() = OS95Colors(
    background = LightBackground,
    surface = LightSurface,
    cardBackground = LightCardBackground,
    primaryText = LightPrimaryText,
    secondaryText = LightSecondaryText,
    mutedText = LightMutedText,
    border = LightBorder,
    accent = LightAccent,
    accentCyan = LightAccentCyan,
    success = ColorSuccess,
    warning = ColorWarning,
    error = ColorError,
    isDark = false
)

fun createGraphiteColors() = OS95Colors(
    background = GraphiteBackground,
    surface = GraphiteSurface,
    cardBackground = GraphiteCardBackground,
    primaryText = GraphitePrimaryText,
    secondaryText = GraphiteSecondaryText,
    mutedText = GraphiteMutedText,
    border = GraphiteBorder,
    accent = GraphiteAccent,
    accentCyan = GraphiteAccentCyan,
    success = ColorSuccess,
    warning = ColorWarning,
    error = ColorError,
    isDark = true
)

fun createSepiaColors() = OS95Colors(
    background = SepiaBackground,
    surface = SepiaSurface,
    cardBackground = SepiaCardBackground,
    primaryText = SepiaPrimaryText,
    secondaryText = SepiaSecondaryText,
    mutedText = SepiaMutedText,
    border = SepiaBorder,
    accent = SepiaAccent,
    accentCyan = SepiaAccentCyan,
    success = ColorSuccess,
    warning = ColorWarning,
    error = ColorError,
    isDark = false
)

fun createForestColors() = OS95Colors(
    background = ForestBackground,
    surface = ForestSurface,
    cardBackground = ForestCardBackground,
    primaryText = ForestPrimaryText,
    secondaryText = ForestSecondaryText,
    mutedText = ForestMutedText,
    border = ForestBorder,
    accent = ForestAccent,
    accentCyan = ForestAccentCyan,
    success = ColorSuccess,
    warning = ColorWarning,
    error = ColorError,
    isDark = true
)

@Composable
fun OS95Theme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()

    val colors = when (themeMode) {
        ThemeMode.SYSTEM -> if (isSystemDark) createDarkObsidianColors() else createLightPaperColors()
        ThemeMode.WARM_OBSIDIAN, ThemeMode.DARK -> createDarkObsidianColors()
        ThemeMode.PAPER_WHITE, ThemeMode.LIGHT -> createLightPaperColors()
        ThemeMode.GRAPHITE_CHAMBER -> createGraphiteColors()
        ThemeMode.SEPIA_SCHOLAR -> createSepiaColors()
        ThemeMode.FOREST_SLATE -> createForestColors()
    }

    CompositionLocalProvider(
        LocalOS95Colors provides colors,
        LocalOS95Typography provides DefaultOS95Typography,
        LocalOS95Shapes provides OS95Shapes(),
        LocalOS95Spacing provides OS95Spacing(),
        content = content
    )
}

object OS95Theme {
    val colors: OS95Colors
        @Composable
        @ReadOnlyComposable
        get() = LocalOS95Colors.current

    val typography: OS95Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalOS95Typography.current

    val shapes: OS95Shapes
        @Composable
        @ReadOnlyComposable
        get() = LocalOS95Shapes.current

    val spacing: OS95Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalOS95Spacing.current
}
