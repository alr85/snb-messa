package com.example.mecca.dataClasses

// ConveyorRetailerSensitivitiesEntity.kt
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ConveyorRetailerSensitivities")
data class ConveyorRetailerSensitivitiesEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val RangeDesc: String,
    val MinProductHeightMM: Double,
    val MaxProductHeightMM: Double,
    val FerrousTargetMM: Double,
    val FerrousMaxMM: Double,
    val NonFerrousTargetMM: Double,
    val NonFerrousMaxMM: Double,
    val Stainless316TargetMM: Double,
    val Stainless316MaxMM: Double,
    val XrayStainless316MaxMM: Double
)

// FreefallThroatRetailerSensitivitiesEntity.kt
@Entity(tableName = "FreefallThroatRetailerSensitivities")
data class FreefallThroatRetailerSensitivitiesEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val RangeDesc: String,
    val MinThroatApertureMM: Double,
    val MaxThroatApertureMM: Double,
    val FerrousTargetMM: Double,
    val FerrousMaxMM: Double,
    val NonFerrousTargetMM: Double,
    val NonFerrousMaxMM: Double,
    val Stainless316TargetMM: Double,
    val Stainless316MaxMM: Double
)

// PipelineDetectionRetailerSensitivities.kt
@Entity(tableName = "PipelineRetailerSensitivities")
data class PipelineRetailerSensitivitiesEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val RangeDesc: String,
    val MinInternalPipeMM: Double,
    val MaxInternalPipeMM: Double,
    val FerrousTargetMM: Double,
    val FerrousMaxMM: Double,
    val NonFerrousTargetMM: Double,
    val NonFerrousMaxMM: Double,
    val Stainless316TargetMM: Double,
    val Stainless316MaxMM: Double
)
