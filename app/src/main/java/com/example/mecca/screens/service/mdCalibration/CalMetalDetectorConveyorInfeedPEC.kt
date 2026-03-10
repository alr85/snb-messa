package com.example.mecca.screens.service.mdCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateInfeedSensorPvResult
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getInfeedSensorPvRules
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.*
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorInfeedPEC(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {

    val fitted by viewModel.infeedSensorFitted
    val detail by viewModel.infeedSensorDetail
    val testMethod by viewModel.infeedSensorTestMethod
    val testMethodOther by viewModel.infeedSensorTestMethodOther
    val testResult by viewModel.infeedSensorTestResult.collectAsState()
    val notes by viewModel.infeedSensorEngineerNotes
    val latched by viewModel.infeedSensorLatched
    val controlledRestart by viewModel.infeedSensorCR

    val pvRequired = viewModel.pvRequired.value

    // Options
    val testMethodOptions = remember {
        listOf("Large Metal Test", "Manual Block", "Device Block", "Other")
    }

    val testResultOptions = remember {
        listOf("No Result", "Audible Notification", "Visual Notification", "On-Screen Notification", "Belt Stops", "In-feed Belt Stops", "Out-feed Belt Stops", "Other")
    }

    // Validation for Next button
    val isNextStepEnabled = when (fitted) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> {
            detail.isNotBlank() &&
                    testMethod.isNotBlank() &&
                    testResult.isNotEmpty() &&
                    latched != YesNoState.NA &&
                    controlledRestart != YesNoState.NA &&
                    (testMethod != "Other" || testMethodOther.isNotBlank())
        }
        else -> false
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    // PV Rules Calculation
    val rules = viewModel.getInfeedSensorPvRules()

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Failsafe Tests - Photogating/Infeed Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                // --- 1. Sensor Fitted & Detail ---
                LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                    label = "Sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setInfeedSensorFitted(newState)
                        if (newState == YesNoState.NA || newState == YesNoState.NO) {
                            viewModel.setInfeedSensorDetail("N/A")
                            viewModel.setInfeedSensorTestMethod("N/A")
                            viewModel.setInfeedSensorTestMethodOther("N/A")
                            viewModel.setInfeedSensorTestResult(emptyList())
                            viewModel.setInfeedSensorLatched(YesNoState.NA)
                            viewModel.setInfeedSensorCR(YesNoState.NA)
                        } else if (newState == YesNoState.YES) {
                            viewModel.setInfeedSensorDetail("")
                            viewModel.setInfeedSensorTestMethod("")
                            viewModel.setInfeedSensorTestMethodOther("")
                            viewModel.setInfeedSensorTestResult(emptyList())
                            viewModel.setInfeedSensorLatched(YesNoState.NO)
                            viewModel.setInfeedSensorCR(YesNoState.NO)
                        }
                        viewModel.autoUpdateInfeedSensorPvResult()
                    },
                    helpText = "Select if there is an in-feed/gated timer sensor fitted.",
                    inputLabel = "Detail",
                    inputValue = detail,
                    onInputValueChange = {
                        viewModel.setInfeedSensorDetail(it)
                        viewModel.autoUpdateInfeedSensorPvResult()
                    },
                    inputMaxLength = 12,
                    pvStatus = if (pvRequired) {
                        if (fitted == YesNoState.YES) rules.getOrNull(0)?.status?.name else "N/A"
                    } else null,
                    pvRules = if (pvRequired) {
                        if (fitted == YesNoState.YES) listOfNotNull(rules.getOrNull(0)) else rules
                    } else emptyList()
                )

                FormSpacer()

                if (fitted == YesNoState.YES) {

                    // --- 2. Test Method ---
                    LabeledDropdownWithHelp(
                        label = "Test Method",
                        options = testMethodOptions,
                        selectedOption = testMethod,
                        onSelectionChange = {
                            viewModel.setInfeedSensorTestMethod(it)
                            viewModel.autoUpdateInfeedSensorPvResult()
                        },
                        helpText = "Select the method used to trigger the sensor.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(1)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(1)) else emptyList()
                    )

                    FormSpacer()

                    if (testMethod == "Other") {
                        LabeledTextFieldWithHelp(
                            label = "Other Test Method",
                            value = testMethodOther,
                            onValueChange = {
                                viewModel.setInfeedSensorTestMethodOther(it)
                                viewModel.autoUpdateInfeedSensorPvResult()
                            },
                            helpText = "Enter the custom test method.",
                            isNAToggleEnabled = false,
                            maxLength = 12
                        )
                        FormSpacer()
                    }

                    // --- 3. Test Result ---
                    LabeledMultiSelectDropdownWithHelp(
                        label = "Test Result",
                        value = testResult.joinToString(", "),
                        options = testResultOptions,
                        selectedOptions = testResult,
                        onSelectionChange = { newSelection ->
                            val cleaned = if ("No Result" in newSelection) listOf("No Result") 
                                          else newSelection.filterNot { it == "No Result" }
                            
                            viewModel.setInfeedSensorTestResult(cleaned)
                            viewModel.autoUpdateInfeedSensorPvResult()
                        },
                        helpText = "Select the outcome of the sensor test.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(2)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(2)) else emptyList()
                    )

                    FormSpacer()

                    // --- 4. Fault Latched ---
                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setInfeedSensorLatched(it)
                            viewModel.autoUpdateInfeedSensorPvResult()
                        },
                        helpText = "Is the fault output latched, or does it clear automatically?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(3)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(3)) else emptyList()
                    )

                    FormSpacer()

                    // --- 5. Controlled Restart ---
                    LabeledTriStateSwitchWithHelp(
                        label = "Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setInfeedSensorCR(it)
                            viewModel.autoUpdateInfeedSensorPvResult()
                        },
                        helpText = "Is a controlled restart required after a fault?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(4)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(4)) else emptyList()
                    )

                    FormSpacer()
                }

                // --- Global Section PV Result ---
                if (pvRequired) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        value = viewModel.infeedSensorTestPvResult.value,
                        onValueChange = viewModel::setInfeedSensorTestPvResult,
                        helpText = """
                        Auto-Pass rules (when PV required):
                          • Sensor fitted = Yes
                          • Detail entered
                          • Test method selected (and 'Other' described if chosen)
                          • At least one test result selected
                          • Fault Latched is not N/A
                          • Controlled Restart is not N/A

                        Otherwise auto-fail. You may override manually.
                    """.trimIndent()
                    )
                    FormSpacer()
                }

                if (viewModel.pvRequired.value) {
                    PvSectionSummaryCard(
                        title = "Infeed sensor test P.V. Summary",
                        rules = rules
                    )
                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setInfeedSensorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
