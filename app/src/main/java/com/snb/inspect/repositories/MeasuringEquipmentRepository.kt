package com.snb.inspect.repositories

import androidx.room.withTransaction
import com.snb.inspect.ApiService
import com.snb.inspect.AppDatabase
import com.snb.inspect.FetchResult
import com.snb.inspect.dataClasses.toLocal
import com.snb.inspect.util.InAppLogger
import kotlinx.coroutines.flow.Flow
import com.snb.inspect.dataClasses.MeasuringEquipmentLocal
import kotlin.collections.emptyList
import kotlin.collections.map

class MeasuringEquipmentRepository(
 private val apiService: ApiService,
 private val db: AppDatabase
) {

 fun observeEquipment(): Flow<List<MeasuringEquipmentLocal>> {
  return db.measuringEquipmentDAO().getAllEquipment()
 }

 suspend fun fetchAndStoreEquipment(): FetchResult {
  InAppLogger.d("Sync: Starting Measuring Equipment refresh...")

  return try {
   val response = apiService.getMeasuringEquipment()

   if (!response.isSuccessful) {
    val errorMsg = "Sync: Failed to fetch measuring equipment (Code: ${response.code()})"
    InAppLogger.e(errorMsg)
    return FetchResult.Failure(errorMsg)
   }

   // Standard Kotlin functions work automatically without specific imports
   val apiItems = response.body() ?: emptyList()
   val localEntities = apiItems.map { it.toLocal() }

   db.withTransaction {
    val oldCount = db.measuringEquipmentDAO().getCount()

    db.measuringEquipmentDAO().deleteAll()
    db.measuringEquipmentDAO().upsertEquipment(localEntities)

    InAppLogger.d("Sync: Measuring Equipment updated. Old Count: $oldCount, New Count: ${localEntities.size}")
   }

   FetchResult.Success("Measuring equipment synchronized successfully.")

  } catch (e: Exception) {
   val fatalError = "Sync: Error updating measuring equipment: ${e.localizedMessage}"
   InAppLogger.e(fatalError)
   FetchResult.Failure("Network error. Please check your connection.")
  }
 }
}
