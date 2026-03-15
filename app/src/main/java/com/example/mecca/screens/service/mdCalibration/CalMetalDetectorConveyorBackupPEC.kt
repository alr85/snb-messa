package com.example.mecca.screens.service.mdCalibration

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
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateBackupSensorPvResult
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getBackupSensorPvRules
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledMultiSelectDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.LabeledYesNoNaSegmentedSwitchWithHelp
import com.example.mecca.formModules.PvSectionSummaryCard
import com.example.mecca.formModules.YesNoState
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorBackupPEC(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val fitted by viewModel.backupSensorFitted
    val testMethod by viewModel.backupSensorTestMethod
    val testMethodOther by viewModel.backupSensorTestMethodOther
    val testResult by viewModel.backupSensorTestResult.collectAsState()
    val notes by viewModel.backupSensorEngineerNotes
    val latched by viewModel.backupSensorLatched
    val controlledRestart by viewModel.backupSensorCR

    val pvRequired = viewModel.pvRequired.value

    val testMethodOptions = remember {
        listOf("Manual Block", "Device Block")
    }

    val testResultOptions = remember {
        listOf(
            "No Result",
            "Audible Notification",
            "Visual Notification",
            "On-Screen Notification",
            "System Belt Stops",
            "In-feed Belt Stops",
            "Out-feed Belt Stops",
            "Other"
        )
    }

    val isNextStepEnabled = when (fitted) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> {
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

    val rules = remember(
        fitted,
        testMethod,
        testMethodOther,
        testResult,
        latched,
        controlledRestart
    ) {
        viewModel.getBackupSensorPvRules()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Failsafe Tests - Backup Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                LabeledYesNoNaSegmentedSwitchWithHelp(
                    label = "Back-up sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setBackupSensorFitted(newState)
                        if (newState == YesNoState.NO || newState == YesNoState.NA) {
                            viewModel.setBackupSensorDetail("")
                            viewModel.setBackupSensorTestMethod("N/A")
                            viewModel.setBackupSensorTestMethodOther("")
                            viewModel.setBackupSensorTestResult(emptyList())
                            viewModel.setBackupSensorLatched(YesNoState.NA)
                            viewModel.setBackupSensorCR(YesNoState.NA)
                        } else if (newState == YesNoState.YES) {
                            viewModel.setBackupSensorDetail("")
                            viewModel.setBackupSensorTestMethod("")
                            viewModel.setBackupSensorTestMethodOther("")
                            viewModel.setBackupSensorTestResult(emptyList())
                            viewModel.setBackupSensorLatched(YesNoState.NO)
                            viewModel.setBackupSensorCR(YesNoState.NO)
                        }
                        viewModel.autoUpdateBackupSensorPvResult()
                    },
                    helpText = "Select if there is a back-up sensor fitted.",
                    onInputValueChange = {
                    },
                    pvStatus = if (pvRequired) rules.find { it.ruleId == "BACKUP_FITTED" }?.status?.name else null,
                    pvRules = if (pvRequired) rules.filter { it.ruleId == "BACKUP_FITTED" } else emptyList()
                )

                FormSpacer()

                if (fitted == YesNoState.YES) {

                    LabeledDropdownWithHelp(
                        label = "Test Method",
                        options = testMethodOptions,
                        selectedOption = testMethod,
                        onSelectionChange = {
                            if (it != "Other") viewModel.setBackupSensorTestMethodOther("")
                            viewModel.setBackupSensorTestMethod(it)
                            viewModel.autoUpdateBackupSensorPvResult()
                        },
                        helpText = "Select the method used to test the back-up failsafe.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BACKUP_METHOD" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "BACKUP_METHOD" } else emptyList()
                    )

                    FormSpacer()

                    if (testMethod == "Other") {
                        LabeledTextFieldWithHelp(
                            label = "Other Test Method",
                            value = testMethodOther,
                            onValueChange = {
                                viewModel.setBackupSensorTestMethodOther(it)
                                viewModel.autoUpdateBackupSensorPvResult()
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
                            val cleaned = if ("No Result" in newSelection) {
                                listOf("No Result")
                            } else {
                                newSelection.filterNot { it == "No Result" }
                            }

                            viewModel.setBackupSensorTestResult(cleaned)
                            viewModel.autoUpdateBackupSensorPvResult()
                        },
                        helpText = "Select the observed failsafe action.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BACKUP_RESULT" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "BACKUP_RESULT" } else emptyList()
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setBackupSensorLatched(it)
                            viewModel.autoUpdateBackupSensorPvResult()
                        },
                        helpText = "Does the fault remain active until manually cleared?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BACKUP_LATCHED" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "BACKUP_LATCHED" } else emptyList()
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setBackupSensorCR(it)
                            viewModel.autoUpdateBackupSensorPvResult()
                        },
                        helpText = "Is a manual reset required to restart the system?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BACKUP_CR" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "BACKUP_CR" } else emptyList()
                    )

                    if (!pvRequired) FormSpacer()
                }

                if (pvRequired) {
                    PvSectionSummaryCard(
                        title = "Backup sensor test P.V. Summary",
                        rules = rules
                    )
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setBackupSensorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}