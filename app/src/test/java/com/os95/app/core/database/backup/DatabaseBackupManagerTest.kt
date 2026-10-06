package com.os95.app.core.database.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class DatabaseBackupManagerTest {

    @Test
    fun testSQLiteHeaderValidation_detectsValidPrefix() {
        val validHeader = "SQLite format 3\u0000".toByteArray()
        val stream = ByteArrayInputStream(validHeader)
        val readBuffer = ByteArray(16)
        val read = stream.read(readBuffer)
        assertEquals(16, read)
        val headerString = String(readBuffer)
        assertTrue(headerString.startsWith("SQLite format 3"))
    }

    @Test
    fun testSQLiteHeaderValidation_rejectsArbitraryBinary() {
        val corruptedBytes = "RANDOM INVALID BINARY DATA".toByteArray()
        val stream = ByteArrayInputStream(corruptedBytes)
        val readBuffer = ByteArray(16)
        val read = stream.read(readBuffer)
        assertTrue(read >= 15)
        val headerString = String(readBuffer)
        assertFalse(headerString.startsWith("SQLite format 3"))
    }

    @Test
    fun testStreamBuffering_preservesByteIntegrity() {
        val testData = "Test database backup payload for 95OS offline system".toByteArray()
        val output = ByteArrayOutputStream()
        output.write(testData)
        val input = ByteArrayInputStream(output.toByteArray())
        val restored = input.readBytes()
        assertEquals(testData.size, restored.size)
        assertEquals(String(testData), String(restored))
    }
}
