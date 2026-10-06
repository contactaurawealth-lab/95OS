package com.os95.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Composable
fun OS95Theme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colors = if (isDark) {
        OS95Colors(
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
    } else {
        OS95Colors(
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
