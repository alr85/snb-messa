package com.example.mecca.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// This data class represents the structure of the data returned by the API
data class NoticeCloud(

    @SerializedName("NoticeId")
    val noticeId: Int?,

    @SerializedName("Title")
    val title: String?,

    @SerializedName("Body")
    val body: String?,

    @SerializedName("DateAdded")
    val dateAdded: String?,

    @SerializedName("CreatedBy")
    val createdBy: String?,

    @SerializedName("IsActive")
    val isActive: Boolean?,

    @SerializedName("IsPinned")
    val isPinned: Boolean?
)
@Entity(tableName = "notice")

data class NoticeLocal(


    @PrimaryKey
    val noticeId: Int,        // MUST be non-null
    val title: String,       // no null nonsense
    val body: String,
    val dateAdded: String?,  // optional is fine
    val createdBy: String?,
    val isActive: Boolean,
    val isPinned: Boolean
)

