/**
 * ---------------------------------------------------------------
 *  Calibration Repository (Database Write Operations)
 * ---------------------------------------------------------------
 *
 * The repository acts as the single interface between the
 * calibration logic and the Room database.
 *
 * Every calibration "update" call (e.g., start details, product
 * details, sensitivity requirements, ferrous results, etc.)
 * flows through this class. The ViewModel never talks to a DAO
 * directly, and it never handles SQL parameter lists.
 *
 * Benefits:
 *   • Centralised write logic for the entire calibration flow
 *   • ViewModel stays readable and clean
 *   • Update functions always receive strongly-typed payloads
 *   • Easier debugging (one place to inspect data writes)
 *   • Future schema changes only require modifying this class
 *
 * In short:
 *   The ViewModel prepares the data.
 *   The repository decides where it goes.
 *   Room handles how it gets stored.
 *
 * If something breaks:
 *   – Compare the repository parameters with the DAO definition
 *   – Ensure the update payload in DatabaseUpdates.kt matches
 *   – Check for nullability mismatches (String vs Double?)
 *
 * ---------------------------------------------------------------
 */


package com.example.mecca.repositories

import com.example.mecca.daos.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.calibrationLogic.metalDetectorConveyor.AirPressureSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.BackupSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.BinDoorMonitorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.BinFullSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.CalibrationEndUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.CalibrationStartUpdate
//import com.example.mecca.calibrationLogic.metalDetectorConveyor.ComplianceConfirmationUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.ConveyorDetailsUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.DetectNotificationUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.DetectionSettingAsLeftUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.DetectionSettingLabelsUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.DetectionSettingsAsFoundUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.FerrousResultUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.IndicatorsUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.InfeedSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.LargeMetalResultUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.NewCalibrationInsert
import com.example.mecca.calibrationLogic.metalDetectorConveyor.NonFerrousResultUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.OperatorTestUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.PackCheckSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.ProductDetailsUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.RejectConfirmSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.RejectSettingsUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.SensitivitiesAsFoundUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.SensitivityRequirementsUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.SpeedSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.StainlessResultUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.SystemChecklistUpdate
import com.example.mecca.dataClasses.MetalDetectorConveyorCalibrationLocal

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

//    suspend fun updateComplianceConfirmation(update: ComplianceConfirmationUpdate) {
//        calibrationDao.updateComplianceConfirmation(
//            sensitivityCompliance = update.sensitivityCompliance,
//            essentialRequirementCompliance = update.essentialRequirementCompliance,
//            failsafeCompliance = update.failsafeCompliance,
//            bestSensitivityCompliance = update.bestSensitivityCompliance,
//            sensitivityRecommendations = update.sensitivityRecommendations,
//            performanceValidationIssued = update.performanceValidationIssued,
//            calibrationId = update.calibrationId
//        )
//    }

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