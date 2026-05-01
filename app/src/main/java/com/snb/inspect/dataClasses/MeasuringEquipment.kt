package com.snb.inspect.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey


// The API response model matching your SQL Server design
data class ApiMeasuringEquipment(
    val Id: Int,
    val DeviceTypeID: Int,
    val Manufacturer: String,
    val Model: String,
    val SerialNumber: String,
    val CalibrationID: Int?,
    val LastCalibrationDate: String?,
    val CalibrationDueDate: String?,
    val CertificateNumber: String?,
    val AssetTag: String?,
    val Description: String?,
    val Location: String?,
    val ServiceStatus: String?,
    val Notes: String?,
    val IsActive: Boolean,
    val CreatedAt: String,
    val UpdatedAt: String?,
    val EmployeeID: Int?,
    val CreatedBy: Int?,
    val ExternallyCalibrated: Boolean
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
