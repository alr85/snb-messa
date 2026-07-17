package com.snb.inspect.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.snb.inspect.dataClasses.CheckweigherWithFullDetails
import com.snb.inspect.dataClasses.CwSystemLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface CwSystemsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(systems: List<CwSystemLocal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystem(system: CwSystemLocal): Long

    @Update
    suspend fun updateSystem(system: CwSystemLocal)

    @Query("SELECT * FROM CwSystems")
    fun getAllSystemsFlow(): Flow<List<CwSystemLocal>>

    @Query("""
        SELECT s.*, 
               COALESCE(m.modelDescription, 'Unknown Model') as modelDescription, 
               COALESCE(st.systemType, 'Checkweigher') as systemType, 
               COALESCE(c.name, 'Unknown Customer') as customerName 
        FROM CwSystems s
        LEFT JOIN CwModels m ON s.modelId = m.meaId
        LEFT JOIN systemTypes st ON s.systemTypeId = st.id
        LEFT JOIN customer c ON s.customerId = c.fusionID
        WHERE s.customerId = :customerId
    """)
    fun getCheckweighersByCustomerId(customerId: Int): Flow<List<CheckweigherWithFullDetails>>

    @Query("SELECT * FROM CwSystems WHERE id = :id")
    suspend fun getSystemById(id: Int): CwSystemLocal?

    @Query("SELECT * FROM CwSystems WHERE serialNumber = :serialNumber")
    suspend fun getSystemBySerialNumber(serialNumber: String): CwSystemLocal?

    @Query("SELECT * FROM CwSystems WHERE cloudId = :cloudId")
    suspend fun getSystemByCloudId(cloudId: Int): CwSystemLocal?

    @Query("SELECT * FROM CwSystems WHERE isSynced = 0")
    suspend fun getUnsyncedSystems(): List<CwSystemLocal>

    @Query("UPDATE CwSystems SET cloudId = :cloudId, isSynced = 1 WHERE id = :localId")
    suspend fun updateCloudId(localId: Int, cloudId: Int)

    @Query("UPDATE CwSystems SET isSynced = :isSynced WHERE (cloudId = :cloudId OR :cloudId IS NULL) AND (id = :localId OR :localId IS NULL)")
    suspend fun updateIsSynced(isSynced: Boolean, cloudId: Int?, localId: Int?)

    @Query("DELETE FROM CwSystems")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM CwSystems")
    suspend fun getCount(): Int

    @Query("""
        SELECT s.*, 
               COALESCE(m.modelDescription, 'Unknown Model') as modelDescription, 
               COALESCE(st.systemType, 'Checkweigher') as systemType, 
               COALESCE(c.name, 'Unknown Customer') as customerName 
        FROM CwSystems s
        LEFT JOIN CwModels m ON s.modelId = m.meaId
        LEFT JOIN systemTypes st ON s.systemTypeId = st.id
        LEFT JOIN customer c ON s.customerId = c.fusionID
        WHERE s.id = :id
    """)
    suspend fun getCheckweigherWithFullDetailsUsingLocalId(id: Int): CheckweigherWithFullDetails?

    @Query("""
        SELECT s.*, 
               COALESCE(m.modelDescription, 'Unknown Model') as modelDescription, 
               COALESCE(st.systemType, 'Checkweigher') as systemType, 
               COALESCE(c.name, 'Unknown Customer') as customerName 
        FROM CwSystems s
        LEFT JOIN CwModels m ON s.modelId = m.meaId
        LEFT JOIN systemTypes st ON s.systemTypeId = st.id
        LEFT JOIN customer c ON s.customerId = c.fusionID
        WHERE s.serialNumber = :serialNumber
        LIMIT 1
    """)
    suspend fun getCheckweigherWithFullDetailsBySerialNumber(serialNumber: String): CheckweigherWithFullDetails?

    @Query("""
        SELECT s.*, 
               COALESCE(m.modelDescription, 'Unknown Model') as modelDescription, 
               COALESCE(st.systemType, 'Checkweigher') as systemType, 
               COALESCE(c.name, 'Unknown Customer') as customerName 
        FROM CwSystems s
        LEFT JOIN CwModels m ON s.modelId = m.meaId
        LEFT JOIN systemTypes st ON s.systemTypeId = st.id
        LEFT JOIN customer c ON s.customerId = c.fusionID
    """)
    suspend fun getAllCheckweighersWithFullDetails(): List<CheckweigherWithFullDetails>
}
