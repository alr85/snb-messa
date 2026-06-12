package com.snb.inspect.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.snb.inspect.dataClasses.MdSystemNoteLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface MdSystemNotesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<MdSystemNoteLocal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: MdSystemNoteLocal): Long

    @Query("SELECT * FROM MdSystemNotes WHERE systemId = :systemId AND isDeleted = 0 ORDER BY addedDate DESC")
    fun observeNotesForSystem(systemId: Int): Flow<List<MdSystemNoteLocal>>

    @Query("SELECT * FROM MdSystemNotes WHERE systemId = :systemId AND isDeleted = 0 ORDER BY addedDate DESC")
    suspend fun getNotesForSystem(systemId: Int): List<MdSystemNoteLocal>

    @Query("SELECT * FROM MdSystemNotes WHERE isSynced = 0")
    suspend fun getUnsyncedNotes(): List<MdSystemNoteLocal>

    @Update
    suspend fun updateNote(note: MdSystemNoteLocal)

    @Query("UPDATE MdSystemNotes SET isSynced = 1, cloudId = :cloudId WHERE id = :localId")
    suspend fun markAsSynced(localId: Int, cloudId: Int)

    @Query("SELECT * FROM MdSystemNotes WHERE cloudId IS NOT NULL")
    suspend fun getAllSyncedNotes(): List<MdSystemNoteLocal>

    @Query("SELECT * FROM MdSystemNotes WHERE cloudId = :cloudId LIMIT 1")
    suspend fun getNoteByCloudId(cloudId: Int): MdSystemNoteLocal?
}
