package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.CalibrationBanner
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.AnimatedActionPill
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSensitivityRequirements(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel // Pass ViewModel here
) {
    // Stops the next button from being pressed until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }


    val progress = viewModel.progress

// Get and update data in the ViewModel
    val desiredCop by viewModel.desiredCop.collectAsState()
    val sensitivityRequirementFerrous by viewModel.sensitivityRequirementFerrous
    val sensitivityRequirementNonFerrous by viewModel.sensitivityRequirementNonFerrous
    val sensitivityRequirementStainless by viewModel.sensitivityRequirementStainless



     //Determine if "Next Step" button should be enabled
    val isNextStepEnabled =
        //desiredCop.isNotEmpty() &&
                sensitivityRequirementFerrous.isNotBlank() &&
                sensitivityRequirementNonFerrous.isNotBlank() &&
                sensitivityRequirementStainless.isNotBlank()

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationBanner(
            progress = progress,
            viewModel = viewModel
        )

        CalibrationNavigationButtons(
            onPreviousClick = { viewModel.updateSensitivityRequirements() },
            onCancelClick = { viewModel.updateSensitivityRequirements() },
            onNextClick = {
                viewModel.updateSensitivityRequirements()
                navController.navigate("CalMetalDetectorConveyorDetectionSettingsAsFound")
            },

            isNextEnabled = isNextStepEnabled,
            isFirstStep = false, // Indicates this is the first step and disables the Previous button
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                //viewModel.saveCalibrationData() // Custom save logic here
            })

        CalibrationHeader("Customer Sensitivity Requirements")




    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {


//        val options = listOf("SALSA", "BRC", "Aldi", "Lidl", "Sainsbury's", "Morrison's", "Tesco", "Waitrose", "M&S")
//
//        // Observe `desiredCop` directly from the ViewModel
//        val desiredCopState by viewModel.desiredCop.collectAsState()

//        Log.d("dropdown","Selected Options: $desiredCopState")
//
//        LabeledMultiSelectDropdownWithHelp(
//            label = "Desired COP(s)",
//            value = desiredCopState.joinToString(" + "), // Display selected options as string
//            options = options,
//            selectedOptions = desiredCopState, // Pass directly from ViewModel
//            onSelectionChange = { newSelectedOptions ->
//                viewModel.setDesiredCop(newSelectedOptions) // Update ViewModel directly
//            },
//            helpText = "If the customer is aiming for a particular Code of Practice (COP), select it here."
//        )

        if (viewModel.pvRequired.value && viewModel.sensitivityData.value != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                AnimatedActionPill(
                    text = "Paste M&S Targets",
                    icon = Icons.Outlined.ContentPasteGo,
                    onClick = { PasteMStargetSensitivities(viewModel) }
                )
            }
        }




        LabeledTextFieldWithHelp(
            label = "Ferrous (mm)",
            value = sensitivityRequirementFerrous,
            onValueChange = { newValue ->
                val cleaned = newValue.replace(",", ".")
                viewModel.setSensitivityRequirementFerrous(cleaned) },
            helpText = """
                Enter the customer requirement for Ferrous sensitivity
                
                M&S Target: ${viewModel.sensitivityData.value?.FerrousTargetMM?.toString() ?: "N/A"} mm
                M&S Max: ${viewModel.sensitivityData.value?.FerrousMaxMM?.toString() ?: "N/A"} mm
            """.trimIndent(),
            keyboardType = KeyboardType.Number
        )

        LabeledTextFieldWithHelp(
            label = "Non Ferrous (mm)",
            value = sensitivityRequirementNonFerrous,
            onValueChange = { newValue ->
                val cleaned = newValue.replace(",", ".")
                viewModel.setSensitivityRequirementNonFerrous(cleaned) },
            helpText = """
                Enter the customer requirement for Non-Ferrous sensitivity
                
                M&S Target: ${viewModel.sensitivityData.value?.NonFerrousTargetMM?.toString() ?: "N/A"} mm
                M&S Max: ${viewModel.sensitivityData.value?.NonFerrousMaxMM?.toString() ?: "N/A"} mm
            """.trimIndent(),
            keyboardType = KeyboardType.Number
        )

        LabeledTextFieldWithHelp(
            label = "Stainless Steel (mm)",
            value = sensitivityRequirementStainless,
            onValueChange = { newValue ->
                val cleaned = newValue.replace(",", ".")
                viewModel.setSensitivityRequirementStainless(cleaned) },
            helpText = """
                Enter the customer requirement for Stainless Steel sensitivity
                
                M&S Target: ${viewModel.sensitivityData.value?.Stainless316TargetMM?.toString() ?: "N/A"} mm
                M&S Max: ${viewModel.sensitivityData.value?.Stainless316MaxMM?.toString() ?: "N/A"} mm
            """.trimIndent(),
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Notes",
            value = viewModel.sensitivityRequirementEngineerNotes.value,
            onValueChange = { newValue -> viewModel.setSensitivityRequirementEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )

    }
    }
}


fun PasteMStargetSensitivities(viewModel: CalibrationMetalDetectorConveyorViewModel) {

    val sensitivityData = viewModel.sensitivityData.value

    if (sensitivityData != null) {
        viewModel.setSensitivityRequirementFerrous(sensitivityData.FerrousTargetMM.toString())
        viewModel.setSensitivityRequirementNonFerrous(sensitivityData.NonFerrousTargetMM.toString())
        viewModel.setSensitivityRequirementStainless(sensitivityData.Stainless316TargetMM.toString())
    }


}
