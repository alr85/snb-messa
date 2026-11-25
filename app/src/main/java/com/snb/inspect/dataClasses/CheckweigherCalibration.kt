package com.snb.inspect.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "CheckweigherCalibrations")
class CheckweigherCalibrationLocal(
    @PrimaryKey var calibrationId: String = ""
) {
    // CALIBRATION SETUP
    var mapVersion: String = "CWC1.0"
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

    //---------------------------------------------------------------------------------Scale Details
    var loadcellType: String = ""
    var scaleInterval: String = ""
    var maxCapacity: String = ""
    var minCapacity: String = ""
    var accuracyClass: String = ""

    //--------------------------------------------------------------------------------System Details
    var beltWidth: String = ""
    var weighConveyorLength: String = ""
    var rejectType: String = ""
    var rejectTypeOther: String = ""
    var printerDataCapture: String = ""
    var rejectMode: String = ""

    //------------------------------------------------------------------------------System Checklist
    var beltCondition: String = ""
    var beltConditionComments: String = ""
    var safetyCircuitCondition: String = ""
    var safetyCircuitConditionComments: String = ""
    var guardCondition: String = ""
    var guardConditionComments: String = ""
    var vibrationCondition: String = ""
    var vibrationConditionComments: String = ""
    var weighTableObstruction: String = ""
    var weighTableObstructionComments: String = ""
    var productTransferCondition: String = ""
    var productTransferConditionComments: String = ""
    var machineStabilityCondition: String = ""
    var machineStabilityConditionComments: String = ""
    var systemChecklistEngineerNotes: String = ""

    //---------------------------------------------------------------------------------Infeed sensor
    var infeedSensorFitted: String = ""
    var infeedSensorDetail: String = ""
    var infeedSensorTestMethod: String = ""
    var infeedSensorTestMethodOther: String = ""
    var infeedSensorTestResult: String = ""
    var infeedSensorEngineerNotes: String = ""
    var infeedSensorLatched: String = ""
    var infeedSensorCR: String = ""

    //-------------------------------------------------------------------------Reject Confirm sensor
    var rejectConfirmSensorFitted: String = ""
    var rejectConfirmSensorDetail: String = ""
    var rejectConfirmSensorTestMethod: String = ""
    var rejectConfirmSensorTestMethodOther: String = ""
    var rejectConfirmSensorTestResult: String = ""
    var rejectConfirmSensorEngineerNotes: String = ""
    var rejectConfirmSensorLatched: String = ""
    var rejectConfirmSensorCR: String = ""

    //-------------------------------------------------------------------------------Bin Full sensor
    var binFullSensorFitted: String = ""
    var binFullSensorDetail: String = ""
    var binFullSensorTestMethod: String = ""
    var binFullSensorTestMethodOther: String = ""
    var binFullSensorTestResult: String = ""
    var binFullSensorEngineerNotes: String = ""
    var binFullSensorLatched: String = ""
    var binFullSensorCR: String = ""

    //--------------------------------------------------------------------------Air Pressure sensor
    var airPressureSensorFitted: String = ""
    var airPressureSensorDetail: String = ""
    var airPressureSensorTestMethod: String = ""
    var airPressureSensorTestMethodOther: String = ""
    var airPressureSensorTestResult: String = ""
    var airPressureSensorEngineerNotes: String = ""
    var airPressureSensorLatched: String = ""
    var airPressureSensorCR: String = ""

    //------------------------------------------------------------------------Bin Door Monitor Test
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

    //-----------------------------------------------------------------------------------Indicators
    var indicator1colour: String = ""
    var indicator1label: String = ""
    var indicator2colour: String = ""
    var indicator2label: String = ""
    var indicator3colour: String = ""
    var indicator3label: String = ""
    var indicator4colour: String = ""
    var indicator4label: String = ""
    var indicator5colour: String = ""
    var indicator5label: String = ""
    var indicator6colour: String = ""
    var indicator6label: String = ""
    var indicatorsEngineerNotes: String = ""

    //-------------------------------------------------------------------------Test Product Details
    var productDescription: String = ""
    var productLength: String = ""
    var productWidth: String = ""
    var productHeight: String = ""
    var grossWeight: String = ""
    var tareWeight: String = ""
    var productLibraryReference: String = ""

    // CALIBRATION TYPE & OIML METRIC SCOPE
    var calibrationType: String = "ROUTINE" // ROUTINE or COMPREHENSIVE

    //-----------------------------------------------------------------------Static Scale Reference
    var staticScaleMakeModel: String = ""
    var staticScaleCertRef: String = ""
    var staticScaleExpiryDate: String = ""
    var staticScaleManufacturer: String = ""
    var staticScaleModel: String = ""
    var staticScaleSerial: String = ""
    var staticScaleResolution: String = ""
    var staticScaleMaxCapacity: String = ""
    var staticScaleMinCapacity: String = ""
    var staticScaleCalDate: String = ""
    var staticScaleCalStatus: String = ""
    var staticScaleFunctionalCheck: String = "SATISFACTORY"
    var staticScaleSuitability: String = "SUITABLE" // SUITABLE, UNSUITABLE, LIMITATIONS
    var staticScaleRefMassCheckResult: String = ""
    var staticScaleLimitationFlags: String = ""

    //-------------------------------------------------------------------------Engineer Test Weight
    var engineerTestWeightId: Int? = null

    //-------------------------------------------------------------------------Operating Conditions
    var beltSpeed: String = ""
    var tareConfiguration: String = ""
    var isStaticRefGrossWeight: Boolean = false
    var operatingConditionsNotes: String = ""

    //-----------------------------------------------------------------------Dynamic Test 'As Found'
    var requiredPassesCount: Int = 30
    var nominalQuantityAsFound: String = ""
    var dynamicPassesAsFound: String = "" // Stored as comma separated values (up to 30)
    var staticScaleWeightAsFound: String = ""
    var checkweigherWeightAsFound: String = ""
    var dynamicMeanAsFound: String = ""
    var dynamicMeanErrorAsFound: String = ""
    var dynamicSampleStdDevAsFound: String = ""
    var dynamicSixSigmaAsFound: String = ""
    var additionalProductionWeightsJson: String = ""

    //-----------------------------------------------Dynamic Eccentricity (Off-Centre Weighing)
    var dynamicEccentricityTestWeightAsFound: String = ""
    var dynamicEccentricityFrontReadingAsFound: String = ""
    var dynamicEccentricityBackReadingAsFound: String = ""
    var dynamicEccentricityCentreReadingAsFound: String = ""
    var dynamicEccentricityResultAsFound: String = ""
    var dynamicEccentricityToleranceSource: String = "OIML_R51"
    var dynamicEccentricityManualType: String = "GRAMS"
    var dynamicEccentricityManualValue: String = ""
    var dynamicEccentricityDivisionMultiplier: String = "1"
    var dynamicEccentricityTnePackWeight: String = ""
    var dynamicEccentricityOimlClass: String = "XIII(1)"

    //------------------------------------------------------------------------Static Test 'As Found'
    var staticTestCountAsFound: Int = 1
    var staticTestSetsJsonAsFound: String = ""

    var offCentreTestWeightAsFound: String = ""
    var offCentreCentreAsFound: String = ""
    var offCentreCorner1AsFound: String = ""
    var offCentreCorner2AsFound: String = ""
    var offCentreCorner3AsFound: String = ""
    var offCentreCorner4AsFound: String = ""
    var offCentreLoadingTestResultAsFound: String = ""

    var repeatabilityTestWeightAsFound: String = ""
    var repeatabilityReading1AsFound: String = ""
    var repeatabilityReading2AsFound: String = ""
    var repeatabilityReading3AsFound: String = ""
    var repeatabilityTestResultAsFound: String = ""

    //------------------------------------------------------------------------------Adjustments Made
    var adjustmentsNotes: String = ""

    //------------------------------------------------------------------------Dynamic Test 'As Left'
    var nominalQuantityAsLeft: String = ""
    var dynamicPassesAsLeft: String = "" // Stored as comma separated values (up to 30)
    var staticScaleWeightAsLeft: String = ""
    var checkweigherWeightAsLeft: String = ""
    var dynamicMeanAsLeft: String = ""
    var dynamicMeanErrorAsLeft: String = ""
    var dynamicSampleStdDevAsLeft: String = ""
    var dynamicSixSigmaAsLeft: String = ""

    //-------------------------------------------------------------------------Static Test 'As Left'
    var offCentreLoadingTestResultAsLeft: String = ""
    var repeatabilityTestResultAsLeft: String = ""

    //--------------------------------------------------------------------Tolerances & Assessment
    var toleranceSource: String = "SNB_GUIDANCE" // MANUFACTURER, CUSTOMER, SNB_GUIDANCE
    var toleranceMode: String = "PACKAGED_GOODS_TNE" // MANUAL_SPEC, DIVISION_BASED, PACKAGED_GOODS_TNE, OIML_R51_BENCHMARK
    var manualMeanErrorValue: String = "1.0"
    var manualMeanErrorUnit: String = "GRAMS"
    var manualStdDevValue: String = "0.5"
    var manualStdDevUnit: String = "GRAMS"
    var staticDivisionMultiplier: String = "1.0"
    var dynamicDivisionMultiplier: String = "2.0"
    var nominalPackageWeightQn: String = ""
    var stdDevPercentageOfTne: String = "10.0"
    var designationFactorX: String = "1.0"
    var inspectionType: String = "INITIAL_VERIFICATION"
    var oimlComplianceModeEnabled: Boolean = false

    var manufacturerTne: String = ""
    var customerTne: String = ""
    var calculatedTne: String = ""
    var benchmark20PercentTne: String = ""
    var benchmark25PercentTne: String = ""
    var overallResultStatus: String = "PASS" // PASS, FAIL, FURTHER_ASSESSMENT_REQUIRED
    var exceedsZoiThreshold: Boolean = false
    var additionalSetPointAllowance: String = ""
    var engineerSetPointAssessmentNotes: String = ""
    var engineerSetPointAssessmentResolved: Boolean = false

    //----------------------------------------------------------------Static Linearity (5 Points)
    var staticLinRef0: String = ""
    var staticLinRef25: String = ""
    var staticLinRef50: String = ""
    var staticLinRef75: String = ""
    var staticLinRef100: String = ""
    var staticLinDisp0: String = ""
    var staticLinDisp25: String = ""
    var staticLinDisp50: String = ""
    var staticLinDisp75: String = ""
    var staticLinDisp100: String = ""

    //----------------------------------------------------------------Static Eccentricity (5 Pos)
    var eccentricityTestWeight: String = ""
    var eccentricityCentreReading: String = ""
    var eccentricityFrontLeftReading: String = ""
    var eccentricityFrontRightReading: String = ""
    var eccentricityRearLeftReading: String = ""
    var eccentricityRearRightReading: String = ""

    //----------------------------------------------------------------Dynamic Repeatability
    var dynamicRefWeight: String = ""
    var dynamicBeltSpeedUnit: String = "m/min"

    //------------------------------------------------------------------------Reject Verification
    var generalRejectTestResult: String = "PASS" // PASS, FAIL, NA
    var generalRejectNotes: String = ""
    var underweightRejectResult: String = "PASS"
    var overweightRejectResult: String = "PASS"
    var timingVerificationResult: String = "PASS"
    var t0TestRefWeight: String = ""
    var t0TestExpected: String = ""
    var t0TestActual: String = ""
    var t0TestResult: String = ""
    var t1TestRefWeight: String = ""
    var t1TestExpected: String = ""
    var t1TestActual: String = ""
    var t1TestResult: String = ""
    var t2TestRefWeight: String = ""
    var t2TestExpected: String = ""
    var t2TestActual: String = ""
    var t2TestResult: String = ""
    var overweightTestRefWeight: String = ""
    var overweightTestExpected: String = ""
    var overweightTestActual: String = ""
    var overweightTestResult: String = ""
}
