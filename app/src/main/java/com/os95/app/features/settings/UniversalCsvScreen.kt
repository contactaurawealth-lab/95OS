package com.os95.app.features.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.os95.app.core.csv.CsvDatasetType
import com.os95.app.core.csv.DuplicateResolutionStrategy
import com.os95.app.core.csv.MissingRelationshipMode
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.component.OS95TextField
import com.os95.app.core.ui.component.OS95TopBar
import com.os95.app.core.ui.theme.OS95Theme

@Composable
fun UniversalCsvScreen(
    viewModel: UniversalCsvViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var copiedNotice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = "Universal Data Engine",
            subtitle = "Offline CSV & Markdown (.md) Hub",
            onBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Format Selector: CSV vs Markdown
            Column {
                Text(
                    text = "File Format",
                    style = typography.caption,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DataPortabilityFormat.values().forEach { format ->
                        val isSelected = uiState.selectedFormat == format
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) colors.cardBackground else colors.surface)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) colors.accent else colors.border
                                )
                                .clickable { viewModel.selectFormat(format) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (format == DataPortabilityFormat.CSV) Icons.Outlined.TableChart else Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = if (isSelected) colors.accent else colors.mutedText,
                                    modifier = Modifier.height(18.dp)
                                )
                                Text(
                                    text = format.displayName,
                                    style = typography.bodySmall,
                                    color = if (isSelected) colors.accent else colors.primaryText
                                )
                            }
                        }
                    }
                }
            }

            // Section 1: Dataset Selection
            Column {
                Text(
                    text = "1. Select Academic Dataset",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = "Choose the structure of the ${uiState.selectedFormat.displayName} to import or export.",
                    style = typography.caption,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(spacing.s))

                // Dataset selector grid / pills
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val datasetTypes = CsvDatasetType.values()
                    val rows = datasetTypes.toList().chunked(2)
                    for (row in rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (type in row) {
                                val isSelected = uiState.selectedDatasetType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) colors.cardBackground else colors.surface)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) colors.accent else colors.border
                                        )
                                        .clickable { viewModel.selectDataset(type) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = type.displayName,
                                            style = typography.bodySmall,
                                            color = if (isSelected) colors.accent else colors.primaryText
                                        )
                                        Text(
                                            text = type.id,
                                            style = typography.caption,
                                            color = colors.mutedText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions: Load Template or Export Current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OS95OutlinedButton(
                    text = "Load Template",
                    icon = Icons.Outlined.FileDownload,
                    onClick = { viewModel.loadTemplate() },
                    modifier = Modifier.weight(1f)
                )
                OS95OutlinedButton(
                    text = "Export Current",
                    icon = Icons.Outlined.FileUpload,
                    onClick = { viewModel.exportDataset(uiState.selectedDatasetType) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Section 2: Data Input
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. ${uiState.selectedFormat.displayName} Content",
                        style = typography.sectionTitle,
                        color = colors.primaryText
                    )
                    Text(
                        text = "${uiState.csvContent.length} chars",
                        style = typography.caption,
                        color = colors.mutedText
                    )
                }
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = if (uiState.selectedFormat == DataPortabilityFormat.MARKDOWN) {
                        "Paste structured Markdown outline (# Subject, ## Chapter, - Topic) or Markdown tables."
                    } else {
                        "Paste raw CSV text with standard headers (e.g. subject_name, chapter_name, etc.)."
                    },
                    style = typography.caption,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(spacing.s))

                OS95TextField(
                    value = uiState.csvContent,
                    onValueChange = { viewModel.setContent(it) },
                    label = "${uiState.selectedDatasetType.displayName} ${uiState.selectedFormat.displayName}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 260.dp)
                )
            }

            // Section 3: Import Configuration Strategies
            Column {
                Text(
                    text = "3. Import Resolution Rules",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(spacing.s))

                OS95Card(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Duplicate Strategy
                        Column {
                            Text(
                                text = "Duplicate Handling",
                                style = typography.bodySmall,
                                color = colors.primaryText
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DuplicateResolutionStrategy.values().forEach { strategy ->
                                    val isSelected = uiState.duplicateStrategy == strategy
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isSelected) colors.cardBackground else colors.surface)
                                            .border(
                                                width = if (isSelected) 1.dp else 0.5.dp,
                                                color = if (isSelected) colors.accent else colors.border
                                            )
                                            .clickable { viewModel.setDuplicateStrategy(strategy) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = strategy.displayName,
                                            style = typography.caption,
                                            color = if (isSelected) colors.accent else colors.mutedText
                                        )
                                    }
                                }
                            }
                        }

                        // Missing Relationship Mode
                        Column {
                            Text(
                                text = "Missing Relationship Strategy",
                                style = typography.bodySmall,
                                color = colors.primaryText
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MissingRelationshipMode.values().forEach { mode ->
                                    val isSelected = uiState.missingRelMode == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isSelected) colors.cardBackground else colors.surface)
                                            .border(
                                                width = if (isSelected) 1.dp else 0.5.dp,
                                                color = if (isSelected) colors.accent else colors.border
                                            )
                                            .clickable { viewModel.setMissingRelMode(mode) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (mode) {
                                                MissingRelationshipMode.CREATE_MISSING -> "Auto-Create"
                                                MissingRelationshipMode.FAIL_ON_MISSING -> "Strict Error"
                                                MissingRelationshipMode.SKIP_ROW -> "Skip Row"
                                            },
                                            style = typography.caption,
                                            color = if (isSelected) colors.accent else colors.mutedText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Preview Action Button
            OS95Button(
                text = if (uiState.isAnalyzing) "Analyzing Content..." else "Validate & Preview Import",
                onClick = { viewModel.runPreview() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isAnalyzing && uiState.csvContent.isNotBlank()
            )

            // Status message banner
            uiState.statusMessage?.let { msg ->
                OS95Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.cardBackground
                ) {
                    Text(
                        text = msg,
                        style = typography.bodySmall,
                        color = colors.primaryText
                    )
                }
            }

            // Section 4: Validation & Preview Result
            uiState.previewResult?.let { preview ->
                OS95Card(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Validation Report",
                                style = typography.sectionTitle,
                                color = colors.primaryText
                            )
                            Text(
                                text = if (preview.canProceed) "PASSED" else "FAILED",
                                style = typography.caption,
                                color = if (preview.canProceed) colors.success else colors.error
                            )
                        }

                        // Summary Statistics Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(colors.surface)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(text = "Total Rows", style = typography.caption, color = colors.mutedText)
                                    Text(text = "${preview.totalRowsDetected}", style = typography.sectionTitle, color = colors.primaryText)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(colors.surface)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(text = "Valid Rows", style = typography.caption, color = colors.mutedText)
                                    Text(text = "${preview.validRows.size}", style = typography.sectionTitle, color = colors.success)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(colors.surface)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(text = "Duplicates", style = typography.caption, color = colors.mutedText)
                                    Text(text = "${preview.duplicateCount}", style = typography.sectionTitle, color = colors.accent)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(colors.surface)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(text = "Errors", style = typography.caption, color = colors.mutedText)
                                    Text(text = "${preview.errorCount}", style = typography.sectionTitle, color = if (preview.errorCount > 0) colors.error else colors.mutedText)
                                }
                            }
                        }

                        // Display validation errors if any
                        if (preview.errors.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Validation Errors (${preview.errors.size})",
                                    style = typography.caption,
                                    color = colors.error
                                )
                                preview.errors.take(10).forEach { err ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ErrorOutline,
                                            contentDescription = "Error",
                                            tint = colors.error,
                                            modifier = Modifier.height(16.dp)
                                        )
                                        Text(
                                            text = "Line ${err.lineNumber}: ${err.message}",
                                            style = typography.caption,
                                            color = colors.error
                                        )
                                    }
                                }
                                if (preview.errors.size > 10) {
                                    Text(
                                        text = "... and ${preview.errors.size - 10} more errors",
                                        style = typography.caption,
                                        color = colors.mutedText
                                    )
                                }
                            }
                        }

                        // Confirm Import Button
                        if (preview.canProceed && preview.validRows.isNotEmpty()) {
                            OS95Button(
                                text = if (uiState.isImporting) "Committing Transaction..." else "Confirm Atomic Import",
                                onClick = { viewModel.confirmImport() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isImporting
                            )
                        }
                    }
                }
            }

            // Section 5: Import Completion Summary
            uiState.importSummary?.let { summary ->
                OS95Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.cardBackground
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (summary.isRollback) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle,
                                contentDescription = "Status",
                                tint = if (summary.isRollback) colors.error else colors.accent
                            )
                            Text(
                                text = if (summary.isRollback) "Import Failed (Rolled Back)" else "Import Succeeded",
                                style = typography.sectionTitle,
                                color = if (summary.isRollback) colors.error else colors.primaryText
                            )
                        }

                        if (summary.isRollback) {
                            Text(
                                text = summary.errorMessage ?: "Transaction rolled back without saving partial data.",
                                style = typography.bodySmall,
                                color = colors.error
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "• Total records saved: ${summary.totalImported}",
                                    style = typography.bodySmall,
                                    color = colors.primaryText
                                )
                                Text(
                                    text = "• Duplicates skipped: ${summary.duplicatesSkipped}",
                                    style = typography.caption,
                                    color = colors.secondaryText
                                )
                                if (summary.subjectsCreated > 0) {
                                    Text(
                                        text = "• New subjects: ${summary.subjectsCreated}",
                                        style = typography.caption,
                                        color = colors.secondaryText
                                    )
                                }
                                if (summary.chaptersCreated > 0) {
                                    Text(
                                        text = "• New chapters: ${summary.chaptersCreated}",
                                        style = typography.caption,
                                        color = colors.secondaryText
                                    )
                                }
                                if (summary.topicsCreated > 0) {
                                    Text(
                                        text = "• New topics: ${summary.topicsCreated}",
                                        style = typography.caption,
                                        color = colors.secondaryText
                                    )
                                }
                                if (summary.questionsCreated > 0) {
                                    Text(
                                        text = "• Questions added to Question Bank: ${summary.questionsCreated}",
                                        style = typography.caption,
                                        color = colors.accentCyan
                                    )
                                }
                            }
                        }

                        OS95OutlinedButton(
                            text = "Import Another File",
                            icon = Icons.Outlined.Refresh,
                            onClick = { viewModel.resetImport() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Exported Content Result display
            uiState.exportedContent?.let { exported ->
                OS95Card(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Exported ${uiState.selectedDatasetType.displayName} (${uiState.selectedFormat.displayName})",
                                style = typography.sectionTitle,
                                color = colors.primaryText
                            )
                            OS95OutlinedButton(
                                text = "Copy",
                                icon = Icons.Outlined.ContentCopy,
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("95OS Export", exported)
                                    clipboard.setPrimaryClip(clip)
                                    copiedNotice = "Copied to clipboard!"
                                }
                            )
                        }

                        copiedNotice?.let { notice ->
                            Text(text = notice, style = typography.caption, color = colors.success)
                        }

                        OS95TextField(
                            value = exported,
                            onValueChange = {},
                            readOnly = true,
                            label = "Export Output",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 240.dp)
                        )
                    }
                }
            }
        }
    }
}
