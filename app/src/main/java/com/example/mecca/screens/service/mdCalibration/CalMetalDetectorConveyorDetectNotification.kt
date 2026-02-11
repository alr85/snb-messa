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
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledMultiSelectDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorDetectNotification(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {


    val result by viewModel.detectNotificationResult.collectAsState()
    val notes by viewModel.detectNotificationEngineerNotes

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
                    },
                    helpText = """
                    Select one or more notification results.
                    
                    If "No Result" is selected, all other options will be cleared.
                """.trimIndent(),
                    isNAToggleEnabled = false

                )

                FormSpacer()

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