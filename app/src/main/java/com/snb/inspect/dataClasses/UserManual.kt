package com.snb.inspect.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// Data class for API response - Updated to match PascalCase from your API JSON
data class UserManual(
    @SerializedName("Id") val id: Int,
    @SerializedName("Description") val description: String,
    @SerializedName("Url") val url: String,
    @SerializedName("Notes") val notes: String?,
    @SerializedName("CreatedAt") val createdAt: String?,
    @SerializedName("SystemType") val systemType: String?
)

// Entity for local Room database (keeping camelCase for local Room table)
@Entity(tableName = "UserManuals")
data class UserManualLocal(
    @PrimaryKey val id: Int,
    val description: String,
    val url: String,
    val notes: String?,
    val systemType: String?
)
