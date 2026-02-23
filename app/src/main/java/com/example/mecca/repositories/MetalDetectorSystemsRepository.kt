package com.example.mecca.repositories

import android.content.Context
import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.FetchResult
import com.example.mecca.network.isNetworkAvailable
import com.example.mecca.dataClasses.MdSystemCloud
import com.example.mecca.dataClasses.MdSystemLocal
import com.example.mecca.dataClasses.MetalDetectorWithFullDetails
import com.example.mecca.util.InAppLogger
import com.example.mecca.util.SerialCheckResult
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class MetalDetectorSystemsRepository(private val apiService: ApiService, private val db: AppDatabase) {

    // Function to update the isSynced status of a metal detector
    suspend fun updateSyncStatus(tempId: Int, isSynced: Boolean, newCloudId: Int): FetchResult {
        try {
            db.mdSystemDAO().updateSyncStatus(tempId = tempId, isSynced = isSynced, newCloudId = newCloudId)
        } catch (e: Exception) {
            val errorMessage = "Error updating the isSynced field for tempId $tempId: ${e.message}"
            InAppLogger.e(errorMessage)
            return FetchResult.Failure(errorMessage)
        }
        return FetchResult.Success("Sync Status Updated")
    }

    // Function to fetch data from the API and store it in the database
    suspend fun fetchAndStoreMdSystems(): FetchResult {

        InAppLogger.d("Fetching MD systems...")

        try {
            val response = apiService.getMdSystems()
            InAppLogger.d("API call to fetch MD systems complete. response = $response")

            if (response.isSuccessful) {
                val apiMdSystems = response.body()
                //InAppLogger.d("API call successful. body = $apiMdSystems")

                if (apiMdSystems != null) {
                    try {
                        db.mdSystemDAO().deleteAllMdSystems()
                        InAppLogger.d("Cleared local MD systems database")
                    } catch (e: Exception) {
                        val errorMessage = "Error clearing MD systems database: ${e.message}"
                        InAppLogger.e(errorMessage)
                        return FetchResult.Failure(errorMessage)
                    }

                    val mdSystemsLocal = apiMdSystems.mapIndexed { _, apiMdSystems ->
                        MdSystemLocal(
                            modelId = apiMdSystems.modelId,
                            cloudId = apiMdSystems.id,
                            customerId = apiMdSystems.customerId,
                            serialNumber = apiMdSystems.serialNumber,
                            apertureWidth = apiMdSystems.apertureWidth,
                            apertureHeight = apiMdSystems.apertureHeight,
                            lastCalibration = apiMdSystems.lastCalibration,
                            addedDate = apiMdSystems.addedDate,
                            calibrationInterval = apiMdSystems.calibrationInterval,
                            systemTypeId = apiMdSystems.systemTypeId,
                            tempId = 0,
                            isSynced = true,
                            lastLocation = apiMdSystems.lastLocation
                        )
                    }

                    //InAppLogger.d("Systems to be inserted into the local database: $mdSystemsLocal")
                    db.mdSystemDAO().insertMdSystem(mdSystemsLocal)
                    InAppLogger.d("Systems successfully inserted into the local database.")

                    return FetchResult.Success("Metal Detector Sync Complete")
                } else {
                    val errorMessage = "No data found."
                    InAppLogger.e(errorMessage)
                    return FetchResult.Failure(errorMessage)
                }
            } else {
                val errorMessage = "Error: ${response.code()}, Message: ${response.message()}"
                InAppLogger.e(errorMessage)
                return FetchResult.Failure(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = "Exception occurred: ${e.message}"
            InAppLogger.e(errorMessage)
            return FetchResult.Failure(errorMessage)
        }
    }

    // Return MD systems using the cloud id
    suspend fun getMetalDetectorUsingCloudId(id: Int?): List<MetalDetectorWithFullDetails> {
        val result = db.mdSystemDAO()
            .getMetalDetectorsWithFullDetailsUsingCloudId(id)

        val summary = when {
            id == null -> "FAILED (cloudId=null)"
            result.isEmpty() -> "SUCCESS (0 records)"
            else -> "SUCCESS (${result.size} record(s))"
        }

        InAppLogger.d("Get MD by CloudId=$id → $summary")
        return result
    }

    // Return MD systems using the Local id
    suspend fun getMetalDetectorsWithFullDetailsUsingLocalId(id: Int?): List<MetalDetectorWithFullDetails> {
        val result = db.mdSystemDAO().getMetalDetectorsWithFullDetailsUsingLocalId(id)
        //InAppLogger.d("Get Metal Detectors With Full Details Using Local ID: Query Result: $result")
        return result
    }

    // Check if a serial number already exists in the local database
    suspend fun isSerialNumberExists(serialNumber: String): Boolean {
        return db.mdSystemDAO().getSystemBySerialNumber(serialNumber) != null
    }

    suspend fun checkSerialNumberStatus(
        context: Context,
        serialNumber: String
    ): SerialCheckResult {
        // 1) No network? Use local cache so engineers can still work
        if (!isNetworkAvailable(context)) {
            val localExists = isSerialNumberExists(serialNumber)
            return if (localExists) {
                SerialCheckResult.ExistsLocalOffline
            } else {
                SerialCheckResult.NotFoundLocalOffline
            }
        }

        // 2) Online: ask the cloud. If it flakes out, return Error, not "false".
        return try {
            val exists = apiService.checkSerialNumberExists(serialNumber)
            if (exists) SerialCheckResult.Exists else SerialCheckResult.NotFound
        } catch (e: Exception) {
            SerialCheckResult.Error(e.message)
        }
    }

    // Add a new metal detector to the local database (offline mode)
    suspend fun addMetalDetectorToLocalDb(
        customerID: Int,
        serialNumber: String,
        apertureWidth: Int,
        apertureHeight: Int,
        lastLocation: String,
        systemTypeId: Int,
        modelId: Int,
        calibrationInterval: Int
    ) {
        val today = LocalDateTime.now()
        val todayString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        val tempId: Int = Random.nextInt(10000000, 20000000)

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
            isSynced = false,
            lastLocation = lastLocation
        )
        db.mdSystemDAO().insertNewMdSystem(newMetalDetector)
        InAppLogger.d("Added new MD locally: $newMetalDetector")
    }

    // Add a new Metal Detector to the Cloud Database
    suspend fun addMetalDetectorToCloud(
        customerID: Int,
        serialNumber: String,
        apertureWidth: Int,
        apertureHeight: Int,
        systemTypeId: Int,
        modelId: Int?,
        calibrationInterval: Int,
        lastLocation: String
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
            lastCalibration = "",
            isSynced = true,
            lastLocation = lastLocation
        )

        try {
            val response = apiService.postMdSystem(newMetalDetector)
            InAppLogger.d("Calling API... Response: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val newCloudId = responseBody.id
                    InAppLogger.d("Data uploaded successfully, new cloud ID: $newCloudId")
                    return newCloudId
                }
            } else {
                InAppLogger.e("Error uploading data: ${response.errorBody()?.string()}")
                return null
            }
        } catch (e: Exception) {
            InAppLogger.e("API call failed: ${e.message}")
        }
        return null
    }

    suspend fun updateSystem(context: Context, cloudId: Int?, tempId: Int?, localId: Int?) {
        InAppLogger.d("updateSystem() called with cloudId=$cloudId, localId=$localId, tempId=$tempId")

        val system = when {
            cloudId != null -> db.mdSystemDAO().getSystemByCloudId(cloudId)
            localId != null -> db.mdSystemDAO().getSystemByLocalId(localId)
            tempId != null -> db.mdSystemDAO().getSystemByTempId(tempId)
            else -> null
        }

        InAppLogger.d("System retrieved: $system")


        if (system == null) {
            InAppLogger.e("System not found locally")
            return
        }

        InAppLogger.d("Checking network availability...")


        if (isNetworkAvailable(context) && cloudId != null) {
            InAppLogger.d("Network available, attempting cloud sync with cloudId=$cloudId")
            val systemCloud = MdSystemCloud(
                modelId = system.modelId,
                customerId = system.customerId,
                serialNumber = system.serialNumber,
                apertureWidth = system.apertureWidth,
                apertureHeight = system.apertureHeight,
                lastCalibration = system.lastCalibration,
                addedDate = system.addedDate,
                calibrationInterval = system.calibrationInterval,
                systemTypeId = system.systemTypeId,
                lastLocation = system.lastLocation
            )

            try {


                val response = apiService.updateMdSystem(cloudId, systemCloud)
                if (response.isSuccessful) {
                    db.mdSystemDAO().updateIsSynced(true, cloudId, localId)
                    InAppLogger.d("Cloud sync success for cloud ID=$cloudId")
                } else {
                    val body = response.errorBody()?.string()
                    InAppLogger.e("Cloud sync failed: ${response.code()} body = $body")
                    db.mdSystemDAO().updateIsSynced(false, cloudId, localId)

                }
            } catch (e: Exception) {
                InAppLogger.e("Exception during cloud sync: ${e.message}")
            }
        } else {
            db.mdSystemDAO().updateIsSynced(false, cloudId, localId)
            InAppLogger.d("📴 Offline: local update only, marked unsynced")
        }
    }
}
