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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSystemDetails(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel()
) {
    //val progress = viewModel.progress
    val scrollState = rememberScrollState() // Scroll state to control the scroll behavior

// Get and update data in the ViewModel

    val rejectSynchronisationSetting by viewModel.rejectSynchronisationSetting
    val rejectSynchronisationDetail by viewModel.rejectSynchronisationDetail
    val rejectDelaySetting by viewModel.rejectDelaySetting
    val rejectDurationSetting by viewModel.rejectDurationSetting
    val rejectConfirmWindowSetting by viewModel.rejectConfirmWindowSetting

    // Determine if "Next Step" button should be enabled
//    val isNextStepEnabled = systemLocation.isNotBlank() &&
//            (canPerformCalibration == true || (canPerformCalibration == false && reasonForNotCalibrating.isNotBlank()))
    val isNextStepEnabled = true


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState) // Add scrolling to the whole column
    ) {


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "System Details",
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
        )

        Spacer(modifier = Modifier.height(20.dp))

        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "Reject Synchronisation",
            currentState = rejectSynchronisationSetting,
            onStateChange = { newState -> viewModel.setRejectSynchronisationSetting(newState) },
            helpText = "Select if there is a method of reject synchronisation",
            inputLabel = "Detail",
            inputValue = rejectSynchronisationDetail,
            onInputValueChange = { newValue -> viewModel.setRejectSynchronisationDetail(newValue) },
            //inputKeyboardType = KeyboardType.Number
        )


        LabeledTextFieldWithHelp(
            label = "Reject Delay",
            value = rejectDelaySetting,
            onValueChange = { newValue -> viewModel.setRejectDelaySetting(newValue) },
            helpText = "Enter the reject delay setting",
            keyboardType = KeyboardType.Number
        )

        LabeledTextFieldWithHelp(
            label = "Reject Duration",
            value = rejectDurationSetting,
            onValueChange = { newValue -> viewModel.setRejectDurationSetting(newValue) },
            helpText = "Enter the metal test sample certificate number, usually located on the test piece",
        )

        LabeledTextFieldWithHelp(
            label = "Reject Confirm Window",
            value = rejectConfirmWindowSetting,
            onValueChange = { newValue -> viewModel.setRejectConfirmWindowSetting(newValue) },
            helpText = "Enter the metal test sample certificate number, usually located on the test piece",
        )

        val options = listOf(
            "Alarm Belt Stop",
            "Air Blast",
            "Air Kicker",
            "Air Divert Arm",
            "Air Divert Flap",
            "Electric Divert Arm",
            "Other")

        var selectedOption by remember { mutableStateOf<String?>(null) }

        LabeledDropdownWithHelp(
            label = "Reject System",
            options = options,
            selectedOption = selectedOption,
            onSelectionChange = { newSelection ->
                selectedOption = newSelection
                viewModel.setRejectDevice(newSelection) // Update the ViewModel with the selected option
            },
            helpText = "Select one option from the dropdown."
        )

        if(selectedOption == "Other"){
            LabeledTextFieldWithHelp(
                label = "Other Reject Device",
                value = rejectDurationSetting,
                onValueChange = { newValue -> viewModel.setRejectDeviceOther(newValue) },
                helpText = "Enter the custom reject device name",
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

    }
}
