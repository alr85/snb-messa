package com.example.mecca.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey

// This data class represents the structure of the data returned by the API and needs to match up
data class SystemType(
    val id: Int?,
    val systemType: String?

)

@Entity(tableName = "systemTypes")
data class SystemTypeLocal(
    @PrimaryKey val id: Int,        // This should be 'id' since you're joining on 'systemTypes.id'
    val systemType: String          // This should match your SQL column
)

