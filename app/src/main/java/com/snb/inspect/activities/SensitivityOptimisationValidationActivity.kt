package com.snb.inspect.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.navigation.compose.rememberNavController
import com.snb.inspect.AppDatabase
import com.snb.inspect.MyApplication
import com.snb.inspect.ApiService
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModelFactory
import com.snb.inspect.dataClasses.MetalDetectorWithFullDetails
import com.snb.inspect.repositories.SensitivityOptimisationValidationRepository
import com.snb.inspect.screens.service.sov.SovScreenWrapper
import com.snb.inspect.ui.theme.MyAppTheme

class SensitivityOptimisationValidationActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getDatabase(applicationContext) }
    private val sovDao by lazy { db.sensitivityOptimisationValidationDAO() }
    private val mdSystemDAO by lazy { db.mdSystemDAO() }
    private val repository by lazy { SensitivityOptimisationValidationRepository(sovDao, mdSystemDAO) }
    private val apiService by lazy { (application as MyApplication).apiService }

    private lateinit var viewModel: SensitivityOptimisationValidationViewModel

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sovId = intent.getStringExtra("SOV_ID") ?: error("Missing SOV_ID")
        val engineerId = intent.getIntExtra("ENGINEER_ID", 0)
        @Suppress("DEPRECATION")
        val system = intent.getParcelableExtra<MetalDetectorWithFullDetails>("SYSTEM_FULL_DETAILS")
            ?: error("Missing SYSTEM_FULL_DETAILS")

        // Labels
        val detectionSetting1label = intent.getStringExtra("DETECTION_SETTING_1_LABEL") ?: ""
        val detectionSetting2label = intent.getStringExtra("DETECTION_SETTING_2_LABEL") ?: ""
        val detectionSetting3label = intent.getStringExtra("DETECTION_SETTING_3_LABEL") ?: ""
        val detectionSetting4label = intent.getStringExtra("DETECTION_SETTING_4_LABEL") ?: ""
        val detectionSetting5label = intent.getStringExtra("DETECTION_SETTING_5_LABEL") ?: ""
        val detectionSetting6label = intent.getStringExtra("DETECTION_SETTING_6_LABEL") ?: ""
        val detectionSetting7label = intent.getStringExtra("DETECTION_SETTING_7_LABEL") ?: ""
        val detectionSetting8label = intent.getStringExtra("DETECTION_SETTING_8_LABEL") ?: ""

        val factory = SensitivityOptimisationValidationViewModelFactory(
            repository = repository,
            sovId = sovId,
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

        viewModel = ViewModelProvider(this, factory).get(SensitivityOptimisationValidationViewModel::class.java)

        setContent {
            MyAppTheme {
                val navController = rememberNavController()
                val windowSizeClass = calculateWindowSizeClass(this)
                
                SovScreenWrapper(
                    navController = navController,
                    viewModel = viewModel,
                    windowSizeClass = windowSizeClass,
                    apiService = apiService
                )
            }
        }
    }
}
