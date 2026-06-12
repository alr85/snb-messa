package com.snb.inspect.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.snb.inspect.dataClasses.CodeOfPracticeLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface CodesOfPracticeDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCodes(codes: List<CodeOfPracticeLocal>)

    @Query("SELECT * FROM CodesOfPractice")
    fun getAllCodes(): Flow<List<CodeOfPracticeLocal>>

    @Query("DELETE FROM CodesOfPractice")
    suspend fun deleteAllCodes()
}
