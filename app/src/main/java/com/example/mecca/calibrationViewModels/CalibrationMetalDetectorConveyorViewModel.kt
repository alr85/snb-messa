package com.example.mecca.calibrationViewModels

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mecca.ApiService
import com.example.mecca.daos.CustomerDAO
import com.example.mecca.daos.MdModelsDAO
import com.example.mecca.daos.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.daos.MetalDetectorSystemsDAO
import com.example.mecca.daos.SystemTypeDAO
import com.example.mecca.calibrationLogic.metalDetectorConveyor.setAllPvResultsNa
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toAirPressureSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toBackupSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toBinDoorMonitorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toBinFullSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toCalibrationEndUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toCalibrationStartUpdate
//import com.example.mecca.calibrationLogic.metalDetectorConveyor.toComplianceConfirmationUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toConveyorDetailsUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toDetectNotificationUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toDetectionSettingAsLeftUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toDetectionSettingLabelsUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toDetectionSettingsAsFoundUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toFerrousResultUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toIndicatorsUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toInfeedSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toLargeMetalResultUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toNewCalibrationInsert
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toNonFerrousResultUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toOperatorTestUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toPackCheckSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toProductDetailsUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toRejectConfirmSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toRejectSettingsUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toSensitivitiesAsFoundUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toSensitivityRequirementsUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toSpeedSensorUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toStainlessResultUpdate
import com.example.mecca.calibrationLogic.metalDetectorConveyor.toSystemChecklistUpdate
import com.example.mecca.dataClasses.ConveyorRetailerSensitivitiesEntity
import com.example.mecca.formModules.ConditionState
import com.example.mecca.formModules.YesNoState
import com.example.mecca.repositories.MetalDetectorConveyorCalibrationRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.repositories.RetailerSensitivitiesRepository
import com.example.mecca.util.CsvUploader
import com.example.mecca.util.InAppLogger
import com.example.mecca.util.toConditionState
import com.example.mecca.util.toYesNoState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CalibrationMetalDetectorConveyorViewModel(
    val engineerId: Int,
    private val calibrationDao: MetalDetectorConveyorCalibrationDAO,
    private val calibrationRepository: MetalDetectorConveyorCalibrationRepository,
    private val mdModelsDAO: MdModelsDAO,
    private val mdSystemsDAO: MetalDetectorSystemsDAO,
    private val systemTypeDAO: SystemTypeDAO,
    private val customerDAO: CustomerDAO,
    private val repository: MetalDetectorSystemsRepository,
    calibrationId: String,
    customerId: Int,
    systemId: Int,
    cloudSystemId: Int,
    tempSystemId: Int,
    systemTypeId: Int,
    serialNumber: String,
    modelDescription: String,
    customerName: String,
    systemTypeDescription: String,
    modelId: Int,
    detectionSetting1label: String,
    detectionSetting2label: String,
    detectionSetting3label: String,
    detectionSetting4label: String,
    detectionSetting5label: String,
    detectionSetting6label: String,
    detectionSetting7label: String,
    detectionSetting8label: String,
    lastLocation: String,
    apiService: ApiService,
    private val retailerSensitivitiesRepo: RetailerSensitivitiesRepository
) : ViewModel() {



    init {
        viewModelScope.launch {

            InAppLogger.d("MD Calibration started. Checking for existing calibration...")
            val existingCalibration = calibrationDao.getCalibrationById(calibrationId)

            if (existingCalibration != null) {

                //region LoadExistingCalibration

                InAppLogger.d("Found existing calibration. Updating UI...")
                InAppLogger.d("PV Required state = ${existingCalibration.pvRequired}")
                // Populate individual state variables from the loaded calibration
                val model =
                    mdModelsDAO.getMdModelDescriptionFromDb(existingCalibration.modelId) ?: "Error"
                _modelDescription.value = model

                val customer =
                    customerDAO.getCustomerName(existingCalibration.customerId) ?: "Error"
                _customerName.value = customer

                val systemType = systemTypeDAO.getMdSystemTypeDescriptionFromDb(existingCalibration.systemTypeId) ?: "Error"
                InAppLogger.d("existingCalibration.systemTypeId = ${existingCalibration.systemTypeId}")
                _systemTypeDescription.value = systemType


                _serialNumber.value = existingCalibration.serialNumber
                _isSynced.value = existingCalibration.isSynced
                _serialNumber.value = existingCalibration.serialNumber
                _modelId.intValue = existingCalibration.modelId
                _customerId.intValue = existingCalibration.customerId
                _systemId.intValue = existingCalibration.systemId
                _cloudSystemId.intValue = existingCalibration.cloudSystemId
                _tempSystemId.intValue = existingCalibration.tempSystemId
                _systemTypeId.intValue = existingCalibration.systemTypeId

                _calibrationStartTime.value = existingCalibration.startDate
                //_calibrationEndTime.value = existingCalibration.endDate
                //_engineerId.value = (existingCalibration.engineerId).toString()
                _newLocation.value = existingCalibration.newLocation
                _lastLocation.value = existingCalibration.lastLocation
                _canPerformCalibration.value = existingCalibration.canPerformCalibration.toBoolean()
                _reasonForNotCalibrating.value = existingCalibration.reasonForNotCalibrating
                _pvRequired.value = existingCalibration.pvRequired


//                _desiredCop.value = existingCalibration.desiredCop
//                    .removeSurrounding("[", "]")
//                    .split(",")
//                    .map { it.trim() }
//                    .filter { it.isNotBlank() }
                _startCalibrationNotes.value = existingCalibration.startCalibrationNotes

                _sensitivityRequirementFerrous.value =
                    existingCalibration.sensitivityRequirementFerrous?.toString() ?: ""
                _sensitivityRequirementNonFerrous.value =
                    existingCalibration.sensitivityRequirementNonFerrous?.toString() ?: ""
                _sensitivityRequirementStainless.value =
                    existingCalibration.sensitivityRequirementStainless?.toString() ?: ""
                _sensitivityRequirementEngineerNotes.value =
                    existingCalibration.sensitivityRequirementEngineerNotes


                existingCalibration.productHeight
                    .toDoubleOrNull()
                    ?.let { fetchSensitivityData(it) }



                _productDescription.value = existingCalibration.productDescription
                _productLibraryReference.value = existingCalibration.productLibraryReference
                _productLibraryNumber.value = existingCalibration.productLibraryNumber
                _productLength.value = existingCalibration.productLength
                _productWidth.value = existingCalibration.productWidth
                _productHeight.value = existingCalibration.productHeight
                _productDetailsEngineerNotes.value = existingCalibration.productDetailsEngineerNotes

                _sensitivityAccessRestriction.value = existingCalibration.sensitivityAccessRestriction
                _detectionSettingAsFound1.value = existingCalibration.detectionSettingAsFound1
                _detectionSettingAsFound2.value = existingCalibration.detectionSettingAsFound2
                _detectionSettingAsFound3.value = existingCalibration.detectionSettingAsFound3
                _detectionSettingAsFound4.value = existingCalibration.detectionSettingAsFound4
                _detectionSettingAsFound5.value = existingCalibration.detectionSettingAsFound5
                _detectionSettingAsFound6.value = existingCalibration.detectionSettingAsFound6
                _detectionSettingAsFound7.value = existingCalibration.detectionSettingAsFound7
                _detectionSettingAsFound8.value = existingCalibration.detectionSettingAsFound8

                _detectionSettingPvResult.value = existingCalibration.detectionSettingPvResult



                _detectionSettingAsFoundEngineerNotes.value =
                    existingCalibration.detectionSettingAsFoundEngineerNotes

                _sensitivityAsFoundFerrous.value = existingCalibration.sensitivityAsFoundFerrous
                _sensitivityAsFoundFerrousPeakSignal.value =
                    existingCalibration.sensitivityAsFoundFerrousPeakSignal
                _sensitivityAsFoundNonFerrous.value =
                    existingCalibration.sensitivityAsFoundNonFerrous
                _sensitivityAsFoundNonFerrousPeakSignal.value =
                    existingCalibration.sensitivityAsFoundNonFerrousPeakSignal
                _sensitivityAsFoundStainless.value = existingCalibration.sensitivityAsFoundStainless
                _sensitivityAsFoundStainlessPeakSignal.value =
                    existingCalibration.sensitivityAsFoundStainlessPeakSignal
                _productPeakSignalAsFound.value = existingCalibration.productPeakSignalAsFound
                _sensitivityAsFoundEngineerNotes.value =
                    existingCalibration.sensitivityAsFoundEngineerNotes


                _sensitivityAsLeftFerrous.value = existingCalibration.sensitivityAsLeftFerrous
                _sampleCertificateNumberFerrous.value =
                    existingCalibration.sampleCertificateNumberFerrous
                _detectRejectFerrousLeading.value =
                    existingCalibration.detectRejectFerrousLeading.toYesNoState()
                _peakSignalFerrousLeading.value =
                    existingCalibration.detectRejectFerrousLeadingPeakSignal
                _detectRejectFerrousMiddle.value =
                    existingCalibration.detectRejectFerrousMiddle.toYesNoState()
                _peakSignalFerrousMiddle.value =
                    existingCalibration.detectRejectFerrousMiddlePeakSignal
                _detectRejectFerrousTrailing.value =
                    existingCalibration.detectRejectFerrousTrailing.toYesNoState()
                _peakSignalFerrousTrailing.value =
                    existingCalibration.detectRejectFerrousTrailingPeakSignal
                _ferrousTestEngineerNotes.value =
                    existingCalibration.ferrousTestEngineerNotes
                _ferrousTestPvResult.value =
                    existingCalibration.ferrousTestPvResult




                _sensitivityAsLeftNonFerrous.value = existingCalibration.sensitivityAsLeftNonFerrous
                _sampleCertificateNumberNonFerrous.value =
                    existingCalibration.sampleCertificateNumberNonFerrous
                _detectRejectNonFerrousLeading.value =
                    existingCalibration.detectRejectNonFerrousLeading.toYesNoState()
                _peakSignalNonFerrousLeading.value =
                    existingCalibration.detectRejectNonFerrousLeadingPeakSignal
                _detectRejectNonFerrousMiddle.value =
                    existingCalibration.detectRejectNonFerrousMiddle.toYesNoState()
                _peakSignalNonFerrousMiddle.value =
                    existingCalibration.detectRejectNonFerrousMiddlePeakSignal
                _detectRejectNonFerrousTrailing.value =
                    existingCalibration.detectRejectNonFerrousTrailing.toYesNoState()
                _peakSignalNonFerrousTrailing.value =
                    existingCalibration.detectRejectNonFerrousTrailingPeakSignal
                _nonFerrousTestEngineerNotes.value =
                    existingCalibration.nonFerrousTestEngineerNotes
                _nonFerrousTestPvResult.value =
                    existingCalibration.nonFerrousTestPvResult





                _sensitivityAsLeftStainless.value = existingCalibration.sensitivityAsLeftStainless
                _sampleCertificateNumberStainless.value =
                    existingCalibration.sampleCertificateNumberStainless
                _detectRejectStainlessLeading.value =
                    existingCalibration.detectRejectStainlessLeading.toYesNoState()
                _peakSignalStainlessLeading.value =
                    existingCalibration.detectRejectStainlessLeadingPeakSignal
                _detectRejectStainlessMiddle.value =
                    existingCalibration.detectRejectStainlessMiddle.toYesNoState()
                _peakSignalStainlessMiddle.value =
                    existingCalibration.detectRejectStainlessMiddlePeakSignal
                _detectRejectStainlessTrailing.value =
                    existingCalibration.detectRejectStainlessTrailing.toYesNoState()
                _peakSignalStainlessTrailing.value =
                    existingCalibration.detectRejectStainlessTrailingPeakSignal
                _stainlessTestEngineerNotes.value =
                    existingCalibration.stainlessTestEngineerNotes
                _stainlessTestPvResult.value =
                    existingCalibration.stainlessTestPvResult


                _detectRejectLargeMetal.value =
                    existingCalibration.detectRejectLargeMetal.toYesNoState()
                _sampleCertificateNumberLargeMetal.value =
                    existingCalibration.sampleCertificateNumberLargeMetal
                _largeMetalTestEngineerNotes.value = existingCalibration.largeMetalTestEngineerNotes
                _largeMetalTestPvResult.value = existingCalibration.largeMetalTestPvResult


                _detectionSettingAsLeft1.value = existingCalibration.detectionSettingAsLeft1
                _detectionSettingAsLeft2.value = existingCalibration.detectionSettingAsLeft2
                _detectionSettingAsLeft3.value = existingCalibration.detectionSettingAsLeft3
                _detectionSettingAsLeft4.value = existingCalibration.detectionSettingAsLeft4
                _detectionSettingAsLeft5.value = existingCalibration.detectionSettingAsLeft5
                _detectionSettingAsLeft6.value = existingCalibration.detectionSettingAsLeft6
                _detectionSettingAsLeft7.value = existingCalibration.detectionSettingAsLeft7
                _detectionSettingAsLeft8.value = existingCalibration.detectionSettingAsLeft8
                _detectionSettingAsLeftEngineerNotes.value =
                    existingCalibration.detectionSettingAsLeftEngineerNotes

                _rejectSynchronisationSetting.value =
                    existingCalibration.rejectSynchronisationSetting.toYesNoState()
                _rejectSynchronisationDetail.value = existingCalibration.rejectSynchronisationDetail
                _rejectDelaySetting.value = existingCalibration.rejectDelaySetting
                _rejectDelayUnits.value = existingCalibration.rejectDelayUnits
                _rejectDurationSetting.value = existingCalibration.rejectDurationSetting
                _rejectDurationUnits.value = existingCalibration.rejectDurationUnits
                _rejectConfirmWindowSetting.value = existingCalibration.rejectConfirmWindowSetting
                _rejectConfirmWindowUnits.value = existingCalibration.rejectConfirmWindowUnits
                _rejectSettingsEngineerNotes.value = existingCalibration.rejectSettingsEngineerNotes

                _infeedBeltHeight.value = existingCalibration.infeedBeltHeight
                _outfeedBeltHeight.value = existingCalibration.outfeedBeltHeight
                _conveyorLength.value = existingCalibration.conveyorLength
                _conveyorHanding.value = existingCalibration.conveyorHanding
                _beltSpeed.value = existingCalibration.beltSpeed
                _rejectDevice.value = existingCalibration.rejectDevice
                _rejectDeviceOther.value = existingCalibration.rejectDeviceOther
                _conveyorDetailsEngineerNotes.value =
                    existingCalibration.conveyorDetailsEngineerNotes

                _beltCondition.value = existingCalibration.beltCondition.toConditionState()
                _beltConditionComments.value = existingCalibration.beltConditionComments
                _guardCondition.value = existingCalibration.guardCondition.toConditionState()
                _guardConditionComments.value = existingCalibration.guardConditionComments
                _safetyCircuitCondition.value =
                    existingCalibration.safetyCircuitCondition.toConditionState()
                _safetyCircuitConditionComments.value =
                    existingCalibration.safetyCircuitConditionComments
                _linerCondition.value = existingCalibration.linerCondition.toConditionState()
                _linerConditionComments.value = existingCalibration.linerConditionComments
                _cablesCondition.value = existingCalibration.cablesCondition.toConditionState()
                _cablesConditionComments.value = existingCalibration.cablesConditionComments
                _screwsCondition.value = existingCalibration.screwsCondition.toConditionState()
                _screwsConditionComments.value = existingCalibration.screwsConditionComments
                _systemChecklistEngineerNotes.value =
                    existingCalibration.systemChecklistEngineerNotes

                _indicator6colour.value = existingCalibration.indicator6colour
                _indicator6label.value = existingCalibration.indicator6label
                _indicator5colour.value = existingCalibration.indicator5colour
                _indicator5label.value = existingCalibration.indicator5label
                _indicator4colour.value = existingCalibration.indicator4colour
                _indicator4label.value = existingCalibration.indicator4label
                _indicator3colour.value = existingCalibration.indicator3colour
                _indicator3label.value = existingCalibration.indicator3label
                _indicator2colour.value = existingCalibration.indicator2colour
                _indicator2label.value = existingCalibration.indicator2label
                _indicator1colour.value = existingCalibration.indicator1colour
                _indicator1label.value = existingCalibration.indicator1label
                _indicatorsEngineerNotes.value = existingCalibration.indicatorsEngineerNotes

                _infeedSensorFitted.value = existingCalibration.infeedSensorFitted.toYesNoState()
                _infeedSensorDetail.value = existingCalibration.infeedSensorDetail
                _infeedSensorTestMethod.value = existingCalibration.infeedSensorTestMethod
                _infeedSensorTestResult.value = existingCalibration.infeedSensorTestResult
                    .removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                _infeedSensorLatched.value = existingCalibration.infeedSensorLatched.toYesNoState()
                _infeedSensorCR.value = existingCalibration.infeedSensorCR.toYesNoState()
                _infeedSensorEngineerNotes.value = existingCalibration.infeedSensorEngineerNotes
                _infeedSensorTestPvResult.value = existingCalibration.infeedSensorTestPvResult





                _rejectConfirmSensorFitted.value =
                    existingCalibration.rejectConfirmSensorFitted.toYesNoState()
                _rejectConfirmSensorDetail.value = existingCalibration.rejectConfirmSensorDetail
                _rejectConfirmSensorTestMethod.value =
                    existingCalibration.rejectConfirmSensorTestMethod
                _rejectConfirmSensorTestResult.value =
                    existingCalibration.rejectConfirmSensorTestResult
                        .removeSurrounding("[", "]")
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                _rejectConfirmSensorLatched.value =
                    existingCalibration.rejectConfirmSensorLatched.toYesNoState()
                _rejectConfirmSensorCR.value =
                    existingCalibration.rejectConfirmSensorCR.toYesNoState()
                _rejectConfirmSensorEngineerNotes.value =
                    existingCalibration.rejectConfirmSensorEngineerNotes
                _rejectConfirmSensorTestPvResult.value =
                    existingCalibration.rejectConfirmSensorTestPvResult


                _binFullSensorFitted.value = existingCalibration.binFullSensorFitted.toYesNoState()
                _binFullSensorDetail.value = existingCalibration.binFullSensorDetail
                _binFullSensorTestMethod.value = existingCalibration.binFullSensorTestMethod
                _binFullSensorTestResult.value = existingCalibration.binFullSensorTestResult
                    .removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                _binFullSensorLatched.value =
                    existingCalibration.binFullSensorLatched.toYesNoState()
                _binFullSensorCR.value = existingCalibration.binFullSensorCR.toYesNoState()
                _binFullSensorEngineerNotes.value = existingCalibration.binFullSensorEngineerNotes
                _binFullSensorTestPvResult.value =
                    existingCalibration.binFullSensorTestPvResult



                _backupSensorFitted.value = existingCalibration.backupSensorFitted.toYesNoState()
                _backupSensorDetail.value = existingCalibration.backupSensorDetail
                _backupSensorTestMethod.value = existingCalibration.backupSensorTestMethod
                _backupSensorTestResult.value = existingCalibration.backupSensorTestResult
                    .removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                _backupSensorLatched.value = existingCalibration.backupSensorLatched.toYesNoState()
                _backupSensorCR.value = existingCalibration.backupSensorCR.toYesNoState()
                _backupSensorEngineerNotes.value = existingCalibration.backupSensorEngineerNotes
                _backupSensorTestPvResult.value = existingCalibration.backupSensorTestPvResult



                _airPressureSensorFitted.value =
                    existingCalibration.airPressureSensorFitted.toYesNoState()
                _airPressureSensorDetail.value =
                    existingCalibration.airPressureSensorDetail
                _airPressureSensorTestMethod.value =
                    existingCalibration.airPressureSensorTestMethod
                _airPressureSensorTestResult.value =
                    existingCalibration.airPressureSensorTestResult
                    .removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                _airPressureSensorLatched.value =
                    existingCalibration.airPressureSensorLatched.toYesNoState()
                _airPressureSensorCR.value =
                    existingCalibration.airPressureSensorCR.toYesNoState()
                _airPressureSensorEngineerNotes.value =
                    existingCalibration.airPressureSensorEngineerNotes
                _airPressureSensorTestPvResult.value =
                    existingCalibration.airPressureSensorTestPvResult



                _packCheckSensorFitted.value =
                    existingCalibration.packCheckSensorFitted.toYesNoState()
                _packCheckSensorDetail.value = existingCalibration.packCheckSensorDetail
                _packCheckSensorTestMethod.value = existingCalibration.packCheckSensorTestMethod
                _packCheckSensorTestResult.value = existingCalibration.packCheckSensorTestResult
                    .removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                _packCheckSensorLatched.value =
                    existingCalibration.packCheckSensorLatched.toYesNoState()
                _packCheckSensorCR.value = existingCalibration.packCheckSensorCR.toYesNoState()
                _packCheckSensorEngineerNotes.value =
                    existingCalibration.packCheckSensorEngineerNotes
                _packCheckSensorTestPvResult.value =
                    existingCalibration.packCheckSensorTestPvResult



                _speedSensorFitted.value = existingCalibration.speedSensorFitted.toYesNoState()
                _speedSensorDetail.value = existingCalibration.speedSensorDetail
                _speedSensorTestMethod.value = existingCalibration.speedSensorTestMethod
                _speedSensorTestResult.value = existingCalibration.speedSensorTestResult
                    .removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                _speedSensorLatched.value = existingCalibration.speedSensorLatched.toYesNoState()
                _speedSensorCR.value = existingCalibration.speedSensorCR.toYesNoState()
                _speedSensorEngineerNotes.value = existingCalibration.speedSensorEngineerNotes
                _speedSensorTestPvResult.value = existingCalibration.speedSensorTestPvResult



                _detectNotificationResult.value = existingCalibration.detectNotificationResult
                    .removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                _detectNotificationEngineerNotes.value =
                    existingCalibration.detectNotificationEngineerNotes

                _binDoorMonitorFitted.value =
                    existingCalibration.binDoorMonitorFitted.toYesNoState()
                _binDoorMonitorDetail.value = existingCalibration.binDoorMonitorDetail
                _binDoorStatusAsFound.value = existingCalibration.binDoorStatusAsFound
                _binDoorOpenIndication.value = existingCalibration.binDoorOpenIndication
                    .removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                _binDoorUnlockedIndication.value = existingCalibration.binDoorUnlockedIndication
                    .removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                _binDoorTimeoutTimer.value = existingCalibration.binDoorTimeoutTimer
                _binDoorTimeoutResult.value = existingCalibration.binDoorTimeoutResult
                    .removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                _binDoorLatched.value = existingCalibration.binDoorLatched.toYesNoState()
                _binDoorCR.value = existingCalibration.binDoorCR.toYesNoState()
                _binDoorEngineerNotes.value = existingCalibration.binDoorEngineerNotes
                _binDoorMonitorTestPvResult.value = existingCalibration.binDoorMonitorTestPvResult


                _operatorTestWitnessed.value =
                    existingCalibration.operatorTestWitnessed.toYesNoState()
                _operatorName.value = existingCalibration.operatorName
                _operatorTestResultFerrous.value = existingCalibration.operatorTestResultFerrous
                _operatorTestResultNonFerrous.value =
                    existingCalibration.operatorTestResultNonFerrous
                _operatorTestResultStainless.value = existingCalibration.operatorTestResultStainless
                _operatorTestResultLargeMetal.value =
                    existingCalibration.operatorTestResultLargeMetal
                _operatorTestResultCertNumberFerrous.value =
                    existingCalibration.operatorTestResultCertNumberFerrous
                _operatorTestResultCertNumberNonFerrous.value =
                    existingCalibration.operatorTestResultCertNumberNonFerrous
                _operatorTestResultCertNumberStainless.value =
                    existingCalibration.operatorTestResultCertNumberStainless
                _operatorTestResultCertNumberLargeMetal.value =
                    existingCalibration.operatorTestResultCertNumberLargeMetal
                _smeName.value = existingCalibration.smeName
                _smeEngineerNotes.value = existingCalibration.smeEngineerNotes
                _smeTestPvResult.value = existingCalibration.smeTestPvResult



//                _sensitivityCompliance.value =
//                    existingCalibration.sensitivityCompliance.toYesNoState()
//                _essentialRequirementCompliance.value =
//                    existingCalibration.essentialRequirementCompliance.toYesNoState()
//                _failsafeCompliance.value = existingCalibration.failsafeCompliance.toYesNoState()
//                _bestSensitivityCompliance.value =
//                    existingCalibration.bestSensitivityCompliance.toYesNoState()
//                _sensitivityRecommendations.value = existingCalibration.sensitivityRecommendations
//                _performanceValidationIssued.value =
//                    existingCalibration.performanceValidationIssued.toYesNoState()
                _detectionSetting1label.value = existingCalibration.detectionSetting1label
                _detectionSetting2label.value = existingCalibration.detectionSetting2label
                _detectionSetting3label.value = existingCalibration.detectionSetting3label
                _detectionSetting4label.value = existingCalibration.detectionSetting4label
                _detectionSetting5label.value = existingCalibration.detectionSetting5label
                _detectionSetting6label.value = existingCalibration.detectionSetting6label
                _detectionSetting7label.value = existingCalibration.detectionSetting7label
                _detectionSetting8label.value = existingCalibration.detectionSetting8label

                //endregion

            } else {
                // Handle the case where no calibration exists
                InAppLogger.d("No existing calibration found. Starting a new one.")
                saveNewCalibration()
            }
        }
    }


    private val _step = MutableStateFlow(0)
    val step: StateFlow<Int> = _step


    private val _currentScreenNextEnabled = MutableStateFlow(true)
    val currentScreenNextEnabled = _currentScreenNextEnabled

    fun setCurrentScreenNextEnabled(enabled: Boolean) {
        _currentScreenNextEnabled.value = enabled
    }




    // -----------------------------------------------------------------------------
    //  GENERAL FIELD STATE DUMP
    // -----------------------------------------------------------------------------
    // This section contains all the mutableState fields and their setters for the
    // calibration UI.
    // -----------------------------------------------------------------------------


    // region MUTABLE STATES FOR UI STATE OBSERVATION

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _isNavigating = MutableStateFlow(false)
    val isNavigating: StateFlow<Boolean> get() = _isNavigating

    fun startNavigation() {
        _isNavigating.value = true
    }

    fun finishNavigation() {
        _isNavigating.value = false
    }

    // endregion



    fun disableFerrousTest(){
        _sampleCertificateNumberFerrous.value = "N/A"
        _detectRejectFerrousLeading.value = YesNoState.NA
        _detectRejectFerrousMiddle.value = YesNoState.NA
        _detectRejectFerrousTrailing.value = YesNoState.NA
        _peakSignalFerrousLeading.value = "N/A"
        _peakSignalFerrousMiddle.value = "N/A"
        _peakSignalFerrousTrailing.value = "N/A"
        _ferrousTestPvResult.value = "N/A"
    }

    fun enableFerrousTest(){
        _sampleCertificateNumberFerrous.value = ""
        _detectRejectFerrousLeading.value = YesNoState.NO
        _detectRejectFerrousMiddle.value = YesNoState.NO
        _detectRejectFerrousTrailing.value = YesNoState.NO
        _peakSignalFerrousLeading.value = ""
        _peakSignalFerrousMiddle.value = ""
        _peakSignalFerrousTrailing.value = ""
        _ferrousTestPvResult.value = ""
    }

    fun disableNonFerrousTest(){
        _sampleCertificateNumberNonFerrous.value = "N/A"
        _detectRejectNonFerrousLeading.value = YesNoState.NA
        _detectRejectNonFerrousMiddle.value = YesNoState.NA
        _detectRejectNonFerrousTrailing.value = YesNoState.NA
        _peakSignalNonFerrousLeading.value = "N/A"
        _peakSignalNonFerrousMiddle.value = "N/A"
        _peakSignalNonFerrousTrailing.value = "N/A"
        _nonFerrousTestPvResult.value = "N/A"
    }

    fun enableNonFerrousTest(){
        _sampleCertificateNumberNonFerrous.value = ""
        _detectRejectNonFerrousLeading.value = YesNoState.NO
        _detectRejectNonFerrousMiddle.value = YesNoState.NO
        _detectRejectNonFerrousTrailing.value = YesNoState.NO
        _peakSignalNonFerrousLeading.value = ""
        _peakSignalNonFerrousMiddle.value = ""
        _peakSignalNonFerrousTrailing.value = ""
        _nonFerrousTestPvResult.value = ""
    }

    fun disableStainlessTest(){
        _sampleCertificateNumberStainless.value = "N/A"
        _detectRejectStainlessLeading.value = YesNoState.NA
        _detectRejectStainlessMiddle.value = YesNoState.NA
        _detectRejectStainlessTrailing.value = YesNoState.NA
        _peakSignalStainlessLeading.value = "N/A"
        _peakSignalStainlessMiddle.value = "N/A"
        _peakSignalStainlessTrailing.value = "N/A"
        _stainlessTestPvResult.value = "N/A"

    }

    fun enableStainlessTest(){
        _sampleCertificateNumberStainless.value = ""
        _detectRejectStainlessLeading.value = YesNoState.NO
        _detectRejectStainlessMiddle.value = YesNoState.NO
        _detectRejectStainlessTrailing.value = YesNoState.NO
        _peakSignalStainlessLeading.value = ""
        _peakSignalStainlessMiddle.value = ""
        _peakSignalStainlessTrailing.value = ""
        _stainlessTestPvResult.value = ""

    }









    // -----------------------------------------------------------------------------
//  DATABASE UPDATE TRIGGERS
// -----------------------------------------------------------------------------
// Each section of the calibration process has its own "update" function here.
// These functions do NOT build SQL queries directly. Instead:
//
//   1. They convert the current ViewModel state into a structured update object
//      using a `toXUpdate()` mapper (defined in DatabaseUpdates.kt).
//
//   2. They hand that object to the CalibrationRepository, which is the only
//      class allowed to talk to the DAO.
//
// This keeps the ViewModel focused on UI state and business logic, while the
// repository handles the persistence layer.
//
// If you ever need to change how a section of calibration is saved to the
// database, update the corresponding mapper and repository function—NOT the
// ViewModel. The ViewModel should remain thin and readable.
// -----------------------------------------------------------------------------

    //region UPDATE FUNCTIONS

    private fun saveNewCalibration() {
        val insert = toNewCalibrationInsert()

        viewModelScope.launch {
            calibrationRepository.insertNewCalibration(insert)
        }
    }

    fun shouldSkipToSummary(): Boolean {
        return !canPerformCalibration.value &&
                reasonForNotCalibrating.value.isNotBlank()
    }


    fun persistCurrentScreen(route: String) {
        when (route.removeSuffix("/{calibrationId}")) {

            "MetalDetectorConveyorCalibrationStart" ->
                updateCalibrationStart()

            "CalMetalDetectorConveyorSensitivityRequirements" ->
                updateSensitivityRequirements()

            "CalMetalDetectorConveyorProductDetails" ->
                updateProductDetails()

            "CalMetalDetectorConveyorDetectionSettingsAsFound" ->
                updateDetectionSettingsAsFound()

            "CalMetalDetectorConveyorSensitivityAsFound" ->
                updateSensitivitiesAsFound()

            "CalMetalDetectorConveyorFerrousTest" ->
                updateFerrousResult()

            "CalMetalDetectorConveyorNonFerrousTest" ->
                updateNonFerrousResult()

            "CalMetalDetectorConveyorStainlessTest" ->
                updateStainlessResult()

            "CalMetalDetectorConveyorLargeMetalTest" ->
                updateLargeMetalResult()

            "CalMetalDetectorConveyorDetectionSettingsAsLeft" ->
                updateDetectionSettingAsLeft()

            "CalMetalDetectorConveyorRejectSettings" ->
                updateRejectSettings()

            "CalMetalDetectorConveyorSystemChecklist" ->
                updateSystemChecklist()

            "CalMetalDetectorConveyorConveyorDetails" ->
                updateConveyorDetails()

            "CalMetalDetectorConveyorIndicators" ->
                updateIndicators()

            "CalMetalDetectorConveyorInfeedPEC" ->
                updateInfeedSensor()

            "CalMetalDetectorConveyorRejectConfirmPEC" ->
                updateRejectConfirmSensor()

            "CalMetalDetectorConveyorBinFullPEC" ->
                updateBinFullSensor()

            "CalMetalDetectorConveyorBackupPEC" ->
                updateBackupSensor()

            "CalMetalDetectorConveyorAirPressureSensor" ->
                updateAirPressureSensor()

            "CalMetalDetectorConveyorPackCheckSensor" ->
                updatePackCheckSensor()

            "CalMetalDetectorConveyorSpeedSensor" ->
                updateSpeedSensor()

            "CalMetalDetectorConveyorDetectNotification" ->
                updateDetectNotification()

            "CalMetalDetectorConveyorBinDoorMonitor" ->
                updateBinDoorMonitor()

            "CalMetalDetectorConveyorSmeDetails" ->
                updateOperatorTest()

//            "CalMetalDetectorConveyorComplianceConfirmation" ->
//                updateComplianceConfirmation()

            else -> {
                // Do nothing for routes we don’t recognise
            }
        }
    }




    fun updateCalibrationStart() {
        if (!pvRequired.value) {
            setAllPvResultsNa()
        }

        val update = toCalibrationStartUpdate()

        viewModelScope.launch {
            calibrationRepository.updateCalibrationStart(update)
        }
    }

    fun updateSensitivityRequirements() {
        val update = toSensitivityRequirementsUpdate()

        viewModelScope.launch {
            calibrationRepository.updateSensitivityRequirements(update)
        }
    }

    fun updateProductDetails() {
        val update = toProductDetailsUpdate()

        viewModelScope.launch {
            calibrationRepository.updateProductDetails(update)
        }
    }

    fun updateDetectionSettingsAsFound() {
        val update = toDetectionSettingsAsFoundUpdate()

        viewModelScope.launch {
            calibrationRepository.updateDetectionSettingsAsFound(update)
        }

    }

    fun updateSensitivitiesAsFound() {
        val update = toSensitivitiesAsFoundUpdate()

        viewModelScope.launch {
            calibrationRepository.updateSensitivitiesAsFound(update)
        }
    }

    fun updateFerrousResult() {
        val update = toFerrousResultUpdate()

        viewModelScope.launch {
            calibrationRepository.updateFerrousResult(update)
        }
    }

    fun updateNonFerrousResult() {
        val update = toNonFerrousResultUpdate()

        viewModelScope.launch {
            calibrationRepository.updateNonFerrousResult(update)
        }
    }

    fun updateStainlessResult() {
        val update = toStainlessResultUpdate()

        viewModelScope.launch {
            calibrationRepository.updateStainlessResult(update)
        }
    }

    fun updateLargeMetalResult() {
        val update = toLargeMetalResultUpdate()

        viewModelScope.launch {
            calibrationRepository.updateLargeMetalResult(update)
        }
    }

    fun updateDetectionSettingAsLeft() {
        val update = toDetectionSettingAsLeftUpdate()

        viewModelScope.launch {
            calibrationRepository.updateDetectionSettingAsLeft(update)
        }
    }

    fun updateRejectSettings() {
        val update = toRejectSettingsUpdate()

        viewModelScope.launch {
            calibrationRepository.updateRejectSettings(update)
        }
    }

    fun updateConveyorDetails() {
        val update = toConveyorDetailsUpdate()

        viewModelScope.launch {
            calibrationRepository.updateConveyorDetails(update)
        }
    }

    fun updateSystemChecklist() {
        val update = toSystemChecklistUpdate()

        viewModelScope.launch {
            calibrationRepository.updateSystemChecklist(update)
        }
    }

    fun updateIndicators() {
        val update = toIndicatorsUpdate()

        viewModelScope.launch {
            calibrationRepository.updateIndicators(update)
        }
    }

    fun updateInfeedSensor() {
        val update = toInfeedSensorUpdate()

        viewModelScope.launch {
            calibrationRepository.updateInfeedSensor(update)
        }
    }

    fun updateRejectConfirmSensor() {
        val update = toRejectConfirmSensorUpdate()

        viewModelScope.launch {
            calibrationRepository.updateRejectConfirmSensor(update)
        }
    }

    fun updateBinFullSensor() {
        val update = toBinFullSensorUpdate()
        viewModelScope.launch { calibrationRepository.updateBinFullSensor(update) }
    }

    fun updateBackupSensor() {
        val update = toBackupSensorUpdate()
        viewModelScope.launch { calibrationRepository.updateBackupSensor(update) }
    }

    fun updateAirPressureSensor() {
        val update = toAirPressureSensorUpdate()
        viewModelScope.launch { calibrationRepository.updateAirPressureSensor(update) }
    }

    fun updatePackCheckSensor() {
        val update = toPackCheckSensorUpdate()
        viewModelScope.launch { calibrationRepository.updatePackCheckSensor(update) }
    }

    fun updateSpeedSensor() {
        val update = toSpeedSensorUpdate()
        viewModelScope.launch { calibrationRepository.updateSpeedSensor(update) }
    }

    fun updateDetectNotification() {
        val update = toDetectNotificationUpdate()
        viewModelScope.launch { calibrationRepository.updateDetectNotification(update) }
    }

    fun updateBinDoorMonitor() {
        val update = toBinDoorMonitorUpdate()
        viewModelScope.launch { calibrationRepository.updateBinDoorMonitor(update) }
    }

    fun updateOperatorTest() {
        val update = toOperatorTestUpdate()
        viewModelScope.launch { calibrationRepository.updateOperatorTest(update) }
    }

//    fun updateComplianceConfirmation() {
//        val update = toComplianceConfirmationUpdate()
//        viewModelScope.launch { calibrationRepository.updateComplianceConfirmation(update) }
//    }

    fun updateDetectionSettingLabels() {
        val update = toDetectionSettingLabelsUpdate()
        viewModelScope.launch { calibrationRepository.updateDetectionSettingLabels(update) }
    }

    fun updateCalibrationEnd() {
        val update = toCalibrationEndUpdate()
        viewModelScope.launch { calibrationRepository.updateCalibrationEnd(update) }
    }


    //endregion




// -----------------------------------------------------------------------------
//  CALIBRATION FIELD STATE DUMP (a.k.a. The Sock Drawer)
// -----------------------------------------------------------------------------
// This section contains all the mutableState fields and their setters for the
// calibration process.
// -----------------------------------------------------------------------------

    //region MUTABLE STATE FIELDS, AND SETTERS

//    val location = mutableStateOf("")
//    private var originalLocation: String? = null
//
//    fun setInitialLocation(value: String) {
//        originalLocation = value
//        location.value = value
//    }
//
//    fun locationChanged(): Boolean {
//        val old = originalLocation?.trim() ?: ""
//        val new = location.value.trim()
//        return !new.equals(old, ignoreCase = true)
//    }

    private val _calibrationId = mutableStateOf(calibrationId)
    val calibrationId: State<String> = _calibrationId

    private val _serialNumber = mutableStateOf(serialNumber)
    val serialNumber: State<String> = _serialNumber

    private val _customerName = mutableStateOf(customerName)
    val customerName: State<String> = _customerName

    private val _modelDescription = mutableStateOf(modelDescription)
    val modelDescription: State<String> = _modelDescription

    private val _systemTypeDescription = mutableStateOf(systemTypeDescription)
    val systemTypeDescription: State<String> = _systemTypeDescription


    private val _customerId = mutableIntStateOf(customerId)
    val customerId: State<Int> = _customerId

    private val _systemId = mutableIntStateOf(systemId)
    val systemId: State<Int> = _systemId

    private val _cloudSystemId = mutableIntStateOf(cloudSystemId)
    val cloudSystemId: State<Int> = _cloudSystemId

    private val _tempSystemId = mutableIntStateOf(tempSystemId)
    val tempSystemId: State<Int> = _tempSystemId

    private val _systemTypeId = mutableIntStateOf(systemTypeId)
    val systemTypeId: State<Int> = _systemTypeId


    private val _modelId = mutableIntStateOf(modelId)
    val modelId: State<Int> = _modelId

    private val _calibrationStartTime = mutableStateOf(
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    )
    val calibrationStartTime: State<String> = _calibrationStartTime


    private val _isSynced = mutableStateOf<Boolean?>(false)
    val isSynced: State<Boolean?> = _isSynced

//    private val _engineerId = mutableStateOf("")
//    val engineerId: State<String> = _engineerId


    private val _detectionSetting1label = mutableStateOf(detectionSetting1label)
    val detectionSetting1label: State<String> = _detectionSetting1label


    fun setDetectionSetting1Label(newDetectionSetting1label: String) {
        _detectionSetting1label.value = newDetectionSetting1label
    }


    private val _detectionSetting2label = mutableStateOf(detectionSetting2label)
    val detectionSetting2label: State<String> = _detectionSetting2label


    fun setDetectionSetting2Label(newDetectionSetting2label: String) {
        _detectionSetting2label.value = newDetectionSetting2label
    }


    private val _detectionSetting3label = mutableStateOf(detectionSetting3label)
    val detectionSetting3label: State<String> = _detectionSetting3label


    fun setDetectionSetting3Label(newDetectionSetting3label: String) {
        _detectionSetting3label.value = newDetectionSetting3label
    }


    private val _detectionSetting4label = mutableStateOf(detectionSetting4label)
    val detectionSetting4label: State<String> = _detectionSetting4label


    fun setDetectionSetting4Label(newDetectionSetting4label: String) {
        _detectionSetting4label.value = newDetectionSetting4label
    }


    private val _detectionSetting5label = mutableStateOf(detectionSetting5label)
    val detectionSetting5label: State<String> = _detectionSetting5label


    fun setDetectionSetting5Label(newDetectionSetting5label: String) {

        _detectionSetting5label.value = newDetectionSetting5label
    }


    private val _detectionSetting6label = mutableStateOf(detectionSetting6label)
    val detectionSetting6label: State<String> = _detectionSetting6label


    fun setDetectionSetting6Label(newDetectionSetting6label: String) {
        _detectionSetting6label.value = newDetectionSetting6label
    }


    private val _detectionSetting7label = mutableStateOf(detectionSetting7label)
    val detectionSetting7label: State<String> = _detectionSetting7label


    fun setDetectionSetting7Label(newDetectionSetting7label: String) {
        _detectionSetting7label.value = newDetectionSetting7label
    }

    private val _detectionSetting8label = mutableStateOf(detectionSetting8label)
    val detectionSetting8label: State<String> = _detectionSetting8label


    fun setDetectionSetting8Label(newDetectionSetting8label: String) {
        _detectionSetting8label.value = newDetectionSetting8label
    }

    private val _lastLocation = mutableStateOf(lastLocation)
    val lastLocation: State<String> = _lastLocation


    private var _newLocation = mutableStateOf("")
    val newLocation: State<String> = _newLocation

    // Functions to update the state
    fun setNewLocation(location: String) {
        _newLocation.value = location
    }

    private var _canPerformCalibration = mutableStateOf(false)
    val canPerformCalibration: State<Boolean> = _canPerformCalibration

    fun setCanPerformCalibration(canPerform: Boolean) {
        _canPerformCalibration.value = canPerform
    }

    private var _pvRequired = mutableStateOf(false)
    val pvRequired: State<Boolean> = _pvRequired

    fun setPvRequired(pvRequired: Boolean) {
        _pvRequired.value = pvRequired
    }

    private var _reasonForNotCalibrating = mutableStateOf("")
    val reasonForNotCalibrating: State<String> = _reasonForNotCalibrating

    fun setReasonForNotCalibrating(reason: String) {
        _reasonForNotCalibrating.value = reason
    }

//    private val _appVersion = mutableStateOf("")
//    val appVersion: State<String> = _appVersion

//    fun setAppVersion(version: String) {
//        _appVersion.value = version
//    }

//    private val _engineerId = mutableStateOf("")
//    val engineerId: State<String> = _engineerId
//
//    fun setEngineerId(engineer: String) {
//        _engineerId.value = engineer
//    }

    private val _desiredCop = MutableStateFlow<List<String>>(emptyList())
    val desiredCop: StateFlow<List<String>> get() = _desiredCop

//    fun setDesiredCop(newSelection: List<String>) {
//        _desiredCop.value = newSelection
//    }

    private val _startCalibrationNotes = mutableStateOf("")
    val startCalibrationNotes: State<String> = _startCalibrationNotes

//    fun setStartCalibrationNotes(newValue: String) {
//        _startCalibrationNotes.value = newValue
//    }


    //-----------------------------------------------------------------------------Product Settings

    private val _productDescription = mutableStateOf("")
    val productDescription: State<String> = _productDescription

    // Function to update product description
    fun setProductDescription(newDescription: String) {
        _productDescription.value = newDescription
    }

    private val _productLibraryReference = mutableStateOf("")
    val productLibraryReference: State<String> = _productLibraryReference

    // Function to update product library reference
    fun setProductLibraryReference(newReference: String) {
        _productLibraryReference.value = newReference
    }

    private val _productLibraryNumber = mutableStateOf("")
    val productLibraryNumber: State<String> = _productLibraryNumber

    // Function to update product library number
    fun setProductLibraryNumber(newNumber: String) {
        _productLibraryNumber.value = newNumber
    }

    private val _productLength = mutableStateOf("")
    val productLength: State<String> = _productLength

    // Function to update product height
    fun setProductLength(newLength: String) {
        _productLength.value = newLength
    }

    private val _productWidth = mutableStateOf("")
    val productWidth: State<String> = _productWidth

    // Function to update product height
    fun setProductWidth(newWidth: String) {
        _productWidth.value = newWidth
    }

    private val _productHeight = mutableStateOf("")
    val productHeight: State<String> = _productHeight

    private val _sensitivityData = mutableStateOf<ConveyorRetailerSensitivitiesEntity?>(null)
    val sensitivityData: State<ConveyorRetailerSensitivitiesEntity?> get() = _sensitivityData

    // Function to update product height
//    fun setProductHeight(newHeight: String) {
//        _productHeight.value = newHeight
//    }

    // Function to set product height and fetch sensitivities
    fun setProductHeight(newHeight: String) {
        _productHeight.value = newHeight
        val heightMm = newHeight.toDoubleOrNull() ?: return // Handle invalid input
        fetchSensitivityData(heightMm)
    }

    // Function to fetch sensitivity data
    private fun fetchSensitivityData(heightMm: Double) {
        viewModelScope.launch {
            // Fetch sensitivities from repository
            val sensitivities = retailerSensitivitiesRepo.getSensitivitiesByHeight(heightMm)
            _sensitivityData.value = sensitivities
        }
    }

    private val _productDetailsEngineerNotes = mutableStateOf("")
    val productDetailsEngineerNotes: State<String> = _productDetailsEngineerNotes

    fun setProductDetailsEngineerNotes(newValue: String) {
        _productDetailsEngineerNotes.value = newValue
    }

    //-----------------------------------------------------------------Detection Settings 'As Found'

    private val _detectionSettingAsFound1 = mutableStateOf("")
    val detectionSettingAsFound1: State<String> = _detectionSettingAsFound1


    fun setDetectionSettingAsFound1(newDetectionSettingAsFound1: String) {
        _detectionSettingAsFound1.value = newDetectionSettingAsFound1
    }


    ///

    private val _detectionSettingAsFound2 = mutableStateOf("")
    val detectionSettingAsFound2: State<String> = _detectionSettingAsFound2


    fun setDetectionSettingAsFound2(newDetectionSettingAsFound2: String) {
        _detectionSettingAsFound2.value = newDetectionSettingAsFound2
    }


    ///

    private val _detectionSettingAsFound3 = mutableStateOf("")
    val detectionSettingAsFound3: State<String> = _detectionSettingAsFound3


    fun setDetectionSettingAsFound3(newDetectionSettingAsFound3: String) {
        _detectionSettingAsFound3.value = newDetectionSettingAsFound3
    }


    ///

    private val _detectionSettingAsFound4 = mutableStateOf("")
    val detectionSettingAsFound4: State<String> = _detectionSettingAsFound4


    fun setDetectionSettingAsFound4(newDetectionSettingAsFound4: String) {
        _detectionSettingAsFound4.value = newDetectionSettingAsFound4
    }


    ///

    private val _detectionSettingAsFound5 = mutableStateOf("")
    val detectionSettingAsFound5: State<String> = _detectionSettingAsFound5


    fun setDetectionSettingAsFound5(newDetectionSettingAsFound5: String) {
        _detectionSettingAsFound5.value = newDetectionSettingAsFound5
    }


    ///

    private val _detectionSettingAsFound6 = mutableStateOf("")
    val detectionSettingAsFound6: State<String> = _detectionSettingAsFound6


    fun setDetectionSettingAsFound6(newDetectionSettingAsFound6: String) {
        _detectionSettingAsFound6.value = newDetectionSettingAsFound6
    }

    private val _detectionSettingAsFound7 = mutableStateOf("")
    val detectionSettingAsFound7: State<String> = _detectionSettingAsFound7


    fun setDetectionSettingAsFound7(newDetectionSettingAsFound7: String) {
        _detectionSettingAsFound7.value = newDetectionSettingAsFound7
    }

    private val _detectionSettingAsFound8 = mutableStateOf("")
    val detectionSettingAsFound8: State<String> = _detectionSettingAsFound8


    fun setDetectionSettingAsFound8(newDetectionSettingAsFound8: String) {
        _detectionSettingAsFound8.value = newDetectionSettingAsFound8
    }


    private val _detectionSettingAsFoundEngineerNotes = mutableStateOf("")
    val detectionSettingAsFoundEngineerNotes: State<String> = _detectionSettingAsFoundEngineerNotes


    fun setDetectionSettingAsFoundEngineerNotes(newValue: String) {
        _detectionSettingAsFoundEngineerNotes.value = newValue
    }

    private val _detectionSettingPvResult = mutableStateOf("")
    val detectionSettingPvResult: State<String> = _detectionSettingPvResult

    fun setDetectionSettingPvResult(newValue: String) {
        _detectionSettingPvResult.value = newValue
    }



    //----------------------------------------------------------------------Sensitivity Requirements

    private val _sensitivityRequirementFerrous = mutableStateOf("")
    val sensitivityRequirementFerrous: State<String> = _sensitivityRequirementFerrous



    fun setSensitivityRequirementFerrous(newValue: String) {
        _sensitivityRequirementFerrous.value = newValue
    }

    private val _sensitivityRequirementNonFerrous = mutableStateOf("")
    val sensitivityRequirementNonFerrous: State<String> = _sensitivityRequirementNonFerrous


    fun setSensitivityRequirementNonFerrous(newValue: String) {
        _sensitivityRequirementNonFerrous.value = newValue
    }

    private val _sensitivityRequirementStainless = mutableStateOf("")
    val sensitivityRequirementStainless: State<String> = _sensitivityRequirementStainless


    fun setSensitivityRequirementStainless(newValue: String) {
        _sensitivityRequirementStainless.value = newValue
    }


    private val _sensitivityRequirementEngineerNotes = mutableStateOf("")
    val sensitivityRequirementEngineerNotes: State<String> = _sensitivityRequirementEngineerNotes


    fun setSensitivityRequirementEngineerNotes(newSensitivityRequirementEngineerNotes: String) {
        _sensitivityRequirementEngineerNotes.value = newSensitivityRequirementEngineerNotes
    }

    //----------------------------------------------------------------------Sensitivities 'As Found'

    private val _sensitivityAccessRestriction = mutableStateOf("")
    val sensitivityAccessRestriction: State<String> = _sensitivityAccessRestriction


    fun setSensitivityAccessRestriction(newSensitivityAccessRestriction: String) {
        _sensitivityAccessRestriction.value = newSensitivityAccessRestriction
    }

    private val _sensitivityAsFoundFerrous = mutableStateOf("")
    val sensitivityAsFoundFerrous: State<String> = _sensitivityAsFoundFerrous


    fun setSensitivityAsFoundFerrous(newSensitivityAsFoundFerrous: String) {
        _sensitivityAsFoundFerrous.value = newSensitivityAsFoundFerrous
    }

    private val _sensitivityAsFoundFerrousPeakSignal = mutableStateOf("")
    val sensitivityAsFoundFerrousPeakSignal: State<String> = _sensitivityAsFoundFerrousPeakSignal


    fun setSensitivityAsFoundFerrousPeakSignal(newSensitivityAsFoundFerrousPeakSignal: String) {
        _sensitivityAsFoundFerrousPeakSignal.value = newSensitivityAsFoundFerrousPeakSignal
    }

    private val _sensitivityAsFoundNonFerrous = mutableStateOf("")
    val sensitivityAsFoundNonFerrous: State<String> = _sensitivityAsFoundNonFerrous


    fun setSensitivityAsFoundNonFerrous(newSensitivityAsFoundNonFerrous: String) {
        _sensitivityAsFoundNonFerrous.value = newSensitivityAsFoundNonFerrous
    }

    private val _sensitivityAsFoundNonFerrousPeakSignal = mutableStateOf("")
    val sensitivityAsFoundNonFerrousPeakSignal: State<String> =
        _sensitivityAsFoundNonFerrousPeakSignal


    fun setSensitivityAsFoundNonFerrousPeakSignal(newSensitivityAsFoundNonFerrousPeakSignal: String) {
        _sensitivityAsFoundNonFerrousPeakSignal.value = newSensitivityAsFoundNonFerrousPeakSignal
    }

    private val _sensitivityAsFoundStainless = mutableStateOf("")
    val sensitivityAsFoundStainless: State<String> = _sensitivityAsFoundStainless


    fun setSensitivityAsFoundStainless(newSensitivityAsFoundStainless: String) {
        _sensitivityAsFoundStainless.value = newSensitivityAsFoundStainless
    }

    private val _sensitivityAsFoundStainlessPeakSignal = mutableStateOf("")
    val sensitivityAsFoundStainlessPeakSignal: State<String> =
        _sensitivityAsFoundStainlessPeakSignal


    fun setSensitivityAsFoundStainlessPeakSignal(newSensitivityAsFoundStainlessPeakSignal: String) {
        _sensitivityAsFoundStainlessPeakSignal.value = newSensitivityAsFoundStainlessPeakSignal
    }

    private val _productPeakSignalAsFound = mutableStateOf("")
    val productPeakSignalAsFound: State<String> = _productPeakSignalAsFound


    fun setProductPeakSignalAsFound(newProductPeakSignalAsFound: String) {
        _productPeakSignalAsFound.value = newProductPeakSignalAsFound
    }

    private val _sensitivityAsFoundEngineerNotes = mutableStateOf("")
    val sensitivityAsFoundEngineerNotes: State<String> = _sensitivityAsFoundEngineerNotes


    fun setSensitivityAsFoundEngineerNotes(newValue: String) {
        _sensitivityAsFoundEngineerNotes.value = newValue
    }

    //--------------------------------------------------------------------Ferrous Sensitivity Result


    private val _sensitivityAsLeftFerrous = mutableStateOf("")
    val sensitivityAsLeftFerrous: State<String> = _sensitivityAsLeftFerrous


    fun setSensitivityAsLeftFerrous(value: String) {
        _sensitivityAsLeftFerrous.value = value}


    // Sample Certificate Number
    private val _sampleCertificateNumberFerrous = mutableStateOf("")
    val sampleCertificateNumberFerrous: State<String> = _sampleCertificateNumberFerrous


    fun setSampleCertificateNumberFerrous(newSampleCertificateNumberFerrous: String) {
        _sampleCertificateNumberFerrous.value = newSampleCertificateNumberFerrous
    }

    // Leading edge detection & signal
    private val _detectRejectFerrousLeading = mutableStateOf(YesNoState.NO)
    val detectRejectFerrousLeading: State<YesNoState> = _detectRejectFerrousLeading

    fun setDetectRejectFerrousLeading(newValue: YesNoState) {
        _detectRejectFerrousLeading.value = newValue
    }

    private val _peakSignalFerrousLeading = mutableStateOf("")
    val peakSignalFerrousLeading: State<String> = _peakSignalFerrousLeading

    fun setPeakSignalFerrousLeading(newPeakSignalFerrousLeading: String) {
        _peakSignalFerrousLeading.value = newPeakSignalFerrousLeading
    }

    // Middle detection & signal
    private val _detectRejectFerrousMiddle = mutableStateOf(YesNoState.NO)
    val detectRejectFerrousMiddle: State<YesNoState> = _detectRejectFerrousMiddle

    fun setDetectRejectFerrousMiddle(newValue: YesNoState) {
        _detectRejectFerrousMiddle.value = newValue
    }

    private val _peakSignalFerrousMiddle = mutableStateOf("")
    val peakSignalFerrousMiddle: State<String> = _peakSignalFerrousMiddle

    fun setPeakSignalFerrousMiddle(newPeakSignalFerrousMiddle: String) {
        _peakSignalFerrousMiddle.value = newPeakSignalFerrousMiddle
    }

    // Trailing edge detection & signal

    private val _detectRejectFerrousTrailing = mutableStateOf(YesNoState.NO)
    val detectRejectFerrousTrailing: State<YesNoState> = _detectRejectFerrousTrailing

    fun setDetectRejectFerrousTrailing(newValue: YesNoState) {
        _detectRejectFerrousTrailing.value = newValue
    }

    private val _peakSignalFerrousTrailing = mutableStateOf("")
    val peakSignalFerrousTrailing: State<String> = _peakSignalFerrousTrailing

    fun setPeakSignalFerrousTrailing(newPeakSignalFerrousTrailing: String) {
        _peakSignalFerrousTrailing.value = newPeakSignalFerrousTrailing
    }

    private val _ferrousTestEngineerNotes = mutableStateOf("")
    val ferrousTestEngineerNotes: State<String> = _ferrousTestEngineerNotes


    fun setFerrousTestEngineerNotes(newValue: String) {
        _ferrousTestEngineerNotes.value = newValue
    }

    private val _ferrousTestPvResult = mutableStateOf("")
    val ferrousTestPvResult: State<String> = _ferrousTestPvResult


    fun setFerrousTestPvResult(newValue: String) {
        _ferrousTestPvResult.value = newValue
    }


    //-----------------------------------------------------------------NonFerrous Sensitivity Result

    private val _sensitivityAsLeftNonFerrous = mutableStateOf("")
    val sensitivityAsLeftNonFerrous: State<String> = _sensitivityAsLeftNonFerrous


    fun setSensitivityAsLeftNonFerrous(newSensitivityAsLeftNonFerrous: String) {
        _sensitivityAsLeftNonFerrous.value = newSensitivityAsLeftNonFerrous
    }

    //-----

    private val _detectRejectNonFerrousLeading = mutableStateOf(YesNoState.NO)
    val detectRejectNonFerrousLeading: State<YesNoState> = _detectRejectNonFerrousLeading

    fun setDetectRejectNonFerrousLeading(newValue: YesNoState) {
        _detectRejectNonFerrousLeading.value = newValue
    }

    //-----

    private val _detectRejectNonFerrousMiddle = mutableStateOf(YesNoState.NO)
    val detectRejectNonFerrousMiddle: State<YesNoState> = _detectRejectNonFerrousMiddle

    fun setDetectRejectNonFerrousMiddle(newValue: YesNoState) {
        _detectRejectNonFerrousMiddle.value = newValue
    }

    //-----

    private val _detectRejectNonFerrousTrailing = mutableStateOf(YesNoState.NO)
    val detectRejectNonFerrousTrailing: State<YesNoState> = _detectRejectNonFerrousTrailing

    fun setDetectRejectNonFerrousTrailing(newValue: YesNoState) {
        _detectRejectNonFerrousTrailing.value = newValue
    }

    //-----

    private val _peakSignalNonFerrousLeading = mutableStateOf("")
    val peakSignalNonFerrousLeading: State<String> = _peakSignalNonFerrousLeading

    fun setPeakSignalNonFerrousLeading(newPeakSignalNonFerrousLeading: String) {
        _peakSignalNonFerrousLeading.value = newPeakSignalNonFerrousLeading
    }

    //-----

    private val _peakSignalNonFerrousMiddle = mutableStateOf("")
    val peakSignalNonFerrousMiddle: State<String> = _peakSignalNonFerrousMiddle

    fun setPeakSignalNonFerrousMiddle(newPeakSignalNonFerrousMiddle: String) {
        _peakSignalNonFerrousMiddle.value = newPeakSignalNonFerrousMiddle
    }

    //-----

    private val _peakSignalNonFerrousTrailing = mutableStateOf("")
    val peakSignalNonFerrousTrailing: State<String> = _peakSignalNonFerrousTrailing

    fun setPeakSignalNonFerrousTrailing(newPeakSignalNonFerrousTrailing: String) {
        _peakSignalNonFerrousTrailing.value = newPeakSignalNonFerrousTrailing
    }

    //-----

    private val _sampleCertificateNumberNonFerrous = mutableStateOf("")
    val sampleCertificateNumberNonFerrous: State<String> = _sampleCertificateNumberNonFerrous


    fun setSampleCertificateNumberNonFerrous(newSampleCertificateNumberNonFerrous: String) {
        _sampleCertificateNumberNonFerrous.value = newSampleCertificateNumberNonFerrous
    }

    //-----

    private val _nonFerrousTestEngineerNotes = mutableStateOf("")
    val nonFerrousTestEngineerNotes: State<String> = _nonFerrousTestEngineerNotes


    fun setNonFerrousTestEngineerNotes(newValue: String) {
        _nonFerrousTestEngineerNotes.value = newValue
    }

    private val _nonFerrousTestPvResult = mutableStateOf("")
    val nonFerrousTestPvResult: State<String> = _nonFerrousTestPvResult

    fun setNonFerrousTestPvResult(newValue: String) {
        _nonFerrousTestPvResult.value = newValue
    }

    //-----------------------------------------------------------------Stainless Sensitivity Result

    private val _sensitivityAsLeftStainless = mutableStateOf("")
    val sensitivityAsLeftStainless: State<String> = _sensitivityAsLeftStainless


    fun setSensitivityAsLeftStainless(newSensitivityAsLeftStainless: String) {
        _sensitivityAsLeftStainless.value = newSensitivityAsLeftStainless
    }


    //-----

    private val _peakSignalStainlessLeading = mutableStateOf("")
    val peakSignalStainlessLeading: State<String> = _peakSignalStainlessLeading

    fun setPeakSignalStainlessLeading(newPeakSignalStainlessLeading: String) {
        _peakSignalStainlessLeading.value = newPeakSignalStainlessLeading
    }

    //-----

    private val _peakSignalStainlessMiddle = mutableStateOf("")
    val peakSignalStainlessMiddle: State<String> = _peakSignalStainlessMiddle

    fun setPeakSignalStainlessMiddle(newPeakSignalStainlessMiddle: String) {
        _peakSignalStainlessMiddle.value = newPeakSignalStainlessMiddle
    }

    //------

    private val _peakSignalStainlessTrailing = mutableStateOf("")
    val peakSignalStainlessTrailing: State<String> = _peakSignalStainlessTrailing

    fun setPeakSignalStainlessTrailing(newPeakSignalStainlessTrailing: String) {
        _peakSignalStainlessTrailing.value = newPeakSignalStainlessTrailing
    }

    //------

    private val _sampleCertificateNumberStainless = mutableStateOf("")
    val sampleCertificateNumberStainless: State<String> = _sampleCertificateNumberStainless


    fun setSampleCertificateNumberStainless(newSampleCertificateNumberStainless: String) {
        _sampleCertificateNumberStainless.value = newSampleCertificateNumberStainless
    }

    //-----

    private val _detectRejectStainlessLeading = mutableStateOf(YesNoState.NO)
    val detectRejectStainlessLeading: State<YesNoState> = _detectRejectStainlessLeading


    fun setDetectRejectStainlessLeading(newValue: YesNoState) {
        _detectRejectStainlessLeading.value = newValue
    }

    //-----

    private val _detectRejectStainlessMiddle = mutableStateOf(YesNoState.NO)
    val detectRejectStainlessMiddle: State<YesNoState> = _detectRejectStainlessMiddle

    fun setDetectRejectStainlessMiddle(newValue: YesNoState) {
        _detectRejectStainlessMiddle.value = newValue
    }

    //-----

    private val _detectRejectStainlessTrailing = mutableStateOf(YesNoState.NO)
    val detectRejectStainlessTrailing: State<YesNoState> = _detectRejectStainlessTrailing

    fun setDetectRejectStainlessTrailing(newValue: YesNoState) {
        _detectRejectStainlessTrailing.value = newValue
    }

    //------

    private val _stainlessTestEngineerNotes = mutableStateOf("")
    val stainlessTestEngineerNotes: State<String> = _stainlessTestEngineerNotes


    fun setStainlessTestEngineerNotes(newValue: String) {
        _stainlessTestEngineerNotes.value = newValue
    }

    private val _stainlessTestPvResult = mutableStateOf("")
    val stainlessTestPvResult: State<String> = _stainlessTestPvResult

    fun setStainlessTestPvResult(newValue: String) {
        _stainlessTestPvResult.value = newValue
    }



    //----------------------------------------------------------------Large Metal Sensitivity Result

    private val _detectRejectLargeMetal = mutableStateOf(YesNoState.NO)
    val detectRejectLargeMetal: State<YesNoState> = _detectRejectLargeMetal

    fun setDetectRejectLargeMetal(newValue: YesNoState) {
        _detectRejectLargeMetal.value = newValue
    }

    //-----


    private val _sampleCertificateNumberLargeMetal = mutableStateOf("")
    val sampleCertificateNumberLargeMetal: State<String> = _sampleCertificateNumberLargeMetal


    fun setSampleCertificateNumberLargeMetal(newSampleCertificateNumberLargeMetal: String) {
        _sampleCertificateNumberLargeMetal.value = newSampleCertificateNumberLargeMetal
    }

    private val _largeMetalTestEngineerNotes = mutableStateOf("")
    val largeMetalTestEngineerNotes: State<String> = _largeMetalTestEngineerNotes


    fun setLargeMetalTestEngineerNotes(newValue: String) {
        _largeMetalTestEngineerNotes.value = newValue
    }

    private val _largeMetalTestPvResult = mutableStateOf("")
    val largeMetalTestPvResult: State<String> = _largeMetalTestPvResult

    fun setLargeMetalTestPvResult(newValue: String) {
        _largeMetalTestPvResult.value = newValue
    }


    //------------------------------------------------------------------Detection Settings 'As Left'

    private val _detectionSettingAsLeft1 = mutableStateOf("")
    val detectionSettingAsLeft1: State<String> = _detectionSettingAsLeft1


    fun setDetectionSettingAsLeft1(newDetectionSettingAsLeft1: String) {
        _detectionSettingAsLeft1.value = newDetectionSettingAsLeft1
    }

    private val _detectionSettingAsLeft2 = mutableStateOf("")
    val detectionSettingAsLeft2: State<String> = _detectionSettingAsLeft2


    fun setDetectionSettingAsLeft2(newDetectionSettingAsLeft2: String) {
        _detectionSettingAsLeft2.value = newDetectionSettingAsLeft2
    }

    private val _detectionSettingAsLeft3 = mutableStateOf("")
    val detectionSettingAsLeft3: State<String> = _detectionSettingAsLeft3


    fun setDetectionSettingAsLeft3(newDetectionSettingAsLeft3: String) {
        _detectionSettingAsLeft3.value = newDetectionSettingAsLeft3
    }

    private val _detectionSettingAsLeft4 = mutableStateOf("")
    val detectionSettingAsLeft4: State<String> = _detectionSettingAsLeft4


    fun setDetectionSettingAsLeft4(newDetectionSettingAsLeft4: String) {
        _detectionSettingAsLeft4.value = newDetectionSettingAsLeft4
    }

    private val _detectionSettingAsLeft5 = mutableStateOf("")
    val detectionSettingAsLeft5: State<String> = _detectionSettingAsLeft5


    fun setDetectionSettingAsLeft5(newDetectionSettingAsLeft5: String) {
        _detectionSettingAsLeft5.value = newDetectionSettingAsLeft5
    }

    private val _detectionSettingAsLeft6 = mutableStateOf("")
    val detectionSettingAsLeft6: State<String> = _detectionSettingAsLeft6


    fun setDetectionSettingAsLeft6(newDetectionSettingAsLeft6: String) {
        _detectionSettingAsLeft6.value = newDetectionSettingAsLeft6
    }

    private val _detectionSettingAsLeft7 = mutableStateOf("")
    val detectionSettingAsLeft7: State<String> = _detectionSettingAsLeft7


    fun setDetectionSettingAsLeft7(newDetectionSettingAsLeft7: String) {
        _detectionSettingAsLeft7.value = newDetectionSettingAsLeft7
    }

    private val _detectionSettingAsLeft8 = mutableStateOf("")
    val detectionSettingAsLeft8: State<String> = _detectionSettingAsLeft8


    fun setDetectionSettingAsLeft8(newDetectionSettingAsLeft8: String) {
        _detectionSettingAsLeft8.value = newDetectionSettingAsLeft8
    }

    private val _detectionSettingAsLeftEngineerNotes = mutableStateOf("")
    val detectionSettingAsLeftEngineerNotes: State<String> = _detectionSettingAsLeftEngineerNotes


    fun setDetectionSettingAsLeftEngineerNotes(newValue: String) {
        _detectionSettingAsLeftEngineerNotes.value = newValue
    }

    //---------------------------------------------------------------------------Rejection Settings

    private val _rejectSynchronisationSetting = mutableStateOf(YesNoState.NO)
    val rejectSynchronisationSetting: State<YesNoState> = _rejectSynchronisationSetting


    fun setRejectSynchronisationSetting(newRejectSynchronisationSetting: YesNoState) {
        _rejectSynchronisationSetting.value = newRejectSynchronisationSetting
    }

    private val _rejectSynchronisationDetail = mutableStateOf("")
    val rejectSynchronisationDetail: State<String> = _rejectSynchronisationDetail


    fun setRejectSynchronisationDetail(newRejectSynchronisationDetail: String) {
        _rejectSynchronisationDetail.value = newRejectSynchronisationDetail
    }


    private val _rejectDelaySetting = mutableStateOf("")
    val rejectDelaySetting: State<String> = _rejectDelaySetting


    fun setRejectDelaySetting(newRejectDelaySetting: String) {
        _rejectDelaySetting.value = newRejectDelaySetting
    }

    private val _rejectDelayUnits = mutableStateOf("")
    val rejectDelayUnits: State<String> = _rejectDelayUnits


    fun setRejectDelayUnits(newRejectDelayUnits: String) {
        _rejectDelayUnits.value = newRejectDelayUnits
    }


    private val _rejectDurationSetting = mutableStateOf("")
    val rejectDurationSetting: State<String> = _rejectDurationSetting


    fun setRejectDurationSetting(newRejectDurationSetting: String) {
        _rejectDurationSetting.value = newRejectDurationSetting
    }

    private val _rejectDurationUnits = mutableStateOf("")
    val rejectDurationUnits: State<String> = _rejectDurationUnits


    fun setRejectDurationUnits(newRejectDurationUnits: String) {
        _rejectDurationUnits.value = newRejectDurationUnits
    }

    private val _rejectConfirmWindowSetting = mutableStateOf("")
    val rejectConfirmWindowSetting: State<String> = _rejectConfirmWindowSetting


    fun setRejectConfirmWindowSetting(newRejectConfirmWindowSetting: String) {
        _rejectConfirmWindowSetting.value = newRejectConfirmWindowSetting
    }

    private val _rejectConfirmWindowUnits = mutableStateOf("")
    val rejectConfirmWindowUnits: State<String> = _rejectConfirmWindowUnits


    fun setRejectConfirmWindowUnits(newRejectConfirmWindowUnits: String) {
        _rejectConfirmWindowUnits.value = newRejectConfirmWindowUnits
    }

    private val _rejectSettingsEngineerNotes = mutableStateOf("")
    val rejectSettingsEngineerNotes: State<String> = _rejectSettingsEngineerNotes


    fun setRejectSettingsEngineerNotes(newValue: String) {
        _rejectSettingsEngineerNotes.value = newValue
    }


    //------------------------------------------------------------------------------Conveyor Details

    private val _infeedBeltHeight = mutableStateOf("")
    val infeedBeltHeight: State<String> = _infeedBeltHeight


    fun setInfeedBeltHeight(newInfeedBeltHeight: String) {
        _infeedBeltHeight.value = newInfeedBeltHeight
    }

    private val _outfeedBeltHeight = mutableStateOf("")
    val outfeedBeltHeight: State<String> = _outfeedBeltHeight


    fun setOutfeedBeltHeight(newOutfeedBeltHeight: String) {
        _outfeedBeltHeight.value = newOutfeedBeltHeight
    }

    private val _conveyorLength = mutableStateOf("")
    val conveyorLength: State<String> = _conveyorLength


    fun setConveyorLength(newConveyorLength: String) {
        _conveyorLength.value = newConveyorLength
    }

    private val _conveyorHanding = mutableStateOf("")
    val conveyorHanding: State<String> = _conveyorHanding


    fun setConveyorHanding(newConveyorHanding: String) {
        _conveyorHanding.value = newConveyorHanding
    }

    private val _beltSpeed = mutableStateOf("")
    val beltSpeed: State<String> = _beltSpeed


    fun setBeltSpeed(newBeltSpeed: String) {
        _beltSpeed.value = newBeltSpeed
    }

    private val _rejectDevice = mutableStateOf("")
    val rejectDevice: State<String> = _rejectDevice


    fun setRejectDevice(newRejectDevice: String) {
        _rejectDevice.value = newRejectDevice
    }

    private val _rejectDeviceOther = mutableStateOf("")
    val rejectDeviceOther: State<String> = _rejectDeviceOther


    fun setRejectDeviceOther(newRejectDeviceOther: String) {
        _rejectDeviceOther.value = newRejectDeviceOther
    }

    private val _conveyorDetailsEngineerNotes = mutableStateOf("")
    val conveyorDetailsEngineerNotes: State<String> = _conveyorDetailsEngineerNotes


    fun setConveyorDetailsEngineerNotes(newValue: String) {
        _conveyorDetailsEngineerNotes.value = newValue
    }

    //------------------------------------------------------------------------------System Checklist

    private val _beltCondition = mutableStateOf((ConditionState.UNSPECIFIED))
    val beltCondition: State<(ConditionState)> = _beltCondition


    fun setBeltCondition(newBeltCondition: ConditionState) {
        _beltCondition.value = newBeltCondition
    }

    private val _beltConditionComments = mutableStateOf("")
    val beltConditionComments: State<String> = _beltConditionComments


    fun setBeltConditionComments(newBeltConditionComments: String) {
        _beltConditionComments.value = newBeltConditionComments
    }

    private val _guardCondition = mutableStateOf((ConditionState.UNSPECIFIED))
    val guardCondition: State<ConditionState> = _guardCondition


    fun setGuardCondition(newGuardCondition: ConditionState) {
        _guardCondition.value = newGuardCondition
    }

    private val _guardConditionComments = mutableStateOf("")
    val guardConditionComments: State<String> = _guardConditionComments


    fun setGuardConditionComments(newGuardConditionComments: String) {
        _guardConditionComments.value = newGuardConditionComments
    }

    private val _safetyCircuitCondition = mutableStateOf((ConditionState.UNSPECIFIED))
    val safetyCircuitCondition: State<ConditionState> = _safetyCircuitCondition


    fun setSafetyCircuitCondition(newSafetyCircuitCondition: ConditionState) {
        _safetyCircuitCondition.value = newSafetyCircuitCondition
    }

    private val _safetyCircuitConditionComments = mutableStateOf("")
    val safetyCircuitConditionComments: State<String> = _safetyCircuitConditionComments


    fun setSafetyCircuitConditionComments(newSafetyCircuitConditionComments: String) {
        _safetyCircuitConditionComments.value = newSafetyCircuitConditionComments
    }

    private val _linerCondition = mutableStateOf((ConditionState.UNSPECIFIED))
    val linerCondition: State<ConditionState> = _linerCondition


    fun setLinerCondition(newLinerCondition: ConditionState) {
        _linerCondition.value = newLinerCondition
    }

    private val _linerConditionComments = mutableStateOf("")
    val linerConditionComments: State<String> = _linerConditionComments


    fun setLinerConditionComments(newLinerConditionComments: String) {
        _linerConditionComments.value = newLinerConditionComments
    }

    private val _cablesCondition = mutableStateOf((ConditionState.UNSPECIFIED))
    val cablesCondition: State<ConditionState> = _cablesCondition


    fun setCablesCondition(newCablesCondition: ConditionState) {
        _cablesCondition.value = newCablesCondition
    }

    private val _cablesConditionComments = mutableStateOf("")
    val cablesConditionComments: State<String> = _cablesConditionComments


    fun setCablesConditionComments(newCablesConditionComments: String) {
        _cablesConditionComments.value = newCablesConditionComments
    }

    private val _screwsCondition = mutableStateOf((ConditionState.UNSPECIFIED))
    val screwsCondition: State<ConditionState> = _screwsCondition


    fun setScrewsCondition(newScrewsCondition: ConditionState) {
        _screwsCondition.value = newScrewsCondition
    }

    private val _screwsConditionComments = mutableStateOf("")
    val screwsConditionComments: State<String> = _screwsConditionComments


    fun setScrewsConditionComments(newScrewsConditionComments: String) {
        _screwsConditionComments.value = newScrewsConditionComments
    }

    private val _systemChecklistEngineerNotes = mutableStateOf("")
    val systemChecklistEngineerNotes: State<String> = _systemChecklistEngineerNotes


    fun setSystemChecklistEngineerNotes(newValue: String) {
        _systemChecklistEngineerNotes.value = newValue
    }

    //-----------------------------------------------------------------------------------Indicators

    private val _indicator6colour = mutableStateOf("")
    val indicator6colour: State<(String)> = _indicator6colour


    fun setIndicator6colour(newValue: String) {
        _indicator6colour.value = newValue
    }

    private val _indicator6label = mutableStateOf("")
    val indicator6label: State<(String)> = _indicator6label


    fun setIndicator6label(newValue: String) {
        _indicator6label.value = newValue
    }


    private val _indicator5colour = mutableStateOf("")
    val indicator5colour: State<(String)> = _indicator5colour


    fun setIndicator5colour(newValue: String) {
        _indicator5colour.value = newValue
    }

    private val _indicator5label = mutableStateOf("")
    val indicator5label: State<(String)> = _indicator5label


    fun setIndicator5label(newValue: String) {
        _indicator5label.value = newValue
    }


    private val _indicator4colour = mutableStateOf("")
    val indicator4colour: State<(String)> = _indicator4colour


    fun setIndicator4colour(newValue: String) {
        _indicator4colour.value = newValue
    }

    private val _indicator4label = mutableStateOf("")
    val indicator4label: State<(String)> = _indicator4label


    fun setIndicator4label(newValue: String) {
        _indicator4label.value = newValue
    }


    private val _indicator3colour = mutableStateOf("")
    val indicator3colour: State<(String)> = _indicator3colour


    fun setIndicator3colour(newValue: String) {
        _indicator3colour.value = newValue
    }

    private val _indicator3label = mutableStateOf("")
    val indicator3label: State<(String)> = _indicator3label


    fun setIndicator3label(newValue: String) {
        _indicator3label.value = newValue
    }


    private val _indicator2colour = mutableStateOf("")
    val indicator2colour: State<(String)> = _indicator2colour


    fun setIndicator2colour(newValue: String) {
        _indicator2colour.value = newValue
    }

    private val _indicator2label = mutableStateOf("")
    val indicator2label: State<(String)> = _indicator2label


    fun setIndicator2label(newValue: String) {
        _indicator2label.value = newValue
    }


    private val _indicator1colour = mutableStateOf("")
    val indicator1colour: State<(String)> = _indicator1colour


    fun setIndicator1colour(newValue: String) {
        _indicator1colour.value = newValue
    }

    private val _indicator1label = mutableStateOf("")
    val indicator1label: State<(String)> = _indicator1label


    fun setIndicator1label(newValue: String) {
        _indicator1label.value = newValue
    }

    private val _indicatorsEngineerNotes = mutableStateOf("")
    val indicatorsEngineerNotes: State<String> = _indicatorsEngineerNotes


    fun setIndicatorsEngineerNotes(newValue: String) {
        _indicatorsEngineerNotes.value = newValue
    }

    //---------------------------------------------------------------------------In feed sensor Test
    private val _infeedSensorFitted = mutableStateOf(YesNoState.NO)
    val infeedSensorFitted: State<YesNoState> = _infeedSensorFitted

    fun setInfeedSensorFitted(newValue: YesNoState) {
        _infeedSensorFitted.value = newValue
    }

    private val _infeedSensorDetail = mutableStateOf("")
    val infeedSensorDetail: State<(String)> = _infeedSensorDetail


    fun setInfeedSensorDetail(newValue: String) {
        _infeedSensorDetail.value = newValue
    }

    private val _infeedSensorTestMethod = mutableStateOf("")
    val infeedSensorTestMethod: State<(String)> = _infeedSensorTestMethod


    fun setInfeedSensorTestMethod(newValue: String) {
        _infeedSensorTestMethod.value = newValue
    }

    private val _infeedSensorTestMethodOther = mutableStateOf("")
    val infeedSensorTestMethodOther: State<(String)> = _infeedSensorTestMethodOther


    fun setInfeedSensorTestMethodOther(newValue: String) {
        _infeedSensorTestMethodOther.value = newValue
    }

    private val _infeedSensorTestResult = MutableStateFlow(listOf<String>())
    val infeedSensorTestResult: StateFlow<List<String>> = _infeedSensorTestResult


    fun setInfeedSensorTestResult(newValue: List<String>) {
        _infeedSensorTestResult.value = newValue
    }


    private val _infeedSensorEngineerNotes = mutableStateOf("")
    val infeedSensorEngineerNotes: State<(String)> = _infeedSensorEngineerNotes

    fun setInfeedSensorEngineerNotes(newValue: String) {
        _infeedSensorEngineerNotes.value = newValue
    }

    private val _infeedSensorLatched = mutableStateOf(YesNoState.NO)
    val infeedSensorLatched: State<YesNoState> = _infeedSensorLatched

    fun setInfeedSensorLatched(newValue: YesNoState) {
        _infeedSensorLatched.value = newValue
    }

    private val _infeedSensorCR = mutableStateOf(YesNoState.NO)
    val infeedSensorCR: State<YesNoState> = _infeedSensorCR

    fun setInfeedSensorCR(newValue: YesNoState) {
        _infeedSensorCR.value = newValue
    }

    private val _infeedSensorTestPvResult = mutableStateOf("")
    val infeedSensorTestPvResult: State<String> = _infeedSensorTestPvResult

    fun setInfeedSensorTestPvResult(newValue: String) {
        _infeedSensorTestPvResult.value = newValue
    }


    //--------------------------------------------------------------------Reject Confirm sensor Test
    private val _rejectConfirmSensorFitted = mutableStateOf(YesNoState.NO)
    val rejectConfirmSensorFitted: State<YesNoState> = _rejectConfirmSensorFitted

    fun setRejectConfirmSensorFitted(newValue: YesNoState) {
        _rejectConfirmSensorFitted.value = newValue
    }

    private val _rejectConfirmSensorDetail = mutableStateOf("")
    val rejectConfirmSensorDetail: State<(String)> = _rejectConfirmSensorDetail


    fun setRejectConfirmSensorDetail(newValue: String) {
        _rejectConfirmSensorDetail.value = newValue
    }

    private val _rejectConfirmSensorTestMethod = mutableStateOf("")
    val rejectConfirmSensorTestMethod: State<(String)> = _rejectConfirmSensorTestMethod


    fun setRejectConfirmSensorTestMethod(newValue: String) {
        _rejectConfirmSensorTestMethod.value = newValue
    }

    private val _rejectConfirmSensorTestMethodOther = mutableStateOf("")
    val rejectConfirmSensorTestMethodOther: State<(String)> = _rejectConfirmSensorTestMethodOther


    fun setRejectConfirmSensorTestMethodOther(newValue: String) {
        _rejectConfirmSensorTestMethodOther.value = newValue
    }

    private val _rejectConfirmSensorTestResult = MutableStateFlow(listOf<String>())
    val rejectConfirmSensorTestResult: StateFlow<List<String>> = _rejectConfirmSensorTestResult

    fun setRejectConfirmSensorTestResult(newValue: List<String>) {
        _rejectConfirmSensorTestResult.value = newValue
    }

    private val _rejectConfirmSensorEngineerNotes = mutableStateOf("")
    val rejectConfirmSensorEngineerNotes: State<(String)> = _rejectConfirmSensorEngineerNotes


    fun setRejectConfirmSensorEngineerNotes(newValue: String) {
        _rejectConfirmSensorEngineerNotes.value = newValue
    }

    private val _rejectConfirmSensorLatched = mutableStateOf(YesNoState.NO)
    val rejectConfirmSensorLatched: State<YesNoState> = _rejectConfirmSensorLatched

    fun setRejectConfirmSensorLatched(newValue: YesNoState) {
        _rejectConfirmSensorLatched.value = newValue
    }

    private val _rejectConfirmSensorCR = mutableStateOf(YesNoState.NO)
    val rejectConfirmSensorCR: State<YesNoState> = _rejectConfirmSensorCR

    fun setRejectConfirmSensorCR(newValue: YesNoState) {
        _rejectConfirmSensorCR.value = newValue
    }

    private val _rejectConfirmSensorStopPosition = mutableStateOf("")
    val rejectConfirmSensorStopPosition: State<String> = _rejectConfirmSensorStopPosition

    fun setRejectConfirmSensorStopPosition(newValue: String) {
        _rejectConfirmSensorStopPosition.value = newValue
    }

    private val _rejectConfirmSensorTestPvResult = mutableStateOf("")
    val rejectConfirmSensorTestPvResult: State<String> = _rejectConfirmSensorTestPvResult

    fun setRejectConfirmSensorTestPvResult(newValue: String) {
        _rejectConfirmSensorTestPvResult.value = newValue
    }

    //-------------------------------------------------------------------------Bin Full sensor Test
    private val _binFullSensorFitted = mutableStateOf(YesNoState.NO)
    val binFullSensorFitted: State<YesNoState> = _binFullSensorFitted

    fun setBinFullSensorFitted(newValue: YesNoState) {
        _binFullSensorFitted.value = newValue
    }

    private val _binFullSensorDetail = mutableStateOf("")
    val binFullSensorDetail: State<(String)> = _binFullSensorDetail


    fun setBinFullSensorDetail(newValue: String) {
        _binFullSensorDetail.value = newValue
    }

    private val _binFullSensorTestMethod = mutableStateOf("")
    val binFullSensorTestMethod: State<(String)> = _binFullSensorTestMethod


    fun setBinFullSensorTestMethod(newValue: String) {
        _binFullSensorTestMethod.value = newValue
    }

    private val _binFullSensorTestMethodOther = mutableStateOf("")
    val binFullSensorTestMethodOther: State<(String)> = _binFullSensorTestMethodOther


    fun setBinFullSensorTestMethodOther(newValue: String) {
        _binFullSensorTestMethodOther.value = newValue
    }

    private val _binFullSensorTestResult = MutableStateFlow(listOf<String>())
    val binFullSensorTestResult: StateFlow<List<String>> = _binFullSensorTestResult


    fun setBinFullSensorTestResult(newValue: List<String>) {
        _binFullSensorTestResult.value = newValue
    }

    private val _binFullSensorEngineerNotes = mutableStateOf("")
    val binFullSensorEngineerNotes: State<(String)> = _binFullSensorEngineerNotes


    fun setBinFullSensorEngineerNotes(newValue: String) {
        _binFullSensorEngineerNotes.value = newValue
    }

    private val _binFullSensorLatched = mutableStateOf(YesNoState.NO)
    val binFullSensorLatched: State<YesNoState> = _binFullSensorLatched

    fun setBinFullSensorLatched(newValue: YesNoState) {
        _binFullSensorLatched.value = newValue
    }

    private val _binFullSensorCR = mutableStateOf(YesNoState.NO)
    val binFullSensorCR: State<YesNoState> = _binFullSensorCR

    fun setBinFullSensorCR(newValue: YesNoState) {
        _binFullSensorCR.value = newValue
    }

    private val _binFullSensorTestPvResult = mutableStateOf("")
    val binFullSensorTestPvResult: State<String> = _binFullSensorTestPvResult

    fun setBinFullSensorTestPvResult(newValue: String) {
        _binFullSensorTestPvResult.value = newValue
    }

    //----------------------------------------------------------------------------Backup sensor Test
    private val _backupSensorFitted = mutableStateOf(YesNoState.NO)
    val backupSensorFitted: State<YesNoState> = _backupSensorFitted

    fun setBackupSensorFitted(newValue: YesNoState) {
        _backupSensorFitted.value = newValue
    }

    private val _backupSensorDetail = mutableStateOf("")
    val backupSensorDetail: State<(String)> = _backupSensorDetail


    fun setBackupSensorDetail(newValue: String) {
        _backupSensorDetail.value = newValue
    }

    private val _backupSensorTestMethod = mutableStateOf("")
    val backupSensorTestMethod: State<(String)> = _backupSensorTestMethod


    fun setBackupSensorTestMethod(newValue: String) {
        _backupSensorTestMethod.value = newValue
    }

    private val _backupSensorTestMethodOther = mutableStateOf("")
    val backupSensorTestMethodOther: State<(String)> = _backupSensorTestMethodOther


    fun setBackupSensorTestMethodOther(newValue: String) {
        _backupSensorTestMethodOther.value = newValue
    }

    private val _backupSensorTestResult = MutableStateFlow(listOf<String>())
    val backupSensorTestResult: StateFlow<List<String>> = _backupSensorTestResult


    fun setBackupSensorTestResult(newValue: List<String>) {
        _backupSensorTestResult.value = newValue
    }

    private val _backupSensorEngineerNotes = mutableStateOf("")
    val backupSensorEngineerNotes: State<(String)> = _backupSensorEngineerNotes


    fun setBackupSensorEngineerNotes(newValue: String) {
        _backupSensorEngineerNotes.value = newValue
    }

    private val _backupSensorLatched = mutableStateOf(YesNoState.NO)
    val backupSensorLatched: State<YesNoState> = _backupSensorLatched

    fun setBackupSensorLatched(newValue: YesNoState) {
        _backupSensorLatched.value = newValue
    }

    private val _backupSensorCR = mutableStateOf(YesNoState.NO)
    val backupSensorCR: State<YesNoState> = _backupSensorCR

    fun setBackupSensorCR(newValue: YesNoState) {
        _backupSensorCR.value = newValue
    }

    private val _backupSensorTestPvResult = mutableStateOf("")
    val backupSensorTestPvResult: State<String> = _backupSensorTestPvResult

    fun setBackupSensorTestPvResult(newValue: String) {
        _backupSensorTestPvResult.value = newValue
    }

    //----------------------------------------------------------------------Air Pressure sensor Test
    private val _airPressureSensorFitted = mutableStateOf(YesNoState.NO)
    val airPressureSensorFitted: State<YesNoState> = _airPressureSensorFitted

    fun setAirPressureSensorFitted(newValue: YesNoState) {
        _airPressureSensorFitted.value = newValue
    }

    private val _airPressureSensorDetail = mutableStateOf("")
    val airPressureSensorDetail: State<(String)> = _airPressureSensorDetail


    fun setAirPressureSensorDetail(newValue: String) {
        _airPressureSensorDetail.value = newValue
    }

    private val _airPressureSensorTestMethod = mutableStateOf("")
    val airPressureSensorTestMethod: State<(String)> = _airPressureSensorTestMethod


    fun setAirPressureSensorTestMethod(newValue: String) {
        _airPressureSensorTestMethod.value = newValue
    }

    private val _airPressureSensorTestMethodOther = mutableStateOf("")
    val airPressureSensorTestMethodOther: State<(String)> = _airPressureSensorTestMethodOther


    fun setAirPressureSensorTestMethodOther(newValue: String) {
        _airPressureSensorTestMethodOther.value = newValue
    }

    private val _airPressureSensorTestResult = MutableStateFlow(listOf<String>())
    val airPressureSensorTestResult: StateFlow<List<String>> = _airPressureSensorTestResult


    fun setAirPressureSensorTestResult(newValue: List<String>) {
        _airPressureSensorTestResult.value = newValue
    }

    private val _airPressureSensorEngineerNotes = mutableStateOf("")
    val airPressureSensorEngineerNotes: State<(String)> = _airPressureSensorEngineerNotes


    fun setAirPressureSensorEngineerNotes(newValue: String) {
        _airPressureSensorEngineerNotes.value = newValue
    }

    private val _airPressureSensorLatched = mutableStateOf(YesNoState.NO)
    val airPressureSensorLatched: State<YesNoState> = _airPressureSensorLatched

    fun setAirPressureSensorLatched(newValue: YesNoState) {
        _airPressureSensorLatched.value = newValue
    }

    private val _airPressureSensorCR = mutableStateOf(YesNoState.NO)
    val airPressureSensorCR: State<YesNoState> = _airPressureSensorCR

    fun setAirPressureSensorCR(newValue: YesNoState) {
        _airPressureSensorCR.value = newValue
    }

    private val _airPressureSensorTestPvResult = mutableStateOf("")
    val airPressureSensorTestPvResult: State<String> = _airPressureSensorTestPvResult

    fun setAirPressureSensorTestPvResult(newValue: String) {
        _airPressureSensorTestPvResult.value = newValue
    }

    //------------------------------------------------------------------------Pack Check sensor Test
    private val _packCheckSensorFitted = mutableStateOf(YesNoState.NO)
    val packCheckSensorFitted: State<YesNoState> = _packCheckSensorFitted

    fun setPackCheckSensorFitted(newValue: YesNoState) {
        _packCheckSensorFitted.value = newValue
    }

    private val _packCheckSensorDetail = mutableStateOf("")
    val packCheckSensorDetail: State<(String)> = _packCheckSensorDetail


    fun setPackCheckSensorDetail(newValue: String) {
        _packCheckSensorDetail.value = newValue
    }

    private val _packCheckSensorTestMethod = mutableStateOf("")
    val packCheckSensorTestMethod: State<(String)> = _packCheckSensorTestMethod


    fun setPackCheckSensorTestMethod(newValue: String) {
        _packCheckSensorTestMethod.value = newValue
    }

    private val _packCheckSensorTestMethodOther = mutableStateOf("")
    val packCheckSensorTestMethodOther: State<(String)> = _packCheckSensorTestMethodOther


    fun setPackCheckSensorTestMethodOther(newValue: String) {
        _packCheckSensorTestMethodOther.value = newValue
    }

    private val _packCheckSensorTestResult = MutableStateFlow(listOf<String>())
    val packCheckSensorTestResult: StateFlow<List<String>> = _packCheckSensorTestResult


    fun setPackCheckSensorTestResult(newValue: List<String>) {
        _packCheckSensorTestResult.value = newValue
    }

    private val _packCheckSensorEngineerNotes = mutableStateOf("")
    val packCheckSensorEngineerNotes: State<(String)> = _packCheckSensorEngineerNotes


    fun setPackCheckSensorEngineerNotes(newValue: String) {
        _packCheckSensorEngineerNotes.value = newValue
    }

    private val _packCheckSensorLatched = mutableStateOf(YesNoState.NO)
    val packCheckSensorLatched: State<YesNoState> = _packCheckSensorLatched

    fun setPackCheckSensorLatched(newValue: YesNoState) {
        _packCheckSensorLatched.value = newValue
    }

    private val _packCheckSensorCR = mutableStateOf(YesNoState.NO)
    val packCheckSensorCR: State<YesNoState> = _packCheckSensorCR

    fun setPackCheckSensorCR(newValue: YesNoState) {
        _packCheckSensorCR.value = newValue
    }

    private val _packCheckSensorTestPvResult = mutableStateOf("")
    val packCheckSensorTestPvResult: State<String> = _packCheckSensorTestPvResult

    fun setPackCheckSensorTestPvResult(newValue: String) {
        _packCheckSensorTestPvResult.value = newValue
    }

    //-----------------------------------------------------------------------------Speed sensor Test

    private val _speedSensorFitted = mutableStateOf(YesNoState.NO)
    val speedSensorFitted: State<YesNoState> = _speedSensorFitted

    fun setSpeedSensorFitted(newValue: YesNoState) {
        _speedSensorFitted.value = newValue
    }

    private val _speedSensorDetail = mutableStateOf("")
    val speedSensorDetail: State<(String)> = _speedSensorDetail


    fun setSpeedSensorDetail(newValue: String) {
        _speedSensorDetail.value = newValue
    }

    private val _speedSensorTestMethod = mutableStateOf("")
    val speedSensorTestMethod: State<(String)> = _speedSensorTestMethod


    fun setSpeedSensorTestMethod(newValue: String) {
        _speedSensorTestMethod.value = newValue
    }

    private val _speedSensorTestMethodOther = mutableStateOf("")
    val speedSensorTestMethodOther: State<(String)> = _speedSensorTestMethodOther


    fun setSpeedSensorTestMethodOther(newValue: String) {
        _speedSensorTestMethodOther.value = newValue
    }

    private val _speedSensorTestResult = MutableStateFlow(listOf<String>())
    val speedSensorTestResult: StateFlow<List<String>> = _speedSensorTestResult


    fun setSpeedSensorTestResult(newValue: List<String>) {
        _speedSensorTestResult.value = newValue
    }

    private val _speedSensorEngineerNotes = mutableStateOf("")
    val speedSensorEngineerNotes: State<(String)> = _speedSensorEngineerNotes


    fun setSpeedSensorEngineerNotes(newValue: String) {
        _speedSensorEngineerNotes.value = newValue
    }

    private val _speedSensorLatched = mutableStateOf(YesNoState.NO)
    val speedSensorLatched: State<YesNoState> = _speedSensorLatched

    fun setSpeedSensorLatched(newValue: YesNoState) {
        _speedSensorLatched.value = newValue
    }

    private val _speedSensorCR = mutableStateOf(YesNoState.NO)
    val speedSensorCR: State<YesNoState> = _speedSensorCR

    fun setSpeedSensorCR(newValue: YesNoState) {
        _speedSensorCR.value = newValue
    }

    private val _speedSensorTestPvResult = mutableStateOf("")
    val speedSensorTestPvResult: State<String> = _speedSensorTestPvResult

    fun setSpeedSensorTestPvResult(newValue: String) {
        _speedSensorTestPvResult.value = newValue
    }

    //-----------------------------------------------------------------------------Detect Notify Test

    private val _detectNotificationResult = MutableStateFlow(listOf<String>())
    val detectNotificationResult: StateFlow<List<(String)>> = _detectNotificationResult

    fun setDetectNotificationResult(newValue: List<String>) {
        _detectNotificationResult.value = newValue
    }

    private val _detectNotificationEngineerNotes = mutableStateOf("")
    val detectNotificationEngineerNotes: State<(String)> = _detectNotificationEngineerNotes


    fun setDetectNotificationEngineerNotes(newValue: String) {
        _detectNotificationEngineerNotes.value = newValue
    }

    //---------------------------------------------------------------------------------Bin Door Test

    private val _binDoorMonitorFitted = mutableStateOf(YesNoState.NO)
    val binDoorMonitorFitted: State<YesNoState> = _binDoorMonitorFitted

    fun setBinDoorMonitorFitted(newValue: YesNoState) {
        _binDoorMonitorFitted.value = newValue
    }

    private val _binDoorMonitorDetail = mutableStateOf("")
    val binDoorMonitorDetail: State<(String)> = _binDoorMonitorDetail


    fun setBinDoorMonitorDetail(newValue: String) {
        _binDoorMonitorDetail.value = newValue
    }

    private val _binDoorStatusAsFound = mutableStateOf("")
    val binDoorStatusAsFound: State<(String)> = _binDoorStatusAsFound


    fun setBinDoorStatusAsFound(newValue: String) {
        _binDoorStatusAsFound.value = newValue
    }

    private val _binDoorUnlockedIndication = MutableStateFlow(listOf<String>())
    val binDoorUnlockedIndication: StateFlow<List<String>> = _binDoorUnlockedIndication


    fun setBinDoorUnlockedIndication(newValue: List<String>) {
        _binDoorUnlockedIndication.value = newValue
    }

    private val _binDoorOpenIndication = MutableStateFlow(listOf<String>())
    val binDoorOpenIndication: StateFlow<List<String>> = _binDoorOpenIndication


    fun setBinDoorOpenIndication(newValue: List<String>) {
        _binDoorOpenIndication.value = newValue
    }

    ///////////////////////////////////////////////////////////////////////////
    private val _binDoorTimeoutTimer = mutableStateOf("")
    val binDoorTimeoutTimer: State<(String)> = _binDoorTimeoutTimer


    fun setBinDoorTimeoutTimer(newValue: String) {
        _binDoorTimeoutTimer.value = newValue
    }

    private val _binDoorTimeoutResult = MutableStateFlow(listOf<String>())
    val binDoorTimeoutResult: StateFlow<List<String>> = _binDoorTimeoutResult


    fun setBinDoorTimeoutResult(newValue: List<String>) {
        _binDoorTimeoutResult.value = newValue
    }

    private val _binDoorLatched = mutableStateOf(YesNoState.NO)
    val binDoorLatched: State<(YesNoState)> = _binDoorLatched


    fun setBinDoorLatched(newValue: YesNoState) {
        _binDoorLatched.value = newValue
    }

    private val _binDoorCR = mutableStateOf(YesNoState.NO)
    val binDoorCR: State<(YesNoState)> = _binDoorCR


    fun setBinDoorCR(newValue: YesNoState) {
        _binDoorCR.value = newValue
    }

    private val _binDoorEngineerNotes = mutableStateOf("")
    val binDoorEngineerNotes: State<(String)> = _binDoorEngineerNotes


    fun setBinDoorEngineerNotes(newValue: String) {
        _binDoorEngineerNotes.value = newValue
    }

    private val _binDoorMonitorTestPvResult = mutableStateOf("")
    val binDoorMonitorTestPvResult: State<String> = _binDoorMonitorTestPvResult

    fun setBinDoorMonitorTestPvResult(newValue: String) {
        _binDoorMonitorTestPvResult.value = newValue
    }

    //---------------------------------------------------------------------------------SME


    private val _operatorName = mutableStateOf("")
    val operatorName: State<(String)> = _operatorName


    fun setOperatorName(newValue: String) {
        _operatorName.value = newValue
    }


    private val _operatorTestWitnessed = mutableStateOf(YesNoState.NO)
    val operatorTestWitnessed: State<YesNoState> = _operatorTestWitnessed

    fun setOperatorTestWitnessed(newValue: YesNoState) {
        _operatorTestWitnessed.value = newValue
    }

    private val _operatorTestResultFerrous = mutableStateOf("")
    val operatorTestResultFerrous: State<(String)> = _operatorTestResultFerrous


    fun setOperatorTestResultFerrous(newValue: String) {
        _operatorTestResultFerrous.value = newValue
    }

    private val _operatorTestResultNonFerrous = mutableStateOf("")
    val operatorTestResultNonFerrous: State<(String)> = _operatorTestResultNonFerrous


    fun setOperatorTestResultNonFerrous(newValue: String) {
        _operatorTestResultNonFerrous.value = newValue
    }

    private val _operatorTestResultStainless = mutableStateOf("")
    val operatorTestResultStainless: State<(String)> = _operatorTestResultStainless


    fun setOperatorTestResultStainless(newValue: String) {
        _operatorTestResultStainless.value = newValue
    }

    private val _operatorTestResultLargeMetal = mutableStateOf("")
    val operatorTestResultLargeMetal: State<(String)> = _operatorTestResultLargeMetal


    fun setOperatorTestResultLargeMetal(newValue: String) {
        _operatorTestResultLargeMetal.value = newValue
    }

    private val _operatorTestResultCertNumberFerrous = mutableStateOf("")
    val operatorTestResultCertNumberFerrous: State<(String)> = _operatorTestResultCertNumberFerrous


    fun setOperatorTestResultCertNumberFerrous(newValue: String) {
        _operatorTestResultCertNumberFerrous.value = newValue
    }

    private val _operatorTestResultCertNumberNonFerrous = mutableStateOf("")
    val operatorTestResultCertNumberNonFerrous: State<(String)> =
        _operatorTestResultCertNumberNonFerrous


    fun setOperatorTestResultCertNumberNonFerrous(newValue: String) {
        _operatorTestResultCertNumberNonFerrous.value = newValue
    }

    private val _operatorTestResultCertNumberStainless = mutableStateOf("")
    val operatorTestResultCertNumberStainless: State<(String)> =
        _operatorTestResultCertNumberStainless


    fun setOperatorTestResultCertNumberStainless(newValue: String) {
        _operatorTestResultCertNumberStainless.value = newValue
    }

    private val _operatorTestResultCertNumberLargeMetal = mutableStateOf("")
    val operatorTestResultCertNumberLargeMetal: State<(String)> =
        _operatorTestResultCertNumberLargeMetal


    fun setOperatorTestResultCertNumberLargeMetal(newValue: String) {
        _operatorTestResultCertNumberLargeMetal.value = newValue
    }

    private val _smeName = mutableStateOf("")
    val smeName: State<(String)> = _smeName


    fun setSmeName(newValue: String) {
        _smeName.value = newValue
    }

    private val _smeEngineerNotes = mutableStateOf("")
    val smeEngineerNotes: State<(String)> = _smeEngineerNotes


    fun setSmeEngineerNotes(newValue: String) {
        _smeEngineerNotes.value = newValue
    }

    private val _smeTestPvResult = mutableStateOf("")
    val smeTestPvResult: State<String> = _smeTestPvResult

    fun setSmeTestPvResult(newValue: String) {
        _smeTestPvResult.value = newValue
    }

    //-----------------------------------------------------------------------Compliance Confirmation


//    private val _sensitivityCompliance = mutableStateOf(YesNoState.NO)
//    val sensitivityCompliance: State<YesNoState> = _sensitivityCompliance
//
//    fun setSensitivityCompliance(newValue: YesNoState) {
//        _sensitivityCompliance.value = newValue
//    }
//
//    private val _essentialRequirementCompliance = mutableStateOf(YesNoState.NO)
//    val essentialRequirementCompliance: State<YesNoState> = _essentialRequirementCompliance
//
//    fun setEssentialRequirementCompliance(newValue: YesNoState) {
//        _essentialRequirementCompliance.value = newValue
//    }
//
//    private val _failsafeCompliance = mutableStateOf(YesNoState.NO)
//    val failsafeCompliance: State<YesNoState> = _failsafeCompliance
//
//    fun setFailsafeCompliance(newValue: YesNoState) {
//        _failsafeCompliance.value = newValue
//    }
//
//    private val _bestSensitivityCompliance = mutableStateOf(YesNoState.NO)
//    val bestSensitivityCompliance: State<YesNoState> = _bestSensitivityCompliance
//
//    fun setBestSensitivityCompliance(newValue: YesNoState) {
//        _bestSensitivityCompliance.value = newValue
//    }
//
//    private val _sensitivityRecommendations = mutableStateOf("")
//    val sensitivityRecommendations: State<(String)> = _sensitivityRecommendations
//
//
//    fun setSensitivityRecommendations(newValue: String) {
//        _sensitivityRecommendations.value = newValue
//    }
//
//    private val _performanceValidationIssued = mutableStateOf(YesNoState.NO)
//    val performanceValidationIssued: State<YesNoState> = _performanceValidationIssued
//
//    fun setPerformanceValidationIssued(newValue: YesNoState) {
//        _performanceValidationIssued.value = newValue
//    }

    //endregion











    //---------------------------------------------------------------------------CSV File Processing
    suspend fun createAndUploadCsv(
        context: Context,
        calibrationId: String,
        apiService: ApiService,
    ): Boolean {


        // Step 1: Create CSV
        InAppLogger.d("Creating CSV file for calibration: $calibrationId")
        val csvFile = createCsvFile(context, calibrationId) ?: return false

        // Step 2: Attempt upload and store result
        InAppLogger.d("Uploading CSV file for calibration: $calibrationId")
        val uploadSuccessful = CsvUploader.uploadCsvFile(
            csvFile = csvFile,
            apiService = apiService,
            fileName = calibrationId
        )

        // Step 3: Update upload status in database
        calibrationDao.updateIsSynced(calibrationId, uploadSuccessful)
        return uploadSuccessful
    }

    private suspend fun createCsvFile(context: Context, calibrationId: String): File? {
        // Define the file name and path
        val fileName = "calibration_data_$calibrationId.csv"
        val csvFile = File(context.filesDir, fileName)

        _isUploading.value = true

        return withContext(Dispatchers.IO) { // Ensures the database operation runs on the IO dispatcher
            val row = calibrationDao.getCalibrationForCsvConversion(calibrationId)

            try {

                // Build data list from row properties, handling nulls with a default value if necessary
                val data = listOf(
                    row.calibrationId,
                    row.mapVersion,
                    row.systemId,
                    row.tempSystemId,
                    row.cloudSystemId,
                    row.systemTypeId,
                    row.modelId,
                    row.engineerId,
                    row.customerId,
                    row.startDate,
                    row.endDate,
                    row.isSynced,
                    row.newLocation,
                    row.canPerformCalibration,
                    row.reasonForNotCalibrating,
                    row.pvRequired,
                    row.desiredCop,
                    row.startCalibrationNotes,
                    row.productDescription,
                    row.productLibraryReference,
                    row.productLibraryNumber,
                    row.productLength,
                    row.productWidth,
                    row.productHeight,
                    row.productDetailsEngineerNotes,
                    row.detectionSettingAsFound1,
                    row.detectionSettingAsFound2,
                    row.detectionSettingAsFound3,
                    row.detectionSettingAsFound4,
                    row.detectionSettingAsFound5,
                    row.detectionSettingAsFound6,
                    row.detectionSettingAsFound7,
                    row.detectionSettingAsFound8,
                    row.detectionSettingAsFoundEngineerNotes,
                    row.sensitivityRequirementFerrous,
                    row.sensitivityRequirementNonFerrous,
                    row.sensitivityRequirementStainless,
                    row.sensitivityRequirementEngineerNotes,
                    row.sensitivityAccessRestriction,
                    row.sensitivityAsFoundFerrous,
                    row.sensitivityAsFoundFerrousPeakSignal,
                    row.sensitivityAsFoundNonFerrous,
                    row.sensitivityAsFoundNonFerrousPeakSignal,
                    row.sensitivityAsFoundStainless,
                    row.sensitivityAsFoundStainlessPeakSignal,
                    row.productPeakSignalAsFound,
                    row.sensitivityAsFoundEngineerNotes,
                    row.sensitivityAsLeftFerrous,
                    row.sampleCertificateNumberFerrous,
                    row.detectRejectFerrousLeading,
                    row.detectRejectFerrousLeadingPeakSignal,
                    row.detectRejectFerrousMiddle,
                    row.detectRejectFerrousMiddlePeakSignal,
                    row.detectRejectFerrousTrailing,
                    row.detectRejectFerrousTrailingPeakSignal,
                    row.ferrousTestEngineerNotes,
                    row.sensitivityAsLeftNonFerrous,
                    row.sampleCertificateNumberNonFerrous,
                    row.detectRejectNonFerrousLeading,
                    row.detectRejectNonFerrousLeadingPeakSignal,
                    row.detectRejectNonFerrousMiddle,
                    row.detectRejectNonFerrousMiddlePeakSignal,
                    row.detectRejectNonFerrousTrailing,
                    row.detectRejectNonFerrousTrailingPeakSignal,
                    row.nonFerrousTestEngineerNotes,
                    row.sensitivityAsLeftStainless,
                    row.sampleCertificateNumberStainless,
                    row.detectRejectStainlessLeading,
                    row.detectRejectStainlessLeadingPeakSignal,
                    row.detectRejectStainlessMiddle,
                    row.detectRejectStainlessMiddlePeakSignal,
                    row.detectRejectStainlessTrailing,
                    row.detectRejectStainlessTrailingPeakSignal,
                    row.stainlessTestEngineerNotes,
                    row.detectRejectLargeMetal,
                    row.sampleCertificateNumberLargeMetal,
                    row.largeMetalTestEngineerNotes,
                    row.detectionSettingAsLeft1,
                    row.detectionSettingAsLeft2,
                    row.detectionSettingAsLeft3,
                    row.detectionSettingAsLeft4,
                    row.detectionSettingAsLeft5,
                    row.detectionSettingAsLeft6,
                    row.detectionSettingAsLeft7,
                    row.detectionSettingAsLeft8,
                    row.detectionSettingAsLeftEngineerNotes,
                    row.rejectSynchronisationSetting,
                    row.rejectSynchronisationDetail,
                    row.rejectDelaySetting,
                    row.rejectDelayUnits,
                    row.rejectDurationSetting,
                    row.rejectDurationUnits,
                    row.rejectConfirmWindowSetting,
                    row.rejectConfirmWindowUnits,
                    row.rejectSettingsEngineerNotes,
                    row.infeedBeltHeight,
                    row.outfeedBeltHeight,
                    row.conveyorLength,
                    row.conveyorHanding,
                    row.beltSpeed,
                    row.rejectDevice,
                    row.rejectDeviceOther,
                    row.conveyorDetailsEngineerNotes,
                    row.beltCondition,
                    row.beltConditionComments,
                    row.guardCondition,
                    row.guardConditionComments,
                    row.safetyCircuitCondition,
                    row.safetyCircuitConditionComments,
                    row.linerCondition,
                    row.linerConditionComments,
                    row.cablesCondition,
                    row.cablesConditionComments,
                    row.screwsCondition,
                    row.screwsConditionComments,
                    row.systemChecklistEngineerNotes,
                    row.indicator6colour,
                    row.indicator6label,
                    row.indicator5colour,
                    row.indicator5label,
                    row.indicator4colour,
                    row.indicator4label,
                    row.indicator3colour,
                    row.indicator3label,
                    row.indicator2colour,
                    row.indicator2label,
                    row.indicator1colour,
                    row.indicator1label,
                    row.indicatorsEngineerNotes,
                    row.infeedSensorFitted,
                    row.infeedSensorDetail,
                    row.infeedSensorTestMethod,
                    row.infeedSensorTestMethodOther,
                    row.infeedSensorTestResult,
                    row.infeedSensorEngineerNotes,
                    row.infeedSensorLatched,
                    row.infeedSensorCR,
                    row.rejectConfirmSensorFitted,
                    row.rejectConfirmSensorDetail,
                    row.rejectConfirmSensorTestMethod,
                    row.rejectConfirmSensorTestMethodOther,
                    row.rejectConfirmSensorTestResult,
                    row.rejectConfirmSensorEngineerNotes,
                    row.rejectConfirmSensorLatched,
                    row.rejectConfirmSensorCR,
                    row.rejectConfirmSensorStopPosition,
                    row.binFullSensorFitted,
                    row.binFullSensorDetail,
                    row.binFullSensorTestMethod,
                    row.binFullSensorTestMethodOther,
                    row.binFullSensorTestResult,
                    row.binFullSensorEngineerNotes,
                    row.binFullSensorLatched,
                    row.binFullSensorCR,
                    row.backupSensorFitted,
                    row.backupSensorDetail,
                    row.backupSensorTestMethod,
                    row.backupSensorTestMethodOther,
                    row.backupSensorTestResult,
                    row.backupSensorEngineerNotes,
                    row.backupSensorLatched,
                    row.backupSensorCR,
                    row.airPressureSensorFitted,
                    row.airPressureSensorDetail,
                    row.airPressureSensorTestMethod,
                    row.airPressureSensorTestMethodOther,
                    row.airPressureSensorTestResult,
                    row.airPressureSensorEngineerNotes,
                    row.airPressureSensorLatched,
                    row.airPressureSensorCR,
                    row.packCheckSensorFitted,
                    row.packCheckSensorDetail,
                    row.packCheckSensorTestMethod,
                    row.packCheckSensorTestMethodOther,
                    row.packCheckSensorTestResult,
                    row.packCheckSensorEngineerNotes,
                    row.packCheckSensorLatched,
                    row.packCheckSensorCR,
                    row.speedSensorFitted,
                    row.speedSensorDetail,
                    row.speedSensorTestMethod,
                    row.speedSensorTestMethodOther,
                    row.speedSensorTestResult,
                    row.speedSensorEngineerNotes,
                    row.speedSensorLatched,
                    row.speedSensorCR,
                    row.detectNotificationResult,
                    row.detectNotificationEngineerNotes,
                    row.binDoorMonitorFitted,
                    row.binDoorMonitorDetail,
                    row.binDoorStatusAsFound,
                    row.binDoorUnlockedIndication,
                    row.binDoorOpenIndication,
                    row.binDoorTimeoutTimer,
                    row.binDoorTimeoutResult,
                    row.binDoorLatched,
                    row.binDoorCR,
                    row.binDoorEngineerNotes,
                    row.operatorName,
                    row.operatorTestWitnessed,
                    row.operatorTestResultFerrous,
                    row.operatorTestResultNonFerrous,
                    row.operatorTestResultStainless,
                    row.operatorTestResultLargeMetal,
                    row.operatorTestResultCertNumberFerrous,
                    row.operatorTestResultCertNumberNonFerrous,
                    row.operatorTestResultCertNumberStainless,
                    row.operatorTestResultCertNumberLargeMetal,
                    row.smeName,
                    row.smeEngineerNotes,
//                    row.sensitivityCompliance,
//                    row.essentialRequirementCompliance,
//                    row.failsafeCompliance,
//                    row.bestSensitivityCompliance,
//                    row.sensitivityRecommendations,
//                    row.performanceValidationIssued,
                    row.detectionSetting1label,
                    row.detectionSetting2label,
                    row.detectionSetting3label,
                    row.detectionSetting4label,
                    row.detectionSetting5label,
                    row.detectionSetting6label,
                    row.detectionSetting7label,
                    row.detectionSetting8label,
                    row.detectionSettingPvResult,
                    row.ferrousTestPvResult,
                    row.nonFerrousTestPvResult,
                    row.stainlessTestPvResult,
                    row.smeTestPvResult,
                    row.infeedSensorTestPvResult,
                    row.binFullSensorTestPvResult,
                    row.largeMetalTestPvResult,
                    row.rejectConfirmSensorTestPvResult,
                    row.backupSensorTestPvResult,
                    row.airPressureSensorTestPvResult,
                    row.packCheckSensorTestPvResult,
                    row.speedSensorTestPvResult,
                    row.binDoorMonitorTestPvResult,



                )


                // Write data to the CSV file
                FileWriter(csvFile).use { writer ->
                    writer.append(data.joinToString(";"))
                    writer.append("\n")  // Add new line for CSV row
                }

                // Check if file exists and is not empty
                if (csvFile.exists() && csvFile.length() > 0) {
                    InAppLogger.d("CSV file written successfully: ${csvFile.absolutePath}")
                    _isUploading.value = false
                    csvFile  // Return the file on success
                } else {
                    InAppLogger.d("CSV file was either empty or does not exist.")
                    _isUploading.value = false
                    null  // Return null if the file is empty

                }
            } catch (e: IOException) {
                e.printStackTrace()
                InAppLogger.e("Error writing CSV file: ${e.message}")
                _isUploading.value = false
                null  // Return null on any exception
            }
        }
    }

    // Add a method to clear all relevant data
    fun clearCalibrationData() {}


    fun deleteCalibration(calibrationId: String) {
        InAppLogger.d("Deleting MD calibration with ID: $calibrationId")
        viewModelScope.launch {
            calibrationDao.deleteCalibration(calibrationId)
        }
    }

    // Add this to stop the ViewModel scope
    override fun onCleared() {
        super.onCleared()
        InAppLogger.d("Clearing the MD Calibration View Model")
        viewModelScope.cancel()  // Cancels all active jobs in this ViewModel scope
    }

    suspend fun updateSystemLocationLocally() {
        mdSystemsDAO.updateLastLocation(systemId.value, newLocation.value)

    }

    suspend fun finaliseCalibrationAndUpload(
        context: Context,
        apiService: ApiService,
        onResult: (String) -> Unit
    ) {
        try {
            // End calibration
            InAppLogger.d("Updating the calibration end time...")
            updateCalibrationEnd()


            InAppLogger.d("Updating the last calibration in the local database...")
            // Update local database last calibration date

            val lastCalibrationDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            mdSystemsDAO.updateLastCalibrationDate(
                systemId.value,
                lastCalibrationDate
            )

            InAppLogger.d("Syncing with the cloud...")

            // Try syncing with the cloud
            repository.updateSystem(
                context = context,
                cloudId = cloudSystemId.value,
                localId = systemId.value,
                tempId = tempSystemId.value
            )

            // Generate + upload CSV
            val csvSuccess = createAndUploadCsv(context, calibrationId.value, apiService)
            if (csvSuccess) {
                onResult("✅ Calibration completed and uploaded to the cloud.")
                InAppLogger.d("Calibration completed and uploaded to the cloud.")
            } else {
                onResult("⚠️ Calibration completed, but NOT uploaded to the cloud. Please try again later.")
                InAppLogger.d("Calibration completed, but NOT uploaded to the cloud.")
            }
        } catch (e: Exception) {
            InAppLogger.e("Error finishing calibration: ${e.message}")
            onResult("❌ An error occurred while finishing calibration. Please try again.")
        }
    }


}


