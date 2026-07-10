package com.snb.inspect.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.snb.inspect.daos.CheckweigherCalibrationDAO
import com.snb.inspect.daos.CwSystemsDAO
import com.snb.inspect.daos.MeasuringEquipmentDAO
import com.snb.inspect.dataClasses.CheckweigherWithFullDetails
import com.snb.inspect.repositories.CheckweigherCalibrationRepository
import com.snb.inspect.repositories.CheckweigherSystemsRepository

class CalibrationCheckweigherViewModelFactory(
    private val engineerId: Int,
    private val calibrationDao: CheckweigherCalibrationDAO,
    private val calibrationRepository: CheckweigherCalibrationRepository,
    private val measuringEquipmentDAO: MeasuringEquipmentDAO,
    private val systemsRepository: CheckweigherSystemsRepository,
    private val cwSystemsDAO: CwSystemsDAO,
    private val calibrationId: String,
    private val system: CheckweigherWithFullDetails
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalibrationCheckweigherViewModel::class.java)) {
            return CalibrationCheckweigherViewModel(
                engineerId = engineerId,
                calibrationDao = calibrationDao,
                calibrationRepository = calibrationRepository,
                measuringEquipmentDAO = measuringEquipmentDAO,
                systemsRepository = systemsRepository,
                cwSystemsDAO = cwSystemsDAO,
                calibrationIdString = calibrationId,
                system = system
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
