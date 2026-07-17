package com.snb.inspect.repositories

import android.content.Context
import com.snb.inspect.ApiService
import com.snb.inspect.FetchResult
import com.snb.inspect.daos.CheckweigherCalibrationDAO
import com.snb.inspect.dataClasses.CheckweigherCalibrationLocal
import com.snb.inspect.network.isNetworkAvailable
import com.snb.inspect.util.CsvUploader
import com.snb.inspect.util.DataBackupManager
import com.snb.inspect.util.InAppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.text.Normalizer

class CheckweigherCalibrationRepository(private val calibrationDao: CheckweigherCalibrationDAO) {

    private val uploadMutex = Mutex()

    suspend fun insertNewCalibration(context: Context, calibration: CheckweigherCalibrationLocal) {
        calibrationDao.insertCalibration(calibration)
        DataBackupManager.backupCalibration(context, calibration, calibration.calibrationId, "CW")
    }

    suspend fun updateCalibration(context: Context, calibration: CheckweigherCalibrationLocal) {
        calibrationDao.updateCalibration(calibration)
        DataBackupManager.backupCalibration(context, calibration, calibration.calibrationId, "CW")
    }

    suspend fun getCalibrationById(id: String): CheckweigherCalibrationLocal? {
        return calibrationDao.getCalibrationById(id)
    }

    suspend fun uploadUnsyncedCalibrations(
        context: Context,
        apiService: ApiService,
        specificId: String? = null
    ): FetchResult = uploadMutex.withLock {
        InAppLogger.d("uploadUnsyncedCalibrations() (Checkweigher) called (specificId=$specificId)")

        if (!isNetworkAvailable(context)) {
            return@withLock FetchResult.Failure("Offline. Upload skipped.")
        }

        val pending = if (specificId != null) {
            val cal = calibrationDao.getCalibrationById(specificId)
            if (cal != null && !cal.isSynced && cal.endDate.isNotBlank()) {
                listOf(cal)
            } else {
                emptyList()
            }
        } else {
            calibrationDao.getUnsyncedCalibrations()
        }

        if (pending.isEmpty()) {
            return@withLock FetchResult.Success("No pending calibrations.")
        }

        var uploaded = 0
        val failed = mutableListOf<String>()

        for (cal in pending) {
            if (cal.cloudSystemId == 0) {
                failed += "${cal.calibrationId} (Waiting for machine sync)"
                continue
            }

            try {
                val csvFile = createCsvFile(context, cal.calibrationId)
                if (csvFile == null || !csvFile.exists()) {
                    failed += "${cal.calibrationId} (CSV generation failed)"
                    continue
                }

                val success = CsvUploader.uploadCsvFile(
                    csvFile = csvFile,
                    apiService = apiService,
                    fileName = cal.calibrationId
                )

                if (success) {
                    calibrationDao.markAsSynced(cal.calibrationId)
                    uploaded++
                    DataBackupManager.removeBackup(context, cal.calibrationId, "CW")
                } else {
                    failed += cal.calibrationId
                }
            } catch (e: Exception) {
                failed += "${cal.calibrationId} (Error: ${e.message})"
            }
        }

        if (failed.isEmpty()) FetchResult.Success("Uploaded $uploaded calibration(s).")
        else FetchResult.Failure("Uploaded $uploaded. Failed: ${failed.joinToString()}")
    }

    private fun normalizeForCsv(input: Any?): String {
        if (input == null) return ""
        var text = input.toString()
        text = text.replace("\uFEFF", "")
        text = text
            .replace("\u2013", "-")
            .replace("\u2014", "-")
            .replace("\u2018", "'")
            .replace("\u2019", "'")
            .replace("\u201C", "\"")
            .replace("\u201D", "\"")
            .replace("\u2026", "...")
        text = text
            .replace(";", ",")
            .replace("\n", " ")
            .replace("\r", "")
        text = Normalizer.normalize(text, Normalizer.Form.NFD)
        text = text.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
        return text.trim()
    }

    suspend fun createCsvFile(context: Context, calibrationId: String): File? = withContext(Dispatchers.IO) {
        val cal = calibrationDao.getCalibrationById(calibrationId) ?: return@withContext null
        val fileName = "checkweigher_calibration_$calibrationId.csv"
        val csvFile = File(context.filesDir, fileName)

        try {
            val rawData = listOf(
                cal.calibrationId,
                cal.mapVersion,
                cal.systemId,
                cal.tempSystemId,
                cal.cloudSystemId,
                cal.systemTypeId,
                cal.modelId,
                cal.serialNumber,
                cal.engineerId,
                cal.customerId,
                cal.startDate,
                cal.endDate,
                cal.newLocation,
                cal.lastLocation,
                cal.canPerformCalibration,
                cal.reasonForNotCalibrating,
                cal.loadcellType,
                cal.scaleInterval,
                cal.maxCapacity,
                cal.beltWidth,
                cal.weighConveyorLength,
                cal.rejectType,
                cal.printerDataCapture,
                cal.rejectMode,
                cal.beltCondition,
                cal.beltConditionComments,
                cal.safetyCircuitCondition,
                cal.safetyCircuitConditionComments,
                cal.guardCondition,
                cal.guardConditionComments,
                cal.vibrationCondition,
                cal.vibrationConditionComments,
                cal.weighTableObstruction,
                cal.weighTableObstructionComments,
                cal.productTransferCondition,
                cal.productTransferConditionComments,
                cal.machineStabilityCondition,
                cal.machineStabilityConditionComments,
                cal.systemChecklistEngineerNotes,
                cal.infeedSensorFitted,
                cal.infeedSensorDetail,
                cal.infeedSensorTestMethod,
                cal.infeedSensorTestMethodOther,
                cal.infeedSensorTestResult,
                cal.infeedSensorEngineerNotes,
                cal.infeedSensorLatched,
                cal.infeedSensorCR,
                cal.rejectConfirmSensorFitted,
                cal.rejectConfirmSensorDetail,
                cal.rejectConfirmSensorTestMethod,
                cal.rejectConfirmSensorTestMethodOther,
                cal.rejectConfirmSensorTestResult,
                cal.rejectConfirmSensorEngineerNotes,
                cal.rejectConfirmSensorLatched,
                cal.rejectConfirmSensorCR,
                cal.binFullSensorFitted,
                cal.binFullSensorDetail,
                cal.binFullSensorTestMethod,
                cal.binFullSensorTestMethodOther,
                cal.binFullSensorTestResult,
                cal.binFullSensorEngineerNotes,
                cal.binFullSensorLatched,
                cal.binFullSensorCR,
                cal.airPressureSensorFitted,
                cal.airPressureSensorDetail,
                cal.airPressureSensorTestMethod,
                cal.airPressureSensorTestMethodOther,
                cal.airPressureSensorTestResult,
                cal.airPressureSensorEngineerNotes,
                cal.airPressureSensorLatched,
                cal.airPressureSensorCR,
                cal.binDoorMonitorFitted,
                cal.binDoorMonitorDetail,
                cal.binDoorStatusAsFound,
                cal.binDoorUnlockedIndication,
                cal.binDoorOpenIndication,
                cal.binDoorTimeoutTimer,
                cal.binDoorTimeoutResult,
                cal.binDoorLatched,
                cal.binDoorCR,
                cal.binDoorEngineerNotes,
                cal.productDescription,
                cal.productLength,
                cal.productWidth,
                cal.productHeight,
                cal.grossWeight,
                cal.tareWeight,
                cal.productLibraryReference,
                cal.staticScaleMakeModel,
                cal.staticScaleCertRef,
                cal.staticScaleExpiryDate,
                cal.engineerTestWeightId,
                cal.nominalQuantityAsFound,
                cal.dynamicPassesAsFound,
                cal.staticScaleWeightAsFound,
                cal.checkweigherWeightAsFound,
                cal.offCentreLoadingTestResultAsFound,
                cal.repeatabilityTestResultAsFound,
                cal.adjustmentsNotes,
                cal.nominalQuantityAsLeft,
                cal.dynamicPassesAsLeft,
                cal.staticScaleWeightAsLeft,
                cal.checkweigherWeightAsLeft,
                cal.offCentreLoadingTestResultAsLeft,
                cal.repeatabilityTestResultAsLeft
            )

            val sanitizedData = rawData.map { normalizeForCsv(it) }
            csvFile.outputStream().use { out ->
                out.bufferedWriter(Charsets.UTF_8).use { writer ->
                    writer.write(sanitizedData.joinToString(";"))
                    writer.write("\r\n")
                }
            }
            csvFile
        } catch (e: Exception) {
            InAppLogger.e("CSV Creation Error (Checkweigher): ${e.message}")
            null
        }
    }
}
