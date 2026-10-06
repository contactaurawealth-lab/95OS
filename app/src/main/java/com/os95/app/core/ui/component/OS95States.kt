package com.os95.app.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.os95.app.core.ui.theme.OS95Theme

@Composable
fun OS95EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 36.dp)
            .semantics { contentDescription = "$title: $description" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.mutedText,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(spacing.m))
        Text(
            text = title,
            style = typography.sectionTitle,
            color = colors.primaryText,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(spacing.xs))
        Text(
            text = description,
            style = typography.bodySmall,
            color = colors.secondaryText,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (primaryActionLabel != null && onPrimaryAction != null) {
            Spacer(modifier = Modifier.height(spacing.l))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OS95Button(
                    text = primaryActionLabel,
                    onClick = onPrimaryAction
                )
                if (secondaryActionLabel != null && onSecondaryAction != null) {
                    Spacer(modifier = Modifier.width(spacing.m))
                    OS95OutlinedButton(
                        text = secondaryActionLabel,
                        onClick = onSecondaryAction
                    )
                }
            }
        }
    }
}

@Composable
fun OS95ErrorState(
    modifier: Modifier = Modifier,
    title: String = "Something couldn't be loaded",
    message: String = "Your saved progress and offline data are completely safe.",
    retryLabel: String? = "Try Again",
    onRetry: (() -> Unit)? = null
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .semantics { contentDescription = "$title: $message" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = colors.error,
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(spacing.m))
        Text(
            text = title,
            style = typography.sectionTitle,
            color = colors.primaryText,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(spacing.xs))
        Text(
            text = message,
            style = typography.bodySmall,
            color = colors.secondaryText,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (retryLabel != null && onRetry != null) {
            Spacer(modifier = Modifier.height(spacing.l))
            OS95OutlinedButton(
                text = retryLabel,
                onClick = onRetry
            )
        }
    }
}

@Composable
fun OS95LoadingState(
    modifier: Modifier = Modifier,
    message: String = "Loading offline workspace..."
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp,
                color = colors.accent
            )
            Spacer(modifier = Modifier.height(spacing.m))
            Text(
                text = message,
                style = typography.caption,
                color = colors.secondaryText
            )
        }
    }
}

@Composable
fun OS95ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val colors = OS95Theme.colors
    val shapes = OS95Theme.shapes

    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(shapes.pill),
        color = colors.accent,
        trackColor = colors.border
    )
}
