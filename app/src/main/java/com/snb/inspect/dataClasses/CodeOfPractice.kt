package com.snb.inspect.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// Data class for API response
data class CodeOfPractice(
    @SerializedName("Id") val id: Int,
    @SerializedName("Title") val title: String,
    @SerializedName("Url") val url: String,
    @SerializedName("Description") val description: String?,
    @SerializedName("Category") val category: String?
)

// Entity for local Room database
@Entity(tableName = "CodesOfPractice")
data class CodeOfPracticeLocal(
    @PrimaryKey val id: Int,
    val title: String,
    val url: String,
    val description: String?,
    val category: String?
)
