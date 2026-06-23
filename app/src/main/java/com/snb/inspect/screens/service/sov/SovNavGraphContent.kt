package com.snb.inspect.screens.service.sov

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.snb.inspect.ApiService
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel

@Composable
fun SovNavGraphContent(
    navController: NavHostController,
    viewModel: SensitivityOptimisationValidationViewModel,
    apiService: ApiService,
    onScreenChanged: @Composable (content: @Composable () -> Unit) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "ValidationStart"
    ) {
        composable("ValidationStart") {
            onScreenChanged { SovStartScreen(viewModel) }
        }
        composable("ValidationProductDetails") {
            onScreenChanged { SovProductDetailsScreen(viewModel) }
        }
        composable("ValidationDetectionSettingsAsLeft") {
            onScreenChanged { SovDetectionSettingsAsLeftScreen(viewModel) }
        }
        composable("ValidationFerrousTestAsLeft") {
            onScreenChanged { SovFerrousTestAsLeftScreen(viewModel) }
        }
        composable("ValidationNonFerrousTestAsLeft") {
            onScreenChanged { SovNonFerrousTestAsLeftScreen(viewModel) }
        }
        composable("ValidationStainlessTestAsLeft") {
            onScreenChanged { SovStainlessTestAsLeftScreen(viewModel) }
        }
        composable("ValidationPackValidation") {
            onScreenChanged { SovPackValidationScreen(viewModel) }
        }
        composable("ValidationSummary") {
            onScreenChanged { SovSummaryScreen(viewModel, apiService) }
        }
    }
}
