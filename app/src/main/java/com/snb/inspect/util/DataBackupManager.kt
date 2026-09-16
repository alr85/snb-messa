package com.snb.inspect.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
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

    // Directory in public Documents to survive uninstalls even if offline
    private const val RECOVERY_DIR_NAME = "SNB_Inspect_Recovery"
    private val RECOVERY_DIR = "${Environment.DIRECTORY_DOCUMENTS}/$RECOVERY_DIR_NAME"
    private val OLD_HIDDEN_DIR = "${Environment.DIRECTORY_DOCUMENTS}/.snb_internal_metadata"
    private const val INTERNAL_BACKUP_DIR = "internal_backups"

    /**
     * Backs up calibration data to both internal storage (for cloud sync)
     * and a public folder (to survive uninstalls offline).
     */
    suspend fun <T> backupCalibration(context: Context, entity: T, id: String, type: String) = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(entity)
            val fileName = "snb_recovery_${type}_$id.json"

            // 1. Save to Internal (Private Storage)
            val internalDir = File(context.filesDir, INTERNAL_BACKUP_DIR)
            if (!internalDir.exists()) internalDir.mkdirs()
            File(internalDir, fileName).writeText(json)

            // 2. Save to Public Folder (Media Store)
            val contentResolver = context.contentResolver
            val externalUri = MediaStore.Files.getContentUri("external")

            // Ensure relative path ends with /
            val relativePath = "$RECOVERY_DIR/"

            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
            val selectionArgs = arrayOf(fileName, relativePath)

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
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
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
     * Removes both internal and public backups once synced to the cloud.
     */
    suspend fun removeBackup(context: Context, id: String, type: String) = withContext(Dispatchers.IO) {
        try {
            val fileName = "snb_recovery_${type}_$id.json"

            // Remove internal
            File(File(context.filesDir, INTERNAL_BACKUP_DIR), fileName).let { if (it.exists()) it.delete() }

            // Remove public
            val contentResolver = context.contentResolver
            val externalUri = MediaStore.Files.getContentUri("external")
            
            // Try removing from both new and old paths just in case
            val paths = arrayOf("$RECOVERY_DIR/", "$OLD_HIDDEN_DIR/")
            paths.forEach { path ->
                val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
                val selectionArgs = arrayOf(fileName, path)
                contentResolver.delete(externalUri, selection, selectionArgs)
            }

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

            // 0. Scan Persisted SAF Folder (if linked)
            val syncPrefs = SyncPreferences(context)
            syncPrefs.getRecoveryFolderUri()?.let { uriString ->
                try {
                    val treeUri = Uri.parse(uriString)
                    InAppLogger.d("Auto-scanning persisted recovery folder: $uriString")
                    val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
                    rootDoc?.listFiles()?.forEach { file ->
                        val name = file.name ?: ""
                        if (name.startsWith("snb_recovery_") && name.endsWith(".json")) {
                            context.contentResolver.openInputStream(file.uri)?.bufferedReader()?.use { it.readText() }?.let {
                                recoveryFiles[name] = it
                            }
                        }
                    }
                } catch (e: Exception) {
                    InAppLogger.e("Persisted scan failed: ${e.message}")
                }
            }

            // 1. Scan Internal
            val internalDir = File(context.filesDir, INTERNAL_BACKUP_DIR)
            InAppLogger.d("Checking internal recovery dir: ${internalDir.absolutePath}")
            if (internalDir.exists()) {
                val files = internalDir.listFiles()
                InAppLogger.d("Internal dir exists. Found ${files?.size ?: 0} files.")
                files?.forEach { 
                    if (it.name.endsWith(".json")) {
                        InAppLogger.d("Adding internal recovery file: ${it.name}")
                        recoveryFiles[it.name] = it.readText() 
                    }
                }
            } else {
                InAppLogger.d("Internal recovery dir does not exist.")
            }

            // 2. Scan Public (MediaStore) - Check both new and old hidden directory
            val contentResolver = context.contentResolver
            val externalUri = MediaStore.Files.getContentUri("external")
            
            // We use a broader query and filter in Kotlin to be safe across Android versions
            val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf("%${Environment.DIRECTORY_DOCUMENTS}%")

            InAppLogger.d("Scanning MediaStore for recovery files...")
            contentResolver.query(
                externalUri, 
                arrayOf(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns._ID, MediaStore.MediaColumns.RELATIVE_PATH), 
                selection, 
                selectionArgs, 
                null
            )?.use { cursor ->
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
                
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameCol)
                    val path = cursor.getString(pathCol)
                    
                    // Only process files in our specific recovery folders
                    val isInNewDir = path.contains(RECOVERY_DIR_NAME)
                    val isInOldDir = path.contains(".snb_internal_metadata")
                    
                    if ((isInNewDir || isInOldDir) && name.startsWith("snb_recovery_") && name.endsWith(".json")) {
                        if (!recoveryFiles.containsKey(name)) {
                            InAppLogger.d("Found recovery file in public storage: $name (path: $path)")
                            val uri = Uri.withAppendedPath(externalUri, cursor.getLong(idCol).toString())
                            try {
                                contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }?.let {
                                    recoveryFiles[name] = it
                                }
                            } catch (e: Exception) {
                                InAppLogger.e("Failed to read $name: ${e.message}")
                            }
                        }
                    }
                }
            }

            if (recoveryFiles.isEmpty()) {
                InAppLogger.d("No automatic recovery files found.")
                return@withContext
            }

            processRecoveryMap(db, recoveryFiles)
        } catch (e: Exception) {
            InAppLogger.e("Self-healing check failed: ${e.message}")
        }
    }

    /**
     * Manual import from a folder selected via SAF (Storage Access Framework).
     * This is the most reliable way to recover data from older installs.
     */
    suspend fun importFromTreeUri(context: Context, treeUri: Uri, db: AppDatabase) = withContext(Dispatchers.IO) {
        try {
            InAppLogger.d("Starting manual import and persisting permission...")
            
            // Persist access to this folder
            context.contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            
            // Save the URI for future auto-scans
            SyncPreferences(context).setRecoveryFolderUri(treeUri.toString())

            val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
            if (rootDoc == null || !rootDoc.isDirectory) {
                InAppLogger.e("Invalid folder selected.")
                return@withContext
            }

            val recoveryFiles = mutableMapOf<String, String>()
            val files = rootDoc.listFiles()
            InAppLogger.d("Selected folder has ${files.size} files.")

            files.forEach { file ->
                val name = file.name ?: ""
                if (name.startsWith("snb_recovery_") && name.endsWith(".json")) {
                    try {
                        context.contentResolver.openInputStream(file.uri)?.bufferedReader()?.use { it.readText() }?.let {
                            recoveryFiles[name] = it
                        }
                    } catch (e: Exception) {
                        InAppLogger.e("Failed to read manual file $name: ${e.message}")
                    }
                }
            }

            if (recoveryFiles.isEmpty()) {
                InAppLogger.d("No valid SNB recovery files found in selected folder.")
            } else {
                processRecoveryMap(db, recoveryFiles)
            }
        } catch (e: Exception) {
            InAppLogger.e("Manual import failed: ${e.message}")
        }
    }

    private suspend fun processRecoveryMap(db: AppDatabase, recoveryFiles: Map<String, String>) {
        InAppLogger.d("Processing ${recoveryFiles.size} potential recovery records...")
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
                InAppLogger.e("Import failed for $name: ${e.message}")
            }
        }
        if (restoredCount > 0) InAppLogger.d("Import complete. Restored/Updated $restoredCount records.")
        else InAppLogger.d("No new records were imported.")
    }
}