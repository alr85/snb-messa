package com.snb.inspect.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// The API response model matching your SQL Server design
data class ApiMeasuringEquipment(
    @SerializedName("id", alternate = ["Id", "ID"]) val Id: Int,
    @SerializedName("deviceTypeID", alternate = ["DeviceTypeID", "deviceTypeId", "DeviceTypeId"]) val DeviceTypeID: Int,
    @SerializedName("manufacturer", alternate = ["Manufacturer"]) val Manufacturer: String,
    @SerializedName("model", alternate = ["Model"]) val Model: String,
    @SerializedName("serialNumber", alternate = ["SerialNumber"]) val SerialNumber: String,
    @SerializedName("calibrationID", alternate = ["CalibrationID", "calibrationId", "CalibrationId"]) val CalibrationID: Int?,
    @SerializedName("lastCalibrationDate", alternate = ["LastCalibrationDate"]) val LastCalibrationDate: String?,
    @SerializedName("calibrationDueDate", alternate = ["CalibrationDueDate"]) val CalibrationDueDate: String?,
    @SerializedName("certificateNumber", alternate = ["CertificateNumber"]) val CertificateNumber: String?,
    @SerializedName("assetTag", alternate = ["AssetTag"]) val AssetTag: String?,
    @SerializedName("description", alternate = ["Description"]) val Description: String?,
    @SerializedName("location", alternate = ["Location"]) val Location: String?,
    @SerializedName("serviceStatus", alternate = ["ServiceStatus"]) val ServiceStatus: String?,
    @SerializedName("notes", alternate = ["Notes"]) val Notes: String?,
    @SerializedName("isActive", alternate = ["IsActive", "Isactive"]) val IsActive: Boolean,
    @SerializedName("createdAt", alternate = ["CreatedAt"]) val CreatedAt: String,
    @SerializedName("updatedAt", alternate = ["UpdatedAt"]) val UpdatedAt: String?,
    @SerializedName("employeeID", alternate = ["EmployeeID", "employeeId", "EmployeeId"]) val EmployeeID: Int?,
    @SerializedName("createdBy", alternate = ["CreatedBy"]) val CreatedBy: Int?,
    @SerializedName("externallyCalibrated", alternate = ["ExternallyCalibrated"]) val ExternallyCalibrated: Boolean
)

// The Room Database model
@Entity(tableName = "measuring_equipment")

data class MeasuringEquipmentLocal(
    @PrimaryKey val id: Int,
    val deviceTypeID: Int,
    val manufacturer: String,
    val model: String,
    val serialNumber: String,
    val calibrationID: Int?,
    val lastCalibrationDate: String?,
    val calibrationDueDate: String?,
    val certificateNumber: String?,
    val assetTag: String?,
    val description: String?,
    val location: String?,
    val serviceStatus: String?,
    val notes: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String?,
    val employeeID: Int?,
    val createdBy: Int?,
    val externallyCalibrated: Boolean
)

// Mapper to convert API (PascalCase from SQL) to Local (camelCase for Kotlin)
fun ApiMeasuringEquipment.toLocal() = MeasuringEquipmentLocal(
    id = Id,
    deviceTypeID = DeviceTypeID,
    manufacturer = Manufacturer,
    model = Model,
    serialNumber = SerialNumber,
    calibrationID = CalibrationID,
    lastCalibrationDate = LastCalibrationDate,
    calibrationDueDate = CalibrationDueDate,
    certificateNumber = CertificateNumber,
    assetTag = AssetTag,
    description = Description,
    location = Location,
    serviceStatus = ServiceStatus,
    notes = Notes,
    isActive = IsActive,
    createdAt = CreatedAt,
    updatedAt = UpdatedAt,
    employeeID = EmployeeID,
    createdBy = CreatedBy,
    externallyCalibrated = ExternallyCalibrated
)
