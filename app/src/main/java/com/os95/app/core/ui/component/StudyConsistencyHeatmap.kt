package com.os95.app.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.os95.app.core.ui.theme.OS95Theme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * A calm, serious monochrome academic study consistency heatmap.
 * Visualizes 10 weeks (70 days) of daily study dedication without gamified distractions.
 */
@Composable
fun StudyConsistencyHeatmap(
    dailyStudyMinutes: Map<Long, Int>, // Key: Epoch Day (timestamp / 86400000), Value: Minutes
    modifier: Modifier = Modifier
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val now = System.currentTimeMillis()
    val todayEpochDay = TimeUnit.MILLISECONDS.toDays(now)

    var selectedDayInfo by remember { mutableStateOf<String?>(null) }

    // 10 weeks = 70 days. Columns: 10, Rows: 7 (Mon..Sun)
    val totalWeeks = 10
    val startEpochDay = todayEpochDay - (totalWeeks * 7 - 1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.cardBackground)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STUDY CONSISTENCY (LAST 70 DAYS)",
                style = typography.caption.copy(letterSpacing = 1.sp),
                color = colors.accent
            )
            val totalMinutes = dailyStudyMinutes.values.sum()
            val totalHours = totalMinutes / 60
            Text(
                text = "${totalHours}h logged",
                style = typography.caption,
                color = colors.mutedText
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Heatmap Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // Day Labels: M, W, F
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                listOf("M", "", "W", "", "F", "", "S").forEach { label ->
                    Box(
                        modifier = Modifier.size(15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = typography.caption.copy(fontSize = 9.sp),
                            color = colors.mutedText
                        )
                    }
                }
            }

            // Columns (Weeks)
            for (w in 0 until totalWeeks) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    for (d in 0 until 7) {
                        val cellEpochDay = startEpochDay + (w * 7) + d
                        val mins = dailyStudyMinutes[cellEpochDay] ?: 0
                        val isFuture = cellEpochDay > todayEpochDay

                        val cellColor = when {
                            isFuture -> Color.Transparent
                            mins == 0 -> colors.surface
                            mins <= 30 -> colors.accent.copy(alpha = 0.35f)
                            mins <= 60 -> colors.accent.copy(alpha = 0.70f)
                            else -> colors.accent
                        }

                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(cellColor)
                                .clickable(enabled = !isFuture) {
                                    val date = Date(TimeUnit.DAYS.toMillis(cellEpochDay))
                                    val dateStr = SimpleDateFormat("EEE, MMM d", Locale.US).format(date)
                                    selectedDayInfo = if (mins > 0) "$dateStr: $mins minutes" else "$dateStr: No sessions logged"
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Day / Legend Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedDayInfo ?: "Tap any square to inspect daily focus",
                style = typography.caption,
                color = if (selectedDayInfo != null) colors.primaryText else colors.mutedText
            )

            // Minimal Legend
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "0m", style = typography.caption.copy(fontSize = 9.sp), color = colors.mutedText)
                Box(modifier = Modifier.size(9.dp).clip(RoundedCornerShape(2.dp)).background(colors.surface))
                Box(modifier = Modifier.size(9.dp).clip(RoundedCornerShape(2.dp)).background(colors.accent.copy(alpha = 0.35f)))
                Box(modifier = Modifier.size(9.dp).clip(RoundedCornerShape(2.dp)).background(colors.accent.copy(alpha = 0.70f)))
                Box(modifier = Modifier.size(9.dp).clip(RoundedCornerShape(2.dp)).background(colors.accent))
                Text(text = "60m+", style = typography.caption.copy(fontSize = 9.sp), color = colors.mutedText)
            }
        }
    }
}
