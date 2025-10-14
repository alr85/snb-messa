package com.example.mecca.ui.theme

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mecca.ApiService
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorAirPressureSensor
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorBackupPEC
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorBinDoorMonitor
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorBinFullPEC
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorCalibrationStart
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorComplianceConfirmation
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorConveyorDetails
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorDetectNotification
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorDetectionSettingsAsFound
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorDetectionSettingsAsLeft
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorFerrousTest
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorIndicators
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorInfeedPEC
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorLargeMetalTest
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorNonFerrousTest
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorPackCheckSensor
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorProductDetails
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorRejectConfirmPEC
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorRejectSettings
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorSensitivityAsFound
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorSensitivityRequirements
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorSmeDetails
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorSpeedSensor
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorStainlessTest
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorSummary
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorSystemChecklist

@Composable
fun MetalDetectorConveyorCalibrationNavGraph(
    navController: NavHostController,
    calibrationViewModel: CalibrationMetalDetectorConveyorViewModel,
    calibrationId: String, // pass calibrationId as parameter
    apiService: ApiService
) {
    NavHost(
        navController = navController,
        startDestination = "MetalDetectorConveyorCalibrationStart/$calibrationId"

    ) {
        composable(
            route = "MetalDetectorConveyorCalibrationStart/{calibrationId}",
            arguments = listOf(
                navArgument("calibrationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val calId = backStackEntry.arguments?.getString("calibrationId") ?: ""
            Log.d("CalibrationNavGraph", "Starting Calibration ID: $calId")

            CalMetalDetectorConveyorCalibrationStart(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable(
            "CalMetalDetectorConveyorSensitivityRequirements",)
        {
            CalMetalDetectorConveyorSensitivityRequirements(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        // Additional screens in the calibration process...
        composable("CalMetalDetectorConveyorProductDetails") {
            CalMetalDetectorConveyorProductDetails(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable(
            "CalMetalDetectorConveyorDetectionSettingsAsFound",)
        {
            CalMetalDetectorConveyorDetectionSettingsAsFound(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable(
            "CalMetalDetectorConveyorSensitivityAsFound",)
        {
            CalMetalDetectorConveyorSensitivityAsFound(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorFerrousTest") {
            CalMetalDetectorConveyorFerrousTest(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorNonFerrousTest") {
            CalMetalDetectorConveyorNonFerrousTest(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorStainlessTest") {
            CalMetalDetectorConveyorStainlessTest(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorLargeMetalTest") {
            CalMetalDetectorConveyorLargeMetalTest(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorDetectionSettingsAsLeft") {
            CalMetalDetectorConveyorDetectionSettingsAsLeft(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorRejectSettings") {
            CalMetalDetectorConveyorRejectSettings(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorSystemChecklist") {
            CalMetalDetectorConveyorSystemChecklist(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorConveyorDetails") {
            CalMetalDetectorConveyorConveyorDetails(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorIndicators") {
            CalMetalDetectorConveyorIndicators(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorInfeedPEC") {
            CalMetalDetectorConveyorInfeedPEC(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorRejectConfirmPEC") {
            CalMetalDetectorConveyorRejectConfirmPEC(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorBinFullPEC") {
            CalMetalDetectorConveyorBinFullPEC(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorBackupPEC") {
            CalMetalDetectorConveyorBackupPEC(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorAirPressureSensor") {
            CalMetalDetectorConveyorAirPressureSensor(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorPackCheckSensor") {
            CalMetalDetectorConveyorPackCheckSensor(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorSpeedSensor") {
            CalMetalDetectorConveyorSpeedSensor(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorDetectNotification") {
            CalMetalDetectorConveyorDetectNotification(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorBinDoorMonitor") {
            CalMetalDetectorConveyorBinDoorMonitor(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorSmeDetails") {
            CalMetalDetectorConveyorSmeDetails(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorComplianceConfirmation") {
            CalMetalDetectorConveyorComplianceConfirmation(
                navController = navController,
                viewModel = calibrationViewModel
            )
        }

        composable("CalMetalDetectorConveyorSummary") {
            CalMetalDetectorConveyorSummary(
                navController = navController,
                viewModel = calibrationViewModel,
                apiService = apiService
            )
        }

    }
}
