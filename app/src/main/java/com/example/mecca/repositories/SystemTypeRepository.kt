package com.example.mecca.repositories

import android.util.Log
import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.dataClasses.SystemTypeLocal
import com.example.mecca.FetchResult


class SystemTypeRepository(private val apiService: ApiService, private val db: AppDatabase) {

    // Function to fetch data from the API and store it in the database
    suspend fun fetchAndStoreSystemTypes(): FetchResult {

        Log.d("DEBUG", "Fetching System Types from API...")

        return try {
            // Make the API call
            val response = apiService.getSystemTypes()

            // Check if the response was successful
            if (response.isSuccessful) {
                val apiSystemTypes = response.body()
                if (apiSystemTypes != null) {

                    // Clear the existing records before inserting new ones
                    try {
                        db.systemTypeDAO().deleteAllSystemTypes()
                        Log.d("DEBUG", "Cleared local system type database")
                    } catch (e: Exception) {
                        val errorMessage = "Error clearing system type database: ${e.message}"
                        Log.e("DEBUG", errorMessage)
                        return FetchResult.Failure(errorMessage)
                    }

                    // Map API data to local entities
                    val systemTypeLocals = apiSystemTypes.mapIndexed { index, apiSystemType ->

                        Log.d(
                            "DEBUG",
                            "Mapping System Types $index: ID=${apiSystemType.id}, Name=${apiSystemType.systemType ?: "Null"}"
                        )

                        // Return SystemTypeLocal for insertion
                        SystemTypeLocal(
                            //id = 0,  // Auto-generate ID in the Room database
                            id = apiSystemType.id ?: 0,  // Handle possible null values
                            systemType = apiSystemType.systemType ?: "Unknown Name"  // Default to "Unknown Name" if null
                        )
                    }

                    // Insert the mapped data into the local database
                    db.systemTypeDAO().insertSystemTypes(systemTypeLocals)
                    Log.d("DEBUG", "System Types successfully inserted into the local database.")

                    return FetchResult.Success("System Types successfully fetched and stored")

                } else {
                    // Handle the case where body is null
                    val errorMessage = "No data found."
                    Log.e("API", errorMessage)
                    return FetchResult.Failure(errorMessage)
                }
            } else {
                // Handle HTTP errors (4xx, 5xx)
                val errorMessage = "Error: ${response.code()}, Message: ${response.message()}"
                Log.e("API", errorMessage)
                return FetchResult.Failure(errorMessage)
            }
        } catch (e: Exception) {
            // Handle any exceptions that occurred during the API call
            val errorMessage = "Exception occurred: ${e.message}"
            Log.e("API", errorMessage)
            return FetchResult.Failure(errorMessage)
        }
    }

    suspend fun getAllSystemTypes(): List<SystemTypeLocal> {
        return db.systemTypeDAO().getAllSystemTypes()
    }


    // Function to get all systems from the local database
    suspend fun getSystemTypesFromDb(): List<SystemTypeLocal> {
        return db.systemTypeDAO().getAllSystemTypes()  // Modify as per your DAO method
    }

}
