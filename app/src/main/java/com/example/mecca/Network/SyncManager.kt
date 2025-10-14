package com.example.mecca.Network

import android.util.Log
import com.example.mecca.ApiService
import com.example.mecca.DAOs.MetalDetectorSystemsDAO

class SyncManager(
    private val mdSystemsDao: MetalDetectorSystemsDAO,
    private val apiService: ApiService
) {

    suspend fun syncUnsyncedMdSystems() {
        val unsyncedMdSystems = mdSystemsDao.getUnsyncedMdSystems()

        if (unsyncedMdSystems.isNotEmpty()) {
            for (mdSystem in unsyncedMdSystems) {
                try {
                    // Send to the cloud
                    val response = apiService.postMdSystem(mdSystem)
                    if (response.isSuccessful) {
                        val cloudId = response.body()?.id // Get the machine's cloud ID

                        // Update the local database with the cloud ID
                        if (cloudId != null) {
                            mdSystem.cloudId = cloudId
                        }
                        mdSystem.isSynced = true
                        mdSystemsDao.updateMdSystem(mdSystem)

                        // Now sync the calibration data associated with this machine
                        //syncCalibrationData(mdSystem.tempId, cloudId)
                    }
                } catch (e: Exception) {
                    Log.e("SyncError", "Failed to sync machine ${mdSystem.serialNumber}: ${e.message}")
                }
            }
        } else {
            Log.d("Sync", "No unsynced machines found.")
        }
    }

//    private suspend fun syncCalibrationData(tempId: String, cloudId: Int) {
//        val unsyncedCalibrations = calibrationDao.getCalibrationsByTempId(tempId)
//
//        if (unsyncedCalibrations.isNotEmpty()) {
//            for (calibration in unsyncedCalibrations) {
//                try {
//                    // Update calibration data with cloud machine ID
//                    calibration.machineCloudId = cloudId
//                    val response = apiService.uploadCalibration(calibration)
//                    if (response.isSuccessful) {
//                        // Mark calibration as synced
//                        calibration.isSynced = true
//                        calibrationDao.updateCalibration(calibration)
//                    }
//                } catch (e: Exception) {
//                    Log.e("SyncError", "Failed to sync calibration ${calibration.id}: ${e.message}")
//                }
//            }
//        }
//    }
}
