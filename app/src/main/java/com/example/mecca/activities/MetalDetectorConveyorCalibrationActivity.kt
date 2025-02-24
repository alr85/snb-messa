package com.example.mecca.activities

import MyAppTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.mecca.ApiService
import com.example.mecca.AppDatabase
import com.example.mecca.CalibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.CalibrationViewModels.CalibrationMetalDetectorConveyorViewModelFactory
import com.example.mecca.RetrofitClient
import com.example.mecca.ui.theme.MetalDetectorConveyorCalibrationNavGraph

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


    // Initialize the database and DAO using the AppDatabase instance
    private val calibrationDao by lazy {
        AppDatabase.getDatabase(applicationContext).metalDetectorConveyorCalibrationDAO()
    }

    private val mdModelsDAO by lazy {
        AppDatabase.getDatabase(applicationContext).mdModelDao()
    }

    private val customerDAO by lazy {
        AppDatabase.getDatabase(applicationContext).customerDao()
    }

    val apiService: ApiService by lazy {
        RetrofitClient.instance
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Do nothing, effectively disabling the back button
    }

    // Scoping the ViewModel to the activity
    private val calibrationViewModel: CalibrationMetalDetectorConveyorViewModel by viewModels {
        CalibrationMetalDetectorConveyorViewModelFactory(
            calibrationDao,
            mdModelsDAO,
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
            detectionSetting6label
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve calibrationId from the intent extras
        calibrationId = intent.getStringExtra("CALIBRATION_ID") ?: ""
        customerId = intent.getIntExtra("CUSTOMER_ID",0)
        systemId = intent.getIntExtra("SYSTEM_ID",0)
        cloudSystemId = intent.getIntExtra("CLOUD_SYSTEM_ID",0)
        tempSystemId = intent.getIntExtra("TEMP_SYSTEM_ID", 0)
        serialNumber = intent.getStringExtra("SERIAL_NUMBER") ?: ""
        modelDescription = intent.getStringExtra("MODEL_DESCRIPTION") ?: ""
        customerName = intent.getStringExtra("CUSTOMER_NAME") ?: ""
        modelId = intent.getIntExtra("MODEL_ID",0)
        engineerId = intent.getIntExtra("ENGINEER_ID", 0) // Retrieve the engineerId
        detectionSetting1label = intent.getStringExtra("DETECTION_SETTING_1_LABEL") ?: ""
        detectionSetting2label = intent.getStringExtra("DETECTION_SETTING_2_LABEL") ?: ""
        detectionSetting3label = intent.getStringExtra("DETECTION_SETTING_3_LABEL") ?: ""
        detectionSetting4label = intent.getStringExtra("DETECTION_SETTING_4_LABEL") ?: ""
        detectionSetting5label = intent.getStringExtra("DETECTION_SETTING_5_LABEL") ?: ""
        detectionSetting6label = intent.getStringExtra("DETECTION_SETTING_6_LABEL") ?: ""




        setContent {
            MyAppTheme {
                // Unique NavController for this activity
                val navController = rememberNavController()

                // Calibration-specific navigation graph
                MetalDetectorConveyorCalibrationNavGraph(
                    navController = navController,
                    calibrationViewModel = calibrationViewModel,
                    calibrationId = calibrationId,
                    apiService = apiService
                )
            }
        }
    }
}
