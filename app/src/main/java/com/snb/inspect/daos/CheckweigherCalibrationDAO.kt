package com.snb.inspect.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.snb.inspect.dataClasses.CheckweigherCalibrationLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckweigherCalibrationDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalibration(calibration: CheckweigherCalibrationLocal)

    @Update
    suspend fun updateCalibration(calibration: CheckweigherCalibrationLocal)

    @Query("SELECT * FROM CheckweigherCalibrations WHERE calibrationId = :id")
    suspend fun getCalibrationById(id: String): CheckweigherCalibrationLocal?

    @Query("SELECT * FROM CheckweigherCalibrations WHERE endDate = '' OR endDate IS NULL")
    fun getAllUnfinishedCalibrations(): Flow<List<CheckweigherCalibrationLocal>>

    @Query("SELECT * FROM CheckweigherCalibrations WHERE endDate != '' AND endDate IS NOT NULL AND isSynced = 0")
    fun getAllPendingCalibrations(): Flow<List<CheckweigherCalibrationLocal>>

    @Query("SELECT * FROM CheckweigherCalibrations WHERE isSynced = 1 ORDER BY endDate DESC LIMIT 10")
    fun getAllCompletedCalibrations(): Flow<List<CheckweigherCalibrationLocal>>

    @Query("SELECT * FROM CheckweigherCalibrations WHERE tempSystemId = :tempId AND isSynced = 0")
    suspend fun getUnsyncedCalibrationByTempId(tempId: Int): CheckweigherCalibrationLocal?

    @Query("SELECT * FROM CheckweigherCalibrations WHERE isSynced = 0")
    suspend fun getUnsyncedCalibrations(): List<CheckweigherCalibrationLocal>

    @Query("UPDATE CheckweigherCalibrations SET isSynced = 1 WHERE calibrationId = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM CheckweigherCalibrations WHERE calibrationId = :id")
    suspend fun deleteCalibration(id: String)

    @Query("SELECT * FROM CheckweigherCalibrations ORDER BY startDate DESC")
    fun getAllCalibrationsFlow(): Flow<List<CheckweigherCalibrationLocal>>

    @Query("UPDATE CheckweigherCalibrations SET cloudSystemId = :cloudId WHERE tempSystemId = :tempId")
    suspend fun updateCalibrationWithCloudId(tempId: Int, cloudId: Int)

    @Query("UPDATE CheckweigherCalibrations SET cloudSystemId = :cloudId WHERE systemId = :systemId")
    suspend fun updateCalibrationWithCloudIdBySystemId(systemId: Int, cloudId: Int)

    @Query("UPDATE CheckweigherCalibrations SET cloudSystemId = :cloudId WHERE calibrationId = :calibrationId")
    suspend fun updateCloudIdByCalibrationId(calibrationId: String, cloudId: Int)
}
