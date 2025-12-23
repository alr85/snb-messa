package com.example.mecca.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.compose.rememberNavController
import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.RetrofitClient
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModelFactory
import com.example.mecca.repositories.MetalDetectorConveyorCalibrationRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.repositories.RetailerSensitivitiesRepository
import com.example.mecca.screens.metaldetectorcalibration.MetalDetectorConveyorCalibrationScreenWrapper
import com.example.mecca.ui.theme.MyAppTheme


class MetalDetectorConveyorCalibrationActivity : ComponentActivity() {

    // Retrieve calibrationId when the activity starts
    private lateinit var calibrationId: String
    private var customerId: Int = 0
    private var systemId: Int = 0
    private var cloudSystemId: Int = 0
    private var tempSystemId: Int = 0
    private lateinit var serialNumber: String
    private lateinit var modelDescription: String
    private lateinit var customerName: String
    private var modelId: Int = 0
    private var engineerId: Int = 0 // New property for engineerId
    private var detectionSetting1label: String = ""
    private var detectionSetting2label: String = ""
    private var detectionSetting3label: String = ""
    private var detectionSetting4label: String = ""
    private var detectionSetting5label: String = ""
    private var detectionSetting6label: String = ""

    private var detectionSetting7label: String = ""
    private var detectionSetting8label: String = ""
    private var lastLocation: String = ""


    // Initialize the database and DAO using the AppDatabase instance
    private val calibrationDao by lazy {
        AppDatabase.getDatabase(applicationContext).metalDetectorConveyorCalibrationDAO()
    }

    private val mdModelsDAO by lazy {
        AppDatabase.getDatabase(applicationContext).mdModelDao()
    }

    private val mdSystemsDAO by lazy {
        AppDatabase.getDatabase(applicationContext).mdSystemDAO()
    }

    private val customerDAO by lazy {
        AppDatabase.getDatabase(applicationContext).customerDao()
    }

    val apiService: ApiService by lazy {
        RetrofitClient.instance
    }

    private val repository by lazy {
        MetalDetectorSystemsRepository(apiService, AppDatabase.getDatabase(applicationContext))
    }

    private val retailerSensitivitiesRepo by lazy {
        RetailerSensitivitiesRepository(apiService, AppDatabase.getDatabase(applicationContext))
    }

    private val calibrationRepository by lazy {
        MetalDetectorConveyorCalibrationRepository(calibrationDao)
    }







    // Scoping the ViewModel to the activity
    private val calibrationViewModel: CalibrationMetalDetectorConveyorViewModel by viewModels {
        CalibrationMetalDetectorConveyorViewModelFactory(
            calibrationDao = calibrationDao,
            repository = repository,
            mdModelsDAO,
            mdSystemsDAO,
            apiService,
            calibrationId,
            customerId,
            systemId,
            tempSystemId,
            cloudSystemId,
            serialNumber,
            modelDescription,
            customerName,
            modelId,
            engineerId,
            customerDAO,
            detectionSetting1label,
            detectionSetting2label,
            detectionSetting3label,
            detectionSetting4label,
            detectionSetting5label,
            detectionSetting6label,
            detectionSetting7label,
            detectionSetting8label,
            lastLocation,
            retailerSensitivitiesRepo,
            calibrationRepository
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve calibrationId from the intent extras
        calibrationId = intent.getStringExtra("CALIBRATION_ID") ?: ""
        customerId = intent.getIntExtra("CUSTOMER_ID", 0)
        systemId = intent.getIntExtra("SYSTEM_ID", 0)
        cloudSystemId = intent.getIntExtra("CLOUD_SYSTEM_ID", 0)
        tempSystemId = intent.getIntExtra("TEMP_SYSTEM_ID", 0)
        serialNumber = intent.getStringExtra("SERIAL_NUMBER") ?: ""
        modelDescription = intent.getStringExtra("MODEL_DESCRIPTION") ?: ""
        customerName = intent.getStringExtra("CUSTOMER_NAME") ?: ""
        modelId = intent.getIntExtra("MODEL_ID", 0)
        engineerId = intent.getIntExtra("ENGINEER_ID", 0) // Retrieve the engineerId
        detectionSetting1label = intent.getStringExtra("DETECTION_SETTING_1_LABEL") ?: ""
        detectionSetting2label = intent.getStringExtra("DETECTION_SETTING_2_LABEL") ?: ""
        detectionSetting3label = intent.getStringExtra("DETECTION_SETTING_3_LABEL") ?: ""
        detectionSetting4label = intent.getStringExtra("DETECTION_SETTING_4_LABEL") ?: ""
        detectionSetting5label = intent.getStringExtra("DETECTION_SETTING_5_LABEL") ?: ""
        detectionSetting6label = intent.getStringExtra("DETECTION_SETTING_6_LABEL") ?: ""
        detectionSetting7label = intent.getStringExtra("DETECTION_SETTING_7_LABEL") ?: ""
        detectionSetting8label = intent.getStringExtra("DETECTION_SETTING_8_LABEL") ?: ""
        lastLocation = intent.getStringExtra("LAST_LOCATION") ?: ""


// Set up the UI


        setContent {
            MyAppTheme {
                // Unique NavController for this activity
                val navController = rememberNavController()

                // Calibration-specific navigation graph
                MetalDetectorConveyorCalibrationScreenWrapper(
                    navController = navController,
                    viewModel = calibrationViewModel,
                    calibrationId = calibrationId,
                    apiService = apiService
                )

            }
        }
    }
}