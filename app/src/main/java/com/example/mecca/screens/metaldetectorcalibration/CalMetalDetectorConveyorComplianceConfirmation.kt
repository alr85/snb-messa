package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.CalibrationBanner
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorComplianceConfirmation(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel()
) {

    // Stops the next button from being pressed until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }

    val progress = viewModel.progress
    val scrollState = rememberScrollState() // Scroll state to control the scroll behavior

    // Get and update data in the ViewModel
    val sensitivityCompliance by viewModel.sensitivityCompliance
    val essentialRequirementCompliance by viewModel.essentialRequirementCompliance
    val failsafeCompliance by viewModel.failsafeCompliance
    val bestSensitivityCompliance by viewModel.bestSensitivityCompliance
    val sensitivityRecommendations by viewModel.sensitivityRecommendations
    val performanceValidationIssued by viewModel.performanceValidationIssued




    // Column layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        CalibrationBanner(
            progress = progress,
            viewModel = viewModel
        )

        // Navigation Buttons
        CalibrationNavigationButtons(
            onPreviousClick = { viewModel.updateComplianceConfirmation() },
            onCancelClick = {
                viewModel.updateComplianceConfirmation()
            },
            onNextClick = {
                viewModel.updateComplianceConfirmation()
                navController.navigate("CalMetalDetectorConveyorSummary") },
            isNextEnabled = true,
            isFirstStep = false,
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateComplianceConfirmation()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Retailer Compliance Confirmation",
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(20.dp))

        LabeledTriStateSwitchWithHelp(
            label = "Sensitivity Compliance",
            currentState = sensitivityCompliance,
            onStateChange = { viewModel.setSensitivityCompliance(it) },
            helpText = "If the achieved sensitivities comply with the M&S code of practice sensitivity requirements, select Yes. Otherwise, select No."
        )

        LabeledTriStateSwitchWithHelp(
            label = "Essential Compliance",
            currentState = essentialRequirementCompliance,
            onStateChange = { viewModel.setEssentialRequirementCompliance(it) },
            helpText = "If the comply with the M&S code of practice sensitivity requirements, select Yes. Otherwise, select No."
        )

        LabeledTriStateSwitchWithHelp(
            label = "Failsafe Compliance",
            currentState = failsafeCompliance,
            onStateChange = { viewModel.setFailsafeCompliance(it) },
            helpText = "If the fail safe tests comply with the M&S code of practice requirements, select Yes. Otherwise, select No."
        )

        LabeledTriStateSwitchWithHelp(
            label = "Best Sensitivity Report Completed",
            currentState = bestSensitivityCompliance,
            onStateChange = { viewModel.setBestSensitivityCompliance(it) },
            helpText = "If a Best Sensitivity Report has been completed for this machine, select Yes. Otherwise, select No."
        )

        LabeledTextFieldWithHelp(
            label = "Sensitivity Recommendations",
            value = sensitivityRecommendations,
            onValueChange = { newValue -> viewModel.setSensitivityRecommendations(newValue) },
            helpText = "Enter details of any Sensitivity Recommendations",
            isNAToggleEnabled = true
        )

        LabeledTriStateSwitchWithHelp(
            label = "Performance Validation Issued?",
            currentState = performanceValidationIssued,
            onStateChange = { viewModel.setPerformanceValidationIssued(it) },
            helpText = "If the complies with the M&S code of practice Performance Validation requirements, select Yes. Otherwise, select No."
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
