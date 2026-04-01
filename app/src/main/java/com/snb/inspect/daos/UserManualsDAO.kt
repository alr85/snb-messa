package com.snb.inspect.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.snb.inspect.dataClasses.UserManualLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface UserManualsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManuals(manuals: List<UserManualLocal>)

    @Query("SELECT * FROM UserManuals")
    fun getAllManuals(): Flow<List<UserManualLocal>>

    @Query("DELETE FROM UserManuals")
    suspend fun deleteAllManuals()
}
