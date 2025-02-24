
package com.example.mecca.screens

import CalibrationBanner
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.CalibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.CalibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledMultiSelectDropdownWithHelp
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
        desiredCop.isNotEmpty() &&
                sensitivityRequirementFerrous.isNotBlank() &&
                sensitivityRequirementNonFerrous.isNotBlank() &&
                sensitivityRequirementStainless.isNotBlank()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display the consistent banner at the top
        CalibrationBanner(
            progress = progress,
            viewModel = viewModel

        )

        // Navigation Buttons
        CalibrationNavigationButtons(
            onPreviousClick = { viewModel.updateSensitivityRequirements() },
            onCancelClick = { viewModel.updateSensitivityRequirements() },
            onNextClick = {
                viewModel.updateSensitivityRequirements()
                navController.navigate("CalMetalDetectorConveyorProductDetails")
                          },

            isNextEnabled = isNextStepEnabled,
            isFirstStep = false, // Indicates this is the first step and disables the Previous button
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                //viewModel.saveCalibrationData() // Custom save logic here
            },
        )


        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Sensitivity Requirements")

        Spacer(modifier = Modifier.height(20.dp))

        val options = listOf("SALSA", "BRC", "Aldi", "Lidl", "Sainsbury's", "Morrisons", "Tesco", "Waitrose", "M&S")

        // Observe `desiredCop` directly from the ViewModel
        val desiredCopState by viewModel.desiredCop.collectAsState()

        Log.d("dropdown","Selected Options: $desiredCopState")

        LabeledMultiSelectDropdownWithHelp(
            label = "Desired COP(s)",
            value = desiredCopState.joinToString(" + "), // Display selected options as string
            options = options,
            selectedOptions = desiredCopState, // Pass directly from ViewModel
            onSelectionChange = { newSelectedOptions ->
                viewModel.setDesiredCop(newSelectedOptions) // Update ViewModel directly
            },
            helpText = "If the customer is aiming for a particular Code of Practice (COP), select it here."
        )


        LabeledTextFieldWithHelp(
            label = "Ferrous (mm)",
            value = sensitivityRequirementFerrous,
            onValueChange = { newValue -> viewModel.setSensitivityRequirementFerrous(newValue) },
            helpText = "Enter the customer requirement for Ferrous sensitivity",
            keyboardType = KeyboardType.Number
        )

        LabeledTextFieldWithHelp(
            label = "Non Ferrous (mm)",
            value = sensitivityRequirementNonFerrous,
            onValueChange = { newValue -> viewModel.setSensitivityRequirementNonFerrous(newValue) },
            helpText = "Enter the customer requirement for Non Ferrous sensitivity",
            keyboardType = KeyboardType.Number
        )

        LabeledTextFieldWithHelp(
            label = "Stainless Steel (mm)",
            value = sensitivityRequirementStainless,
            onValueChange = { newValue -> viewModel.setSensitivityRequirementStainless(newValue) },
            helpText = "Enter the customer requirement for Stainless Steel sensitivity",
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
