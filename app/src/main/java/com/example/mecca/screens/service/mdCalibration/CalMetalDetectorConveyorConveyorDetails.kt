package com.example.mecca.screens.service.mdCalibration


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorConveyorDetails(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val infeedBeltHeight by viewModel.infeedBeltHeight
    val outfeedBeltHeight by viewModel.outfeedBeltHeight
    val conveyorLength by viewModel.conveyorLength
    val conveyorHanding by viewModel.conveyorHanding
    val beltSpeed by viewModel.beltSpeed
    val rejectDevice by viewModel.rejectDevice
    val rejectDeviceOther by viewModel.rejectDeviceOther
    val conveyorDetailsEngineerNotes by viewModel.conveyorDetailsEngineerNotes

    // Validation
    val isNextStepEnabled =
        infeedBeltHeight.isNotBlank() &&
                outfeedBeltHeight.isNotBlank() &&
                conveyorLength.isNotBlank() &&
                beltSpeed.isNotBlank() &&
                rejectDevice.isNotBlank() &&
                (rejectDevice != "Other" || rejectDeviceOther.isNotBlank())

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    val rejectOptions = remember {
        listOf(
            "Alarm Belt Stop",
            "Air Blast",
            "Air Kicker",
            "Air Divert Arm",
            "Air Divert Flap",
            "Electric Divert Arm",
            "Other"
        )
    }

    val handingOptions = remember {
        listOf("Left to Right", "Right to Left", "Universal")
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Conveyor Details")

        // Scrollable area with scrollbar
        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            // if you want it red via theme:
            // scrollbarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ) {
            Column {
                LabeledTextFieldWithHelp(
                    label = "In-feed Belt Height (mm)",
                    value = infeedBeltHeight,
                    onValueChange = viewModel::setInfeedBeltHeight,
                    helpText = "Distance from floor to belt on infeed end.",
                    keyboardType = KeyboardType.Number,
                    maxLength = 4
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Out-feed Belt Height (mm)",
                    value = outfeedBeltHeight,
                    onValueChange = viewModel::setOutfeedBeltHeight,
                    helpText = "Distance from floor to belt on outfeed end.",
                    keyboardType = KeyboardType.Number,
                    maxLength = 4
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Conveyor Length (mm)",
                    value = conveyorLength,
                    onValueChange = viewModel::setConveyorLength,
                    helpText = "Enter base length (floor space) for inclined conveyors.",
                    keyboardType = KeyboardType.Number,
                    maxLength = 4
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Belt Speed (m/m)",
                    value = beltSpeed,
                    onValueChange = viewModel::setBeltSpeed,
                    helpText = "Measured using a tachometer.",
                    keyboardType = KeyboardType.Number,
                    maxLength = 3
                )

                FormSpacer()

                LabeledDropdownWithHelp(
                    label = "Conveyor Handing",
                    options = handingOptions,
                    selectedOption = conveyorHanding,
                    onSelectionChange = viewModel::setConveyorHanding,
                    helpText = "Select left-to-right or right-to-left orientation."
                )

                FormSpacer()

                LabeledDropdownWithHelp(
                    label = "Reject System",
                    options = rejectOptions,
                    selectedOption = rejectDevice,
                    onSelectionChange = viewModel::setRejectDevice,
                    helpText = "Select the reject device type."
                )

                FormSpacer()

                if (rejectDevice == "Other") {
                    LabeledTextFieldWithHelp(
                        label = "Other Reject Device",
                        value = rejectDeviceOther,
                        onValueChange = viewModel::setRejectDeviceOther,
                        helpText = "Enter custom description.",
                        maxLength = 12
                    )

                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = conveyorDetailsEngineerNotes,
                    onValueChange = viewModel::setConveyorDetailsEngineerNotes,
                    helpText = "Optional notes for this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                // optional breathing room so last field isn't glued to bottom buttons
                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
