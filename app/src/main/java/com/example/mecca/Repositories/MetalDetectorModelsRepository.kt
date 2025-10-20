package com.example.mecca.Repositories

import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.FetchResult
import com.example.mecca.dataClasses.MdModelsLocal
import com.example.mecca.util.InAppLogger

class MetalDetectorModelsRepository(private val apiService: ApiService, private val db: AppDatabase) {

    // Function to fetch data from the API and store it in the database
    suspend fun fetchAndStoreMdModels(): FetchResult {

        InAppLogger.d("Fetching metal detector models from API...")

        return try {
            val response = apiService.getMdModels()
            InAppLogger.d("API call to fetch MD models complete. HTTP ${response.code()}")

            if (response.isSuccessful) {
                val apiMdModels = response.body()
                InAppLogger.d("API returned ${apiMdModels?.size ?: 0} models.")

                if (apiMdModels != null) {
                    try {
                        db.mdModelDao().deleteAllMdModels()
                        InAppLogger.d("Cleared local MD models database.")
                    } catch (e: Exception) {
                        val errorMessage = "Error clearing MD models database: ${e.message}"
                        InAppLogger.e(errorMessage)
                        return FetchResult.Failure(errorMessage)
                    }

                    val mdModelLocals = apiMdModels.mapIndexed { index, apiMdModel ->
//                        InAppLogger.d(
//                            "Mapping model $index: ID=${apiMdModel.model_id ?: "Null"}, " +
//                                    "Description=${apiMdModel.model_description ?: "Null"}"
//                        )

                        MdModelsLocal(
                            id = 0, // Auto-generate ID in Room
                            meaId = apiMdModel.model_id ?: 0,
                            modelDescription = apiMdModel.model_description ?: "Unknown Name",
                            detectionSetting1 = apiMdModel.detectionSetting1 ?: "N/A",
                            detectionSetting2 = apiMdModel.detectionSetting2 ?: "N/A",
                            detectionSetting3 = apiMdModel.detectionSetting3 ?: "N/A",
                            detectionSetting4 = apiMdModel.detectionSetting4 ?: "N/A",
                            detectionSetting5 = apiMdModel.detectionSetting5 ?: "N/A",
                            detectionSetting6 = apiMdModel.detectionSetting6 ?: "N/A",
                            detectionSetting7 = apiMdModel.detectionSetting7 ?: "N/A",
                            detectionSetting8 = apiMdModel.detectionSetting8 ?: "N/A"
                        )
                    }

                    db.mdModelDao().insertMdModel(mdModelLocals)
                    InAppLogger.d("Inserted ${mdModelLocals.size} MD models into local database.")

                    return FetchResult.Success("MD models successfully fetched and stored.")
                } else {
                    val errorMessage = "No MD model data returned by API."
                    InAppLogger.e(errorMessage)
                    return FetchResult.Failure(errorMessage)
                }
            } else {
                val errorMessage = "HTTP ${response.code()} error: ${response.message()}"
                InAppLogger.e(errorMessage)
                return FetchResult.Failure(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = "Exception during MD model fetch: ${e.message}"
            InAppLogger.e(errorMessage)
            return FetchResult.Failure(errorMessage)
        }
    }

    // Function to get all models from the local database
    suspend fun getMdModelsFromDb(): List<MdModelsLocal> {
        val models = db.mdModelDao().getAllMdModels()
        InAppLogger.d("Fetched ${models.size} MD models from local database.")
        return models
    }

    // Function to get full model details by meaId
    suspend fun getMdModelDetails(meaId: Int): MdModelsLocal? {
        val details = db.mdModelDao().getMdModelDetailsFromDb(meaId)
        InAppLogger.d("Fetched MD model details for meaId=$meaId -> $details")
        return details
    }
}
