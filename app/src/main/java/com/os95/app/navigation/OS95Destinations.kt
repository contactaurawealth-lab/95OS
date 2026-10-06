package com.os95.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.ui.graphics.vector.ImageVector
import com.os95.app.core.ui.component.OS95NavItem

sealed class OS95Screen(val route: String) {
    // Root Destinations
    object Home : OS95Screen("home")
    object Syllabus : OS95Screen("syllabus")
    object Recall : OS95Screen("recall")
    object Papers : OS95Screen("papers")
    object Mistakes : OS95Screen("mistakes")
    object Progress : OS95Screen("progress")

    // Secondary Destinations
    object SubjectDetail : OS95Screen("subject/{subjectId}") {
        fun createRoute(subjectId: String) = "subject/$subjectId"
    }

    object ChapterDetail : OS95Screen("chapter/{chapterId}?subjectId={subjectId}") {
        fun createRoute(chapterId: String, subjectId: String? = null): String {
            return if (subjectId != null) "chapter/$chapterId?subjectId=$subjectId" else "chapter/$chapterId"
        }
    }

    object PaperDetail : OS95Screen("paper/{paperId}") {
        fun createRoute(paperId: String) = "paper/$paperId"
    }

    object RecallSession : OS95Screen("recall/session")

    // Focus & Utility Destinations
    object Focus : OS95Screen("focus")
    object Settings : OS95Screen("settings")
    object UniversalCsv : OS95Screen("csv")

    // Onboarding
    object Onboarding : OS95Screen("onboarding")
}

val OS95RootNavigationItems = listOf(
    OS95NavItem(OS95Screen.Home.route, "Home", Icons.Outlined.Dashboard),
    OS95NavItem(OS95Screen.Syllabus.route, "Syllabus", Icons.AutoMirrored.Outlined.MenuBook),
    OS95NavItem(OS95Screen.Recall.route, "Recall", Icons.Outlined.Psychology),
    OS95NavItem(OS95Screen.Papers.route, "Papers", Icons.AutoMirrored.Outlined.Assignment),
    OS95NavItem(OS95Screen.Mistakes.route, "Mistakes", Icons.Outlined.WarningAmber),
    OS95NavItem(OS95Screen.Progress.route, "Progress", Icons.AutoMirrored.Outlined.ShowChart)
)
