package com.snb.inspect.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "MdSystemNotes")
data class MdSystemNoteLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cloudId: Int? = null,
    val systemId: Int, // Local system ID
    val cloudSystemId: Int? = null,
    val addedBy: Int,
    val addedDate: String?,
    val noteText: String?,
    val noteType: String?,
    val isImportant: Boolean = false,
    val isDeleted: Boolean = false,
    val editedBy: Int? = null,
    val editedDate: String? = null,
    val deletedBy: Int? = null,
    val deletedDate: String? = null,
    var isSynced: Boolean = false
)

data class MdSystemNoteCloud(
    @SerializedName("id", alternate = ["Id"]) val id: Int? = null,
    @SerializedName("systemId", alternate = ["SystemId"]) val systemId: Int,
    @SerializedName("addedBy", alternate = ["AddedBy"]) val addedBy: Int,
    @SerializedName("addedDate", alternate = ["AddedDate"]) val addedDate: String?,
    @SerializedName("noteText", alternate = ["NoteText"]) val noteText: String?,
    @SerializedName("noteType", alternate = ["NoteType"]) val noteType: String?,
    @SerializedName("isImportant", alternate = ["IsImportant"]) val isImportant: Boolean,
    @SerializedName("isDeleted", alternate = ["IsDeleted"]) val isDeleted: Boolean,
    @SerializedName("editedBy", alternate = ["EditedBy"]) val editedBy: Int?,
    @SerializedName("editedDate", alternate = ["EditedDate"]) val editedDate: String?,
    @SerializedName("deletedBy", alternate = ["DeletedBy"]) val deletedBy: Int?,
    @SerializedName("deletedDate", alternate = ["DeletedDate"]) val deletedDate: String?
)
