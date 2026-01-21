package com.example.mecca.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mecca.dataClasses.MdModelsLocal

@Dao
interface MdModelsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMdModel(mdModels: List<MdModelsLocal>)

    @Query("SELECT * FROM MdModels")
    suspend fun getAllMdModels(): List<MdModelsLocal>

    @Query("DELETE FROM MdModels")
    suspend fun deleteAllMdModels()  // Function to delete all MD Models

    @Query("SELECT modelDescription FROM MdModels WHERE meaId = :meaId")
    suspend fun getMdModelDescriptionFromDb(meaId: Int): String?

    @Query("SELECT * FROM MdModels WHERE meaId = :meaId Limit 1")
    suspend fun getMdModelDetailsFromDb(meaId: Int): MdModelsLocal?

}
