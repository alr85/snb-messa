package com.snb.inspect.activities

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
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModelFactory
import com.snb.inspect.dataClasses.MetalDetectorWithFullDetails
import com.snb.inspect.repositories.MeasuringEquipmentRepository
import com.snb.inspect.repositories.MetalDetectorConveyorCalibrationRepository
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.repositories.RetailerSensitivitiesRepository
import com.snb.inspect.screens.service.mdCalibration.MetalDetectorConveyorCalibrationScreenWrapper
import com.snb.inspect.ui.theme.MyAppTheme

class MetalDetectorConveyorCalibrationActivity : ComponentActivity() {

    // DB / DAO
    private val db by lazy { AppDatabase.getDatabase(applicationContext) }

    private val calibrationDao by lazy { db.metalDetectorConveyorCalibrationDAO() }
    private val mdModelsDAO by lazy { db.mdModelDao() }
    private val mdSystemsDAO by lazy { db.mdSystemDAO() }
    private val systemTypeDAO by lazy { db.systemTypeDAO() }
    private val customerDAO by lazy { db.customerDao() }
    private val measuringEquipmentDAO by lazy { db.measuringEquipmentDAO() }

    // API / repos
    private val apiService: ApiService by lazy { RetrofitClient.instance }

    private val systemsRepository by lazy {
        MetalDetectorSystemsRepository(apiService, db)
    }

    private val retailerSensitivitiesRepo by lazy {
        RetailerSensitivitiesRepository(apiService, db)
    }

    private val calibrationRepository by lazy {
        MetalDetectorConveyorCalibrationRepository(calibrationDao)
    }

    private val measuringEquipmentRepository by lazy {
        MeasuringEquipmentRepository(apiService, db)
    }

    private lateinit var calibrationViewModel: CalibrationMetalDetectorConveyorViewModel

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Required extras ---
        val calibrationId = intent.getStringExtra("CALIBRATION_ID")
            ?: error("Missing CALIBRATION_ID")

        val engineerId = intent.getIntExtra("ENGINEER_ID", 0)

        @Suppress("DEPRECATION")
        val system = intent.getParcelableExtra<MetalDetectorWithFullDetails>("SYSTEM_FULL_DETAILS")
            ?: error("Missing SYSTEM_FULL_DETAILS")

        // --- Optional extras (labels) ---
        val detectionSetting1label = intent.getStringExtra("DETECTION_SETTING_1_LABEL") ?: ""
        val detectionSetting2label = intent.getStringExtra("DETECTION_SETTING_2_LABEL") ?: ""
        val detectionSetting3label = intent.getStringExtra("DETECTION_SETTING_3_LABEL") ?: ""
        val detectionSetting4label = intent.getStringExtra("DETECTION_SETTING_4_LABEL") ?: ""
        val detectionSetting5label = intent.getStringExtra("DETECTION_SETTING_5_LABEL") ?: ""
        val detectionSetting6label = intent.getStringExtra("DETECTION_SETTING_6_LABEL") ?: ""
        val detectionSetting7label = intent.getStringExtra("DETECTION_SETTING_7_LABEL") ?: ""
        val detectionSetting8label = intent.getStringExtra("DETECTION_SETTING_8_LABEL") ?: ""

        // --- Build VM factory AFTER reading extras ---
        val factory = CalibrationMetalDetectorConveyorViewModelFactory(
            calibrationDao = calibrationDao,
            repository = systemsRepository,
            mdModelsDAO = mdModelsDAO,
            mdSystemsDAO = mdSystemsDAO,
            systemTypeDao = systemTypeDAO,
            retailerSensitivitiesRepo = retailerSensitivitiesRepo,
            calibrationRepository = calibrationRepository,
            customersDao = customerDAO,
            measuringEquipmentRepository = measuringEquipmentRepository,
            measuringEquipmentDAO = measuringEquipmentDAO,
            apiService = apiService,
            calibrationId = calibrationId,
            system = system,
            engineerId = engineerId,
            detectionSetting1label = detectionSetting1label,
            detectionSetting2label = detectionSetting2label,
            detectionSetting3label = detectionSetting3label,
            detectionSetting4label = detectionSetting4label,
            detectionSetting5label = detectionSetting5label,
            detectionSetting6label = detectionSetting6label,
            detectionSetting7label = detectionSetting7label,
            detectionSetting8label = detectionSetting8label
        )

        calibrationViewModel = ViewModelProvider(this, factory)
            .get(CalibrationMetalDetectorConveyorViewModel::class.java)

        // --- UI ---
        setContent {
            MyAppTheme {
                val navController = rememberNavController()
                val windowSizeClass = calculateWindowSizeClass(this)

                MetalDetectorConveyorCalibrationScreenWrapper(
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
