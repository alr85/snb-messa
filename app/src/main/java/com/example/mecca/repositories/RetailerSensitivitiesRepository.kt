package com.example.mecca.repositories

import androidx.room.withTransaction
import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.FetchResult
import com.example.mecca.dataClasses.ConveyorRetailerSensitivitiesEntity
import com.example.mecca.dataClasses.FreefallThroatRetailerSensitivitiesEntity
import com.example.mecca.dataClasses.PipelineRetailerSensitivitiesEntity

class RetailerSensitivitiesRepository(
    private val api: ApiService,
    private val db: AppDatabase
) {

    // ------------------ CLOUD SYNC ------------------

    suspend fun fetchAndStoreConveyor(): FetchResult =
        try {
            val items = api.getConveyorLevels()
            db.withTransaction {
                db.conveyorDao().clearAll()
                db.conveyorDao().insertAll(items)
            }
            FetchResult.Success("Conveyor levels synced: ${items.size}")
        } catch (e: Exception) {
            FetchResult.Failure("Conveyor sync failed: ${e.message}")
        }

    suspend fun fetchAndStoreFreefall(): FetchResult =
        try {
            val items = api.getFreefallLevels()
            db.withTransaction {
                db.freefallDao().clearAll()
                db.freefallDao().insertAll(items)
            }
            FetchResult.Success("Freefall levels synced: ${items.size}")
        } catch (e: Exception) {
            FetchResult.Failure("Freefall sync failed: ${e.message}")
        }

    suspend fun fetchAndStorePipeline(): FetchResult =
        try {
            val items = api.getPipelineLevels()
            db.withTransaction {
                db.pipelineDao().clearAll()
                db.pipelineDao().insertAll(items)
            }
            FetchResult.Success("Pipeline levels synced: ${items.size}")
        } catch (e: Exception) {
            FetchResult.Failure("Pipeline sync failed: ${e.message}")
        }

    // ------------------ LOCAL QUERIES ------------------

    suspend fun getSensitivitiesByHeight(heightMm: Double): ConveyorRetailerSensitivitiesEntity? {
        return db.conveyorDao().findByHeight(heightMm)
    }

    suspend fun getPipelineSensitivitiesByAperture(heightMm: Double): PipelineRetailerSensitivitiesEntity? {
        return db.pipelineDao().findByHeight(heightMm)
    }

    suspend fun getFreefallSensitivitiesByAperture(heightMm: Double): FreefallThroatRetailerSensitivitiesEntity? {
        return db.freefallDao().findByHeight(heightMm)
    }

}
