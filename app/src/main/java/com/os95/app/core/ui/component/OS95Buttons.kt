package com.os95.app.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.os95.app.core.ui.theme.OS95Theme

@Composable
fun OS95Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    isSecondary: Boolean = false
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val shapes = OS95Theme.shapes
    val spacing = OS95Theme.spacing

    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = spacing.minTouchTarget),
        enabled = enabled,
        shape = shapes.medium,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSecondary) colors.cardBackground else colors.accent,
            contentColor = if (isSecondary) colors.primaryText else colors.surface,
            disabledContainerColor = colors.border,
            disabledContentColor = colors.mutedText
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(spacing.s))
            }
            Text(
                text = text,
                style = typography.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            )
        }
    }
}

@Composable
fun OS95OutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val shapes = OS95Theme.shapes
    val spacing = OS95Theme.spacing

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = spacing.minTouchTarget),
        enabled = enabled,
        shape = shapes.medium,
        border = BorderStroke(1.dp, colors.border),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.primaryText,
            disabledContentColor = colors.mutedText
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colors.primaryText
                )
                Spacer(modifier = Modifier.width(spacing.s))
            }
            Text(
                text = text,
                style = typography.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            )
        }
    }
}

@Composable
fun OS95TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = spacing.minTouchTarget),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = colors.accent,
            disabledContentColor = colors.mutedText
        )
    ) {
        Text(
            text = text,
            style = typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
        )
    }
}

@Composable
fun OS95IconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = OS95Theme.colors
    val shapes = OS95Theme.shapes
    val spacing = OS95Theme.spacing

    Box(
        modifier = modifier
            .size(spacing.minTouchTarget)
            .clip(shapes.small)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) colors.primaryText else colors.mutedText,
            modifier = Modifier.size(20.dp)
        )
    }
}
