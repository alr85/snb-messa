package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel

@Composable
fun SovSummaryDetails(
    viewModel: SensitivityOptimisationValidationViewModel,
    isConfirmationMode: Boolean = false,
    confirmedSections: Map<String, Boolean> = emptyMap(),
    onSectionConfirmChange: (String, Boolean) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // Title
        Text(
            text = "Validation Summary",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Helper function to display a section
        @Composable
        fun Section(
            title: String,
            forceShowCheckbox: Boolean = true,
            content: @Composable ColumnScope.() -> Unit
        ) {
            val isConfirmed = confirmedSections[title] ?: false

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Section title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isConfirmationMode && forceShowCheckbox && !isConfirmed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Section content
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isConfirmationMode && forceShowCheckbox && isConfirmed) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        content = content
                    )

                    if (isConfirmationMode && forceShowCheckbox) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                .clickable { onSectionConfirmChange(title, !isConfirmed) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isConfirmed,
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "I have verified the $title values",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Section(title = "Validation Details") {
            SummaryItem(label = "Product", value = viewModel.productDescription.value)
            SummaryItem(label = "Library Ref", value = viewModel.productLibraryReference.value)
            SummaryItem(label = "Belt Speed", value = viewModel.beltSpeed.value)
            SummaryItem(label = "Current Location", value = viewModel.lastLocation.value)
            if (viewModel.newLocation.value != viewModel.lastLocation.value) {
                SummaryItem(label = "New Location", value = viewModel.newLocation.value)
            }
        }

        Section(title = "Product Dimensions") {
            SummaryItem(label = "Length (mm)", value = viewModel.productLength.value)
            SummaryItem(label = "Width (mm)", value = viewModel.productWidth.value)
            SummaryItem(label = "Height (mm)", value = viewModel.productHeight.value)
            SummaryItem(label = "Weight (g)", value = viewModel.productWeight.value)
        }

        Section(title = "Sensitivity (As Left)") {
            SummaryItem(label = "Ferrous", value = viewModel.sensitivityAsLeftFerrous.value)
            SummaryItem(label = "Non-Ferrous", value = viewModel.sensitivityAsLeftNonFerrous.value)
            SummaryItem(label = "Stainless", value = viewModel.sensitivityAsLeftStainless.value)
        }

        Section(title = "Validation Results") {
            val val1TotalPasses = (viewModel.val1LeadingPasses.value.toIntOrNull() ?: 0) + (viewModel.val1MiddlePasses.value.toIntOrNull() ?: 0) + (viewModel.val1TrailingPasses.value.toIntOrNull() ?: 0)
            val val1TotalSuccesses = (viewModel.val1LeadingSuccesses.value.toIntOrNull() ?: 0) + (viewModel.val1MiddleSuccesses.value.toIntOrNull() ?: 0) + (viewModel.val1TrailingSuccesses.value.toIntOrNull() ?: 0)
            SummaryItem(label = viewModel.validationTest1Description.value, value = "$val1TotalSuccesses / $val1TotalPasses")

            val val2TotalPasses = (viewModel.val2LeadingPasses.value.toIntOrNull() ?: 0) + (viewModel.val2MiddlePasses.value.toIntOrNull() ?: 0) + (viewModel.val2TrailingPasses.value.toIntOrNull() ?: 0)
            val val2TotalSuccesses = (viewModel.val2LeadingSuccesses.value.toIntOrNull() ?: 0) + (viewModel.val2MiddleSuccesses.value.toIntOrNull() ?: 0) + (viewModel.val2TrailingSuccesses.value.toIntOrNull() ?: 0)
            SummaryItem(label = viewModel.validationTest2Description.value, value = "$val2TotalSuccesses / $val2TotalPasses")

            val val3TotalPasses = (viewModel.val3LeadingPasses.value.toIntOrNull() ?: 0) + (viewModel.val3MiddlePasses.value.toIntOrNull() ?: 0) + (viewModel.val3TrailingPasses.value.toIntOrNull() ?: 0)
            val val3TotalSuccesses = (viewModel.val3LeadingSuccesses.value.toIntOrNull() ?: 0) + (viewModel.val3MiddleSuccesses.value.toIntOrNull() ?: 0) + (viewModel.val3TrailingSuccesses.value.toIntOrNull() ?: 0)
            SummaryItem(label = viewModel.validationTest3Description.value, value = "$val3TotalSuccesses / $val3TotalPasses")
        }

        Section(title = "Pack Validation") {
            SummaryItem(label = "30 Good Packs Stability Test", value = if (viewModel.packValidationPassed.value) "PASSED" else "NOT COMPLETED")
        }

        if (viewModel.sensitivityAsLeftFerrous.value != "N/A" || viewModel.sensitivityAsLeftNonFerrous.value != "N/A" || viewModel.sensitivityAsLeftStainless.value != "N/A") {
            Section(title = "Lowest Signals") {
                if (viewModel.sensitivityAsLeftFerrous.value != "N/A") {
                    val ferrousSignal = if (viewModel.system.systemTypeId == 1)
                        "${viewModel.minSignalAsLeftFerrousLeading.value} / ${viewModel.minSignalAsLeftFerrousMiddle.value} / ${viewModel.minSignalAsLeftFerrousTrailing.value}"
                    else viewModel.minSignalAsLeftFerrousLeading.value
                    SummaryItem(label = "Ferrous (L/M/T)", value = ferrousSignal)
                }

                if (viewModel.sensitivityAsLeftNonFerrous.value != "N/A") {
                    val nonFerrousSignal = if (viewModel.system.systemTypeId == 1)
                        "${viewModel.minSignalAsLeftNonFerrousLeading.value} / ${viewModel.minSignalAsLeftNonFerrousMiddle.value} / ${viewModel.minSignalAsLeftNonFerrousTrailing.value}"
                    else viewModel.minSignalAsLeftNonFerrousLeading.value
                    SummaryItem(label = "Non-Ferrous (L/M/T)", value = nonFerrousSignal)
                }

                if (viewModel.sensitivityAsLeftStainless.value != "N/A") {
                    val stainlessSignal = if (viewModel.system.systemTypeId == 1)
                        "${viewModel.minSignalAsLeftStainlessLeading.value} / ${viewModel.minSignalAsLeftStainlessMiddle.value} / ${viewModel.minSignalAsLeftStainlessTrailing.value}"
                    else viewModel.minSignalAsLeftStainlessLeading.value
                    SummaryItem(label = "Stainless (L/M/T)", value = stainlessSignal)
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}
