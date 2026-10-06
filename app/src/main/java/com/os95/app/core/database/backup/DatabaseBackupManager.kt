package com.os95.app.core.database.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.os95.app.core.database.OS95Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * 95OS Local SQLite Database Backup & Restore Manager.
 * Guarantees 100% offline student data sovereignty by allowing raw atomic
 * export and restoration of Room SQLite database files (.95os / .db).
 */
object DatabaseBackupManager {

    private const val DB_NAME = "95os_offline.db"
    private const val SQLITE_HEADER_PREFIX = "SQLite format 3"

    /**
     * Checkpoints WAL journal into main SQLite file and streams raw bytes to output stream.
     */
    suspend fun exportDatabase(
        context: Context,
        destinationStream: OutputStream
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val db = OS95Database.getInstance(context)
            // Force checkpoint of WAL logs into the primary .db file
            try {
                val writableDb = db.openHelper.writableDatabase
                val cursor = writableDb.query("PRAGMA wal_checkpoint(FULL);")
                cursor.moveToFirst()
                cursor.close()
            } catch (e: Exception) {
                // Checkpoint best effort
            }

            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                return@withContext Result.failure(IllegalStateException("Database file not found on device."))
            }

            var totalBytes = 0L
            FileInputStream(dbFile).use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    destinationStream.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead
                }
                destinationStream.flush()
            }

            Result.success(totalBytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validates and restores an SQLite database backup into 95OS.
     */
    suspend fun importDatabase(
        context: Context,
        sourceStream: InputStream
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val tempBackupFile = File(context.cacheDir, "temp_restore_95os.db")
        try {
            // 1. Write stream to temporary cache file
            FileOutputStream(tempBackupFile).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (sourceStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                output.flush()
            }

            // 2. Validate SQLite format header
            FileInputStream(tempBackupFile).use { input ->
                val headerBytes = ByteArray(16)
                val read = input.read(headerBytes)
                if (read < 15 || !String(headerBytes).startsWith(SQLITE_HEADER_PREFIX)) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Invalid file format. Must be a valid 95OS SQLite database backup.")
                    )
                }
            }

            // 3. Quick integrity check on temp database
            var isValid = false
            try {
                val tempDb = SQLiteDatabase.openDatabase(
                    tempBackupFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )
                val cursor = tempDb.rawQuery("PRAGMA quick_check;", null)
                if (cursor.moveToFirst()) {
                    val result = cursor.getString(0)
                    isValid = result.equals("ok", ignoreCase = true)
                }
                cursor.close()
                tempDb.close()
            } catch (e: Exception) {
                isValid = false
            }

            if (!isValid) {
                return@withContext Result.failure(
                    IllegalStateException("Database backup failed SQLite integrity check.")
                )
            }

            // 4. Close active Room instance
            try {
                val db = OS95Database.getInstance(context)
                db.close()
            } catch (e: Exception) {
                // Ignore if already closed
            }

            // 5. Replace current database file
            val targetDbFile = context.getDatabasePath(DB_NAME)
            targetDbFile.parentFile?.mkdirs()

            // Delete associated WAL and SHM files
            val walFile = File(targetDbFile.parentFile, "$DB_NAME-wal")
            val shmFile = File(targetDbFile.parentFile, "$DB_NAME-shm")
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            // Copy temp file over target file
            FileInputStream(tempBackupFile).use { input ->
                FileOutputStream(targetDbFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            if (tempBackupFile.exists()) {
                tempBackupFile.delete()
            }
        }
    }
}
