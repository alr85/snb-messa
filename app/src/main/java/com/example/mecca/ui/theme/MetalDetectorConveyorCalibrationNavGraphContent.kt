package com.example.mecca.ui.theme

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
import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorNonFerrousTest

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
                CalMetalDetectorConveyorCalibrationStart(navController, viewModel)
            }
        }

        // -------------------- SENSITIVITY REQUIREMENTS --------------------
        composable("CalMetalDetectorConveyorSensitivityRequirements") {
            onScreenChanged {
                CalMetalDetectorConveyorSensitivityRequirements(navController, viewModel)
            }
        }

        // -------------------- PRODUCT DETAILS --------------------
        composable("CalMetalDetectorConveyorProductDetails") {
            onScreenChanged {
                CalMetalDetectorConveyorProductDetails(navController, viewModel)
            }
        }

        // -------------------- DETECTION SETTINGS (AS FOUND) --------------------
        composable("CalMetalDetectorConveyorDetectionSettingsAsFound") {
            onScreenChanged {
                CalMetalDetectorConveyorDetectionSettingsAsFound(navController, viewModel)
            }
        }

        composable("CalMetalDetectorConveyorSensitivityAsFound") {
            onScreenChanged {
                CalMetalDetectorConveyorSensitivityAsFound(navController, viewModel)
            }
        }

        // -------------------- METAL TESTS --------------------
        composable("CalMetalDetectorConveyorFerrousTest") {
            onScreenChanged {
                CalMetalDetectorConveyorFerrousTest(navController, viewModel)
            }
        }

        composable("CalMetalDetectorConveyorNonFerrousTest") {
            onScreenChanged {
                CalMetalDetectorConveyorNonFerrousTest(navController, viewModel)
            }
        }

        composable("CalMetalDetectorConveyorStainlessTest") {
            onScreenChanged {
                CalMetalDetectorConveyorStainlessTest(navController, viewModel)
            }
        }

        composable("CalMetalDetectorConveyorLargeMetalTest") {
            onScreenChanged {
                CalMetalDetectorConveyorLargeMetalTest(navController, viewModel)
            }
        }

        // -------------------- DETECTION SETTINGS (AS LEFT) --------------------
        composable("CalMetalDetectorConveyorDetectionSettingsAsLeft") {
            onScreenChanged {
                CalMetalDetectorConveyorDetectionSettingsAsLeft(navController, viewModel)
            }
        }

        // -------------------- REJECT SYSTEM --------------------
        composable("CalMetalDetectorConveyorRejectSettings") {
            onScreenChanged {
                CalMetalDetectorConveyorRejectSettings(navController, viewModel)
            }
        }

        // -------------------- SYSTEM CHECKLIST --------------------
        composable("CalMetalDetectorConveyorSystemChecklist") {
            onScreenChanged {
                CalMetalDetectorConveyorSystemChecklist(navController, viewModel)
            }
        }

        // -------------------- CONVEYOR DETAILS --------------------
        composable("CalMetalDetectorConveyorConveyorDetails") {
            onScreenChanged {
                CalMetalDetectorConveyorConveyorDetails(navController, viewModel)
            }
        }

        // -------------------- INDICATORS --------------------
        composable("CalMetalDetectorConveyorIndicators") {
            onScreenChanged {
                CalMetalDetectorConveyorIndicators(navController, viewModel)
            }
        }

        // -------------------- PEC SENSORS --------------------
        composable("CalMetalDetectorConveyorInfeedPEC") {
            onScreenChanged {
                CalMetalDetectorConveyorInfeedPEC(navController, viewModel)
            }
        }

        composable("CalMetalDetectorConveyorRejectConfirmPEC") {
            onScreenChanged {
                CalMetalDetectorConveyorRejectConfirmPEC(navController, viewModel)
            }
        }

        composable("CalMetalDetectorConveyorBinFullPEC") {
            onScreenChanged {
                CalMetalDetectorConveyorBinFullPEC(navController, viewModel)
            }
        }

        composable("CalMetalDetectorConveyorBackupPEC") {
            onScreenChanged {
                CalMetalDetectorConveyorBackupPEC(navController, viewModel)
            }
        }

        composable("CalMetalDetectorConveyorAirPressureSensor") {
            onScreenChanged {
                CalMetalDetectorConveyorAirPressureSensor(navController, viewModel)
            }
        }

        composable("CalMetalDetectorConveyorPackCheckSensor") {
            onScreenChanged {
                CalMetalDetectorConveyorPackCheckSensor(navController, viewModel)
            }
        }

        composable("CalMetalDetectorConveyorSpeedSensor") {
            onScreenChanged {
                CalMetalDetectorConveyorSpeedSensor(navController, viewModel)
            }
        }

        composable("CalMetalDetectorConveyorDetectNotification") {
            onScreenChanged {
                CalMetalDetectorConveyorDetectNotification(navController, viewModel)
            }
        }

        composable("CalMetalDetectorConveyorBinDoorMonitor") {
            onScreenChanged {
                CalMetalDetectorConveyorBinDoorMonitor(navController, viewModel)
            }
        }

        // -------------------- SME DETAILS --------------------
        composable("CalMetalDetectorConveyorSmeDetails") {
            onScreenChanged {
                CalMetalDetectorConveyorSmeDetails(navController, viewModel)
            }
        }

        // -------------------- COMPLIANCE CONFIRMATION --------------------
        composable("CalMetalDetectorConveyorComplianceConfirmation") {
            onScreenChanged {
                CalMetalDetectorConveyorComplianceConfirmation(navController, viewModel)
            }
        }

        // -------------------- SUMMARY --------------------
        composable("CalMetalDetectorConveyorSummary") {
            onScreenChanged {
                CalMetalDetectorConveyorSummary(navController, viewModel, apiService)
            }
        }
    }
}