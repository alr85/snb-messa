package com.example.mecca.screens

import CalibrationBanner
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.CalibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.CalibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.LabeledTwoTextInputsWithHelp
import com.example.mecca.formModules.YesNoState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSmeDetails(
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
    val operatorName by viewModel.operatorName
    val operatorTestWitnessed by viewModel.operatorTestWitnessed
    val operatorTestResultFerrous by viewModel.operatorTestResultFerrous
    val operatorTestResultNonFerrous by viewModel.operatorTestResultNonFerrous
    val operatorTestResultStainless by viewModel.operatorTestResultStainless
    val operatorTestResultLargeMetal by viewModel.operatorTestResultLargeMetal
    val operatorTestResultCertNumberFerrous by viewModel.operatorTestResultCertNumberFerrous
    val operatorTestResultCertNumberNonFerrous by viewModel.operatorTestResultCertNumberNonFerrous
    val operatorTestResultCertNumberStainless by viewModel.operatorTestResultCertNumberStainless
    val operatorTestResultCertNumberLargeMetal by viewModel.operatorTestResultCertNumberLargeMetal
    val smeName by viewModel.smeName
    val smeEngineerNotes by viewModel.smeEngineerNotes

    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled = when (operatorTestWitnessed) {
        YesNoState.NO, YesNoState.NA -> true // Button enabled for NO or NA
        YesNoState.YES -> {
            // Button enabled only if all other fields are valid
            operatorName.isNotBlank() &&
                    operatorTestResultFerrous.isNotBlank() &&
                    operatorTestResultNonFerrous.isNotBlank() &&
                    operatorTestResultStainless.isNotBlank() &&
                    operatorTestResultLargeMetal.isNotBlank() &&
                    operatorTestResultCertNumberFerrous.isNotBlank() &&
                    operatorTestResultCertNumberNonFerrous.isNotBlank() &&
                    operatorTestResultCertNumberStainless.isNotBlank() &&
                    operatorTestResultCertNumberLargeMetal.isNotBlank() &&
                    smeName.isNotBlank()

        }
        else -> false // Default to false for safety
    }


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
            onPreviousClick = { viewModel.updateOperatorTest() },
            onCancelClick = { viewModel.updateOperatorTest() },
            onNextClick = {
                viewModel.updateOperatorTest()
                navController.navigate("CalMetalDetectorConveyorComplianceConfirmation") },
            isNextEnabled = true,
            isFirstStep = false,
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateOperatorTest()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Compliance Checks - Operator Test")

        Spacer(modifier = Modifier.height(20.dp))

        LabeledTriStateSwitchWithHelp(
            label = "Operator Test Witnessed?",
            currentState = operatorTestWitnessed,
            onStateChange = { newState ->
                viewModel.setOperatorTestWitnessed(newState)
                if (newState == YesNoState.NA || newState == YesNoState.NO) {
                    // Set all relevant fields to N/A
                    viewModel.setOperatorName("N/A")
                    viewModel.setOperatorTestResultFerrous("N/A")
                    viewModel.setOperatorTestResultNonFerrous("N/A")
                    viewModel.setOperatorTestResultStainless("N/A")
                    viewModel.setOperatorTestResultLargeMetal("N/A")
                    viewModel.setOperatorTestResultCertNumberFerrous("N/A")
                    viewModel.setOperatorTestResultCertNumberNonFerrous("N/A")
                    viewModel.setOperatorTestResultCertNumberStainless("N/A")
                    viewModel.setOperatorTestResultCertNumberLargeMetal("N/A")
                    viewModel.setSmeName("N/A")
                } else if (newState == YesNoState.YES) {
                    // Set all relevant fields to 
                    viewModel.setOperatorName("")
                    viewModel.setOperatorTestResultFerrous("")
                    viewModel.setOperatorTestResultNonFerrous("")
                    viewModel.setOperatorTestResultStainless("")
                    viewModel.setOperatorTestResultLargeMetal("")
                    viewModel.setOperatorTestResultCertNumberFerrous("")
                    viewModel.setOperatorTestResultCertNumberNonFerrous("")
                    viewModel.setOperatorTestResultCertNumberStainless("")
                    viewModel.setOperatorTestResultCertNumberLargeMetal("")
                    viewModel.setSmeName("")

                }
            },
            helpText = "If you have witnessed an operator do a successful sensitivity check, select Yes. Otherwise, select No."
        )

        if(operatorTestWitnessed == YesNoState.YES) {
            LabeledTextFieldWithHelp(
                label = "Operator Name",
                value = operatorName,
                onValueChange = { newValue -> viewModel.setOperatorName(newValue) },
                helpText = "Enter the name of the operator in charge of this system",
                isNAToggleEnabled = true
            )

            LabeledTwoTextInputsWithHelp(
                label = "Ferrous Test",
                firstInputLabel = "Size",
                firstInputValue = operatorTestResultFerrous,
                onFirstInputValueChange = { newValue -> viewModel.setOperatorTestResultFerrous(newValue) },
                secondInputLabel = "Certificate No.",
                secondInputValue = operatorTestResultCertNumberFerrous,
                onSecondInputValueChange = { newValue -> viewModel.setOperatorTestResultCertNumberFerrous(newValue) },
                helpText = "Enter the details of the Operator Test for ferrous metal",
                firstInputKeyboardType = KeyboardType.Number, // Change if different type of input is required
                secondInputKeyboardType = KeyboardType.Text, // Change if different type of input is required
                isNAToggleEnabled = true // You can set this to false if the N/A toggle isn't required
            )

            LabeledTwoTextInputsWithHelp(
                label = "Non Ferrous Test",
                firstInputLabel = "Size",
                firstInputValue = operatorTestResultNonFerrous,
                onFirstInputValueChange = { newValue -> viewModel.setOperatorTestResultNonFerrous(newValue) },
                secondInputLabel = "Certificate No.",
                secondInputValue = operatorTestResultCertNumberNonFerrous,
                onSecondInputValueChange = { newValue -> viewModel.setOperatorTestResultCertNumberNonFerrous(newValue) },
                helpText = "Enter the details of the Operator Test for non-ferrous metal",
                firstInputKeyboardType = KeyboardType.Number, // Change if different type of input is required
                secondInputKeyboardType = KeyboardType.Text, // Change if different type of input is required
                isNAToggleEnabled = true // You can set this to false if the N/A toggle isn't required
            )

            LabeledTwoTextInputsWithHelp(
                label = "Stainless Test",
                firstInputLabel = "Sample Size",
                firstInputValue = operatorTestResultStainless,
                onFirstInputValueChange = { newValue -> viewModel.setOperatorTestResultStainless(newValue) },
                secondInputLabel = "Certificate No.",
                secondInputValue = operatorTestResultCertNumberStainless,
                onSecondInputValueChange = { newValue -> viewModel.setOperatorTestResultCertNumberStainless(newValue) },
                helpText = "Enter the details of the Operator Test for stainless metal",
                firstInputKeyboardType = KeyboardType.Number, // Change if different type of input is required
                secondInputKeyboardType = KeyboardType.Text, // Change if different type of input is required
                isNAToggleEnabled = true // You can set this to false if the N/A toggle isn't required
            )

            LabeledTwoTextInputsWithHelp(
                label = "Large Metal",
                firstInputLabel = "Size",
                firstInputValue = operatorTestResultLargeMetal,
                onFirstInputValueChange = { newValue -> viewModel.setOperatorTestResultLargeMetal(newValue) },
                secondInputLabel = "Certificate No.",
                secondInputValue = operatorTestResultCertNumberLargeMetal,
                onSecondInputValueChange = { newValue -> viewModel.setOperatorTestResultCertNumberLargeMetal(newValue) },
                helpText = "Enter the details of the Operator Test for large metal",
                firstInputKeyboardType = KeyboardType.Number, // Change if different type of input is required
                secondInputKeyboardType = KeyboardType.Text, // Change if different type of input is required
                isNAToggleEnabled = true // You can set this to false if the N/A toggle isn't required
            )

            LabeledTextFieldWithHelp(
                label = "On Site SME Name",
                value = smeName,
                onValueChange = { newValue -> viewModel.setSmeName(newValue) },
                helpText = "Enter the name of the SME currently on site",
                isNAToggleEnabled = true
            )
        }



        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Comments",
            value = smeEngineerNotes,
            onValueChange = { newValue -> viewModel.setSmeEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
