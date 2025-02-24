package com.example.mecca.Repositories

import android.util.Log
import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.DataClasses.MdModelsLocal
import com.example.mecca.FetchResult


class MetalDetectorModelsRepository(private val apiService: ApiService, private val db: AppDatabase) {

    // Function to fetch data from the API and store it in the database
    suspend fun fetchAndStoreMdModels(): FetchResult {

        Log.d("DEBUG", "Fetching MD models from API...")

        return try {
            // Make the API call
            val response = apiService.getMdModels()

            // Check if the response was successful
            if (response.isSuccessful) {
                val apiMdModels = response.body()
                if (apiMdModels != null) {

                    // Clear the existing records before inserting new ones
                    try {
                        db.mdModelDao().deleteAllMdModels()
                        Log.d("DEBUG", "Cleared local MD systems database")
                    } catch (e: Exception) {
                        val errorMessage = "Error clearing MD systems database: ${e.message}"
                        Log.e("DEBUG", errorMessage)
                        return FetchResult.Failure(errorMessage)
                    }

                    // Map API data to local entities
                    val mdModelLocals = apiMdModels.mapIndexed { index, apiMdModels ->

                        Log.d(
                            "DEBUG",
                            "Mapping MD Systems $index: ID=${apiMdModels.model_id}, Model ID=${apiMdModels.model_id ?: "Null"}"
                        )

                        // Return MD ModelLocal for insertion
                    MdModelsLocal(
                        id = 0,  // Auto-generate ID in the Room database
                        meaId = apiMdModels.model_id ?:0,
                        modelDescription = apiMdModels.model_description ?: "Unknown Name",  // Default to "Unknown Name" if null
                        detectionSetting1 = apiMdModels.detectionSetting1 ?: "N/A",
                        detectionSetting2 = apiMdModels.detectionSetting2 ?: "N/A",
                        detectionSetting3 = apiMdModels.detectionSetting3 ?: "N/A",
                        detectionSetting4 = apiMdModels.detectionSetting4 ?: "N/A",
                        detectionSetting5 = apiMdModels.detectionSetting5 ?: "N/A",
                        detectionSetting6 = apiMdModels.detectionSetting6 ?: "N/A"
                        )
                    }

                    // Insert the mapped data into the local database
                    db.mdModelDao().insertMdModel(mdModelLocals)
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

    // Function to get all customers from the local database
    suspend fun getMdModelsFromDb(): List<MdModelsLocal> {
        return db.mdModelDao().getAllMdModels()
    }

    // Function to get model description
    suspend fun getMdModelDescription(meaId: Int): String? {
        return db.mdModelDao().getMdModelDescriptionFromDb(meaId)
    }

    // Function to get all model details
    suspend fun getMdModelDetails(meaId: Int): MdModelsLocal? {
        return db.mdModelDao().getMdModelDetailsFromDb(meaId)
    }


}

