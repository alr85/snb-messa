package com.snb.inspect.repositories

import com.snb.inspect.ApiService
import com.snb.inspect.AppDatabase
import com.snb.inspect.FetchResult
import com.snb.inspect.dataClasses.UserManualLocal
import com.snb.inspect.util.InAppLogger
import kotlinx.coroutines.flow.Flow

class UserManualsRepository(
    private val apiService: ApiService,
    private val db: AppDatabase
) {
    val allManuals: Flow<List<UserManualLocal>> = db.userManualsDAO().getAllManuals()

    suspend fun fetchAndStoreManuals(): FetchResult {
        InAppLogger.d("Fetching user manuals from API...")
        return try {
            val response = apiService.getUserManuals()
            if (response.isSuccessful) {
                val apiManuals = response.body() ?: emptyList()
                db.userManualsDAO().deleteAllManuals()
                
                val locals = apiManuals.map { 
                    UserManualLocal(
                        id = it.id,
                        description = it.description,
                        url = it.url,
                        notes = it.notes,
                        systemType = it.systemType
                    )
                }
                db.userManualsDAO().insertManuals(locals)
                FetchResult.Success("Synced ${locals.size} manuals")
            } else {
                FetchResult.Failure("API Error: ${response.code()}")
            }
        } catch (e: Exception) {
            FetchResult.Failure("Sync failed: ${e.message}")
        }
    }
}
