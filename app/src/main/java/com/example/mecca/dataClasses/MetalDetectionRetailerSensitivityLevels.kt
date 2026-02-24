package com.example.mecca.dataClasses

// ConveyorRetailerSensitivitiesEntity.kt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
@Entity(tableName = "ConveyorRetailerSensitivities")
data class ConveyorRetailerSensitivitiesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @SerializedName("RangeDesc")
    val rangeDesc: String,

    @SerializedName("MinProductHeightMM")
    @ColumnInfo(name = "MinProductHeightMM")
    val minProductHeightMM: Double,

    @SerializedName("MaxProductHeightMM")
    @ColumnInfo(name = "MaxProductHeightMM")
    val maxProductHeightMM: Double,

    @SerializedName("FerrousTargetMM")
    @ColumnInfo(name = "FerrousTargetMM")
    override val ferrousTargetMM: Double,

    @SerializedName("FerrousMaxMM")
    @ColumnInfo(name = "FerrousMaxMM")
    override val ferrousMaxMM: Double,

    @SerializedName("NonFerrousTargetMM")
    @ColumnInfo(name = "NonFerrousTargetMM")
    override val nonFerrousTargetMM: Double,

    @SerializedName("NonFerrousMaxMM")
    @ColumnInfo(name = "NonFerrousMaxMM")
    override val nonFerrousMaxMM: Double,

    @SerializedName("Stainless316TargetMM")
    @ColumnInfo(name = "Stainless316TargetMM")
    override val stainless316TargetMM: Double,

    @SerializedName("Stainless316MaxMM")
    @ColumnInfo(name = "Stainless316MaxMM")
    override val stainless316MaxMM: Double,

    @SerializedName("XrayFerrousTargetMM")
    @ColumnInfo(name = "XrayStainless316MaxMM")
    val xrayStainless316MaxMM: Double
) : RetailerSensitivity

// FreefallThroatRetailerSensitivitiesEntity.kt
@Entity(tableName = "FreefallThroatRetailerSensitivities")
data class FreefallThroatRetailerSensitivitiesEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @SerializedName("RangeDesc")
    @ColumnInfo(name = "RangeDesc")
    val rangeDesc: String,

    @SerializedName("MinThroatApertureMM")
    @ColumnInfo(name = "MinThroatApertureMM")
    val minThroatApertureMM: Double,

    @SerializedName("MaxThroatApertureMM")
    @ColumnInfo(name = "MaxThroatApertureMM")
    val maxThroatApertureMM: Double,

    @SerializedName("FerrousTargetMM")
    @ColumnInfo(name = "FerrousTargetMM")
    override val ferrousTargetMM: Double,

    @SerializedName("FerrousMaxMM")
    @ColumnInfo(name = "FerrousMaxMM")
    override val ferrousMaxMM: Double,

    @SerializedName("NonFerrousTargetMM")
    @ColumnInfo(name = "NonFerrousTargetMM")
    override val nonFerrousTargetMM: Double,

    @SerializedName("NonFerrousMaxMM")
    @ColumnInfo(name = "NonFerrousMaxMM")
    override val nonFerrousMaxMM: Double,

    @SerializedName("Stainless316TargetMM")
    @ColumnInfo(name = "Stainless316TargetMM")
    override val stainless316TargetMM: Double,

    @SerializedName("Stainless316MaxMM")
    @ColumnInfo(name = "Stainless316MaxMM")
    override val stainless316MaxMM: Double

) : RetailerSensitivity

// PipelineDetectionRetailerSensitivities.kt
@Entity(tableName = "PipelineRetailerSensitivities")
data class PipelineRetailerSensitivitiesEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @SerializedName("RangeDesc")
    @ColumnInfo(name = "RangeDesc")
    val rangeDesc: String,

    @SerializedName("MinInternalPipeMM")
    @ColumnInfo(name = "MinInternalPipeMM")
    val minInternalPipeMM: Double,

    @SerializedName("MaxInternalPipeMM")
    @ColumnInfo(name = "MaxInternalPipeMM")
    val maxInternalPipeMM: Double,

    @SerializedName("FerrousTargetMM")
    @ColumnInfo(name = "FerrousTargetMM")
    override val ferrousTargetMM: Double,

    @SerializedName("FerrousMaxMM")
    @ColumnInfo(name = "FerrousMaxMM")
    override val ferrousMaxMM: Double,

    @SerializedName("NonFerrousTargetMM")
    @ColumnInfo(name = "NonFerrousTargetMM")
    override val nonFerrousTargetMM: Double,

    @SerializedName("NonFerrousMaxMM")
    @ColumnInfo(name = "NonFerrousMaxMM")
    override val nonFerrousMaxMM: Double,

    @SerializedName("Stainless316TargetMM")
    @ColumnInfo(name = "Stainless316TargetMM")
    override val stainless316TargetMM: Double,

    @SerializedName("Stainless316MaxMM")
    @ColumnInfo(name = "Stainless316MaxMM")
    override val stainless316MaxMM: Double

) : RetailerSensitivity