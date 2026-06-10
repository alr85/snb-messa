package com.snb.inspect.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.snb.inspect.repositories.SensitivityOptimisationValidationRepository
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.dataClasses.MetalDetectorWithFullDetails

class SensitivityOptimisationValidationViewModelFactory(
    private val repository: SensitivityOptimisationValidationRepository,
    private val mdSystemsRepository: MetalDetectorSystemsRepository,
    private val sovId: String,
    private val system: MetalDetectorWithFullDetails,
    private val engineerId: Int,
    private val detectionSetting1label: String,
    private val detectionSetting2label: String,
    private val detectionSetting3label: String,
    private val detectionSetting4label: String,
    private val detectionSetting5label: String,
    private val detectionSetting6label: String,
    private val detectionSetting7label: String,
    private val detectionSetting8label: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensitivityOptimisationValidationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SensitivityOptimisationValidationViewModel(
                repository, mdSystemsRepository, sovId, system, engineerId,
                detectionSetting1label, detectionSetting2label, detectionSetting3label, detectionSetting4label,
                detectionSetting5label, detectionSetting6label, detectionSetting7label, detectionSetting8label
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
