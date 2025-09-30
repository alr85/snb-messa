package com.example.mecca.CalibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mecca.DAOs.CustomerDAO
import com.example.mecca.DAOs.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.MdModelsDAO

class CalibrationMetalDetectorConveyorViewModelFactory(
    private val calibrationDao: MetalDetectorConveyorCalibrationDAO,
    private val mdModelsDAO: MdModelsDAO,
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
    private val detectionSetting8label: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalibrationMetalDetectorConveyorViewModel::class.java)) {
            return CalibrationMetalDetectorConveyorViewModel(
                engineerId = engineerId,
                calibrationDao = calibrationDao,
                mdModelsDAO = mdModelsDAO,
                calibrationId = calibrationId,
                customerId = customerId,
                systemId = systemId,
                cloudSystemId = cloudSystemId,
                tempSystemId = tempSystemId,
                serialNumber = serialNumber,
                modelDescription = modelDescription,
                customerName = customerName,
                modelId = modelId,
                customerDAO = customersDao,
                detectionSetting1label = detectionSetting1label,
                detectionSetting2label = detectionSetting2label,
                detectionSetting3label = detectionSetting3label,
                detectionSetting4label = detectionSetting4label,
                detectionSetting5label = detectionSetting5label,
                detectionSetting6label = detectionSetting6label,
                detectionSetting7label = detectionSetting7label,
                detectionSetting8label = detectionSetting8label
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

