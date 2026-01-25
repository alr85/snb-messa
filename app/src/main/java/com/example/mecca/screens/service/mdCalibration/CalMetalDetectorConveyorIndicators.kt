package com.example.mecca.screens.service.mdCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithTextInput
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar
import com.example.mecca.util.capitaliseFirstChar

@Composable
fun CalMetalDetectorConveyorIndicators(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    // --- ViewModel state ---
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

    val notes by viewModel.indicatorsEngineerNotes

    val colourOptions = remember {
        listOf("Red", "Yellow", "Green", "Blue", "Amber", "White", "Sounder", "Other")
    }

    // Next enabled rules
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

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    // Local helper (no @Composable annotation needed)
    @Composable
    fun indicatorRow(
        number: Int,
        colour: String,
        label: String,
        onColourChange: (String) -> Unit,
        onLabelChange: (String) -> Unit
    ) {
        LabeledDropdownWithTextInput(
            label = "Indicator $number",
            dropdownLabel = "Colour",
            options = colourOptions,
            selectedOption = colour,
            onOptionChange = onColourChange,
            helpText = "Select a colour. If 'Other' is selected, describe it in the label.",
            inputLabel = "Label",
            inputValue = label,
            onInputValueChange = onLabelChange,
            isNAToggleEnabled = true
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Indicators")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),  // replaces your old Column padding
            // Make thumb red (if your wrapper supports it):
            // scrollbarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
        ) {
            Column {

                indicatorRow(
                    number = 6,
                    colour = indicator6colour,
                    label = indicator6label,
                    onColourChange = viewModel::setIndicator6colour,
                    onLabelChange = { viewModel.setIndicator6label(capitaliseFirstChar(it)) }
                )

                FormSpacer()

                indicatorRow(
                    number = 5,
                    colour = indicator5colour,
                    label = indicator5label,
                    onColourChange = viewModel::setIndicator5colour,
                    onLabelChange = { viewModel.setIndicator5label(capitaliseFirstChar(it)) }
                )

                FormSpacer()

                indicatorRow(
                    number = 4,
                    colour = indicator4colour,
                    label = indicator4label,
                    onColourChange = viewModel::setIndicator4colour,
                    onLabelChange = { viewModel.setIndicator4label(capitaliseFirstChar(it)) }
                )

                FormSpacer()

                indicatorRow(
                    number = 3,
                    colour = indicator3colour,
                    label = indicator3label,
                    onColourChange = viewModel::setIndicator3colour,
                    onLabelChange = { viewModel.setIndicator3label(capitaliseFirstChar(it)) }
                )

                FormSpacer()

                indicatorRow(
                    number = 2,
                    colour = indicator2colour,
                    label = indicator2label,
                    onColourChange = viewModel::setIndicator2colour,
                    onLabelChange = { viewModel.setIndicator2label(capitaliseFirstChar(it)) }
                )

                FormSpacer()

                indicatorRow(
                    number = 1,
                    colour = indicator1colour,
                    label = indicator1label,
                    onColourChange = viewModel::setIndicator1colour,
                    onLabelChange = { viewModel.setIndicator1label(capitaliseFirstChar(it)) }
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = notes,
                    onValueChange = viewModel::setIndicatorsEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
