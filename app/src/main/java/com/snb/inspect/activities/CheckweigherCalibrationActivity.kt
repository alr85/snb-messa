package com.snb.inspect.activities

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.snb.inspect.ApiService
import com.snb.inspect.AppDatabase
import com.snb.inspect.RetrofitClient
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModelFactory
import com.snb.inspect.dataClasses.CheckweigherWithFullDetails
import com.snb.inspect.repositories.CheckweigherCalibrationRepository
import com.snb.inspect.repositories.CheckweigherSystemsRepository
import com.snb.inspect.screens.service.cwCalibration.CheckweigherCalibrationScreenWrapper
import com.snb.inspect.ui.theme.MyAppTheme

class CheckweigherCalibrationActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getDatabase(applicationContext) }
    private val calibrationDao by lazy { db.checkweigherCalibrationDAO() }
    private val measuringEquipmentDAO by lazy { db.measuringEquipmentDAO() }
    private val cwSystemsDAO by lazy { db.cwSystemsDAO() }
    private val apiService: ApiService by lazy { RetrofitClient.instance }

    private val calibrationRepository by lazy {
        CheckweigherCalibrationRepository(calibrationDao)
    }

    private val systemsRepository by lazy {
        CheckweigherSystemsRepository(apiService, db)
    }

    private lateinit var calibrationViewModel: CalibrationCheckweigherViewModel

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val calibrationId = intent.getStringExtra("CALIBRATION_ID") ?: error("Missing CALIBRATION_ID")
        val engineerId = intent.getIntExtra("ENGINEER_ID", 0)

        @Suppress("DEPRECATION")
        val system = intent.getParcelableExtra<CheckweigherWithFullDetails>("SYSTEM_FULL_DETAILS")
            ?: error("Missing SYSTEM_FULL_DETAILS")

        val factory = CalibrationCheckweigherViewModelFactory(
            engineerId = engineerId,
            calibrationDao = calibrationDao,
            calibrationRepository = calibrationRepository,
            measuringEquipmentDAO = measuringEquipmentDAO,
            systemsRepository = systemsRepository,
            cwSystemsDAO = cwSystemsDAO,
            calibrationId = calibrationId,
            system = system
        )

        calibrationViewModel = ViewModelProvider(this, factory).get(CalibrationCheckweigherViewModel::class.java)

        setContent {
            MyAppTheme {
                val navController = rememberNavController()
                val windowSizeClass = calculateWindowSizeClass(this)

                CheckweigherCalibrationScreenWrapper(
                    navController = navController,
                    viewModel = calibrationViewModel,
                    calibrationId = calibrationId,
                    apiService = apiService,
                    windowSizeClass = windowSizeClass
                )
            }
        }
    }
}
