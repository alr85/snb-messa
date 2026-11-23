package com.snb.inspect.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val meaId: Int, // From the server
    val fusionId: Int,
    val username: String,
    val pinHash: String,
    val isActive: Boolean = true, // Default to active
    val lastSynced: Date = Date() // Last sync timestamp
)

data class CloudUser(
    @SerializedName("meaId", alternate = ["MeaId", "MEAID", "MeaID", "Id", "ID"]) val meaId: Int,
    @SerializedName("fusionId", alternate = ["FusionId", "FusionID", "fusionID", "EmployeeId", "EmployeeID", "employeeId", "employeeID"]) val fusionId: Int,
    @SerializedName("username", alternate = ["Username", "UserName"]) val username: String,
    @SerializedName("isActive", alternate = ["IsActive", "Isactive"]) val isActive: Boolean,
    @SerializedName("pinHash", alternate = ["PinHash"]) val pinHash: String
)