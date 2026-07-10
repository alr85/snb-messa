package com.snb.inspect.repositories

import android.content.Context
import androidx.room.withTransaction
import com.snb.inspect.ApiService
import com.snb.inspect.AppDatabase
import com.snb.inspect.FetchResult
import com.snb.inspect.dataClasses.CwSystemNoteCloud
import com.snb.inspect.dataClasses.CwSystemNoteLocal
import com.snb.inspect.network.isNetworkAvailable
import com.snb.inspect.util.InAppLogger
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CwSystemNotesRepository(private val apiService: ApiService, private val db: AppDatabase) {

    private val dao = db.cwSystemNotesDAO()

    fun observeNotesForSystem(systemId: Int): Flow<List<CwSystemNoteLocal>> {
        return dao.getNotesForSystem(systemId)
    }

    suspend fun getAuthorName(meaId: Int): String {
        return db.userDao().getUsernameByMeaId(meaId) ?: "Unknown"
    }

    suspend fun addNote(
        systemId: Int,
        cloudSystemId: Int?,
        addedBy: Int,
        noteText: String,
        noteType: String? = "General",
        isImportant: Boolean = false
    ) {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val note = CwSystemNoteLocal(
            systemId = systemId,
            cloudSystemId = cloudSystemId,
            addedBy = addedBy,
            addedDate = now,
            noteText = noteText,
            noteType = noteType,
            isImportant = isImportant,
            isSynced = false
        )
        dao.insertNote(note)
    }

    suspend fun syncNotes(context: Context, systemId: Int, cloudSystemId: Int?): FetchResult {
        if (!isNetworkAvailable(context)) return FetchResult.Failure("Offline")

        var effectiveCloudSystemId = cloudSystemId
        if (effectiveCloudSystemId == null || effectiveCloudSystemId == 0) {
            val system = db.cwSystemsDAO().getSystemById(systemId)
            effectiveCloudSystemId = system?.cloudId
        }

        try {
            db.withTransaction {
                // 1. Pull from cloud if effectiveCloudSystemId is available
                if (effectiveCloudSystemId != null && effectiveCloudSystemId != 0) {
                    val response = apiService.getCwSystemNotes(effectiveCloudSystemId)
                    if (response.isSuccessful) {
                        val cloudNotes = response.body() ?: emptyList()
                        val existingNotes = dao.getAllSyncedNotes().associateBy { it.cloudId }

                        val localNotes = cloudNotes.map { cloudNote ->
                            val localMatch = if (cloudNote.id != null) existingNotes[cloudNote.id] else null
                            CwSystemNoteLocal(
                                id = localMatch?.id ?: 0, // Preserve local primary key
                                cloudId = cloudNote.id,
                                systemId = systemId,
                                cloudSystemId = effectiveCloudSystemId,
                                addedBy = cloudNote.addedBy,
                                addedDate = cloudNote.addedDate,
                                noteText = cloudNote.noteText,
                                noteType = cloudNote.noteType,
                                isImportant = cloudNote.isImportant,
                                isDeleted = cloudNote.isDeleted,
                                editedBy = cloudNote.editedBy,
                                editedDate = cloudNote.editedDate,
                                deletedBy = cloudNote.deletedBy,
                                deletedDate = cloudNote.deletedDate,
                                isSynced = true
                            )
                        }
                        dao.insertAll(localNotes)

                        // PRUNING: Remove local synced notes that are no longer in the cloud for this machine
                        val cloudIds = cloudNotes.mapNotNull { it.id }
                        if (cloudIds.isEmpty()) {
                            dao.deleteAllSyncedNotesForSystem(systemId)
                        } else {
                            dao.deleteSyncedNotesNotIn(systemId, cloudIds)
                        }

                        InAppLogger.d("Synced ${localNotes.size} notes from cloud for CW system $systemId.")
                    } else {
                        InAppLogger.e("Failed to pull CW notes: ${response.code()}")
                    }
                }
            }

            // 2. Push unsynced notes
            return syncAllUnsyncedNotes(context)
        } catch (e: Exception) {
            InAppLogger.e("CW Notes Sync Error: ${e.message}")
            return FetchResult.Failure(e.message ?: "Unknown error")
        }
    }

    suspend fun syncAllUnsyncedNotes(context: Context): FetchResult {
        if (!isNetworkAvailable(context)) return FetchResult.Failure("Offline")
        
        try {
            val unsynced = dao.getUnsyncedNotes()
            if (unsynced.isEmpty()) return FetchResult.Success("No notes to sync")
            
            var successCount = 0
            var failCount = 0

            for (note in unsynced) {
                var cloudSystemId = note.cloudSystemId
                if (cloudSystemId == null || cloudSystemId == 0) {
                    val system = db.cwSystemsDAO().getSystemById(note.systemId)
                    cloudSystemId = system?.cloudId
                }
                
                if (cloudSystemId == null || cloudSystemId == 0) {
                    InAppLogger.e("Cannot sync note ${note.id}: System ${note.systemId} has no Cloud ID yet.")
                    continue
                }

                val cloudNote = CwSystemNoteCloud(
                    id = note.cloudId,
                    systemId = cloudSystemId,
                    addedBy = note.addedBy,
                    addedDate = note.addedDate ?: "",
                    noteText = note.noteText ?: "",
                    noteType = note.noteType,
                    isImportant = note.isImportant,
                    isDeleted = note.isDeleted,
                    editedBy = note.editedBy,
                    editedDate = note.editedDate,
                    deletedBy = note.deletedBy,
                    deletedDate = note.deletedDate
                )

                if (note.cloudId == null) {
                    val resp = apiService.postCwSystemNote(cloudNote)
                    if (resp.isSuccessful) {
                        resp.body()?.id?.let { newCloudId ->
                            dao.updateCloudId(note.id, newCloudId)
                            successCount++
                        }
                    } else {
                        InAppLogger.e("Failed to POST CW note: ${resp.code()} ${resp.errorBody()?.string()}")
                        failCount++
                    }
                } else {
                    val resp = apiService.updateCwSystemNote(note.cloudId, cloudNote)
                    if (resp.isSuccessful) {
                        dao.updateCloudId(note.id, note.cloudId)
                        successCount++
                    } else {
                        InAppLogger.e("Failed to PUT CW note: ${resp.code()} ${resp.errorBody()?.string()}")
                        failCount++
                    }
                }
            }
            return FetchResult.Success("Synced $successCount notes ($failCount failed)")
        } catch (e: Exception) {
            InAppLogger.e("syncAllUnsyncedNotes Error: ${e.message}")
            return FetchResult.Failure(e.message ?: "Unknown error")
        }
    }

    suspend fun fetchAndStoreAllNotes(): FetchResult {
        try {
            val response = apiService.getAllCwSystemNotes()
            if (response.isSuccessful) {
                val cloudNotes = response.body() ?: emptyList()
                // Atomic merge sync could be implemented here if needed for mass pull
                return FetchResult.Success("All notes pulled")
            }
            return FetchResult.Failure("Error: ${response.code()}")
        } catch (e: Exception) {
            return FetchResult.Failure(e.message ?: "Unknown error")
        }
    }
}
