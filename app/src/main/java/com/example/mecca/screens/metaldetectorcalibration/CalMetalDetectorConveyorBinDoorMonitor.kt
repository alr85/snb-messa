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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledMultiSelectDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.YesNoState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorBinDoorMonitor(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel()
) {

    // Stops the next button from being pressed until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }

    //val progress = viewModel.progress
    val scrollState = rememberScrollState() // Scroll state to control the scroll behavior

    // Get and update data in the ViewModel
    val binDoorMonitorFitted by viewModel.binDoorMonitorFitted
    val binDoorMonitorDetail by viewModel.binDoorMonitorDetail
    val binDoorStatusAsFound by viewModel.binDoorStatusAsFound
    val binDoorUnlockedIndication by viewModel.binDoorUnlockedIndication.collectAsState()
    val binDoorOpenIndication by viewModel.binDoorOpenIndication.collectAsState()
    val binDoorTimeoutTimer by viewModel.binDoorTimeoutTimer
    val binDoorTimeoutResult by viewModel.binDoorTimeoutResult.collectAsState()
    val binDoorLatched by viewModel.binDoorLatched
    val binDoorCR by viewModel.binDoorCR
    val binDoorEngineerNotes by viewModel.binDoorEngineerNotes

    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled = when (binDoorMonitorFitted) {
        YesNoState.NO, YesNoState.NA -> true // Button enabled for NO or NA
        YesNoState.YES -> {
            // Button enabled only if all other fields are valid
            binDoorMonitorDetail.isNotBlank() &&
                    binDoorStatusAsFound.isNotBlank() &&
                    binDoorUnlockedIndication.isNotEmpty() &&
                    binDoorOpenIndication.isNotEmpty() &&
                    binDoorTimeoutTimer.isNotBlank() &&
                    binDoorTimeoutResult.isNotEmpty() &&
                    binDoorLatched != YesNoState.NA &&
                    binDoorCR != YesNoState.NA
        }

        else -> false // Default to false for safety
    }

    // Test options
    val binDoorAsFoundOptions = listOf(
        "Open and Unlocked",
        "Open and Locked",
        "Closed and Unlocked",
        "Closed and Locked"
    )

    val binDoorTimeoutResults = listOf(
        "No Result",
        "Audible Notification",
        "Visual Notification",
        "On-Screen Notification",
        "Belt Stops",
        "In-feed Belt Stops",
        "Out-feed Belt Stops",
        "Other"
    )

    val binDoorIndications = listOf(
        "No Result",
        "Audible Notification",
        "Visual Notification",
        "On-Screen Notification",
    )

    Column(modifier = Modifier.fillMaxSize()) {
//        CalibrationBanner(
//            progress = progress,
//            viewModel = viewModel
//        )

        // Navigation Buttons
//        CalibrationNavigationButtons(
//            onPreviousClick = { viewModel.updateBinDoorMonitor() },
//            onCancelClick = {
//                viewModel.updateBinDoorMonitor()
//            },
//            onNextClick = {
//                viewModel.updateBinDoorMonitor()
//                navController.navigate("CalMetalDetectorConveyorSmeDetails")
//            },
//            isNextEnabled = isNextStepEnabled,
//            isFirstStep = false,
//            navController = navController,
//            viewModel = viewModel,
//            onSaveAndExitClick = {
//                viewModel.updateBinDoorMonitor()
//            },
//        )

        CalibrationHeader("Compliance Checks - Bin Door Monitor")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {

            LabeledTriStateSwitchAndTextInputWithHelp(
                label = "Bin Door Monitor Fitted?",
                currentState = binDoorMonitorFitted,
                onStateChange = { newState ->
                    viewModel.setBinDoorMonitorFitted(newState)
                    if (newState == YesNoState.NA || newState == YesNoState.NO) {
                        // Set all relevant fields to N/A
                        viewModel.setBinDoorMonitorDetail("N/A")
                        viewModel.setBinDoorStatusAsFound("N/A")
                        viewModel.setBinDoorOpenIndication(emptyList())
                        viewModel.setBinDoorUnlockedIndication(emptyList())
                        viewModel.setBinDoorTimeoutTimer("N/A")
                        viewModel.setBinDoorTimeoutResult(emptyList())
                        viewModel.setBinDoorLatched(YesNoState.NA)
                        viewModel.setBinDoorCR(YesNoState.NA)
                    } else if (newState == YesNoState.YES) {
                        // Clear N/A from selected options when switching back to YES
                        viewModel.setBinDoorMonitorDetail("")
                        viewModel.setBinDoorStatusAsFound("")
                        viewModel.setBinDoorOpenIndication(emptyList())
                        viewModel.setBinDoorUnlockedIndication(emptyList())
                        viewModel.setBinDoorTimeoutTimer("")
                        viewModel.setBinDoorTimeoutResult(emptyList())
                        viewModel.setBinDoorLatched(YesNoState.NO)
                        viewModel.setBinDoorCR(YesNoState.NO)

                    }
                },
                helpText = "Select if there is a Bin Door Monitor fitted",
                inputLabel = "Detail",
                inputValue = binDoorMonitorDetail,
                onInputValueChange = { newValue -> viewModel.setBinDoorMonitorDetail(newValue) }
            )


            // Conditionally display remaining fields if "Yes" is selected for In-feed sensor fitted
            if (binDoorMonitorFitted == YesNoState.YES) {
                LabeledDropdownWithHelp(
                    label = "Bin Door Status As Found",
                    options = binDoorAsFoundOptions,
                    selectedOption = binDoorStatusAsFound,
                    onSelectionChange = { newSelection ->
                        viewModel.setBinDoorStatusAsFound(newSelection)
                    },
                    helpText = "Select one option from the dropdown.",
                    isNAToggleEnabled = false
                )

                val selectedOptionsOpenIndication by viewModel.binDoorOpenIndication.collectAsState()

                LabeledMultiSelectDropdownWithHelp(
                    label = "Bin Door Open Indication",
                    options = binDoorIndications,
                    value = selectedOptionsOpenIndication.joinToString(" + "),
                    selectedOptions = selectedOptionsOpenIndication,
                    onSelectionChange = { newSelectedOptions ->
                        viewModel.setBinDoorOpenIndication(
                            newSelectedOptions
                        )
                    },
                    helpText = "Select one or more items from the dropdown.",
                    isNAToggleEnabled = false
                )

                val selectedOptionsUnlockedIndication by viewModel.binDoorUnlockedIndication.collectAsState()

                LabeledMultiSelectDropdownWithHelp(
                    label = "Bin Door Unlocked Indication",
                    options = binDoorIndications,
                    value = selectedOptionsUnlockedIndication.joinToString(" + "),
                    selectedOptions = selectedOptionsUnlockedIndication,
                    onSelectionChange = { newSelectedOptions ->
                        viewModel.setBinDoorUnlockedIndication(
                            newSelectedOptions
                        )
                    },
                    helpText = "Select one or more items from the dropdown.",
                    isNAToggleEnabled = false
                )

                LabeledTextFieldWithHelp(
                    label = "Bin Door Timeout",
                    value = binDoorTimeoutTimer,
                    onValueChange = { newValue -> viewModel.setBinDoorTimeoutTimer(newValue) },
                    helpText = "How many seconds until the system acknowledges the bin door has been left open/unlocked",
                    isNAToggleEnabled = false
                )

                val selectedOptionsTimeOutResult by viewModel.binDoorTimeoutResult.collectAsState()

                LabeledMultiSelectDropdownWithHelp(
                    label = "Bin Door Timeout Result",
                    options = binDoorTimeoutResults,
                    value = selectedOptionsTimeOutResult.joinToString(" + "),
                    selectedOptions = selectedOptionsTimeOutResult,
                    onSelectionChange = { newSelectedOptions ->
                        viewModel.setBinDoorTimeoutResult(
                            newSelectedOptions
                        )
                    },
                    helpText = "What happens after the Bin Door timer elapses? Select one or more items from the dropdown.",
                    isNAToggleEnabled = false
                )



                LabeledTriStateSwitchWithHelp(
                    label = "Fault Latched?",
                    currentState = binDoorLatched,
                    onStateChange = { newState -> viewModel.setBinDoorLatched(newState) },
                    helpText = "Is the fault output latched, or does it clear automatically?",
                    isNAToggleEnabled = false
                )

                LabeledTriStateSwitchWithHelp(
                    label = "Fault Controlled Restart?",
                    currentState = binDoorCR,
                    onStateChange = { newState -> viewModel.setBinDoorCR(newState) },
                    helpText = "Is the fault output latched, or does it clear automatically?",
                    isNAToggleEnabled = false
                )


            }

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextFieldWithHelp(
                label = "Engineer Comments",
                value = binDoorEngineerNotes,
                onValueChange = { newValue -> viewModel.setBinDoorEngineerNotes(newValue) },
                helpText = "Enter any notes relevant to this section",
                isNAToggleEnabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}