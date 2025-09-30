package com.example.mecca.DataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey

// This data class represents the structure of the data returned by the API
data class MdModel(
    val model_id: Int,              // Match the API key
    val model_description: String,   // Match the API key
    val detectionSetting1: String,
    val detectionSetting2: String,
    val detectionSetting3: String,
    val detectionSetting4: String,
    val detectionSetting5: String,
    val detectionSetting6: String,
    val detectionSetting7: String,
    val detectionSetting8: String,
)

// This is the entity class that corresponds to the local Room database table
@Entity(tableName = "MdModels")
data class MdModelsLocal(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val meaId: Int,                 // Adjusted to match your previous naming convention
    val modelDescription: String,    // Adjusted to match your previous naming convention
    val detectionSetting1: String,
    val detectionSetting2: String,
    val detectionSetting3: String,
    val detectionSetting4: String,
    val detectionSetting5: String,
    val detectionSetting6: String,
    val detectionSetting7: String,
    val detectionSetting8: String,
)


