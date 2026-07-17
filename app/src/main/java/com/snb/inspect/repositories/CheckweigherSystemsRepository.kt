package com.snb.inspect.repositories

import android.content.Context
import androidx.room.withTransaction
import com.snb.inspect.ApiService
import com.snb.inspect.AppDatabase
import com.snb.inspect.FetchResult
import com.snb.inspect.dataClasses.CheckweigherWithFullDetails
import com.snb.inspect.dataClasses.CwModelsLocal
import com.snb.inspect.dataClasses.CwSystemCloud
import com.snb.inspect.dataClasses.CwSystemLocal
import com.snb.inspect.network.isNetworkAvailable
import com.snb.inspect.util.InAppLogger
import com.snb.inspect.util.SerialCheckResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class CheckweigherSystemsRepository(private val apiService: ApiService, private val db: AppDatabase) {

    fun observeCheckweighersByCustomerId(customerId: Int): Flow<List<CheckweigherWithFullDetails>> {
        return db.cwSystemsDAO().getCheckweighersByCustomerId(customerId)
    }

    suspend fun fetchAndStoreCwModels(): FetchResult {
        try {
            val response = apiService.getCwModels()
            if (response.isSuccessful) {
                val apiModels = response.body() ?: emptyList()
                val localModels = apiModels.map { 
                    CwModelsLocal(
                        meaId = it.model_id ?: 0,
                        modelDescription = it.model_description ?: "Unknown",
                        manualUrl = it.manualUrl
                    )
                }
                db.withTransaction {
                    db.cwModelsDAO().deleteAll()
                    db.cwModelsDAO().insertAll(localModels)
                }
                return FetchResult.Success("Checkweigher Models Synced")
            }
            return FetchResult.Failure("Error fetching CW models: ${response.code()}")
        } catch (e: Exception) {
            return FetchResult.Failure("Exception fetching CW models: ${e.message}")
        }
    }

    suspend fun fetchAndStoreCwSystems(): FetchResult {
        InAppLogger.d("fetchAndStoreCwSystems() called")
        try {
            val response = apiService.getCwSystems()
            if (response.isSuccessful) {
                val apiSystems = response.body() ?: emptyList()
                val dao = db.cwSystemsDAO()
                
                db.withTransaction {
                    val allLocal = dao.getAllSystemsFlow().first()
                    val localMap = allLocal.associateBy { it.serialNumber }

                    for (apiSys in apiSystems) {
                        val localMatch = localMap[apiSys.serialNumber]

                        // Link calibrations if we're discovering a Cloud ID for a local system
                        if (localMatch != null && localMatch.cloudId != apiSys.id) {
                            db.checkweigherCalibrationDAO().updateCalibrationWithCloudIdBySystemId(localMatch.id!!, apiSys.id)
                        }

                        val entity = CwSystemLocal(
                            id = localMatch?.id,
                            cloudId = apiSys.id,
                            tempId = localMatch?.tempId ?: 0,
                            modelId = apiSys.modelId,
                            customerId = apiSys.customerId,
                            serialNumber = apiSys.serialNumber,
                            lastCalibration = apiSys.lastCalibration,
                            addedDate = apiSys.addedDate,
                            calibrationInterval = apiSys.calibrationInterval,
                            systemTypeId = apiSys.systemTypeId,
                            isSynced = true,
                            lastLocation = apiSys.lastLocation
                        )
                        dao.insertSystem(entity)
                    }
                }
                return FetchResult.Success("Checkweigher Systems Synced")
            }
            return FetchResult.Failure("Error fetching CW systems: ${response.code()}")
        } catch (e: Exception) {
            InAppLogger.e("CW System Sync Exception: ${e.message}")
            return FetchResult.Failure("Exception fetching CW systems: ${e.message}")
        }
    }

    suspend fun addCheckweigherToLocalDb(
        customerId: Int,
        serialNumber: String,
        lastLocation: String,
        systemTypeId: Int,
        modelId: Int,
        calibrationInterval: Int
    ) {
        val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        val tempId = Random.nextInt(20000000, 30000000)

        val newSystem = CwSystemLocal(
            customerId = customerId,
            serialNumber = serialNumber,
            systemTypeId = systemTypeId,
            modelId = modelId,
            addedDate = today,
            calibrationInterval = calibrationInterval,
            lastCalibration = "-",
            cloudId = 0,
            tempId = tempId,
            isSynced = false,
            lastLocation = lastLocation
        )
        db.cwSystemsDAO().insertSystem(newSystem)
    }

    suspend fun addCheckweigherToCloud(
        customerId: Int,
        serialNumber: String,
        systemTypeId: Int,
        modelId: Int?,
        calibrationInterval: Int,
        lastLocation: String
    ): Int? {
        val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val systemCloud = CwSystemCloud(
            modelId = modelId,
            customerId = customerId,
            serialNumber = serialNumber,
            systemTypeId = systemTypeId,
            addedDate = today,
            calibrationInterval = calibrationInterval,
            lastCalibration = "",
            lastLocation = lastLocation
        )

        try {
            val response = apiService.postCwSystem(systemCloud)
            if (response.isSuccessful) {
                return response.body()?.id
            }
        } catch (e: Exception) {
            InAppLogger.e("CW Cloud Post Error: ${e.message}")
        }
        return null
    }

    suspend fun updateSystem(context: Context, cloudId: Int?, tempId: Int?, localId: Int?) {
        InAppLogger.d("CW updateSystem() called with cloudId=$cloudId, localId=$localId, tempId=$tempId")

        val system = when {
            localId != null -> db.cwSystemsDAO().getSystemById(localId)
            else -> null
        }

        if (system == null) {
            InAppLogger.e("CW System not found locally")
            return
        }

        if (isNetworkAvailable(context) && cloudId != null) {
            val systemCloud = CwSystemCloud(
                modelId = system.modelId,
                customerId = system.customerId,
                serialNumber = system.serialNumber,
                lastCalibration = system.lastCalibration,
                addedDate = system.addedDate,
                calibrationInterval = system.calibrationInterval,
                systemTypeId = system.systemTypeId,
                lastLocation = system.lastLocation
            )

            try {
                val response = apiService.updateCwSystem(cloudId, systemCloud)
                if (response.isSuccessful) {
                    db.withTransaction {
                        db.cwSystemsDAO().updateIsSynced(true, cloudId, localId)
                    }
                    InAppLogger.d("CW Cloud sync success for cloud ID=$cloudId")
                } else {
                    InAppLogger.e("CW Cloud sync failed: ${response.code()}")
                    db.cwSystemsDAO().updateIsSynced(false, cloudId, localId)
                }
            } catch (e: Exception) {
                InAppLogger.e("Exception during CW cloud sync: ${e.message}")
            }
        } else {
            db.cwSystemsDAO().updateIsSynced(false, cloudId, localId)
            InAppLogger.d("📴 CW Offline: local update only")
        }
    }

    suspend fun uploadUnsyncedSystems(context: Context): FetchResult {
        InAppLogger.d("CW uploadUnsyncedSystems() called")

        val dao = db.cwSystemsDAO()
        val pending = dao.getUnsyncedSystems()

        if (pending.isEmpty()) return FetchResult.Success("No unsynced systems to upload.")

        if (!isNetworkAvailable(context)) {
            return FetchResult.Failure("Offline. Sync aborted.")
        }

        var uploaded = 0
        val failed = mutableListOf<String>()

        for (sys in pending) {
            val serial = sys.serialNumber
            val cloudId = sys.cloudId

            if (cloudId == null || cloudId == 0) {
                // POST new
                try {
                    val systemCloud = CwSystemCloud(
                        modelId = sys.modelId,
                        customerId = sys.customerId,
                        serialNumber = sys.serialNumber,
                        systemTypeId = sys.systemTypeId,
                        addedDate = sys.addedDate,
                        calibrationInterval = sys.calibrationInterval,
                        lastCalibration = sys.lastCalibration,
                        lastLocation = sys.lastLocation
                    )

                    val postResp = apiService.postCwSystem(systemCloud)
                    if (postResp.isSuccessful) {
                        val newCloudId = postResp.body()?.id
                        if (newCloudId != null) {
                            db.withTransaction {
                                dao.updateCloudId(sys.id ?: 0, newCloudId)
                                // Link any local calibrations to this new Cloud ID
                                db.checkweigherCalibrationDAO().updateCalibrationWithCloudIdBySystemId(sys.id!!, newCloudId)
                            }
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
            } else {
                // UPDATE existing
                try {
                    val systemCloud = CwSystemCloud(
                        modelId = sys.modelId,
                        customerId = sys.customerId,
                        serialNumber = sys.serialNumber,
                        systemTypeId = sys.systemTypeId,
                        addedDate = sys.addedDate,
                        calibrationInterval = sys.calibrationInterval,
                        lastCalibration = sys.lastCalibration,
                        lastLocation = sys.lastLocation
                    )
                    val resp = apiService.updateCwSystem(cloudId, systemCloud)
                    if (resp.isSuccessful) {
                        dao.updateIsSynced(true, cloudId, sys.id)
                        uploaded++
                    } else {
                        failed += "$serial (update failed: ${resp.code()})"
                    }
                } catch (e: Exception) {
                    failed += "$serial (update error: ${e.message})"
                }
            }
        }

        return if (failed.isEmpty()) FetchResult.Success("Uploaded $uploaded CW system(s).")
        else FetchResult.Failure("Uploaded $uploaded. Failed: ${failed.joinToString()}")
    }
    
    suspend fun getAllModels(): List<CwModelsLocal> {
        return db.cwModelsDAO().getAllModels()
    }

    suspend fun getCheckweigherWithFullDetailsUsingLocalId(id: Int): CheckweigherWithFullDetails? {
        return db.cwSystemsDAO().getCheckweigherWithFullDetailsUsingLocalId(id)
    }

    suspend fun getCwModelDetails(modelId: Int): CwModelsLocal? {
        return db.cwModelsDAO().getAllModels().find { it.meaId == modelId }
    }

    suspend fun checkSerialNumberStatus(
        context: Context,
        serialNumber: String
    ): SerialCheckResult<CheckweigherWithFullDetails> {
        val normalizedInput = normalizeSerial(serialNumber)

        // 1) Online check
        if (isNetworkAvailable(context)) {
            try {
                // For CW, we don't have a specific checkSerialNumberExists API yet, 
                // but we can check the local DB which is synced from the cloud.
                // Or we can fetch all from API and check. 
                // For now, let's stick to local DB which should have all cloud data after sync.
                val systemDetails = db.cwSystemsDAO().getCheckweigherWithFullDetailsBySerialNumber(serialNumber)
                if (systemDetails != null) {
                    return SerialCheckResult.Exists(systemDetails)
                }

                // Fuzzy check
                val allSystems = db.cwSystemsDAO().getAllCheckweighersWithFullDetails()
                val fuzzyMatch = allSystems.find { normalizeSerial(it.serialNumber) == normalizedInput }
                if (fuzzyMatch != null) {
                    return SerialCheckResult.FuzzyMatch(fuzzyMatch)
                }

                return SerialCheckResult.NotFound
            } catch (e: Exception) {
                return SerialCheckResult.Error(e.message)
            }
        }

        // 2) Offline check
        val allLocalSystems = db.cwSystemsDAO().getAllCheckweighersWithFullDetails()

        // Exact local match
        val exactLocal = allLocalSystems.find { it.serialNumber == serialNumber }
        if (exactLocal != null) {
            return SerialCheckResult.ExistsLocalOffline(exactLocal)
        }

        // Fuzzy local match
        val fuzzyLocal = allLocalSystems.find { normalizeSerial(it.serialNumber) == normalizedInput }
        if (fuzzyLocal != null) {
            return SerialCheckResult.FuzzyMatch(fuzzyLocal)
        }

        return SerialCheckResult.NotFoundLocalOffline
    }

    suspend fun checkSerialNumberExists(serialNumber: String): Boolean {
        return db.cwSystemsDAO().getSystemBySerialNumber(serialNumber) != null
    }

    private fun normalizeSerial(serial: String): String {
        return serial.replace(Regex("[^A-Za-z0-9]"), "").uppercase()
    }

    suspend fun fetchAndStoreAll() {
        fetchAndStoreCwModels()
        fetchAndStoreCwSystems()
    }
}
