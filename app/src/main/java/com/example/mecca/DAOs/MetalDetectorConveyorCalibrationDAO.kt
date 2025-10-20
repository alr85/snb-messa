package com.example.mecca.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mecca.dataClasses.MetalDetectorConveyorCalibrationLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface MetalDetectorConveyorCalibrationDAO {

    @Query("UPDATE MetalDetectorConveyorCalibrations SET cloudSystemId = :cloudId WHERE tempSystemId = :tempId")
    suspend fun updateCalibrationWithCloudId(tempId: Int?, cloudId: Int)

    @Query("UPDATE MetalDetectorConveyorCalibrations SET isSynced = :isSynced WHERE calibrationId = :calibrationId")
    suspend fun updateIsSynced(calibrationId: String, isSynced: Boolean)

//    @Query("SELECT * FROM MetalDetectorConveyorCalibrations WHERE (cloudSystemId = :systemId OR tempSystemId = :systemId) AND (endDate IS NULL OR endDate = '')")
//    fun getUnfinishedCalibrations(systemId: Int): Flow<List<MetalDetectorConveyorCalibrationLocal>>

    @Query("SELECT * FROM MetalDetectorConveyorCalibrations WHERE (endDate IS NULL OR endDate = '')")
    fun getAllUnfinishedCalibrations(): Flow<List<MetalDetectorConveyorCalibrationLocal>>

//    @Query("SELECT * FROM MetalDetectorConveyorCalibrations WHERE (cloudSystemId = :systemId OR tempSystemId = :systemId) AND (isSynced IS NULL OR isSynced = 0) AND endDate IS NOT NULL AND endDate != ''")
//    fun getPendingCalibrations(systemId: Int): Flow<List<MetalDetectorConveyorCalibrationLocal>>

    @Query("SELECT * FROM MetalDetectorConveyorCalibrations WHERE (isSynced IS NULL OR isSynced = 0) AND endDate IS NOT NULL AND endDate != ''")
    fun getAllPendingCalibrations(): Flow<List<MetalDetectorConveyorCalibrationLocal>>


//    @Query("SELECT * FROM MetalDetectorConveyorCalibrations WHERE (cloudSystemId = :systemId OR tempSystemId = :systemId) AND (isSynced = True OR isSynced = 1) AND endDate IS NOT NULL AND endDate != ''")
//    fun getCompletedCalibrations(systemId: Int): Flow<List<MetalDetectorConveyorCalibrationLocal>>

    @Query("SELECT * FROM MetalDetectorConveyorCalibrations WHERE (isSynced = True OR isSynced = 1) AND endDate IS NOT NULL AND endDate != ''")
    fun getAllCompletedCalibrations(): Flow<List<MetalDetectorConveyorCalibrationLocal>>


    @Query("SELECT * FROM MetalDetectorConveyorCalibrations WHERE calibrationId = :calibrationId")
    suspend fun getCalibrationById(calibrationId: String): MetalDetectorConveyorCalibrationLocal?

    @Query("DELETE FROM MetalDetectorConveyorCalibrations WHERE calibrationId = :calibrationId")
    suspend fun deleteCalibration(calibrationId: String)

    @Query("SELECT * FROM MetalDetectorConveyorCalibrations WHERE calibrationId = :calibrationId")
    fun getCalibrationForCsvConversion(calibrationId: String): MetalDetectorConveyorCalibrationLocal



    // Save Calibration Start to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET newLocation = :newLocation, " +
                "lastLocation = :lastLocation, " +
                "canPerformCalibration = :canPerformCalibration, " +
                "reasonForNotCalibrating = :reasonForNotCalibrating, " +
                "startCalibrationNotes = :startCalibrationNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateCalibrationStart(
        newLocation: String,
        lastLocation: String,
        canPerformCalibration: String,
        reasonForNotCalibrating: String,

        startCalibrationNotes: String,
        calibrationId: String
    )

    // Save Sensitivity Requirements to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET sensitivityRequirementFerrous = :sensitivityRequirementFerrous, " +
                "desiredCop = :desiredCop, " +
                "sensitivityRequirementNonFerrous = :sensitivityRequirementNonFerrous, " +
                "sensitivityRequirementStainless = :sensitivityRequirementStainless, " +
                "sensitivityRequirementEngineerNotes = :sensitivityRequirementEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateSensitivityRequirements(
        sensitivityRequirementFerrous: String,
        sensitivityRequirementNonFerrous: String,
        sensitivityRequirementStainless: String,
        sensitivityRequirementEngineerNotes: String,
        calibrationId: String,
        desiredCop: String,
    )


    // Save Product Details to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET productDescription = :productDescription, " +
                "productLibraryReference = :productLibraryReference, " +
                "productLibraryNumber = :productLibraryNumber, " +
                "productLength = :productLength," +
                "productWidth = :productWidth," +
                "productHeight = :productHeight," +
                "productDetailsEngineerNotes = :productDetailsEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateProductDetails(
        productDescription: String,
        productLibraryReference: String,
        productLibraryNumber: String,
        productLength: String,
        productWidth: String,
        productHeight: String,
        productDetailsEngineerNotes: String,
        calibrationId: String
    )




    // Save Detection Settings As Found to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET detectionSettingAsFound1 = :detectionSettingAsFound1, " +
                "detectionSettingAsFound2 = :detectionSettingAsFound2, " +
                "detectionSettingAsFound3 = :detectionSettingAsFound3, " +
                "detectionSettingAsFound4 = :detectionSettingAsFound4," +
                "detectionSettingAsFound5 = :detectionSettingAsFound5," +
                "detectionSettingAsFound6 = :detectionSettingAsFound6," +
                "detectionSettingAsFound7 = :detectionSettingAsFound7," +
                "detectionSettingAsFound8 = :detectionSettingAsFound8," +
                "detectionSettingAsFoundEngineerNotes = :detectionSettingAsFoundEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateDetectionSettingsAsFound(
        detectionSettingAsFound1: String,
        detectionSettingAsFound2: String,
        detectionSettingAsFound3: String,
        detectionSettingAsFound4: String,
        detectionSettingAsFound5: String,
        detectionSettingAsFound6: String,
        detectionSettingAsFound7: String,
        detectionSettingAsFound8: String,
        detectionSettingAsFoundEngineerNotes: String,
        calibrationId: String
    )


    // Save Sensitivities As Found to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET sensitivityAccessRestriction = :sensitivityAccessRestriction, " +
                "sensitivityAsFoundFerrous = :sensitivityAsFoundFerrous, " +
                "sensitivityAsFoundFerrousPeakSignal = :sensitivityAsFoundFerrousPeakSignal, " +
                "sensitivityAsFoundNonFerrous = :sensitivityAsFoundNonFerrous, " +
                "sensitivityAsFoundNonFerrousPeakSignal = :sensitivityAsFoundNonFerrousPeakSignal, " +
                "sensitivityAsFoundStainless = :sensitivityAsFoundStainless," +
                "sensitivityAsFoundStainlessPeakSignal = :sensitivityAsFoundStainlessPeakSignal," +
                "productPeakSignalAsFound = :productPeakSignalAsFound," +
                "sensitivityAsFoundEngineerNotes = :sensitivityAsFoundEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateSensitivitiesAsFound(
        sensitivityAccessRestriction: String,
        sensitivityAsFoundFerrous: String,
        sensitivityAsFoundFerrousPeakSignal: String,
        sensitivityAsFoundNonFerrous: String,
        sensitivityAsFoundNonFerrousPeakSignal: String,
        sensitivityAsFoundStainless: String,
        sensitivityAsFoundStainlessPeakSignal: String,
        productPeakSignalAsFound: String,
        sensitivityAsFoundEngineerNotes: String,
        calibrationId: String
    )

    // Save Ferrous Result As Found to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET sensitivityAsLeftFerrous = :sensitivityAsLeftFerrous, " +
                "sampleCertificateNumberFerrous = :sampleCertificateNumberFerrous, " +
                "detectRejectFerrousLeading = :detectRejectFerrousLeading, " +
                "detectRejectFerrousLeadingPeakSignal = :detectRejectFerrousLeadingPeakSignal," +
                "detectRejectFerrousMiddle = :detectRejectFerrousMiddle," +
                "detectRejectFerrousMiddlePeakSignal = :detectRejectFerrousMiddlePeakSignal, " +
                "detectRejectFerrousTrailing = :detectRejectFerrousTrailing," +
                "detectRejectFerrousTrailingPeakSignal = :detectRejectFerrousTrailingPeakSignal," +
                "ferrousTestEngineerNotes = :ferrousTestEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateFerrousResult(
        sensitivityAsLeftFerrous: String,
        sampleCertificateNumberFerrous: String,
        detectRejectFerrousLeading: String,
        detectRejectFerrousLeadingPeakSignal: String,
        detectRejectFerrousMiddle: String,
        detectRejectFerrousMiddlePeakSignal: String,
        detectRejectFerrousTrailing: String,
        detectRejectFerrousTrailingPeakSignal: String,
        ferrousTestEngineerNotes: String,
        calibrationId: String
    )

    // Save Stainless As Found to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET sensitivityAsLeftStainless = :sensitivityAsLeftStainless, " +
                "sampleCertificateNumberStainless = :sampleCertificateNumberStainless, " +
                "detectRejectStainlessLeading = :detectRejectStainlessLeading, " +
                "detectRejectStainlessLeadingPeakSignal = :detectRejectStainlessLeadingPeakSignal," +
                "detectRejectStainlessMiddle = :detectRejectStainlessMiddle," +
                "detectRejectStainlessMiddlePeakSignal = :detectRejectStainlessMiddlePeakSignal, " +
                "detectRejectStainlessTrailing = :detectRejectStainlessTrailing," +
                "detectRejectStainlessTrailingPeakSignal = :detectRejectStainlessTrailingPeakSignal," +
                "stainlessTestEngineerNotes = :stainlessTestEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateStainlessResult(
        sensitivityAsLeftStainless: String,
        sampleCertificateNumberStainless: String,
        detectRejectStainlessLeading: String,
        detectRejectStainlessLeadingPeakSignal: String,
        detectRejectStainlessMiddle: String,
        detectRejectStainlessMiddlePeakSignal: String,
        detectRejectStainlessTrailing: String,
        detectRejectStainlessTrailingPeakSignal: String,
        stainlessTestEngineerNotes: String,
        calibrationId: String
    )

    // Save Non Ferrous Result As Found to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET sensitivityAsLeftNonFerrous = :sensitivityAsLeftNonFerrous, " +
                "sampleCertificateNumberNonFerrous = :sampleCertificateNumberNonFerrous, " +
                "detectRejectNonFerrousLeading = :detectRejectNonFerrousLeading, " +
                "detectRejectNonFerrousLeadingPeakSignal = :detectRejectNonFerrousLeadingPeakSignal," +
                "detectRejectNonFerrousMiddle = :detectRejectNonFerrousMiddle," +
                "detectRejectNonFerrousMiddlePeakSignal = :detectRejectNonFerrousMiddlePeakSignal, " +
                "detectRejectNonFerrousTrailing = :detectRejectNonFerrousTrailing," +
                "detectRejectNonFerrousTrailingPeakSignal = :detectRejectNonFerrousTrailingPeakSignal," +
                "nonFerrousTestEngineerNotes = :nonFerrousTestEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateNonFerrousResult(
        sensitivityAsLeftNonFerrous: String,
        sampleCertificateNumberNonFerrous: String,
        detectRejectNonFerrousLeading: String,
        detectRejectNonFerrousLeadingPeakSignal: String,
        detectRejectNonFerrousMiddle: String,
        detectRejectNonFerrousMiddlePeakSignal: String,
        detectRejectNonFerrousTrailing: String,
        detectRejectNonFerrousTrailingPeakSignal: String,
        nonFerrousTestEngineerNotes: String,
        calibrationId: String
    )

    // Save Detection Setting As Left to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET detectionSettingAsLeft1 = :detectionSettingAsLeft1, " +
                "detectionSettingAsLeft2 = :detectionSettingAsLeft2, " +
                "detectionSettingAsLeft3 = :detectionSettingAsLeft3, " +
                "detectionSettingAsLeft4 = :detectionSettingAsLeft4," +
                "detectionSettingAsLeft5 = :detectionSettingAsLeft5," +
                "detectionSettingAsLeft6 = :detectionSettingAsLeft6," +
                "detectionSettingAsLeft7 = :detectionSettingAsLeft7," +
                "detectionSettingAsLeft8 = :detectionSettingAsLeft8," +
                "detectionSettingAsLeftEngineerNotes = :detectionSettingAsLeftEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateDetectionSettingAsLeft(
        detectionSettingAsLeft1: String,
        detectionSettingAsLeft2: String,
        detectionSettingAsLeft3: String,
        detectionSettingAsLeft4: String,
        detectionSettingAsLeft5: String,
        detectionSettingAsLeft6: String,
        detectionSettingAsLeft7: String,
        detectionSettingAsLeft8: String,
        detectionSettingAsLeftEngineerNotes: String,
        calibrationId: String
    )



    // Save Large Metal Result As Found to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET detectRejectLargeMetal = :detectRejectLargeMetal, " +
                "sampleCertificateNumberLargeMetal = :sampleCertificateNumberLargeMetal, " +
                "largeMetalTestEngineerNotes = :largeMetalTestEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateLargeMetalResult(
        detectRejectLargeMetal: String,
        sampleCertificateNumberLargeMetal: String,
        largeMetalTestEngineerNotes: String,
        calibrationId: String
    )

    // Save Reject Setting to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET rejectSynchronisationSetting = :rejectSynchronisationSetting, " +
                "rejectSynchronisationDetail = :rejectSynchronisationDetail, " +
                "rejectDelaySetting = :rejectDelaySetting, " +
                "rejectDelayUnits = :rejectDelayUnits," +
                "rejectDurationSetting = :rejectDurationSetting," +
                "rejectDurationUnits = :rejectDurationUnits, " +
                "rejectConfirmWindowSetting = :rejectConfirmWindowSetting," +
                "rejectConfirmWindowUnits = :rejectConfirmWindowUnits, " +
                "rejectSettingsEngineerNotes = :rejectSettingsEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateRejectSettings(
        rejectSynchronisationSetting: String,
        rejectSynchronisationDetail: String,
        rejectDelaySetting: String,
        rejectDelayUnits: String,
        rejectDurationSetting: String,
        rejectDurationUnits: String,
        rejectConfirmWindowSetting: String,
        rejectConfirmWindowUnits: String,
        rejectSettingsEngineerNotes: String,
        calibrationId: String
    )

    // Save Conveyor Details to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET infeedBeltHeight = :infeedBeltHeight, " +
                "outfeedBeltHeight = :outfeedBeltHeight, " +
                "conveyorLength = :conveyorLength, " +
                "conveyorHanding = :conveyorHanding, " +
                "beltSpeed = :beltSpeed," +
                "rejectDevice = :rejectDevice," +
                "rejectDeviceOther = :rejectDeviceOther, " +
                "conveyorDetailsEngineerNotes = :conveyorDetailsEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateConveyorDetails(
        infeedBeltHeight: String,
        outfeedBeltHeight: String,
        conveyorLength: String,
        conveyorHanding: String,
        beltSpeed: String,
        rejectDevice: String,
        rejectDeviceOther: String,
        conveyorDetailsEngineerNotes: String,
        calibrationId: String
    )

    // Save System Checklist to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET beltCondition = :beltCondition, " +
                "beltConditionComments = :beltConditionComments, " +
                "guardCondition = :guardCondition, " +
                "guardConditionComments = :guardConditionComments," +
                "safetyCircuitCondition = :safetyCircuitCondition," +
                "safetyCircuitConditionComments = :safetyCircuitConditionComments, " +
                "linerCondition = :linerCondition," +
                "linerConditionComments = :linerConditionComments," +
                "cablesCondition = :cablesCondition," +
                "cablesConditionComments = :cablesConditionComments, " +
                "screwsCondition = :screwsCondition, " +
                "screwsConditionComments = :screwsConditionComments, " +
                "systemChecklistEngineerNotes = :systemChecklistEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateSystemChecklist(
        beltCondition: String,
        beltConditionComments: String,
        guardCondition: String,
        guardConditionComments: String,
        safetyCircuitCondition: String,
        safetyCircuitConditionComments: String,
        linerCondition: String,
        linerConditionComments: String,
        cablesCondition: String,
        cablesConditionComments: String,
        screwsCondition: String,
        screwsConditionComments: String,
        systemChecklistEngineerNotes: String,
        calibrationId: String
    )

    // Save Indicators to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET indicator6label = :indicator6label, " +
                "indicator6colour = :indicator6colour, " +
                "indicator5label = :indicator5label, " +
                "indicator5colour = :indicator5colour," +
                "indicator4label = :indicator4label," +
                "indicator4colour = :indicator4colour, " +
                "indicator3label = :indicator3label," +
                "indicator3colour = :indicator3colour," +
                "indicator2label = :indicator2label," +
                "indicator2colour = :indicator2colour, " +
                "indicator1label = :indicator1label, " +
                "indicator1colour = :indicator1colour, " +
                "indicatorsEngineerNotes = :indicatorsEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateIndicators(
        indicator6label: String,
        indicator6colour: String,
        indicator5label: String,
        indicator5colour: String,
        indicator4label: String,
        indicator4colour: String,
        indicator3label: String,
        indicator3colour: String,
        indicator2label: String,
        indicator2colour: String,
        indicator1label: String,
        indicator1colour: String,
        indicatorsEngineerNotes: String,
        calibrationId: String
    )

    // Save Infeed Sensor to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET infeedSensorFitted = :infeedSensorFitted, " +
                "infeedSensorDetail = :infeedSensorDetail, " +
                "infeedSensorTestMethod = :infeedSensorTestMethod, " +
                "infeedSensorTestMethodOther = :infeedSensorTestMethodOther," +
                "infeedSensorTestResult = :infeedSensorTestResult," +
                "infeedSensorEngineerNotes = :infeedSensorEngineerNotes, " +
                "infeedSensorLatched = :infeedSensorLatched," +
                "infeedSensorCR = :infeedSensorCR " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateInfeedSensor(
        infeedSensorFitted: String,
        infeedSensorDetail: String,
        infeedSensorTestMethod: String,
        infeedSensorTestMethodOther: String,
        infeedSensorTestResult: String,
        infeedSensorEngineerNotes: String,
        infeedSensorLatched: String,
        infeedSensorCR: String,
        calibrationId: String
    )

    // Save RC Sensor to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET rejectConfirmSensorFitted = :rejectConfirmSensorFitted, " +
                "rejectConfirmSensorDetail = :rejectConfirmSensorDetail, " +
                "rejectConfirmSensorTestMethod = :rejectConfirmSensorTestMethod, " +
                "rejectConfirmSensorTestMethodOther = :rejectConfirmSensorTestMethodOther," +
                "rejectConfirmSensorTestResult = :rejectConfirmSensorTestResult," +
                "rejectConfirmSensorEngineerNotes = :rejectConfirmSensorEngineerNotes, " +
                "rejectConfirmSensorLatched = :rejectConfirmSensorLatched," +
                "rejectConfirmSensorCR = :rejectConfirmSensorCR," +
                "rejectConfirmSensorStopPosition = :rejectConfirmSensorStopPosition " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateRejectConfirmSensor(
        rejectConfirmSensorFitted: String,
        rejectConfirmSensorDetail: String,
        rejectConfirmSensorTestMethod: String,
        rejectConfirmSensorTestMethodOther: String,
        rejectConfirmSensorTestResult: String,
        rejectConfirmSensorEngineerNotes: String,
        rejectConfirmSensorLatched: String,
        rejectConfirmSensorCR: String,
        rejectConfirmSensorStopPosition: String,
        calibrationId: String
    )

    // Save BF Sensor to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET binFullSensorFitted = :binFullSensorFitted, " +
                "binFullSensorDetail = :binFullSensorDetail, " +
                "binFullSensorTestMethod = :binFullSensorTestMethod, " +
                "binFullSensorTestMethodOther = :binFullSensorTestMethodOther," +
                "binFullSensorTestResult = :binFullSensorTestResult," +
                "binFullSensorEngineerNotes = :binFullSensorEngineerNotes, " +
                "binFullSensorLatched = :binFullSensorLatched," +
                "binFullSensorCR = :binFullSensorCR " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateBinFullSensor(
        binFullSensorFitted: String,
        binFullSensorDetail: String,
        binFullSensorTestMethod: String,
        binFullSensorTestMethodOther: String,
        binFullSensorTestResult: String,
        binFullSensorEngineerNotes: String,
        binFullSensorLatched: String,
        binFullSensorCR: String,
        calibrationId: String
    )
    // Save BU Sensor to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET backupSensorFitted = :backupSensorFitted, " +
                "backupSensorDetail = :backupSensorDetail, " +
                "backupSensorTestMethod = :backupSensorTestMethod, " +
                "backupSensorTestMethodOther = :backupSensorTestMethodOther," +
                "backupSensorTestResult = :backupSensorTestResult," +
                "backupSensorEngineerNotes = :backupSensorEngineerNotes, " +
                "backupSensorLatched = :backupSensorLatched," +
                "backupSensorCR = :backupSensorCR " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateBackupSensor(
        backupSensorFitted: String,
        backupSensorDetail: String,
        backupSensorTestMethod: String,
        backupSensorTestMethodOther: String,
        backupSensorTestResult: String,
        backupSensorEngineerNotes: String,
        backupSensorLatched: String,
        backupSensorCR: String,
        calibrationId: String
    )

    // Save AP Sensor to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET airPressureSensorFitted = :airPressureSensorFitted, " +
                "airPressureSensorDetail = :airPressureSensorDetail, " +
                "airPressureSensorTestMethod = :airPressureSensorTestMethod, " +
                "airPressureSensorTestMethodOther = :airPressureSensorTestMethodOther," +
                "airPressureSensorTestResult = :airPressureSensorTestResult," +
                "airPressureSensorEngineerNotes = :airPressureSensorEngineerNotes, " +
                "airPressureSensorLatched = :airPressureSensorLatched," +
                "airPressureSensorCR = :airPressureSensorCR " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateAirPressureSensor(
        airPressureSensorFitted: String,
        airPressureSensorDetail: String,
        airPressureSensorTestMethod: String,
        airPressureSensorTestMethodOther: String,
        airPressureSensorTestResult: String,
        airPressureSensorEngineerNotes: String,
        airPressureSensorLatched: String,
        airPressureSensorCR: String,
        calibrationId: String
    )

    // Save PC Sensor to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET packCheckSensorFitted = :packCheckSensorFitted, " +
                "packCheckSensorDetail = :packCheckSensorDetail, " +
                "packCheckSensorTestMethod = :packCheckSensorTestMethod, " +
                "packCheckSensorTestMethodOther = :packCheckSensorTestMethodOther," +
                "packCheckSensorTestResult = :packCheckSensorTestResult," +
                "packCheckSensorEngineerNotes = :packCheckSensorEngineerNotes, " +
                "packCheckSensorLatched = :packCheckSensorLatched," +
                "packCheckSensorCR = :packCheckSensorCR " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updatePackCheckSensor(
        packCheckSensorFitted: String,
        packCheckSensorDetail: String,
        packCheckSensorTestMethod: String,
        packCheckSensorTestMethodOther: String,
        packCheckSensorTestResult: String,
        packCheckSensorEngineerNotes: String,
        packCheckSensorLatched: String,
        packCheckSensorCR: String,
        calibrationId: String
    )

    // Save Speed Sensor to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET speedSensorFitted = :speedSensorFitted, " +
                "speedSensorDetail = :speedSensorDetail, " +
                "speedSensorTestMethod = :speedSensorTestMethod, " +
                "speedSensorTestMethodOther = :speedSensorTestMethodOther," +
                "speedSensorTestResult = :speedSensorTestResult," +
                "speedSensorEngineerNotes = :speedSensorEngineerNotes, " +
                "speedSensorLatched = :speedSensorLatched," +
                "speedSensorCR = :speedSensorCR " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateSpeedSensor(
        speedSensorFitted: String,
        speedSensorDetail: String,
        speedSensorTestMethod: String,
        speedSensorTestMethodOther: String,
        speedSensorTestResult: String,
        speedSensorEngineerNotes: String,
        speedSensorLatched: String,
        speedSensorCR: String,
        calibrationId: String
    )

    // Save DetectNotification to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET detectNotificationResult = :detectNotificationResult, " +
                "detectNotificationEngineerNotes = :detectNotificationEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateDetectNotification(
        detectNotificationResult: String,
        detectNotificationEngineerNotes: String,
        calibrationId: String
    )

    // Save Bin Door to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET binDoorMonitorFitted = :binDoorMonitorFitted, " +
                "binDoorMonitorDetail = :binDoorMonitorDetail, " +
                "binDoorStatusAsFound = :binDoorStatusAsFound," +
                "binDoorUnlockedIndication = :binDoorUnlockedIndication," +
                "binDoorOpenIndication = :binDoorOpenIndication," +
                "binDoorTimeoutTimer = :binDoorTimeoutTimer," +
                "binDoorTimeoutResult = :binDoorTimeoutResult," +
                "binDoorLatched = :binDoorLatched," +
                "binDoorCR = :binDoorCR," +
                "binDoorEngineerNotes = :binDoorEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateBinDoorMonitor(
        binDoorMonitorFitted: String,
        binDoorMonitorDetail: String,
        binDoorStatusAsFound: String,
        binDoorUnlockedIndication: String,
        binDoorOpenIndication: String,
        binDoorTimeoutTimer: String,
        binDoorTimeoutResult: String,
        binDoorLatched: String,
        binDoorCR: String,
        binDoorEngineerNotes: String,
        calibrationId: String
    )

    // Save Operator Test to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET operatorName = :operatorName, " +
                "operatorTestWitnessed = :operatorTestWitnessed, " +
                "operatorTestResultFerrous = :operatorTestResultFerrous," +
                "operatorTestResultNonFerrous = :operatorTestResultNonFerrous," +
                "operatorTestResultStainless = :operatorTestResultStainless, " +
                "operatorTestResultLargeMetal = :operatorTestResultLargeMetal," +
                "operatorTestResultCertNumberFerrous = :operatorTestResultCertNumberFerrous," +
                "operatorTestResultCertNumberNonFerrous = :operatorTestResultCertNumberNonFerrous," +
                "operatorTestResultCertNumberStainless = :operatorTestResultCertNumberStainless," +
                "operatorTestResultCertNumberLargeMetal = :operatorTestResultCertNumberLargeMetal, " +
                "smeName =:smeName, " +
                "smeEngineerNotes = :smeEngineerNotes " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateOperatorTest(
        operatorName: String,
        operatorTestWitnessed: String,
        operatorTestResultFerrous: String,
        operatorTestResultNonFerrous: String,
        operatorTestResultStainless: String,
        operatorTestResultLargeMetal: String,
        operatorTestResultCertNumberFerrous: String,
        operatorTestResultCertNumberNonFerrous: String,
        operatorTestResultCertNumberStainless: String,
        operatorTestResultCertNumberLargeMetal: String,
        smeName: String,
        smeEngineerNotes: String,
        calibrationId: String
    )

    // Save Retailer Compliance to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET sensitivityCompliance = :sensitivityCompliance, " +
                "essentialRequirementCompliance = :essentialRequirementCompliance, " +
                "failsafeCompliance = :failsafeCompliance," +
                "bestSensitivityCompliance = :bestSensitivityCompliance," +
                "sensitivityRecommendations = :sensitivityRecommendations, " +
                "performanceValidationIssued = :performanceValidationIssued " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateComplianceConfirmation(
        sensitivityCompliance: String,
        essentialRequirementCompliance: String,
        failsafeCompliance: String,
        bestSensitivityCompliance: String,
        sensitivityRecommendations: String,
        performanceValidationIssued: String,
        calibrationId: String
    )

    // Save Detection Setting labels to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET detectionSetting1label = :detectionSetting1label, " +
                "detectionSetting2label = :detectionSetting2label, " +
                "detectionSetting3label = :detectionSetting3label, " +
                "detectionSetting4label = :detectionSetting4label," +
                "detectionSetting5label = :detectionSetting5label," +
                "detectionSetting6label = :detectionSetting6label, " +
                "detectionSetting7label = :detectionSetting7label," +
                "detectionSetting8label = :detectionSetting8label " +

                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateDetectionSettingLabels(
        detectionSetting1label: String,
        detectionSetting2label: String,
        detectionSetting3label: String,
        detectionSetting4label: String,
        detectionSetting5label: String,
        detectionSetting6label: String,
        detectionSetting7label: String,
        detectionSetting8label: String,
        calibrationId: String
    )

    // Save End to database
    @Query(
        "UPDATE MetalDetectorConveyorCalibrations " +
                "SET endDate = :endDate " +
                "WHERE calibrationId = :calibrationId"
    )
    suspend fun updateCalibrationEnd(
        endDate: String,
        calibrationId: String
    )
    


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCalibration(calibration: MetalDetectorConveyorCalibrationLocal)

    @Update
    suspend fun updateCalibration(calibration: MetalDetectorConveyorCalibrationLocal)
}