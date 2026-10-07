package com.os95.app.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.os95.app.core.ui.theme.OS95Theme
import com.os95.app.navigation.OS95Screen

data class OS95DrawerItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val badge: String? = null
)

data class OS95DrawerSection(
    val title: String,
    val items: List<OS95DrawerItem>
)

val OS95DrawerSections = listOf(
    OS95DrawerSection(
        title = "CORE EXAM LOOP",
        items = listOf(
            OS95DrawerItem(OS95Screen.Home.route, "Command Center", Icons.Outlined.Dashboard),
            OS95DrawerItem(OS95Screen.Syllabus.route, "Syllabus Tracker", Icons.AutoMirrored.Outlined.MenuBook),
            OS95DrawerItem(OS95Screen.Recall.route, "Recall & Flashcards", Icons.Outlined.Psychology),
            OS95DrawerItem(OS95Screen.Papers.route, "Practice Papers", Icons.AutoMirrored.Outlined.Assignment),
            OS95DrawerItem(OS95Screen.Mistakes.route, "Mistake Bank", Icons.Outlined.WarningAmber),
            OS95DrawerItem(OS95Screen.Progress.route, "Score & Radar", Icons.AutoMirrored.Outlined.ShowChart)
        )
    ),
    OS95DrawerSection(
        title = "ADVANCED ENGINES",
        items = listOf(
            OS95DrawerItem(OS95Screen.TimeToMarks.route, "Time-to-Marks Intel", Icons.Outlined.Speed),
            OS95DrawerItem(OS95Screen.AdaptiveRetest.route, "Adaptive Re-Test", Icons.Outlined.Refresh),
            OS95DrawerItem(OS95Screen.ExamSimulator.route, "Exam Simulator", Icons.Outlined.History),
            OS95DrawerItem(OS95Screen.Last7Days.route, "Last-7-Days Mode", Icons.Outlined.DateRange)
        )
    ),
    OS95DrawerSection(
        title = "HIGH-YIELD VAULTS",
        items = listOf(
            OS95DrawerItem(OS95Screen.FormulaVault.route, "Formula Vault", Icons.Outlined.Functions),
            OS95DrawerItem(OS95Screen.ExamDayProtocol.route, "T-3H Exam Protocol", Icons.Outlined.Checklist),
            OS95DrawerItem(OS95Screen.Focus.route, "Focus & Acoustics", Icons.Outlined.Timer)
        )
    ),
    OS95DrawerSection(
        title = "SYSTEM & SOVEREIGNTY",
        items = listOf(
            OS95DrawerItem(OS95Screen.UniversalCsv.route, "Data Portability (CSV)", Icons.Outlined.FileDownload),
            OS95DrawerItem(OS95Screen.Settings.route, "Settings & Targets", Icons.Outlined.Settings)
        )
    )
)

/**
 * 95OS Navigation Drawer / Side Bar Content Sheet.
 * Provides instant 1-tap navigation across the entire offline academic engine.
 */
@Composable
fun OS95DrawerContent(
    currentRoute: String?,
    studentName: String,
    targetScorePercentage: Float,
    targetExam: String?,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val shapes = OS95Theme.shapes
    val spacing = OS95Theme.spacing
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.cardBackground)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "95OS",
                                style = typography.heroTitle.copy(fontSize = 24.sp),
                                color = colors.accentCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Offline Invariant Dot Badge
                            Surface(
                                shape = shapes.pill,
                                color = colors.surface,
                                border = BorderStroke(1.dp, colors.success)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(colors.success)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "OFFLINE",
                                        style = typography.caption.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = colors.success
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Offline Exam Operating System",
                            style = typography.caption.copy(fontSize = 11.sp),
                            color = colors.mutedText
                        )
                    }

                    OS95IconButton(
                        icon = Icons.Outlined.Close,
                        contentDescription = "Close Drawer",
                        onClick = onClose
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Student Profile Card
                Surface(
                    shape = shapes.medium,
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = studentName.ifBlank { "Cadet Scholar" },
                                style = typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!targetExam.isNullOrBlank()) {
                                Text(
                                    text = targetExam,
                                    style = typography.caption.copy(fontSize = 11.sp),
                                    color = colors.mutedText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = shapes.pill,
                            color = colors.cardBackground,
                            border = BorderStroke(1.dp, colors.accent)
                        ) {
                            Text(
                                text = "${targetScorePercentage.toInt()}% TARGET",
                                style = typography.caption.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = colors.accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = colors.border,
                thickness = 1.dp
            )

            // Scrollable Categorized Navigation Sections
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(vertical = 12.dp, horizontal = 12.dp)
            ) {
                OS95DrawerSections.forEachIndexed { index, section ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = section.title,
                        style = typography.caption.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = colors.mutedText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    section.items.forEach { item ->
                        val isSelected = currentRoute == item.route

                        Surface(
                            shape = shapes.medium,
                            color = if (isSelected) colors.cardBackground else colors.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) colors.accent else androidx.compose.ui.graphics.Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = spacing.minTouchTarget)
                                .clip(shapes.medium)
                                .clickable { onNavigate(item.route) }
                                .semantics { role = Role.Tab }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) colors.accentCyan else colors.secondaryText,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = item.title,
                                    style = typography.caption.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) colors.primaryText else colors.secondaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                if (item.badge != null) {
                                    Surface(
                                        shape = shapes.pill,
                                        color = colors.cardBackground,
                                        border = BorderStroke(1.dp, colors.border)
                                    ) {
                                        Text(
                                            text = item.badge,
                                            style = typography.caption.copy(fontSize = 10.sp),
                                            color = colors.accentCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }

            HorizontalDivider(
                color = colors.border,
                thickness = 1.dp
            )

            // Footer Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Local SQLite • Zero Telemetry",
                    style = typography.caption.copy(fontSize = 10.sp),
                    color = colors.mutedText
                )
                Text(
                    text = "v2.3.1",
                    style = typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = colors.secondaryText
                )
            }
        }
    }
}
