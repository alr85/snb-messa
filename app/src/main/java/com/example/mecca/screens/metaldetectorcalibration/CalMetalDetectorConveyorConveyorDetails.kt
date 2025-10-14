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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.CalibrationBanner
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorConveyorDetails(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel()
) {
    // Stops the next button from being pressed until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }

    val progress = viewModel.progress
    val scrollState = rememberScrollState()

    // Get and update data in the ViewModel

    val infeedBeltHeight by viewModel.infeedBeltHeight
    val outfeedBeltHeight by viewModel.outfeedBeltHeight
    val conveyorLength by viewModel.conveyorLength
    val conveyorHanding by viewModel.conveyorHanding
    val beltSpeed by viewModel.beltSpeed
    val rejectDevice by viewModel.rejectDevice
    val rejectDeviceOther by viewModel.rejectDeviceOther
    val conveyorDetailsEngineerNotes by viewModel.conveyorDetailsEngineerNotes

    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled =
        infeedBeltHeight.isNotBlank() &&
                outfeedBeltHeight.isNotBlank() &&
                conveyorLength.isNotBlank() &&
                beltSpeed.isNotBlank() &&
                rejectDevice.isNotBlank()

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
            onPreviousClick = { viewModel.updateConveyorDetails() },
            onCancelClick = { viewModel.updateConveyorDetails() },
            onNextClick = {
                viewModel.updateConveyorDetails()
                navController.navigate("CalMetalDetectorConveyorSystemChecklist") },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false,
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateConveyorDetails()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Conveyor Details")

        Spacer(modifier = Modifier.height(20.dp))

        LabeledTextFieldWithHelp(
            label = "In-feed Belt Height (mm)",
            value = infeedBeltHeight,
            onValueChange = { newValue -> viewModel.setInfeedBeltHeight(newValue) },
            helpText = "Enter the distance from the floor to the belt on the in feed end",
            keyboardType = KeyboardType.Number
        )

        LabeledTextFieldWithHelp(
            label = "Out-feed Belt Height (mm)",
            value = outfeedBeltHeight,
            onValueChange = { newValue -> viewModel.setOutfeedBeltHeight(newValue) },
            helpText = "Enter the distance from the floor to the belt on the out feed end",
            keyboardType = KeyboardType.Number
        )

        LabeledTextFieldWithHelp(
            label = "Conveyor Length (mm)",
            value = conveyorLength,
            onValueChange = { newValue -> viewModel.setConveyorLength(newValue) },
            helpText = "Enter the length of the conveyor. For inclined conveyors - enter the base, or the amount of floor space the conveyor takes up",
            keyboardType = KeyboardType.Number
        )

        LabeledTextFieldWithHelp(
            label = "Belt Speed (m/m)",
            value = beltSpeed,
            onValueChange = { newValue -> viewModel.setBeltSpeed(newValue) },
            helpText = "Using a tachometer, enter the speed of the belt in metres per minute",
            keyboardType = KeyboardType.Number
        )

        val rejectOptions = listOf(
            "Alarm Belt Stop",
            "Air Blast",
            "Air Kicker",
            "Air Divert Arm",
            "Air Divert Flap",
            "Electric Divert Arm",
            "Other")

        val handingOptions = listOf(
            "Left to Right",
            "Right to Left",
            "Universal")

        LabeledDropdownWithHelp(
            label = "Conveyor Handing",
            options = handingOptions,
            selectedOption = conveyorHanding,
            onSelectionChange = { newSelection ->
                viewModel.setConveyorHanding(newSelection) // Update the ViewModel with the selected option
            },
            helpText = "Select one option from the dropdown."
        )

        LabeledDropdownWithHelp(
            label = "Reject System",
            options = rejectOptions,
            selectedOption = rejectDevice,
            onSelectionChange = { newSelection ->
                viewModel.setRejectDevice(newSelection) // Update the ViewModel with the selected option
            },
            helpText = "Select one option from the dropdown."
        )

        if(rejectDevice == "Other"){
            LabeledTextFieldWithHelp(
                label = "Other Reject Device",
                value = rejectDeviceOther,
                onValueChange = { newValue -> viewModel.setRejectDeviceOther(newValue) },
                helpText = "Enter the custom reject device name",
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Notes",
            value = conveyorDetailsEngineerNotes,
            onValueChange = { newValue -> viewModel.setConveyorDetailsEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )

    }
}
