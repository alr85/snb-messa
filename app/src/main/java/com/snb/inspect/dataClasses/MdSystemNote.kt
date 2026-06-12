package com.snb.inspect.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val id: Int? = null,
    val systemId: Int,
    val addedBy: Int,
    val addedDate: String?,
    val noteText: String?,
    val noteType: String?,
    val isImportant: Boolean,
    val isDeleted: Boolean,
    val editedBy: Int?,
    val editedDate: String?,
    val deletedBy: Int?,
    val deletedDate: String?
)
