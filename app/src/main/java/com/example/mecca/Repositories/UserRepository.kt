package com.example.mecca.Repositories

import android.util.Log
import com.example.mecca.ApiService
import com.example.mecca.DAOs.UserDao
import com.example.mecca.dataClasses.UserEntity
import java.util.Date

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

            // Fetch users from cloud
            Log.d("MESSA-DEBUG", "Fetching users from API...")
            val cloudUsers = apiService.getUsers()
                .also { Log.d("MESSA-DEBUG", "API call complete. success = ${it.isSuccessful} code = ${it.code()}") }

            if (!cloudUsers.isSuccessful) {
                val body = cloudUsers.errorBody()?.string()
                Log.e("MESSA-DEBUG", "Error fetching users from API: HTTP ${cloudUsers.code()} ${cloudUsers.message()} body=$body")
                throw IllegalStateException("Users API failed: ${cloudUsers.code()} ${cloudUsers.message()}")
            }

            val users = cloudUsers.body() ?: emptyList()
            Log.d("MESSA-DEBUG", "Fetched ${users.size} users from cloud")

            // Map cloud users to local entities (if necessary)
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
                Log.d("MESSA-DEBUG", "No users fetched; skipping clear to avoid wiping cache")
                return
            }

            // Update the local database
            Log.d("MESSA-DEBUG", "Clearing local users table...")
            userDao.clearAllUsers()
            Log.d("MESSA-DEBUG", "Inserting ${localUsers.size} users...")
            userDao.insertUsers(localUsers)
            Log.d("MESSA-DEBUG", "Insert OK.")

    }
}

