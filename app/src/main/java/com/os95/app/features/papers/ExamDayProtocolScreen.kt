package com.os95.app.features.papers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.theme.OS95Theme
import kotlinx.coroutines.delay

enum class ExamDayTab(val label: String) {
    CHECKLIST("Logistics Checklist"),
    TIMELINE("T-Minus Pacing"),
    PRIMING("Cognitive Priming")
}

data class ChecklistItem(val id: String, val title: String, val detail: String)

val DEFAULT_EXAM_CHECKLIST = listOf(
    ChecklistItem("admit_card", "Printed Admit Card & Photo ID", "Physical printout + institutional identity card"),
    ChecklistItem("pens", "2+ Tested Black/Blue Ballpoint Pens", "Pre-tested, smooth flow, non-gel if OMR sheet"),
    ChecklistItem("pencils_geom", "Pencils, Eraser & Geometry Kit", "Sharpened 2B pencils, ruler, compass, protractor"),
    ChecklistItem("pouch", "Transparent Exam Stationery Pouch", "Meets strict exam hall invigilation requirements"),
    ChecklistItem("watch", "Analog Wristwatch (Non-Smart)", "Digital & smart watches are prohibited in examination halls"),
    ChecklistItem("water", "Transparent Water Bottle", "Hydration prevents cognitive fatigue and brain fog"),
    ChecklistItem("board", "Plain Examination Clipboard", "Clean, non-transparent surfaces only without markings")
)

@Composable
fun ExamDayProtocolScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val checkedItems = rememberSaveable(
        saver = Saver(
            save = { it.toMap() },
            restore = { saved ->
                mutableStateMapOf<String, Boolean>().apply {
                    putAll(saved)
                }
            }
        )
    ) { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = spacing.l, vertical = spacing.m)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    text = "T-Minus 3H Protocol",
                    style = typography.screenTitle,
                    color = colors.primaryText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Zero-panic readiness & mental priming",
                    style = typography.bodySmall,
                    color = colors.mutedText
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.m))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = colors.surface,
            contentColor = colors.accent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = colors.accent,
                    height = 2.dp
                )
            }
        ) {
            ExamDayTab.values().forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.defaultMinSize(minHeight = spacing.minTouchTarget),
                    text = {
                        Text(
                            text = tab.label,
                            style = typography.bodySmall,
                            color = if (selectedTab == index) colors.primaryText else colors.mutedText,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.m))

        // Tab Content
        when (ExamDayTab.values()[selectedTab]) {
            ExamDayTab.CHECKLIST -> ChecklistTabContent(
                checkedItems = checkedItems,
                onToggleItem = { id -> checkedItems[id] = !(checkedItems[id] ?: false) },
                onResetAll = { checkedItems.clear() }
            )
            ExamDayTab.TIMELINE -> TimelineTabContent()
            ExamDayTab.PRIMING -> CognitivePrimingTabContent()
        }
    }
}

@Composable
private fun ChecklistTabContent(
    checkedItems: Map<String, Boolean>,
    onToggleItem: (String) -> Unit,
    onResetAll: () -> Unit
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    val totalCount = DEFAULT_EXAM_CHECKLIST.size
    val checkedCount = DEFAULT_EXAM_CHECKLIST.count { checkedItems[it.id] == true }
    val isAllReady = checkedCount == totalCount

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            OS95Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAllReady) "All Gear Verified" else "Gear Readiness: $checkedCount / $totalCount",
                            style = typography.sectionTitle,
                            color = if (isAllReady) colors.accent else colors.primaryText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isAllReady) "You have all required tools. Maintain calm." else "Confirm each item physically before departure.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )
                    }

                    if (checkedCount > 0) {
                        OS95OutlinedButton(
                            text = "Reset",
                            onClick = onResetAll,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        items(DEFAULT_EXAM_CHECKLIST.size) { index ->
            val item = DEFAULT_EXAM_CHECKLIST[index]
            val isChecked = checkedItems[item.id] == true

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = spacing.minTouchTarget)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleItem(item.id) },
                shape = RoundedCornerShape(8.dp),
                color = if (isChecked) colors.surface.copy(alpha = 0.6f) else colors.surface,
                border = BorderStroke(1.dp, if (isChecked) colors.accent.copy(alpha = 0.4f) else colors.border)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isChecked) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = if (isChecked) "Checked" else "Unchecked",
                        tint = if (isChecked) colors.accent else colors.mutedText,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = typography.body,
                            color = if (isChecked) colors.mutedText else colors.primaryText,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = item.detail,
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )
                    }
                }
            }
        }
    }
}

data class TimelineStep(
    val timeLabel: String,
    val title: String,
    val instructions: List<String>,
    val caution: String
)

val TIMELINE_STEPS = listOf(
    TimelineStep(
        timeLabel = "T - 3:00 Hours",
        title = "Gear Seal & Light Nutrition",
        instructions = listOf(
            "Pack all stationery into the transparent pouch.",
            "Double-check physical admit card, ID card, and analog watch.",
            "Consume light, low-glycemic food (nuts, fruit, light toast). Avoid sugar spikes and heavy carbs that cause postprandial fatigue."
        ),
        caution = "No new notes or last-minute heavy topic cramming."
    ),
    TimelineStep(
        timeLabel = "T - 2:00 Hours",
        title = "Departure & Transport Buffer",
        instructions = listOf(
            "Depart with a minimum 40-minute traffic/delay margin.",
            "Verify the exam centre code and route beforehand.",
            "Wear comfortable clothing suitable for exam temperature."
        ),
        caution = "Avoid discussing expected questions or difficult topics during commute."
    ),
    TimelineStep(
        timeLabel = "T - 1:00 Hour",
        title = "Centre Arrival & Roll Check",
        instructions = listOf(
            "Locate your examination room, hall number, and desk allotment on notice boards.",
            "Visit the washroom now — avoid taking breaks during the first 60 minutes.",
            "Find a quiet, sheltered bench away from noisy study groups."
        ),
        caution = "Do NOT join peer conversations debating syllabus predictions or fear."
    ),
    TimelineStep(
        timeLabel = "T - 0:30 Hour",
        title = "Hall Entry & Materials Lockdown",
        instructions = listOf(
            "Deposit all mobile phones, bags, and rough sheets in the designated locker.",
            "Enter examination hall with only approved tools and transparent water bottle.",
            "Sit at your assigned desk and verify your roll number sticker on the desk."
        ),
        caution = "Inspect your desk surface and remove any stray papers to avoid invigilation disputes."
    ),
    TimelineStep(
        timeLabel = "T - 0:15 Hour",
        title = "Deskside Calming & OMR/Sheet Filling",
        instructions = listOf(
            "Carefully fill student roll number, subject code, and set code on the answer booklet.",
            "Perform 3 cycles of Box Breathing (4-4-4-4) to steady heart rate.",
            "Remind yourself: You have revised methodically. Read questions carefully before writing."
        ),
        caution = "Triple check roll number bubbles on OMR sheets."
    )
)

@Composable
private fun TimelineTabContent() {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(TIMELINE_STEPS.size) { index ->
            val step = TIMELINE_STEPS[index]

            OS95Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = colors.accent.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = step.timeLabel,
                                style = typography.caption,
                                color = colors.accent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = colors.mutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.title,
                        style = typography.sectionTitle,
                        color = colors.primaryText,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    step.instructions.forEach { instr ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(text = "• ", color = colors.accent, fontWeight = FontWeight.Bold)
                            Text(text = instr, style = typography.body, color = colors.secondaryText)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        color = colors.background,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = colors.mutedText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step.caution,
                                style = typography.bodySmall,
                                color = colors.mutedText
                            )
                        }
                    }
                }
            }
        }
    }
}

val PRIMING_CARDS = listOf(
    Pair("1. Box Breathing (4-4-4-4)", "Deep 4s inhale, 4s hold, 4s exhale, 4s hold. Lowers sympathetic fight-or-flight response, reducing cortisol and mental blankness."),
    Pair("2. The First-Pass Paper Scan (10 Min)", "Read the whole paper without picking up the pen. Identify the 5 easiest questions. Securing the first 20 marks dissolves exam anxiety completely."),
    Pair("3. Strict Time-to-Mark Budgeting", "Calculate 1.5–1.8 minutes per mark. If a 3-mark question takes longer than 5 minutes, leave space, star the margin, and keep moving."),
    Pair("4. Protocol When Blocked", "If an answer escapes you, write given values, definitions, and relevant formulas. Partial credit awards up to 50% marks even without full derivations."),
    Pair("5. The Final 15-Minute Audit", "Put pens down on new derivations at T-15m. Check question numbers, ensure no sub-parts were skipped, and verify SI units and final answer boxes.")
)

@Composable
private fun CognitivePrimingTabContent() {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    var isBreathingActive by remember { mutableStateOf(false) }
    var breathPhase by remember { mutableStateOf("Ready") }
    var secondsInPhase by remember { mutableIntStateOf(0) }
    val breathScale = remember { Animatable(1f) }

    LaunchedEffect(isBreathingActive) {
        if (!isBreathingActive) {
            breathPhase = "Ready"
            secondsInPhase = 0
            breathScale.snapTo(1f)
            return@LaunchedEffect
        }

        while (isBreathingActive) {
            // Phase 1: Inhale 4s
            breathPhase = "Inhale slowly"
            val inhaleJob = launch {
                breathScale.animateTo(1.25f, tween(4000, easing = LinearEasing))
            }
            for (i in 1..4) {
                secondsInPhase = i
                delay(1000)
            }
            inhaleJob.join()

            // Phase 2: Hold 4s
            breathPhase = "Hold breath"
            for (i in 1..4) {
                secondsInPhase = i
                delay(1000)
            }

            // Phase 3: Exhale 4s
            breathPhase = "Exhale slowly"
            val exhaleJob = launch {
                breathScale.animateTo(1.0f, tween(4000, easing = LinearEasing))
            }
            for (i in 1..4) {
                secondsInPhase = i
                delay(1000)
            }
            exhaleJob.join()

            // Phase 4: Hold 4s
            breathPhase = "Hold empty"
            for (i in 1..4) {
                secondsInPhase = i
                delay(1000)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Breathing Tool Card
        item {
            OS95Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Parasympathetic Nervous Reset",
                        style = typography.sectionTitle,
                        color = colors.primaryText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "4-4-4-4 Box Breathing to normalize heart rate before paper distribution",
                        style = typography.bodySmall,
                        color = colors.secondaryText
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Breathing visual circle
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(breathScale.value),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = colors.accent.copy(alpha = 0.15f),
                            border = BorderStroke(2.dp, colors.accent),
                            modifier = Modifier.fillMaxSize()
                        ) {}

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = breathPhase,
                                style = typography.bodySmall,
                                color = colors.primaryText,
                                fontWeight = FontWeight.Bold
                            )
                            if (isBreathingActive) {
                                Text(
                                    text = "$secondsInPhase s",
                                    style = typography.statNumber.copy(fontSize = 20.sp),
                                    color = colors.accent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OS95Button(
                        text = if (isBreathingActive) "Stop Breathing" else "Start 4-4-4-4 Reset",
                        onClick = { isBreathingActive = !isBreathingActive },
                        icon = if (isBreathingActive) Icons.Outlined.Stop else Icons.Outlined.PlayArrow
                    )
                }
            }
        }

        // 5 Anchors
        items(PRIMING_CARDS.size) { index ->
            val (title, text) = PRIMING_CARDS[index]
            OS95Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        style = typography.sectionTitle,
                        color = colors.accent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = text,
                        style = typography.body,
                        color = colors.secondaryText
                    )
                }
            }
        }
    }
}
