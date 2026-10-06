package com.os95.app.features.settings

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
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Refresh
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = "Universal CSV Engine",
            subtitle = "Offline Import & Export Hub",
            onBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Dataset Selection
            Column {
                Text(
                    text = "1. Select Academic Dataset",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = "Choose the structure of the CSV you wish to import or export.",
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

            // Section 2: CSV Data Input
            Column {
                Text(
                    text = "2. CSV Content",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = "Paste your CSV text below or edit the template directly.",
                    style = typography.caption,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(spacing.s))

                OS95TextField(
                    value = uiState.csvContent,
                    onValueChange = { viewModel.setCsvContent(it) },
                    label = "${uiState.selectedDatasetType.displayName} CSV",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 260.dp)
                )
            }

            // Section 3: Import Preferences (Strategies)
            OS95Card(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Import Configurations",
                        style = typography.sectionTitle,
                        color = colors.primaryText
                    )

                    // Duplicate strategy
                    Column {
                        Text(
                            text = "Duplicate Strategy:",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DuplicateResolutionStrategy.values().forEach { strategy ->
                                val selected = uiState.duplicateStrategy == strategy
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 44.dp)
                                        .background(if (selected) colors.cardBackground else colors.surface)
                                        .border(
                                            1.dp,
                                            if (selected) colors.accent else colors.border
                                        )
                                        .clickable { viewModel.setDuplicateStrategy(strategy) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = strategy.displayName,
                                        style = typography.caption,
                                        color = if (selected) colors.accent else colors.secondaryText
                                    )
                                }
                            }
                        }
                    }

                    // Missing Relationship mode
                    Column {
                        Text(
                            text = "Missing Academic Hierarchy:",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MissingRelationshipMode.values().forEach { mode ->
                                val selected = uiState.missingRelMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 44.dp)
                                        .background(if (selected) colors.cardBackground else colors.surface)
                                        .border(
                                            1.dp,
                                            if (selected) colors.accent else colors.border
                                        )
                                        .clickable { viewModel.setMissingRelMode(mode) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode.displayName.take(15) + "...",
                                        style = typography.caption,
                                        color = if (selected) colors.accent else colors.secondaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Primary Action: Analyze & Preview
            OS95Button(
                text = if (uiState.isAnalyzing) "Validating..." else "Validate & Preview",
                onClick = { viewModel.runPreview() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isAnalyzing && !uiState.isImporting
            )

            // Section 4: Validation Preview Result
            uiState.previewResult?.let { preview ->
                OS95Card(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Import Preview",
                                style = typography.sectionTitle,
                                color = colors.primaryText
                            )
                            Text(
                                text = "${preview.totalRowsDetected} Rows Detected",
                                style = typography.caption,
                                color = colors.accentCyan
                            )
                        }

                        // Summary Statistics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(colors.surface)
                                    .padding(8.dp)
                            ) {
                                Text(text = "Valid", style = typography.caption, color = colors.mutedText)
                                Text(
                                    text = "${preview.validRows.size}",
                                    style = typography.sectionTitle,
                                    color = colors.accent
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(colors.surface)
                                    .padding(8.dp)
                            ) {
                                Text(text = "Duplicates", style = typography.caption, color = colors.mutedText)
                                Text(
                                    text = "${preview.duplicateCount}",
                                    style = typography.sectionTitle,
                                    color = colors.primaryText
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(colors.surface)
                                    .padding(8.dp)
                            ) {
                                Text(text = "Errors", style = typography.caption, color = colors.mutedText)
                                Text(
                                    text = "${preview.errorCount}",
                                    style = typography.sectionTitle,
                                    color = if (preview.errorCount > 0) colors.error else colors.accent
                                )
                            }
                        }

                        // Entity Creation Projections
                        if (preview.newSubjectsToCreate.isNotEmpty() ||
                            preview.newChaptersToCreate.isNotEmpty() ||
                            preview.newTopicsToCreate.isNotEmpty() ||
                            preview.newQuestionsCount > 0
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Academic Entities to be created:",
                                    style = typography.caption,
                                    color = colors.mutedText
                                )
                                if (preview.newSubjectsToCreate.isNotEmpty()) {
                                    Text(
                                        text = "• Subjects: ${preview.newSubjectsToCreate.size} (${preview.newSubjectsToCreate.joinToString(", ")})",
                                        style = typography.caption,
                                        color = colors.secondaryText
                                    )
                                }
                                if (preview.newChaptersToCreate.isNotEmpty()) {
                                    Text(
                                        text = "• Chapters: ${preview.newChaptersToCreate.size}",
                                        style = typography.caption,
                                        color = colors.secondaryText
                                    )
                                }
                                if (preview.newTopicsToCreate.isNotEmpty()) {
                                    Text(
                                        text = "• Topics: ${preview.newTopicsToCreate.size}",
                                        style = typography.caption,
                                        color = colors.secondaryText
                                    )
                                }
                                if (preview.newQuestionsCount > 0) {
                                    Text(
                                        text = "• Questions: ${preview.newQuestionsCount}",
                                        style = typography.caption,
                                        color = colors.secondaryText
                                    )
                                }
                            }
                        }

                        // Errors List if any
                        if (preview.errors.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Validation Errors to Resolve:",
                                    style = typography.bodySmall,
                                    color = colors.error
                                )
                                preview.errors.take(10).forEach { err ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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

            // Exported CSV Result display if any
            uiState.exportedCsv?.let { exported ->
                OS95Card(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Exported ${uiState.selectedDatasetType.displayName} CSV",
                            style = typography.sectionTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "RFC 4180 compliant export. Ready for backup, spreadsheet editing, or re-import.",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        OS95TextField(
                            value = exported,
                            onValueChange = {},
                            readOnly = true,
                            label = "Export Output",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 220.dp)
                        )
                    }
                }
            }
        }
    }
}
