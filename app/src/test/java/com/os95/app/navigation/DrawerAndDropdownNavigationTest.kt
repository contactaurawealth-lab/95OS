package com.os95.app.navigation

import com.os95.app.core.ui.component.OS95DrawerSections
import com.os95.app.core.ui.component.OS95MenuAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawerAndDropdownNavigationTest {

    @Test
    fun testDrawerSectionsIntegrityAndUniqueRoutes() {
        assertEquals(4, OS95DrawerSections.size)

        val sectionTitles = OS95DrawerSections.map { it.title }
        assertTrue(sectionTitles.contains("CORE EXAM LOOP"))
        assertTrue(sectionTitles.contains("ADVANCED ENGINES"))
        assertTrue(sectionTitles.contains("HIGH-YIELD VAULTS"))
        assertTrue(sectionTitles.contains("SYSTEM & SOVEREIGNTY"))

        val allDrawerItems = OS95DrawerSections.flatMap { it.items }
        val routes = allDrawerItems.map { it.route }

        // All routes must be distinct
        assertEquals(routes.size, routes.toSet().size)

        // Verify Core Exam Loop destinations
        val coreRoutes = OS95DrawerSections.first { it.title == "CORE EXAM LOOP" }.items.map { it.route }
        assertEquals(6, coreRoutes.size)
        assertTrue(coreRoutes.contains(OS95Screen.Home.route))
        assertTrue(coreRoutes.contains(OS95Screen.Syllabus.route))
        assertTrue(coreRoutes.contains(OS95Screen.Recall.route))
        assertTrue(coreRoutes.contains(OS95Screen.Papers.route))
        assertTrue(coreRoutes.contains(OS95Screen.Mistakes.route))
        assertTrue(coreRoutes.contains(OS95Screen.Progress.route))

        // Verify Advanced Engines destinations
        val advancedRoutes = OS95DrawerSections.first { it.title == "ADVANCED ENGINES" }.items.map { it.route }
        assertEquals(4, advancedRoutes.size)
        assertTrue(advancedRoutes.contains(OS95Screen.TimeToMarks.route))
        assertTrue(advancedRoutes.contains(OS95Screen.AdaptiveRetest.route))
        assertTrue(advancedRoutes.contains(OS95Screen.ExamSimulator.route))
        assertTrue(advancedRoutes.contains(OS95Screen.Last7Days.route))

        // Verify High-Yield Vaults destinations
        val vaultRoutes = OS95DrawerSections.first { it.title == "HIGH-YIELD VAULTS" }.items.map { it.route }
        assertEquals(3, vaultRoutes.size)
        assertTrue(vaultRoutes.contains(OS95Screen.FormulaVault.route))
        assertTrue(vaultRoutes.contains(OS95Screen.ExamDayProtocol.route))
        assertTrue(vaultRoutes.contains(OS95Screen.Focus.route))

        // Verify System & Sovereignty destinations
        val systemRoutes = OS95DrawerSections.first { it.title == "SYSTEM & SOVEREIGNTY" }.items.map { it.route }
        assertEquals(2, systemRoutes.size)
        assertTrue(systemRoutes.contains(OS95Screen.UniversalCsv.route))
        assertTrue(systemRoutes.contains(OS95Screen.Settings.route))
    }

    @Test
    fun testDrawerItemsHaveNonBlankTitles() {
        val allItems = OS95DrawerSections.flatMap { it.items }
        allItems.forEach { item ->
            assertTrue("Title should not be blank for route ${item.route}", item.title.isNotBlank())
            assertTrue("Route should not be blank", item.route.isNotBlank())
        }
    }

    @Test
    fun testOS95MenuActionModel() {
        var clicked = false
        val action = OS95MenuAction(
            label = "Test Action",
            onClick = { clicked = true }
        )

        assertEquals("Test Action", action.label)
        assertNull(action.icon)
        assertFalse(action.isSelected)
        assertFalse(action.isDestructive)
        assertTrue(action.enabled)

        action.onClick()
        assertTrue("Action callback should execute", clicked)

        val destructiveAction = OS95MenuAction(
            label = "Reset",
            isDestructive = true,
            isSelected = true,
            enabled = false,
            onClick = {}
        )
        assertTrue(destructiveAction.isDestructive)
        assertTrue(destructiveAction.isSelected)
        assertFalse(destructiveAction.enabled)
    }
}
