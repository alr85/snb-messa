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
import com.snb.inspect.calibrationLogic.metalDetectorConveyor.autoUpdateDetectNotificationTestPvResult
import com.snb.inspect.calibrationLogic.metalDetectorConveyor.getDetectNotificationPvRules
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledMultiSelectDropdownWithHelp
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.formModules.PvSectionSummaryCard
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorDetectNotification(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {


    val result by viewModel.detectNotificationResult.collectAsState()
    val notes by viewModel.detectNotificationEngineerNotes

    val pvRequired by viewModel.pvRequired


    val resultOptions = remember {
        listOf(
            "No Result",
            "Audible Notification (Latched)",
            "Visual Notification (Latched)",
            "On-Screen Notification (Latched)",
            "Belt Stops (Latched)",
            "Audible Notification (Not Latched)",
            "Visual Notification (Not Latched)",
            "On-Screen Notification (Not Latched)",
            "Belt Stops (Not Latched)",
            "In-feed Belt Stops",
            "Out-feed Belt Stops",
            "Other"
        )
    }

    // Next enabled
    val isNextStepEnabled = result.isNotEmpty()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    // PV Rules Calculation
    val rules = viewModel.getDetectNotificationPvRules()

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Failsafe Tests - Detect Notification")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {

            Column {

                LabeledMultiSelectDropdownWithHelp(
                    label = "Detect Notification Result",
                    value = result.joinToString(", "),
                    options = resultOptions,
                    selectedOptions = result,
                    onSelectionChange = { newSelection ->

                        val cleaned = when {
                            // If "No Result" is selected, it must be the ONLY option
                            "No Result" in newSelection -> listOf("No Result")

                            // Otherwise remove "No Result" if present
                            else -> newSelection.filterNot { it == "No Result" }
                        }

                        viewModel.setDetectNotificationResult(cleaned)

                        viewModel.autoUpdateDetectNotificationTestPvResult()
                    },
                    helpText = """
                    Select one or more notification results.
                    
                    If "No Result" is selected, all other options will be cleared.
                """.trimIndent(),
                    isNAToggleEnabled = false,
                    pvStatus = if (pvRequired) rules.find { it.ruleId == "DETECT_NOTIFICATION" }?.status?.name else null,
                    pvRules = if (pvRequired) rules.filter { it.ruleId == "DETECT_NOTIFICATION" } else emptyList()



                )

                FormSpacer()

                //-----------------------------------------------------
                //  PV Summary Card (replacing the old radio selector)
                //-----------------------------------------------------
                if (pvRequired) {
                    PvSectionSummaryCard(
                        title = "Detect Notification P.V. Summary",
                        rules = rules
                    )

                }


//                if (viewModel.pvRequired.value) {
//                    LabeledFourOptionRadioWithHelp(
//                        label = "P.V. Result",
//                        value = viewModel.detectNotificationTestPvResult.value,
//                        onValueChange = viewModel::setDetectNotificationTestPvResult,
//                        helpText = """
//                    Auto-Pass rules (when PV required):
//
//                    Otherwise auto-fail. You may override manually.
//                    """.trimIndent()
//                    )
//
//                    FormSpacer()
//                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setDetectNotificationEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}