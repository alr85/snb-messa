package com.example.mecca.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mecca.ApiService
import com.example.mecca.daos.CustomerDAO
import com.example.mecca.daos.MdModelsDAO
import com.example.mecca.daos.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.daos.MetalDetectorSystemsDAO
import com.example.mecca.daos.SystemTypeDAO
import com.example.mecca.dataClasses.MetalDetectorWithFullDetails
import com.example.mecca.repositories.MetalDetectorConveyorCalibrationRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.repositories.RetailerSensitivitiesRepository

class CalibrationMetalDetectorConveyorViewModelFactory(
    private val calibrationDao: MetalDetectorConveyorCalibrationDAO,
    private val repository: MetalDetectorSystemsRepository,
    private val mdModelsDAO: MdModelsDAO,
    private val mdSystemsDAO: MetalDetectorSystemsDAO,
    private val systemTypeDao: SystemTypeDAO,
    private val retailerSensitivitiesRepo: RetailerSensitivitiesRepository,
    private val calibrationRepository: MetalDetectorConveyorCalibrationRepository,
    private val customersDao: CustomerDAO,
    private val apiService: ApiService,
    private val calibrationId: String,
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

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalibrationMetalDetectorConveyorViewModel::class.java)) {
            return CalibrationMetalDetectorConveyorViewModel(
                engineerId = engineerId,
                calibrationDao = calibrationDao,
                calibrationRepository = calibrationRepository,
                mdModelsDAO = mdModelsDAO,
                mdSystemsDAO = mdSystemsDAO,
                customerDAO = customersDao,
                systemTypeDAO = systemTypeDao,
                repository = repository,
                calibrationId = calibrationId,
                system = system,
                detectionSetting1label = detectionSetting1label,
                detectionSetting2label = detectionSetting2label,
                detectionSetting3label = detectionSetting3label,
                detectionSetting4label = detectionSetting4label,
                detectionSetting5label = detectionSetting5label,
                detectionSetting6label = detectionSetting6label,
                detectionSetting7label = detectionSetting7label,
                detectionSetting8label = detectionSetting8label,
                apiService = apiService,
                retailerSensitivitiesRepo = retailerSensitivitiesRepo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

