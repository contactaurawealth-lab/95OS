package com.os95.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationDestinationTest {

    @Test
    fun testRootNavigationItemsCountAndRoutes() {
        assertEquals(6, OS95RootNavigationItems.size)
        val routes = OS95RootNavigationItems.map { it.route }
        assertTrue(routes.contains(OS95Screen.Home.route))
        assertTrue(routes.contains(OS95Screen.Syllabus.route))
        assertTrue(routes.contains(OS95Screen.Recall.route))
        assertTrue(routes.contains(OS95Screen.Papers.route))
        assertTrue(routes.contains(OS95Screen.Mistakes.route))
        assertTrue(routes.contains(OS95Screen.Progress.route))
    }

    @Test
    fun testParameterizedRouteCreation() {
        assertEquals("subject/math101", OS95Screen.SubjectDetail.createRoute("math101"))
        assertEquals("chapter/chap01?subjectId=sub01", OS95Screen.ChapterDetail.createRoute("chap01", "sub01"))
        assertEquals("paper/paper99", OS95Screen.PaperDetail.createRoute("paper99"))
    }
}
