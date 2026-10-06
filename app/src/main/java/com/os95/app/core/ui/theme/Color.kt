package com.os95.app.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// 95OS Light Palette (Warm academic paper, restrained graphite, amber accent)
val LightBackground = Color(0xFFF8F8F5)
val LightSurface = Color(0xFFFFFFFF)
val LightCardBackground = Color(0xFFF0F0EA)
val LightPrimaryText = Color(0xFF181816)
val LightSecondaryText = Color(0xFF62625C)
val LightMutedText = Color(0xFF909088)
val LightBorder = Color(0xFFDEDDD5)
val LightAccent = Color(0xFFB8781E) // Restrained Amber
val LightAccentCyan = Color(0xFF0284C7) // Target Cyan

// 95OS Dark Palette (Warm obsidian, charcoal graphite, muted gold / cyan)
val DarkBackground = Color(0xFF121211)
val DarkSurface = Color(0xFF1C1C1A)
val DarkCardBackground = Color(0xFF242421)
val DarkPrimaryText = Color(0xFFF4F4EE)
val DarkSecondaryText = Color(0xFFAFAFA4)
val DarkMutedText = Color(0xFF7E7E75)
val DarkBorder = Color(0xFF2D2D29)
val DarkAccent = Color(0xFFD99B38) // Warm Amber
val DarkAccentCyan = Color(0xFF38BDF8) // Electric Target Cyan

// Status & Metric Colors (Calm, non-vibrant)
val ColorSuccess = Color(0xFF2E7D32)
val ColorWarning = Color(0xFFD97706)
val ColorError = Color(0xFFC62828)

@Immutable
data class OS95Colors(
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val border: Color,
    val accent: Color,
    val accentCyan: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val isDark: Boolean
)

val LocalOS95Colors = staticCompositionLocalOf {
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
