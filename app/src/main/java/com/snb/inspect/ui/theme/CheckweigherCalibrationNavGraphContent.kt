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

        composable("CwFailsafes") {
            onScreenChanged {
                CalCwFailsafes(viewModel)
            }
        }

        composable("CwTestProductDetails") {
            onScreenChanged {
                CalCwTestProductDetails(viewModel)
            }
        }

        composable("CwStaticScaleReference") {
            onScreenChanged {
                CalCwStaticScaleReference(viewModel)
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

        composable("CwStaticTestAsFound") {
            onScreenChanged {
                CalCwStaticTestAsFound(viewModel)
            }
        }

        composable("CwAdjustmentsMade") {
            onScreenChanged {
                CalCwAdjustmentsMade(viewModel)
            }
        }

        composable("CwDynamicTestAsLeft") {
            onScreenChanged {
                CalCwDynamicTestAsLeft(viewModel)
            }
        }

        composable("CwStaticTestAsLeft") {
            onScreenChanged {
                CalCwStaticTestAsLeft(viewModel)
            }
        }

        composable("CwSummary") {
            onScreenChanged {
                CalCwSummary(viewModel, apiService)
            }
        }
    }
}
