package com.example.mecca.DataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val meaId: Int, // From the server
    val fusionId: Int,
    val username: String,
    val isActive: Boolean = true, // Default to active
    val lastSynced: Date = Date() // Last sync timestamp
)

data class CloudUser(
    val meaId: Int,
    val fusionId: Int,
    val username: String,
    val isActive: Boolean
)