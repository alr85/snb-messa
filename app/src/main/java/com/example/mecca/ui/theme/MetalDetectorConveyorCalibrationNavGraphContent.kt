package com.example.mecca.ui.theme

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mecca.ApiService
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorAirPressureSensor
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorBackupPEC
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorBinDoorMonitor
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorBinFullPEC
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorCalibrationStart
//import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorComplianceConfirmation
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorConveyorDetails
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorDetectNotification
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorDetectionSettingsAsFound
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorDetectionSettingsAsLeft
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorFerrousTest
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorIndicators
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorInfeedPEC
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorLargeMetalTest
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorNonFerrousTest
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorPackCheckSensor
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorProductDetails
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorRejectConfirmPEC
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorRejectSettings
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorSensitivityAsFound
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorSensitivityRequirements
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorSmeDetails
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorSpeedSensor
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorStainlessTest
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorSummary
import com.example.mecca.screens.service.mdCalibration.CalMetalDetectorConveyorSystemChecklist

@Composable
fun MetalDetectorConveyorCalibrationNavGraphContent(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel,
    calibrationId: String,
    apiService: ApiService,
    onScreenChanged: @Composable (content: @Composable () -> Unit) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "MetalDetectorConveyorCalibrationStart/$calibrationId"
    ) {

        // -------------------- START SCREEN --------------------
        composable(
            route = "MetalDetectorConveyorCalibrationStart/{calibrationId}",
            arguments = listOf(navArgument("calibrationId") { type = NavType.StringType })
        ) {
            onScreenChanged {
                CalMetalDetectorConveyorCalibrationStart( viewModel)
            }
        }

        // -------------------- SENSITIVITY REQUIREMENTS --------------------
        composable("CalMetalDetectorConveyorSensitivityRequirements") {
            onScreenChanged {
                CalMetalDetectorConveyorSensitivityRequirements( viewModel)
            }
        }

        // -------------------- PRODUCT DETAILS --------------------
        composable("CalMetalDetectorConveyorProductDetails") {
            onScreenChanged {
                CalMetalDetectorConveyorProductDetails(viewModel)
            }
        }

        // -------------------- DETECTION SETTINGS (AS FOUND) --------------------
        composable("CalMetalDetectorConveyorDetectionSettingsAsFound") {
            onScreenChanged {
                CalMetalDetectorConveyorDetectionSettingsAsFound(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorSensitivityAsFound") {
            onScreenChanged {
                CalMetalDetectorConveyorSensitivityAsFound(viewModel)
            }
        }

        // -------------------- METAL TESTS --------------------
        composable("CalMetalDetectorConveyorFerrousTest") {
            onScreenChanged {
                CalMetalDetectorConveyorFerrousTest(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorNonFerrousTest") {
            onScreenChanged {
                CalMetalDetectorConveyorNonFerrousTest(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorStainlessTest") {
            onScreenChanged {
                CalMetalDetectorConveyorStainlessTest(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorLargeMetalTest") {
            onScreenChanged {
                CalMetalDetectorConveyorLargeMetalTest(viewModel)
            }
        }

        // -------------------- DETECTION SETTINGS (AS LEFT) --------------------
        composable("CalMetalDetectorConveyorDetectionSettingsAsLeft") {
            onScreenChanged {
                CalMetalDetectorConveyorDetectionSettingsAsLeft(viewModel)
            }
        }

        // -------------------- REJECT SYSTEM --------------------
        composable("CalMetalDetectorConveyorRejectSettings") {
            onScreenChanged {
                CalMetalDetectorConveyorRejectSettings(viewModel)
            }
        }

        // -------------------- SYSTEM CHECKLIST --------------------
        composable("CalMetalDetectorConveyorSystemChecklist") {
            onScreenChanged {
                CalMetalDetectorConveyorSystemChecklist(viewModel)
            }
        }

        // -------------------- CONVEYOR DETAILS --------------------
        composable("CalMetalDetectorConveyorConveyorDetails") {
            onScreenChanged {
                CalMetalDetectorConveyorConveyorDetails( viewModel)
            }
        }

        // -------------------- INDICATORS --------------------
        composable("CalMetalDetectorConveyorIndicators") {
            onScreenChanged {
                CalMetalDetectorConveyorIndicators(viewModel)
            }
        }

        // -------------------- PEC SENSORS --------------------
        composable("CalMetalDetectorConveyorInfeedPEC") {
            onScreenChanged {
                CalMetalDetectorConveyorInfeedPEC(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorRejectConfirmPEC") {
            onScreenChanged {
                CalMetalDetectorConveyorRejectConfirmPEC(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorBinFullPEC") {
            onScreenChanged {
                CalMetalDetectorConveyorBinFullPEC(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorBackupPEC") {
            onScreenChanged {
                CalMetalDetectorConveyorBackupPEC(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorAirPressureSensor") {
            onScreenChanged {
                CalMetalDetectorConveyorAirPressureSensor(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorPackCheckSensor") {
            onScreenChanged {
                CalMetalDetectorConveyorPackCheckSensor(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorSpeedSensor") {
            onScreenChanged {
                CalMetalDetectorConveyorSpeedSensor(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorDetectNotification") {
            onScreenChanged {
                CalMetalDetectorConveyorDetectNotification(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorBinDoorMonitor") {
            onScreenChanged {
                CalMetalDetectorConveyorBinDoorMonitor( viewModel)
            }
        }

        // -------------------- SME DETAILS --------------------
        composable("CalMetalDetectorConveyorSmeDetails") {
            onScreenChanged {
                CalMetalDetectorConveyorSmeDetails(viewModel)
            }
        }

//        // -------------------- COMPLIANCE CONFIRMATION --------------------
//        composable("CalMetalDetectorConveyorComplianceConfirmation") {
//            onScreenChanged {
//                CalMetalDetectorConveyorComplianceConfirmation(navController, viewModel)
//            }
//        }

        // -------------------- SUMMARY --------------------
        composable("CalMetalDetectorConveyorSummary") {
            onScreenChanged {
                CalMetalDetectorConveyorSummary( viewModel, apiService)
            }
        }
    }
}