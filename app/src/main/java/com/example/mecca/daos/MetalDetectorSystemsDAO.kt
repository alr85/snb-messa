package com.example.mecca.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mecca.dataClasses.MdSystemLocal
import com.example.mecca.dataClasses.MetalDetectorWithFullDetails


//What it does: Defines methods for interacting with the data in the database (like querying, inserting, or deleting rows).
//How it interacts: The DAO uses the entity class (MdSystemLocal) to know what data structure it is working with and performs actions like reading or writing data to the corresponding table.

@Dao
interface MetalDetectorSystemsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMdSystem(mdSystems: List<MdSystemLocal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewMdSystem(mdSystems: MdSystemLocal)

//    @Query("SELECT * FROM MdSystems")
//    suspend fun getAllMdSystems(): List<MdSystemLocal>

    @Query("DELETE FROM MdSystems")
    suspend fun deleteAllMdSystems()  // Function to delete all MD Systems

    @Query(
        """
            SELECT 
        MdSystems.id, 
        MdSystems.cloudId,
        MdSystems.modelId, 
        MdSystems.customerId, 
        MdSystems.serialNumber, 
        MdSystems.apertureWidth, 
        MdSystems.apertureHeight, 
        MdSystems.lastCalibration, 
        MdSystems.addedDate, 
        MdSystems.calibrationInterval,
        MdSystems.systemTypeId,
        MdSystems.cloudId,
        MdSystems.tempId,
        MdSystems.isSynced,
        MdSystems.lastLocation,
        customer.fusionID,
        MdModels.modelDescription,
        systemTypes.systemType,
        customer.name AS customerName
    FROM MdSystems
    JOIN MdModels ON MdSystems.modelId = MdModels.meaId
    JOIN systemTypes ON MdSystems.systemTypeId = systemTypes.id
    JOIN customer ON MdSystems.customerId = customer.fusionID
    WHERE (:systemId IS NULL OR MdSystems.cloudId = :systemId)
    """
    )
    suspend fun getMetalDetectorsWithFullDetailsUsingCloudId(systemId: Int?): List<MetalDetectorWithFullDetails>

    @Query(
        """
            SELECT 
        MdSystems.id, 
        MdSystems.cloudId,
        MdSystems.modelId, 
        MdSystems.customerId, 
        MdSystems.serialNumber, 
        MdSystems.apertureWidth, 
        MdSystems.apertureHeight, 
        MdSystems.lastCalibration, 
        MdSystems.addedDate, 
        MdSystems.calibrationInterval,
        MdSystems.systemTypeId,
        MdSystems.cloudId,
        MdSystems.tempId,
        MdSystems.isSynced,
        MdSystems.lastLocation,
        customer.fusionID,
        MdModels.modelDescription,
        systemTypes.systemType,
        customer.name AS customerName
    FROM MdSystems
    JOIN MdModels ON MdSystems.modelId = MdModels.meaId
    JOIN systemTypes ON MdSystems.systemTypeId = systemTypes.id
    JOIN customer ON MdSystems.customerId = customer.fusionID
    WHERE (:systemId IS NULL OR MdSystems.id = :systemId)
    """
    )
    suspend fun getMetalDetectorsWithFullDetailsUsingLocalId(systemId: Int?): List<MetalDetectorWithFullDetails>


    @Query("SELECT * FROM MdSystems WHERE isSynced = 0")
    suspend fun getUnsyncedMdSystems(): List<MdSystemLocal>

    @Update
    suspend fun updateMdSystem(mdSystem: MdSystemLocal)

    @Query("SELECT * FROM MdSystems WHERE serialNumber = :serialNumber LIMIT 1")
    suspend fun getSystemBySerialNumber(serialNumber: String): MdSystemLocal?

    @Query("UPDATE MdSystems SET lastCalibration = :lastCalibration WHERE id = :systemId")
    suspend fun updateLastCalibrationDate(systemId: Int, lastCalibration: String)

    @Query("UPDATE MdSystems SET isSynced = :isSynced, cloudId = :newCloudId WHERE tempId = :tempId")
    suspend fun updateSyncStatus(isSynced: Boolean, tempId: Int, newCloudId: Int)

    @Query("UPDATE MdSystems SET lastLocation = :lastLocation WHERE id = :systemId")
    suspend fun updateLastLocation(systemId: Int, lastLocation: String)

    @Query("UPDATE MdSystems SET isSynced = :isSynced WHERE cloudId = :cloudId OR id = :localId")
    suspend fun updateIsSynced(isSynced: Boolean, cloudId: Int?, localId: Int?)

    @Query("SELECT * FROM MdSystems WHERE cloudId = :cloudId LIMIT 1")
    suspend fun getSystemByCloudId(cloudId: Int): MdSystemLocal?

    @Query("SELECT * FROM MdSystems WHERE id = :localId LIMIT 1")
    suspend fun getSystemByLocalId(localId: Int): MdSystemLocal?

    @Query("SELECT * FROM MdSystems WHERE tempId = :tempId LIMIT 1")
    suspend fun getSystemByTempId(tempId: Int): MdSystemLocal?

    @Query("SELECT * FROM MdSystems WHERE isSynced = 0 OR cloudId = 0")
    suspend fun getUnsyncedOrNoCloudId(): List<MdSystemLocal>

    @Query("SELECT * FROM MdSystems WHERE isSynced = 0 OR cloudId IS NULL OR cloudId = 0")
    suspend fun getSystemsNeedingUpload(): List<MdSystemLocal>

    @Query("SELECT COUNT(*) FROM MdSystems WHERE isSynced = 0 OR cloudId IS NULL OR cloudId = 0")
    suspend fun countSystemsNeedingUpload(): Int






}
