package com.snb.inspect.ui.theme

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.snb.inspect.ApiService
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.screens.service.cwCalibration.*

@Composable
fun CheckweigherCalibrationNavGraphContent(
    navController: NavHostController,
    viewModel: CalibrationCheckweigherViewModel,
    calibrationId: String,
    apiService: ApiService,
    onScreenChanged: @Composable (content: @Composable () -> Unit) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "CheckweigherCalibrationStart/$calibrationId"
    ) {

        composable(
            route = "CheckweigherCalibrationStart/{calibrationId}",
            arguments = listOf(navArgument("calibrationId") { type = NavType.StringType })
        ) {
            onScreenChanged {
                CalCheckweigherCalibrationStart(viewModel)
            }
        }

        composable("CwScaleDetails") {
            onScreenChanged {
                CalCwScaleDetails(viewModel)
            }
        }

        composable("CwSystemDetails") {
            onScreenChanged {
                CalCwSystemDetails(viewModel)
            }
        }

        composable("CwSystemChecklist") {
            onScreenChanged {
                CalCwSystemChecklist(viewModel)
            }
        }

        composable("CwIndicators") {
            onScreenChanged {
                CalCwIndicators(viewModel)
            }
        }

        composable("CwStaticScaleReference") {
            onScreenChanged {
                CalCwStaticScaleReference(viewModel)
            }
        }

        composable("CwTolerancesAndAssessment") {
            onScreenChanged {
                CalCwTolerancesAndAssessment(viewModel)
            }
        }

        composable("CwTestProductDetails") {
            onScreenChanged {
                CalCwTestProductDetails(viewModel)
            }
        }

        composable("CwEngineerTestWeight") {
            onScreenChanged {
                CalCwEngineerTestWeight(viewModel)
            }
        }

        composable("CwDynamicTestAsFound") {
            onScreenChanged {
                CalCwDynamicTestAsFound(viewModel)
            }
        }

        composable("CwDynamicEccentricityTest") {
            onScreenChanged {
                CalCwDynamicEccentricityTest(viewModel)
            }
        }

        composable("CwStaticTestAsFound") {
            onScreenChanged {
                CalCwStaticTestAsFound(viewModel)
            }
        }

        composable("CwInfeedSensor") {
            onScreenChanged {
                CalCwInfeedSensor(viewModel)
            }
        }

        composable("CwRejectConfirmSensor") {
            onScreenChanged {
                CalCwRejectConfirmSensor(viewModel)
            }
        }

        composable("CwBinFullSensor") {
            onScreenChanged {
                CalCwBinFullSensor(viewModel)
            }
        }

        composable("CwAirPressureSensor") {
            onScreenChanged {
                CalCwAirPressureSensor(viewModel)
            }
        }

        composable("CwBinDoorMonitor") {
            onScreenChanged {
                CalCwBinDoorMonitor(viewModel)
            }
        }

        composable("CwRejectVerification") {
            onScreenChanged {
                CalCwRejectVerification(viewModel)
            }
        }

        composable("CwSummary") {
            onScreenChanged {
                CalCwSummary(viewModel, apiService)
            }
        }
    }
}
