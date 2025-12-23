package com.example.mecca.screens.metaldetectorcalibration


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorConveyorDetails(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val scrollState = rememberScrollState()

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

    // Tell wrapper when Next should be enabled
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

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {

            LabeledTextFieldWithHelp(
                label = "In-feed Belt Height (mm)",
                value = infeedBeltHeight,
                onValueChange = viewModel::setInfeedBeltHeight,
                helpText = "Distance from floor to belt on infeed end.",
                keyboardType = KeyboardType.Number
            )

            LabeledTextFieldWithHelp(
                label = "Out-feed Belt Height (mm)",
                value = outfeedBeltHeight,
                onValueChange = viewModel::setOutfeedBeltHeight,
                helpText = "Distance from floor to belt on outfeed end.",
                keyboardType = KeyboardType.Number
            )

            LabeledTextFieldWithHelp(
                label = "Conveyor Length (mm)",
                value = conveyorLength,
                onValueChange = viewModel::setConveyorLength,
                helpText = "Enter base length (floor space) for inclined conveyors.",
                keyboardType = KeyboardType.Number
            )

            LabeledTextFieldWithHelp(
                label = "Belt Speed (m/m)",
                value = beltSpeed,
                onValueChange = viewModel::setBeltSpeed,
                helpText = "Measured using a tachometer.",
                keyboardType = KeyboardType.Number
            )

            LabeledDropdownWithHelp(
                label = "Conveyor Handing",
                options = handingOptions,
                selectedOption = conveyorHanding,
                onSelectionChange = viewModel::setConveyorHanding,
                helpText = "Select left-to-right or right-to-left orientation."
            )

            LabeledDropdownWithHelp(
                label = "Reject System",
                options = rejectOptions,
                selectedOption = rejectDevice,
                onSelectionChange = viewModel::setRejectDevice,
                helpText = "Select the reject device type."
            )

            if (rejectDevice == "Other") {
                LabeledTextFieldWithHelp(
                    label = "Other Reject Device",
                    value = rejectDeviceOther,
                    onValueChange = viewModel::setRejectDeviceOther,
                    helpText = "Enter custom description."
                )
            }

            Spacer(Modifier.height(16.dp))

            LabeledTextFieldWithHelp(
                label = "Engineer Notes",
                value = conveyorDetailsEngineerNotes,
                onValueChange = viewModel::setConveyorDetailsEngineerNotes,
                helpText = "Optional notes for this section.",
                isNAToggleEnabled = false
            )
        }
    }
}
