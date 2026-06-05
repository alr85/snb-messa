package com.snb.inspect.calibrationViewModels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snb.inspect.dataClasses.SensitivityOptimisationValidationLocal
import com.snb.inspect.repositories.SensitivityOptimisationValidationRepository
import com.snb.inspect.dataClasses.MetalDetectorWithFullDetails
import com.snb.inspect.formModules.YesNoState
import com.snb.inspect.util.toYesNoState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SensitivityOptimisationValidationViewModel(
    private val repository: SensitivityOptimisationValidationRepository,
    val sovId: String,
    val system: MetalDetectorWithFullDetails,
    val engineerId: Int,
    detectionSetting1label: String,
    detectionSetting2label: String,
    detectionSetting3label: String,
    detectionSetting4label: String,
    detectionSetting5label: String,
    detectionSetting6label: String,
    detectionSetting7label: String,
    detectionSetting8label: String
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    // UI State fields
    var productDescription = mutableStateOf("")
    var productLibraryReference = mutableStateOf("")
    var productLibraryNumber = mutableStateOf("")
    var beltSpeed = mutableStateOf("")

    // AS FOUND
    var sensitivityAsFoundFerrous = mutableStateOf("")
    var sampleCertAsFoundFerrous = mutableStateOf("")
    var detectRejectAsFoundFerrousLeading = mutableStateOf(YesNoState.NO)
    var peakAsFoundFerrousLeading = mutableStateOf("")
    var detectRejectAsFoundFerrousMiddle = mutableStateOf(YesNoState.NO)
    var peakAsFoundFerrousMiddle = mutableStateOf("")
    var detectRejectAsFoundFerrousTrailing = mutableStateOf(YesNoState.NO)
    var peakAsFoundFerrousTrailing = mutableStateOf("")
    var notesAsFoundFerrous = mutableStateOf("")

    var sensitivityAsFoundNonFerrous = mutableStateOf("")
    var sampleCertAsFoundNonFerrous = mutableStateOf("")
    var detectRejectAsFoundNonFerrousLeading = mutableStateOf(YesNoState.NO)
    var peakAsFoundNonFerrousLeading = mutableStateOf("")
    var detectRejectAsFoundNonFerrousMiddle = mutableStateOf(YesNoState.NO)
    var peakAsFoundNonFerrousMiddle = mutableStateOf("")
    var detectRejectAsFoundNonFerrousTrailing = mutableStateOf(YesNoState.NO)
    var peakAsFoundNonFerrousTrailing = mutableStateOf("")
    var notesAsFoundNonFerrous = mutableStateOf("")

    var sensitivityAsFoundStainless = mutableStateOf("")
    var sampleCertAsFoundStainless = mutableStateOf("")
    var detectRejectAsFoundStainlessLeading = mutableStateOf(YesNoState.NO)
    var peakAsFoundStainlessLeading = mutableStateOf("")
    var detectRejectAsFoundStainlessMiddle = mutableStateOf(YesNoState.NO)
    var peakAsFoundStainlessMiddle = mutableStateOf("")
    var detectRejectAsFoundStainlessTrailing = mutableStateOf(YesNoState.NO)
    var peakAsFoundStainlessTrailing = mutableStateOf("")
    var notesAsFoundStainless = mutableStateOf("")

    var detectionSettingAsFound1 = mutableStateOf("")
    var detectionSettingAsFound2 = mutableStateOf("")
    var detectionSettingAsFound3 = mutableStateOf("")
    var detectionSettingAsFound4 = mutableStateOf("")
    var detectionSettingAsFound5 = mutableStateOf("")
    var detectionSettingAsFound6 = mutableStateOf("")
    var detectionSettingAsFound7 = mutableStateOf("")
    var detectionSettingAsFound8 = mutableStateOf("")
    var notesAsFoundDetectionSettings = mutableStateOf("")
    var productPeakSignalAsFound = mutableStateOf("")

    // AS LEFT
    var sensitivityAsLeftFerrous = mutableStateOf("")
    var sampleCertAsLeftFerrous = mutableStateOf("")
    var detectRejectAsLeftFerrousLeading = mutableStateOf(YesNoState.NO)
    var peakAsLeftFerrousLeading = mutableStateOf("")
    var detectRejectAsLeftFerrousMiddle = mutableStateOf(YesNoState.NO)
    var peakAsLeftFerrousMiddle = mutableStateOf("")
    var detectRejectAsLeftFerrousTrailing = mutableStateOf(YesNoState.NO)
    var peakAsLeftFerrousTrailing = mutableStateOf("")
    var notesAsLeftFerrous = mutableStateOf("")

    var sensitivityAsLeftNonFerrous = mutableStateOf("")
    var sampleCertAsLeftNonFerrous = mutableStateOf("")
    var detectRejectAsLeftNonFerrousLeading = mutableStateOf(YesNoState.NO)
    var peakAsLeftNonFerrousLeading = mutableStateOf("")
    var detectRejectAsLeftNonFerrousMiddle = mutableStateOf(YesNoState.NO)
    var peakAsLeftNonFerrousMiddle = mutableStateOf("")
    var detectRejectAsLeftNonFerrousTrailing = mutableStateOf(YesNoState.NO)
    var peakAsLeftNonFerrousTrailing = mutableStateOf("")
    var notesAsLeftNonFerrous = mutableStateOf("")

    var sensitivityAsLeftStainless = mutableStateOf("")
    var sampleCertAsLeftStainless = mutableStateOf("")
    var detectRejectAsLeftStainlessLeading = mutableStateOf(YesNoState.NO)
    var peakAsLeftStainlessLeading = mutableStateOf("")
    var detectRejectAsLeftStainlessMiddle = mutableStateOf(YesNoState.NO)
    var peakAsLeftStainlessMiddle = mutableStateOf("")
    var detectRejectAsLeftStainlessTrailing = mutableStateOf(YesNoState.NO)
    var peakAsLeftStainlessTrailing = mutableStateOf("")
    var notesAsLeftStainless = mutableStateOf("")

    var detectionSettingAsLeft1 = mutableStateOf("")
    var detectionSettingAsLeft2 = mutableStateOf("")
    var detectionSettingAsLeft3 = mutableStateOf("")
    var detectionSettingAsLeft4 = mutableStateOf("")
    var detectionSettingAsLeft5 = mutableStateOf("")
    var detectionSettingAsLeft6 = mutableStateOf("")
    var detectionSettingAsLeft7 = mutableStateOf("")
    var detectionSettingAsLeft8 = mutableStateOf("")
    var notesAsLeftDetectionSettings = mutableStateOf("")
    var productPeakSignalAsLeft = mutableStateOf("")

    // Labels
    var detectionSetting1label = mutableStateOf(detectionSetting1label)
    var detectionSetting2label = mutableStateOf(detectionSetting2label)
    var detectionSetting3label = mutableStateOf(detectionSetting3label)
    var detectionSetting4label = mutableStateOf(detectionSetting4label)
    var detectionSetting5label = mutableStateOf(detectionSetting5label)
    var detectionSetting6label = mutableStateOf(detectionSetting6label)
    var detectionSetting7label = mutableStateOf(detectionSetting7label)
    var detectionSetting8label = mutableStateOf(detectionSetting8label)

    // Validation Results
    var validationTest1Description = mutableStateOf("")
    var val1LeadingPasses = mutableStateOf("0")
    var val1LeadingSuccesses = mutableStateOf("0")
    var val1MiddlePasses = mutableStateOf("0")
    var val1MiddleSuccesses = mutableStateOf("0")
    var val1TrailingPasses = mutableStateOf("0")
    var val1TrailingSuccesses = mutableStateOf("0")

    var validationTest2Description = mutableStateOf("")
    var val2LeadingPasses = mutableStateOf("0")
    var val2LeadingSuccesses = mutableStateOf("0")
    var val2MiddlePasses = mutableStateOf("0")
    var val2MiddleSuccesses = mutableStateOf("0")
    var val2TrailingPasses = mutableStateOf("0")
    var val2TrailingSuccesses = mutableStateOf("0")

    var validationTest3Description = mutableStateOf("")
    var val3LeadingPasses = mutableStateOf("0")
    var val3LeadingSuccesses = mutableStateOf("0")
    var val3MiddlePasses = mutableStateOf("0")
    var val3MiddleSuccesses = mutableStateOf("0")
    var val3TrailingPasses = mutableStateOf("0")
    var val3TrailingSuccesses = mutableStateOf("0")

    var systemComments = mutableStateOf("")
    var productComments = mutableStateOf("")
    var customerName = mutableStateOf("")
    var lastLocation = mutableStateOf("")
    var newLocation = mutableStateOf("")

    private val _currentScreenNextEnabled = MutableStateFlow(true)
    val currentScreenNextEnabled = _currentScreenNextEnabled

    fun setCurrentScreenNextEnabled(enabled: Boolean) {
        _currentScreenNextEnabled.value = enabled
    }

    init {
        loadSov()
    }

    private fun loadSov() {
        viewModelScope.launch {
            val existing = repository.getById(sovId)
            if (existing != null) {
                productDescription.value = existing.productDescription
                productLibraryReference.value = existing.productLibraryReference
                productLibraryNumber.value = existing.productLibraryNumber
                beltSpeed.value = existing.beltSpeed
                
                sensitivityAsFoundFerrous.value = existing.sensitivityAsFoundFerrous
                sampleCertAsFoundFerrous.value = existing.sampleCertAsFoundFerrous
                detectRejectAsFoundFerrousLeading.value = existing.detectRejectAsFoundFerrousLeading.toYesNoState()
                peakAsFoundFerrousLeading.value = existing.peakAsFoundFerrousLeading
                detectRejectAsFoundFerrousMiddle.value = existing.detectRejectAsFoundFerrousMiddle.toYesNoState()
                peakAsFoundFerrousMiddle.value = existing.peakAsFoundFerrousMiddle
                detectRejectAsFoundFerrousTrailing.value = existing.detectRejectAsFoundFerrousTrailing.toYesNoState()
                peakAsFoundFerrousTrailing.value = existing.peakAsFoundFerrousTrailing
                notesAsFoundFerrous.value = existing.notesAsFoundFerrous

                sensitivityAsFoundNonFerrous.value = existing.sensitivityAsFoundNonFerrous
                sampleCertAsFoundNonFerrous.value = existing.sampleCertAsFoundNonFerrous
                detectRejectAsFoundNonFerrousLeading.value = existing.detectRejectAsFoundNonFerrousLeading.toYesNoState()
                peakAsFoundNonFerrousLeading.value = existing.peakAsFoundNonFerrousLeading
                detectRejectAsFoundNonFerrousMiddle.value = existing.detectRejectAsFoundNonFerrousMiddle.toYesNoState()
                peakAsFoundNonFerrousMiddle.value = existing.peakAsFoundNonFerrousMiddle
                detectRejectAsFoundNonFerrousTrailing.value = existing.detectRejectAsFoundNonFerrousTrailing.toYesNoState()
                peakAsFoundNonFerrousTrailing.value = existing.peakAsFoundNonFerrousTrailing
                notesAsFoundNonFerrous.value = existing.notesAsFoundNonFerrous

                sensitivityAsFoundStainless.value = existing.sensitivityAsFoundStainless
                sampleCertAsFoundStainless.value = existing.sampleCertAsFoundStainless
                detectRejectAsFoundStainlessLeading.value = existing.detectRejectAsFoundStainlessLeading.toYesNoState()
                peakAsFoundStainlessLeading.value = existing.peakAsFoundStainlessLeading
                detectRejectAsFoundStainlessMiddle.value = existing.detectRejectAsFoundStainlessMiddle.toYesNoState()
                peakAsFoundStainlessMiddle.value = existing.peakAsFoundStainlessMiddle
                detectRejectAsFoundStainlessTrailing.value = existing.detectRejectAsFoundStainlessTrailing.toYesNoState()
                peakAsFoundStainlessTrailing.value = existing.peakAsFoundStainlessTrailing
                notesAsFoundStainless.value = existing.notesAsFoundStainless

                detectionSettingAsFound1.value = existing.detectionSettingAsFound1
                detectionSettingAsFound2.value = existing.detectionSettingAsFound2
                detectionSettingAsFound3.value = existing.detectionSettingAsFound3
                detectionSettingAsFound4.value = existing.detectionSettingAsFound4
                detectionSettingAsFound5.value = existing.detectionSettingAsFound5
                detectionSettingAsFound6.value = existing.detectionSettingAsFound6
                detectionSettingAsFound7.value = existing.detectionSettingAsFound7
                detectionSettingAsFound8.value = existing.detectionSettingAsFound8
                notesAsFoundDetectionSettings.value = existing.notesAsFoundDetectionSettings
                productPeakSignalAsFound.value = existing.productPeakSignalAsFound

                detectionSetting1label.value = existing.detectionSetting1label
                detectionSetting2label.value = existing.detectionSetting2label
                detectionSetting3label.value = existing.detectionSetting3label
                detectionSetting4label.value = existing.detectionSetting4label
                detectionSetting5label.value = existing.detectionSetting5label
                detectionSetting6label.value = existing.detectionSetting6label
                detectionSetting7label.value = existing.detectionSetting7label
                detectionSetting8label.value = existing.detectionSetting8label
                
                validationTest1Description.value = existing.validationTest1Description
                val1LeadingPasses.value = existing.val1LeadingPasses.toString()
                val1LeadingSuccesses.value = existing.val1LeadingSuccesses.toString()
                val1MiddlePasses.value = existing.val1MiddlePasses.toString()
                val1MiddleSuccesses.value = existing.val1MiddleSuccesses.toString()
                val1TrailingPasses.value = existing.val1TrailingPasses.toString()
                val1TrailingSuccesses.value = existing.val1TrailingSuccesses.toString()
                
                validationTest2Description.value = existing.validationTest2Description
                val2LeadingPasses.value = existing.val2LeadingPasses.toString()
                val2LeadingSuccesses.value = existing.val2LeadingSuccesses.toString()
                val2MiddlePasses.value = existing.val2MiddlePasses.toString()
                val2MiddleSuccesses.value = existing.val2MiddleSuccesses.toString()
                val2TrailingPasses.value = existing.val2TrailingPasses.toString()
                val2TrailingSuccesses.value = existing.val2TrailingSuccesses.toString()
                
                validationTest3Description.value = existing.validationTest3Description
                val3LeadingPasses.value = existing.val3LeadingPasses.toString()
                val3LeadingSuccesses.value = existing.val3LeadingSuccesses.toString()
                val3MiddlePasses.value = existing.val3MiddlePasses.toString()
                val3MiddleSuccesses.value = existing.val3MiddleSuccesses.toString()
                val3TrailingPasses.value = existing.val3TrailingPasses.toString()
                val3TrailingSuccesses.value = existing.val3TrailingSuccesses.toString()
                
                sensitivityAsLeftFerrous.value = existing.sensitivityAsLeftFerrous
                sampleCertAsLeftFerrous.value = existing.sampleCertAsLeftFerrous
                detectRejectAsLeftFerrousLeading.value = existing.detectRejectAsLeftFerrousLeading.toYesNoState()
                peakAsLeftFerrousLeading.value = existing.peakAsLeftFerrousLeading
                detectRejectAsLeftFerrousMiddle.value = existing.detectRejectAsLeftFerrousMiddle.toYesNoState()
                peakAsLeftFerrousMiddle.value = existing.peakAsLeftFerrousMiddle
                detectRejectAsLeftFerrousTrailing.value = existing.detectRejectAsLeftFerrousTrailing.toYesNoState()
                peakAsLeftFerrousTrailing.value = existing.peakAsLeftFerrousTrailing
                notesAsLeftFerrous.value = existing.notesAsLeftFerrous

                sensitivityAsLeftNonFerrous.value = existing.sensitivityAsLeftNonFerrous
                sampleCertAsLeftNonFerrous.value = existing.sampleCertAsLeftNonFerrous
                detectRejectAsLeftNonFerrousLeading.value = existing.detectRejectAsLeftNonFerrousLeading.toYesNoState()
                peakAsLeftNonFerrousLeading.value = existing.peakAsLeftNonFerrousLeading
                detectRejectAsLeftNonFerrousMiddle.value = existing.detectRejectAsLeftNonFerrousMiddle.toYesNoState()
                peakAsLeftNonFerrousMiddle.value = existing.peakAsLeftNonFerrousMiddle
                detectRejectAsLeftNonFerrousTrailing.value = existing.detectRejectAsLeftNonFerrousTrailing.toYesNoState()
                peakAsLeftNonFerrousTrailing.value = existing.peakAsLeftNonFerrousTrailing
                notesAsLeftNonFerrous.value = existing.notesAsLeftNonFerrous

                sensitivityAsLeftStainless.value = existing.sensitivityAsLeftStainless
                sampleCertAsLeftStainless.value = existing.sampleCertAsLeftStainless
                detectRejectAsLeftStainlessLeading.value = existing.detectRejectAsLeftStainlessLeading.toYesNoState()
                peakAsLeftStainlessLeading.value = existing.peakAsLeftStainlessLeading
                detectRejectAsLeftStainlessMiddle.value = existing.detectRejectAsLeftStainlessMiddle.toYesNoState()
                peakAsLeftStainlessMiddle.value = existing.peakAsLeftStainlessMiddle
                detectRejectAsLeftStainlessTrailing.value = existing.detectRejectAsLeftStainlessTrailing.toYesNoState()
                peakAsLeftStainlessTrailing.value = existing.peakAsLeftStainlessTrailing
                notesAsLeftStainless.value = existing.notesAsLeftStainless

                detectionSettingAsLeft1.value = existing.detectionSettingAsLeft1
                detectionSettingAsLeft2.value = existing.detectionSettingAsLeft2
                detectionSettingAsLeft3.value = existing.detectionSettingAsLeft3
                detectionSettingAsLeft4.value = existing.detectionSettingAsLeft4
                detectionSettingAsLeft5.value = existing.detectionSettingAsLeft5
                detectionSettingAsLeft6.value = existing.detectionSettingAsLeft6
                detectionSettingAsLeft7.value = existing.detectionSettingAsLeft7
                detectionSettingAsLeft8.value = existing.detectionSettingAsLeft8
                notesAsLeftDetectionSettings.value = existing.notesAsLeftDetectionSettings
                productPeakSignalAsLeft.value = existing.productPeakSignalAsLeft
                
                systemComments.value = existing.systemComments
                productComments.value = existing.productComments
                customerName.value = existing.customerName
                lastLocation.value = existing.lastLocation
                newLocation.value = existing.newLocation
            } else {
                // Initialize with defaults or machine info
                customerName.value = system.customerName
                lastLocation.value = system.lastLocation
                newLocation.value = system.lastLocation
                // Initialize validation descriptions based on system type
                if (system.systemType.contains("X-ray", ignoreCase = true)) {
                    validationTest1Description.value = "Stainless Steel"
                    validationTest2Description.value = "Foreign Body 1"
                    validationTest3Description.value = "Foreign Body 2"
                } else {
                    validationTest1Description.value = "Ferrous"
                    validationTest2Description.value = "Non-Ferrous"
                    validationTest3Description.value = "Stainless Steel"
                }
                saveSov()
            }
            _isLoading.value = false
        }
    }

    fun saveSov() {
        viewModelScope.launch {
            val sov = SensitivityOptimisationValidationLocal(sovId).apply {
                systemId = system.id
                cloudSystemId = system.cloudId ?: 0
                serialNumber = system.serialNumber
                engineerId = this@SensitivityOptimisationValidationViewModel.engineerId
                customerId = system.customerId
                
                productDescription = this@SensitivityOptimisationValidationViewModel.productDescription.value
                productLibraryReference = this@SensitivityOptimisationValidationViewModel.productLibraryReference.value
                productLibraryNumber = this@SensitivityOptimisationValidationViewModel.productLibraryNumber.value
                beltSpeed = this@SensitivityOptimisationValidationViewModel.beltSpeed.value
                
                sensitivityAsFoundFerrous = this@SensitivityOptimisationValidationViewModel.sensitivityAsFoundFerrous.value
                sampleCertAsFoundFerrous = this@SensitivityOptimisationValidationViewModel.sampleCertAsFoundFerrous.value
                detectRejectAsFoundFerrousLeading = this@SensitivityOptimisationValidationViewModel.detectRejectAsFoundFerrousLeading.value.name
                peakAsFoundFerrousLeading = this@SensitivityOptimisationValidationViewModel.peakAsFoundFerrousLeading.value
                detectRejectAsFoundFerrousMiddle = this@SensitivityOptimisationValidationViewModel.detectRejectAsFoundFerrousMiddle.value.name
                peakAsFoundFerrousMiddle = this@SensitivityOptimisationValidationViewModel.peakAsFoundFerrousMiddle.value
                detectRejectAsFoundFerrousTrailing = this@SensitivityOptimisationValidationViewModel.detectRejectAsFoundFerrousTrailing.value.name
                peakAsFoundFerrousTrailing = this@SensitivityOptimisationValidationViewModel.peakAsFoundFerrousTrailing.value
                notesAsFoundFerrous = this@SensitivityOptimisationValidationViewModel.notesAsFoundFerrous.value

                sensitivityAsFoundNonFerrous = this@SensitivityOptimisationValidationViewModel.sensitivityAsFoundNonFerrous.value
                sampleCertAsFoundNonFerrous = this@SensitivityOptimisationValidationViewModel.sampleCertAsFoundNonFerrous.value
                detectRejectAsFoundNonFerrousLeading = this@SensitivityOptimisationValidationViewModel.detectRejectAsFoundNonFerrousLeading.value.name
                peakAsFoundNonFerrousLeading = this@SensitivityOptimisationValidationViewModel.peakAsFoundNonFerrousLeading.value
                detectRejectAsFoundNonFerrousMiddle = this@SensitivityOptimisationValidationViewModel.detectRejectAsFoundNonFerrousMiddle.value.name
                peakAsFoundNonFerrousMiddle = this@SensitivityOptimisationValidationViewModel.peakAsFoundNonFerrousMiddle.value
                detectRejectAsFoundNonFerrousTrailing = this@SensitivityOptimisationValidationViewModel.detectRejectAsFoundNonFerrousTrailing.value.name
                peakAsFoundNonFerrousTrailing = this@SensitivityOptimisationValidationViewModel.peakAsFoundNonFerrousTrailing.value
                notesAsFoundNonFerrous = this@SensitivityOptimisationValidationViewModel.notesAsFoundNonFerrous.value

                sensitivityAsFoundStainless = this@SensitivityOptimisationValidationViewModel.sensitivityAsFoundStainless.value
                sampleCertAsFoundStainless = this@SensitivityOptimisationValidationViewModel.sampleCertAsFoundStainless.value
                detectRejectAsFoundStainlessLeading = this@SensitivityOptimisationValidationViewModel.detectRejectAsFoundStainlessLeading.value.name
                peakAsFoundStainlessLeading = this@SensitivityOptimisationValidationViewModel.peakAsFoundStainlessLeading.value
                detectRejectAsFoundStainlessMiddle = this@SensitivityOptimisationValidationViewModel.detectRejectAsFoundStainlessMiddle.value.name
                peakAsFoundStainlessMiddle = this@SensitivityOptimisationValidationViewModel.peakAsFoundStainlessMiddle.value
                detectRejectAsFoundStainlessTrailing = this@SensitivityOptimisationValidationViewModel.detectRejectAsFoundStainlessTrailing.value.name
                peakAsFoundStainlessTrailing = this@SensitivityOptimisationValidationViewModel.peakAsFoundStainlessTrailing.value
                notesAsFoundStainless = this@SensitivityOptimisationValidationViewModel.notesAsFoundStainless.value

                detectionSettingAsFound1 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound1.value
                detectionSettingAsFound2 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound2.value
                detectionSettingAsFound3 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound3.value
                detectionSettingAsFound4 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound4.value
                detectionSettingAsFound5 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound5.value
                detectionSettingAsFound6 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound6.value
                detectionSettingAsFound7 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound7.value
                detectionSettingAsFound8 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound8.value
                notesAsFoundDetectionSettings = this@SensitivityOptimisationValidationViewModel.notesAsFoundDetectionSettings.value
                productPeakSignalAsFound = this@SensitivityOptimisationValidationViewModel.productPeakSignalAsFound.value

                detectionSetting1label = this@SensitivityOptimisationValidationViewModel.detectionSetting1label.value
                detectionSetting2label = this@SensitivityOptimisationValidationViewModel.detectionSetting2label.value
                detectionSetting3label = this@SensitivityOptimisationValidationViewModel.detectionSetting3label.value
                detectionSetting4label = this@SensitivityOptimisationValidationViewModel.detectionSetting4label.value
                detectionSetting5label = this@SensitivityOptimisationValidationViewModel.detectionSetting5label.value
                detectionSetting6label = this@SensitivityOptimisationValidationViewModel.detectionSetting6label.value
                detectionSetting7label = this@SensitivityOptimisationValidationViewModel.detectionSetting7label.value
                detectionSetting8label = this@SensitivityOptimisationValidationViewModel.detectionSetting8label.value
                
                validationTest1Description = this@SensitivityOptimisationValidationViewModel.validationTest1Description.value
                val1LeadingPasses = this@SensitivityOptimisationValidationViewModel.val1LeadingPasses.value.toIntOrNull() ?: 0
                val1LeadingSuccesses = this@SensitivityOptimisationValidationViewModel.val1LeadingSuccesses.value.toIntOrNull() ?: 0
                val1MiddlePasses = this@SensitivityOptimisationValidationViewModel.val1MiddlePasses.value.toIntOrNull() ?: 0
                val1MiddleSuccesses = this@SensitivityOptimisationValidationViewModel.val1MiddleSuccesses.value.toIntOrNull() ?: 0
                val1TrailingPasses = this@SensitivityOptimisationValidationViewModel.val1TrailingPasses.value.toIntOrNull() ?: 0
                val1TrailingSuccesses = this@SensitivityOptimisationValidationViewModel.val1TrailingSuccesses.value.toIntOrNull() ?: 0
                
                validationTest2Description = this@SensitivityOptimisationValidationViewModel.validationTest2Description.value
                val2LeadingPasses = this@SensitivityOptimisationValidationViewModel.val2LeadingPasses.value.toIntOrNull() ?: 0
                val2LeadingSuccesses = this@SensitivityOptimisationValidationViewModel.val2LeadingSuccesses.value.toIntOrNull() ?: 0
                val2MiddlePasses = this@SensitivityOptimisationValidationViewModel.val2MiddlePasses.value.toIntOrNull() ?: 0
                val2MiddleSuccesses = this@SensitivityOptimisationValidationViewModel.val2MiddleSuccesses.value.toIntOrNull() ?: 0
                val2TrailingPasses = this@SensitivityOptimisationValidationViewModel.val2TrailingPasses.value.toIntOrNull() ?: 0
                val2TrailingSuccesses = this@SensitivityOptimisationValidationViewModel.val2TrailingSuccesses.value.toIntOrNull() ?: 0
                
                validationTest3Description = this@SensitivityOptimisationValidationViewModel.validationTest3Description.value
                val3LeadingPasses = this@SensitivityOptimisationValidationViewModel.val3LeadingPasses.value.toIntOrNull() ?: 0
                val3LeadingSuccesses = this@SensitivityOptimisationValidationViewModel.val3LeadingSuccesses.value.toIntOrNull() ?: 0
                val3MiddlePasses = this@SensitivityOptimisationValidationViewModel.val3MiddlePasses.value.toIntOrNull() ?: 0
                val3MiddleSuccesses = this@SensitivityOptimisationValidationViewModel.val3MiddleSuccesses.value.toIntOrNull() ?: 0
                val3TrailingPasses = this@SensitivityOptimisationValidationViewModel.val3TrailingPasses.value.toIntOrNull() ?: 0
                val3TrailingSuccesses = this@SensitivityOptimisationValidationViewModel.val3TrailingSuccesses.value.toIntOrNull() ?: 0
                
                sensitivityAsLeftFerrous = this@SensitivityOptimisationValidationViewModel.sensitivityAsLeftFerrous.value
                sampleCertAsLeftFerrous = this@SensitivityOptimisationValidationViewModel.sampleCertAsLeftFerrous.value
                detectRejectAsLeftFerrousLeading = this@SensitivityOptimisationValidationViewModel.detectRejectAsLeftFerrousLeading.value.name
                peakAsLeftFerrousLeading = this@SensitivityOptimisationValidationViewModel.peakAsLeftFerrousLeading.value
                detectRejectAsLeftFerrousMiddle = this@SensitivityOptimisationValidationViewModel.detectRejectAsLeftFerrousMiddle.value.name
                peakAsLeftFerrousMiddle = this@SensitivityOptimisationValidationViewModel.peakAsLeftFerrousMiddle.value
                detectRejectAsLeftFerrousTrailing = this@SensitivityOptimisationValidationViewModel.detectRejectAsLeftFerrousTrailing.value.name
                peakAsLeftFerrousTrailing = this@SensitivityOptimisationValidationViewModel.peakAsLeftFerrousTrailing.value
                notesAsLeftFerrous = this@SensitivityOptimisationValidationViewModel.notesAsLeftFerrous.value

                sensitivityAsLeftNonFerrous = this@SensitivityOptimisationValidationViewModel.sensitivityAsLeftNonFerrous.value
                sampleCertAsLeftNonFerrous = this@SensitivityOptimisationValidationViewModel.sampleCertAsLeftNonFerrous.value
                detectRejectAsLeftNonFerrousLeading = this@SensitivityOptimisationValidationViewModel.detectRejectAsLeftNonFerrousLeading.value.name
                peakAsLeftNonFerrousLeading = this@SensitivityOptimisationValidationViewModel.peakAsLeftNonFerrousLeading.value
                detectRejectAsLeftNonFerrousMiddle = this@SensitivityOptimisationValidationViewModel.detectRejectAsLeftNonFerrousMiddle.value.name
                peakAsLeftNonFerrousMiddle = this@SensitivityOptimisationValidationViewModel.peakAsLeftNonFerrousMiddle.value
                detectRejectAsLeftNonFerrousTrailing = this@SensitivityOptimisationValidationViewModel.detectRejectAsLeftNonFerrousTrailing.value.name
                peakAsLeftNonFerrousTrailing = this@SensitivityOptimisationValidationViewModel.peakAsLeftNonFerrousTrailing.value
                notesAsLeftNonFerrous = this@SensitivityOptimisationValidationViewModel.notesAsLeftNonFerrous.value

                sensitivityAsLeftStainless = this@SensitivityOptimisationValidationViewModel.sensitivityAsLeftStainless.value
                sampleCertAsLeftStainless = this@SensitivityOptimisationValidationViewModel.sampleCertAsLeftStainless.value
                detectRejectAsLeftStainlessLeading = this@SensitivityOptimisationValidationViewModel.detectRejectAsLeftStainlessLeading.value.name
                peakAsLeftStainlessLeading = this@SensitivityOptimisationValidationViewModel.peakAsLeftStainlessLeading.value
                detectRejectAsLeftStainlessMiddle = this@SensitivityOptimisationValidationViewModel.detectRejectAsLeftStainlessMiddle.value.name
                peakAsLeftStainlessMiddle = this@SensitivityOptimisationValidationViewModel.peakAsLeftStainlessMiddle.value
                detectRejectAsLeftStainlessTrailing = this@SensitivityOptimisationValidationViewModel.detectRejectAsLeftStainlessTrailing.value.name
                peakAsLeftStainlessTrailing = this@SensitivityOptimisationValidationViewModel.peakAsLeftStainlessTrailing.value
                notesAsLeftStainless = this@SensitivityOptimisationValidationViewModel.notesAsLeftStainless.value

                detectionSettingAsLeft1 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft1.value
                detectionSettingAsLeft2 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft2.value
                detectionSettingAsLeft3 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft3.value
                detectionSettingAsLeft4 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft4.value
                detectionSettingAsLeft5 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft5.value
                detectionSettingAsLeft6 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft6.value
                detectionSettingAsLeft7 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft7.value
                detectionSettingAsLeft8 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft8.value
                notesAsLeftDetectionSettings = this@SensitivityOptimisationValidationViewModel.notesAsLeftDetectionSettings.value
                productPeakSignalAsLeft = this@SensitivityOptimisationValidationViewModel.productPeakSignalAsLeft.value
                
                systemComments = this@SensitivityOptimisationValidationViewModel.systemComments.value
                productComments = this@SensitivityOptimisationValidationViewModel.productComments.value
                customerName = this@SensitivityOptimisationValidationViewModel.customerName.value
                lastLocation = this@SensitivityOptimisationValidationViewModel.lastLocation.value
                newLocation = this@SensitivityOptimisationValidationViewModel.newLocation.value
            }
            repository.insertOrUpdate(sov)
        }
    }

    fun setNewLocation(location: String) {
        newLocation.value = location
    }

    suspend fun updateSystemLocationLocally() {
        repository.updateSystemLocation(system.id, newLocation.value)
    }

    // Ferrous As Found

    fun enableFerrousAsFound() {
        sampleCertAsFoundFerrous.value = ""
        detectRejectAsFoundFerrousLeading.value = YesNoState.NO
        detectRejectAsFoundFerrousMiddle.value = YesNoState.NO
        detectRejectAsFoundFerrousTrailing.value = YesNoState.NO
        peakAsFoundFerrousLeading.value = ""
        peakAsFoundFerrousMiddle.value = ""
        peakAsFoundFerrousTrailing.value = ""
    }

    fun disableFerrousAsFound() {
        sampleCertAsFoundFerrous.value = "N/A"
        detectRejectAsFoundFerrousLeading.value = YesNoState.NA
        detectRejectAsFoundFerrousMiddle.value = YesNoState.NA
        detectRejectAsFoundFerrousTrailing.value = YesNoState.NA
        peakAsFoundFerrousLeading.value = "N/A"
        peakAsFoundFerrousMiddle.value = "N/A"
        peakAsFoundFerrousTrailing.value = "N/A"
    }

    // Non-Ferrous As Found

    fun enableNonFerrousAsFound() {
        sampleCertAsFoundNonFerrous.value = ""
        detectRejectAsFoundNonFerrousLeading.value = YesNoState.NO
        detectRejectAsFoundNonFerrousMiddle.value = YesNoState.NO
        detectRejectAsFoundNonFerrousTrailing.value = YesNoState.NO
        peakAsFoundNonFerrousLeading.value = ""
        peakAsFoundNonFerrousMiddle.value = ""
        peakAsFoundNonFerrousTrailing.value = ""
    }

    fun disableNonFerrousAsFound() {
        sampleCertAsFoundNonFerrous.value = "N/A"
        detectRejectAsFoundNonFerrousLeading.value = YesNoState.NA
        detectRejectAsFoundNonFerrousMiddle.value = YesNoState.NA
        detectRejectAsFoundNonFerrousTrailing.value = YesNoState.NA
        peakAsFoundNonFerrousLeading.value = "N/A"
        peakAsFoundNonFerrousMiddle.value = "N/A"
        peakAsFoundNonFerrousTrailing.value = "N/A"
    }

    // Stainless As Found

    fun enableStainlessAsFound() {
        sampleCertAsFoundStainless.value = ""
        detectRejectAsFoundStainlessLeading.value = YesNoState.NO
        detectRejectAsFoundStainlessMiddle.value = YesNoState.NO
        detectRejectAsFoundStainlessTrailing.value = YesNoState.NO
        peakAsFoundStainlessLeading.value = ""
        peakAsFoundStainlessMiddle.value = ""
        peakAsFoundStainlessTrailing.value = ""
    }

    fun disableStainlessAsFound() {
        sampleCertAsFoundStainless.value = "N/A"
        detectRejectAsFoundStainlessLeading.value = YesNoState.NA
        detectRejectAsFoundStainlessMiddle.value = YesNoState.NA
        detectRejectAsFoundStainlessTrailing.value = YesNoState.NA
        peakAsFoundStainlessLeading.value = "N/A"
        peakAsFoundStainlessMiddle.value = "N/A"
        peakAsFoundStainlessTrailing.value = "N/A"
    }

    // Ferrous As Left

    fun enableFerrousAsLeft() {
        sampleCertAsLeftFerrous.value = ""
        detectRejectAsLeftFerrousLeading.value = YesNoState.NO
        detectRejectAsLeftFerrousMiddle.value = YesNoState.NO
        detectRejectAsLeftFerrousTrailing.value = YesNoState.NO
        peakAsLeftFerrousLeading.value = ""
        peakAsLeftFerrousMiddle.value = ""
        peakAsLeftFerrousTrailing.value = ""
    }

    fun disableFerrousAsLeft() {
        sampleCertAsLeftFerrous.value = "N/A"
        detectRejectAsLeftFerrousLeading.value = YesNoState.NA
        detectRejectAsLeftFerrousMiddle.value = YesNoState.NA
        detectRejectAsLeftFerrousTrailing.value = YesNoState.NA
        peakAsLeftFerrousLeading.value = "N/A"
        peakAsLeftFerrousMiddle.value = "N/A"
        peakAsLeftFerrousTrailing.value = "N/A"
        val1LeadingSuccesses.value = "0"
        val1MiddleSuccesses.value = "0"
        val1TrailingSuccesses.value = "0"
    }

    // Non-Ferrous As Left

    fun enableNonFerrousAsLeft() {
        sampleCertAsLeftNonFerrous.value = ""
        detectRejectAsLeftNonFerrousLeading.value = YesNoState.NO
        detectRejectAsLeftNonFerrousMiddle.value = YesNoState.NO
        detectRejectAsLeftNonFerrousTrailing.value = YesNoState.NO
        peakAsLeftNonFerrousLeading.value = ""
        peakAsLeftNonFerrousMiddle.value = ""
        peakAsLeftNonFerrousTrailing.value = ""
    }

    fun disableNonFerrousAsLeft() {
        sampleCertAsLeftNonFerrous.value = "N/A"
        detectRejectAsLeftNonFerrousLeading.value = YesNoState.NA
        detectRejectAsLeftNonFerrousMiddle.value = YesNoState.NA
        detectRejectAsLeftNonFerrousTrailing.value = YesNoState.NA
        peakAsLeftNonFerrousLeading.value = "N/A"
        peakAsLeftNonFerrousMiddle.value = "N/A"
        peakAsLeftNonFerrousTrailing.value = "N/A"
        val2LeadingSuccesses.value = "0"
        val2MiddleSuccesses.value = "0"
        val2TrailingSuccesses.value = "0"
    }

    // Stainless As Left

    fun enableStainlessAsLeft() {
        sampleCertAsLeftStainless.value = ""
        detectRejectAsLeftStainlessLeading.value = YesNoState.NO
        detectRejectAsLeftStainlessMiddle.value = YesNoState.NO
        detectRejectAsLeftStainlessTrailing.value = YesNoState.NO
        peakAsLeftStainlessLeading.value = ""
        peakAsLeftStainlessMiddle.value = ""
        peakAsLeftStainlessTrailing.value = ""
    }

    fun disableStainlessAsLeft() {
        sampleCertAsLeftStainless.value = "N/A"
        detectRejectAsLeftStainlessLeading.value = YesNoState.NA
        detectRejectAsLeftStainlessMiddle.value = YesNoState.NA
        detectRejectAsLeftStainlessTrailing.value = YesNoState.NA
        peakAsLeftStainlessLeading.value = "N/A"
        peakAsLeftStainlessMiddle.value = "N/A"
        peakAsLeftStainlessTrailing.value = "N/A"
        val3LeadingSuccesses.value = "0"
        val3MiddleSuccesses.value = "0"
        val3TrailingSuccesses.value = "0"
    }

    suspend fun finaliseAndUpload(
        context: android.content.Context,
        apiService: com.snb.inspect.ApiService,
        onResult: (String) -> Unit
    ) {
        try {
            _isUploading.value = true
            saveSov() // Ensure all current state is saved
            
            val existing = repository.getById(sovId)
            if (existing != null) {
                existing.endDate = LocalDateTime.now().toString()
                repository.insertOrUpdate(existing)
            }

            val result = repository.uploadUnsynced(context, apiService, sovId)
            if (result is com.snb.inspect.FetchResult.Success) {
                onResult("✅ SOV completed and uploaded.")
            } else {
                onResult("⚠️ SOV completed locally, but upload failed. It will retry later.")
            }
        } catch (e: Exception) {
            onResult("❌ Error: ${e.message}")
        } finally {
            _isUploading.value = false
        }
    }
}
