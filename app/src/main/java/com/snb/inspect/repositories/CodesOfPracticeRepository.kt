package com.snb.inspect.repositories

import com.snb.inspect.ApiService
import com.snb.inspect.AppDatabase
import com.snb.inspect.FetchResult
import com.snb.inspect.dataClasses.CodeOfPracticeLocal
import com.snb.inspect.util.InAppLogger
import kotlinx.coroutines.flow.Flow

class CodesOfPracticeRepository(
    private val apiService: ApiService,
    private val db: AppDatabase
) {
    val allCodes: Flow<List<CodeOfPracticeLocal>> = db.codesOfPracticeDAO().getAllCodes()

    suspend fun fetchAndStoreCodes(): FetchResult {
        InAppLogger.d("Fetching codes of practice from API...")
        return try {
            val response = apiService.getCodesOfPractice()
            if (response.isSuccessful) {
                val apiCodes = response.body() ?: emptyList()
                db.codesOfPracticeDAO().deleteAllCodes()
                
                val locals = apiCodes.map { 
                    CodeOfPracticeLocal(
                        id = it.id,
                        title = it.title,
                        url = it.url,
                        description = it.description,
                        category = it.category
                    )
                }
                db.codesOfPracticeDAO().insertCodes(locals)
                FetchResult.Success("Synced ${locals.size} codes of practice")
            } else {
                FetchResult.Failure("API Error: ${response.code()}")
            }
        } catch (e: Exception) {
            FetchResult.Failure("Sync failed: ${e.message}")
        }
    }
}
