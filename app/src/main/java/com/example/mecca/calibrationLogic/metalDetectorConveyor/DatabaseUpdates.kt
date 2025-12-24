package com.example.mecca.calibrationLogic.metalDetectorConveyor

import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * ---------------------------------------------------------------
 *  Calibration Database Update Mappers & Payloads
 * ---------------------------------------------------------------
 *
 * This file exists to keep the ViewModel clean and sane.
 *
 * Every calibration "update" operation in the app follows the
 * same basic pattern:
 *
 *   1. Read a bunch of UI state values from the ViewModel
 *   2. Convert that state into a simple data object
 *   3. Pass that data object to the repository, which calls Room
 *
 * Previously, all of these steps were hard-coded inside the
 * 3000+ line ViewModel, which made the file impossible to read,
 * maintain, or safely modify.
 *
 * These small mapper functions:
 *
 *   • Group related update fields into clear data classes
 *   • Provide extension functions on the ViewModel that build
 *     those update objects cleanly
 *   • Centralise all calibration DB write logic in one place
 *   • Avoid repeating long parameter lists across the project
 *   • Make adding new fields or refactoring existing ones far
 *     easier (only update the mapper here)
 *
 * In short:
 *   The ViewModel should describe *what* is being updated.
 *   This file describes *how* the update data is constructed.
 *   The repository handles *where* it gets stored.
 *
 * If you’re reading this because something broke:
 *   – Check that the ViewModel imports the correct mapper
 *   – Check that the update payload matches the DAO call
 *   – Check the repository function for mismatched fields
 *
 * Otherwise, enjoy the fact that this logic is no longer buried
 * somewhere between 3000 lines of unrelated state handling.
 *
 * ---------------------------------------------------------------
 */


//-------------------------------------------------------------------------------------------------
// CALIBRATION START
//-------------------------------------------------------------------------------------------------

data class NewCalibrationInsert(
    val calibrationId: String,
    val mapVersion: String,
    val systemId: Int,
    val tempSystemId: Int,
    val cloudSystemId: Int,
    val modelId: Int,
    val serialNumber: String,
    val engineerId: Int,
    val customerId: Int,
    val startDate: String,
    val detectionSetting1label: String,
    val detectionSetting2label: String,
    val detectionSetting3label: String,
    val detectionSetting4label: String,
    val detectionSetting5label: String,
    val detectionSetting6label: String,
    val detectionSetting7label: String,
    val detectionSetting8label: String,
    val lastLocation: String
)

fun CalibrationMetalDetectorConveyorViewModel.toNewCalibrationInsert(): NewCalibrationInsert {
    return NewCalibrationInsert(
        calibrationId = calibrationId.value,
        mapVersion = "MDC1.0",
        systemId = systemId.value,
        tempSystemId = tempSystemId.value,
        cloudSystemId = cloudSystemId.value,
        modelId = modelId.value,
        serialNumber = serialNumber.value,
        engineerId = engineerId,
        customerId = customerId.value,
        startDate = calibrationStartTime.value,
        detectionSetting1label = detectionSetting1label.value,
        detectionSetting2label = detectionSetting2label.value,
        detectionSetting3label = detectionSetting3label.value,
        detectionSetting4label = detectionSetting4label.value,
        detectionSetting5label = detectionSetting5label.value,
        detectionSetting6label = detectionSetting6label.value,
        detectionSetting7label = detectionSetting7label.value,
        detectionSetting8label = detectionSetting8label.value,
        lastLocation = lastLocation.value
    )
}




data class CalibrationStartUpdate(
    val newLocation: String,
    val lastLocation: String,
    val canPerformCalibration: String,
    val reasonForNotCalibrating: String,
    val startCalibrationNotes: String,
    val pvRequired: Boolean,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toCalibrationStartUpdate(): CalibrationStartUpdate {
    return CalibrationStartUpdate(
        newLocation = newLocation.value,
        lastLocation = lastLocation.value,
        canPerformCalibration = canPerformCalibration.value.toString(),
        reasonForNotCalibrating = reasonForNotCalibrating.value,
        startCalibrationNotes = startCalibrationNotes.value,
        pvRequired = pvRequired.value,
        calibrationId = calibrationId.value
    )
}

//-------------------------------------------------------------------------------------------------
// CUSTOMER SENSITIVITY REQUIREMENTS
//-------------------------------------------------------------------------------------------------

data class SensitivityRequirementsUpdate(
    val desiredCop: String,
    val sensitivityRequirementFerrous: String,
    val sensitivityRequirementNonFerrous: String,
    val sensitivityRequirementStainless: String,
    val sensitivityRequirementEngineerNotes: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toSensitivityRequirementsUpdate()
        : SensitivityRequirementsUpdate {

    return SensitivityRequirementsUpdate(
        desiredCop = desiredCop.value.toString(),
        sensitivityRequirementFerrous = sensitivityRequirementFerrous.value,
        sensitivityRequirementNonFerrous = sensitivityRequirementNonFerrous.value,
        sensitivityRequirementStainless = sensitivityRequirementStainless.value,
        sensitivityRequirementEngineerNotes = sensitivityRequirementEngineerNotes.value,
        calibrationId = calibrationId.value
    )
}

//-------------------------------------------------------------------------------------------------
// PRODUCT DETAILS
//-------------------------------------------------------------------------------------------------


data class ProductDetailsUpdate(
    val productDescription: String,
    val productLibraryReference: String,
    val productLibraryNumber: String,
    val productLength: String,
    val productWidth: String,
    val productHeight: String,
    val productDetailsEngineerNotes: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toProductDetailsUpdate()
        : ProductDetailsUpdate {

    return ProductDetailsUpdate(
        productDescription = productDescription.value,
        productLibraryReference = productLibraryReference.value,
        productLibraryNumber = productLibraryNumber.value,
        productLength = productLength.value,
        productWidth = productWidth.value,
        productHeight = productHeight.value,
        productDetailsEngineerNotes = productDetailsEngineerNotes.value,
        calibrationId = calibrationId.value
    )
}

//-------------------------------------------------------------------------------------------------
// DETECTION SETTINGS (AS FOUND)
//-------------------------------------------------------------------------------------------------

data class DetectionSettingsAsFoundUpdate(
    val detectionSettingAsFound1: String,
    val detectionSettingAsFound2: String,
    val detectionSettingAsFound3: String,
    val detectionSettingAsFound4: String,
    val detectionSettingAsFound5: String,
    val detectionSettingAsFound6: String,
    val detectionSettingAsFound7: String,
    val detectionSettingAsFound8: String,
    val sensitivityAccessRestriction: String,
    val detectionSettingPvResult: String,
    val detectionSettingAsFoundEngineerNotes: String,
    val calibrationId: String
)


fun CalibrationMetalDetectorConveyorViewModel.toDetectionSettingsAsFoundUpdate()
: DetectionSettingsAsFoundUpdate {

    return DetectionSettingsAsFoundUpdate(
        detectionSettingAsFound1 = detectionSettingAsFound1.value,
        detectionSettingAsFound2 = detectionSettingAsFound2.value,
        detectionSettingAsFound3 = detectionSettingAsFound3.value,
        detectionSettingAsFound4 = detectionSettingAsFound4.value,
        detectionSettingAsFound5 = detectionSettingAsFound5.value,
        detectionSettingAsFound6 = detectionSettingAsFound6.value,
        detectionSettingAsFound7 = detectionSettingAsFound7.value,
        detectionSettingAsFound8 = detectionSettingAsFound8.value,
        sensitivityAccessRestriction = sensitivityAccessRestriction.value,
        detectionSettingPvResult = detectionSettingPvResult.value,
        detectionSettingAsFoundEngineerNotes = detectionSettingAsFoundEngineerNotes.value,
        calibrationId = calibrationId.value
    )
}

//-------------------------------------------------------------------------------------------------
// SENSITIVITIES (AS FOUND)
//-------------------------------------------------------------------------------------------------


data class SensitivitiesAsFoundUpdate(
    val sensitivityAsFoundFerrous: String,
    val sensitivityAsFoundFerrousPeakSignal: String,
    val sensitivityAsFoundNonFerrous: String,
    val sensitivityAsFoundNonFerrousPeakSignal: String,
    val sensitivityAsFoundStainless: String,
    val sensitivityAsFoundStainlessPeakSignal: String,
    val productPeakSignalAsFound: String,
    val sensitivityAsFoundEngineerNotes: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toSensitivitiesAsFoundUpdate()
    : SensitivitiesAsFoundUpdate {

    return SensitivitiesAsFoundUpdate(
        sensitivityAsFoundFerrous = sensitivityAsFoundFerrous.value,
        sensitivityAsFoundFerrousPeakSignal = sensitivityAsFoundFerrousPeakSignal.value,
        sensitivityAsFoundNonFerrous = sensitivityAsFoundNonFerrous.value,
        sensitivityAsFoundNonFerrousPeakSignal = sensitivityAsFoundNonFerrousPeakSignal.value,
        sensitivityAsFoundStainless = sensitivityAsFoundStainless.value,
        sensitivityAsFoundStainlessPeakSignal = sensitivityAsFoundStainlessPeakSignal.value,
        productPeakSignalAsFound = productPeakSignalAsFound.value,
        sensitivityAsFoundEngineerNotes = sensitivityAsFoundEngineerNotes.value,
        calibrationId = calibrationId.value
    )
}

//-------------------------------------------------------------------------------------------------
// FERROUS TEST RESULTS
//-------------------------------------------------------------------------------------------------


data class FerrousResultUpdate(
    val sensitivityAsLeftFerrous: String,
    val sampleCertificateNumberFerrous: String,
    val detectRejectFerrousLeading: String,
    val peakSignalFerrousLeading: String,
    val detectRejectFerrousMiddle: String,
    val peakSignalFerrousMiddle: String,
    val detectRejectFerrousTrailing: String,
    val peakSignalFerrousTrailing: String,
    val ferrousTestPvResult: String,
    val ferrousTestEngineerNotes: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toFerrousResultUpdate()
    : FerrousResultUpdate {

    return FerrousResultUpdate(
        sensitivityAsLeftFerrous = sensitivityAsLeftFerrous.value,
        sampleCertificateNumberFerrous = sampleCertificateNumberFerrous.value,
        detectRejectFerrousLeading = detectRejectFerrousLeading.value.toString(),
        peakSignalFerrousLeading = peakSignalFerrousLeading.value,
        detectRejectFerrousMiddle = detectRejectFerrousMiddle.value.toString(),
        peakSignalFerrousMiddle = peakSignalFerrousMiddle.value,
        detectRejectFerrousTrailing = detectRejectFerrousTrailing.value.toString(),
        peakSignalFerrousTrailing = peakSignalFerrousTrailing.value,
        ferrousTestEngineerNotes = ferrousTestEngineerNotes.value,
        ferrousTestPvResult = ferrousTestPvResult.value,
        calibrationId = calibrationId.value
    )
}

data class NonFerrousResultUpdate(
    val sensitivityAsLeftNonFerrous: String,
    val sampleCertificateNumberNonFerrous: String,
    val detectRejectNonFerrousLeading: String,
    val detectRejectNonFerrousLeadingPeakSignal: String,
    val detectRejectNonFerrousMiddle: String,
    val detectRejectNonFerrousMiddlePeakSignal: String,
    val detectRejectNonFerrousTrailing: String,
    val detectRejectNonFerrousTrailingPeakSignal: String,
    val nonFerrousTestEngineerNotes: String,
    val nonFerrousTestPvResult: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toNonFerrousResultUpdate(): NonFerrousResultUpdate {
    return NonFerrousResultUpdate(
        sensitivityAsLeftNonFerrous = sensitivityAsLeftNonFerrous.value,
        sampleCertificateNumberNonFerrous = sampleCertificateNumberNonFerrous.value,
        detectRejectNonFerrousLeading = detectRejectNonFerrousLeading.value.toString(),
        detectRejectNonFerrousLeadingPeakSignal = peakSignalNonFerrousLeading.value,
        detectRejectNonFerrousMiddle = detectRejectNonFerrousMiddle.value.toString(),
        detectRejectNonFerrousMiddlePeakSignal = peakSignalNonFerrousMiddle.value,
        detectRejectNonFerrousTrailing = detectRejectNonFerrousTrailing.value.toString(),
        detectRejectNonFerrousTrailingPeakSignal = peakSignalNonFerrousTrailing.value,
        nonFerrousTestEngineerNotes = nonFerrousTestEngineerNotes.value,
        nonFerrousTestPvResult = nonFerrousTestPvResult.value,
        calibrationId = calibrationId.value
    )
}

data class StainlessResultUpdate(
    val sensitivityAsLeftStainless: String,
    val sampleCertificateNumberStainless: String,
    val detectRejectStainlessLeading: String,
    val detectRejectStainlessLeadingPeakSignal: String,
    val detectRejectStainlessMiddle: String,
    val detectRejectStainlessMiddlePeakSignal: String,
    val detectRejectStainlessTrailing: String,
    val detectRejectStainlessTrailingPeakSignal: String,
    val stainlessTestEngineerNotes: String,
    val stainlessTestPvResult: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toStainlessResultUpdate(): StainlessResultUpdate {
    return StainlessResultUpdate(
        sensitivityAsLeftStainless = sensitivityAsLeftStainless.value,
        sampleCertificateNumberStainless = sampleCertificateNumberStainless.value,
        detectRejectStainlessLeading = detectRejectStainlessLeading.value.toString(),
        detectRejectStainlessLeadingPeakSignal = peakSignalStainlessLeading.value,
        detectRejectStainlessMiddle = detectRejectStainlessMiddle.value.toString(),
        detectRejectStainlessMiddlePeakSignal = peakSignalStainlessMiddle.value,
        detectRejectStainlessTrailing = detectRejectStainlessTrailing.value.toString(),
        detectRejectStainlessTrailingPeakSignal = peakSignalStainlessTrailing.value,
        stainlessTestEngineerNotes = stainlessTestEngineerNotes.value,
        stainlessTestPvResult = stainlessTestPvResult.value,
        calibrationId = calibrationId.value
    )
}

data class LargeMetalResultUpdate(
    val detectRejectLargeMetal: String,
    val sampleCertificateNumberLargeMetal: String,
    val largeMetalTestEngineerNotes: String,
    val largeMetalTestPvResult: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toLargeMetalResultUpdate(): LargeMetalResultUpdate {
    return LargeMetalResultUpdate(
        detectRejectLargeMetal = detectRejectLargeMetal.value.toString(),
        sampleCertificateNumberLargeMetal = sampleCertificateNumberLargeMetal.value,
        largeMetalTestEngineerNotes = largeMetalTestEngineerNotes.value,
        largeMetalTestPvResult = largeMetalTestPvResult.value,
        calibrationId = calibrationId.value
    )
}

data class DetectionSettingAsLeftUpdate(
    val detectionSettingAsLeft1: String,
    val detectionSettingAsLeft2: String,
    val detectionSettingAsLeft3: String,
    val detectionSettingAsLeft4: String,
    val detectionSettingAsLeft5: String,
    val detectionSettingAsLeft6: String,
    val detectionSettingAsLeft7: String,
    val detectionSettingAsLeft8: String,
    val detectionSettingAsLeftEngineerNotes: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toDetectionSettingAsLeftUpdate()
        : DetectionSettingAsLeftUpdate {

    return DetectionSettingAsLeftUpdate(
        detectionSettingAsLeft1 = detectionSettingAsLeft1.value,
        detectionSettingAsLeft2 = detectionSettingAsLeft2.value,
        detectionSettingAsLeft3 = detectionSettingAsLeft3.value,
        detectionSettingAsLeft4 = detectionSettingAsLeft4.value,
        detectionSettingAsLeft5 = detectionSettingAsLeft5.value,
        detectionSettingAsLeft6 = detectionSettingAsLeft6.value,
        detectionSettingAsLeft7 = detectionSettingAsLeft7.value,
        detectionSettingAsLeft8 = detectionSettingAsLeft8.value,
        detectionSettingAsLeftEngineerNotes = detectionSettingAsLeftEngineerNotes.value,
        calibrationId = calibrationId.value
    )
}

data class RejectSettingsUpdate(
    val rejectSynchronisationSetting: String,
    val rejectSynchronisationDetail: String,
    val rejectDelaySetting: String,
    val rejectDelayUnits: String,
    val rejectDurationSetting: String,
    val rejectDurationUnits: String,
    val rejectConfirmWindowSetting: String,
    val rejectConfirmWindowUnits: String,
    val rejectSettingsEngineerNotes: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toRejectSettingsUpdate(): RejectSettingsUpdate {
    return RejectSettingsUpdate(
        rejectSynchronisationSetting = rejectSynchronisationSetting.value.toString(),
        rejectSynchronisationDetail = rejectSynchronisationDetail.value,
        rejectDelaySetting = rejectDelaySetting.value,
        rejectDelayUnits = rejectDelayUnits.value,
        rejectDurationSetting = rejectDurationSetting.value,
        rejectDurationUnits = rejectDurationUnits.value,
        rejectConfirmWindowSetting = rejectConfirmWindowSetting.value,
        rejectConfirmWindowUnits = rejectConfirmWindowUnits.value,
        rejectSettingsEngineerNotes = rejectSettingsEngineerNotes.value,
        calibrationId = calibrationId.value
    )
}

data class ConveyorDetailsUpdate(
    val infeedBeltHeight: String,
    val outfeedBeltHeight: String,
    val conveyorLength: String,
    val conveyorHanding: String,
    val beltSpeed: String,
    val rejectDevice: String,
    val rejectDeviceOther: String,
    val conveyorDetailsEngineerNotes: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toConveyorDetailsUpdate(): ConveyorDetailsUpdate {
    return ConveyorDetailsUpdate(
        infeedBeltHeight = infeedBeltHeight.value,
        outfeedBeltHeight = outfeedBeltHeight.value,
        conveyorLength = conveyorLength.value,
        conveyorHanding = conveyorHanding.value,
        beltSpeed = beltSpeed.value,
        rejectDevice = rejectDevice.value,
        rejectDeviceOther = rejectDeviceOther.value,
        conveyorDetailsEngineerNotes = conveyorDetailsEngineerNotes.value,
        calibrationId = calibrationId.value
    )
}

data class SystemChecklistUpdate(
    val beltCondition: String,
    val beltConditionComments: String,
    val guardCondition: String,
    val guardConditionComments: String,
    val safetyCircuitCondition: String,
    val safetyCircuitConditionComments: String,
    val linerCondition: String,
    val linerConditionComments: String,
    val cablesCondition: String,
    val cablesConditionComments: String,
    val screwsCondition: String,
    val screwsConditionComments: String,
    val systemChecklistEngineerNotes: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toSystemChecklistUpdate(): SystemChecklistUpdate {
    return SystemChecklistUpdate(
        beltCondition = beltCondition.value.toString(),
        beltConditionComments = beltConditionComments.value,
        guardCondition = guardCondition.value.toString(),
        guardConditionComments = guardConditionComments.value,
        safetyCircuitCondition = safetyCircuitCondition.value.toString(),
        safetyCircuitConditionComments = safetyCircuitConditionComments.value,
        linerCondition = linerCondition.value.toString(),
        linerConditionComments = linerConditionComments.value,
        cablesCondition = cablesCondition.value.toString(),
        cablesConditionComments = cablesConditionComments.value,
        screwsCondition = screwsCondition.value.toString(),
        screwsConditionComments = screwsConditionComments.value,
        systemChecklistEngineerNotes = systemChecklistEngineerNotes.value,
        calibrationId = calibrationId.value
    )
}

data class IndicatorsUpdate(
    val indicator6label: String,
    val indicator6colour: String,
    val indicator5label: String,
    val indicator5colour: String,
    val indicator4label: String,
    val indicator4colour: String,
    val indicator3label: String,
    val indicator3colour: String,
    val indicator2label: String,
    val indicator2colour: String,
    val indicator1label: String,
    val indicator1colour: String,
    val indicatorsEngineerNotes: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toIndicatorsUpdate(): IndicatorsUpdate {
    return IndicatorsUpdate(
        indicator6label = indicator6label.value,
        indicator6colour = indicator6colour.value,
        indicator5label = indicator5label.value,
        indicator5colour = indicator5colour.value,
        indicator4label = indicator4label.value,
        indicator4colour = indicator4colour.value,
        indicator3label = indicator3label.value,
        indicator3colour = indicator3colour.value,
        indicator2label = indicator2label.value,
        indicator2colour = indicator2colour.value,
        indicator1label = indicator1label.value,
        indicator1colour = indicator1colour.value,
        indicatorsEngineerNotes = indicatorsEngineerNotes.value,
        calibrationId = calibrationId.value
    )
}

data class InfeedSensorUpdate(
    val infeedSensorFitted: String,
    val infeedSensorDetail: String,
    val infeedSensorTestMethod: String,
    val infeedSensorTestMethodOther: String,
    val infeedSensorTestResult: String,
    val infeedSensorEngineerNotes: String,
    val infeedSensorLatched: String,
    val infeedSensorCR: String,
    val infeedSensorTestPvResult: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toInfeedSensorUpdate(): InfeedSensorUpdate {
    return InfeedSensorUpdate(
        infeedSensorFitted = infeedSensorFitted.value.toString(),
        infeedSensorDetail = infeedSensorDetail.value,
        infeedSensorTestMethod = infeedSensorTestMethod.value,
        infeedSensorTestMethodOther = infeedSensorTestMethodOther.value,
        infeedSensorTestResult = infeedSensorTestResult.value.toString(),
        infeedSensorEngineerNotes = infeedSensorEngineerNotes.value,
        infeedSensorLatched = infeedSensorLatched.value.toString(),
        infeedSensorCR = infeedSensorCR.value.toString(),
        infeedSensorTestPvResult = infeedSensorTestPvResult.value,
        calibrationId = calibrationId.value
    )
}

data class RejectConfirmSensorUpdate(
    val rejectConfirmSensorFitted: String,
    val rejectConfirmSensorDetail: String,
    val rejectConfirmSensorTestMethod: String,
    val rejectConfirmSensorTestMethodOther: String,
    val rejectConfirmSensorTestResult: String,
    val rejectConfirmSensorEngineerNotes: String,
    val rejectConfirmSensorLatched: String,
    val rejectConfirmSensorCR: String,
    val rejectConfirmSensorStopPosition: String,
    val rejectConfirmSensorTestPvResult: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toRejectConfirmSensorUpdate()
        : RejectConfirmSensorUpdate {

    return RejectConfirmSensorUpdate(
        rejectConfirmSensorFitted = rejectConfirmSensorFitted.value.toString(),
        rejectConfirmSensorDetail = rejectConfirmSensorDetail.value,
        rejectConfirmSensorTestMethod = rejectConfirmSensorTestMethod.value,
        rejectConfirmSensorTestMethodOther = rejectConfirmSensorTestMethodOther.value,
        rejectConfirmSensorTestResult = rejectConfirmSensorTestResult.value.toString(),
        rejectConfirmSensorEngineerNotes = rejectConfirmSensorEngineerNotes.value,
        rejectConfirmSensorLatched = rejectConfirmSensorLatched.value.toString(),
        rejectConfirmSensorCR = rejectConfirmSensorCR.value.toString(),
        rejectConfirmSensorStopPosition = rejectConfirmSensorStopPosition.value,
        rejectConfirmSensorTestPvResult = rejectConfirmSensorTestPvResult.value,
        calibrationId = calibrationId.value
    )
}

data class BinFullSensorUpdate(
    val binFullSensorFitted: String,
    val binFullSensorDetail: String,
    val binFullSensorTestMethod: String,
    val binFullSensorTestMethodOther: String,
    val binFullSensorTestResult: String,
    val binFullSensorEngineerNotes: String,
    val binFullSensorLatched: String,
    val binFullSensorCR: String,
    val binFullSensorTestPvResult: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toBinFullSensorUpdate(): BinFullSensorUpdate {
    return BinFullSensorUpdate(
        binFullSensorFitted = binFullSensorFitted.value.toString(),
        binFullSensorDetail = binFullSensorDetail.value,
        binFullSensorTestMethod = binFullSensorTestMethod.value,
        binFullSensorTestMethodOther = binFullSensorTestMethodOther.value,
        binFullSensorTestResult = binFullSensorTestResult.value.toString(),
        binFullSensorEngineerNotes = binFullSensorEngineerNotes.value,
        binFullSensorLatched = binFullSensorLatched.value.toString(),
        binFullSensorCR = binFullSensorCR.value.toString(),
        binFullSensorTestPvResult = binFullSensorTestPvResult.value,
        calibrationId = calibrationId.value
    )
}

data class BackupSensorUpdate(
    val backupSensorFitted: String,
    val backupSensorDetail: String,
    val backupSensorTestMethod: String,
    val backupSensorTestMethodOther: String,
    val backupSensorTestResult: String,
    val backupSensorEngineerNotes: String,
    val backupSensorLatched: String,
    val backupSensorCR: String,
    val backupSensorTestPvResult: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toBackupSensorUpdate(): BackupSensorUpdate {
    return BackupSensorUpdate(
        backupSensorFitted = backupSensorFitted.value.toString(),
        backupSensorDetail = backupSensorDetail.value,
        backupSensorTestMethod = backupSensorTestMethod.value,
        backupSensorTestMethodOther = backupSensorTestMethodOther.value,
        backupSensorTestResult = backupSensorTestResult.value.toString(),
        backupSensorEngineerNotes = backupSensorEngineerNotes.value,
        backupSensorLatched = backupSensorLatched.value.toString(),
        backupSensorCR = backupSensorCR.value.toString(),
        backupSensorTestPvResult = backupSensorTestPvResult.value,
        calibrationId = calibrationId.value
    )
}

data class AirPressureSensorUpdate(
    val airPressureSensorFitted: String,
    val airPressureSensorDetail: String,
    val airPressureSensorTestMethod: String,
    val airPressureSensorTestMethodOther: String,
    val airPressureSensorTestResult: String,
    val airPressureSensorEngineerNotes: String,
    val airPressureSensorLatched: String,
    val airPressureSensorCR: String,
    val airPressureSensorTestPvResult: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toAirPressureSensorUpdate(): AirPressureSensorUpdate {
    return AirPressureSensorUpdate(
        airPressureSensorFitted = airPressureSensorFitted.value.toString(),
        airPressureSensorDetail = airPressureSensorDetail.value,
        airPressureSensorTestMethod = airPressureSensorTestMethod.value,
        airPressureSensorTestMethodOther = airPressureSensorTestMethodOther.value,
        airPressureSensorTestResult = airPressureSensorTestResult.value.toString(),
        airPressureSensorEngineerNotes = airPressureSensorEngineerNotes.value,
        airPressureSensorLatched = airPressureSensorLatched.value.toString(),
        airPressureSensorCR = airPressureSensorCR.value.toString(),
        airPressureSensorTestPvResult = airPressureSensorTestPvResult.value,
        calibrationId = calibrationId.value
    )
}

data class PackCheckSensorUpdate(
    val packCheckSensorFitted: String,
    val packCheckSensorDetail: String,
    val packCheckSensorTestMethod: String,
    val packCheckSensorTestMethodOther: String,
    val packCheckSensorTestResult: String,
    val packCheckSensorEngineerNotes: String,
    val packCheckSensorLatched: String,
    val packCheckSensorCR: String,
    val packCheckSensorTestPvResult: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toPackCheckSensorUpdate(): PackCheckSensorUpdate {
    return PackCheckSensorUpdate(
        packCheckSensorFitted = packCheckSensorFitted.value.toString(),
        packCheckSensorDetail = packCheckSensorDetail.value,
        packCheckSensorTestMethod = packCheckSensorTestMethod.value,
        packCheckSensorTestMethodOther = packCheckSensorTestMethodOther.value,
        packCheckSensorTestResult = packCheckSensorTestResult.value.toString(),
        packCheckSensorEngineerNotes = packCheckSensorEngineerNotes.value,
        packCheckSensorLatched = packCheckSensorLatched.value.toString(),
        packCheckSensorCR = packCheckSensorCR.value.toString(),
        packCheckSensorTestPvResult = packCheckSensorTestPvResult.value,
        calibrationId = calibrationId.value
    )
}

data class SpeedSensorUpdate(
    val speedSensorFitted: String,
    val speedSensorDetail: String,
    val speedSensorTestMethod: String,
    val speedSensorTestMethodOther: String,
    val speedSensorTestResult: String,
    val speedSensorEngineerNotes: String,
    val speedSensorLatched: String,
    val speedSensorCR: String,
    val speedSensorTestPvResult: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toSpeedSensorUpdate(): SpeedSensorUpdate {
    return SpeedSensorUpdate(
        speedSensorFitted = speedSensorFitted.value.toString(),
        speedSensorDetail = speedSensorDetail.value,
        speedSensorTestMethod = speedSensorTestMethod.value,
        speedSensorTestMethodOther = speedSensorTestMethodOther.value,
        speedSensorTestResult = speedSensorTestResult.value.toString(),
        speedSensorEngineerNotes = speedSensorEngineerNotes.value,
        speedSensorLatched = speedSensorLatched.value.toString(),
        speedSensorCR = speedSensorCR.value.toString(),
        speedSensorTestPvResult = speedSensorTestPvResult.value,
        calibrationId = calibrationId.value
    )
}

data class DetectNotificationUpdate(
    val detectNotificationResult: String,
    val detectNotificationEngineerNotes: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toDetectNotificationUpdate(): DetectNotificationUpdate {
    return DetectNotificationUpdate(
        detectNotificationResult = detectNotificationResult.value.toString(),
        detectNotificationEngineerNotes = detectNotificationEngineerNotes.value,
        calibrationId = calibrationId.value
    )
}

data class BinDoorMonitorUpdate(
    val binDoorMonitorFitted: String,
    val binDoorMonitorDetail: String,
    val binDoorStatusAsFound: String,
    val binDoorUnlockedIndication: String,
    val binDoorOpenIndication: String,
    val binDoorTimeoutTimer: String,
    val binDoorTimeoutResult: String,
    val binDoorLatched: String,
    val binDoorCR: String,
    val binDoorEngineerNotes: String,
    val binDoorMonitorTestPvResult: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toBinDoorMonitorUpdate(): BinDoorMonitorUpdate {
    return BinDoorMonitorUpdate(
        binDoorMonitorFitted = binDoorMonitorFitted.value.toString(),
        binDoorMonitorDetail = binDoorMonitorDetail.value,
        binDoorStatusAsFound = binDoorStatusAsFound.value,
        binDoorUnlockedIndication = binDoorUnlockedIndication.value.toString(),
        binDoorOpenIndication = binDoorOpenIndication.value.toString(),
        binDoorTimeoutTimer = binDoorTimeoutTimer.value,
        binDoorTimeoutResult = binDoorTimeoutResult.value.toString(),
        binDoorLatched = binDoorLatched.value.toString(),
        binDoorCR = binDoorCR.value.toString(),
        binDoorEngineerNotes = binDoorEngineerNotes.value,
        binDoorMonitorTestPvResult = binDoorMonitorTestPvResult.value,
        calibrationId = calibrationId.value
    )
}

data class OperatorTestUpdate(
    val operatorName: String,
    val operatorTestWitnessed: String,
    val operatorTestResultFerrous: String,
    val operatorTestResultNonFerrous: String,
    val operatorTestResultStainless: String,
    val operatorTestResultLargeMetal: String,
    val operatorTestResultCertNumberFerrous: String,
    val operatorTestResultCertNumberNonFerrous: String,
    val operatorTestResultCertNumberStainless: String,
    val operatorTestResultCertNumberLargeMetal: String,
    val smeName: String,
    val smeEngineerNotes: String,
    val smeTestPvResult: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toOperatorTestUpdate(): OperatorTestUpdate {
    return OperatorTestUpdate(
        operatorName = operatorName.value,
        operatorTestWitnessed = operatorTestWitnessed.value.toString(),
        operatorTestResultFerrous = operatorTestResultFerrous.value,
        operatorTestResultNonFerrous = operatorTestResultNonFerrous.value,
        operatorTestResultStainless = operatorTestResultStainless.value,
        operatorTestResultLargeMetal = operatorTestResultLargeMetal.value,
        operatorTestResultCertNumberFerrous = operatorTestResultCertNumberFerrous.value,
        operatorTestResultCertNumberNonFerrous = operatorTestResultCertNumberNonFerrous.value,
        operatorTestResultCertNumberStainless = operatorTestResultCertNumberStainless.value,
        operatorTestResultCertNumberLargeMetal = operatorTestResultCertNumberLargeMetal.value,
        smeName = smeName.value,
        smeEngineerNotes = smeEngineerNotes.value,
        smeTestPvResult = smeTestPvResult.value,
        calibrationId = calibrationId.value
    )
}

//data class ComplianceConfirmationUpdate(
//    val sensitivityCompliance: String,
//    val essentialRequirementCompliance: String,
//    val failsafeCompliance: String,
//    val bestSensitivityCompliance: String,
//    val sensitivityRecommendations: String,
//    val performanceValidationIssued: String,
//    val calibrationId: String
//)

//fun CalibrationMetalDetectorConveyorViewModel.toComplianceConfirmationUpdate()
//        : ComplianceConfirmationUpdate {
//
//    return ComplianceConfirmationUpdate(
//        sensitivityCompliance = sensitivityCompliance.value.toString(),
//        essentialRequirementCompliance = essentialRequirementCompliance.value.toString(),
//        failsafeCompliance = failsafeCompliance.value.toString(),
//        bestSensitivityCompliance = bestSensitivityCompliance.value.toString(),
//        sensitivityRecommendations = sensitivityRecommendations.value,
//        performanceValidationIssued = performanceValidationIssued.value.toString(),
//        calibrationId = calibrationId.value
//    )
//}

data class DetectionSettingLabelsUpdate(
    val detectionSetting1label: String,
    val detectionSetting2label: String,
    val detectionSetting3label: String,
    val detectionSetting4label: String,
    val detectionSetting5label: String,
    val detectionSetting6label: String,
    val detectionSetting7label: String,
    val detectionSetting8label: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toDetectionSettingLabelsUpdate()
        : DetectionSettingLabelsUpdate {

    return DetectionSettingLabelsUpdate(
        detectionSetting1label = detectionSetting1label.value,
        detectionSetting2label = detectionSetting2label.value,
        detectionSetting3label = detectionSetting3label.value,
        detectionSetting4label = detectionSetting4label.value,
        detectionSetting5label = detectionSetting5label.value,
        detectionSetting6label = detectionSetting6label.value,
        detectionSetting7label = detectionSetting7label.value,
        detectionSetting8label = detectionSetting8label.value,
        calibrationId = calibrationId.value
    )
}

data class CalibrationEndUpdate(
    val endDate: String,
    val calibrationId: String
)

fun CalibrationMetalDetectorConveyorViewModel.toCalibrationEndUpdate(): CalibrationEndUpdate {
    val now = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    return CalibrationEndUpdate(
        endDate = now,
        calibrationId = calibrationId.value
    )
}










































