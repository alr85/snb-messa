/**
 * ---------------------------------------------------------------
 *  Calibration Repository (Database Write Operations)
 * ---------------------------------------------------------------
 */


package com.example.mecca.repositories

import android.content.Context
import com.example.mecca.ApiService
import com.example.mecca.FetchResult
import com.example.mecca.daos.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.calibrationLogic.metalDetectorConveyor.*
import com.example.mecca.dataClasses.MetalDetectorConveyorCalibrationLocal
import com.example.mecca.network.isNetworkAvailable
import com.example.mecca.util.CsvUploader
import com.example.mecca.util.InAppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException

class MetalDetectorConveyorCalibrationRepository(private val calibrationDao: MetalDetectorConveyorCalibrationDAO) {

    suspend fun insertNewCalibration(insert: NewCalibrationInsert) {
        val entity = MetalDetectorConveyorCalibrationLocal(
            calibrationId = insert.calibrationId,
            mapVersion = insert.mapVersion,
            systemId = insert.systemId,
            tempSystemId = insert.tempSystemId,
            cloudSystemId = insert.cloudSystemId,
            modelId = insert.modelId,
            serialNumber = insert.serialNumber,
            engineerId = insert.engineerId,
            customerId = insert.customerId,
            startDate = insert.startDate,
            detectionSetting1label = insert.detectionSetting1label,
            detectionSetting2label = insert.detectionSetting2label,
            detectionSetting3label = insert.detectionSetting3label,
            detectionSetting4label = insert.detectionSetting4label,
            detectionSetting5label = insert.detectionSetting5label,
            detectionSetting6label = insert.detectionSetting6label,
            detectionSetting7label = insert.detectionSetting7label,
            detectionSetting8label = insert.detectionSetting8label,
            lastLocation = insert.lastLocation,
            systemTypeId = insert.systemTypeId
        )

        calibrationDao.insertOrUpdateCalibration(entity)
    }

    /**
     * BULLETPROOF BACKGROUND UPLOAD:
     * Identifies all calibrations that are finished but not yet synced, and attempts to upload them.
     */
    suspend fun uploadUnsyncedCalibrations(context: Context, apiService: ApiService): FetchResult {
        InAppLogger.d("uploadUnsyncedCalibrations() called")

        if (!isNetworkAvailable(context)) {
            return FetchResult.Failure("Offline. Background upload skipped.")
        }

        val pending = calibrationDao.getAllPendingCalibrations().first()
        if (pending.isEmpty()) return FetchResult.Success("No pending calibrations.")

        var uploaded = 0
        val failed = mutableListOf<String>()

        for (cal in pending) {
            // CRITICAL: We don't upload if we don't have a Cloud ID yet.
            // Machine sync must happen first!
            if (cal.cloudSystemId == 0) {
                InAppLogger.d("Skipping cal ${cal.calibrationId}: No cloudSystemId yet.")
                failed += "${cal.calibrationId} (Waiting for machine sync)"
                continue
            }

            try {
                // 1. RE-GENERATE the CSV to ensure it has the correct CloudSystemId
                val csvFile = createCsvFile(context, cal.calibrationId)
                
                if (csvFile == null || !csvFile.exists()) {
                    failed += "${cal.calibrationId} (CSV generation failed)"
                    continue
                }

                // 2. UPLOAD the fresh file
                val success = CsvUploader.uploadCsvFile(
                    csvFile = csvFile,
                    apiService = apiService,
                    fileName = cal.calibrationId
                )

                if (success) {
                    calibrationDao.updateIsSynced(cal.calibrationId, true)
                    uploaded++
                } else {
                    failed += cal.calibrationId
                }
            } catch (e: Exception) {
                failed += "${cal.calibrationId} (Error: ${e.message})"
            }
        }

        return if (failed.isEmpty()) FetchResult.Success("Uploaded $uploaded calibration(s).")
        else FetchResult.Failure("Uploaded $uploaded. Failed: ${failed.joinToString()}")
    }

    suspend fun createCsvFile(context: Context, calibrationId: String): File? = withContext(Dispatchers.IO) {
        val row = calibrationDao.getCalibrationForCsvConversion(calibrationId)
        val fileName = "calibration_data_$calibrationId.csv"
        val csvFile = File(context.filesDir, fileName)

        try {
            val data = listOf(
                row.calibrationId, row.mapVersion, row.systemId, row.tempSystemId, row.cloudSystemId,
                row.systemTypeId, row.modelId, row.engineerId, row.customerId, row.startDate, row.endDate,
                row.isSynced, row.newLocation, row.canPerformCalibration, row.reasonForNotCalibrating,
                row.pvRequired, row.desiredCop, row.startCalibrationNotes, row.productDescription,
                row.productLibraryReference, row.productLibraryNumber, row.productLength, row.productWidth,
                row.productHeight, row.productDetailsEngineerNotes, row.detectionSettingAsFound1,
                row.detectionSettingAsFound2, row.detectionSettingAsFound3, row.detectionSettingAsFound4,
                row.detectionSettingAsFound5, row.detectionSettingAsFound6, row.detectionSettingAsFound7,
                row.detectionSettingAsFound8, row.detectionSettingAsFoundEngineerNotes,
                row.sensitivityRequirementFerrous, row.sensitivityRequirementNonFerrous,
                row.sensitivityRequirementStainless, row.sensitivityRequirementEngineerNotes,
                row.sensitivityAccessRestriction, row.sensitivityAsFoundFerrous,
                row.sensitivityAsFoundFerrousPeakSignal, row.sensitivityAsFoundNonFerrous,
                row.sensitivityAsFoundNonFerrousPeakSignal, row.sensitivityAsFoundStainless,
                row.sensitivityAsFoundStainlessPeakSignal, row.productPeakSignalAsFound,
                row.sensitivityAsFoundEngineerNotes, row.sensitivityAsLeftFerrous,
                row.sampleCertificateNumberFerrous, row.detectRejectFerrousLeading,
                row.detectRejectFerrousLeadingPeakSignal, row.detectRejectFerrousMiddle,
                row.detectRejectFerrousMiddlePeakSignal, row.detectRejectFerrousTrailing,
                row.detectRejectFerrousTrailingPeakSignal, row.ferrousTestEngineerNotes,
                row.sensitivityAsLeftNonFerrous, row.sampleCertificateNumberNonFerrous,
                row.detectRejectNonFerrousLeading, row.detectRejectNonFerrousLeadingPeakSignal,
                row.detectRejectNonFerrousMiddle, row.detectRejectNonFerrousMiddlePeakSignal,
                row.detectRejectNonFerrousTrailing, row.detectRejectNonFerrousTrailingPeakSignal,
                row.nonFerrousTestEngineerNotes, row.sensitivityAsLeftStainless,
                row.sampleCertificateNumberStainless, row.detectRejectStainlessLeading,
                row.detectRejectStainlessLeadingPeakSignal, row.detectRejectStainlessMiddle,
                row.detectRejectStainlessMiddlePeakSignal, row.detectRejectStainlessTrailing,
                row.detectRejectStainlessTrailingPeakSignal, row.stainlessTestEngineerNotes,
                row.detectRejectLargeMetal, row.sampleCertificateNumberLargeMetal,
                row.largeMetalTestEngineerNotes, row.detectionSettingAsLeft1,
                row.detectionSettingAsLeft2, row.detectionSettingAsLeft3, row.detectionSettingAsLeft4,
                row.detectionSettingAsLeft5, row.detectionSettingAsLeft6, row.detectionSettingAsLeft7,
                row.detectionSettingAsLeft8, row.detectionSettingAsLeftEngineerNotes,
                row.rejectSynchronisationSetting, row.rejectSynchronisationDetail,
                row.rejectDelaySetting, row.rejectDelayUnits, row.rejectDurationSetting,
                row.rejectDurationUnits, row.rejectConfirmWindowSetting, row.rejectConfirmWindowUnits,
                row.rejectSettingsEngineerNotes, row.infeedBeltHeight, row.outfeedBeltHeight,
                row.conveyorLength, row.conveyorHanding, row.beltSpeed, row.rejectDevice,
                row.rejectDeviceOther, row.conveyorDetailsEngineerNotes, row.beltCondition,
                row.beltConditionComments, row.guardCondition, row.guardConditionComments,
                row.safetyCircuitCondition, row.safetyCircuitConditionComments, row.linerCondition,
                row.linerConditionComments, row.cablesCondition, row.cablesConditionComments,
                row.screwsCondition, row.screwsConditionComments, row.systemChecklistEngineerNotes,
                row.indicator6colour, row.indicator6label, row.indicator5colour, row.indicator5label,
                row.indicator4colour, row.indicator4label, row.indicator3colour, row.indicator3label,
                row.indicator2colour, row.indicator2label, row.indicator1colour, row.indicator1label,
                row.indicatorsEngineerNotes, row.infeedSensorFitted, row.infeedSensorDetail,
                row.infeedSensorTestMethod, row.infeedSensorTestMethodOther, row.infeedSensorTestResult,
                row.infeedSensorEngineerNotes, row.infeedSensorLatched, row.infeedSensorCR,
                row.rejectConfirmSensorFitted, row.rejectConfirmSensorDetail,
                row.rejectConfirmSensorTestMethod, row.rejectConfirmSensorTestMethodOther,
                row.rejectConfirmSensorTestResult, row.rejectConfirmSensorEngineerNotes,
                row.rejectConfirmSensorLatched, row.rejectConfirmSensorCR,
                row.rejectConfirmSensorStopPosition, row.binFullSensorFitted,
                row.binFullSensorDetail, row.binFullSensorTestMethod, row.binFullSensorTestMethodOther,
                row.binFullSensorTestResult, row.binFullSensorEngineerNotes,
                row.binFullSensorLatched, row.binFullSensorCR, row.backupSensorFitted,
                row.backupSensorDetail, row.backupSensorTestMethod, row.backupSensorTestMethodOther,
                row.backupSensorTestResult, row.backupSensorEngineerNotes, row.backupSensorLatched,
                row.backupSensorCR, row.airPressureSensorFitted, row.airPressureSensorDetail,
                row.airPressureSensorTestMethod, row.airPressureSensorTestMethodOther,
                row.airPressureSensorTestResult, row.airPressureSensorEngineerNotes,
                row.airPressureSensorLatched, row.airPressureSensorCR, row.packCheckSensorFitted,
                row.packCheckSensorDetail, row.packCheckSensorTestMethod,
                row.packCheckSensorTestMethodOther, row.packCheckSensorTestResult,
                row.packCheckSensorEngineerNotes, row.packCheckSensorLatched, row.packCheckSensorCR,
                row.speedSensorFitted, row.speedSensorDetail, row.speedSensorTestMethod,
                row.speedSensorTestMethodOther, row.speedSensorTestResult,
                row.speedSensorEngineerNotes, row.speedSensorLatched, row.speedSensorCR,
                row.detectNotificationResult, row.detectNotificationEngineerNotes,
                row.binDoorMonitorFitted, row.binDoorMonitorDetail, row.binDoorStatusAsFound,
                row.binDoorUnlockedIndication, row.binDoorOpenIndication, row.binDoorTimeoutTimer,
                row.binDoorTimeoutResult, row.binDoorLatched, row.binDoorCR,
                row.binDoorEngineerNotes, row.operatorName, row.operatorTestWitnessed,
                row.operatorTestResultFerrous, row.operatorTestResultNonFerrous,
                row.operatorTestResultStainless, row.operatorTestResultLargeMetal,
                row.operatorTestResultCertNumberFerrous, row.operatorTestResultCertNumberNonFerrous,
                row.operatorTestResultCertNumberStainless, row.operatorTestResultCertNumberLargeMetal,
                row.smeName, row.smeEngineerNotes, row.detectionSetting1label,
                row.detectionSetting2label, row.detectionSetting3label, row.detectionSetting4label,
                row.detectionSetting5label, row.detectionSetting6label, row.detectionSetting7label,
                row.detectionSetting8label, row.detectionSettingPvResult, row.ferrousTestPvResult,
                row.nonFerrousTestPvResult, row.stainlessTestPvResult, row.smeTestPvResult,
                row.infeedSensorTestPvResult, row.binFullSensorTestPvResult,
                row.largeMetalTestPvResult, row.rejectConfirmSensorTestPvResult,
                row.backupSensorTestPvResult, row.airPressureSensorTestPvResult,
                row.packCheckSensorTestPvResult, row.speedSensorTestPvResult,
                row.binDoorMonitorTestPvResult, row.detectNotificationTestPvResult
            )

            FileWriter(csvFile).use { writer ->
                writer.append(data.joinToString(";"))
                writer.append("\n")
            }
            csvFile
        } catch (e: IOException) {
            InAppLogger.e("CSV Creation Error: ${e.message}")
            null
        }
    }


    suspend fun updateCalibrationStart(update: CalibrationStartUpdate) {
        calibrationDao.updateCalibrationStart(
            newLocation = update.newLocation,
            lastLocation = update.lastLocation,
            canPerformCalibration = update.canPerformCalibration,
            reasonForNotCalibrating = update.reasonForNotCalibrating,
            startCalibrationNotes = update.startCalibrationNotes,
            calibrationId = update.calibrationId,
            pvRequired = update.pvRequired
        )
    }

    suspend fun updateSensitivityRequirements(update: SensitivityRequirementsUpdate) {
        calibrationDao.updateSensitivityRequirements(
            desiredCop = update.desiredCop,
            sensitivityRequirementFerrous = update.sensitivityRequirementFerrous,
            sensitivityRequirementNonFerrous = update.sensitivityRequirementNonFerrous,
            sensitivityRequirementStainless = update.sensitivityRequirementStainless,
            sensitivityRequirementEngineerNotes = update.sensitivityRequirementEngineerNotes,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateProductDetails(update: ProductDetailsUpdate) {
        calibrationDao.updateProductDetails(
            productDescription = update.productDescription,
            productLibraryReference = update.productLibraryReference,
            productLibraryNumber = update.productLibraryNumber,
            productLength = update.productLength,
            productWidth = update.productWidth,
            productHeight = update.productHeight,
            productDetailsEngineerNotes = update.productDetailsEngineerNotes,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateDetectionSettingsAsFound(update: DetectionSettingsAsFoundUpdate) {
        calibrationDao.updateDetectionSettingsAsFound(
            detectionSettingAsFound1 = update.detectionSettingAsFound1,
            detectionSettingAsFound2 = update.detectionSettingAsFound2,
            detectionSettingAsFound3 = update.detectionSettingAsFound3,
            detectionSettingAsFound4 = update.detectionSettingAsFound4,
            detectionSettingAsFound5 = update.detectionSettingAsFound5,
            detectionSettingAsFound6 = update.detectionSettingAsFound6,
            detectionSettingAsFound7 = update.detectionSettingAsFound7,
            detectionSettingAsFound8 = update.detectionSettingAsFound8,
            detectionSetting1label = update.detectionSetting1label,
            detectionSetting2label = update.detectionSetting2label,
            detectionSetting3label = update.detectionSetting3label,
            detectionSetting4label = update.detectionSetting4label,
            detectionSetting5label = update.detectionSetting5label,
            detectionSetting6label = update.detectionSetting6label,
            detectionSetting7label = update.detectionSetting7label,
            detectionSetting8label = update.detectionSetting8label,
            sensitivityAccessRestriction = update.sensitivityAccessRestriction,
            detectionSettingPvResult = update.detectionSettingPvResult,
            detectionSettingAsFoundEngineerNotes = update.detectionSettingAsFoundEngineerNotes,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateSensitivitiesAsFound(update: SensitivitiesAsFoundUpdate) {
        calibrationDao.updateSensitivitiesAsFound(
            sensitivityAsFoundFerrous = update.sensitivityAsFoundFerrous,
            sensitivityAsFoundFerrousPeakSignal = update.sensitivityAsFoundFerrousPeakSignal,
            sensitivityAsFoundNonFerrous = update.sensitivityAsFoundNonFerrous,
            sensitivityAsFoundNonFerrousPeakSignal = update.sensitivityAsFoundNonFerrousPeakSignal,
            sensitivityAsFoundStainless = update.sensitivityAsFoundStainless,
            sensitivityAsFoundStainlessPeakSignal = update.sensitivityAsFoundStainlessPeakSignal,
            productPeakSignalAsFound = update.productPeakSignalAsFound,
            sensitivityAsFoundEngineerNotes = update.sensitivityAsFoundEngineerNotes,
            calibrationId = update.calibrationId,
        )
    }

    suspend fun updateFerrousResult(update: FerrousResultUpdate) {
        calibrationDao.updateFerrousResult(
            sensitivityAsLeftFerrous = update.sensitivityAsLeftFerrous,
            sampleCertificateNumberFerrous = update.sampleCertificateNumberFerrous,
            detectRejectFerrousLeading = update.detectRejectFerrousLeading,
            detectRejectFerrousLeadingPeakSignal = update.peakSignalFerrousLeading,
            detectRejectFerrousMiddle = update.detectRejectFerrousMiddle,
            detectRejectFerrousMiddlePeakSignal = update.peakSignalFerrousMiddle,
            detectRejectFerrousTrailing = update.detectRejectFerrousTrailing,
            detectRejectFerrousTrailingPeakSignal = update.peakSignalFerrousTrailing,
            ferrousTestEngineerNotes = update.ferrousTestEngineerNotes,
            ferrousTestPvResult = update.ferrousTestPvResult,
            calibrationId = update.calibrationId,
        )
    }

    suspend fun updateNonFerrousResult(update: NonFerrousResultUpdate) {
        calibrationDao.updateNonFerrousResult(
            sensitivityAsLeftNonFerrous = update.sensitivityAsLeftNonFerrous,
            sampleCertificateNumberNonFerrous = update.sampleCertificateNumberNonFerrous,
            detectRejectNonFerrousLeading = update.detectRejectNonFerrousLeading,
            detectRejectNonFerrousLeadingPeakSignal = update.detectRejectNonFerrousLeadingPeakSignal,
            detectRejectNonFerrousMiddle = update.detectRejectNonFerrousMiddle,
            detectRejectNonFerrousMiddlePeakSignal = update.detectRejectNonFerrousMiddlePeakSignal,
            detectRejectNonFerrousTrailing = update.detectRejectNonFerrousTrailing,
            detectRejectNonFerrousTrailingPeakSignal = update.detectRejectNonFerrousTrailingPeakSignal,
            nonFerrousTestEngineerNotes = update.nonFerrousTestEngineerNotes,
            nonFerrousTestPvResult = update.nonFerrousTestPvResult,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateStainlessResult(update: StainlessResultUpdate) {
        calibrationDao.updateStainlessResult(
            sensitivityAsLeftStainless = update.sensitivityAsLeftStainless,
            sampleCertificateNumberStainless = update.sampleCertificateNumberStainless,
            detectRejectStainlessLeading = update.detectRejectStainlessLeading,
            detectRejectStainlessLeadingPeakSignal = update.detectRejectStainlessLeadingPeakSignal,
            detectRejectStainlessMiddle = update.detectRejectStainlessMiddle,
            detectRejectStainlessMiddlePeakSignal = update.detectRejectStainlessMiddlePeakSignal,
            detectRejectStainlessTrailing = update.detectRejectStainlessTrailing,
            detectRejectStainlessTrailingPeakSignal = update.detectRejectStainlessTrailingPeakSignal,
            stainlessTestEngineerNotes = update.stainlessTestEngineerNotes,
            stainlessTestPvResult = update.stainlessTestPvResult,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateLargeMetalResult(update: LargeMetalResultUpdate) {
        calibrationDao.updateLargeMetalResult(
            detectRejectLargeMetal = update.detectRejectLargeMetal,
            sampleCertificateNumberLargeMetal = update.sampleCertificateNumberLargeMetal,
            largeMetalTestEngineerNotes = update.largeMetalTestEngineerNotes,
            largeMetalTestPvResult = update.largeMetalTestPvResult,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateDetectionSettingAsLeft(update: DetectionSettingAsLeftUpdate) {
        calibrationDao.updateDetectionSettingAsLeft(
            detectionSettingAsLeft1 = update.detectionSettingAsLeft1,
            detectionSettingAsLeft2 = update.detectionSettingAsLeft2,
            detectionSettingAsLeft3 = update.detectionSettingAsLeft3,
            detectionSettingAsLeft4 = update.detectionSettingAsLeft4,
            detectionSettingAsLeft5 = update.detectionSettingAsLeft5,
            detectionSettingAsLeft6 = update.detectionSettingAsLeft6,
            detectionSettingAsLeft7 = update.detectionSettingAsLeft7,
            detectionSettingAsLeft8 = update.detectionSettingAsLeft8,
            detectionSettingAsLeftEngineerNotes = update.detectionSettingAsLeftEngineerNotes,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateRejectSettings(update: RejectSettingsUpdate) {
        calibrationDao.updateRejectSettings(
            rejectSynchronisationSetting = update.rejectSynchronisationSetting,
            rejectSynchronisationDetail = update.rejectSynchronisationDetail,
            rejectDelaySetting = update.rejectDelaySetting,
            rejectDelayUnits = update.rejectDelayUnits,
            rejectDurationSetting = update.rejectDurationSetting,
            rejectDurationUnits = update.rejectDurationUnits,
            rejectConfirmWindowSetting = update.rejectConfirmWindowSetting,
            rejectConfirmWindowUnits = update.rejectConfirmWindowUnits,
            rejectSettingsEngineerNotes = update.rejectSettingsEngineerNotes,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateConveyorDetails(update: ConveyorDetailsUpdate) {
        calibrationDao.updateConveyorDetails(
            infeedBeltHeight = update.infeedBeltHeight,
            outfeedBeltHeight = update.outfeedBeltHeight,
            conveyorLength = update.conveyorLength,
            conveyorHanding = update.conveyorHanding,
            beltSpeed = update.beltSpeed,
            rejectDevice = update.rejectDevice,
            rejectDeviceOther = update.rejectDeviceOther,
            conveyorDetailsEngineerNotes = update.conveyorDetailsEngineerNotes,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateSystemChecklist(update: SystemChecklistUpdate) {
        calibrationDao.updateSystemChecklist(
            beltCondition = update.beltCondition,
            beltConditionComments = update.beltConditionComments,
            guardCondition = update.guardCondition,
            guardConditionComments = update.guardConditionComments,
            safetyCircuitCondition = update.safetyCircuitCondition,
            safetyCircuitConditionComments = update.safetyCircuitConditionComments,
            linerCondition = update.linerCondition,
            linerConditionComments = update.linerConditionComments,
            cablesCondition = update.cablesCondition,
            cablesConditionComments = update.cablesConditionComments,
            screwsCondition = update.screwsCondition,
            screwsConditionComments = update.screwsConditionComments,
            systemChecklistEngineerNotes = update.systemChecklistEngineerNotes,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateIndicators(update: IndicatorsUpdate) {
        calibrationDao.updateIndicators(
            indicator6label = update.indicator6label,
            indicator6colour = update.indicator6colour,
            indicator5label = update.indicator5label,
            indicator5colour = update.indicator5colour,
            indicator4label = update.indicator4label,
            indicator4colour = update.indicator4colour,
            indicator3label = update.indicator3label,
            indicator3colour = update.indicator3colour,
            indicator2label = update.indicator2label,
            indicator2colour = update.indicator2colour,
            indicator1label = update.indicator1label,
            indicator1colour = update.indicator1colour,
            indicatorsEngineerNotes = update.indicatorsEngineerNotes,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateInfeedSensor(update: InfeedSensorUpdate) {
        calibrationDao.updateInfeedSensor(
            infeedSensorFitted = update.infeedSensorFitted,
            infeedSensorDetail = update.infeedSensorDetail,
            infeedSensorTestMethod = update.infeedSensorTestMethod,
            infeedSensorTestMethodOther = update.infeedSensorTestMethodOther,
            infeedSensorTestResult = update.infeedSensorTestResult,
            infeedSensorEngineerNotes = update.infeedSensorEngineerNotes,
            infeedSensorLatched = update.infeedSensorLatched,
            infeedSensorCR = update.infeedSensorCR,
            infeedSensorTestPvResult = update.infeedSensorTestPvResult,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateRejectConfirmSensor(update: RejectConfirmSensorUpdate) {
        calibrationDao.updateRejectConfirmSensor(
            rejectConfirmSensorFitted = update.rejectConfirmSensorFitted,
            rejectConfirmSensorDetail = update.rejectConfirmSensorDetail,
            rejectConfirmSensorTestMethod = update.rejectConfirmSensorTestMethod,
            rejectConfirmSensorTestMethodOther = update.rejectConfirmSensorTestMethodOther,
            rejectConfirmSensorTestResult = update.rejectConfirmSensorTestResult,
            rejectConfirmSensorEngineerNotes = update.rejectConfirmSensorEngineerNotes,
            rejectConfirmSensorLatched = update.rejectConfirmSensorLatched,
            rejectConfirmSensorCR = update.rejectConfirmSensorCR,
            rejectConfirmSensorStopPosition = update.rejectConfirmSensorStopPosition,
            rejectConfirmSensorTestPvResult = update.rejectConfirmSensorTestPvResult,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateBinFullSensor(update: BinFullSensorUpdate) {
        calibrationDao.updateBinFullSensor(
            binFullSensorFitted = update.binFullSensorFitted,
            binFullSensorDetail = update.binFullSensorDetail,
            binFullSensorTestMethod = update.binFullSensorTestMethod,
            binFullSensorTestMethodOther = update.binFullSensorTestMethodOther,
            binFullSensorTestResult = update.binFullSensorTestResult,
            binFullSensorEngineerNotes = update.binFullSensorEngineerNotes,
            binFullSensorLatched = update.binFullSensorLatched,
            binFullSensorCR = update.binFullSensorCR,
            binFullSensorTestPvResult = update.binFullSensorTestPvResult,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateBackupSensor(update: BackupSensorUpdate) {
        calibrationDao.updateBackupSensor(
            backupSensorFitted = update.backupSensorFitted,
            backupSensorDetail = update.backupSensorDetail,
            backupSensorTestMethod = update.backupSensorTestMethod,
            backupSensorTestMethodOther = update.backupSensorTestMethodOther,
            backupSensorTestResult = update.backupSensorTestResult,
            backupSensorEngineerNotes = update.backupSensorEngineerNotes,
            backupSensorLatched = update.backupSensorLatched,
            backupSensorCR = update.backupSensorCR,
            backupSensorTestPvResult = update.backupSensorTestPvResult,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateAirPressureSensor(update: AirPressureSensorUpdate) {
        calibrationDao.updateAirPressureSensor(
            airPressureSensorFitted = update.airPressureSensorFitted,
            airPressureSensorDetail = update.airPressureSensorDetail,
            airPressureSensorTestMethod = update.airPressureSensorTestMethod,
            airPressureSensorTestMethodOther = update.airPressureSensorTestMethodOther,
            airPressureSensorTestResult = update.airPressureSensorTestResult,
            airPressureSensorEngineerNotes = update.airPressureSensorEngineerNotes,
            airPressureSensorLatched = update.airPressureSensorLatched,
            airPressureSensorCR = update.airPressureSensorCR,
            airPressureSensorTestPvResult = update.airPressureSensorTestPvResult,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updatePackCheckSensor(update: PackCheckSensorUpdate) {
        calibrationDao.updatePackCheckSensor(
            packCheckSensorFitted = update.packCheckSensorFitted,
            packCheckSensorDetail = update.packCheckSensorDetail,
            packCheckSensorTestMethod = update.packCheckSensorTestMethod,
            packCheckSensorTestMethodOther = update.packCheckSensorTestMethodOther,
            packCheckSensorTestResult = update.packCheckSensorTestResult,
            packCheckSensorEngineerNotes = update.packCheckSensorEngineerNotes,
            packCheckSensorLatched = update.packCheckSensorLatched,
            packCheckSensorCR = update.packCheckSensorCR,
            packCheckSensorTestPvResult = update.packCheckSensorTestPvResult,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateSpeedSensor(update: SpeedSensorUpdate) {
        calibrationDao.updateSpeedSensor(
            speedSensorFitted = update.speedSensorFitted,
            speedSensorDetail = update.speedSensorDetail,
            speedSensorTestMethod = update.speedSensorTestMethod,
            speedSensorTestMethodOther = update.speedSensorTestMethodOther,
            speedSensorTestResult = update.speedSensorTestResult,
            speedSensorEngineerNotes = update.speedSensorEngineerNotes,
            speedSensorLatched = update.speedSensorLatched,
            speedSensorCR = update.speedSensorCR,
            speedSensorTestPvResult = update.speedSensorTestPvResult,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateDetectNotification(update: DetectNotificationUpdate) {
        calibrationDao.updateDetectNotification(
            detectNotificationResult = update.detectNotificationResult,
            detectNotificationEngineerNotes = update.detectNotificationEngineerNotes,
            detectNotificationTestPvResult = update.detectNotificationTestPvResult,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateBinDoorMonitor(update: BinDoorMonitorUpdate) {
        calibrationDao.updateBinDoorMonitor(
            binDoorMonitorFitted = update.binDoorMonitorFitted,
            binDoorMonitorDetail = update.binDoorMonitorDetail,
            binDoorStatusAsFound = update.binDoorStatusAsFound,
            binDoorUnlockedIndication = update.binDoorUnlockedIndication,
            binDoorOpenIndication = update.binDoorOpenIndication,
            binDoorTimeoutTimer = update.binDoorTimeoutTimer,
            binDoorTimeoutResult = update.binDoorTimeoutResult,
            binDoorLatched = update.binDoorLatched,
            binDoorCR = update.binDoorCR,
            binDoorEngineerNotes = update.binDoorEngineerNotes,
            binDoorMonitorTestPvResult = update.binDoorMonitorTestPvResult,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateOperatorTest(update: OperatorTestUpdate) {
        calibrationDao.updateOperatorTest(
            operatorName = update.operatorName,
            operatorTestWitnessed = update.operatorTestWitnessed,
            operatorTestResultFerrous = update.operatorTestResultFerrous,
            operatorTestResultNonFerrous = update.operatorTestResultNonFerrous,
            operatorTestResultStainless = update.operatorTestResultStainless,
            operatorTestResultLargeMetal = update.operatorTestResultLargeMetal,
            operatorTestResultCertNumberFerrous = update.operatorTestResultCertNumberFerrous,
            operatorTestResultCertNumberNonFerrous = update.operatorTestResultCertNumberNonFerrous,
            operatorTestResultCertNumberStainless = update.operatorTestResultCertNumberStainless,
            operatorTestResultCertNumberLargeMetal = update.operatorTestResultCertNumberLargeMetal,
            smeName = update.smeName,
            smeEngineerNotes = update.smeEngineerNotes,
            smeTestPvResult = update.smeTestPvResult,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateDetectionSettingLabels(update: DetectionSettingLabelsUpdate) {
        calibrationDao.updateDetectionSettingLabels(
            detectionSetting1label = update.detectionSetting1label,
            detectionSetting2label = update.detectionSetting2label,
            detectionSetting3label = update.detectionSetting3label,
            detectionSetting4label = update.detectionSetting4label,
            detectionSetting5label = update.detectionSetting5label,
            detectionSetting6label = update.detectionSetting6label,
            detectionSetting7label = update.detectionSetting7label,
            detectionSetting8label = update.detectionSetting8label,
            calibrationId = update.calibrationId
        )
    }

    suspend fun updateCalibrationEnd(update: CalibrationEndUpdate) {
        calibrationDao.updateCalibrationEnd(
            endDate = update.endDate,
            calibrationId = update.calibrationId
        )
    }
}
