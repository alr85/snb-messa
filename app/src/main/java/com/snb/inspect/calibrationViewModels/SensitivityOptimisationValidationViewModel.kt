package com.snb.inspect.calibrationViewModels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snb.inspect.dataClasses.SensitivityOptimisationValidationLocal
import com.snb.inspect.repositories.SensitivityOptimisationValidationRepository
import com.snb.inspect.dataClasses.MetalDetectorWithFullDetails
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

    // UI State fields
    var productDescription = mutableStateOf("")
    var productLibraryReference = mutableStateOf("")
    var productLibraryNumber = mutableStateOf("")
    var beltSpeed = mutableStateOf("")

    var sensitivityAsFoundFerrous = mutableStateOf("")
    var sensitivityAsFoundNonFerrous = mutableStateOf("")
    var sensitivityAsFoundStainless = mutableStateOf("")
    
    var detectionSettingAsFound1 = mutableStateOf("")
    var detectionSettingAsFound2 = mutableStateOf("")
    var detectionSettingAsFound3 = mutableStateOf("")
    var detectionSettingAsFound4 = mutableStateOf("")
    var detectionSettingAsFound5 = mutableStateOf("")
    var detectionSettingAsFound6 = mutableStateOf("")
    var detectionSettingAsFound7 = mutableStateOf("")
    var detectionSettingAsFound8 = mutableStateOf("")

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
    var validationTest1Passes = mutableStateOf("0")
    var validationTest1Successes = mutableStateOf("0")

    var validationTest2Description = mutableStateOf("")
    var validationTest2Passes = mutableStateOf("0")
    var validationTest2Successes = mutableStateOf("0")

    var validationTest3Description = mutableStateOf("")
    var validationTest3Passes = mutableStateOf("0")
    var validationTest3Successes = mutableStateOf("0")

    var sensitivityAsLeftFerrous = mutableStateOf("")
    var sensitivityAsLeftNonFerrous = mutableStateOf("")
    var sensitivityAsLeftStainless = mutableStateOf("")

    var detectionSettingAsLeft1 = mutableStateOf("")
    var detectionSettingAsLeft2 = mutableStateOf("")
    var detectionSettingAsLeft3 = mutableStateOf("")
    var detectionSettingAsLeft4 = mutableStateOf("")
    var detectionSettingAsLeft5 = mutableStateOf("")
    var detectionSettingAsLeft6 = mutableStateOf("")
    var detectionSettingAsLeft7 = mutableStateOf("")
    var detectionSettingAsLeft8 = mutableStateOf("")

    var systemComments = mutableStateOf("")
    var productComments = mutableStateOf("")
    var customerName = mutableStateOf("")
    var lastLocation = mutableStateOf("")

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
                sensitivityAsFoundNonFerrous.value = existing.sensitivityAsFoundNonFerrous
                sensitivityAsFoundStainless.value = existing.sensitivityAsFoundStainless
                
                detectionSettingAsFound1.value = existing.detectionSettingAsFound1
                detectionSettingAsFound2.value = existing.detectionSettingAsFound2
                detectionSettingAsFound3.value = existing.detectionSettingAsFound3
                detectionSettingAsFound4.value = existing.detectionSettingAsFound4
                detectionSettingAsFound5.value = existing.detectionSettingAsFound5
                detectionSettingAsFound6.value = existing.detectionSettingAsFound6
                detectionSettingAsFound7.value = existing.detectionSettingAsFound7
                detectionSettingAsFound8.value = existing.detectionSettingAsFound8

                detectionSetting1label.value = existing.detectionSetting1label
                detectionSetting2label.value = existing.detectionSetting2label
                detectionSetting3label.value = existing.detectionSetting3label
                detectionSetting4label.value = existing.detectionSetting4label
                detectionSetting5label.value = existing.detectionSetting5label
                detectionSetting6label.value = existing.detectionSetting6label
                detectionSetting7label.value = existing.detectionSetting7label
                detectionSetting8label.value = existing.detectionSetting8label
                
                validationTest1Description.value = existing.validationTest1Description
                validationTest1Passes.value = existing.validationTest1Passes.toString()
                validationTest1Successes.value = existing.validationTest1Successes.toString()
                
                validationTest2Description.value = existing.validationTest2Description
                validationTest2Passes.value = existing.validationTest2Passes.toString()
                validationTest2Successes.value = existing.validationTest2Successes.toString()
                
                validationTest3Description.value = existing.validationTest3Description
                validationTest3Passes.value = existing.validationTest3Passes.toString()
                validationTest3Successes.value = existing.validationTest3Successes.toString()
                
                sensitivityAsLeftFerrous.value = existing.sensitivityAsLeftFerrous
                sensitivityAsLeftNonFerrous.value = existing.sensitivityAsLeftNonFerrous
                sensitivityAsLeftStainless.value = existing.sensitivityAsLeftStainless
                
                detectionSettingAsLeft1.value = existing.detectionSettingAsLeft1
                detectionSettingAsLeft2.value = existing.detectionSettingAsLeft2
                detectionSettingAsLeft3.value = existing.detectionSettingAsLeft3
                detectionSettingAsLeft4.value = existing.detectionSettingAsLeft4
                detectionSettingAsLeft5.value = existing.detectionSettingAsLeft5
                detectionSettingAsLeft6.value = existing.detectionSettingAsLeft6
                detectionSettingAsLeft7.value = existing.detectionSettingAsLeft7
                detectionSettingAsLeft8.value = existing.detectionSettingAsLeft8
                
                systemComments.value = existing.systemComments
                productComments.value = existing.productComments
                customerName.value = existing.customerName
                lastLocation.value = existing.lastLocation
            } else {
                // Initialize with defaults or machine info
                customerName.value = system.customerName
                lastLocation.value = system.lastLocation
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
                sensitivityAsFoundNonFerrous = this@SensitivityOptimisationValidationViewModel.sensitivityAsFoundNonFerrous.value
                sensitivityAsFoundStainless = this@SensitivityOptimisationValidationViewModel.sensitivityAsFoundStainless.value
                
                detectionSettingAsFound1 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound1.value
                detectionSettingAsFound2 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound2.value
                detectionSettingAsFound3 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound3.value
                detectionSettingAsFound4 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound4.value
                detectionSettingAsFound5 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound5.value
                detectionSettingAsFound6 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound6.value
                detectionSettingAsFound7 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound7.value
                detectionSettingAsFound8 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsFound8.value

                detectionSetting1label = this@SensitivityOptimisationValidationViewModel.detectionSetting1label.value
                detectionSetting2label = this@SensitivityOptimisationValidationViewModel.detectionSetting2label.value
                detectionSetting3label = this@SensitivityOptimisationValidationViewModel.detectionSetting3label.value
                detectionSetting4label = this@SensitivityOptimisationValidationViewModel.detectionSetting4label.value
                detectionSetting5label = this@SensitivityOptimisationValidationViewModel.detectionSetting5label.value
                detectionSetting6label = this@SensitivityOptimisationValidationViewModel.detectionSetting6label.value
                detectionSetting7label = this@SensitivityOptimisationValidationViewModel.detectionSetting7label.value
                detectionSetting8label = this@SensitivityOptimisationValidationViewModel.detectionSetting8label.value
                
                validationTest1Description = this@SensitivityOptimisationValidationViewModel.validationTest1Description.value
                validationTest1Passes = this@SensitivityOptimisationValidationViewModel.validationTest1Passes.value.toIntOrNull() ?: 0
                validationTest1Successes = this@SensitivityOptimisationValidationViewModel.validationTest1Successes.value.toIntOrNull() ?: 0
                
                validationTest2Description = this@SensitivityOptimisationValidationViewModel.validationTest2Description.value
                validationTest2Passes = this@SensitivityOptimisationValidationViewModel.validationTest2Passes.value.toIntOrNull() ?: 0
                validationTest2Successes = this@SensitivityOptimisationValidationViewModel.validationTest2Successes.value.toIntOrNull() ?: 0
                
                validationTest3Description = this@SensitivityOptimisationValidationViewModel.validationTest3Description.value
                validationTest3Passes = this@SensitivityOptimisationValidationViewModel.validationTest3Passes.value.toIntOrNull() ?: 0
                validationTest3Successes = this@SensitivityOptimisationValidationViewModel.validationTest3Successes.value.toIntOrNull() ?: 0
                
                sensitivityAsLeftFerrous = this@SensitivityOptimisationValidationViewModel.sensitivityAsLeftFerrous.value
                sensitivityAsLeftNonFerrous = this@SensitivityOptimisationValidationViewModel.sensitivityAsLeftNonFerrous.value
                sensitivityAsLeftStainless = this@SensitivityOptimisationValidationViewModel.sensitivityAsLeftStainless.value
                
                detectionSettingAsLeft1 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft1.value
                detectionSettingAsLeft2 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft2.value
                detectionSettingAsLeft3 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft3.value
                detectionSettingAsLeft4 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft4.value
                detectionSettingAsLeft5 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft5.value
                detectionSettingAsLeft6 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft6.value
                detectionSettingAsLeft7 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft7.value
                detectionSettingAsLeft8 = this@SensitivityOptimisationValidationViewModel.detectionSettingAsLeft8.value
                
                systemComments = this@SensitivityOptimisationValidationViewModel.systemComments.value
                productComments = this@SensitivityOptimisationValidationViewModel.productComments.value
                customerName = this@SensitivityOptimisationValidationViewModel.customerName.value
                lastLocation = this@SensitivityOptimisationValidationViewModel.lastLocation.value
            }
            repository.insertOrUpdate(sov)
        }
    }
}
