package com.os95.app.core.csv

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversalCsvProcessorTest {

    private val processor = DefaultUniversalCsvProcessor()

    @Test
    fun testTemplateGenerationNotEmpty() {
        UniversalCsvEntity.values().forEach { entity ->
            val template = processor.getTemplate(entity)
            assertTrue("Template for $entity should not be empty", template.isNotBlank())
            assertTrue("Template for $entity should contain newline", template.contains("\n"))
        }
    }

    @Test
    fun testParseValidSyllabusCsv() {
        val csv = "SubjectName,ChapterName,TopicName\n" +
                  "Physics,Mechanics,Newton's Laws\n" +
                  "Mathematics,Calculus,Derivatives\n"

        val result = processor.parseAndValidate(UniversalCsvEntity.SYLLABUS, csv)
        assertEquals(2, result.validRows.size)
        assertEquals(0, result.errors.size)
        assertEquals("Physics", result.validRows[0]["subjectname"])
        assertEquals("Newton's Laws", result.validRows[0]["topicname"])
    }

    @Test
    fun testMissingHeaderGeneratesError() {
        val csv = "SubjectName,ChapterName\n" +
                  "Physics,Mechanics\n"

        val result = processor.parseAndValidate(UniversalCsvEntity.SYLLABUS, csv)
        assertEquals(0, result.validRows.size)
        assertEquals(1, result.errors.size)
        assertTrue(result.errors.first().message.contains("Missing required columns"))
    }
}
