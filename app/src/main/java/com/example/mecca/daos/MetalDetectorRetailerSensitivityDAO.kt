package com.example.mecca.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mecca.dataClasses.ConveyorRetailerSensitivitiesEntity
import com.example.mecca.dataClasses.FreefallThroatRetailerSensitivitiesEntity
import com.example.mecca.dataClasses.PipelineRetailerSensitivitiesEntity

@Dao
interface ConveyorDao {
    @Query("DELETE FROM ConveyorRetailerSensitivities") suspend fun clearAll()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(items: List<ConveyorRetailerSensitivitiesEntity>)
    @Query("SELECT * FROM ConveyorRetailerSensitivities WHERE :h BETWEEN minProductHeightMM AND maxProductHeightMM LIMIT 1")
    suspend fun findByHeight(h: Double): ConveyorRetailerSensitivitiesEntity?
}

@Dao
interface PipelineDao {
    @Query("DELETE FROM PipelineRetailerSensitivities") suspend fun clearAll()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(items: List<PipelineRetailerSensitivitiesEntity>)
    @Query("SELECT * FROM PipelineRetailerSensitivities WHERE :d BETWEEN minInternalPipeMM AND maxInternalPipeMM LIMIT 1")
    suspend fun findByHeight(d: Double): PipelineRetailerSensitivitiesEntity?
}

@Dao
interface FreefallDao {
    @Query("DELETE FROM FreefallThroatRetailerSensitivities") suspend fun clearAll()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(items: List<FreefallThroatRetailerSensitivitiesEntity>)
    @Query("SELECT * FROM FreefallThroatRetailerSensitivities WHERE :d BETWEEN minThroatApertureMM AND maxThroatApertureMM LIMIT 1")
    suspend fun findByHeight(d: Double): FreefallThroatRetailerSensitivitiesEntity?
}



