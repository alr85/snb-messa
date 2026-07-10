package com.snb.inspect.calibrationViewModels

import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow

interface ICalibrationViewModel {
    val calibrationId: State<String>
    val serialNumber: State<String>
    val modelDescription: State<String>

    val screenValidities: StateFlow<Map<String, Boolean>>
    fun setScreenValidity(route: String, isValid: Boolean)
    fun isCalibrationValid(routeOrder: List<String>): Boolean
    fun getDisplayNameForRoute(route: String): String
    fun persistCurrentScreen(route: String)
    
    // Add other common properties if needed by shared UI components
    val currentScreenNextEnabled: StateFlow<Boolean>
    fun setCurrentScreenNextEnabled(enabled: Boolean)
    fun clearCalibrationData()
    fun deleteCalibration(id: String)
    fun shouldSkipToSummary(): Boolean
}
