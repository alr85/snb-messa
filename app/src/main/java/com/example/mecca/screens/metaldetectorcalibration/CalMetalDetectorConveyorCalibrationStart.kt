package com.example.mecca.screens.metaldetectorcalibration

//import com.example.mecca.screens.getAppVersion
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.CalibrationBanner
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledRadioButtonWithHelp
import com.example.mecca.formModules.LabeledReadOnlyField
import com.example.mecca.formModules.LabeledTextFieldWithHelp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorCalibrationStart(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel, // Pass ViewModel here

) {

    val progress = viewModel.progress

    // Get current values from the ViewModel
    val lastLocation by viewModel.lastLocation
    val canPerformCalibration by viewModel.canPerformCalibration
    val reasonForNotCalibrating by viewModel.reasonForNotCalibrating
    //val lastLocation by viewModel.lastLocation



    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled =
            (canPerformCalibration == true || (canPerformCalibration == false && reasonForNotCalibrating.isNotBlank()))



    //viewModel.setAppVersion(getAppVersion())
    //viewModel.setEngineerId("Engineer ID")


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
            navController = navController,
            onPreviousClick = { /* Disable or hide the Previous button */ },
            onCancelClick = {
                viewModel.updateCalibrationStart()
            },
            onNextClick = {
                viewModel.updateCalibrationStart()
                if(canPerformCalibration == true){
                    navController.navigate("CalMetalDetectorConveyorSensitivityRequirements")
                } else {
                    navController.navigate("CalMetalDetectorConveyorSummary")
                }

            },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = true,
            viewModel = viewModel,
            onSaveAndExitClick = {
                //viewModel.saveCalibrationData() // Custom save logic here
            },
        )



        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Calibration Start")

        Spacer(modifier = Modifier.height(20.dp))

        LabeledReadOnlyField(
            label = "Serial Number",
            value = viewModel.serialNumber.value,
            helpText = "This is the unique identifier of the system. It cannot be changed."
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledReadOnlyField(
            label = "Make/Model",
            value = viewModel.modelDescription.value,
            helpText = "This is the make/model of the system. It cannot be changed."
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledReadOnlyField(
            label = "Last Location",
            value = viewModel.lastLocation.value,
            helpText = "This is in reference to the last known location of this system e.g. 'Line 7'"
        )

        Spacer(modifier = Modifier.height(16.dp))



        LabeledTextFieldWithHelp(
            label = "New Location",
            value = viewModel.newLocation.value,
            onValueChange = { newValue ->
                viewModel.setNewLocation(
                    newValue
                )
            },
            helpText = "Enter the machine's new location (e.g., 'Line 1'). This helps with identifying the system on site.",
            isNAToggleEnabled = false

            )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledRadioButtonWithHelp(
            label = "Able to Calibrate?",
            value = canPerformCalibration,
            onValueChange = { newValue ->
                viewModel.setCanPerformCalibration(newValue)

                        if(newValue) {
                            viewModel.setReasonForNotCalibrating("N/A")
                        }
                },
            helpText = "Select 'Yes' if you are able to calibrate this product. Select 'No' otherwise."
        )

        // Conditional TextField: Only show if "No" is selected
        if (canPerformCalibration == false) {
            Spacer(modifier = Modifier.height(8.dp))

            LabeledTextFieldWithHelp(
                label = "Reason for not calibrating",
                value = reasonForNotCalibrating,
                onValueChange = { newValue ->
                    viewModel.setReasonForNotCalibrating(
                        newValue
                    )
                },
                helpText = "Enter the reason for being unable to calibrate this machine.",
                isNAToggleEnabled = false
            )

        }


    }
}