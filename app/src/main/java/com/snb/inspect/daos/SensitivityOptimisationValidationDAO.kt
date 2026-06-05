package com.snb.inspect.daos

import androidx.room.*
import com.snb.inspect.dataClasses.SensitivityOptimisationValidationLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface SensitivityOptimisationValidationDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(sov: SensitivityOptimisationValidationLocal)

    @Query("SELECT * FROM SensitivityOptimisationValidations WHERE sovId = :id")
    suspend fun getById(id: String): SensitivityOptimisationValidationLocal?

    @Query("SELECT * FROM SensitivityOptimisationValidations WHERE sovId = :id")
    fun getByIdFlow(id: String): Flow<SensitivityOptimisationValidationLocal?>

    @Query("SELECT * FROM SensitivityOptimisationValidations WHERE (endDate IS NULL OR endDate = '')")
    fun getAllUnfinished(): Flow<List<SensitivityOptimisationValidationLocal>>

    @Query("SELECT * FROM SensitivityOptimisationValidations WHERE (isSynced IS NULL OR isSynced = 0) AND endDate IS NOT NULL AND endDate != ''")
    fun getAllPending(): Flow<List<SensitivityOptimisationValidationLocal>>

    @Query("SELECT * FROM SensitivityOptimisationValidations WHERE (isSynced = True OR isSynced = 1) AND endDate IS NOT NULL AND endDate != ''")
    fun getAllCompleted(): Flow<List<SensitivityOptimisationValidationLocal>>

    @Query("UPDATE SensitivityOptimisationValidations SET isSynced = :isSynced WHERE sovId = :id")
    suspend fun updateIsSynced(id: String, isSynced: Boolean)

    @Delete
    suspend fun delete(sov: SensitivityOptimisationValidationLocal)
}
