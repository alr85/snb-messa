package com.example.mecca.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey

// This data class represents the structure of the data returned by the API
data class Customer(
    val customerID: Int?,
    val customerName: String?,
    val customerPostcode: String?,
    val fusionID: Int?,
    val dateAdded: String?, // If the API returns a string, it's fine for now.
    val longitude: Double?,
    val latitude: Double?,
    val customerCityTown: String?
)

// This is the entity class that corresponds to the local Room database table
@Entity(tableName = "customer")
data class CustomerLocal(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String?,
    val postcode: String?,
    val fusionID: Int?,
    val dateAdded: String?,  // Adjust this to a more appropriate type like Date if needed
    val lat: Double?,
    val lon: Double?,
    val customerCityTown: String?
)
