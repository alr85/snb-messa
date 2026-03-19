package com.snb.inspect.repositories

import androidx.room.withTransaction
import com.snb.inspect.ApiService
import com.snb.inspect.AppDatabase
import com.snb.inspect.FetchResult
import com.snb.inspect.dataClasses.mappers.toLocal
import com.snb.inspect.util.InAppLogger

class NoticeRepository(
    private val apiService: ApiService,
    private val db: AppDatabase
) {

    fun observeActiveNotices() = db.noticesDAO().getActiveNotices()

    suspend fun fetchAndStoreNotices(force: Boolean = false): FetchResult {

        InAppLogger.d("Refreshing notices (full sync)...")

        return try {


            val response = apiService.getNotices()

            if (!response.isSuccessful) {
                val msg = "Could not refresh notices (${response.code()})"
                InAppLogger.e(msg)
                return FetchResult.Failure(msg)
            }

            val apiNotices = response.body().orEmpty()
            val noticeLocals = apiNotices.mapNotNull { it.toLocal() }

            val oldCount = db.noticesDAO().getNoticeCount()
            val newCount = noticeLocals.size

            db.withTransaction {
                db.noticesDAO().deleteAllNotices()
                db.noticesDAO().upsertNotices(noticeLocals)

            }


            InAppLogger.d("Notice sync complete. Old=$oldCount New=$newCount")

            val message = when {
                newCount == 0 -> "You're all caught up 👍"
                newCount > oldCount -> "${newCount - oldCount} new notice${if (newCount - oldCount > 1) "s" else ""}"
                else -> "Notices refreshed"
            }

            FetchResult.Success(message)

        } catch (e: Exception) {

            val msg = "Notice refresh failed: ${e.message}"
            InAppLogger.e(msg)

            FetchResult.Failure("Could not refresh notices. Check connection.")
        }
    }
}


