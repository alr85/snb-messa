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
import com.snb.inspect.daos.CustomerDAO
import com.snb.inspect.daos.MdModelsDAO
import com.snb.inspect.daos.MeasuringEquipmentDAO
import com.snb.inspect.daos.MetalDetectorSystemsDAO
import com.snb.inspect.daos.SystemTypeDAO
import com.snb.inspect.dataClasses.CheckweigherCalibrationLocal
import com.snb.inspect.dataClasses.MetalDetectorWithFullDetails
import com.snb.inspect.formModules.ConditionState
import com.snb.inspect.formModules.YesNoState
import com.snb.inspect.repositories.CheckweigherCalibrationRepository
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.util.InAppLogger
import com.snb.inspect.util.toConditionState
import com.snb.inspect.util.toYesNoState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
            if (existing != null) {
                loadCalibration(existing)
            } else {
                saveNewCalibration()
            }
            revalidateAllScreens()
            _isLoading.value = false
        }
    }

    private fun loadCalibration(cal: CheckweigherCalibrationLocal) {
        _newLocation.value = cal.newLocation
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
        
        // Scale Details
        _loadcellType.value = cal.loadcellType
        _scaleInterval.value = cal.scaleInterval
        _maxCapacity.value = cal.maxCapacity
        
        // System Details
        _beltWidth.value = cal.beltWidth
        _weighConveyorLength.value = cal.weighConveyorLength
        _rejectType.value = cal.rejectType
        _printerDataCapture.value = cal.printerDataCapture
        _rejectMode.value = cal.rejectMode
        
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
        _infeedSensorTestResult.value = cal.infeedSensorTestResult.split(",").filter { it.isNotBlank() }
        _infeedSensorLatched.value = cal.infeedSensorLatched.toYesNoState()
        _infeedSensorCR.value = cal.infeedSensorCR.toYesNoState()
        _infeedSensorEngineerNotes.value = cal.infeedSensorEngineerNotes

        _rejectConfirmSensorFitted.value = cal.rejectConfirmSensorFitted.toYesNoState()
        _rejectConfirmSensorDetail.value = cal.rejectConfirmSensorDetail
        _rejectConfirmSensorTestMethod.value = cal.rejectConfirmSensorTestMethod
        _rejectConfirmSensorTestResult.value = cal.rejectConfirmSensorTestResult.split(",").filter { it.isNotBlank() }
        _rejectConfirmSensorLatched.value = cal.rejectConfirmSensorLatched.toYesNoState()
        _rejectConfirmSensorCR.value = cal.rejectConfirmSensorCR.toYesNoState()
        _rejectConfirmSensorEngineerNotes.value = cal.rejectConfirmSensorEngineerNotes

        _binFullSensorFitted.value = cal.binFullSensorFitted.toYesNoState()
        _binFullSensorDetail.value = cal.binFullSensorDetail
        _binFullSensorTestMethod.value = cal.binFullSensorTestMethod
        _binFullSensorTestResult.value = cal.binFullSensorTestResult.split(",").filter { it.isNotBlank() }
        _binFullSensorLatched.value = cal.binFullSensorLatched.toYesNoState()
        _binFullSensorCR.value = cal.binFullSensorCR.toYesNoState()
        _binFullSensorEngineerNotes.value = cal.binFullSensorEngineerNotes

        _airPressureSensorFitted.value = cal.airPressureSensorFitted.toYesNoState()
        _airPressureSensorDetail.value = cal.airPressureSensorDetail
        _airPressureSensorTestMethod.value = cal.airPressureSensorTestMethod
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
        
        // Test Weight
        _engineerTestWeightId.value = cal.engineerTestWeightId
        
        // Results As Found
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
        }
        viewModelScope.launch {
            calibrationRepository.insertNewCalibration(context, newCal)
        }
    }

    override fun persistCurrentScreen(route: String) {
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
        
        cal.beltWidth = _beltWidth.value
        cal.weighConveyorLength = _weighConveyorLength.value
        cal.rejectType = _rejectType.value
        cal.printerDataCapture = _printerDataCapture.value
        cal.rejectMode = _rejectMode.value
        
        cal.beltCondition = _beltCondition.value.toString()
        cal.beltConditionComments = _beltConditionComments.value
        cal.safetyCircuitCondition = _safetyCircuitCondition.value.toString()
        cal.safetyCircuitConditionComments = _safetyCircuitConditionComments.value
        cal.guardCondition = _guardCondition.value.toString()
        cal.guardConditionComments = _guardConditionComments.value
        cal.vibrationCondition = _vibrationCondition.value.toString()
        cal.vibrationConditionComments = _vibrationConditionComments.value
        cal.weighTableObstruction = _weighTableObstruction.value.toString()
        cal.weighTableObstructionComments = _weighTableObstructionComments.value
        cal.productTransferCondition = _productTransferCondition.value.toString()
        cal.productTransferConditionComments = _productTransferConditionComments.value
        cal.machineStabilityCondition = _machineStabilityCondition.value.toString()
        cal.machineStabilityConditionComments = _machineStabilityConditionComments.value
        cal.systemChecklistEngineerNotes = _systemChecklistEngineerNotes.value
        
        cal.infeedSensorFitted = _infeedSensorFitted.value.toString()
        cal.infeedSensorDetail = _infeedSensorDetail.value
        cal.infeedSensorTestMethod = _infeedSensorTestMethod.value
        cal.infeedSensorTestResult = _infeedSensorTestResult.value.joinToString(",")
        cal.infeedSensorLatched = _infeedSensorLatched.value.toString()
        cal.infeedSensorCR = _infeedSensorCR.value.toString()
        cal.infeedSensorEngineerNotes = _infeedSensorEngineerNotes.value

        cal.rejectConfirmSensorFitted = _rejectConfirmSensorFitted.value.toString()
        cal.rejectConfirmSensorDetail = _rejectConfirmSensorDetail.value
        cal.rejectConfirmSensorTestMethod = _rejectConfirmSensorTestMethod.value
        cal.rejectConfirmSensorTestResult = _rejectConfirmSensorTestResult.value.joinToString(",")
        cal.rejectConfirmSensorLatched = _rejectConfirmSensorLatched.value.toString()
        cal.rejectConfirmSensorCR = _rejectConfirmSensorCR.value.toString()
        cal.rejectConfirmSensorEngineerNotes = _rejectConfirmSensorEngineerNotes.value

        cal.binFullSensorFitted = _binFullSensorFitted.value.toString()
        cal.binFullSensorDetail = _binFullSensorDetail.value
        cal.binFullSensorTestMethod = _binFullSensorTestMethod.value
        cal.binFullSensorTestResult = _binFullSensorTestResult.value.joinToString(",")
        cal.binFullSensorLatched = _binFullSensorLatched.value.toString()
        cal.binFullSensorCR = _binFullSensorCR.value.toString()
        cal.binFullSensorEngineerNotes = _binFullSensorEngineerNotes.value

        cal.airPressureSensorFitted = _airPressureSensorFitted.value.toString()
        cal.airPressureSensorDetail = _airPressureSensorDetail.value
        cal.airPressureSensorTestMethod = _airPressureSensorTestMethod.value
        cal.airPressureSensorTestResult = _airPressureSensorTestResult.value.joinToString(",")
        cal.airPressureSensorLatched = _airPressureSensorLatched.value.toString()
        cal.airPressureSensorCR = _airPressureSensorCR.value.toString()
        cal.airPressureSensorEngineerNotes = _airPressureSensorEngineerNotes.value

        cal.binDoorMonitorFitted = _binDoorMonitorFitted.value.toString()
        cal.binDoorMonitorDetail = _binDoorMonitorDetail.value
        cal.binDoorStatusAsFound = _binDoorStatusAsFound.value
        cal.binDoorUnlockedIndication = _binDoorUnlockedIndication.value.joinToString(",")
        cal.binDoorOpenIndication = _binDoorOpenIndication.value.joinToString(",")
        cal.binDoorTimeoutTimer = _binDoorTimeoutTimer.value
        cal.binDoorTimeoutResult = _binDoorTimeoutResult.value.joinToString(",")
        cal.binDoorLatched = _binDoorLatched.value.toString()
        cal.binDoorCR = _binDoorCR.value.toString()
        cal.binDoorEngineerNotes = _binDoorEngineerNotes.value
        
        cal.productDescription = _productDescription.value
        cal.productLength = _productLength.value
        cal.productWidth = _productWidth.value
        cal.productHeight = _productHeight.value
        cal.grossWeight = _grossWeight.value
        cal.tareWeight = _tareWeight.value
        cal.productLibraryReference = _productLibraryReference.value
        
        cal.staticScaleMakeModel = _staticScaleMakeModel.value
        cal.staticScaleCertRef = _staticScaleCertRef.value
        cal.staticScaleExpiryDate = _staticScaleExpiryDate.value
        
        cal.engineerTestWeightId = _engineerTestWeightId.value
        
        cal.nominalQuantityAsFound = _nominalQuantityAsFound.value
        cal.dynamicPassesAsFound = _dynamicPassesAsFound.value.joinToString(",")
        cal.staticScaleWeightAsFound = _staticScaleWeightAsFound.value
        cal.checkweigherWeightAsFound = _checkweigherWeightAsFound.value
        cal.offCentreLoadingTestResultAsFound = _offCentreLoadingTestResultAsFound.value
        cal.repeatabilityTestResultAsFound = _repeatabilityTestResultAsFound.value
        
        cal.adjustmentsNotes = _adjustmentsNotes.value
        
        cal.nominalQuantityAsLeft = _nominalQuantityAsLeft.value
        cal.dynamicPassesAsLeft = _dynamicPassesAsLeft.value.joinToString(",")
        cal.staticScaleWeightAsLeft = _staticScaleWeightAsLeft.value
        cal.checkweigherWeightAsLeft = _checkweigherWeightAsLeft.value
        cal.offCentreLoadingTestResultAsLeft = _offCentreLoadingTestResultAsLeft.value
        cal.repeatabilityTestResultAsLeft = _repeatabilityTestResultAsLeft.value
    }

    // region STATE VARIABLES

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

    // Engineer Test Weight
    private val _engineerTestWeightId = mutableStateOf<Int?>(null)
    val engineerTestWeightId: State<Int?> = _engineerTestWeightId
    fun setEngineerTestWeightId(v: Int?) { _engineerTestWeightId.value = v }

    // Results As Found
    private val _nominalQuantityAsFound = mutableStateOf("")
    val nominalQuantityAsFound: State<String> = _nominalQuantityAsFound
    fun setNominalQuantityAsFound(v: String) { _nominalQuantityAsFound.value = v }

    private val _dynamicPassesAsFound = mutableStateOf<List<String>>(List(10) { "" })
    val dynamicPassesAsFound: State<List<String>> = _dynamicPassesAsFound
    fun setDynamicPassAsFound(index: Int, v: String) {
        val newList = _dynamicPassesAsFound.value.toMutableList()
        newList[index] = v
        _dynamicPassesAsFound.value = newList
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

    private val _dynamicPassesAsLeft = mutableStateOf<List<String>>(List(10) { "" })
    val dynamicPassesAsLeft: State<List<String>> = _dynamicPassesAsLeft
    fun setDynamicPassAsLeft(index: Int, v: String) {
        val newList = _dynamicPassesAsLeft.value.toMutableList()
        newList[index] = v
        _dynamicPassesAsLeft.value = newList
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

    val dynamicAccuracyResultAsLeft = androidx.compose.runtime.snapshotFlow {
        val tNomVal = _nominalQuantityAsLeft.value.toDoubleOrNull() ?: return@snapshotFlow null
        CheckweigherAccuracyCalculator.calculate(
            tNomVal,
            _dynamicPassesAsLeft.value.map { it.toDoubleOrNull() },
            _staticScaleWeightAsLeft.value.toDoubleOrNull(),
            _checkweigherWeightAsLeft.value.toDoubleOrNull()
        )
    }

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
        validMap["CwScaleDetails"] = _loadcellType.value.isNotBlank() && _scaleInterval.value.isNotBlank() && _maxCapacity.value.isNotBlank()
        validMap["CwSystemDetails"] = _beltWidth.value.isNotBlank() && _weighConveyorLength.value.isNotBlank()
        validMap["CwSystemChecklist"] = _beltCondition.value != ConditionState.UNSPECIFIED && _safetyCircuitCondition.value != ConditionState.UNSPECIFIED
        
        validMap["CwInfeedSensor"] = validateFailsafe(_infeedSensorFitted.value, _infeedSensorTestMethod.value, _infeedSensorTestResult.value, _infeedSensorLatched.value, _infeedSensorCR.value)
        validMap["CwRejectConfirmSensor"] = validateFailsafe(_rejectConfirmSensorFitted.value, _rejectConfirmSensorTestMethod.value, _rejectConfirmSensorTestResult.value, _rejectConfirmSensorLatched.value, _rejectConfirmSensorCR.value)
        validMap["CwBinFullSensor"] = validateFailsafe(_binFullSensorFitted.value, _binFullSensorTestMethod.value, _binFullSensorTestResult.value, _binFullSensorLatched.value, _binFullSensorCR.value)
        validMap["CwAirPressureSensor"] = validateFailsafe(_airPressureSensorFitted.value, _airPressureSensorTestMethod.value, _airPressureSensorTestResult.value, _airPressureSensorLatched.value, _airPressureSensorCR.value)
        validMap["CwBinDoorMonitor"] = validateBinDoor()
        
        validMap["CwTestProductDetails"] = _productDescription.value.isNotBlank() && _productLength.value.isNotBlank()
        validMap["CwStaticScaleReference"] = _staticScaleMakeModel.value.isNotBlank()
        validMap["CwEngineerTestWeight"] = _engineerTestWeightId.value != null
        
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
            "CwTestProductDetails" -> "Product Details"
            "CwStaticScaleReference" -> "Static Scale Ref"
            "CwEngineerTestWeight" -> "Test Weight"
            "CwDynamicTestAsFound" -> "Dynamic Test (Found)"
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
        if (passesString.isBlank()) return List(10) { "" }
        val list = passesString.split(",").map { it.trim() }
        return List(10) { i -> list.getOrNull(i) ?: "" }
    }
}
