package com.snb.inspect.ui.theme

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.snb.inspect.ApiService
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.screens.service.mdCalibration.*

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

        composable("CalMetalDetectorConveyorFerrousTestAsFound") {
            onScreenChanged {
                CalMetalDetectorConveyorFerrousTestAsFound(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorNonFerrousTestAsFound") {
            onScreenChanged {
                CalMetalDetectorConveyorNonFerrousTestAsFound(viewModel)
            }
        }

        composable("CalMetalDetectorConveyorStainlessTestAsFound") {
            onScreenChanged {
                CalMetalDetectorConveyorStainlessTestAsFound(viewModel)
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

        // -------------------- EQUIPMENT USED --------------------
        composable("CalMetalDetectorConveyorEquipmentUsed") {
            onScreenChanged {
                CalMetalDetectorConveyorEquipmentUsed(viewModel)
            }
        }

        // -------------------- SUMMARY --------------------
        composable("CalMetalDetectorConveyorSummary") {
            onScreenChanged {
                CalMetalDetectorConveyorSummary( viewModel, apiService)
            }
        }
    }
}
