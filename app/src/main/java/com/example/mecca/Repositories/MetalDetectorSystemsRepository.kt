package com.example.mecca.Repositories

import android.util.Log
import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.DataClasses.MdSystemLocal
import com.example.mecca.DataClasses.MetalDetectorWithFullDetails
import com.example.mecca.FetchResult
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.random.Random


class MetalDetectorSystemsRepository(private val apiService: ApiService, private val db: AppDatabase) {


    //Function to update the isSynced status of a metal detector
    suspend fun updateSyncStatus(tempId: Int, isSynced: Boolean, newCloudId: Int): FetchResult {
        try{
            db.mdSystemDAO().updateSyncStatus(tempId = tempId, isSynced = isSynced, newCloudId = newCloudId)
        } catch (e: Exception) {
            val errorMessage = "Error updating the isSynced field for tempId $tempId: ${e.message}"
            Log.e("DEBUG", errorMessage)
            return FetchResult.Failure(errorMessage)
        }
        return FetchResult.Success("Sync Status Updated")
    }


    

    // Function to fetch data from the API and store it in the database
    suspend fun fetchAndStoreMdSystems(): FetchResult {

        Log.d("DEBUG", "Fetching MD systems from API...")

        return try {
            // Make the API call
            val response = apiService.getMdSystems()
            Log.d("DEBUG", "getMdSystems = $response")
            // Check if the response was successful
            if (response.isSuccessful) {
                val apiMdSystems = response.body()
                Log.d("DEBUG", "$apiMdSystems")
                if (apiMdSystems != null) {

                    // Clear the existing records before inserting new ones
                    try {
                        db.mdSystemDAO().deleteAllMdSystems()
                        Log.d("DEBUG", "Cleared local MD systems database")
                    } catch (e: Exception) {
                        val errorMessage = "Error clearing MD systems database: ${e.message}"
                        Log.e("DEBUG", errorMessage)
                        return FetchResult.Failure(errorMessage)
                    }

                    // Map API data to local entities
                    val mdSystemsLocal = apiMdSystems.mapIndexed { index, apiMdSystems ->

//                        Log.d(
//                            "DEBUG",
//                            "Mapping MD Systems $index: ID=${apiMdSystems.id}, Model ID=${apiMdSystems.modelId ?: "Null"}"
//                        )

                        // Return MdSystemLocal for insertion
                        MdSystemLocal(
                            //id = 0,  // Auto-generate ID in the Room database
                            modelId = apiMdSystems.modelId ?:0,
                            cloudId = apiMdSystems.id ?:0,
                            customerId = apiMdSystems.customerId?:0,
                            serialNumber = apiMdSystems.serialNumber ?:"Unknown",
                            apertureWidth = apiMdSystems.apertureWidth ?:0,
                            apertureHeight = apiMdSystems.apertureHeight ?:0,
                            lastCalibration = apiMdSystems.lastCalibration,
                            addedDate = apiMdSystems.addedDate,
                            calibrationInterval = apiMdSystems.calibrationInterval,
                            systemTypeId = apiMdSystems.systemTypeId,
                            tempId = 0,
                            isSynced = true
                        )
                    }
                    Log.d("DEBUG", "Systems to be inserted inserted into the local database: $mdSystemsLocal.")
                    // Insert the mapped data into the local database
                    db.mdSystemDAO().insertMdSystem(mdSystemsLocal)
                    Log.d("DEBUG", "Systems successfully inserted into the local database.")

                    return FetchResult.Success("Metal Detector Sync Complete")

                } else {
                    // Handle the case where body is null
                    val errorMessage = "No data found."
                    Log.e("API", errorMessage)
                    return FetchResult.Failure(errorMessage)
                }
            } else {
                // Handle HTTP errors (4xx, 5xx)
                val errorMessage = "Error: ${response.code()}, Message: ${response.message()}"
                Log.e("API", errorMessage)
                return FetchResult.Failure(errorMessage)
            }
        } catch (e: Exception) {
            // Handle any exceptions that occurred during the API call
            val errorMessage = "Exception occurred: ${e.message}"
            Log.e("API", errorMessage)
            return FetchResult.Failure(errorMessage)
        }
    }
    
    
    // Function to get all systems from the local database
    suspend fun getMdSystemsFromDb(): List<MdSystemLocal> {
        return db.mdSystemDAO().getAllMdSystems()
    }


    //function to return MD systems using the cloud id
    suspend fun getMetalDetectorUsingCloudId(id: Int?): List<MetalDetectorWithFullDetails> {
        val result = db.mdSystemDAO().getMetalDetectorsWithFullDetailsUsingCloudId(id)
        Log.d("MetalDetector", "Query Result: $result")
        return result
    }

    //function to return MD systems using the Local id
    suspend fun getMetalDetectorsWithFullDetailsUsingLocalId(id: Int?): List<MetalDetectorWithFullDetails> {
        val result = db.mdSystemDAO().getMetalDetectorsWithFullDetailsUsingLocalId(id)
        Log.d("MetalDetector", "Query Result: $result")
        return result
    }

    // function to check if a serial number already exists in the local database
    suspend fun isSerialNumberExists(serialNumber: String): Boolean {
        return db.mdSystemDAO().getSystemBySerialNumber(serialNumber) != null
}

// function to check if a serial number already exists in the cloud database
    suspend fun isSerialNumberExistsInCloud(serialNumber: String): Boolean {
    Log.d("NewMD", "Checking serial number: $serialNumber")
        return try {
            // Call the API to check if the serial number exists in the cloud
            return apiService.checkSerialNumberExists(serialNumber)
        } catch (e: Exception) {
            // Handle any errors with the network or API call
            Log.e("NewMD", "Error checking serial number in cloud: ${e.message}")
            false
        }
    }

    //function to add a new metal detector to the local database if there is no network connection
    suspend fun addMetalDetectorToLocalDb(
        customerID: Int,
        serialNumber: String,
        apertureWidth: Int,
        apertureHeight: Int,
        systemTypeId: Int,
        modelId: Int,
        calibrationInterval: Int
    ) {
        // Add code to save data locally in the Room database
        val today = LocalDateTime.now()  // Using LocalDateTime for date and time
        val todayString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))  // ISO 8601 format
        val tempId: Int = Random.nextInt(10000000, 20000000) // Generates a random integer between 1 and 9999

        val newMetalDetector = MdSystemLocal(
            customerId = customerID,
            serialNumber = serialNumber,
            apertureWidth = apertureWidth,
            apertureHeight = apertureHeight,
            systemTypeId = systemTypeId,
            modelId = modelId,
            addedDate = todayString,
            calibrationInterval = calibrationInterval,
            lastCalibration = "-",
            cloudId = 0,
            tempId = tempId,
            isSynced = false
        )
        db.mdSystemDAO().insertNewMdSystem(newMetalDetector)

    }


    // function to add a new Metal Detector to the Cloud Database
    suspend fun addMetalDetectorToCloud(
        customerID: Int,
        serialNumber: String,
        apertureWidth: Int,
        apertureHeight: Int,
        systemTypeId: Int,
        modelId: Int,
        calibrationInterval: Int
    ): Int? {
        val today = LocalDateTime.now()
        val todayString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val newMetalDetector = MdSystemLocal(
            customerId = customerID,
            serialNumber = serialNumber,
            apertureWidth = apertureWidth,
            apertureHeight = apertureHeight,
            systemTypeId = systemTypeId,
            modelId = modelId,
            addedDate = todayString,
            calibrationInterval = calibrationInterval,
            lastCalibration = todayString,
            isSynced = true
        )

        try {
            val response = apiService.postMdSystem(newMetalDetector)
            Log.d("NewMD", "API Response: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val newCloudId = responseBody.id // Extract cloudId from the response


                    Log.d("NewMD", "Data uploaded successfully, new cloud ID: $newCloudId")
                    return newCloudId
                }
            } else {
                Log.e("NewMD", "Error uploading data: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("NewMD", "API call failed: ${e.message}")
        }

        return null
    }






}


