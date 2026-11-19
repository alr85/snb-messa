package com.snb.inspect.calibrationViewModels

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snb.inspect.ApiService
import com.snb.inspect.FetchResult
import com.snb.inspect.daos.CheckweigherCalibrationDAO
import com.snb.inspect.daos.MdModelsDAO
import com.snb.inspect.daos.MeasuringEquipmentDAO
import com.snb.inspect.daos.MetalDetectorSystemsDAO
import com.snb.inspect.daos.SystemTypeDAO
import com.snb.inspect.dataClasses.CheckweigherCalibrationLocal
import com.snb.inspect.dataClasses.MetalDetectorWithFullDetails
import com.snb.inspect.formModules.ConditionState
import com.snb.inspect.formModules.YesNoState
import com.snb.inspect.util.toSafeString
import com.snb.inspect.repositories.CheckweigherCalibrationRepository
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.util.InAppLogger
import com.snb.inspect.util.toConditionState
import com.snb.inspect.util.toYesNoState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.snb.inspect.daos.CwSystemsDAO
import com.snb.inspect.dataClasses.CheckweigherWithFullDetails
import com.snb.inspect.util.CheckweigherAccuracyCalculator
import com.snb.inspect.util.CheckweigherAccuracyResult
import com.snb.inspect.repositories.CheckweigherSystemsRepository

class CalibrationCheckweigherViewModel(
    private val context: android.content.Context,
    val engineerId: Int,
    private val calibrationDao: CheckweigherCalibrationDAO,
    private val calibrationRepository: CheckweigherCalibrationRepository,
    private val measuringEquipmentDAO: MeasuringEquipmentDAO,
    private val systemsRepository: CheckweigherSystemsRepository,
    private val cwSystemsDAO: CwSystemsDAO,
    val calibrationIdString: String,
    val system: CheckweigherWithFullDetails
) : ViewModel(), ICalibrationViewModel {

    private val _calibrationId = mutableStateOf(calibrationIdString)
    override val calibrationId: State<String> = _calibrationId

    override val serialNumber: State<String> = mutableStateOf(system.serialNumber)
    override val modelDescription: State<String> = mutableStateOf(system.modelDescription)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _systemId = mutableIntStateOf(system.id)
    private val _cloudSystemId = mutableIntStateOf(system.cloudId ?: 0)
    private val _tempSystemId = mutableIntStateOf(system.tempId)

    private val _currentScreenNextEnabled = MutableStateFlow(true)
    override val currentScreenNextEnabled = _currentScreenNextEnabled

    private val _screenValidities = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    override val screenValidities: StateFlow<Map<String, Boolean>> = _screenValidities

    init {
        viewModelScope.launch {
            _isLoading.value = true
            val existing = calibrationDao.getCalibrationById(calibrationId.value)
            if (existing != null && existing.systemId == system.id) {
                loadCalibration(existing)
            } else {
                saveNewCalibration()
            }
            revalidateAllScreens()
            _isLoading.value = false
        }
    }

    private fun loadCalibration(cal: CheckweigherCalibrationLocal) {
        _newLocation.value = cal.newLocation.ifBlank { system.lastLocation }
        _canPerformCalibration.value = cal.canPerformCalibration.toBoolean()
        _reasonForNotCalibrating.value = cal.reasonForNotCalibrating.split(", ").filter { it.isNotBlank() }

        _systemId.intValue = cal.systemId
        _tempSystemId.intValue = cal.tempSystemId

        // FIX: Ensure cloudSystemId is pulled from the fresh system object if the 
        // calibration record is still 0 (e.g. machine synced while this was incomplete)
        val effectiveCloudId = if (cal.cloudSystemId == 0 && system.cloudId != null) {
            system.cloudId
        } else {
            cal.cloudSystemId
        }
        _cloudSystemId.intValue = effectiveCloudId
        
        _calibrationType.value = cal.calibrationType.ifBlank { "ROUTINE" }

        // Scale Details
        _loadcellType.value = cal.loadcellType
        _scaleInterval.value = cal.scaleInterval.ifBlank { system.displayResolution }
        _maxCapacity.value = cal.maxCapacity.ifBlank { system.maxCapacity }
        _minCapacity.value = cal.minCapacity.ifBlank { system.minCapacity }
        _accuracyClass.value = cal.accuracyClass.ifBlank { system.accuracyClass }

        // System Details
        _beltWidth.value = cal.beltWidth
        _weighConveyorLength.value = cal.weighConveyorLength
        _rejectType.value = cal.rejectType
        _rejectTypeOther.value = cal.rejectTypeOther
        _printerDataCapture.value = cal.printerDataCapture
        _rejectMode.value = cal.rejectMode
        _beltSpeed.value = cal.beltSpeed
        _tareConfiguration.value = cal.tareConfiguration
        _operatingConditionsNotes.value = cal.operatingConditionsNotes

        // Checklist
        _beltCondition.value = cal.beltCondition.toConditionState()
        _beltConditionComments.value = cal.beltConditionComments
        _safetyCircuitCondition.value = cal.safetyCircuitCondition.toConditionState()
        _safetyCircuitConditionComments.value = cal.safetyCircuitConditionComments
        _guardCondition.value = cal.guardCondition.toConditionState()
        _guardConditionComments.value = cal.guardConditionComments
        _vibrationCondition.value = cal.vibrationCondition.toConditionState()
        _vibrationConditionComments.value = cal.vibrationConditionComments
        _weighTableObstruction.value = cal.weighTableObstruction.toConditionState()
        _weighTableObstructionComments.value = cal.weighTableObstructionComments
        _productTransferCondition.value = cal.productTransferCondition.toConditionState()
        _productTransferConditionComments.value = cal.productTransferConditionComments
        _machineStabilityCondition.value = cal.machineStabilityCondition.toConditionState()
        _machineStabilityConditionComments.value = cal.machineStabilityConditionComments
        _systemChecklistEngineerNotes.value = cal.systemChecklistEngineerNotes

        // Failsafes
        _infeedSensorFitted.value = cal.infeedSensorFitted.toYesNoState()
        _infeedSensorDetail.value = cal.infeedSensorDetail
        _infeedSensorTestMethod.value = cal.infeedSensorTestMethod
        _infeedSensorTestMethodOther.value = cal.infeedSensorTestMethodOther
        _infeedSensorTestResult.value = cal.infeedSensorTestResult.split(",").filter { it.isNotBlank() }
        _infeedSensorLatched.value = cal.infeedSensorLatched.toYesNoState()
        _infeedSensorCR.value = cal.infeedSensorCR.toYesNoState()
        _infeedSensorEngineerNotes.value = cal.infeedSensorEngineerNotes

        _rejectConfirmSensorFitted.value = cal.rejectConfirmSensorFitted.toYesNoState()
        _rejectConfirmSensorDetail.value = cal.rejectConfirmSensorDetail
        _rejectConfirmSensorTestMethod.value = cal.rejectConfirmSensorTestMethod
        _rejectConfirmSensorTestMethodOther.value = cal.rejectConfirmSensorTestMethodOther
        _rejectConfirmSensorTestResult.value = cal.rejectConfirmSensorTestResult.split(",").filter { it.isNotBlank() }
        _rejectConfirmSensorLatched.value = cal.rejectConfirmSensorLatched.toYesNoState()
        _rejectConfirmSensorCR.value = cal.rejectConfirmSensorCR.toYesNoState()
        _rejectConfirmSensorEngineerNotes.value = cal.rejectConfirmSensorEngineerNotes

        _binFullSensorFitted.value = cal.binFullSensorFitted.toYesNoState()
        _binFullSensorDetail.value = cal.binFullSensorDetail
        _binFullSensorTestMethod.value = cal.binFullSensorTestMethod
        _binFullSensorTestMethodOther.value = cal.binFullSensorTestMethodOther
        _binFullSensorTestResult.value = cal.binFullSensorTestResult.split(",").filter { it.isNotBlank() }
        _binFullSensorLatched.value = cal.binFullSensorLatched.toYesNoState()
        _binFullSensorCR.value = cal.binFullSensorCR.toYesNoState()
        _binFullSensorEngineerNotes.value = cal.binFullSensorEngineerNotes

        _airPressureSensorFitted.value = cal.airPressureSensorFitted.toYesNoState()
        _airPressureSensorDetail.value = cal.airPressureSensorDetail
        _airPressureSensorTestMethod.value = cal.airPressureSensorTestMethod
        _airPressureSensorTestMethodOther.value = cal.airPressureSensorTestMethodOther
        _airPressureSensorTestResult.value = cal.airPressureSensorTestResult.split(",").filter { it.isNotBlank() }
        _airPressureSensorLatched.value = cal.airPressureSensorLatched.toYesNoState()
        _airPressureSensorCR.value = cal.airPressureSensorCR.toYesNoState()
        _airPressureSensorEngineerNotes.value = cal.airPressureSensorEngineerNotes

        _binDoorMonitorFitted.value = cal.binDoorMonitorFitted.toYesNoState()
        _binDoorMonitorDetail.value = cal.binDoorMonitorDetail
        _binDoorStatusAsFound.value = cal.binDoorStatusAsFound
        _binDoorUnlockedIndication.value = cal.binDoorUnlockedIndication.split(",").filter { it.isNotBlank() }
        _binDoorOpenIndication.value = cal.binDoorOpenIndication.split(",").filter { it.isNotBlank() }
        _binDoorTimeoutTimer.value = cal.binDoorTimeoutTimer
        _binDoorTimeoutResult.value = cal.binDoorTimeoutResult.split(",").filter { it.isNotBlank() }
        _binDoorLatched.value = cal.binDoorLatched.toYesNoState()
        _binDoorCR.value = cal.binDoorCR.toYesNoState()
        _binDoorEngineerNotes.value = cal.binDoorEngineerNotes

        // Indicators
        _indicator1colour.value = cal.indicator1colour
        _indicator1label.value = cal.indicator1label
        _indicator2colour.value = cal.indicator2colour
        _indicator2label.value = cal.indicator2label
        _indicator3colour.value = cal.indicator3colour
        _indicator3label.value = cal.indicator3label
        _indicator4colour.value = cal.indicator4colour
        _indicator4label.value = cal.indicator4label
        _indicator5colour.value = cal.indicator5colour
        _indicator5label.value = cal.indicator5label
        _indicator6colour.value = cal.indicator6colour
        _indicator6label.value = cal.indicator6label
        _indicatorsEngineerNotes.value = cal.indicatorsEngineerNotes

        // Product Details
        _productDescription.value = cal.productDescription
        _productLength.value = cal.productLength
        _productWidth.value = cal.productWidth
        _productHeight.value = cal.productHeight
        _grossWeight.value = cal.grossWeight
        _tareWeight.value = cal.tareWeight
        _productLibraryReference.value = cal.productLibraryReference

        // Static Scale Ref
        _staticScaleMakeModel.value = cal.staticScaleMakeModel
        _staticScaleCertRef.value = cal.staticScaleCertRef
        _staticScaleExpiryDate.value = cal.staticScaleExpiryDate
        _staticScaleManufacturer.value = cal.staticScaleManufacturer
        _staticScaleModel.value = cal.staticScaleModel
        _staticScaleSerial.value = cal.staticScaleSerial
        _staticScaleResolution.value = cal.staticScaleResolution
        _staticScaleMaxCapacity.value = cal.staticScaleMaxCapacity
        _staticScaleMinCapacity.value = cal.staticScaleMinCapacity
        _staticScaleCalDate.value = cal.staticScaleCalDate
        _staticScaleCalStatus.value = cal.staticScaleCalStatus
        _staticScaleFunctionalCheck.value = cal.staticScaleFunctionalCheck.ifBlank { "SATISFACTORY" }
        _staticScaleSuitability.value = cal.staticScaleSuitability.ifBlank { "SUITABLE" }
        _staticScaleRefMassCheckResult.value = cal.staticScaleRefMassCheckResult
        _staticScaleLimitationFlags.value = cal.staticScaleLimitationFlags

        // Test Weight
        _engineerTestWeightId.value = cal.engineerTestWeightId

        // Results As Found
        _requiredPassesCount.value = if (cal.requiredPassesCount >= 10) cal.requiredPassesCount else 30
        _nominalQuantityAsFound.value = cal.nominalQuantityAsFound
        _dynamicPassesAsFound.value = parsePasses(cal.dynamicPassesAsFound)
        _staticScaleWeightAsFound.value = cal.staticScaleWeightAsFound
        _checkweigherWeightAsFound.value = cal.checkweigherWeightAsFound
        _offCentreLoadingTestResultAsFound.value = cal.offCentreLoadingTestResultAsFound
        _repeatabilityTestResultAsFound.value = cal.repeatabilityTestResultAsFound

        _adjustmentsNotes.value = cal.adjustmentsNotes

        // Results As Left
        _nominalQuantityAsLeft.value = cal.nominalQuantityAsLeft
        _dynamicPassesAsLeft.value = parsePasses(cal.dynamicPassesAsLeft)
        _staticScaleWeightAsLeft.value = cal.staticScaleWeightAsLeft
        _checkweigherWeightAsLeft.value = cal.checkweigherWeightAsLeft
        _offCentreLoadingTestResultAsLeft.value = cal.offCentreLoadingTestResultAsLeft
        _repeatabilityTestResultAsLeft.value = cal.repeatabilityTestResultAsLeft

        // Tolerances & Assessment
        _toleranceSource.value = cal.toleranceSource.ifBlank { "SNB_GUIDANCE" }
        _manufacturerTne.value = cal.manufacturerTne
        _customerTne.value = cal.customerTne
        _engineerSetPointAssessmentNotes.value = cal.engineerSetPointAssessmentNotes
        _engineerSetPointAssessmentResolved.value = cal.engineerSetPointAssessmentResolved

        // Reject Verification
        _generalRejectTestResult.value = cal.generalRejectTestResult.ifBlank { "PASS" }
        _generalRejectNotes.value = cal.generalRejectNotes
        _t0TestRefWeight.value = cal.t0TestRefWeight
        _t0TestExpected.value = cal.t0TestExpected
        _t0TestActual.value = cal.t0TestActual
        _t0TestResult.value = cal.t0TestResult
        _t1TestRefWeight.value = cal.t1TestRefWeight
        _t1TestExpected.value = cal.t1TestExpected
        _t1TestActual.value = cal.t1TestActual
        _t1TestResult.value = cal.t1TestResult
        _t2TestRefWeight.value = cal.t2TestRefWeight
        _t2TestExpected.value = cal.t2TestExpected
        _t2TestActual.value = cal.t2TestActual
        _t2TestResult.value = cal.t2TestResult
        _overweightTestRefWeight.value = cal.overweightTestRefWeight
        _overweightTestExpected.value = cal.overweightTestExpected
        _overweightTestActual.value = cal.overweightTestActual
        _overweightTestResult.value = cal.overweightTestResult
    }

    private fun saveNewCalibration() {
        val newCal = CheckweigherCalibrationLocal(calibrationId.value).apply {
            systemId = system.id
            tempSystemId = system.tempId
            cloudSystemId = system.cloudId ?: 0
            systemTypeId = system.systemTypeId
            modelId = system.modelId ?: 0
            serialNumber = system.serialNumber
            engineerId = this@CalibrationCheckweigherViewModel.engineerId
            customerId = system.customerId
            startDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            lastLocation = system.lastLocation
            newLocation = system.lastLocation
            maxCapacity = system.maxCapacity
            minCapacity = system.minCapacity
            scaleInterval = system.displayResolution
            accuracyClass = system.accuracyClass
        }
        viewModelScope.launch {
            calibrationRepository.insertNewCalibration(context, newCal)
        }
        loadCalibration(newCal)
    }

    override fun persistCurrentScreen(route: String) {
        viewModelScope.launch {
            val current = calibrationDao.getCalibrationById(calibrationId.value) ?: return@launch
            updateCalibrationFromState(current)
            calibrationRepository.updateCalibration(context, current)
        }
    }

    fun persistAllSections() {
        viewModelScope.launch {
            val current = calibrationDao.getCalibrationById(calibrationId.value) ?: return@launch
            updateCalibrationFromState(current)
            calibrationRepository.updateCalibration(context, current)
        }
    }

    private fun updateCalibrationFromState(cal: CheckweigherCalibrationLocal) {
        cal.newLocation = _newLocation.value
        cal.canPerformCalibration = _canPerformCalibration.value.toString()
        cal.reasonForNotCalibrating = _reasonForNotCalibrating.value.joinToString(", ")
        
        cal.loadcellType = _loadcellType.value
        cal.scaleInterval = _scaleInterval.value
        cal.maxCapacity = _maxCapacity.value
        cal.minCapacity = _minCapacity.value
        cal.accuracyClass = _accuracyClass.value
        
        cal.beltWidth = _beltWidth.value
        cal.weighConveyorLength = _weighConveyorLength.value
        cal.rejectType = _rejectType.value
        cal.rejectTypeOther = _rejectTypeOther.value
        cal.printerDataCapture = _printerDataCapture.value
        cal.rejectMode = _rejectMode.value
        
        cal.beltCondition = _beltCondition.value.toSafeString()
        cal.beltConditionComments = _beltConditionComments.value
        cal.safetyCircuitCondition = _safetyCircuitCondition.value.toSafeString()
        cal.safetyCircuitConditionComments = _safetyCircuitConditionComments.value
        cal.guardCondition = _guardCondition.value.toSafeString()
        cal.guardConditionComments = _guardConditionComments.value
        cal.vibrationCondition = _vibrationCondition.value.toSafeString()
        cal.vibrationConditionComments = _vibrationConditionComments.value
        cal.weighTableObstruction = _weighTableObstruction.value.toSafeString()
        cal.weighTableObstructionComments = _weighTableObstructionComments.value
        cal.productTransferCondition = _productTransferCondition.value.toSafeString()
        cal.productTransferConditionComments = _productTransferConditionComments.value
        cal.machineStabilityCondition = _machineStabilityCondition.value.toSafeString()
        cal.machineStabilityConditionComments = _machineStabilityConditionComments.value
        cal.systemChecklistEngineerNotes = _systemChecklistEngineerNotes.value
        
        cal.infeedSensorFitted = _infeedSensorFitted.value.toSafeString()
        cal.infeedSensorDetail = _infeedSensorDetail.value
        cal.infeedSensorTestMethod = _infeedSensorTestMethod.value
        cal.infeedSensorTestMethodOther = _infeedSensorTestMethodOther.value
        cal.infeedSensorTestResult = _infeedSensorTestResult.value.joinToString(",")
        cal.infeedSensorLatched = _infeedSensorLatched.value.toSafeString()
        cal.infeedSensorCR = _infeedSensorCR.value.toSafeString()
        cal.infeedSensorEngineerNotes = _infeedSensorEngineerNotes.value

        cal.rejectConfirmSensorFitted = _rejectConfirmSensorFitted.value.toSafeString()
        cal.rejectConfirmSensorDetail = _rejectConfirmSensorDetail.value
        cal.rejectConfirmSensorTestMethod = _rejectConfirmSensorTestMethod.value
        cal.rejectConfirmSensorTestMethodOther = _rejectConfirmSensorTestMethodOther.value
        cal.rejectConfirmSensorTestResult = _rejectConfirmSensorTestResult.value.joinToString(",")
        cal.rejectConfirmSensorLatched = _rejectConfirmSensorLatched.value.toSafeString()
        cal.rejectConfirmSensorCR = _rejectConfirmSensorCR.value.toSafeString()
        cal.rejectConfirmSensorEngineerNotes = _rejectConfirmSensorEngineerNotes.value

        cal.binFullSensorFitted = _binFullSensorFitted.value.toSafeString()
        cal.binFullSensorDetail = _binFullSensorDetail.value
        cal.binFullSensorTestMethod = _binFullSensorTestMethod.value
        cal.binFullSensorTestMethodOther = _binFullSensorTestMethodOther.value
        cal.binFullSensorTestResult = _binFullSensorTestResult.value.joinToString(",")
        cal.binFullSensorLatched = _binFullSensorLatched.value.toSafeString()
        cal.binFullSensorCR = _binFullSensorCR.value.toSafeString()
        cal.binFullSensorEngineerNotes = _binFullSensorEngineerNotes.value

        cal.airPressureSensorFitted = _airPressureSensorFitted.value.toSafeString()
        cal.airPressureSensorDetail = _airPressureSensorDetail.value
        cal.airPressureSensorTestMethod = _airPressureSensorTestMethod.value
        cal.airPressureSensorTestMethodOther = _airPressureSensorTestMethodOther.value
        cal.airPressureSensorTestResult = _airPressureSensorTestResult.value.joinToString(",")
        cal.airPressureSensorLatched = _airPressureSensorLatched.value.toSafeString()
        cal.airPressureSensorCR = _airPressureSensorCR.value.toSafeString()
        cal.airPressureSensorEngineerNotes = _airPressureSensorEngineerNotes.value

        cal.binDoorMonitorFitted = _binDoorMonitorFitted.value.toSafeString()
        cal.binDoorMonitorDetail = _binDoorMonitorDetail.value
        cal.binDoorStatusAsFound = _binDoorStatusAsFound.value
        cal.binDoorUnlockedIndication = _binDoorUnlockedIndication.value.joinToString(",")
        cal.binDoorOpenIndication = _binDoorOpenIndication.value.joinToString(",")
        cal.binDoorTimeoutTimer = _binDoorTimeoutTimer.value
        cal.binDoorTimeoutResult = _binDoorTimeoutResult.value.joinToString(",")
        cal.binDoorLatched = _binDoorLatched.value.toSafeString()
        cal.binDoorCR = _binDoorCR.value.toSafeString()
        cal.binDoorEngineerNotes = _binDoorEngineerNotes.value

        cal.indicator1colour = _indicator1colour.value
        cal.indicator1label = _indicator1label.value
        cal.indicator2colour = _indicator2colour.value
        cal.indicator2label = _indicator2label.value
        cal.indicator3colour = _indicator3colour.value
        cal.indicator3label = _indicator3label.value
        cal.indicator4colour = _indicator4colour.value
        cal.indicator4label = _indicator4label.value
        cal.indicator5colour = _indicator5colour.value
        cal.indicator5label = _indicator5label.value
        cal.indicator6colour = _indicator6colour.value
        cal.indicator6label = _indicator6label.value
        cal.indicatorsEngineerNotes = _indicatorsEngineerNotes.value
        
        cal.productDescription = _productDescription.value
        cal.productLength = _productLength.value
        cal.productWidth = _productWidth.value
        cal.productHeight = _productHeight.value
        cal.grossWeight = _grossWeight.value
        cal.tareWeight = _tareWeight.value
        cal.productLibraryReference = _productLibraryReference.value

        cal.calibrationType = _calibrationType.value
        cal.staticScaleMakeModel = _staticScaleMakeModel.value
        cal.staticScaleCertRef = _staticScaleCertRef.value
        cal.staticScaleExpiryDate = _staticScaleExpiryDate.value
        cal.staticScaleManufacturer = _staticScaleManufacturer.value
        cal.staticScaleModel = _staticScaleModel.value
        cal.staticScaleSerial = _staticScaleSerial.value
        cal.staticScaleResolution = _staticScaleResolution.value
        cal.staticScaleMaxCapacity = _staticScaleMaxCapacity.value
        cal.staticScaleMinCapacity = _staticScaleMinCapacity.value
        cal.staticScaleCalDate = _staticScaleCalDate.value
        cal.staticScaleCalStatus = _staticScaleCalStatus.value
        cal.staticScaleFunctionalCheck = _staticScaleFunctionalCheck.value
        cal.staticScaleSuitability = _staticScaleSuitability.value
        cal.staticScaleRefMassCheckResult = _staticScaleRefMassCheckResult.value
        cal.staticScaleLimitationFlags = _staticScaleLimitationFlags.value

        cal.engineerTestWeightId = _engineerTestWeightId.value
        cal.beltSpeed = _beltSpeed.value
        cal.tareConfiguration = _tareConfiguration.value
        cal.operatingConditionsNotes = _operatingConditionsNotes.value

        cal.requiredPassesCount = _requiredPassesCount.value
        cal.nominalQuantityAsFound = _nominalQuantityAsFound.value
        cal.dynamicPassesAsFound = _dynamicPassesAsFound.value.joinToString(",")
        cal.staticScaleWeightAsFound = _staticScaleWeightAsFound.value
        cal.checkweigherWeightAsFound = _checkweigherWeightAsFound.value
        val resFound = evaluatedCalculationResultAsFound.value
        cal.dynamicMeanAsFound = resFound?.mean?.toString() ?: ""
        cal.dynamicMeanErrorAsFound = resFound?.meanDynamicError?.toString() ?: ""
        cal.dynamicSampleStdDevAsFound = resFound?.sampleStdDev?.toString() ?: ""
        cal.dynamicSixSigmaAsFound = resFound?.sixSigmaZoi?.toString() ?: ""
        cal.offCentreLoadingTestResultAsFound = _offCentreLoadingTestResultAsFound.value
        cal.repeatabilityTestResultAsFound = _repeatabilityTestResultAsFound.value

        cal.adjustmentsNotes = _adjustmentsNotes.value

        cal.nominalQuantityAsLeft = _nominalQuantityAsLeft.value
        cal.dynamicPassesAsLeft = _dynamicPassesAsLeft.value.joinToString(",")
        cal.staticScaleWeightAsLeft = _staticScaleWeightAsLeft.value
        cal.checkweigherWeightAsLeft = _checkweigherWeightAsLeft.value
        val resLeft = evaluatedCalculationResultAsLeft.value
        cal.dynamicMeanAsLeft = resLeft?.mean?.toString() ?: ""
        cal.dynamicMeanErrorAsLeft = resLeft?.meanDynamicError?.toString() ?: ""
        cal.dynamicSampleStdDevAsLeft = resLeft?.sampleStdDev?.toString() ?: ""
        cal.dynamicSixSigmaAsLeft = resLeft?.sixSigmaZoi?.toString() ?: ""
        cal.offCentreLoadingTestResultAsLeft = _offCentreLoadingTestResultAsLeft.value
        cal.repeatabilityTestResultAsLeft = _repeatabilityTestResultAsLeft.value

        cal.toleranceSource = _toleranceSource.value
        cal.manufacturerTne = _manufacturerTne.value
        cal.customerTne = _customerTne.value
        cal.calculatedTne = resFound?.tne?.toString() ?: ""
        cal.benchmark20PercentTne = resFound?.benchmark20PercentTne?.toString() ?: ""
        cal.benchmark25PercentTne = resFound?.benchmark25PercentTne?.toString() ?: ""
        cal.overallResultStatus = resFound?.overallStatus?.name ?: "PASS"
        cal.exceedsZoiThreshold = resFound?.exceeds25PercentTneLimit ?: false
        cal.additionalSetPointAllowance = resFound?.additionalSetPointAllowance?.toString() ?: ""
        cal.engineerSetPointAssessmentNotes = _engineerSetPointAssessmentNotes.value
        cal.engineerSetPointAssessmentResolved = _engineerSetPointAssessmentResolved.value

        cal.generalRejectTestResult = _generalRejectTestResult.value
        cal.generalRejectNotes = _generalRejectNotes.value
        cal.t0TestRefWeight = _t0TestRefWeight.value
        cal.t0TestExpected = _t0TestExpected.value
        cal.t0TestActual = _t0TestActual.value
        cal.t0TestResult = _t0TestResult.value
        cal.t1TestRefWeight = _t1TestRefWeight.value
        cal.t1TestExpected = _t1TestExpected.value
        cal.t1TestActual = _t1TestActual.value
        cal.t1TestResult = _t1TestResult.value
        cal.t2TestRefWeight = _t2TestRefWeight.value
        cal.t2TestExpected = _t2TestExpected.value
        cal.t2TestActual = _t2TestActual.value
        cal.t2TestResult = _t2TestResult.value
        cal.overweightTestRefWeight = _overweightTestRefWeight.value
        cal.overweightTestExpected = _overweightTestExpected.value
        cal.overweightTestActual = _overweightTestActual.value
        cal.overweightTestResult = _overweightTestResult.value
    }

    // region STATE VARIABLES

    private val _calibrationType = mutableStateOf("ROUTINE")
    val calibrationType: State<String> = _calibrationType
    fun setCalibrationType(v: String) { _calibrationType.value = v }

    private val _newLocation = mutableStateOf("")
    val newLocation: State<String> = _newLocation
    fun setNewLocation(v: String) { _newLocation.value = v }

    private val _canPerformCalibration = mutableStateOf(true)
    val canPerformCalibration: State<Boolean> = _canPerformCalibration
    fun setCanPerformCalibration(v: Boolean) { _canPerformCalibration.value = v }

    private val _reasonForNotCalibrating = mutableStateOf<List<String>>(emptyList())
    val reasonForNotCalibrating: State<List<String>> = _reasonForNotCalibrating
    fun setReasonForNotCalibrating(v: List<String>) { _reasonForNotCalibrating.value = v }

    // Scale Details
    private val _loadcellType = mutableStateOf("")
    val loadcellType: State<String> = _loadcellType
    fun setLoadcellType(v: String) { _loadcellType.value = v }

    private val _scaleInterval = mutableStateOf("")
    val scaleInterval: State<String> = _scaleInterval
    fun setScaleInterval(v: String) { _scaleInterval.value = v }

    private val _maxCapacity = mutableStateOf("")
    val maxCapacity: State<String> = _maxCapacity
    fun setMaxCapacity(v: String) { _maxCapacity.value = v }

    private val _minCapacity = mutableStateOf("")
    val minCapacity: State<String> = _minCapacity
    fun setMinCapacity(v: String) { _minCapacity.value = v }

    private val _accuracyClass = mutableStateOf("")
    val accuracyClass: State<String> = _accuracyClass
    fun setAccuracyClass(v: String) { _accuracyClass.value = v }

    // System Details
    private val _beltWidth = mutableStateOf("")
    val beltWidth: State<String> = _beltWidth
    fun setBeltWidth(v: String) { _beltWidth.value = v }

    private val _weighConveyorLength = mutableStateOf("")
    val weighConveyorLength: State<String> = _weighConveyorLength
    fun setWeighConveyorLength(v: String) { _weighConveyorLength.value = v }

    private val _rejectType = mutableStateOf("")
    val rejectType: State<String> = _rejectType
    fun setRejectType(v: String) { _rejectType.value = v }

    private val _rejectTypeOther = mutableStateOf("")
    val rejectTypeOther: State<String> = _rejectTypeOther
    fun setRejectTypeOther(v: String) { _rejectTypeOther.value = v }

    private val _printerDataCapture = mutableStateOf("")
    val printerDataCapture: State<String> = _printerDataCapture
    fun setPrinterDataCapture(v: String) { _printerDataCapture.value = v }

    private val _rejectMode = mutableStateOf("")
    val rejectMode: State<String> = _rejectMode
    fun setRejectMode(v: String) { _rejectMode.value = v }

    // Checklist
    private val _beltCondition = mutableStateOf(ConditionState.UNSPECIFIED)
    val beltCondition: State<ConditionState> = _beltCondition
    fun setBeltCondition(v: ConditionState) { _beltCondition.value = v }
    private val _beltConditionComments = mutableStateOf("")
    val beltConditionComments: State<String> = _beltConditionComments
    fun setBeltConditionComments(v: String) { _beltConditionComments.value = v }

    private val _safetyCircuitCondition = mutableStateOf(ConditionState.UNSPECIFIED)
    val safetyCircuitCondition: State<ConditionState> = _safetyCircuitCondition
    fun setSafetyCircuitCondition(v: ConditionState) { _safetyCircuitCondition.value = v }
    private val _safetyCircuitConditionComments = mutableStateOf("")
    val safetyCircuitConditionComments: State<String> = _safetyCircuitConditionComments
    fun setSafetyCircuitConditionComments(v: String) { _safetyCircuitConditionComments.value = v }

    private val _guardCondition = mutableStateOf(ConditionState.UNSPECIFIED)
    val guardCondition: State<ConditionState> = _guardCondition
    fun setGuardCondition(v: ConditionState) { _guardCondition.value = v }
    private val _guardConditionComments = mutableStateOf("")
    val guardConditionComments: State<String> = _guardConditionComments
    fun setGuardConditionComments(v: String) { _guardConditionComments.value = v }

    private val _vibrationCondition = mutableStateOf(ConditionState.UNSPECIFIED)
    val vibrationCondition: State<ConditionState> = _vibrationCondition
    fun setVibrationCondition(v: ConditionState) { _vibrationCondition.value = v }
    private val _vibrationConditionComments = mutableStateOf("")
    val vibrationConditionComments: State<String> = _vibrationConditionComments
    fun setVibrationConditionComments(v: String) { _vibrationConditionComments.value = v }

    private val _weighTableObstruction = mutableStateOf(ConditionState.UNSPECIFIED)
    val weighTableObstruction: State<ConditionState> = _weighTableObstruction
    fun setWeighTableObstruction(v: ConditionState) { _weighTableObstruction.value = v }
    private val _weighTableObstructionComments = mutableStateOf("")
    val weighTableObstructionComments: State<String> = _weighTableObstructionComments
    fun setWeighTableObstructionComments(v: String) { _weighTableObstructionComments.value = v }

    private val _productTransferCondition = mutableStateOf(ConditionState.UNSPECIFIED)
    val productTransferCondition: State<ConditionState> = _productTransferCondition
    fun setProductTransferCondition(v: ConditionState) { _productTransferCondition.value = v }
    private val _productTransferConditionComments = mutableStateOf("")
    val productTransferConditionComments: State<String> = _productTransferConditionComments
    fun setProductTransferConditionComments(v: String) { _productTransferConditionComments.value = v }

    private val _machineStabilityCondition = mutableStateOf(ConditionState.UNSPECIFIED)
    val machineStabilityCondition: State<ConditionState> = _machineStabilityCondition
    fun setMachineStabilityCondition(v: ConditionState) { _machineStabilityCondition.value = v }
    private val _machineStabilityConditionComments = mutableStateOf("")
    val machineStabilityConditionComments: State<String> = _machineStabilityConditionComments
    fun setMachineStabilityConditionComments(v: String) { _machineStabilityConditionComments.value = v }

    private val _systemChecklistEngineerNotes = mutableStateOf("")
    val systemChecklistEngineerNotes: State<String> = _systemChecklistEngineerNotes
    fun setSystemChecklistEngineerNotes(v: String) { _systemChecklistEngineerNotes.value = v }

    // Failsafes
    private val _infeedSensorFitted = mutableStateOf(YesNoState.UNSPECIFIED)
    val infeedSensorFitted: State<YesNoState> = _infeedSensorFitted
    fun setInfeedSensorFitted(v: YesNoState) { _infeedSensorFitted.value = v }

    private val _infeedSensorDetail = mutableStateOf("")
    val infeedSensorDetail: State<String> = _infeedSensorDetail
    fun setInfeedSensorDetail(v: String) { _infeedSensorDetail.value = v }

    private val _infeedSensorTestMethod = mutableStateOf("")
    val infeedSensorTestMethod: State<String> = _infeedSensorTestMethod
    fun setInfeedSensorTestMethod(v: String) { _infeedSensorTestMethod.value = v }

    private val _infeedSensorTestMethodOther = mutableStateOf("")
    val infeedSensorTestMethodOther: State<String> = _infeedSensorTestMethodOther
    fun setInfeedSensorTestMethodOther(v: String) { _infeedSensorTestMethodOther.value = v }

    private val _infeedSensorTestResult = mutableStateOf<List<String>>(emptyList())
    val infeedSensorTestResult: State<List<String>> = _infeedSensorTestResult
    fun setInfeedSensorTestResult(v: List<String>) { _infeedSensorTestResult.value = v }

    private val _infeedSensorLatched = mutableStateOf(YesNoState.UNSPECIFIED)
    val infeedSensorLatched: State<YesNoState> = _infeedSensorLatched
    fun setInfeedSensorLatched(v: YesNoState) { _infeedSensorLatched.value = v }

    private val _infeedSensorCR = mutableStateOf(YesNoState.UNSPECIFIED)
    val infeedSensorCR: State<YesNoState> = _infeedSensorCR
    fun setInfeedSensorCR(v: YesNoState) { _infeedSensorCR.value = v }

    private val _infeedSensorEngineerNotes = mutableStateOf("")
    val infeedSensorEngineerNotes: State<String> = _infeedSensorEngineerNotes
    fun setInfeedSensorEngineerNotes(v: String) { _infeedSensorEngineerNotes.value = v }

    // Reject Confirm
    private val _rejectConfirmSensorFitted = mutableStateOf(YesNoState.UNSPECIFIED)
    val rejectConfirmSensorFitted: State<YesNoState> = _rejectConfirmSensorFitted
    fun setRejectConfirmSensorFitted(v: YesNoState) { _rejectConfirmSensorFitted.value = v }

    private val _rejectConfirmSensorDetail = mutableStateOf("")
    val rejectConfirmSensorDetail: State<String> = _rejectConfirmSensorDetail
    fun setRejectConfirmSensorDetail(v: String) { _rejectConfirmSensorDetail.value = v }

    private val _rejectConfirmSensorTestMethod = mutableStateOf("")
    val rejectConfirmSensorTestMethod: State<String> = _rejectConfirmSensorTestMethod
    fun setRejectConfirmSensorTestMethod(v: String) { _rejectConfirmSensorTestMethod.value = v }

    private val _rejectConfirmSensorTestMethodOther = mutableStateOf("")
    val rejectConfirmSensorTestMethodOther: State<String> = _rejectConfirmSensorTestMethodOther
    fun setRejectConfirmSensorTestMethodOther(v: String) { _rejectConfirmSensorTestMethodOther.value = v }

    private val _rejectConfirmSensorTestResult = mutableStateOf<List<String>>(emptyList())
    val rejectConfirmSensorTestResult: State<List<String>> = _rejectConfirmSensorTestResult
    fun setRejectConfirmSensorTestResult(v: List<String>) { _rejectConfirmSensorTestResult.value = v }

    private val _rejectConfirmSensorLatched = mutableStateOf(YesNoState.UNSPECIFIED)
    val rejectConfirmSensorLatched: State<YesNoState> = _rejectConfirmSensorLatched
    fun setRejectConfirmSensorLatched(v: YesNoState) { _rejectConfirmSensorLatched.value = v }

    private val _rejectConfirmSensorCR = mutableStateOf(YesNoState.UNSPECIFIED)
    val rejectConfirmSensorCR: State<YesNoState> = _rejectConfirmSensorCR
    fun setRejectConfirmSensorCR(v: YesNoState) { _rejectConfirmSensorCR.value = v }

    private val _rejectConfirmSensorEngineerNotes = mutableStateOf("")
    val rejectConfirmSensorEngineerNotes: State<String> = _rejectConfirmSensorEngineerNotes
    fun setRejectConfirmSensorEngineerNotes(v: String) { _rejectConfirmSensorEngineerNotes.value = v }

    // Bin Full
    private val _binFullSensorFitted = mutableStateOf(YesNoState.UNSPECIFIED)
    val binFullSensorFitted: State<YesNoState> = _binFullSensorFitted
    fun setBinFullSensorFitted(v: YesNoState) { _binFullSensorFitted.value = v }

    private val _binFullSensorDetail = mutableStateOf("")
    val binFullSensorDetail: State<String> = _binFullSensorDetail
    fun setBinFullSensorDetail(v: String) { _binFullSensorDetail.value = v }

    private val _binFullSensorTestMethod = mutableStateOf("")
    val binFullSensorTestMethod: State<String> = _binFullSensorTestMethod
    fun setBinFullSensorTestMethod(v: String) { _binFullSensorTestMethod.value = v }

    private val _binFullSensorTestMethodOther = mutableStateOf("")
    val binFullSensorTestMethodOther: State<String> = _binFullSensorTestMethodOther
    fun setBinFullSensorTestMethodOther(v: String) { _binFullSensorTestMethodOther.value = v }

    private val _binFullSensorTestResult = mutableStateOf<List<String>>(emptyList())
    val binFullSensorTestResult: State<List<String>> = _binFullSensorTestResult
    fun setBinFullSensorTestResult(v: List<String>) { _binFullSensorTestResult.value = v }

    private val _binFullSensorLatched = mutableStateOf(YesNoState.UNSPECIFIED)
    val binFullSensorLatched: State<YesNoState> = _binFullSensorLatched
    fun setBinFullSensorLatched(v: YesNoState) { _binFullSensorLatched.value = v }

    private val _binFullSensorCR = mutableStateOf(YesNoState.UNSPECIFIED)
    val binFullSensorCR: State<YesNoState> = _binFullSensorCR
    fun setBinFullSensorCR(v: YesNoState) { _binFullSensorCR.value = v }

    private val _binFullSensorEngineerNotes = mutableStateOf("")
    val binFullSensorEngineerNotes: State<String> = _binFullSensorEngineerNotes
    fun setBinFullSensorEngineerNotes(v: String) { _binFullSensorEngineerNotes.value = v }

    // Air Pressure
    private val _airPressureSensorFitted = mutableStateOf(YesNoState.UNSPECIFIED)
    val airPressureSensorFitted: State<YesNoState> = _airPressureSensorFitted
    fun setAirPressureSensorFitted(v: YesNoState) { _airPressureSensorFitted.value = v }

    private val _airPressureSensorDetail = mutableStateOf("")
    val airPressureSensorDetail: State<String> = _airPressureSensorDetail
    fun setAirPressureSensorDetail(v: String) { _airPressureSensorDetail.value = v }

    private val _airPressureSensorTestMethod = mutableStateOf("")
    val airPressureSensorTestMethod: State<String> = _airPressureSensorTestMethod
    fun setAirPressureSensorTestMethod(v: String) { _airPressureSensorTestMethod.value = v }

    private val _airPressureSensorTestMethodOther = mutableStateOf("")
    val airPressureSensorTestMethodOther: State<String> = _airPressureSensorTestMethodOther
    fun setAirPressureSensorTestMethodOther(v: String) { _airPressureSensorTestMethodOther.value = v }

    private val _airPressureSensorTestResult = mutableStateOf<List<String>>(emptyList())
    val airPressureSensorTestResult: State<List<String>> = _airPressureSensorTestResult
    fun setAirPressureSensorTestResult(v: List<String>) { _airPressureSensorTestResult.value = v }

    private val _airPressureSensorLatched = mutableStateOf(YesNoState.UNSPECIFIED)
    val airPressureSensorLatched: State<YesNoState> = _airPressureSensorLatched
    fun setAirPressureSensorLatched(v: YesNoState) { _airPressureSensorLatched.value = v }

    private val _airPressureSensorCR = mutableStateOf(YesNoState.UNSPECIFIED)
    val airPressureSensorCR: State<YesNoState> = _airPressureSensorCR
    fun setAirPressureSensorCR(v: YesNoState) { _airPressureSensorCR.value = v }

    private val _airPressureSensorEngineerNotes = mutableStateOf("")
    val airPressureSensorEngineerNotes: State<String> = _airPressureSensorEngineerNotes
    fun setAirPressureSensorEngineerNotes(v: String) { _airPressureSensorEngineerNotes.value = v }

    // Bin Door
    private val _binDoorMonitorFitted = mutableStateOf(YesNoState.UNSPECIFIED)
    val binDoorMonitorFitted: State<YesNoState> = _binDoorMonitorFitted
    fun setBinDoorMonitorFitted(v: YesNoState) { _binDoorMonitorFitted.value = v }

    private val _binDoorMonitorDetail = mutableStateOf("")
    val binDoorMonitorDetail: State<String> = _binDoorMonitorDetail
    fun setBinDoorMonitorDetail(v: String) { _binDoorMonitorDetail.value = v }

    private val _binDoorStatusAsFound = mutableStateOf("")
    val binDoorStatusAsFound: State<String> = _binDoorStatusAsFound
    fun setBinDoorStatusAsFound(v: String) { _binDoorStatusAsFound.value = v }

    private val _binDoorUnlockedIndication = mutableStateOf<List<String>>(emptyList())
    val binDoorUnlockedIndication: State<List<String>> = _binDoorUnlockedIndication
    fun setBinDoorUnlockedIndication(v: List<String>) { _binDoorUnlockedIndication.value = v }

    private val _binDoorOpenIndication = mutableStateOf<List<String>>(emptyList())
    val binDoorOpenIndication: State<List<String>> = _binDoorOpenIndication
    fun setBinDoorOpenIndication(v: List<String>) { _binDoorOpenIndication.value = v }

    private val _binDoorTimeoutTimer = mutableStateOf("")
    val binDoorTimeoutTimer: State<String> = _binDoorTimeoutTimer
    fun setBinDoorTimeoutTimer(v: String) { _binDoorTimeoutTimer.value = v }

    private val _binDoorTimeoutResult = mutableStateOf<List<String>>(emptyList())
    val binDoorTimeoutResult: State<List<String>> = _binDoorTimeoutResult
    fun setBinDoorTimeoutResult(v: List<String>) { _binDoorTimeoutResult.value = v }

    private val _binDoorLatched = mutableStateOf(YesNoState.UNSPECIFIED)
    val binDoorLatched: State<YesNoState> = _binDoorLatched
    fun setBinDoorLatched(v: YesNoState) { _binDoorLatched.value = v }

    private val _binDoorCR = mutableStateOf(YesNoState.UNSPECIFIED)
    val binDoorCR: State<YesNoState> = _binDoorCR
    fun setBinDoorCR(v: YesNoState) { _binDoorCR.value = v }

    private val _binDoorEngineerNotes = mutableStateOf("")
    val binDoorEngineerNotes: State<String> = _binDoorEngineerNotes
    fun setBinDoorEngineerNotes(v: String) { _binDoorEngineerNotes.value = v }

    // Indicators
    private val _indicator1colour = mutableStateOf("")
    val indicator1colour: State<String> = _indicator1colour
    fun setIndicator1colour(v: String) { _indicator1colour.value = v }

    private val _indicator1label = mutableStateOf("")
    val indicator1label: State<String> = _indicator1label
    fun setIndicator1label(v: String) { _indicator1label.value = v }

    private val _indicator2colour = mutableStateOf("")
    val indicator2colour: State<String> = _indicator2colour
    fun setIndicator2colour(v: String) { _indicator2colour.value = v }

    private val _indicator2label = mutableStateOf("")
    val indicator2label: State<String> = _indicator2label
    fun setIndicator2label(v: String) { _indicator2label.value = v }

    private val _indicator3colour = mutableStateOf("")
    val indicator3colour: State<String> = _indicator3colour
    fun setIndicator3colour(v: String) { _indicator3colour.value = v }

    private val _indicator3label = mutableStateOf("")
    val indicator3label: State<String> = _indicator3label
    fun setIndicator3label(v: String) { _indicator3label.value = v }

    private val _indicator4colour = mutableStateOf("")
    val indicator4colour: State<String> = _indicator4colour
    fun setIndicator4colour(v: String) { _indicator4colour.value = v }

    private val _indicator4label = mutableStateOf("")
    val indicator4label: State<String> = _indicator4label
    fun setIndicator4label(v: String) { _indicator4label.value = v }

    private val _indicator5colour = mutableStateOf("")
    val indicator5colour: State<String> = _indicator5colour
    fun setIndicator5colour(v: String) { _indicator5colour.value = v }

    private val _indicator5label = mutableStateOf("")
    val indicator5label: State<String> = _indicator5label
    fun setIndicator5label(v: String) { _indicator5label.value = v }

    private val _indicator6colour = mutableStateOf("")
    val indicator6colour: State<String> = _indicator6colour
    fun setIndicator6colour(v: String) { _indicator6colour.value = v }

    private val _indicator6label = mutableStateOf("")
    val indicator6label: State<String> = _indicator6label
    fun setIndicator6label(v: String) { _indicator6label.value = v }

    private val _indicatorsEngineerNotes = mutableStateOf("")
    val indicatorsEngineerNotes: State<String> = _indicatorsEngineerNotes
    fun setIndicatorsEngineerNotes(v: String) { _indicatorsEngineerNotes.value = v }

    // Product Details
    private val _productDescription = mutableStateOf("")
    val productDescription: State<String> = _productDescription
    fun setProductDescription(v: String) { _productDescription.value = v }

    private val _productLength = mutableStateOf("")
    val productLength: State<String> = _productLength
    fun setProductLength(v: String) { _productLength.value = v }

    private val _productWidth = mutableStateOf("")
    val productWidth: State<String> = _productWidth
    fun setProductWidth(v: String) { _productWidth.value = v }

    private val _productHeight = mutableStateOf("")
    val productHeight: State<String> = _productHeight
    fun setProductHeight(v: String) { _productHeight.value = v }

    private val _grossWeight = mutableStateOf("")
    val grossWeight: State<String> = _grossWeight
    fun setGrossWeight(v: String) { _grossWeight.value = v }

    private val _tareWeight = mutableStateOf("")
    val tareWeight: State<String> = _tareWeight
    fun setTareWeight(v: String) { _tareWeight.value = v }

    private val _productLibraryReference = mutableStateOf("")
    val productLibraryReference: State<String> = _productLibraryReference
    fun setProductLibraryReference(v: String) { _productLibraryReference.value = v }

    // Static Scale Reference
    private val _staticScaleMakeModel = mutableStateOf("")
    val staticScaleMakeModel: State<String> = _staticScaleMakeModel
    fun setStaticScaleMakeModel(v: String) { _staticScaleMakeModel.value = v }

    private val _staticScaleCertRef = mutableStateOf("")
    val staticScaleCertRef: State<String> = _staticScaleCertRef
    fun setStaticScaleCertRef(v: String) { _staticScaleCertRef.value = v }

    private val _staticScaleExpiryDate = mutableStateOf("")
    val staticScaleExpiryDate: State<String> = _staticScaleExpiryDate
    fun setStaticScaleExpiryDate(v: String) { _staticScaleExpiryDate.value = v }

    private val _staticScaleManufacturer = mutableStateOf("")
    val staticScaleManufacturer: State<String> = _staticScaleManufacturer
    fun setStaticScaleManufacturer(v: String) { _staticScaleManufacturer.value = v }

    private val _staticScaleModel = mutableStateOf("")
    val staticScaleModel: State<String> = _staticScaleModel
    fun setStaticScaleModel(v: String) { _staticScaleModel.value = v }

    private val _staticScaleSerial = mutableStateOf("")
    val staticScaleSerial: State<String> = _staticScaleSerial
    fun setStaticScaleSerial(v: String) { _staticScaleSerial.value = v }

    private val _staticScaleResolution = mutableStateOf("")
    val staticScaleResolution: State<String> = _staticScaleResolution
    fun setStaticScaleResolution(v: String) {
        _staticScaleResolution.value = com.snb.inspect.core.InputTransforms.decimal(v)
    }

    private val _staticScaleMaxCapacity = mutableStateOf("")
    val staticScaleMaxCapacity: State<String> = _staticScaleMaxCapacity
    fun setStaticScaleMaxCapacity(v: String) {
        _staticScaleMaxCapacity.value = com.snb.inspect.core.InputTransforms.decimal(v)
    }

    private val _staticScaleMinCapacity = mutableStateOf("")
    val staticScaleMinCapacity: State<String> = _staticScaleMinCapacity
    fun setStaticScaleMinCapacity(v: String) {
        _staticScaleMinCapacity.value = com.snb.inspect.core.InputTransforms.decimal(v)
    }

    private val _staticScaleFunctionalCheck = mutableStateOf("SATISFACTORY")
    val staticScaleFunctionalCheck: State<String> = _staticScaleFunctionalCheck
    fun setStaticScaleFunctionalCheck(v: String) { _staticScaleFunctionalCheck.value = v }

    private val _staticScaleCalDate = mutableStateOf("")
    val staticScaleCalDate: State<String> = _staticScaleCalDate
    fun setStaticScaleCalDate(v: String) {
        _staticScaleCalDate.value = v
        reassessStaticScaleStatus(v)
    }

    private val _staticScaleCalStatus = mutableStateOf("MISSING / UNKNOWN")
    val staticScaleCalStatus: State<String> = _staticScaleCalStatus
    fun setStaticScaleCalStatus(v: String) { _staticScaleCalStatus.value = v }

    private fun reassessStaticScaleStatus(dateStr: String) {
        val (status, isValid) = evaluateCalibrationDateStatus(dateStr)
        _staticScaleCalStatus.value = status
        if (!isValid) {
            if (_staticScaleSuitability.value == "SUITABLE") {
                _staticScaleSuitability.value = "LIMITATIONS"
            }
            if (_staticScaleLimitationFlags.value.isBlank()) {
                _staticScaleLimitationFlags.value = "Static scale calibration date is expired, missing, or invalid."
            }
        }
    }

    fun evaluateCalibrationDateStatus(dateStr: String): Pair<String, Boolean> {
        if (dateStr.isBlank()) return Pair("MISSING / UNKNOWN", false)
        val formats = listOf(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"),
            java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd")
        )
        var date: java.time.LocalDate? = null
        for (fmt in formats) {
            try {
                date = java.time.LocalDate.parse(dateStr.trim(), fmt)
                if (date != null) break
            } catch (_: Exception) {}
        }
        if (date == null) return Pair("INVALID DATE FORMAT", false)
        val today = java.time.LocalDate.now()
        return if (date.isBefore(today)) {
            Pair("EXPIRED / INVALID", false)
        } else {
            Pair("VALID", true)
        }
    }

    fun calculateWelmecRequiredE(nominalWeightGrams: Double): Double {
        return when {
            nominalWeightGrams <= 100.0 -> 0.05
            nominalWeightGrams <= 200.0 -> 0.1
            nominalWeightGrams <= 1000.0 -> 0.2
            nominalWeightGrams <= 2000.0 -> 0.5
            else -> {
                val table3Interval = if (nominalWeightGrams <= 15000.0) 5.0 else nominalWeightGrams * 0.0025
                table3Interval / 10.0
            }
        }
    }

    data class StaticScaleAssessmentResult(
        val suitability: String,
        val isWelmecCompliant: Boolean,
        val requiredE: Double?,
        val actualE: Double?,
        val isCapacityValid: Boolean,
        val summaryMessage: String,
        val detailedNotes: String
    )

    fun evaluateStaticScaleAssessment(): StaticScaleAssessmentResult {
        val serial = _staticScaleSerial.value.trim()
        val certRef = _staticScaleCertRef.value.trim()
        val packWeight = _nominalQuantityAsFound.value.toDoubleOrNull()
            ?: _grossWeight.value.toDoubleOrNull()
            ?: 0.0

        val actualE = _staticScaleResolution.value.toDoubleOrNull()
        val maxCap = _staticScaleMaxCapacity.value.toDoubleOrNull()
        val minCap = _staticScaleMinCapacity.value.toDoubleOrNull()
        val calStatus = _staticScaleCalStatus.value
        val functionalOk = _staticScaleFunctionalCheck.value != "UNSATISFACTORY"

        val hasSerial = serial.isNotBlank() && serial != "N/A"
        val hasCertRef = certRef.isNotBlank()
        val requiredE = if (packWeight > 0.0) calculateWelmecRequiredE(packWeight) else null

        val isCalValid = hasSerial && (calStatus.contains("VALID", ignoreCase = true) || (!calStatus.contains("EXPIRED") && !calStatus.contains("MISSING")))
        val meetsEConstraint = actualE != null && actualE > 0.0 && requiredE != null && actualE <= requiredE

        val isCapSpecValid = maxCap != null && maxCap > 0.0 && minCap != null && minCap >= 0.0 && minCap <= maxCap
        val isWithinRange = maxCap != null && minCap != null && maxCap > 0.0 && packWeight >= minCap && packWeight <= maxCap
        val isCapacityValid = isCapSpecValid && isWithinRange

        val isWelmecCompliant = isCalValid && hasCertRef && functionalOk && meetsEConstraint && isCapacityValid

        val (suitability, summary, details) = when {
            !hasSerial -> Triple(
                "LIMITATIONS",
                "⚠️ Un-traceable: Scale serial number is missing.",
                "Serial number is required to establish equipment identity and calibration traceability."
            )
            !functionalOk -> Triple(
                "UNSUITABLE",
                "❌ Unsuitable: Scale functional check was unsatisfactory.",
                "Scale failed basic 3-repeatability pack check or zero stability."
            )
            !isCalValid -> Triple(
                "LIMITATIONS",
                "⚠️ Qualified Assessment: Scale calibration status is $calStatus.",
                "Scale calibration certificate is expired, invalid, or unverified."
            )
            !hasCertRef -> Triple(
                "LIMITATIONS",
                "⚠️ Qualified Assessment: Calibration / verification reference number is missing.",
                "Certificate or verification reference number must be documented for audit evidence."
            )
            !isCapSpecValid -> Triple(
                "LIMITATIONS",
                "⚠️ Qualified Assessment: Scale Max/Min capacity specification is missing or invalid.",
                "Min/Max capacity must be positive numbers with Min ≤ Max."
            )
            !isWithinRange && packWeight > 0.0 -> Triple(
                "LIMITATIONS",
                "⚠️ Qualified Assessment: Pack weight (${packWeight}g) is outside scale capacity range [${minCap}g - ${maxCap}g].",
                "Pack weight falls outside the reference scale's minimum (${minCap}g) / maximum (${maxCap}g) weighing range."
            )
            !meetsEConstraint && actualE != null && requiredE != null -> Triple(
                "LIMITATIONS",
                "⚠️ Qualified Routine Check Only: Scale interval (e = ${actualE}g) exceeds WELMEC Annex 5 threshold (e ≤ ${requiredE}g) for ${packWeight}g pack.",
                "Reference scale verification interval e = ${actualE}g exceeds WELMEC 6.4 Annex 5 requirement (e ≤ ${requiredE}g for nominal pack weight ${packWeight}g). Qualified routine check only; does not meet WELMEC Annex 5 methodology."
            )
            isWelmecCompliant -> Triple(
                "SUITABLE",
                "✅ Fully Compliant: Meets WELMEC 6.4 Annex 5 & OIML R 51-1 reference scale requirements.",
                "Serial $serial is verified. Scale has valid calibration (Cert: $certRef), passes functional check, and verification interval e = ${actualE}g satisfies e ≤ ${requiredE}g threshold for ${packWeight}g pack weight."
            )
            else -> Triple(
                "LIMITATIONS",
                "⚠️ Qualified Assessment: Reference scale parameters under assessment.",
                "Check serial number, calibration date, verification interval e, and Min/Max capacity."
            )
        }

        return StaticScaleAssessmentResult(
            suitability = suitability,
            isWelmecCompliant = isWelmecCompliant,
            requiredE = requiredE,
            actualE = actualE,
            isCapacityValid = isCapacityValid,
            summaryMessage = summary,
            detailedNotes = details
        )
    }

    private val _staticScaleSuitability = mutableStateOf("SUITABLE")
    val staticScaleSuitability: State<String> = _staticScaleSuitability
    fun setStaticScaleSuitability(v: String) { _staticScaleSuitability.value = v }

    private val _staticScaleRefMassCheckResult = mutableStateOf("")
    val staticScaleRefMassCheckResult: State<String> = _staticScaleRefMassCheckResult
    fun setStaticScaleRefMassCheckResult(v: String) { _staticScaleRefMassCheckResult.value = v }

    private val _staticScaleLimitationFlags = mutableStateOf("")
    val staticScaleLimitationFlags: State<String> = _staticScaleLimitationFlags
    fun setStaticScaleLimitationFlags(v: String) { _staticScaleLimitationFlags.value = v }

    // Operating Conditions
    private val _beltSpeed = mutableStateOf("")
    val beltSpeed: State<String> = _beltSpeed
    fun setBeltSpeed(v: String) { _beltSpeed.value = v }

    private val _tareConfiguration = mutableStateOf("")
    val tareConfiguration: State<String> = _tareConfiguration
    fun setTareConfiguration(v: String) { _tareConfiguration.value = v }

    private val _operatingConditionsNotes = mutableStateOf("")
    val operatingConditionsNotes: State<String> = _operatingConditionsNotes
    fun setOperatingConditionsNotes(v: String) { _operatingConditionsNotes.value = v }

    // Engineer Test Weight
    private val _engineerTestWeightId = mutableStateOf<Int?>(null)
    val engineerTestWeightId: State<Int?> = _engineerTestWeightId
    fun setEngineerTestWeightId(v: Int?) { _engineerTestWeightId.value = v }

    // Results As Found
    private val _requiredPassesCount = mutableStateOf(30)
    val requiredPassesCount: State<Int> = _requiredPassesCount
    fun setRequiredPassesCount(v: String) {
        val parsed = v.toIntOrNull()
        if (parsed != null) {
            _requiredPassesCount.value = parsed.coerceAtLeast(10)
        } else if (v.isBlank()) {
            _requiredPassesCount.value = 10
        }
    }
    fun setRequiredPassesCountInt(v: Int) {
        _requiredPassesCount.value = v.coerceAtLeast(10)
    }

    private val _nominalQuantityAsFound = mutableStateOf("")
    val nominalQuantityAsFound: State<String> = _nominalQuantityAsFound
    fun setNominalQuantityAsFound(v: String) { _nominalQuantityAsFound.value = v }

    private val _dynamicPassesAsFound = mutableStateOf<List<String>>(List(30) { "" })
    val dynamicPassesAsFound: State<List<String>> = _dynamicPassesAsFound
    fun setDynamicPassAsFound(index: Int, v: String) {
        if (index in 0 until 30) {
            val newList = _dynamicPassesAsFound.value.toMutableList()
            newList[index] = v
            _dynamicPassesAsFound.value = newList
        }
    }

    private val _staticScaleWeightAsFound = mutableStateOf("")
    val staticScaleWeightAsFound: State<String> = _staticScaleWeightAsFound
    fun setStaticScaleWeightAsFound(v: String) { _staticScaleWeightAsFound.value = v }

    private val _checkweigherWeightAsFound = mutableStateOf("")
    val checkweigherWeightAsFound: State<String> = _checkweigherWeightAsFound
    fun setCheckweigherWeightAsFound(v: String) { _checkweigherWeightAsFound.value = v }

    private val _offCentreLoadingTestResultAsFound = mutableStateOf("")
    val offCentreLoadingTestResultAsFound: State<String> = _offCentreLoadingTestResultAsFound
    fun setOffCentreLoadingTestResultAsFound(v: String) { _offCentreLoadingTestResultAsFound.value = v }

    private val _repeatabilityTestResultAsFound = mutableStateOf("")
    val repeatabilityTestResultAsFound: State<String> = _repeatabilityTestResultAsFound
    fun setRepeatabilityTestResultAsFound(v: String) { _repeatabilityTestResultAsFound.value = v }

    private val _adjustmentsNotes = mutableStateOf("")
    val adjustmentsNotes: State<String> = _adjustmentsNotes
    fun setAdjustmentsNotes(v: String) { _adjustmentsNotes.value = v }

    // Equipment
    val equipmentList = measuringEquipmentDAO.getAllEquipment()

    // Results As Left
    private val _nominalQuantityAsLeft = mutableStateOf("")
    val nominalQuantityAsLeft: State<String> = _nominalQuantityAsLeft
    fun setNominalQuantityAsLeft(v: String) { _nominalQuantityAsLeft.value = v }

    private val _dynamicPassesAsLeft = mutableStateOf<List<String>>(List(30) { "" })
    val dynamicPassesAsLeft: State<List<String>> = _dynamicPassesAsLeft
    fun setDynamicPassAsLeft(index: Int, v: String) {
        if (index in 0 until 30) {
            val newList = _dynamicPassesAsLeft.value.toMutableList()
            newList[index] = v
            _dynamicPassesAsLeft.value = newList
        }
    }

    private val _staticScaleWeightAsLeft = mutableStateOf("")
    val staticScaleWeightAsLeft: State<String> = _staticScaleWeightAsLeft
    fun setStaticScaleWeightAsLeft(v: String) { _staticScaleWeightAsLeft.value = v }

    private val _checkweigherWeightAsLeft = mutableStateOf("")
    val checkweigherWeightAsLeft: State<String> = _checkweigherWeightAsLeft
    fun setCheckweigherWeightAsLeft(v: String) { _checkweigherWeightAsLeft.value = v }

    private val _offCentreLoadingTestResultAsLeft = mutableStateOf("")
    val offCentreLoadingTestResultAsLeft: State<String> = _offCentreLoadingTestResultAsLeft
    fun setOffCentreLoadingTestResultAsLeft(v: String) { _offCentreLoadingTestResultAsLeft.value = v }

    private val _repeatabilityTestResultAsLeft = mutableStateOf("")
    val repeatabilityTestResultAsLeft: State<String> = _repeatabilityTestResultAsLeft
    fun setRepeatabilityTestResultAsLeft(v: String) { _repeatabilityTestResultAsLeft.value = v }

    // Tolerances State
    private val _toleranceSource = mutableStateOf("SNB_GUIDANCE")
    val toleranceSource: State<String> = _toleranceSource
    fun setToleranceSource(v: String) { _toleranceSource.value = v }

    private val _manufacturerTne = mutableStateOf("")
    val manufacturerTne: State<String> = _manufacturerTne
    fun setManufacturerTne(v: String) { _manufacturerTne.value = v }

    private val _customerTne = mutableStateOf("")
    val customerTne: State<String> = _customerTne
    fun setCustomerTne(v: String) { _customerTne.value = v }

    private val _engineerSetPointAssessmentNotes = mutableStateOf("")
    val engineerSetPointAssessmentNotes: State<String> = _engineerSetPointAssessmentNotes
    fun setEngineerSetPointAssessmentNotes(v: String) { _engineerSetPointAssessmentNotes.value = v }

    private val _engineerSetPointAssessmentResolved = mutableStateOf(false)
    val engineerSetPointAssessmentResolved: State<Boolean> = _engineerSetPointAssessmentResolved
    fun setEngineerSetPointAssessmentResolved(v: Boolean) { _engineerSetPointAssessmentResolved.value = v }

    // Reject Verification State
    private val _generalRejectTestResult = mutableStateOf("PASS")
    val generalRejectTestResult: State<String> = _generalRejectTestResult
    fun setGeneralRejectTestResult(v: String) { _generalRejectTestResult.value = v }

    private val _generalRejectNotes = mutableStateOf("")
    val generalRejectNotes: State<String> = _generalRejectNotes
    fun setGeneralRejectNotes(v: String) { _generalRejectNotes.value = v }

    private val _t0TestRefWeight = mutableStateOf("")
    val t0TestRefWeight: State<String> = _t0TestRefWeight
    fun setT0TestRefWeight(v: String) { _t0TestRefWeight.value = v }

    private val _t0TestExpected = mutableStateOf("")
    val t0TestExpected: State<String> = _t0TestExpected
    fun setT0TestExpected(v: String) { _t0TestExpected.value = v }

    private val _t0TestActual = mutableStateOf("")
    val t0TestActual: State<String> = _t0TestActual
    fun setT0TestActual(v: String) { _t0TestActual.value = v }

    private val _t0TestResult = mutableStateOf("")
    val t0TestResult: State<String> = _t0TestResult
    fun setT0TestResult(v: String) { _t0TestResult.value = v }

    private val _t1TestRefWeight = mutableStateOf("")
    val t1TestRefWeight: State<String> = _t1TestRefWeight
    fun setT1TestRefWeight(v: String) { _t1TestRefWeight.value = v }

    private val _t1TestExpected = mutableStateOf("")
    val t1TestExpected: State<String> = _t1TestExpected
    fun setT1TestExpected(v: String) { _t1TestExpected.value = v }

    private val _t1TestActual = mutableStateOf("")
    val t1TestActual: State<String> = _t1TestActual
    fun setT1TestActual(v: String) { _t1TestActual.value = v }

    private val _t1TestResult = mutableStateOf("")
    val t1TestResult: State<String> = _t1TestResult
    fun setT1TestResult(v: String) { _t1TestResult.value = v }

    private val _t2TestRefWeight = mutableStateOf("")
    val t2TestRefWeight: State<String> = _t2TestRefWeight
    fun setT2TestRefWeight(v: String) { _t2TestRefWeight.value = v }

    private val _t2TestExpected = mutableStateOf("")
    val t2TestExpected: State<String> = _t2TestExpected
    fun setT2TestExpected(v: String) { _t2TestExpected.value = v }

    private val _t2TestActual = mutableStateOf("")
    val t2TestActual: State<String> = _t2TestActual
    fun setT2TestActual(v: String) { _t2TestActual.value = v }

    private val _t2TestResult = mutableStateOf("")
    val t2TestResult: State<String> = _t2TestResult
    fun setT2TestResult(v: String) { _t2TestResult.value = v }

    private val _overweightTestRefWeight = mutableStateOf("")
    val overweightTestRefWeight: State<String> = _overweightTestRefWeight
    fun setOverweightTestRefWeight(v: String) { _overweightTestRefWeight.value = v }

    private val _overweightTestExpected = mutableStateOf("")
    val overweightTestExpected: State<String> = _overweightTestExpected
    fun setOverweightTestExpected(v: String) { _overweightTestExpected.value = v }

    private val _overweightTestActual = mutableStateOf("")
    val overweightTestActual: State<String> = _overweightTestActual
    fun setOverweightTestActual(v: String) { _overweightTestActual.value = v }

    private val _overweightTestResult = mutableStateOf("")
    val overweightTestResult: State<String> = _overweightTestResult
    fun setOverweightTestResult(v: String) { _overweightTestResult.value = v }

    // Calculation Results
    val dynamicAccuracyResultAsFound = androidx.compose.runtime.snapshotFlow {
        val tNomVal = _nominalQuantityAsFound.value.toDoubleOrNull() ?: return@snapshotFlow null
        CheckweigherAccuracyCalculator.calculate(
            tNomVal,
            _dynamicPassesAsFound.value.map { it.toDoubleOrNull() },
            _staticScaleWeightAsFound.value.toDoubleOrNull(),
            _checkweigherWeightAsFound.value.toDoubleOrNull()
        )
    }

    val evaluatedCalculationResultAsFound = androidx.compose.runtime.snapshotFlow {
        val tNomVal = _nominalQuantityAsFound.value.toDoubleOrNull() ?: return@snapshotFlow null
        val staticRef = _staticScaleWeightAsFound.value.toDoubleOrNull() ?: tNomVal
        val passes = _dynamicPassesAsFound.value.mapNotNull { it.toDoubleOrNull() }
        val effectiveTne = when (_toleranceSource.value) {
            "MANUFACTURER" -> _manufacturerTne.value.toDoubleOrNull()
            "CUSTOMER" -> _customerTne.value.toDoubleOrNull()
            else -> null
        }
        val genRejectPass = _generalRejectTestResult.value != "FAIL"
        val tolSrc = when (_toleranceSource.value) {
            "MANUFACTURER" -> com.snb.inspect.util.ToleranceSource.MANUFACTURER
            "CUSTOMER" -> com.snb.inspect.util.ToleranceSource.CUSTOMER
            else -> com.snb.inspect.util.ToleranceSource.SNB_GUIDANCE
        }
        com.snb.inspect.util.CheckweigherCalculator.evaluate(
            nominalWeight = tNomVal,
            referenceWeight = staticRef,
            passes = passes,
            effectiveTne = effectiveTne,
            toleranceSource = tolSrc,
            generalRejectPassed = genRejectPass
        )
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, null)

    val dynamicAccuracyResultAsLeft = androidx.compose.runtime.snapshotFlow {
        val tNomVal = _nominalQuantityAsLeft.value.toDoubleOrNull() ?: return@snapshotFlow null
        CheckweigherAccuracyCalculator.calculate(
            tNomVal,
            _dynamicPassesAsLeft.value.map { it.toDoubleOrNull() },
            _staticScaleWeightAsLeft.value.toDoubleOrNull(),
            _checkweigherWeightAsLeft.value.toDoubleOrNull()
        )
    }

    val evaluatedCalculationResultAsLeft = androidx.compose.runtime.snapshotFlow {
        val tNomVal = _nominalQuantityAsLeft.value.toDoubleOrNull() ?: return@snapshotFlow null
        val staticRef = _staticScaleWeightAsLeft.value.toDoubleOrNull() ?: tNomVal
        val passes = _dynamicPassesAsLeft.value.mapNotNull { it.toDoubleOrNull() }
        val effectiveTne = when (_toleranceSource.value) {
            "MANUFACTURER" -> _manufacturerTne.value.toDoubleOrNull()
            "CUSTOMER" -> _customerTne.value.toDoubleOrNull()
            else -> null
        }
        val genRejectPass = _generalRejectTestResult.value != "FAIL"
        val tolSrc = when (_toleranceSource.value) {
            "MANUFACTURER" -> com.snb.inspect.util.ToleranceSource.MANUFACTURER
            "CUSTOMER" -> com.snb.inspect.util.ToleranceSource.CUSTOMER
            else -> com.snb.inspect.util.ToleranceSource.SNB_GUIDANCE
        }
        com.snb.inspect.util.CheckweigherCalculator.evaluate(
            nominalWeight = tNomVal,
            referenceWeight = staticRef,
            passes = passes,
            effectiveTne = effectiveTne,
            toleranceSource = tolSrc,
            generalRejectPassed = genRejectPass
        )
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, null)

    // endregion

    override fun setCurrentScreenNextEnabled(enabled: Boolean) {
        _currentScreenNextEnabled.value = enabled
    }

    override fun isCalibrationValid(routeOrder: List<String>): Boolean {
        return routeOrder.filter { !it.contains("Summary") }.all { isScreenValid(it) }
    }

    fun isScreenValid(route: String): Boolean {
        val baseRoute = route.split("/").first()
        return _screenValidities.value[baseRoute] ?: false
    }

    override fun setScreenValidity(route: String, isValid: Boolean) {
        val baseRoute = route.split("/").first()
        if (_screenValidities.value[baseRoute] != isValid) {
            val updatedMap = _screenValidities.value.toMutableMap()
            updatedMap[baseRoute] = isValid
            _screenValidities.value = updatedMap
        }
    }

    private fun revalidateAllScreens() {
        val validMap = mutableMapOf<String, Boolean>()
        validMap["CheckweigherCalibrationStart"] = _canPerformCalibration.value || _reasonForNotCalibrating.value.isNotEmpty()
        validMap["CwScaleDetails"] = _scaleInterval.value.isNotBlank() && _maxCapacity.value.isNotBlank()
        validMap["CwSystemDetails"] = _beltWidth.value.isNotBlank() && _weighConveyorLength.value.isNotBlank()
        validMap["CwSystemChecklist"] = _beltCondition.value != ConditionState.UNSPECIFIED && _safetyCircuitCondition.value != ConditionState.UNSPECIFIED

        validMap["CwInfeedSensor"] = validateFailsafe(_infeedSensorFitted.value, _infeedSensorTestMethod.value, _infeedSensorTestResult.value, _infeedSensorLatched.value, _infeedSensorCR.value)
        validMap["CwRejectConfirmSensor"] = validateFailsafe(_rejectConfirmSensorFitted.value, _rejectConfirmSensorTestMethod.value, _rejectConfirmSensorTestResult.value, _rejectConfirmSensorLatched.value, _rejectConfirmSensorCR.value)
        validMap["CwBinFullSensor"] = validateFailsafe(_binFullSensorFitted.value, _binFullSensorTestMethod.value, _binFullSensorTestResult.value, _binFullSensorLatched.value, _binFullSensorCR.value)
        validMap["CwAirPressureSensor"] = validateFailsafe(_airPressureSensorFitted.value, _airPressureSensorTestMethod.value, _airPressureSensorTestResult.value, _airPressureSensorLatched.value, _airPressureSensorCR.value)
        validMap["CwBinDoorMonitor"] = validateBinDoor()
        validMap["CwIndicators"] = _indicator1colour.value.isNotBlank() && _indicator1label.value.isNotBlank() &&
                _indicator2colour.value.isNotBlank() && _indicator2label.value.isNotBlank() &&
                _indicator3colour.value.isNotBlank() && _indicator3label.value.isNotBlank() &&
                _indicator4colour.value.isNotBlank() && _indicator4label.value.isNotBlank() &&
                _indicator5colour.value.isNotBlank() && _indicator5label.value.isNotBlank() &&
                _indicator6colour.value.isNotBlank() && _indicator6label.value.isNotBlank()

        validMap["CwTestProductDetails"] = _productDescription.value.isNotBlank() && _productLength.value.isNotBlank()
        val isStaticRefValid = (_staticScaleMakeModel.value.isNotBlank() || _staticScaleManufacturer.value.isNotBlank()) &&
                _staticScaleResolution.value.toDoubleOrNull() != null &&
                _staticScaleCalDate.value.isNotBlank()
        validMap["CwStaticScaleReference"] = isStaticRefValid
        validMap["CwEngineerTestWeight"] = _engineerTestWeightId.value != null
        validMap["CwDynamicTestAsFound"] = _nominalQuantityAsFound.value.isNotBlank() &&
                _dynamicPassesAsFound.value.take(_requiredPassesCount.value).count { it.isNotBlank() } >= _requiredPassesCount.value
        validMap["CwTolerancesAndAssessment"] = _engineerSetPointAssessmentResolved.value || evaluatedCalculationResultAsFound.value?.overallStatus != com.snb.inspect.util.CheckweigherStatus.FURTHER_ASSESSMENT_REQUIRED
        validMap["CwRejectVerification"] = _generalRejectTestResult.value.isNotBlank()

        _screenValidities.value = validMap
    }

    private fun validateFailsafe(fitted: YesNoState, method: String, result: List<String>, latched: YesNoState, cr: YesNoState): Boolean {
        return when (fitted) {
            YesNoState.NO, YesNoState.NA -> true
            YesNoState.YES -> method.isNotBlank() && result.isNotEmpty() && latched != YesNoState.UNSPECIFIED && cr != YesNoState.UNSPECIFIED
            else -> false
        }
    }

    private fun validateBinDoor(): Boolean {
        return when (_binDoorMonitorFitted.value) {
            YesNoState.NO, YesNoState.NA -> true
            YesNoState.YES -> _binDoorStatusAsFound.value.isNotBlank() && _binDoorLatched.value != YesNoState.UNSPECIFIED && _binDoorCR.value != YesNoState.UNSPECIFIED
            else -> false
        }
    }

    override fun shouldSkipToSummary(): Boolean {
        return !_canPerformCalibration.value && _reasonForNotCalibrating.value.isNotEmpty()
    }

    override fun clearCalibrationData() {
        // Implementation
    }

    override fun deleteCalibration(calibrationId: String) {
        viewModelScope.launch {
            calibrationDao.deleteCalibration(calibrationId)
        }
    }

    override fun getDisplayNameForRoute(route: String): String {
        return when (route.split("/").first()) {
            "CheckweigherCalibrationStart" -> "Calibration Start"
            "CwScaleDetails" -> "Scale Details"
            "CwSystemDetails" -> "System Details"
            "CwSystemChecklist" -> "System Checklist"
            "CwInfeedSensor" -> "Infeed Sensor"
            "CwRejectConfirmSensor" -> "Reject Confirm Sensor"
            "CwBinFullSensor" -> "Bin Full Sensor"
            "CwAirPressureSensor" -> "Air Pressure Sensor"
            "CwBinDoorMonitor" -> "Bin Door Monitor"
            "CwIndicators" -> "Indicators"
            "CwTestProductDetails" -> "Product Details"
            "CwStaticScaleReference" -> "Static Scale Ref"
            "CwEngineerTestWeight" -> "Test Weight"
            "CwDynamicTestAsFound" -> "Dynamic Test (30 Passes)"
            "CwTolerancesAndAssessment" -> "Tolerances & Assessment"
            "CwRejectVerification" -> "Reject Verification"
            "CwStaticTestAsFound" -> "Static Test (Found)"
            "CwAdjustmentsMade" -> "Adjustments"
            "CwDynamicTestAsLeft" -> "Dynamic Test (Left)"
            "CwStaticTestAsLeft" -> "Static Test (Left)"
            "CwSummary" -> "Summary"
            else -> route
        }
    }

    suspend fun finaliseCalibrationAndUpload(
        context: Context,
        apiService: ApiService,
        onResult: (String) -> Unit
    ) {
        try {
            val cal = calibrationDao.getCalibrationById(calibrationId.value) ?: return
            cal.endDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            calibrationDao.updateCalibration(cal)

            // 1. Sync machine details with cloud (Ensures cloudSystemId is present)
            InAppLogger.d("Syncing machine record with the cloud (CW)...")
            systemsRepository.updateSystem(
                context = context,
                cloudId = _cloudSystemId.intValue,
                localId = _systemId.intValue,
                tempId = _tempSystemId.intValue
            )

            // 2. Refresh the cloudSystemId in the calibration record to ensure sync
            val updatedSystem = cwSystemsDAO.getSystemById(_systemId.intValue)
            val newCloudId = updatedSystem?.cloudId ?: 0
            if (newCloudId != 0) {
                InAppLogger.d("Updating calibration with new cloudSystemId: $newCloudId")
                calibrationDao.updateCloudIdByCalibrationId(calibrationId.value, newCloudId)
                _cloudSystemId.intValue = newCloudId
            }

            val result = calibrationRepository.uploadUnsyncedCalibrations(context, apiService, calibrationId.value)
            if (result is FetchResult.Success) {
                onResult("✅ Calibration completed and uploaded.")
            } else {
                onResult("⚠️ Calibration completed locally, but cloud upload failed.")
            }
        } catch (e: Exception) {
            onResult("❌ Error finishing calibration.")
        }
    }

    private fun parsePasses(passesString: String): List<String> {
        if (passesString.isBlank()) return List(30) { "" }
        val list = passesString.split(",").map { it.trim() }
        return List(30) { i -> list.getOrNull(i) ?: "" }
    }

    fun wipeAllData() {
        setBeltCondition(ConditionState.NA)
        setBeltConditionComments("")
        setSafetyCircuitCondition(ConditionState.NA)
        setSafetyCircuitConditionComments("")
        setGuardCondition(ConditionState.NA)
        setGuardConditionComments("")
        setVibrationCondition(ConditionState.NA)
        setVibrationConditionComments("")
        setWeighTableObstruction(ConditionState.NA)
        setWeighTableObstructionComments("")
        setProductTransferCondition(ConditionState.NA)
        setProductTransferConditionComments("")
        setMachineStabilityCondition(ConditionState.NA)
        setMachineStabilityConditionComments("")
        setSystemChecklistEngineerNotes("")

        setInfeedSensorFitted(YesNoState.NA)
        setInfeedSensorDetail("")
        setInfeedSensorTestMethod("")
        setInfeedSensorTestResult(emptyList())
        setInfeedSensorLatched(YesNoState.NA)
        setInfeedSensorCR(YesNoState.NA)
        setInfeedSensorEngineerNotes("")

        setRejectConfirmSensorFitted(YesNoState.NA)
        setRejectConfirmSensorDetail("")
        setRejectConfirmSensorTestMethod("")
        setRejectConfirmSensorTestResult(emptyList())
        setRejectConfirmSensorLatched(YesNoState.NA)
        setRejectConfirmSensorCR(YesNoState.NA)
        setRejectConfirmSensorEngineerNotes("")

        setBinFullSensorFitted(YesNoState.NA)
        setBinFullSensorDetail("")
        setBinFullSensorTestMethod("")
        setBinFullSensorTestResult(emptyList())
        setBinFullSensorLatched(YesNoState.NA)
        setBinFullSensorCR(YesNoState.NA)
        setBinFullSensorEngineerNotes("")

        setAirPressureSensorFitted(YesNoState.NA)
        setAirPressureSensorDetail("")
        setAirPressureSensorTestMethod("")
        setAirPressureSensorTestResult(emptyList())
        setAirPressureSensorLatched(YesNoState.NA)
        setAirPressureSensorCR(YesNoState.NA)
        setAirPressureSensorEngineerNotes("")

        setBinDoorMonitorFitted(YesNoState.NA)
        setBinDoorMonitorDetail("")
        setBinDoorStatusAsFound("")
        setBinDoorUnlockedIndication(emptyList())
        setBinDoorOpenIndication(emptyList())
        setBinDoorTimeoutTimer("")
        setBinDoorTimeoutResult(emptyList())
        setBinDoorLatched(YesNoState.NA)
        setBinDoorCR(YesNoState.NA)
        setBinDoorEngineerNotes("")

        setIndicator1colour("")
        setIndicator1label("")
        setIndicator2colour("")
        setIndicator2label("")
        setIndicator3colour("")
        setIndicator3label("")
        setIndicator4colour("")
        setIndicator4label("")
        setIndicator5colour("")
        setIndicator5label("")
        setIndicator6colour("")
        setIndicator6label("")
        setIndicatorsEngineerNotes("")

        setProductDescription("")
        setProductLength("")
        setProductWidth("")
        setProductHeight("")
        setGrossWeight("")
        setTareWeight("")
        setProductLibraryReference("")

        setStaticScaleMakeModel("")
        setStaticScaleCertRef("")
        setStaticScaleExpiryDate("")
        setEngineerTestWeightId(null)

        setNominalQuantityAsFound("")
        for (i in 0 until 30) setDynamicPassAsFound(i, "")
        setStaticScaleWeightAsFound("")
        setCheckweigherWeightAsFound("")
        setOffCentreLoadingTestResultAsFound("")
        setRepeatabilityTestResultAsFound("")
        setAdjustmentsNotes("")

        setNominalQuantityAsLeft("")
        for (i in 0 until 30) setDynamicPassAsLeft(i, "")
        setStaticScaleWeightAsLeft("")
        setCheckweigherWeightAsLeft("")
        setOffCentreLoadingTestResultAsLeft("")
        setRepeatabilityTestResultAsLeft("")
    }
}
