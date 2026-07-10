package com.snb.inspect.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "CheckweigherCalibrations")
class CheckweigherCalibrationLocal(
    @PrimaryKey val calibrationId: String
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

    //--------------------------------------------------------------------------------System Details
    var beltWidth: String = ""
    var weighConveyorLength: String = ""
    var rejectType: String = ""
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

    //-------------------------------------------------------------------------Test Product Details
    var productDescription: String = ""
    var productLength: String = ""
    var productWidth: String = ""
    var productHeight: String = ""
    var grossWeight: String = ""
    var tareWeight: String = ""
    var productLibraryReference: String = ""

    //-----------------------------------------------------------------------Static Scale Reference
    var staticScaleMakeModel: String = ""
    var staticScaleCertRef: String = ""
    var staticScaleExpiryDate: String = ""

    //-------------------------------------------------------------------------Engineer Test Weight
    var engineerTestWeightId: Int? = null

    //-----------------------------------------------------------------------Dynamic Test 'As Found'
    var nominalQuantityAsFound: String = ""
    var dynamicPassesAsFound: String = "" // Stored as comma separated values
    var staticScaleWeightAsFound: String = ""
    var checkweigherWeightAsFound: String = ""

    //------------------------------------------------------------------------Static Test 'As Found'
    var offCentreLoadingTestResultAsFound: String = ""
    var repeatabilityTestResultAsFound: String = ""

    //------------------------------------------------------------------------------Adjustments Made
    var adjustmentsNotes: String = ""

    //------------------------------------------------------------------------Dynamic Test 'As Left'
    var nominalQuantityAsLeft: String = ""
    var dynamicPassesAsLeft: String = "" // Stored as comma separated values
    var staticScaleWeightAsLeft: String = ""
    var checkweigherWeightAsLeft: String = ""

    //-------------------------------------------------------------------------Static Test 'As Left'
    var offCentreLoadingTestResultAsLeft: String = ""
    var repeatabilityTestResultAsLeft: String = ""
}
