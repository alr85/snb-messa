package com.snb.inspect.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.snb.inspect.ApiService
import com.snb.inspect.daos.CustomerDAO
import com.snb.inspect.daos.MdModelsDAO
import com.snb.inspect.daos.MeasuringEquipmentDAO
import com.snb.inspect.daos.MetalDetectorConveyorCalibrationDAO
import com.snb.inspect.daos.MetalDetectorSystemsDAO
import com.snb.inspect.daos.SystemTypeDAO
import com.snb.inspect.dataClasses.MetalDetectorWithFullDetails
import com.snb.inspect.repositories.MeasuringEquipmentRepository
import com.snb.inspect.repositories.MetalDetectorConveyorCalibrationRepository
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.repositories.RetailerSensitivitiesRepository

class CalibrationMetalDetectorConveyorViewModelFactory(
    private val calibrationDao: MetalDetectorConveyorCalibrationDAO,
    private val repository: MetalDetectorSystemsRepository,
    private val mdModelsDAO: MdModelsDAO,
    private val mdSystemsDAO: MetalDetectorSystemsDAO,
    private val systemTypeDao: SystemTypeDAO,
    private val retailerSensitivitiesRepo: RetailerSensitivitiesRepository,
    private val calibrationRepository: MetalDetectorConveyorCalibrationRepository,
    private val customersDao: CustomerDAO,
    private val measuringEquipmentRepository: MeasuringEquipmentRepository,
    private val measuringEquipmentDAO: MeasuringEquipmentDAO,
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
                measuringEquipmentRepository = measuringEquipmentRepository,
                measuringEquipmentDAO = measuringEquipmentDAO,
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

