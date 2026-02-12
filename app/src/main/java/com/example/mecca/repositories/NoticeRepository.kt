package com.example.mecca.repositories

import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.FetchResult
import com.example.mecca.dataClasses.NoticeLocal
import com.example.mecca.dataClasses.mappers.toLocal
import com.example.mecca.util.InAppLogger

class NoticeRepository(
    private val apiService: ApiService,
    private val db: AppDatabase
) {
    fun observeActiveNotices() = db.noticesDAO().getActiveNotices()

    suspend fun fetchAndStoreNotices(): FetchResult {
        InAppLogger.d("Fetching notices from API...")
        return try {
            val response = apiService.getNotices()
            InAppLogger.d("API call to fetch notices complete. HTTP ${response.code()}")

            if (!response.isSuccessful) {
                val msg = "HTTP ${response.code()} error: ${response.message()}"
                InAppLogger.e(msg)
                return FetchResult.Failure(msg)
            }

            val apiNotices = response.body().orEmpty()
            val noticeLocals = apiNotices.mapNotNull { it.toLocal() }

            db.noticesDAO().upsertNotices(noticeLocals)
            InAppLogger.d("Upserted ${noticeLocals.size} notices into local DB.")

            FetchResult.Success(
                if (noticeLocals.isEmpty()) "No notices returned."
                else "Notices successfully fetched and stored."
            )

        } catch (e: Exception) {
            val msg = "Exception during fetch: ${e.message}"
            InAppLogger.e(msg)
            FetchResult.Failure(msg)
        }
    }
}

