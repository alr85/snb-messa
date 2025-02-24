package com.example.mecca.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mecca.DataClasses.SystemTypeLocal

@Dao
interface SystemTypeDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemTypes(systemTypes: List<SystemTypeLocal>)

    @Query("SELECT * FROM systemTypes")
    suspend fun getAllSystemTypes(): List<SystemTypeLocal>

    @Query("DELETE FROM systemTypes")
    suspend fun deleteAllSystemTypes()  // Function to delete all systemTypes

}

