package com.example.mecca.repositories

import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.FetchResult
import com.example.mecca.dataClasses.MdModelsLocal
import com.example.mecca.util.InAppLogger

class MetalDetectorModelsRepository(
    private val apiService: ApiService,
    private val db: AppDatabase
) {

    suspend fun fetchAndStoreMdModels(): FetchResult {
        InAppLogger.d("Fetching metal detector models from API...")

        val result: FetchResult = try {
            val response = apiService.getMdModels()
            InAppLogger.d("MD models API call complete. HTTP ${response.code()}")

            if (!response.isSuccessful) {
                FetchResult.Failure("HTTP ${response.code()} error: ${response.message()}")
            } else {
                val apiMdModels = response.body()

                if (apiMdModels.isNullOrEmpty()) {
                    FetchResult.Failure("No MD model data returned by API.")
                } else {
                    db.mdModelDao().deleteAllMdModels()
                    InAppLogger.d("Cleared local MD models database.")

                    val mdModelLocals = apiMdModels.map { apiMdModel ->
                        MdModelsLocal(
                            id = 0,
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

                    FetchResult.Success("Fetched & stored ${mdModelLocals.size} MD model(s).")
                }
            }
        } catch (e: Exception) {
            FetchResult.Failure("MD model fetch/store failed: ${e.message}")
        }

        // Brief summary log (now correct)
        when (result) {
            is FetchResult.Success -> InAppLogger.d("MD models sync SUCCESS: ${result.message}")
            is FetchResult.Failure -> InAppLogger.e("MD models sync FAIL: ${result.errorMessage}")
        }

        return result
    }

    suspend fun getMdModelsFromDb(): List<MdModelsLocal> {
        val models = db.mdModelDao().getAllMdModels()
        InAppLogger.d("Fetched ${models.size} MD models from local database.")
        return models
    }

    suspend fun getMdModelDetails(meaId: Int): MdModelsLocal? {
        val details = db.mdModelDao().getMdModelDetailsFromDb(meaId)
        InAppLogger.d("Fetched MD model details for meaId=$meaId -> ${details?.meaId ?: "null"}")
        return details
    }
}
