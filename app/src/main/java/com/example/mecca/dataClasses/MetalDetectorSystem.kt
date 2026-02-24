package com.example.mecca.dataClasses

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

// This data class represents the structure of the data returned by the API
data class MdSystem(
    val id: Int,
    val modelId: Int,
    val customerId: Int,
    val serialNumber: String,
    val apertureWidth: Int,
    val apertureHeight: Int,
    val lastCalibration: String,
    val addedDate: String,
    val calibrationInterval: Int,
    val systemTypeId: Int,
    val lastLocation: String
)

// This is for posting new systems to the cloud
data class MdSystemCloud(
    val modelId: Int?,
    val customerId: Int,
    val serialNumber: String,
    val apertureWidth: Int,
    val apertureHeight: Int,
    val lastCalibration: String,
    val addedDate: String,
    val calibrationInterval: Int,
    val systemTypeId: Int,
    val lastLocation: String
)



// This is the entity class that corresponds to the local Room database table
// What it does: Represents a table in the local Room database. Each field in the class corresponds to a column in the table.
// How it interacts: This class is used to define the structure of data that will be stored in the database.

@Entity(tableName = "MdSystems")
data class MdSystemLocal(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    var cloudId: Int? = null,
    val tempId: Int? = null,
    val modelId: Int?,
    val customerId: Int,
    val serialNumber: String,
    val apertureWidth: Int,
    val apertureHeight: Int,
    val lastCalibration: String,
    val addedDate: String,
    val calibrationInterval: Int,
    val systemTypeId: Int,
    var isSynced: Boolean,
    var lastLocation: String = ""

)

@Parcelize
data class MetalDetectorWithFullDetails(
    val id: Int,                       // From MdSystems
    val modelId: Int?,                  // From MdSystems
    val cloudId: Int?,                   // From MdSystems
    val customerId: Int,               // From MdSystems
    val serialNumber: String,          // From MdSystems
    val apertureWidth: Int,            // From MdSystems
    val apertureHeight: Int,           // From MdSystems
    val lastCalibration: String,       // From MdSystems
    val addedDate: String,             // From MdSystems
    val calibrationInterval: Int,      // From MdSystems
    val systemTypeId: Int,               //FromMdSystems
    val modelDescription: String,  // From MdModels
    val systemType: String,  // From systemTypes
    val tempId: Int,
    val isSynced: Boolean,
    val customerName: String,
    val fusionID: Int,
    val lastLocation: String
): Parcelable


