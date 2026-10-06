package com.os95.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.os95.app.core.di.OS95AppContainer
import com.os95.app.core.ui.component.OS95BottomBar
import com.os95.app.core.ui.component.OS95IconButton
import com.os95.app.core.ui.theme.OS95Theme
import com.os95.app.features.focus.FocusScreen
import com.os95.app.features.focus.FocusViewModel
import com.os95.app.features.home.HomeScreen
import com.os95.app.features.home.HomeViewModel
import com.os95.app.features.mistakes.MistakesScreen
import com.os95.app.features.mistakes.MistakesViewModel
import com.os95.app.features.onboarding.OnboardingScreen
import com.os95.app.features.onboarding.OnboardingViewModel
import com.os95.app.features.papers.PapersScreen
import com.os95.app.features.papers.PapersViewModel
import com.os95.app.features.progress.ProgressScreen
import com.os95.app.features.progress.ProgressViewModel
import com.os95.app.features.recall.RecallScreen
import com.os95.app.features.recall.RecallViewModel
import com.os95.app.features.settings.SettingsScreen
import com.os95.app.features.settings.SettingsViewModel
import com.os95.app.features.syllabus.ChapterDetailScreen
import com.os95.app.features.syllabus.SubjectDetailScreen
import com.os95.app.features.syllabus.SyllabusScreen
import com.os95.app.features.syllabus.SyllabusViewModel

@Composable
fun OS95App(
    container: OS95AppContainer,
    isOnboardingCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val colors = OS95Theme.colors
    val typography = OS95Theme.typography

    val startDestination = if (isOnboardingCompleted) {
        OS95Screen.Home.route
    } else {
        OS95Screen.Onboarding.route
    }

    val isRootRoute = OS95RootNavigationItems.any { it.route == currentRoute }
    val isOnboardingRoute = currentRoute == OS95Screen.Onboarding.route
    val showNavigationShell = !isOnboardingRoute

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        if (isExpandedScreen && showNavigationShell) {
            // Tablet / Landscape layout with Navigation Rail
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
            ) {
                NavigationRail(
                    containerColor = colors.surface,
                    contentColor = colors.primaryText,
                    modifier = Modifier.border(1.dp, colors.border),
                    header = {
                        Text(
                            text = "95OS",
                            style = typography.sectionTitle,
                            color = colors.accentCyan,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                ) {
                    OS95RootNavigationItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(OS95Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text(text = item.title, style = typography.caption) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = colors.accent,
                                selectedTextColor = colors.accent,
                                indicatorColor = colors.cardBackground,
                                unselectedIconColor = colors.secondaryText,
                                unselectedTextColor = colors.secondaryText
                            )
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    OS95IconButton(
                        icon = Icons.Outlined.Timer,
                        contentDescription = "Focus Timer",
                        onClick = { navController.navigate(OS95Screen.Focus.route) }
                    )
                    OS95IconButton(
                        icon = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        onClick = { navController.navigate(OS95Screen.Settings.route) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Box(modifier = Modifier.weight(1f)) {
                    OS95NavGraph(
                        navController = navController,
                        container = container,
                        startDestination = startDestination
                    )
                }
            }
        } else {
            // Mobile layout with Bottom Bar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = colors.background,
                bottomBar = {
                    if (showNavigationShell && isRootRoute) {
                        OS95BottomBar(
                            items = OS95RootNavigationItems,
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(OS95Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    OS95NavGraph(
                        navController = navController,
                        container = container,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}

@Composable
fun OS95NavGraph(
    navController: NavHostController,
    container: OS95AppContainer,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Onboarding
        composable(OS95Screen.Onboarding.route) {
            val vm = viewModel {
                OnboardingViewModel(
                    preferencesManager = container.preferencesManager,
                    studentRepository = container.studentRepository,
                    syllabusRepository = container.syllabusRepository
                )
            }
            OnboardingScreen(
                viewModel = vm,
                onComplete = {
                    navController.navigate(OS95Screen.Home.route) {
                        popUpTo(OS95Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Root 1: Home
        composable(OS95Screen.Home.route) {
            val vm = viewModel {
                HomeViewModel(
                    studentRepository = container.studentRepository,
                    syllabusRepository = container.syllabusRepository,
                    recallRepository = container.recallRepository,
                    paperRepository = container.paperRepository
                )
            }
            HomeScreen(
                viewModel = vm,
                onNavigateToSyllabus = { navController.navigate(OS95Screen.Syllabus.route) },
                onNavigateToRecall = { navController.navigate(OS95Screen.Recall.route) },
                onNavigateToPapers = { navController.navigate(OS95Screen.Papers.route) },
                onNavigateToMistakes = { navController.navigate(OS95Screen.Mistakes.route) },
                onNavigateToFocus = { navController.navigate(OS95Screen.Focus.route) },
                onNavigateToSettings = { navController.navigate(OS95Screen.Settings.route) }
            )
        }

        // Root 2: Syllabus
        composable(OS95Screen.Syllabus.route) {
            val vm = viewModel { SyllabusViewModel(container.syllabusRepository) }
            SyllabusScreen(
                viewModel = vm,
                onOpenSubject = { subjectId ->
                    navController.navigate(OS95Screen.SubjectDetail.createRoute(subjectId))
                }
            )
        }

        // Root 3: Recall
        composable(OS95Screen.Recall.route) {
            val vm = viewModel { RecallViewModel(container.recallRepository) }
            RecallScreen(viewModel = vm)
        }

        // Root 4: Papers
        composable(OS95Screen.Papers.route) {
            val vm = viewModel {
                PapersViewModel(
                    paperRepository = container.paperRepository,
                    syllabusRepository = container.syllabusRepository
                )
            }
            PapersScreen(viewModel = vm)
        }

        // Root 5: Mistakes
        composable(OS95Screen.Mistakes.route) {
            val vm = viewModel {
                MistakesViewModel(
                    mistakeRepository = container.mistakeRepository,
                    syllabusRepository = container.syllabusRepository
                )
            }
            MistakesScreen(viewModel = vm)
        }

        // Root 6: Progress
        composable(OS95Screen.Progress.route) {
            val vm = viewModel {
                ProgressViewModel(
                    studentRepository = container.studentRepository,
                    syllabusRepository = container.syllabusRepository,
                    paperRepository = container.paperRepository,
                    mistakeRepository = container.mistakeRepository
                )
            }
            ProgressScreen(viewModel = vm)
        }

        // Secondary: Subject Detail
        composable(
            route = OS95Screen.SubjectDetail.route,
            arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            val vm = viewModel { SyllabusViewModel(container.syllabusRepository) }
            SubjectDetailScreen(
                subjectId = subjectId,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenChapter = { chapterId ->
                    navController.navigate(OS95Screen.ChapterDetail.createRoute(chapterId, subjectId))
                }
            )
        }

        // Secondary: Chapter Detail
        composable(
            route = OS95Screen.ChapterDetail.route,
            arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            val vm = viewModel { SyllabusViewModel(container.syllabusRepository) }
            ChapterDetailScreen(
                chapterId = chapterId,
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        // Modal / Utility: Focus Timer
        composable(OS95Screen.Focus.route) {
            val vm = viewModel {
                FocusViewModel(
                    studentRepository = container.studentRepository,
                    syllabusRepository = container.syllabusRepository
                )
            }
            FocusScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Modal / Utility: Settings
        composable(OS95Screen.Settings.route) {
            val vm = viewModel {
                SettingsViewModel(
                    preferencesManager = container.preferencesManager,
                    studentRepository = container.studentRepository
                )
            }
            SettingsScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
