package com.example.mecca.repositories

import android.util.Log
import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.dataClasses.SystemTypeLocal
import com.example.mecca.FetchResult


class SystemTypeRepository(
    private val apiService: ApiService,
    private val db: AppDatabase
) {

    suspend fun fetchAndStoreSystemTypes(): FetchResult {
        Log.d("DEBUG", "Fetching System Types from API...")

        return try {
            val response = apiService.getSystemTypes()

            if (!response.isSuccessful) {
                val msg = "Error: ${response.code()}, Message: ${response.message()}"
                Log.e("API", msg)
                return FetchResult.Failure(msg)
            }

            val apiSystemTypes = response.body()
            if (apiSystemTypes.isNullOrEmpty()) {
                val msg = "No data found."
                Log.e("API", msg)
                return FetchResult.Failure(msg)
            }

            // Clear existing
            try {
                db.systemTypeDAO().deleteAllSystemTypes()
                Log.d("DEBUG", "Cleared local system type database")
            } catch (e: Exception) {
                val msg = "Error clearing system type database: ${e.message}"
                Log.e("DEBUG", msg)
                return FetchResult.Failure(msg)
            }

            // Map API -> local entities
            val systemTypeLocals = apiSystemTypes.map { apiSystemType ->
                SystemTypeLocal(
                    id = apiSystemType.id ?: 0,
                    systemType = apiSystemType.systemType ?: "Unknown Name"
                )
            }

            db.systemTypeDAO().insertSystemTypes(systemTypeLocals)
            Log.d("DEBUG", "System Types successfully inserted into the local database.")

            FetchResult.Success("System Types successfully fetched and stored")

        } catch (e: Exception) {
            val msg = "Exception occurred: ${e.message}"
            Log.e("API", msg)
            FetchResult.Failure(msg)
        }
    }

    suspend fun getSystemTypesFromDb(): List<SystemTypeLocal> =
        db.systemTypeDAO().getAllSystemTypes()
}
