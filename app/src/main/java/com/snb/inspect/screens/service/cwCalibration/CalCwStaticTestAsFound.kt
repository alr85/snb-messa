package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.ui.theme.AppCard
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalCwStaticTestAsFound(viewModel: CalibrationCheckweigherViewModel) {
    val maxCap = viewModel.maxCapacity.value.toDoubleOrNull() ?: 1000.0
    val limits by viewModel.evaluatedToleranceLimits.collectAsState()

    val linearityResult by viewModel.evaluatedStaticLinearityResult.collectAsState()
    val eccentricityResult by viewModel.evaluatedStaticEccentricityResult.collectAsState()

    val refWeights = viewModel.staticLinAppliedRefWeights.value
    val dispReadings = viewModel.staticLinDisplayedReadings.value

    val isLinearityComplete = (0..4).all { idx ->
        dispReadings.getOrNull(idx)?.isNotBlank() == true
    }

    val isEccentricityComplete = viewModel.eccentricityTestWeight.value.isNotBlank() &&
            viewModel.eccentricityCentreReading.value.isNotBlank() &&
            viewModel.eccentricityFrontLeftReading.value.isNotBlank() &&
            viewModel.eccentricityFrontRightReading.value.isNotBlank() &&
            viewModel.eccentricityRearLeftReading.value.isNotBlank() &&
            viewModel.eccentricityRearRightReading.value.isNotBlank()

    val isNextStepEnabled = isLinearityComplete && isEccentricityComplete

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(
            label = "Static Linearity & Eccentricity Tests",
            isValid = isNextStepEnabled
        )

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(Modifier.height(4.dp))

                // Section 1: Static Linearity Test (5 Points)
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LinearScale, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "1. Static Linearity Test (5 Points)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            if (linearityResult != null) {
                                StatusBadge(isPass = linearityResult!!.isOverallPass)
                            }
                        }

                        Text(
                            "Test 5 points across scale capacity (0%, 25%, 50%, 75%, 100% of Max Cap ${"%.1f".format(maxCap)}g). Allowed MPE: ±${"%.2f".format(limits.allowedStaticMpe)}g",
                            style = MaterialTheme.typography.bodySmall
                        )

                        val percentages = listOf(0, 25, 50, 75, 100)

                        for (i in 0..4) {
                            val pct = percentages[i]
                            val targetRef = (pct / 100.0) * maxCap
                            val appliedVal = refWeights.getOrNull(i)?.ifBlank { "%.1f".format(targetRef) } ?: "%.1f".format(targetRef)
                            val dispVal = dispReadings.getOrNull(i) ?: ""
                            val pointRes = linearityResult?.points?.getOrNull(i)

                            LinearityPointRow(
                                pointIndex = i + 1,
                                percent = pct,
                                targetRef = targetRef,
                                appliedVal = appliedVal,
                                onAppliedChange = { viewModel.setStaticLinAppliedRefWeight(i, it) },
                                dispVal = dispVal,
                                onDispChange = { viewModel.setStaticLinDisplayedReading(i, it) },
                                pointResult = pointRes
                            )
                        }
                    }
                }

                // Section 2: Static Eccentricity (Off-Centre) Test (5 Positions)
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "2. Static Eccentricity (Off-Centre) Test",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            if (eccentricityResult != null) {
                                StatusBadge(isPass = eccentricityResult!!.isPass)
                            }
                        }

                        Text(
                            "Place test load (~1/3 max cap) at Centre and 4 corners. Allowed MPE: ±${"%.2f".format(limits.allowedStaticMpe)}g",
                            style = MaterialTheme.typography.bodySmall
                        )

                        OutlinedTextField(
                            value = viewModel.eccentricityTestWeight.value,
                            onValueChange = viewModel::setEccentricityTestWeight,
                            label = { Text("Applied Test Weight (g) [~${"%.0f".format(maxCap / 3.0)}g]") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = viewModel.eccentricityCentreReading.value,
                            onValueChange = viewModel::setEccentricityCentreReading,
                            label = { Text("Centre Position Reading (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Corner Position Readings:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = viewModel.eccentricityFrontLeftReading.value,
                                onValueChange = viewModel::setEccentricityFrontLeftReading,
                                label = { Text("Front-Left (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = viewModel.eccentricityFrontRightReading.value,
                                onValueChange = viewModel::setEccentricityFrontRightReading,
                                label = { Text("Front-Right (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = viewModel.eccentricityRearLeftReading.value,
                                onValueChange = viewModel::setEccentricityRearLeftReading,
                                label = { Text("Rear-Left (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = viewModel.eccentricityRearRightReading.value,
                                onValueChange = viewModel::setEccentricityRearRightReading,
                                label = { Text("Rear-Right (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (eccentricityResult != null) {
                            val res = eccentricityResult!!
                            Surface(
                                color = if (res.isPass) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Max Corner Dev: ${"%.2f".format(res.maxCornerDeviation)}g (${res.maxCornerName})",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "Allowed Limit: ±${"%.2f".format(res.allowedLimit)}g",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    StatusBadge(isPass = res.isPass)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun LinearityPointRow(
    pointIndex: Int,
    percent: Int,
    targetRef: Double,
    appliedVal: String,
    onAppliedChange: (String) -> Unit,
    dispVal: String,
    onDispChange: (String) -> Unit,
    pointResult: com.snb.inspect.util.StaticLinearityPointResult?
) {
    AppCard(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pt $pointIndex ($percent% Cap = ${"%.1f".format(targetRef)}g)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                if (pointResult != null) {
                    Text(
                        "Error: ${"%.2f".format(pointResult.error)}g",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (pointResult.isPass) Color(0xFF2E7D32) else Color.Red
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = appliedVal,
                    onValueChange = onAppliedChange,
                    label = { Text("Ref Load (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = dispVal,
                    onValueChange = onDispChange,
                    label = { Text("Displayed (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(isPass: Boolean) {
    Surface(
        color = if (isPass) Color(0xFF2E7D32) else Color(0xFFC62828),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = if (isPass) "PASS" else "FAIL",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
