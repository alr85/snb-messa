package com.snb.inspect.screens.service.sov

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel

@Composable
fun SovNavGraphContent(
    navController: NavHostController,
    viewModel: SensitivityOptimisationValidationViewModel,
    onScreenChanged: @Composable (content: @Composable () -> Unit) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "SovStart"
    ) {
        composable("SovStart") {
            onScreenChanged { SovStartScreen(viewModel) }
        }
        composable("SovProductDetails") {
            onScreenChanged { SovProductDetailsScreen(viewModel) }
        }
        composable("SovAsFound") {
            onScreenChanged { SovAsFoundScreen(viewModel) }
        }
        composable("SovOptimisation") {
            onScreenChanged { SovOptimisationScreen(viewModel) }
        }
        composable("SovValidation") {
            onScreenChanged { SovValidationScreen(viewModel) }
        }
        composable("SovAsLeft") {
            onScreenChanged { SovAsLeftScreen(viewModel) }
        }
        composable("SovComments") {
            onScreenChanged { SovCommentsScreen(viewModel) }
        }
        composable("SovSummary") {
            onScreenChanged { SovSummaryScreen(viewModel) }
        }
    }
}
