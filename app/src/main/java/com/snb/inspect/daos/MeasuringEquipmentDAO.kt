package com.snb.inspect.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.snb.inspect.dataClasses.MeasuringEquipmentLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasuringEquipmentDAO {
    @Query("SELECT * FROM measuring_equipment ORDER BY manufacturer ASC, model ASC")
    fun getAllEquipment(): Flow<List<MeasuringEquipmentLocal>>

    @Query("SELECT * FROM measuring_equipment WHERE deviceTypeID = :typeId AND isActive = 1 ORDER BY manufacturer ASC, model ASC")
    fun getEquipmentByType(typeId: Int): Flow<List<MeasuringEquipmentLocal>>

    @Upsert
    suspend fun upsertEquipment(items: List<MeasuringEquipmentLocal>)

    @Query("DELETE FROM measuring_equipment")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM measuring_equipment")
    suspend fun getCount(): Int
}