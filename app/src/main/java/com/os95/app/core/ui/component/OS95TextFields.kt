package com.os95.app.core.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.os95.app.core.ui.theme.OS95Theme

@Composable
fun OS95TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val shapes = OS95Theme.shapes
    val spacing = OS95Theme.spacing

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = label, style = typography.bodySmall) },
            placeholder = if (placeholder != null) {
                { Text(text = placeholder, style = typography.bodySmall, color = colors.mutedText) }
            } else null,
            isError = isError,
            singleLine = singleLine,
            shape = shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.border,
                errorBorderColor = colors.error,
                focusedTextColor = colors.primaryText,
                unfocusedTextColor = colors.primaryText,
                focusedLabelColor = colors.accent,
                unfocusedLabelColor = colors.secondaryText
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )

        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(spacing.xxs))
            Text(
                text = errorMessage,
                style = typography.caption,
                color = colors.error,
                modifier = Modifier.padding(start = spacing.xs)
            )
        } else if (helperText != null) {
            Spacer(modifier = Modifier.height(spacing.xxs))
            Text(
                text = helperText,
                style = typography.caption,
                color = colors.mutedText,
                modifier = Modifier.padding(start = spacing.xs)
            )
        }
    }
}
