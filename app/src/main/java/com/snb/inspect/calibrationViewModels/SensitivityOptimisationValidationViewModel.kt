package com.snb.inspect.calibrationViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snb.inspect.dataClasses.SensitivityOptimisationValidationLocal
import com.snb.inspect.repositories.SensitivityOptimisationValidationRepository
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.dataClasses.MetalDetectorWithFullDetails
import com.snb.inspect.formModules.YesNoState
import com.snb.inspect.util.InAppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class SensitivityOptimisationValidationViewModel(
    private val repository: SensitivityOptimisationValidationRepository,
    private val mdSystemsRepository: MetalDetectorSystemsRepository,
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

    private val _systemId = mutableStateOf(system.id)
    private val _cloudSystemId = mutableStateOf(system.cloudId ?: 0)
    private val _tempSystemId = mutableStateOf(system.tempId)

    // --- Metadata & State ---
    var startDate = mutableStateOf(LocalDateTime.now().toString())
    var endDate = mutableStateOf("")
    var isSynced = mutableStateOf(false)
    var mapVersion = mutableStateOf("SOV1.0")

    // --- Product Details ---
    var productDescription = mutableStateOf("")
    var productLibraryReference = mutableStateOf("")
    var beltSpeed = mutableStateOf("")
    var productLength = mutableStateOf("")
    var productWidth = mutableStateOf("")
    var productHeight = mutableStateOf("")
    var productWeight = mutableStateOf("")

    // --- VALIDATION RESULTS ---
    var validationTest1Description = mutableStateOf("")
    var val1LeadingPasses = mutableStateOf("")
    var val1LeadingSuccesses = mutableStateOf("")
    var val1MiddlePasses = mutableStateOf("")
    var val1MiddleSuccesses = mutableStateOf("")
    var val1TrailingPasses = mutableStateOf("")
    var val1TrailingSuccesses = mutableStateOf("")

    var validationTest2Description = mutableStateOf("")
    var val2LeadingPasses = mutableStateOf("")
    var val2LeadingSuccesses = mutableStateOf("")
    var val2MiddlePasses = mutableStateOf("")
    var val2MiddleSuccesses = mutableStateOf("")
    var val2TrailingPasses = mutableStateOf("")
    var val2TrailingSuccesses = mutableStateOf("")

    var validationTest3Description = mutableStateOf("")
    var val3LeadingPasses = mutableStateOf("")
    var val3LeadingSuccesses = mutableStateOf("")
    var val3MiddlePasses = mutableStateOf("")
    var val3MiddleSuccesses = mutableStateOf("")
    var val3TrailingPasses = mutableStateOf("")
    var val3TrailingSuccesses = mutableStateOf("")

    // --- AS LEFT ---
    var sensitivityAsLeftFerrous = mutableStateOf("")
    var sampleCertAsLeftFerrous = mutableStateOf("")
    var minSignalAsLeftFerrousLeading = mutableStateOf("")
    var minSignalAsLeftFerrousMiddle = mutableStateOf("")
    var minSignalAsLeftFerrousTrailing = mutableStateOf("")
    var notesAsLeftFerrous = mutableStateOf("")

    var sensitivityAsLeftNonFerrous = mutableStateOf("")
    var sampleCertAsLeftNonFerrous = mutableStateOf("")
    var minSignalAsLeftNonFerrousLeading = mutableStateOf("")
    var minSignalAsLeftNonFerrousMiddle = mutableStateOf("")
    var minSignalAsLeftNonFerrousTrailing = mutableStateOf("")
    var notesAsLeftNonFerrous = mutableStateOf("")

    var sensitivityAsLeftStainless = mutableStateOf("")
    var sampleCertAsLeftStainless = mutableStateOf("")
    var minSignalAsLeftStainlessLeading = mutableStateOf("")
    var minSignalAsLeftStainlessMiddle = mutableStateOf("")
    var minSignalAsLeftStainlessTrailing = mutableStateOf("")
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

    // --- Comments & Labels ---
    var systemComments = mutableStateOf("")
    var productComments = mutableStateOf("")
    var packValidationPassed = mutableStateOf(false)
    var customerName = mutableStateOf("")
    var lastLocation = mutableStateOf("")
    var newLocation = mutableStateOf("")

    var detectionSetting1label = mutableStateOf(detectionSetting1label)
    var detectionSetting2label = mutableStateOf(detectionSetting2label)
    var detectionSetting3label = mutableStateOf(detectionSetting3label)
    var detectionSetting4label = mutableStateOf(detectionSetting4label)
    var detectionSetting5label = mutableStateOf(detectionSetting5label)
    var detectionSetting6label = mutableStateOf(detectionSetting6label)
    var detectionSetting7label = mutableStateOf(detectionSetting7label)
    var detectionSetting8label = mutableStateOf(detectionSetting8label)

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
                // Metadata
                startDate.value = existing.startDate
                endDate.value = existing.endDate
                isSynced.value = existing.isSynced
                mapVersion.value = existing.mapVersion

                _systemId.value = existing.systemId
                _tempSystemId.value = existing.tempSystemId

                // FIX: Ensure cloudSystemId is pulled from the fresh system object if the 
                // SOV record is still 0 (e.g. machine synced while this was incomplete)
                val effectiveCloudId = if (existing.cloudSystemId == 0 && system.cloudId != null) {
                    system.cloudId
                } else {
                    existing.cloudSystemId
                }
                _cloudSystemId.value = effectiveCloudId

                // Product
                productDescription.value = existing.productDescription
                productLibraryReference.value = existing.productLibraryReference
                beltSpeed.value = existing.beltSpeed
                productLength.value = existing.productLength
                productWidth.value = existing.productWidth
                productHeight.value = existing.productHeight
                productWeight.value = existing.productWeight
                
                // Labels
                detectionSetting1label.value = existing.detectionSetting1label
                detectionSetting2label.value = existing.detectionSetting2label
                detectionSetting3label.value = existing.detectionSetting3label
                detectionSetting4label.value = existing.detectionSetting4label
                detectionSetting5label.value = existing.detectionSetting5label
                detectionSetting6label.value = existing.detectionSetting6label
                detectionSetting7label.value = existing.detectionSetting7label
                detectionSetting8label.value = existing.detectionSetting8label
                
                // VALIDATION
                validationTest1Description.value = existing.validationTest1Description
                val1LeadingPasses.value = if (existing.val1LeadingPasses == 0) "" else existing.val1LeadingPasses.toString()
                val1LeadingSuccesses.value = if (existing.val1LeadingSuccesses == 0) "" else existing.val1LeadingSuccesses.toString()
                val1MiddlePasses.value = if (existing.val1MiddlePasses == 0) "" else existing.val1MiddlePasses.toString()
                val1MiddleSuccesses.value = if (existing.val1MiddleSuccesses == 0) "" else existing.val1MiddleSuccesses.toString()
                val1TrailingPasses.value = if (existing.val1TrailingPasses == 0) "" else existing.val1TrailingPasses.toString()
                val1TrailingSuccesses.value = if (existing.val1TrailingSuccesses == 0) "" else existing.val1TrailingSuccesses.toString()
                
                validationTest2Description.value = existing.validationTest2Description
                val2LeadingPasses.value = if (existing.val2LeadingPasses == 0) "" else existing.val2LeadingPasses.toString()
                val2LeadingSuccesses.value = if (existing.val2LeadingSuccesses == 0) "" else existing.val2LeadingSuccesses.toString()
                val2MiddlePasses.value = if (existing.val2MiddlePasses == 0) "" else existing.val2MiddlePasses.toString()
                val2MiddleSuccesses.value = if (existing.val2MiddleSuccesses == 0) "" else existing.val2MiddleSuccesses.toString()
                val2TrailingPasses.value = if (existing.val2TrailingPasses == 0) "" else existing.val2TrailingPasses.toString()
                val2TrailingSuccesses.value = if (existing.val2TrailingSuccesses == 0) "" else existing.val2TrailingSuccesses.toString()
                
                validationTest3Description.value = existing.validationTest3Description
                val3LeadingPasses.value = if (existing.val3LeadingPasses == 0) "" else existing.val3LeadingPasses.toString()
                val3LeadingSuccesses.value = if (existing.val3LeadingSuccesses == 0) "" else existing.val3LeadingSuccesses.toString()
                val3MiddlePasses.value = if (existing.val3MiddlePasses == 0) "" else existing.val3MiddlePasses.toString()
                val3MiddleSuccesses.value = if (existing.val3MiddleSuccesses == 0) "" else existing.val3MiddleSuccesses.toString()
                val3TrailingPasses.value = if (existing.val3TrailingPasses == 0) "" else existing.val3TrailingPasses.toString()
                val3TrailingSuccesses.value = if (existing.val3TrailingSuccesses == 0) "" else existing.val3TrailingSuccesses.toString()
                
                // AS LEFT
                sensitivityAsLeftFerrous.value = existing.sensitivityAsLeftFerrous
                sampleCertAsLeftFerrous.value = existing.sampleCertAsLeftFerrous
                minSignalAsLeftFerrousLeading.value = existing.minSignalAsLeftFerrousLeading
                minSignalAsLeftFerrousMiddle.value = existing.minSignalAsLeftFerrousMiddle
                minSignalAsLeftFerrousTrailing.value = existing.minSignalAsLeftFerrousTrailing
                notesAsLeftFerrous.value = existing.notesAsLeftFerrous

                sensitivityAsLeftNonFerrous.value = existing.sensitivityAsLeftNonFerrous
                sampleCertAsLeftNonFerrous.value = existing.sampleCertAsLeftNonFerrous
                minSignalAsLeftNonFerrousLeading.value = existing.minSignalAsLeftNonFerrousLeading
                minSignalAsLeftNonFerrousMiddle.value = existing.minSignalAsLeftNonFerrousMiddle
                minSignalAsLeftNonFerrousTrailing.value = existing.minSignalAsLeftNonFerrousTrailing
                notesAsLeftNonFerrous.value = existing.notesAsLeftNonFerrous

                sensitivityAsLeftStainless.value = existing.sensitivityAsLeftStainless
                sampleCertAsLeftStainless.value = existing.sampleCertAsLeftStainless
                minSignalAsLeftStainlessLeading.value = existing.minSignalAsLeftStainlessLeading
                minSignalAsLeftStainlessMiddle.value = existing.minSignalAsLeftStainlessMiddle
                minSignalAsLeftStainlessTrailing.value = existing.minSignalAsLeftStainlessTrailing
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
                
                // Final
                systemComments.value = existing.systemComments
                productComments.value = existing.productComments
                packValidationPassed.value = existing.packValidationPassed
                customerName.value = existing.customerName
                lastLocation.value = existing.lastLocation
                newLocation.value = existing.newLocation
            } else {
                // Initialize with defaults or machine info
                customerName.value = system.customerName
                lastLocation.value = system.lastLocation
                newLocation.value = system.lastLocation

                // Initialize validation descriptions based on system type
                val isConveyor = system.systemTypeId == 1
                val target = if (isConveyor) 10 else 30
                
                if (system.systemType.contains("X-ray", ignoreCase = true)) {
                    validationTest1Description.value = "Stainless Steel"
                    validationTest2Description.value = "Foreign Body 1"
                    validationTest3Description.value = "Foreign Body 2"
                } else {
                    validationTest1Description.value = "Ferrous"
                    validationTest2Description.value = "Non-Ferrous"
                    validationTest3Description.value = "Stainless Steel"
                }

                // Set default pass targets
                val1LeadingPasses.value = target.toString()
                val1MiddlePasses.value = if (isConveyor) "10" else ""
                val1TrailingPasses.value = if (isConveyor) "10" else ""

                val2LeadingPasses.value = target.toString()
                val2MiddlePasses.value = if (isConveyor) "10" else ""
                val2TrailingPasses.value = if (isConveyor) "10" else ""

                val3LeadingPasses.value = target.toString()
                val3MiddlePasses.value = if (isConveyor) "10" else ""
                val3TrailingPasses.value = if (isConveyor) "10" else ""

                saveSov()
            }
            _isLoading.value = false
        }
    }

    fun saveSov() {
        viewModelScope.launch {
            val sov = SensitivityOptimisationValidationLocal(sovId).apply {
                // Metadata
                startDate = this@SensitivityOptimisationValidationViewModel.startDate.value
                endDate = this@SensitivityOptimisationValidationViewModel.endDate.value
                isSynced = this@SensitivityOptimisationValidationViewModel.isSynced.value
                mapVersion = this@SensitivityOptimisationValidationViewModel.mapVersion.value

                systemId = _systemId.value
                tempSystemId = _tempSystemId.value
                cloudSystemId = _cloudSystemId.value
                systemTypeId = system.systemTypeId
                modelId = system.modelId ?: 0
                serialNumber = system.serialNumber
                engineerId = this@SensitivityOptimisationValidationViewModel.engineerId
                customerId = system.customerId
                
                productDescription = this@SensitivityOptimisationValidationViewModel.productDescription.value
                productLibraryReference = this@SensitivityOptimisationValidationViewModel.productLibraryReference.value
                beltSpeed = this@SensitivityOptimisationValidationViewModel.beltSpeed.value
                productLength = this@SensitivityOptimisationValidationViewModel.productLength.value
                productWidth = this@SensitivityOptimisationValidationViewModel.productWidth.value
                productHeight = this@SensitivityOptimisationValidationViewModel.productHeight.value
                productWeight = this@SensitivityOptimisationValidationViewModel.productWeight.value
                
                // Labels
                detectionSetting1label = this@SensitivityOptimisationValidationViewModel.detectionSetting1label.value
                detectionSetting2label = this@SensitivityOptimisationValidationViewModel.detectionSetting2label.value
                detectionSetting3label = this@SensitivityOptimisationValidationViewModel.detectionSetting3label.value
                detectionSetting4label = this@SensitivityOptimisationValidationViewModel.detectionSetting4label.value
                detectionSetting5label = this@SensitivityOptimisationValidationViewModel.detectionSetting5label.value
                detectionSetting6label = this@SensitivityOptimisationValidationViewModel.detectionSetting6label.value
                detectionSetting7label = this@SensitivityOptimisationValidationViewModel.detectionSetting7label.value
                detectionSetting8label = this@SensitivityOptimisationValidationViewModel.detectionSetting8label.value
                
                // VALIDATION
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
                
                // AS LEFT
                sensitivityAsLeftFerrous = this@SensitivityOptimisationValidationViewModel.sensitivityAsLeftFerrous.value
                sampleCertAsLeftFerrous = this@SensitivityOptimisationValidationViewModel.sampleCertAsLeftFerrous.value
                minSignalAsLeftFerrousLeading = this@SensitivityOptimisationValidationViewModel.minSignalAsLeftFerrousLeading.value
                minSignalAsLeftFerrousMiddle = this@SensitivityOptimisationValidationViewModel.minSignalAsLeftFerrousMiddle.value
                minSignalAsLeftFerrousTrailing = this@SensitivityOptimisationValidationViewModel.minSignalAsLeftFerrousTrailing.value
                notesAsLeftFerrous = this@SensitivityOptimisationValidationViewModel.notesAsLeftFerrous.value

                sensitivityAsLeftNonFerrous = this@SensitivityOptimisationValidationViewModel.sensitivityAsLeftNonFerrous.value
                sampleCertAsLeftNonFerrous = this@SensitivityOptimisationValidationViewModel.sampleCertAsLeftNonFerrous.value
                minSignalAsLeftNonFerrousLeading = this@SensitivityOptimisationValidationViewModel.minSignalAsLeftNonFerrousLeading.value
                minSignalAsLeftNonFerrousMiddle = this@SensitivityOptimisationValidationViewModel.minSignalAsLeftNonFerrousMiddle.value
                minSignalAsLeftNonFerrousTrailing = this@SensitivityOptimisationValidationViewModel.minSignalAsLeftNonFerrousTrailing.value
                notesAsLeftNonFerrous = this@SensitivityOptimisationValidationViewModel.notesAsLeftNonFerrous.value

                sensitivityAsLeftStainless = this@SensitivityOptimisationValidationViewModel.sensitivityAsLeftStainless.value
                sampleCertAsLeftStainless = this@SensitivityOptimisationValidationViewModel.sampleCertAsLeftStainless.value
                minSignalAsLeftStainlessLeading = this@SensitivityOptimisationValidationViewModel.minSignalAsLeftStainlessLeading.value
                minSignalAsLeftStainlessMiddle = this@SensitivityOptimisationValidationViewModel.minSignalAsLeftStainlessMiddle.value
                minSignalAsLeftStainlessTrailing = this@SensitivityOptimisationValidationViewModel.minSignalAsLeftStainlessTrailing.value
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
                
                // Final
                systemComments = this@SensitivityOptimisationValidationViewModel.systemComments.value
                productComments = this@SensitivityOptimisationValidationViewModel.productComments.value
                packValidationPassed = this@SensitivityOptimisationValidationViewModel.packValidationPassed.value
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

    // Ferrous As Left

    fun enableFerrousAsLeft() {
        sampleCertAsLeftFerrous.value = ""
        minSignalAsLeftFerrousLeading.value = ""
        minSignalAsLeftFerrousMiddle.value = ""
        minSignalAsLeftFerrousTrailing.value = ""
    }

    fun disableFerrousAsLeft() {
        sampleCertAsLeftFerrous.value = "N/A"
        minSignalAsLeftFerrousLeading.value = "N/A"
        minSignalAsLeftFerrousMiddle.value = "N/A"
        minSignalAsLeftFerrousTrailing.value = "N/A"
        val1LeadingSuccesses.value = ""
        val1MiddleSuccesses.value = ""
        val1TrailingSuccesses.value = ""
    }

    // Non-Ferrous As Left

    fun enableNonFerrousAsLeft() {
        sampleCertAsLeftNonFerrous.value = ""
        minSignalAsLeftNonFerrousLeading.value = ""
        minSignalAsLeftNonFerrousMiddle.value = ""
        minSignalAsLeftNonFerrousTrailing.value = ""
    }

    fun disableNonFerrousAsLeft() {
        sampleCertAsLeftNonFerrous.value = "N/A"
        minSignalAsLeftNonFerrousLeading.value = "N/A"
        minSignalAsLeftNonFerrousMiddle.value = "N/A"
        minSignalAsLeftNonFerrousTrailing.value = "N/A"
        val2LeadingSuccesses.value = ""
        val2MiddleSuccesses.value = ""
        val2TrailingSuccesses.value = ""
    }

    // Stainless As Left

    fun enableStainlessAsLeft() {
        sampleCertAsLeftStainless.value = ""
        minSignalAsLeftStainlessLeading.value = ""
        minSignalAsLeftStainlessMiddle.value = ""
        minSignalAsLeftStainlessTrailing.value = ""
    }

    fun disableStainlessAsLeft() {
        sampleCertAsLeftStainless.value = "N/A"
        minSignalAsLeftStainlessLeading.value = "N/A"
        minSignalAsLeftStainlessMiddle.value = "N/A"
        minSignalAsLeftStainlessTrailing.value = "N/A"
        val3LeadingSuccesses.value = ""
        val3MiddleSuccesses.value = ""
        val3TrailingSuccesses.value = ""
    }

    suspend fun finaliseAndUpload(
        context: android.content.Context,
        apiService: com.snb.inspect.ApiService,
        onResult: (String) -> Unit
    ) {
        try {
            _isUploading.value = true
            
            // 1. Update system location locally
            updateSystemLocationLocally()
            
            // 2. Sync system with cloud to push the new location
            mdSystemsRepository.updateSystem(
                context = context,
                cloudId = _cloudSystemId.value,
                localId = _systemId.value,
                tempId = _tempSystemId.value
            )

            // 2.5 Refresh the cloudSystemId in the SOV record to ensure sync
            val updatedSystem = mdSystemsRepository.getMetalDetectorsWithFullDetailsUsingLocalId(_systemId.value).firstOrNull()
            val newCloudId = updatedSystem?.cloudId ?: 0
            if (newCloudId != 0) {
                InAppLogger.d("Updating SOV with new cloudSystemId: $newCloudId")
                repository.updateCloudIdBySovId(sovId, newCloudId)
                _cloudSystemId.value = newCloudId
            }

            // 3. Save SOV and set end date
            saveSov() // Ensure all current state is saved
            
            val existing = repository.getById(sovId)
            if (existing != null) {
                existing.endDate = LocalDateTime.now().toString()
                repository.insertOrUpdate(existing)
            }

            // 4. Upload the SOV CSV
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
