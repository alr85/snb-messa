package com.example.mecca.Repositories

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

    suspend fun syncUsers() {
        try {
            // Fetch users from cloud
            val cloudUsers = apiService.getUsers()

            // Map cloud users to local entities (if necessary)
            val localUsers = cloudUsers.map { cloudUser ->
                UserEntity(
                    id = 0,
                    meaId = cloudUser.meaId,
                    fusionId = cloudUser.fusionId,
                    username = cloudUser.username,
                    isActive = cloudUser.isActive,
                    lastSynced = Date()
                )
            }

            // Update the local database
            userDao.clearAllUsers()
            userDao.insertUsers(localUsers)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error gracefully, maybe show a message to the user
        }
    }
}

