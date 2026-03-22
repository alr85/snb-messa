package com.snb.inspect.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "MetalDetectorConveyorCalibrations")
class MetalDetectorConveyorCalibrationLocal(
    @PrimaryKey val calibrationId: String
) {
    // CALIBRATION SETUP
    var mapVersion : String = "MDC1.0"
    var systemId: Int = 0
    var tempSystemId: Int = 0
    var cloudSystemId: Int = 0
    var systemTypeId: Int = 0
    var modelId: Int = 0
    var serialNumber: String = ""
    var engineerId: Int = 0
    var customerId: Int = 0
    var startDate: String = LocalDateTime.now().toString()
    var endDate: String = ""
    var isSynced: Boolean = false

    //-----------------------------------------------------------------------------Calibration Start
    var newLocation: String = ""
    var lastLocation: String = ""
    var canPerformCalibration: String = ""
    var reasonForNotCalibrating: String = ""
    var desiredCop: String = ""
    var startCalibrationNotes: String = ""
    var pvRequired: Boolean = false

    //------------------------------------------------------------------------------Product Settings
    var productDescription: String = ""
    var productLibraryReference: String = ""
    var productLibraryNumber: String = ""
    var productLength: String = ""
    var productWidth: String = ""
    var productHeight: String = ""
    var productDetailsEngineerNotes: String = ""

    //-------------------------------------------------------------------Detection Settings As Found
    var detectionSettingAsFound1: String = ""
    var detectionSettingAsFound2: String = ""
    var detectionSettingAsFound3: String = ""
    var detectionSettingAsFound4: String = ""
    var detectionSettingAsFound5: String = ""
    var detectionSettingAsFound6: String = ""
    var detectionSettingAsFound7: String = ""
    var detectionSettingAsFound8: String = ""
    var detectionSettingPvResult: String = ""
    var productPeakSignalAsFound: String = ""
    var detectionSettingAsFoundEngineerNotes: String = ""

    //----------------------------------------------------------------------Sensitivity Requirements
    var sensitivityRequirementFerrous: Double? = null
    var sensitivityRequirementNonFerrous: Double? = null
    var sensitivityRequirementStainless: Double? = null
    var sensitivityRequirementEngineerNotes: String = ""

    //--------------------------------------------------------------Sensitivities 'As Found' Ferrous
    var sensitivityAsFoundFerrous: String = ""
    var sampleCertificateNumberAsFoundFerrous: String = ""
    var detectRejectAsFoundFerrousLeading: String = ""
    var peakSignalAsFoundFerrousLeading: String = ""
    var detectRejectAsFoundFerrousMiddle: String = ""
    var peakSignalAsFoundFerrousMiddle: String = ""
    var detectRejectAsFoundFerrousTrailing: String = ""
    var peakSignalAsFoundFerrousTrailing: String = ""
    var ferrousAsFoundEngineerNotes: String = ""

    //----------------------------------------------------------Sensitivities 'As Found' Non-Ferrous
    var sensitivityAsFoundNonFerrous: String = ""
    var sampleCertificateNumberAsFoundNonFerrous: String = ""
    var detectRejectAsFoundNonFerrousLeading: String = ""
    var peakSignalAsFoundNonFerrousLeading: String = ""
    var detectRejectAsFoundNonFerrousMiddle: String = ""
    var peakSignalAsFoundNonFerrousMiddle: String = ""
    var detectRejectAsFoundNonFerrousTrailing: String = ""
    var peakSignalAsFoundNonFerrousTrailing: String = ""
    var nonFerrousAsFoundEngineerNotes: String = ""

    //------------------------------------------------------------Sensitivities 'As Found' Stainless
    var sensitivityAsFoundStainless: String = ""
    var sampleCertificateNumberAsFoundStainless: String = ""
    var detectRejectAsFoundStainlessLeading: String = ""
    var peakSignalAsFoundStainlessLeading: String = ""
    var detectRejectAsFoundStainlessMiddle: String = ""
    var peakSignalAsFoundStainlessMiddle: String = ""
    var detectRejectAsFoundStainlessTrailing: String = ""
    var peakSignalAsFoundStainlessTrailing: String = ""
    var stainlessAsFoundEngineerNotes: String = ""

    //--------------------------------------------------------------------------------Ferrous Result
    var sensitivityAsLeftFerrous: String = ""
    var sampleCertificateNumberFerrous: String = ""
    var detectRejectFerrousLeading: String = ""
    var detectRejectFerrousLeadingPeakSignal: String = ""
    var detectRejectFerrousMiddle: String = ""
    var detectRejectFerrousMiddlePeakSignal: String = ""
    var detectRejectFerrousTrailing: String = ""
    var detectRejectFerrousTrailingPeakSignal: String = ""
    var ferrousTestEngineerNotes: String = ""
    var ferrousTestPvResult: String = ""

    //----------------------------------------------------------------------------Non-Ferrous Result
    var sensitivityAsLeftNonFerrous: String = ""
    var sampleCertificateNumberNonFerrous: String = ""
    var detectRejectNonFerrousLeading: String = ""
    var detectRejectNonFerrousLeadingPeakSignal: String = ""
    var detectRejectNonFerrousMiddle: String = ""
    var detectRejectNonFerrousMiddlePeakSignal: String = ""
    var detectRejectNonFerrousTrailing: String = ""
    var detectRejectNonFerrousTrailingPeakSignal: String = ""
    var nonFerrousTestEngineerNotes: String = ""
    var nonFerrousTestPvResult: String = ""

    //------------------------------------------------------------------------------Stainless Result
    var sensitivityAsLeftStainless: String = ""
    var sampleCertificateNumberStainless: String = ""
    var detectRejectStainlessLeading: String = ""
    var detectRejectStainlessLeadingPeakSignal: String = ""
    var detectRejectStainlessMiddle: String = ""
    var detectRejectStainlessMiddlePeakSignal: String = ""
    var detectRejectStainlessTrailing: String = ""
    var detectRejectStainlessTrailingPeakSignal: String = ""
    var stainlessTestEngineerNotes: String = ""
    var stainlessTestPvResult: String = ""

    //----------------------------------------------------------------------------Large Metal Result
    var detectRejectLargeMetal: String = ""
    var sampleCertificateNumberLargeMetal: String = ""
    var largeMetalTestEngineerNotes: String = ""
    var largeMetalTestPvResult: String = ""

    //--------------------------------------------------------------------Detection Settings As Left
    var detectionSettingAsLeft1: String = ""
    var detectionSettingAsLeft2: String = ""
    var detectionSettingAsLeft3: String = ""
    var detectionSettingAsLeft4: String = ""
    var detectionSettingAsLeft5: String = ""
    var detectionSettingAsLeft6: String = ""
    var detectionSettingAsLeft7: String = ""
    var detectionSettingAsLeft8: String = ""
    var productPeakSignalAsLeft: String = ""
    var detectionSettingAsLeftEngineerNotes: String = ""

    //-------------------------------------------------------------------------------Reject Settings
    var rejectSynchronisationSetting: String = ""
    var rejectSynchronisationDetail: String = ""
    var rejectDelaySetting: String = ""
    var rejectDelayUnits: String = ""
    var rejectDurationSetting: String = ""
    var rejectDurationUnits: String = ""
    var rejectConfirmWindowSetting: String = ""
    var rejectConfirmWindowUnits: String = ""
    var rejectSettingsEngineerNotes: String = ""

    //------------------------------------------------------------------------------Conveyor Details
    var infeedBeltHeight: String = ""
    var outfeedBeltHeight: String = ""
    var conveyorLength: String = ""
    var conveyorHanding: String = ""
    var beltSpeed: String = ""
    var rejectDevice: String = ""
    var rejectDeviceOther: String = ""
    var conveyorDetailsEngineerNotes: String = ""

    //------------------------------------------------------------------------------System Checklist
    var beltCondition: String = ""
    var beltConditionComments: String = ""
    var guardCondition: String = ""
    var guardConditionComments: String = ""
    var safetyCircuitCondition: String = ""
    var safetyCircuitConditionComments: String = ""
    var linerCondition: String = ""
    var linerConditionComments: String = ""
    var cablesCondition: String = ""
    var cablesConditionComments: String = ""
    var screwsCondition: String = ""
    var screwsConditionComments: String = ""
    var systemChecklistEngineerNotes: String = ""

    //------------------------------------------------------------------------------------Indicators
    var indicator6colour: String = ""
    var indicator6label: String = ""
    var indicator5colour: String = ""
    var indicator5label: String = ""
    var indicator4colour: String = ""
    var indicator4label: String = ""
    var indicator3colour: String = ""
    var indicator3label: String = ""
    var indicator2colour: String = ""
    var indicator2label: String = ""
    var indicator1colour: String = ""
    var indicator1label: String = ""
    var indicatorsEngineerNotes: String = ""

    //---------------------------------------------------------------------------------Infeed sensor
    var infeedSensorFitted: String = ""
    var infeedSensorDetail: String = ""
    var infeedSensorTestMethod: String = ""
    var infeedSensorTestMethodOther: String = ""
    var infeedSensorTestResult: String = ""
    var infeedSensorEngineerNotes: String = ""
    var infeedSensorLatched: String = ""
    var infeedSensorCR: String = ""
    var infeedSensorTestPvResult: String = ""

    //-------------------------------------------------------------------------Reject Confirm sensor
    var rejectConfirmSensorFitted: String = ""
    var rejectConfirmSensorDetail: String = ""
    var rejectConfirmSensorTestMethod: String = ""
    var rejectConfirmSensorTestMethodOther: String = ""
    var rejectConfirmSensorTestResult: String = ""
    var rejectConfirmSensorEngineerNotes: String = ""
    var rejectConfirmSensorLatched: String = ""
    var rejectConfirmSensorCR: String = ""
    var rejectConfirmSensorStopPosition: String = ""
    var rejectConfirmSensorTestPvResult: String = ""

    //-------------------------------------------------------------------------------Bin Full sensor
    var binFullSensorFitted: String = ""
    var binFullSensorDetail: String = ""
    var binFullSensorTestMethod: String = ""
    var binFullSensorTestMethodOther: String = ""
    var binFullSensorTestResult: String = ""
    var binFullSensorEngineerNotes: String = ""
    var binFullSensorLatched: String = ""
    var binFullSensorCR: String = ""
    var binFullSensorTestPvResult: String = ""

    //---------------------------------------------------------------------------------Backup sensor
    var backupSensorFitted: String = ""
    var backupSensorDetail: String = ""
    var backupSensorTestMethod: String = ""
    var backupSensorTestMethodOther: String = ""
    var backupSensorTestResult: String = ""
    var backupSensorEngineerNotes: String = ""
    var backupSensorLatched: String = ""
    var backupSensorCR: String = ""
    var backupSensorTestPvResult: String = ""

    //--------------------------------------------------------------------------Air Pressure sensor
    var airPressureSensorFitted: String = ""
    var airPressureSensorDetail: String = ""
    var airPressureSensorTestMethod: String = ""
    var airPressureSensorTestMethodOther: String = ""
    var airPressureSensorTestResult: String = ""
    var airPressureSensorEngineerNotes: String = ""
    var airPressureSensorLatched: String = ""
    var airPressureSensorCR: String = ""
    var airPressureSensorTestPvResult: String = ""

    //------------------------------------------------------------------------Pack Check sensor Test
    var packCheckSensorFitted: String = ""
    var packCheckSensorDetail: String = ""
    var packCheckSensorTestMethod: String = ""
    var packCheckSensorTestMethodOther: String = ""
    var packCheckSensorTestResult: String = ""
    var packCheckSensorEngineerNotes: String = ""
    var packCheckSensorLatched: String = ""
    var packCheckSensorCR: String = ""
    var packCheckSensorTestPvResult: String = ""

    //-----------------------------------------------------------------------------Speed sensor Test
    var speedSensorFitted: String = ""
    var speedSensorDetail: String = ""
    var speedSensorTestMethod: String = ""
    var speedSensorTestMethodOther: String = ""
    var speedSensorTestResult: String = ""
    var speedSensorEngineerNotes: String = ""
    var speedSensorLatched: String = ""
    var speedSensorCR: String = ""
    var speedSensorTestPvResult: String = ""

    //----------------------------------------------------------------------Detect Notification Test
    var detectNotificationResult: String = ""
    var detectNotificationEngineerNotes: String = ""
    var detectNotificationTestPvResult: String = ""

    //-------------------------------------------------------------------------------------Bin Door
    var binDoorMonitorFitted: String = ""
    var binDoorMonitorDetail: String = ""
    var binDoorStatusAsFound: String = ""
    var binDoorUnlockedIndication: String = ""
    var binDoorOpenIndication: String = ""
    var binDoorTimeoutTimer: String = ""
    var binDoorTimeoutResult: String = ""
    var binDoorLatched: String = ""
    var binDoorCR: String = ""
    var binDoorEngineerNotes: String = ""
    var binDoorMonitorTestPvResult: String = ""

    //---------------------------------------------------------------------------------SME
    var operatorName: String = ""
    var operatorTestWitnessed: String = ""
    var operatorTestResultFerrous: String = ""
    var operatorTestResultNonFerrous: String = ""
    var operatorTestResultStainless: String = ""
    var operatorTestResultLargeMetal: String = ""
    var operatorTestResultCertNumberFerrous: String = ""
    var operatorTestResultCertNumberNonFerrous: String = ""
    var operatorTestResultCertNumberStainless: String = ""
    var operatorTestResultCertNumberLargeMetal: String = ""
    var smeName: String = ""
    var operatorTestWitnessedInfeed: String = ""
    var operatorTestWitnessedRejectConfirm: String = ""
    var operatorTestWitnessedBinFull: String = ""
    var operatorTestWitnessedBinDoor: String = ""
    var operatorTestWitnessedAirFail: String = ""
    var operatorTestWitnessedPackCheck: String = ""
    var operatorTestWitnessedSpeedSensor: String = ""
    var operatorTestWitnessedBackup: String = ""
    var smeEngineerNotes: String = ""
    var smeTestPvResult: String = ""

    //----------------------------------------------------------------------Detection Setting Labels
    var detectionSetting1label: String = ""
    var detectionSetting2label: String = ""
    var detectionSetting3label: String = ""
    var detectionSetting4label: String = ""
    var detectionSetting5label: String = ""
    var detectionSetting6label: String = ""
    var detectionSetting7label: String = ""
    var detectionSetting8label: String = ""

    // Sensitivity Access Restriction (legacy/shared)
    var sensitivityAccessRestriction: String = ""
}
