package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.AppCard
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalCwTestProductDetails(viewModel: CalibrationCheckweigherViewModel) {
    val description = viewModel.productDescription.value
    val targetQn = viewModel.nominalPackageWeightQn.value
    val gross = viewModel.grossWeight.value

    val isNextStepEnabled = description.isNotBlank() || targetQn.isNotBlank() || gross.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Test Product & Operating Details", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Spacer(Modifier.height(4.dp))

                // Card 1: Product Identification
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Product Identification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        LabeledTextFieldWithHelp(
                            label = "Product / Pack Description",
                            value = viewModel.productDescription.value,
                            onValueChange = viewModel::setProductDescription,
                            helpText = "Enter description of the product or test pack (e.g. 500g Ready Meal)."
                        )

                        LabeledTextFieldWithHelp(
                            label = "Product Library / PLU Reference",
                            value = viewModel.productLibraryReference.value,
                            onValueChange = viewModel::setProductLibraryReference,
                            helpText = "Reference number or product memory index in the checkweigher library."
                        )
                    }
                }

                // Card 2: Package Weights & Target Specification
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Package Weights & Target Specification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        LabeledTextFieldWithHelp(
                            label = "Target Nominal Pack Weight Qn (g)",
                            value = viewModel.nominalPackageWeightQn.value,
                            onValueChange = viewModel::setNominalPackageWeightQn,
                            helpText = "Target package weight (Qn) used for Packaged Goods TNE / tolerance evaluations."
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = viewModel.grossWeight.value,
                                onValueChange = viewModel::setGrossWeight,
                                label = { Text("Gross Weight (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = viewModel.tareWeight.value,
                                onValueChange = viewModel::setTareWeight,
                                label = { Text("Tare Weight (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Card 3: Package Dimensions
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Package Physical Dimensions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = viewModel.productLength.value,
                                onValueChange = viewModel::setProductLength,
                                label = { Text("Length (mm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = viewModel.productWidth.value,
                                onValueChange = viewModel::setProductWidth,
                                label = { Text("Width (mm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = viewModel.productHeight.value,
                                onValueChange = viewModel::setProductHeight,
                                label = { Text("Height (mm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Card 4: Operating Conditions
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Machine Operating Conditions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = viewModel.beltSpeed.value,
                                onValueChange = viewModel::setBeltSpeed,
                                label = { Text("Belt Speed (m/min)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = viewModel.tareConfiguration.value,
                                onValueChange = viewModel::setTareConfiguration,
                                label = { Text("Tare Config (e.g. Pre-set)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        LabeledTextFieldWithHelp(
                            label = "Operating Conditions Notes",
                            value = viewModel.operatingConditionsNotes.value,
                            onValueChange = viewModel::setOperatingConditionsNotes,
                            helpText = "Additional notes on operating conditions during testing."
                        )
                    }
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
