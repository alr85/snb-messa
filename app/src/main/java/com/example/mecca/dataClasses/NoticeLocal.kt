package com.example.mecca.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey

// This data class represents the structure of the data returned by the API
data class NoticeCloud(
    val noticeId: Int?,
    val title: String?,
    val body: String?,
    val dateAdded: String?,
    val createdBy: String?,
    val isActive: Boolean?
)

@Entity(tableName = "notice")
data class NoticeLocal(

    @PrimaryKey
    val noticeId: Int,        // MUST be non-null
    val title: String,       // no null nonsense
    val body: String,
    val dateAdded: String?,  // optional is fine
    val createdBy: String?,
    val isActive: Boolean
)

