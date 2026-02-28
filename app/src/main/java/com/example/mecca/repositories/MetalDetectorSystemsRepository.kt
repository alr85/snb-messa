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
        InAppLogger.d("fetchAndStoreMdSystems() called")

        try {
            val response = apiService.getMdSystems()
            if (response.isSuccessful) {
                val apiMdSystems = response.body()

                if (apiMdSystems != null) {
                    val dao = db.mdSystemDAO()
                    
                    // 1) Get all local systems that are NOT synced yet (unsynced/no cloudId)
                    val unsyncedSystems = dao.getSystemsNeedingUpload()
                    InAppLogger.d("Preserving ${unsyncedSystems.size} unsynced systems.")

                    // 2) Delete ONLY synced systems from local DB
                    dao.deleteSyncedMdSystems() 
                    InAppLogger.d("Cleared synced MD systems from local database")

                    // 3) Map API data to local entities
                    val mdSystemsLocal = apiMdSystems.map { apiSystem ->
                        MdSystemLocal(
                            modelId = apiSystem.modelId,
                            cloudId = apiSystem.id,
                            customerId = apiSystem.customerId,
                            serialNumber = apiSystem.serialNumber,
                            apertureWidth = apiSystem.apertureWidth,
                            apertureHeight = apiSystem.apertureHeight,
                            lastCalibration = apiSystem.lastCalibration,
                            addedDate = apiSystem.addedDate,
                            calibrationInterval = apiSystem.calibrationInterval,
                            systemTypeId = apiSystem.systemTypeId,
                            tempId = 0,
                            isSynced = true,
                            lastLocation = apiSystem.lastLocation
                        )
                    }

                    // 4) Insert the fresh cloud data (synced)
                    dao.insertMdSystem(mdSystemsLocal)
                    InAppLogger.d("Cloud systems inserted into local database.")

                    return FetchResult.Success("Metal Detector Sync Complete")
                } else {
                    return FetchResult.Failure("No data found from server.")
                }
            } else {
                return FetchResult.Failure("Error: ${response.code()}, Message: ${response.message()}")
            }
        } catch (e: Exception) {
            InAppLogger.e("Exception occurred: ${e.message}")
            return FetchResult.Failure("Sync failed: ${e.message}")
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

        val systemCloud = MdSystemCloud(
            modelId = modelId,
            customerId = customerID,
            serialNumber = serialNumber,
            apertureWidth = apertureWidth,
            apertureHeight = apertureHeight,
            systemTypeId = systemTypeId,
            addedDate = todayString,
            calibrationInterval = calibrationInterval,
            lastCalibration = "",
            lastLocation = lastLocation
        )

        try {
            val response = apiService.postMdSystem(systemCloud)
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

        if (system == null) {
            InAppLogger.e("System not found locally")
            return
        }

        if (isNetworkAvailable(context) && cloudId != null) {
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
                    InAppLogger.e("Cloud sync failed: ${response.code()}")
                    db.mdSystemDAO().updateIsSynced(false, cloudId, localId)
                }
            } catch (e: Exception) {
                InAppLogger.e("Exception during cloud sync: ${e.message}")
            }
        } else {
            db.mdSystemDAO().updateIsSynced(false, cloudId, localId)
            InAppLogger.d("📴 Offline: local update only")
        }
    }

    suspend fun uploadUnsyncedSystems(context: Context): FetchResult {
        InAppLogger.d("uploadUnsyncedSystems() called")

        val dao = db.mdSystemDAO()
        val pending = dao.getSystemsNeedingUpload()

        if (pending.isEmpty()) return FetchResult.Success("No unsynced systems to upload.")

        if (!isNetworkAvailable(context)) {
            return FetchResult.Failure("Offline. Sync aborted.")
        }

        var uploaded = 0
        val failed = mutableListOf<String>()

        for (sys in pending) {
            val serial = sys.serialNumber
            val cloudId = sys.cloudId

            if (cloudId != null && cloudId != 0) {
                // UPDATE existing
                try {
                    val systemCloud = MdSystemCloud(
                        modelId = sys.modelId,
                        customerId = sys.customerId,
                        serialNumber = sys.serialNumber,
                        apertureWidth = sys.apertureWidth,
                        apertureHeight = sys.apertureHeight,
                        lastCalibration = sys.lastCalibration,
                        addedDate = sys.addedDate,
                        calibrationInterval = sys.calibrationInterval,
                        systemTypeId = sys.systemTypeId,
                        lastLocation = sys.lastLocation
                    )
                    val resp = apiService.updateMdSystem(cloudId, systemCloud)
                    if (resp.isSuccessful) {
                        dao.updateIsSynced(true, cloudId, sys.id)
                        uploaded++
                    } else {
                        failed += "$serial (update failed)"
                    }
                } catch (e: Exception) {
                    failed += "$serial (update error: ${e.message})"
                }
            } else {
                // POST new
                try {
                    val exists = apiService.checkSerialNumberExists(serial)
                    if (exists) {
                        failed += "$serial (already in cloud)"
                        continue
                    }

                    val systemCloud = MdSystemCloud(
                        modelId = sys.modelId,
                        customerId = sys.customerId,
                        serialNumber = sys.serialNumber,
                        apertureWidth = sys.apertureWidth,
                        apertureHeight = sys.apertureHeight,
                        lastCalibration = sys.lastCalibration,
                        addedDate = sys.addedDate,
                        calibrationInterval = sys.calibrationInterval,
                        systemTypeId = sys.systemTypeId,
                        lastLocation = sys.lastLocation
                    )

                    val postResp = apiService.postMdSystem(systemCloud)
                    if (postResp.isSuccessful) {
                        val newCloudId = postResp.body()?.id
                        if (newCloudId != null && sys.tempId != null) {
                            dao.updateSyncStatus(isSynced = true, tempId = sys.tempId, newCloudId = newCloudId)
                            uploaded++
                        } else {
                            failed += "$serial (no ID returned)"
                        }
                    } else {
                        failed += "$serial (post failed: ${postResp.code()})"
                    }
                } catch (e: Exception) {
                    failed += "$serial (post error: ${e.message})"
                }
            }
        }

        return if (failed.isEmpty()) FetchResult.Success("Uploaded $uploaded system(s).")
        else FetchResult.Failure("Uploaded $uploaded. Failed: ${failed.joinToString()}")
    }
}
