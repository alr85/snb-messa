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
        composable("ValidationSystemComments") {
            onScreenChanged { SovSystemCommentsScreen(viewModel) }
        }
        composable("ValidationProductDetails") {
            onScreenChanged { SovProductDetailsScreen(viewModel) }
        }
        composable("ValidationProductComments") {
            onScreenChanged { SovProductCommentsScreen(viewModel) }
        }
        composable("ValidationDetectionSettingsAsFound") {
            onScreenChanged { SovDetectionSettingsAsFoundScreen(viewModel) }
        }
        composable("ValidationFerrousTestAsFound") {
            onScreenChanged { SovFerrousTestAsFoundScreen(viewModel) }
        }
        composable("ValidationNonFerrousTestAsFound") {
            onScreenChanged { SovNonFerrousTestAsFoundScreen(viewModel) }
        }
        composable("ValidationStainlessTestAsFound") {
            onScreenChanged { SovStainlessTestAsFoundScreen(viewModel) }
        }
        composable("ValidationOptimisation") {
            onScreenChanged { SovOptimisationScreen(viewModel) }
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
        composable("ValidationSummary") {
            onScreenChanged { SovSummaryScreen(viewModel, apiService) }
        }
    }
}
