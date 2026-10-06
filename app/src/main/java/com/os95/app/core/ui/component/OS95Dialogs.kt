package com.os95.app.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.os95.app.core.ui.theme.OS95Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OS95Dialog(
    title: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = "",
    dismissButtonText: String? = "Cancel",
    content: (@Composable () -> Unit)? = null
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val shapes = OS95Theme.shapes
    val spacing = OS95Theme.spacing

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Surface(
            shape = shapes.medium,
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = title,
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(spacing.s))
                    Text(
                        text = message,
                        style = typography.bodySmall,
                        color = colors.secondaryText
                    )
                }
                if (content != null) {
                    Spacer(modifier = Modifier.height(spacing.m))
                    content()
                }
                Spacer(modifier = Modifier.height(spacing.xl))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (dismissButtonText != null) {
                        OS95TextButton(
                            text = dismissButtonText,
                            onClick = onDismissRequest
                        )
                        Spacer(modifier = Modifier.width(spacing.s))
                    }
                    OS95Button(
                        text = confirmButtonText,
                        onClick = onConfirm
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OS95Dialog(
    title: String,
    message: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissButtonText: String? = "Cancel"
) {
    OS95Dialog(
        title = title,
        confirmButtonText = confirmButtonText,
        onConfirm = onConfirm,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        message = message,
        dismissButtonText = dismissButtonText,
        content = null
    )
}
