package com.example.mecca.repositories

import android.util.Log
import com.example.mecca.ApiService
import com.example.mecca.DAOs.UserDao
import com.example.mecca.dataClasses.UserEntity
import com.example.mecca.util.InAppLogger
import java.net.SocketTimeoutException
import java.util.Date

import kotlinx.coroutines.delay
import retrofit2.Response
import java.io.IOException


class UserRepository(
    private val userDao: UserDao,
    private val apiService: ApiService
) {

    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun fetchUserIdByUsername(username: String): Int? {
        return userDao.getUserIdByUsername(username)
    }

    suspend fun hasLocalUsers(): Boolean {
        return userDao.getUserCount() > 0
    }


    suspend fun syncUsers() {
        InAppLogger.d("Fetching users from API (with retry)...")

        val cloudUsers = retryWithBackoff {
            apiService.getUsers()
        }.also {
            InAppLogger.d("API call complete. success=${it.isSuccessful} code=${it.code()}")
        }

        if (!cloudUsers.isSuccessful) {
            val body = cloudUsers.errorBody()?.string()

            InAppLogger.e("Error fetching users: HTTP ${cloudUsers.code()} ${cloudUsers.message()} body=$body")
            throw IllegalStateException("Users API failed: ${cloudUsers.code()} ${cloudUsers.message()}")
        }

        val users = cloudUsers.body() ?: emptyList()
        InAppLogger.d("Fetched ${users.size} users from cloud")

        val localUsers = users.map { u ->
            UserEntity(
                id = 0,
                meaId = u.meaId,
                fusionId = u.fusionId,
                username = u.username,
                isActive = u.isActive,
                lastSynced = Date()
            )
        }

        if (localUsers.isEmpty()) {
            InAppLogger.d("No users fetched; skipping clear to avoid wiping cache")
            return
        }

        Log.d("MESSA-DEBUG", "Clearing local users table...")
        InAppLogger.d("Clearing local users table...")
        userDao.clearAllUsers()
        InAppLogger.d("Inserting ${localUsers.size} users...")
        userDao.insertUsers(localUsers)
        InAppLogger.d("Insert OK.")

    }

}

private suspend fun <T> retryWithBackoff(
    attempts: Int = 5,
    initialDelayMs: Long = 600,
    maxDelayMs: Long = 8000,
    block: suspend () -> Response<T>
): Response<T> {
    var delayMs = initialDelayMs
    var lastException: Throwable? = null

    repeat(attempts - 1) { attemptIndex ->
        try {
            val resp = block()

            // If Azure is waking up, you may see these transient gateway errors.
            if (resp.code() !in listOf(502, 503, 504)) return resp

            //Log.w("MESSA-DEBUG", "Transient HTTP ${resp.code()} on attempt ${attemptIndex + 1}. Retrying in ${delayMs}ms")
            InAppLogger.d("Transient HTTP ${resp.code()} on attempt ${attemptIndex + 1}. Retrying in ${delayMs}ms")
        } catch (t: Throwable) {
            lastException = t
            val retriable = t is IOException || t is SocketTimeoutException
            if (!retriable) throw t

            Log.w("MESSA-DEBUG", "Network exception on attempt ${attemptIndex + 1}: ${t.javaClass.simpleName} ${t.message}. Retrying in ${delayMs}ms")
            InAppLogger.d("Network exception on attempt ${attemptIndex + 1}: ${t.javaClass.simpleName} ${t.message}. Retrying in ${delayMs}ms")
        }

        delay(delayMs)
        delayMs = (delayMs * 2).coerceAtMost(maxDelayMs)
    }

    // Final attempt: let it throw or return whatever it returns
    try {
        return block()
    } catch (t: Throwable) {
        InAppLogger.d("Final attempt failed: ${t.javaClass.simpleName} ${t.message}")
        throw lastException ?: t

    }
}

