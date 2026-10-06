package com.os95.app.features.formulas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.os95.app.core.database.entity.FormulaEntity
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95EmptyState
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.theme.OS95Theme

@Composable
fun FormulaVaultScreen(
    viewModel: FormulaVaultViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = spacing.l, vertical = spacing.m)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.defaultMinSize(minWidth = spacing.minTouchTarget, minHeight = spacing.minTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.primaryText
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Formula Vault",
                        style = typography.screenTitle,
                        color = colors.primaryText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Key equations, laws & definitions",
                        style = typography.bodySmall,
                        color = colors.mutedText
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Bookmark filter toggle button
                IconButton(
                    onClick = { viewModel.toggleFilterBookmarked() },
                    modifier = Modifier.defaultMinSize(minWidth = spacing.minTouchTarget, minHeight = spacing.minTouchTarget)
                ) {
                    Icon(
                        imageVector = if (state.onlyBookmarked) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (state.onlyBookmarked) "Show all" else "Show bookmarked only",
                        tint = if (state.onlyBookmarked) colors.accent else colors.mutedText
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                OS95Button(
                    text = "Add",
                    onClick = { showAddDialog = true },
                    icon = Icons.Outlined.Add
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.m))

        // Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search formulas, laws, or variables...", style = typography.bodySmall, color = colors.mutedText) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = colors.mutedText
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = spacing.minTouchTarget),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.border,
                focusedTextColor = colors.primaryText,
                unfocusedTextColor = colors.primaryText
            )
        )

        Spacer(modifier = Modifier.height(spacing.m))

        // Subject Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubjectFilterChip(
                label = "All Subjects",
                isSelected = state.selectedSubjectId == null,
                onClick = { viewModel.selectSubject(null) }
            )
            state.subjects.forEach { subject ->
                SubjectFilterChip(
                    label = subject.name,
                    isSelected = state.selectedSubjectId == subject.id,
                    onClick = { viewModel.selectSubject(subject.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.m))

        // Content
        if (state.filteredFormulas.isEmpty()) {
            if (state.formulas.isEmpty()) {
                OS95EmptyState(
                    title = "No Formulas Recorded",
                    description = "Capture high-frequency formulas, theorems, and definitions for rapid review before your exam.",
                    primaryActionLabel = "Add First Formula",
                    onPrimaryAction = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                OS95EmptyState(
                    title = "No Matches Found",
                    description = "Try searching for a different keyword or resetting your subject filter.",
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(state.filteredFormulas, key = { it.id }) { formula ->
                    val subjectName = state.subjects.find { it.id == formula.subjectId }?.name ?: "Subject"
                    FormulaItemCard(
                        formula = formula,
                        subjectName = subjectName,
                        onToggleBookmark = { viewModel.toggleBookmark(formula.id, formula.isBookmarked) },
                        onDelete = { viewModel.deleteFormula(formula) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddFormulaDialog(
            viewModel = viewModel,
            subjects = state.subjects,
            onDismiss = { showAddDialog = false },
            onAdd = { subjectId, chapterId, title, expr, expl, relevance ->
                viewModel.addFormula(subjectId, chapterId, title, expr, expl, relevance)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SubjectFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    Surface(
        modifier = Modifier
            .defaultMinSize(minHeight = spacing.minTouchTarget)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) colors.accent else colors.surface,
        border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.border)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = typography.bodySmall,
                color = if (isSelected) colors.surface else colors.secondaryText,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun FormulaItemCard(
    formula: FormulaEntity,
    subjectName: String,
    onToggleBookmark: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    var showDeleteConfirm by remember { mutableStateOf(false) }

    OS95Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Subject and Relevance Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subjectName,
                    style = typography.caption,
                    color = colors.mutedText
                )

                // Exam Relevance Pill
                val relevanceColor = when (formula.examRelevance.uppercase()) {
                    "HIGH" -> colors.accent
                    "MEDIUM" -> colors.accentCyan
                    else -> colors.mutedText
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = relevanceColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, relevanceColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${formula.examRelevance} YIELD",
                        style = typography.caption.copy(fontSize = 10.sp),
                        color = relevanceColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = formula.title,
                style = typography.sectionTitle,
                color = colors.primaryText,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Formula Expression Box (Monospace paper box)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = colors.background,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = formula.expression,
                        fontFamily = FontFamily.Monospace,
                        style = typography.body,
                        color = colors.primaryText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Explanation / Notes (if non-empty)
            if (formula.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = formula.explanation,
                    style = typography.bodySmall,
                    color = colors.secondaryText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Actions Row: Bookmark & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(spacing.minTouchTarget)
                ) {
                    Icon(
                        imageVector = if (formula.isBookmarked) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Bookmark",
                        tint = if (formula.isBookmarked) colors.accent else colors.mutedText
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(spacing.minTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete",
                        tint = colors.mutedText
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Formula", style = typography.sectionTitle, color = colors.primaryText) },
            text = { Text("Are you sure you want to remove '${formula.title}' from your vault?", style = typography.bodySmall, color = colors.secondaryText) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = colors.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = colors.secondaryText)
                }
            },
            containerColor = colors.surface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFormulaDialog(
    viewModel: FormulaVaultViewModel,
    subjects: List<com.os95.app.core.database.entity.SubjectEntity>,
    onDismiss: () -> Unit,
    onAdd: (subjectId: String, chapterId: String, title: String, expression: String, explanation: String, relevance: String) -> Unit
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()) }
    val chaptersFlow = remember(selectedSubject?.id) {
        viewModel.getChaptersForSubject(selectedSubject?.id ?: "")
    }
    val chapters by chaptersFlow.collectAsState(initial = emptyList())
    var selectedChapterId by remember(chapters) { mutableStateOf(chapters.firstOrNull()?.id ?: "") }

    var title by remember { mutableStateOf("") }
    var expression by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    var relevance by remember { mutableStateOf("HIGH") }

    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var chapterMenuExpanded by remember { mutableStateOf(false) }
    var relevanceMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Formula to Vault", style = typography.sectionTitle, color = colors.primaryText) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Subject Dropdown
                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = { subjectMenuExpanded = !subjectMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSubject?.name ?: "Select Subject",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject", style = typography.caption) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.primaryText,
                            unfocusedTextColor = colors.primaryText
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        subjects.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.name) },
                                onClick = {
                                    selectedSubject = s
                                    subjectMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Chapter Dropdown
                if (chapters.isNotEmpty()) {
                    val currentChapterName = chapters.find { it.id == selectedChapterId }?.name ?: "Select Chapter"
                    ExposedDropdownMenuBox(
                        expanded = chapterMenuExpanded,
                        onExpandedChange = { chapterMenuExpanded = !chapterMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentChapterName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Chapter", style = typography.caption) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.primaryText,
                                unfocusedTextColor = colors.primaryText
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = chapterMenuExpanded,
                            onDismissRequest = { chapterMenuExpanded = false }
                        ) {
                            chapters.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        selectedChapterId = c.id
                                        chapterMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "This subject has no chapters. Please create a chapter under this subject in Syllabus first before adding formulas.",
                        style = typography.caption,
                        color = colors.warning,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Formula / Law Name") },
                    placeholder = { Text("e.g. Quadratic Formula, Ohm's Law") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Expression
                OutlinedTextField(
                    value = expression,
                    onValueChange = { expression = it },
                    label = { Text("Formula Expression") },
                    placeholder = { Text("e.g. x = (-b ± √(b² - 4ac)) / (2a)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Explanation
                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Conditions / Key Notes (Optional)") },
                    placeholder = { Text("e.g. Valid when a ≠ 0 and discriminant ≥ 0") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Exam Relevance
                ExposedDropdownMenuBox(
                    expanded = relevanceMenuExpanded,
                    onExpandedChange = { relevanceMenuExpanded = !relevanceMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = "$relevance YIELD",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Exam Weightage") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relevanceMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = relevanceMenuExpanded,
                        onDismissRequest = { relevanceMenuExpanded = false }
                    ) {
                        listOf("HIGH", "MEDIUM", "LOW").forEach { r ->
                            DropdownMenuItem(
                                text = { Text("$r YIELD") },
                                onClick = {
                                    relevance = r
                                    relevanceMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val sId = selectedSubject?.id ?: ""
            val cId = selectedChapterId.ifBlank { chapters.firstOrNull()?.id ?: "" }
            val canSave = sId.isNotBlank() && chapters.isNotEmpty() && cId.isNotBlank() && title.isNotBlank() && expression.isNotBlank()

            OS95Button(
                text = "Save Formula",
                onClick = {
                    if (canSave) {
                        onAdd(sId, cId, title, expression, explanation, relevance)
                    }
                },
                enabled = canSave
            )
        },
        dismissButton = {
            OS95OutlinedButton(
                text = "Cancel",
                onClick = onDismiss
            )
        },
        containerColor = colors.surface
    )
}
