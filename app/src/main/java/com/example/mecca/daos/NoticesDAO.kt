package com.example.mecca.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mecca.dataClasses.NoticeLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotices(notices: List<NoticeLocal>)


    @Query("SELECT * FROM notice WHERE isActive = 1 ORDER BY isPinned DESC, dateAdded DESC")
    fun getActiveNotices(): Flow<List<NoticeLocal>>

    @Query("SELECT * FROM notice WHERE isActive = 1 ORDER BY isPinned DESC, dateAdded DESC")
    suspend fun getActiveNoticesOnce(): List<NoticeLocal>


}