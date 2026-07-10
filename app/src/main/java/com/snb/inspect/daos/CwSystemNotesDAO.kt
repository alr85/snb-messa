package com.snb.inspect.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.snb.inspect.dataClasses.CwSystemNoteLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface CwSystemNotesDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<CwSystemNoteLocal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: CwSystemNoteLocal): Long

    @Update
    suspend fun updateNote(note: CwSystemNoteLocal)

    @Query("SELECT * FROM CwSystemNotes WHERE systemId = :systemId")
    fun getNotesForSystem(systemId: Int): Flow<List<CwSystemNoteLocal>>

    @Query("SELECT * FROM CwSystemNotes WHERE isSynced = 0")
    suspend fun getUnsyncedNotes(): List<CwSystemNoteLocal>

    @Query("UPDATE CwSystemNotes SET cloudId = :cloudId, isSynced = 1 WHERE id = :localId")
    suspend fun updateCloudId(localId: Int, cloudId: Int)

    @Query("SELECT * FROM CwSystemNotes WHERE cloudId IS NOT NULL")
    suspend fun getAllSyncedNotes(): List<CwSystemNoteLocal>

    @Query("DELETE FROM CwSystemNotes WHERE systemId = :systemId AND isSynced = 1 AND cloudId NOT IN (:cloudIds)")
    suspend fun deleteSyncedNotesNotIn(systemId: Int, cloudIds: List<Int>)

    @Query("DELETE FROM CwSystemNotes WHERE systemId = :systemId AND isSynced = 1")
    suspend fun deleteAllSyncedNotesForSystem(systemId: Int)

    @Query("DELETE FROM CwSystemNotes")
    suspend fun deleteAll()
}
