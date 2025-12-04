package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithTextInput
import com.example.mecca.formModules.LabeledTextFieldWithHelp

////@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorIndicators(
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

    val indicator6colour by viewModel.indicator6colour
    val indicator5colour by viewModel.indicator5colour
    val indicator4colour by viewModel.indicator4colour
    val indicator3colour by viewModel.indicator3colour
    val indicator2colour by viewModel.indicator2colour
    val indicator1colour by viewModel.indicator1colour

    val indicator6label by viewModel.indicator6label
    val indicator5label by viewModel.indicator5label
    val indicator4label by viewModel.indicator4label
    val indicator3label by viewModel.indicator3label
    val indicator2label by viewModel.indicator2label
    val indicator1label by viewModel.indicator1label

    val indicatorsEngineerNotes by viewModel.indicatorsEngineerNotes

    val isNextStepEnabled =
        indicator6colour.isNotBlank() &&
                indicator5colour.isNotBlank() &&
                indicator4colour.isNotBlank() &&
                indicator3colour.isNotBlank() &&
                indicator2colour.isNotBlank() &&
                indicator1colour.isNotBlank() &&
                indicator6label.isNotBlank() &&
                indicator5label.isNotBlank() &&
                indicator4label.isNotBlank() &&
                indicator3label.isNotBlank() &&
                indicator2label.isNotBlank() &&
                indicator1label.isNotBlank()

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationBanner(
            progress = progress,
            viewModel = viewModel

        )

        // Navigation Buttons
        CalibrationNavigationButtons(
            onPreviousClick = { viewModel.updateIndicators() },
            onCancelClick = { viewModel.updateIndicators() },
            onNextClick = {
                viewModel.updateIndicators()
                navController.navigate("CalMetalDetectorConveyorLargeMetalTest")
            },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false, // Indicates this is the first step and disables the Previous button
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateIndicators()
            },
        )
        CalibrationHeader("Indicators")


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState) // Add scrolling to the whole column
        ) {

            val colourOptions =
                listOf(
                    "Red",
                    "Yellow",
                    "Green",
                    "White",
                    "Green",
                    "Blue",
                    "Amber",
                    "Sounder",
                    "Other"
                )

            LabeledDropdownWithTextInput(
                label = "Indicator 6",
                dropdownLabel = "Colour",
                options = colourOptions,
                selectedOption = indicator6colour,
                onOptionChange = { viewModel.setIndicator6colour(it) },
                helpText = "Please select an option from the dropdown. If 'Other' is selected, provide more details.",
                inputLabel = "Label",
                inputValue = indicator6label,
                onInputValueChange = { viewModel.setIndicator6label(it) },
                isNAToggleEnabled = true
            )


            LabeledDropdownWithTextInput(
                label = "Indicator 5",
                dropdownLabel = "Colour",
                options = colourOptions,
                selectedOption = indicator5colour,
                onOptionChange = { viewModel.setIndicator5colour(it) },
                helpText = "Please select an option from the dropdown. If 'Other' is selected, provide more details.",
                inputLabel = "Label",
                inputValue = indicator5label,
                onInputValueChange = { viewModel.setIndicator5label(it) },
                isNAToggleEnabled = true
            )

            LabeledDropdownWithTextInput(
                label = "Indicator 4",
                dropdownLabel = "Colour",
                options = colourOptions,
                selectedOption = indicator4colour,
                onOptionChange = { viewModel.setIndicator4colour(it) },
                helpText = "Please select an option from the dropdown. If 'Other' is selected, provide more details.",
                inputLabel = "Label",
                inputValue = indicator4label,
                onInputValueChange = { viewModel.setIndicator4label(it) },
                isNAToggleEnabled = true
            )


            LabeledDropdownWithTextInput(
                label = "Indicator 3",
                dropdownLabel = "Colour",
                options = colourOptions,
                selectedOption = indicator3colour,
                onOptionChange = { viewModel.setIndicator3colour(it) },
                helpText = "Please select an option from the dropdown. If 'Other' is selected, provide more details.",
                inputLabel = "Label",
                inputValue = indicator3label,
                onInputValueChange = { viewModel.setIndicator3label(it) },
                isNAToggleEnabled = true
            )

            LabeledDropdownWithTextInput(
                label = "Indicator 2",
                dropdownLabel = "Colour",
                options = colourOptions,
                selectedOption = indicator2colour,
                onOptionChange = { viewModel.setIndicator2colour(it) },
                helpText = "Please select an option from the dropdown. If 'Other' is selected, provide more details.",
                inputLabel = "Label",
                inputValue = indicator2label,
                onInputValueChange = { viewModel.setIndicator2label(it) },
                isNAToggleEnabled = true
            )

            LabeledDropdownWithTextInput(
                label = "Indicator 1",
                dropdownLabel = "Colour",
                options = colourOptions,
                selectedOption = indicator1colour,
                onOptionChange = { viewModel.setIndicator1colour(it) },
                helpText = "Please select an option from the dropdown. If 'Other' is selected, provide more details.",
                inputLabel = "Label",
                inputValue = indicator1label,
                onInputValueChange = { viewModel.setIndicator1label(it) },
                isNAToggleEnabled = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextFieldWithHelp(
                label = "Engineer Notes",
                value = indicatorsEngineerNotes,
                onValueChange = { newValue -> viewModel.setIndicatorsEngineerNotes(newValue) },
                helpText = "Enter any notes relevant to this section",
                isNAToggleEnabled = false
            )
        }
    }
}
