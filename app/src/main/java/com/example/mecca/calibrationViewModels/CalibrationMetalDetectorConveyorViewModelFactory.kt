package com.example.mecca.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mecca.ApiService
import com.example.mecca.DAOs.CustomerDAO
import com.example.mecca.DAOs.MdModelsDAO
import com.example.mecca.DAOs.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.DAOs.MetalDetectorSystemsDAO
import com.example.mecca.repositories.MetalDetectorConveyorCalibrationRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.repositories.RetailerSensitivitiesRepository

class CalibrationMetalDetectorConveyorViewModelFactory(
    private val calibrationDao: MetalDetectorConveyorCalibrationDAO,
    private val repository: MetalDetectorSystemsRepository,
    private val mdModelsDAO: MdModelsDAO,
    private val mdSystemsDAO: MetalDetectorSystemsDAO,
    private val apiService: ApiService,
    private val calibrationId: String,
    private val customerId: Int,
    private val systemId: Int,
    private val tempSystemId: Int,
    private val cloudSystemId: Int,
    private val serialNumber: String,
    private val modelDescription: String,
    private val customerName: String,
    private val modelId: Int,
    private val engineerId: Int, // Add engineerId here,
    private val customersDao: CustomerDAO,
    private val detectionSetting1label: String,
    private val detectionSetting2label: String,
    private val detectionSetting3label: String,
    private val detectionSetting4label: String,
    private val detectionSetting5label: String,
    private val detectionSetting6label: String,
    private val detectionSetting7label: String,
    private val detectionSetting8label: String,
    private val lastLocation: String,
    private val retailerSensitivitiesRepo: RetailerSensitivitiesRepository,
    private val calibrationRepository: MetalDetectorConveyorCalibrationRepository,

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
                repository = repository,
                calibrationId = calibrationId,
                customerId = customerId,
                systemId = systemId,
                cloudSystemId = cloudSystemId,
                tempSystemId = tempSystemId,
                serialNumber = serialNumber,
                modelDescription = modelDescription,
                customerName = customerName,
                modelId = modelId,
                detectionSetting1label = detectionSetting1label,
                detectionSetting2label = detectionSetting2label,
                detectionSetting3label = detectionSetting3label,
                detectionSetting4label = detectionSetting4label,
                detectionSetting5label = detectionSetting5label,
                detectionSetting6label = detectionSetting6label,
                detectionSetting7label = detectionSetting7label,
                detectionSetting8label = detectionSetting8label,
                lastLocation = lastLocation,
                apiService = apiService,
                retailerSensitivitiesRepo = retailerSensitivitiesRepo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

