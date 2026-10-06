package com.os95.app.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.os95.app.core.ui.theme.OS95Theme

@Composable
fun OS95Card(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable () -> Unit
) {
    val colors = OS95Theme.colors
    val shapes = OS95Theme.shapes

    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = cardModifier,
        shape = shapes.medium,
        color = backgroundColor ?: colors.surface,
        border = BorderStroke(1.dp, borderColor ?: colors.border)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
