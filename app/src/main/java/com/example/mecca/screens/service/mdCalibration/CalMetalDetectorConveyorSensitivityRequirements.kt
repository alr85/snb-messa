package com.example.mecca.screens.service.mdCalibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.AnimatedActionPill
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSensitivityRequirements(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val ferrous by viewModel.sensitivityRequirementFerrous
    val nonFerrous by viewModel.sensitivityRequirementNonFerrous
    val stainless by viewModel.sensitivityRequirementStainless
    val engineerNotes by viewModel.sensitivityRequirementEngineerNotes

    val pvRequired by viewModel.pvRequired
    val sensitivityData = viewModel.sensitivityData.value

    // Next enabled
    val isNextStepEnabled =
        ferrous.isNotBlank() &&
                nonFerrous.isNotBlank() &&
                stainless.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Customer Sensitivity Requirements")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                // Paste M&S targets button (centred)
                if (pvRequired && sensitivityData != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AnimatedActionPill(
                            text = "Paste M&S Targets",
                            icon = Icons.Outlined.ContentPasteGo,
                            onClick = { pasteMStargetSensitivities(viewModel) }
                        )
                    }

                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Ferrous (mm)",
                    value = ferrous,
                    onValueChange = { viewModel.setSensitivityRequirementFerrous(it.replace(",", ".")) },
                    helpText = """
                        Enter the customer requirement for Ferrous sensitivity
                        
                        M&S Target: ${sensitivityData?.ferrousTargetMM?.toString() ?: "N/A"} mm
                        M&S Max: ${sensitivityData?.ferrousMaxMM?.toString() ?: "N/A"} mm
                    """.trimIndent(),
                    keyboardType = KeyboardType.Decimal,
                    maxLength = 4
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Non-Ferrous (mm)",
                    value = nonFerrous,
                    onValueChange = { viewModel.setSensitivityRequirementNonFerrous(it.replace(",", ".")) },
                    helpText = """
                        Enter the customer requirement for Non-Ferrous sensitivity
                        
                        M&S Target: ${sensitivityData?.nonFerrousTargetMM?.toString() ?: "N/A"} mm
                        M&S Max: ${sensitivityData?.nonFerrousMaxMM?.toString() ?: "N/A"} mm
                    """.trimIndent(),
                    keyboardType = KeyboardType.Decimal,
                    maxLength = 4
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Stainless Steel (mm)",
                    value = stainless,
                    onValueChange = { viewModel.setSensitivityRequirementStainless(it.replace(",", ".")) },
                    helpText = """
                        Enter the customer requirement for Stainless Steel sensitivity
                        
                        M&S Target: ${sensitivityData?.stainless316TargetMM?.toString() ?: "N/A"} mm
                        M&S Max: ${sensitivityData?.stainless316MaxMM?.toString() ?: "N/A"} mm
                    """.trimIndent(),
                    keyboardType = KeyboardType.Decimal,
                    maxLength = 4
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = engineerNotes,
                    onValueChange = viewModel::setSensitivityRequirementEngineerNotes,
                    helpText = "Enter any notes relevant to this section",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

private fun pasteMStargetSensitivities(viewModel: CalibrationMetalDetectorConveyorViewModel) {
    val data = viewModel.sensitivityData.value ?: return

    viewModel.setSensitivityRequirementFerrous(data.ferrousTargetMM.toString())
    viewModel.setSensitivityRequirementNonFerrous(data.nonFerrousTargetMM.toString())
    viewModel.setSensitivityRequirementStainless(data.stainless316TargetMM.toString())
}

