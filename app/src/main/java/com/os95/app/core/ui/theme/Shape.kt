package com.os95.app.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class OS95Shapes(
    val small: Shape = RoundedCornerShape(6.dp),
    val medium: Shape = RoundedCornerShape(10.dp),
    val large: Shape = RoundedCornerShape(14.dp),
    val pill: Shape = RoundedCornerShape(50.dp)
)

val LocalOS95Shapes = staticCompositionLocalOf { OS95Shapes() }
