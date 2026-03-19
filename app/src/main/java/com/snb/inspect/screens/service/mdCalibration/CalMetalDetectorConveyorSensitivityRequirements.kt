package com.snb.inspect.screens.service.mdCalibration

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.formModules.AnimatedActionPill
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSensitivityRequirements(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val context = LocalContext.current
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

    // Warning Flags
    val fVal = ferrous.toDoubleOrNull() ?: 0.0
    val fMax = sensitivityData?.ferrousMaxMM ?: 0.0
    val isFerrousWarning = fVal > fMax && fVal > 0.0 && fMax > 0.0

    val nfVal = nonFerrous.toDoubleOrNull() ?: 0.0
    val nfMax = sensitivityData?.nonFerrousMaxMM ?: 0.0
    val isNonFerrousWarning = nfVal > nfMax && nfVal > 0.0 && nfMax > 0.0

    val sVal = stainless.toDoubleOrNull() ?: 0.0
    val sMax = sensitivityData?.stainless316MaxMM ?: 0.0
    val isStainlessWarning = sVal > sMax && sVal > 0.0 && sMax > 0.0

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Customer Sensitivity Requirements")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                // Paste M&S targets button (centred)
                if (pvRequired) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AnimatedActionPill(
                            text = "Paste M&S Targets",
                            icon = Icons.Outlined.ContentPasteGo,
                            onClick = { pasteMStargetSensitivities(context, viewModel) }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
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
                    isNAToggleEnabled = true,
                    maxLength = 4,
                    showInputLabel = false
                )

                if (pvRequired && isFerrousWarning) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.small)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Customer Requirement ($fVal mm) is worse than Retailer Maximum ($fMax mm).",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

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
                    isNAToggleEnabled = true,
                    maxLength = 4,
                    showInputLabel = false
                )

                if (pvRequired && isNonFerrousWarning) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.small)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Customer Requirement ($nfVal mm) is worse than Retailer Maximum ($nfMax mm).",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

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
                    isNAToggleEnabled = true,
                    maxLength = 4,
                    showInputLabel = false
                )

                if (pvRequired && isStainlessWarning) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.small)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Customer Requirement ($sVal mm) is worse than Retailer Maximum ($sMax mm).",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = engineerNotes,
                    onValueChange = viewModel::setSensitivityRequirementEngineerNotes,
                    helpText = "Enter any notes relevant to this section",
                    isNAToggleEnabled = false,
                    maxLength = 50,
                    showInputLabel = false
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Suppress("SpellCheckingInspection")
private fun pasteMStargetSensitivities(context: Context, viewModel: CalibrationMetalDetectorConveyorViewModel) {
    val data = viewModel.sensitivityData.value
    if (data == null) {
        Toast.makeText(context, "⚠️ No M&S sensitivity targets found for this system.", Toast.LENGTH_SHORT).show()
        return
    }

    viewModel.setSensitivityRequirementFerrous(data.ferrousMaxMM.toString())
    viewModel.setSensitivityRequirementNonFerrous(data.nonFerrousMaxMM.toString())
    viewModel.setSensitivityRequirementStainless(data.stainless316MaxMM.toString())
}
