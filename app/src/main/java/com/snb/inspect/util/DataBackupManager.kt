package com.snb.inspect.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.google.gson.GsonBuilder
import com.snb.inspect.AppDatabase
import com.snb.inspect.dataClasses.CheckweigherCalibrationLocal
import com.snb.inspect.dataClasses.MetalDetectorConveyorCalibrationLocal
import com.snb.inspect.dataClasses.SensitivityOptimisationValidationLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object DataBackupManager {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    // Hidden directory in public Documents to survive uninstalls even if offline
    private val HIDDEN_DIR = "${Environment.DIRECTORY_DOCUMENTS}/.snb_internal_metadata"
    private const val INTERNAL_BACKUP_DIR = "internal_backups"

    /**
     * Backs up calibration data to both internal storage (for cloud sync)
     * and a hidden public folder (to survive uninstalls offline).
     */
    suspend fun <T> backupCalibration(context: Context, entity: T, id: String, type: String) = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(entity)
            val fileName = "snb_recovery_${type}_$id.json"

            // 1. Save to Internal (Private Storage)
            val internalDir = File(context.filesDir, INTERNAL_BACKUP_DIR)
            if (!internalDir.exists()) internalDir.mkdirs()
            File(internalDir, fileName).writeText(json)

            // 2. Save to Hidden Public Folder (Media Store)
            val contentResolver = context.contentResolver
            val externalUri = MediaStore.Files.getContentUri("external")

            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
            val selectionArgs = arrayOf(fileName, "$HIDDEN_DIR/")

            var existingUri: Uri? = null
            contentResolver.query(externalUri, arrayOf(MediaStore.MediaColumns._ID), selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    existingUri = Uri.withAppendedPath(externalUri, cursor.getLong(idCol).toString())
                }
            }

            val targetUri = existingUri ?: contentResolver.insert(externalUri, ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, HIDDEN_DIR)
            })

            targetUri?.let { uri ->
                contentResolver.openOutputStream(uri, "wt")?.use { out ->
                    out.write(json.toByteArray())
                }
            }
            InAppLogger.d("Multi-layer backup secured: $fileName")
        } catch (e: Exception) {
            InAppLogger.e("Backup failed for $id: ${e.message}")
        }
    }

    /**
     * Removes both internal and hidden public backups once synced to the cloud.
     */
    suspend fun removeBackup(context: Context, id: String, type: String) = withContext(Dispatchers.IO) {
        try {
            val fileName = "snb_recovery_${type}_$id.json"

            // Remove internal
            File(File(context.filesDir, INTERNAL_BACKUP_DIR), fileName).let { if (it.exists()) it.delete() }

            // Remove hidden public
            val contentResolver = context.contentResolver
            val externalUri = MediaStore.Files.getContentUri("external")
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
            val selectionArgs = arrayOf(fileName, "$HIDDEN_DIR/")
            contentResolver.delete(externalUri, selection, selectionArgs)

            InAppLogger.d("Backup cleaned up for $id")
        } catch (e: Exception) {
            InAppLogger.e("Cleanup failed for $id: ${e.message}")
        }
    }

    /**
     * Scans for any available recovery files and restores them if the DB is empty/records missing.
     */
    suspend fun checkAndRestore(context: Context, db: AppDatabase) = withContext(Dispatchers.IO) {
        try {
            val recoveryFiles = mutableMapOf<String, String>() // Map<FileName, JsonContent>

            // 1. Scan Internal
            val internalDir = File(context.filesDir, INTERNAL_BACKUP_DIR)
            if (internalDir.exists()) {
                internalDir.listFiles()?.forEach { recoveryFiles[it.name] = it.readText() }
            }

            // 2. Scan Hidden Public (MediaStore)
            val contentResolver = context.contentResolver
            val externalUri = MediaStore.Files.getContentUri("external")
            val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
            val selectionArgs = arrayOf("$HIDDEN_DIR/")

            contentResolver.query(externalUri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns._ID), selection, selectionArgs, null)?.use { cursor ->
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameCol)
                    if (!recoveryFiles.containsKey(name)) {
                        val uri = Uri.withAppendedPath(externalUri, cursor.getLong(idCol).toString())
                        contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }?.let {
                            recoveryFiles[name] = it
                        }
                    }
                }
            }

            if (recoveryFiles.isEmpty()) return@withContext

            var restoredCount = 0
            recoveryFiles.forEach { (name, json) ->
                try {
                    when {
                        name.contains("_MD_") -> {
                            val entity = gson.fromJson(json, MetalDetectorConveyorCalibrationLocal::class.java)
                            db.metalDetectorConveyorCalibrationDAO().insertOrUpdateCalibration(entity)
                            restoredCount++
                        }
                        name.contains("_CW_") -> {
                            val entity = gson.fromJson(json, CheckweigherCalibrationLocal::class.java)
                            db.checkweigherCalibrationDAO().insertCalibration(entity)
                            restoredCount++
                        }
                        name.contains("_SOV_") -> {
                            val entity = gson.fromJson(json, SensitivityOptimisationValidationLocal::class.java)
                            db.sensitivityOptimisationValidationDAO().insertOrUpdate(entity)
                            restoredCount++
                        }
                    }
                } catch (e: Exception) {
                    InAppLogger.e("Self-heal failed for $name: ${e.message}")
                }
            }
            if (restoredCount > 0) InAppLogger.d("Self-healing complete. Restored $restoredCount records.")
        } catch (e: Exception) {
            InAppLogger.e("Self-healing check failed: ${e.message}")
        }
    }
}