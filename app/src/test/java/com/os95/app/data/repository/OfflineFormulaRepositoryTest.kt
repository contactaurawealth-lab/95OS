package com.os95.app.data.repository

import com.os95.app.core.database.entity.FormulaEntity
import com.os95.app.testutil.FakeFormulaDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OfflineFormulaRepositoryTest {

    private lateinit var dao: FakeFormulaDao
    private lateinit var repository: OfflineFormulaRepository

    @Before
    fun setUp() {
        dao = FakeFormulaDao()
        repository = OfflineFormulaRepository(dao)
    }

    @Test
    fun testAddAndGetAllFormulas() = runBlocking {
        val f1 = repository.addFormula(
            subjectId = "sub_math",
            chapterId = "ch_calc",
            title = "Product Rule",
            expression = "(uv)' = u'v + uv'",
            explanation = "Calculus derivative rule",
            examRelevance = "HIGH"
        )

        val f2 = repository.addFormula(
            subjectId = "sub_physics",
            chapterId = "ch_kinematics",
            title = "First Equation of Motion",
            expression = "v = u + at",
            explanation = "Uniform acceleration",
            examRelevance = "HIGH"
        )

        val all = repository.getAllFormulas().first()
        assertEquals(2, all.size)
        assertTrue(all.any { it.id == f1.id })
        assertTrue(all.any { it.id == f2.id })
    }

    @Test
    fun testGetFormulasBySubject() = runBlocking {
        repository.addFormula("sub_math", "ch_calc", "Integral", "∫ x dx = x^2/2 + C", "", "MEDIUM")
        repository.addFormula("sub_physics", "ch_em", "Gauss Law", "∮ E·dA = Q/ε0", "", "HIGH")

        val mathFormulas = repository.getFormulasBySubject("sub_math").first()
        assertEquals(1, mathFormulas.size)
        assertEquals("Integral", mathFormulas[0].title)

        val physicsFormulas = repository.getFormulasBySubject("sub_physics").first()
        assertEquals(1, physicsFormulas.size)
        assertEquals("Gauss Law", physicsFormulas[0].title)
    }

    @Test
    fun testToggleBookmarkAndGetBookmarked() = runBlocking {
        val f = repository.addFormula("sub_math", "ch_trig", "Pythagorean Identity", "sin^2(x) + cos^2(x) = 1", "", "HIGH")
        assertFalse(f.isBookmarked)

        repository.toggleBookmark(f.id, true)
        val bookmarked = repository.getBookmarkedFormulas().first()
        assertEquals(1, bookmarked.size)
        assertEquals(f.id, bookmarked[0].id)
        assertTrue(bookmarked[0].isBookmarked)

        repository.toggleBookmark(f.id, false)
        val bookmarkedAfter = repository.getBookmarkedFormulas().first()
        assertEquals(0, bookmarkedAfter.size)
    }

    @Test
    fun testDeleteFormula() = runBlocking {
        val f = repository.addFormula("sub_chem", "ch_thermo", "Gibbs Free Energy", "ΔG = ΔH - TΔS", "", "HIGH")
        val before = repository.getAllFormulas().first()
        assertEquals(1, before.size)

        repository.deleteFormula(f)
        val after = repository.getAllFormulas().first()
        assertEquals(0, after.size)
    }
}
