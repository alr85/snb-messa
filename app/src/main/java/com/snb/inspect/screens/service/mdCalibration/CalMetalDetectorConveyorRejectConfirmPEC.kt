package com.snb.inspect.screens.service.mdCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationLogic.metalDetectorConveyor.autoUpdateRejectConfirmSensorPvResult
import com.snb.inspect.calibrationLogic.metalDetectorConveyor.getRejectConfirmSensorPvRules
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.formModules.*
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorRejectConfirmPEC(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val fitted by viewModel.rejectConfirmSensorFitted
    val testMethod by viewModel.rejectConfirmSensorTestMethod
    val testMethodOther by viewModel.rejectConfirmSensorTestMethodOther
    val testResult by viewModel.rejectConfirmSensorTestResult.collectAsState()
    val stopPosition by viewModel.rejectConfirmSensorStopPosition
    val latched by viewModel.rejectConfirmSensorLatched
    val controlledRestart by viewModel.rejectConfirmSensorCR
    val notes by viewModel.rejectConfirmSensorEngineerNotes
    val ind1label by viewModel.indicator1label
    val ind1colour by viewModel.indicator1colour
    val ind2label by viewModel.indicator2label
    val ind2colour by viewModel.indicator2colour
    val ind3label by viewModel.indicator3label
    val ind3colour by viewModel.indicator3colour
    val ind4label by viewModel.indicator4label
    val ind4colour by viewModel.indicator4colour
    val ind5label by viewModel.indicator5label
    val ind5colour by viewModel.indicator5colour
    val ind6label by viewModel.indicator6label
    val ind6colour by viewModel.indicator6colour

    val pvRequired = viewModel.pvRequired.value

    val testMethodOptions = remember {
        listOf("Reject Override Switch/Button", "Remove Pack", "Other")
    }

    val testResultOptions = remember(
        ind1label, ind1colour,
        ind2label, ind2colour,
        ind3label, ind3colour,
        ind4label, ind4colour,
        ind5label, ind5colour,
        ind6label, ind6colour
    ) {
        buildList {
            add("No Result")
            if (ind6label.isNotBlank() && ind6label != "N/A") add("Indicator 6 ($ind6colour)")
            if (ind5label.isNotBlank() && ind5label != "N/A") add("Indicator 5 ($ind5colour)")
            if (ind4label.isNotBlank() && ind4label != "N/A") add("Indicator 4 ($ind4colour)")
            if (ind3label.isNotBlank() && ind3label != "N/A") add("Indicator 3 ($ind3colour)")
            if (ind2label.isNotBlank() && ind2label != "N/A") add("Indicator 2 ($ind2colour)")
            if (ind1label.isNotBlank() && ind1label != "N/A") add("Indicator 1 ($ind1colour)")
            addAll(
                listOf(
                    "Audible Notification",
                    "Visual Notification",
                    "On-Screen Notification",
                    "System Belt Stops",
                    "In-feed Belt Stops",
                    "Out-feed Belt Stops",
                    "Other"
                )
            )
        }
    }

    val stopPositionOptions = remember {
        listOf("Controlled", "Uncontrolled")
    }

    val isNextStepEnabled = when (fitted) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> {
                    testMethod.isNotBlank() &&
                    testResult.isNotEmpty() &&
                    stopPosition.isNotBlank() &&
                    latched != YesNoState.NA &&
                    controlledRestart != YesNoState.NA
        }
        else -> false
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    val rules = remember(
        fitted,
        testMethod,
        testMethodOther,
        testResult,
        stopPosition,
        latched,
        controlledRestart
    ) {
        viewModel.getRejectConfirmSensorPvRules()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader("Failsafe Tests - Reject Confirm/Activation Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {
                LabeledYesNoNaSegmentedSwitchWithHelp(
                    label = "Sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setRejectConfirmSensorFitted(newState)
                        if (newState != YesNoState.YES) {
                            viewModel.setRejectConfirmSensorTestMethod("N/A")
                            viewModel.setRejectConfirmSensorTestMethodOther("")
                            viewModel.setRejectConfirmSensorTestResult(emptyList())
                            viewModel.setRejectConfirmSensorStopPosition("N/A")
                            viewModel.setRejectConfirmSensorLatched(YesNoState.NA)
                            viewModel.setRejectConfirmSensorCR(YesNoState.NA)
                        } else {
                            viewModel.setRejectConfirmSensorTestMethod("")
                            viewModel.setRejectConfirmSensorTestMethodOther("")
                            viewModel.setRejectConfirmSensorTestResult(emptyList())
                            viewModel.setRejectConfirmSensorStopPosition("")
                            viewModel.setRejectConfirmSensorLatched(YesNoState.NO)
                            viewModel.setRejectConfirmSensorCR(YesNoState.NO)
                        }
                        viewModel.autoUpdateRejectConfirmSensorPvResult()
                    },
                    helpText = "Is a sensor fitted to confirm the pack was correctly rejected?",
                    onInputValueChange = {

                    },
                    pvStatus = if (pvRequired) rules.find { it.ruleId == "REJECT_CONFIRM_FITTED" }?.status?.name else null,
                    pvRules = rules.filter { it.ruleId == "REJECT_CONFIRM_FITTED" }
                )

                FormSpacer()

                if (fitted == YesNoState.YES) {
                    LabeledDropdownWithHelp(
                        label = "Test Method",
                        options = testMethodOptions,
                        selectedOption = testMethod,
                        onSelectionChange = {
                            if(it != "Other") viewModel.setRejectConfirmSensorTestMethodOther("")
                            viewModel.setRejectConfirmSensorTestMethod(it)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Select the method used to trigger the reject confirmation fault.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "REJECT_CONFIRM_METHOD" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "REJECT_CONFIRM_METHOD" }
                    )

                    FormSpacer()

                    if (testMethod == "Other") {
                        LabeledTextFieldWithHelp(
                            label = "Other Test Method",
                            value = testMethodOther,
                            onValueChange = {
                                viewModel.setRejectConfirmSensorTestMethodOther(it)
                                viewModel.autoUpdateRejectConfirmSensorPvResult()
                            },
                            helpText = "Enter the custom test method.",
                            isNAToggleEnabled = false,
                            maxLength = 12
                        )
                        FormSpacer()
                    }

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Test Result",
                        value = testResult.joinToString(", "),
                        options = testResultOptions,
                        selectedOptions = testResult,
                        onSelectionChange = { newSelection ->
                            val cleaned = if ("No Result" in newSelection) listOf("No Result")
                                            else newSelection.filterNot { it == "No Result" }

                            viewModel.setRejectConfirmSensorTestResult(cleaned)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Select the outcome of the sensor test.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "REJECT_CONFIRM_RESULT" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "REJECT_CONFIRM_RESULT" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setRejectConfirmSensorLatched(it)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Is the fault output latched, or does it clear automatically?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "REJECT_CONFIRM_LATCHED" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "REJECT_CONFIRM_LATCHED" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setRejectConfirmSensorCR(it)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Is a manual reset required to restart the system?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "REJECT_CONFIRM_CR" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "REJECT_CONFIRM_CR" }
                    )

                    FormSpacer()

                    LabeledDropdownWithHelp(
                        label = "Pack Stop Position",
                        options = stopPositionOptions,
                        selectedOption = stopPosition,
                        onSelectionChange = {
                            viewModel.setRejectConfirmSensorStopPosition(it)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Where does the pack stop when this fault occurs?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "REJECT_CONFIRM_STOP_POS" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "REJECT_CONFIRM_STOP_POS" }                    )

                    if(!pvRequired) FormSpacer()
                }

                if (pvRequired) {

                    PvSectionSummaryCard(
                        title = "Reject confirm/activation test P.V. Summary",
                        rules = rules
                    )

                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setRejectConfirmSensorEngineerNotes,
                    helpText = "Optional notes for this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
