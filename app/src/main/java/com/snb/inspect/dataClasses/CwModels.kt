package com.snb.inspect.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// This data class represents the structure of the data returned by the API
data class CwModel(
    @SerializedName("model_id", alternate = ["Model_id"]) val model_id: Int?,
    @SerializedName("model_description", alternate = ["Model_description"]) val model_description: String?,
    @SerializedName("manualUrl", alternate = ["ManualUrl"]) val manualUrl: String?
)

// This is the entity class that corresponds to the local Room database table
@Entity(tableName = "CwModels")
data class CwModelsLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val meaId: Int,
    val modelDescription: String,
    val manualUrl: String?
)
