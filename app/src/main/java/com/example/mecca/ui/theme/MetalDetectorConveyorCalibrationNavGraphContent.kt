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
//import com.example.mecca.screens.metaldetectorcalibration.CalMetalDetectorConveyorComplianceConfirmation
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