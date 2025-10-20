package com.example.mecca.DAOs


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mecca.dataClasses.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT meaId FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserIdByUsername(username: String): Int?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int


}
