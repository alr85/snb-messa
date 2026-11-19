package com.snb.inspect.dataClasses

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class CwSystem(
    @SerializedName("id", alternate = ["Id"]) val id: Int,
    @SerializedName("modelId", alternate = ["ModelId"]) val modelId: Int,
    @SerializedName("customerId", alternate = ["CustomerId"]) val customerId: Int,
    @SerializedName("serialNumber", alternate = ["SerialNumber"]) val serialNumber: String,
    @SerializedName("lastCalibration", alternate = ["LastCalibration"]) val lastCalibration: String?,
    @SerializedName("addedDate", alternate = ["AddedDate"]) val addedDate: String,
    @SerializedName("calibrationInterval", alternate = ["CalibrationInterval"]) val calibrationInterval: Int,
    @SerializedName("systemTypeId", alternate = ["SystemTypeId"]) val systemTypeId: Int,
    @SerializedName("lastLocation", alternate = ["LastLocation"]) val lastLocation: String,
    @SerializedName("maxCapacity", alternate = ["MaxCapacity"]) val maxCapacity: String? = "",
    @SerializedName("minCapacity", alternate = ["MinCapacity"]) val minCapacity: String? = "",
    @SerializedName("displayResolution", alternate = ["DisplayResolution"]) val displayResolution: String? = "",
    @SerializedName("accuracyClass", alternate = ["AccuracyClass"]) val accuracyClass: String? = ""
)

data class CwSystemCloud(
    @SerializedName("modelId", alternate = ["ModelId"]) val modelId: Int?,
    @SerializedName("customerId", alternate = ["CustomerId"]) val customerId: Int,
    @SerializedName("serialNumber", alternate = ["SerialNumber"]) val serialNumber: String,
    @SerializedName("lastCalibration", alternate = ["LastCalibration"]) val lastCalibration: String?,
    @SerializedName("addedDate", alternate = ["AddedDate"]) val addedDate: String,
    @SerializedName("calibrationInterval", alternate = ["CalibrationInterval"]) val calibrationInterval: Int,
    @SerializedName("systemTypeId", alternate = ["SystemTypeId"]) val systemTypeId: Int,
    @SerializedName("lastLocation", alternate = ["LastLocation"]) val lastLocation: String,
    @SerializedName("maxCapacity", alternate = ["MaxCapacity"]) val maxCapacity: String = "",
    @SerializedName("minCapacity", alternate = ["MinCapacity"]) val minCapacity: String = "",
    @SerializedName("displayResolution", alternate = ["DisplayResolution"]) val displayResolution: String = "",
    @SerializedName("accuracyClass", alternate = ["AccuracyClass"]) val accuracyClass: String = ""
)

@Entity(
    tableName = "CwSystems",
    indices = [Index(value = ["serialNumber"], unique = true)]
)
data class CwSystemLocal(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    var cloudId: Int? = null,
    val tempId: Int? = null,
    val modelId: Int?,
    val customerId: Int,
    val serialNumber: String,
    var lastCalibration: String?,
    val addedDate: String,
    val calibrationInterval: Int,
    val systemTypeId: Int,
    var isSynced: Boolean,
    var lastLocation: String = "",
    var maxCapacity: String = "",
    var minCapacity: String = "",
    var displayResolution: String = "",
    var accuracyClass: String = ""
)

@Parcelize
data class CheckweigherWithFullDetails(
    val id: Int,
    val modelId: Int?,
    val cloudId: Int?,
    val customerId: Int,
    val serialNumber: String,
    val lastCalibration: String?,
    val addedDate: String,
    val calibrationInterval: Int,
    val systemTypeId: Int,
    val modelDescription: String,
    val systemType: String,
    val tempId: Int,
    val isSynced: Boolean,
    val customerName: String,
    val lastLocation: String,
    val maxCapacity: String = "",
    val minCapacity: String = "",
    val displayResolution: String = "",
    val accuracyClass: String = ""
): Parcelable
