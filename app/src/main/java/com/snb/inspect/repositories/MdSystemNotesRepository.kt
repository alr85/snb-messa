package com.snb.inspect.repositories

import android.content.Context
import com.snb.inspect.ApiService
import com.snb.inspect.AppDatabase
import com.snb.inspect.FetchResult
import com.snb.inspect.dataClasses.MdSystemNoteCloud
import com.snb.inspect.dataClasses.MdSystemNoteLocal
import com.snb.inspect.network.isNetworkAvailable
import com.snb.inspect.util.InAppLogger
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MdSystemNotesRepository(private val apiService: ApiService, private val db: AppDatabase) {

    private val dao = db.mdSystemNotesDAO()

    fun observeNotesForSystem(systemId: Int): Flow<List<MdSystemNoteLocal>> {
        return dao.observeNotesForSystem(systemId)
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
        val note = MdSystemNoteLocal(
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

        try {
            // 1. Pull from cloud if cloudSystemId is available
            if (cloudSystemId != null && cloudSystemId != 0) {
                val response = apiService.getMdSystemNotes(cloudSystemId)
                if (response.isSuccessful) {
                    val cloudNotes = response.body() ?: emptyList()
                    val existingNotes = dao.getAllSyncedNotes().associateBy { it.cloudId }
                    
                    val localNotes = cloudNotes.map { cloudNote ->
                        val localMatch = existingNotes[cloudNote.id]
                        MdSystemNoteLocal(
                            id = localMatch?.id ?: 0, // Preserve local primary key
                            cloudId = cloudNote.id,
                            systemId = systemId,
                            cloudSystemId = cloudSystemId,
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
                    dao.insertNotes(localNotes)
                }
            }

            // 2. Push unsynced notes
            return syncAllUnsyncedNotes(context)
        } catch (e: Exception) {
            InAppLogger.e("Notes Sync Error: ${e.message}")
            return FetchResult.Failure(e.message ?: "Unknown error")
        }
    }

    suspend fun fetchAndStoreAllNotes(): FetchResult {
        try {
            val response = apiService.getAllMdSystemNotes()
            if (response.isSuccessful) {
                val cloudNotes = response.body() ?: emptyList()
                val existingNotes = dao.getAllSyncedNotes().associateBy { it.cloudId }
                
                val entities = cloudNotes.map { cloudNote ->
                    val localSystem = db.mdSystemDAO().getSystemByCloudId(cloudNote.systemId)
                    val localMatch = existingNotes[cloudNote.id]
                    
                    MdSystemNoteLocal(
                        id = localMatch?.id ?: 0, // Preserve local primary key
                        cloudId = cloudNote.id,
                        systemId = localSystem?.id ?: 0,
                        cloudSystemId = cloudNote.systemId,
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
                }.filter { it.systemId != 0 }

                dao.insertNotes(entities)
                return FetchResult.Success("Bulk notes pull complete")
            }
            return FetchResult.Failure("Server error: ${response.code()}")
        } catch (e: Exception) {
            InAppLogger.e("Bulk Notes Pull Error: ${e.message}")
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
                // We need cloudSystemId to post to cloud.
                // If it's null, we might need to look it up from MdSystems table if it was synced in a previous step.
                var cloudSystemId = note.cloudSystemId
                if (cloudSystemId == null || cloudSystemId == 0) {
                    val system = db.mdSystemDAO().getSystemByLocalId(note.systemId)
                    cloudSystemId = system?.cloudId
                }
                
                if (cloudSystemId == null || cloudSystemId == 0) {
                    InAppLogger.d("Skipping note ${note.id}: Parent Machine has no Cloud ID yet.")
                    continue
                }

                val cloudNote = MdSystemNoteCloud(
                    id = note.cloudId,
                    systemId = cloudSystemId,
                    addedBy = note.addedBy,
                    addedDate = note.addedDate,
                    noteText = note.noteText,
                    noteType = note.noteType,
                    isImportant = note.isImportant,
                    isDeleted = note.isDeleted,
                    editedBy = note.editedBy,
                    editedDate = note.editedDate,
                    deletedBy = note.deletedBy,
                    deletedDate = note.deletedDate
                )

                if (note.cloudId == null) {
                    val resp = apiService.postMdSystemNote(cloudNote)
                    if (resp.isSuccessful) {
                        resp.body()?.id?.let { newCloudId ->
                            dao.markAsSynced(note.id, newCloudId)
                            InAppLogger.d("Note ${note.id} uploaded successfully.")
                            successCount++
                        }
                    } else {
                        failCount++
                        InAppLogger.e("Failed to upload note ${note.id}: Server returned ${resp.code()}")
                    }
                } else {
                    val resp = apiService.updateMdSystemNote(note.cloudId, cloudNote)
                    if (resp.isSuccessful) {
                        dao.markAsSynced(note.id, note.cloudId)
                        InAppLogger.d("Note ${note.id} updated in cloud.")
                        successCount++
                    } else {
                        failCount++
                        InAppLogger.e("Failed to update note ${note.id}: Server returned ${resp.code()}")
                    }
                }
            }
            return if (failCount == 0) FetchResult.Success("All notes uploaded ($successCount)")
            else FetchResult.Failure("Uploaded $successCount, Failed $failCount")
        } catch (e: Exception) {
            InAppLogger.e("Bulk Notes Sync Error: ${e.message}")
            return FetchResult.Failure(e.message ?: "Unknown error")
        }
    }
}
