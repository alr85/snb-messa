package com.snb.inspect.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.snb.inspect.dataClasses.CwModelsLocal

@Dao
interface CwModelsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(models: List<CwModelsLocal>)

    @Query("SELECT * FROM CwModels ORDER BY modelDescription ASC")
    suspend fun getAllModels(): List<CwModelsLocal>

    @Query("DELETE FROM CwModels")
    suspend fun deleteAll()

    @Query("SELECT modelDescription FROM CwModels WHERE meaId = :modelId")
    suspend fun getModelDescription(modelId: Int): String?
}
