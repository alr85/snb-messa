package com.snb.inspect.screens.service.cwCalibration

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snb.inspect.ApiService
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.ui.theme.AppCard
import com.snb.inspect.ui.theme.ScrollableWithScrollbar
import kotlinx.coroutines.launch

@Composable
fun CalCwSummary(
    viewModel: CalibrationCheckweigherViewModel,
    apiService: ApiService
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    var submitStatusMessage by remember { mutableStateOf<String?>(null) }

    val calcResult by viewModel.evaluatedCalculationResultAsFound.collectAsState()
    val isAssessmentResolved = viewModel.engineerSetPointAssessmentResolved.value
    val generalRejectResult = viewModel.generalRejectTestResult.value

    val isAssessmentBlocked = calcResult?.exceeds25PercentTneLimit == true && !isAssessmentResolved
    val isRejectFailed = generalRejectResult == "FAIL"
    val isCanSubmit = !isAssessmentBlocked && !isRejectFailed && !isSubmitting

    LaunchedEffect(Unit) {
        viewModel.setCurrentScreenNextEnabled(false)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Calibration Summary", isValid = isCanSubmit)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Spacer(Modifier.height(4.dp))

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("System Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Serial Number: ${viewModel.serialNumber.value}")
                        Text("Model: ${viewModel.modelDescription.value}")
                        Text("Location: ${viewModel.newLocation.value}")
                        Text("Type: ${viewModel.calibrationType.value}")
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Static Scale Reference", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Manufacturer: ${viewModel.staticScaleManufacturer.value.ifBlank { viewModel.staticScaleMakeModel.value }}")
                        Text("Serial: ${viewModel.staticScaleSerial.value}")
                        Text("Cert Ref: ${viewModel.staticScaleCertRef.value}")
                        Text("Suitability: ${viewModel.staticScaleSuitability.value}")
                    }
                }

                if (calcResult != null) {
                    val r = calcResult!!
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Dynamic Test Results (30 Passes)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Nominal Weight: ${r.nominalWeight}g")
                            Text("Pass Count: ${r.passCount} / 30")
                            Text("Mean (x̄): ${r.mean?.let { "%.2fg".format(it) } ?: "—"}")
                            Text("Mean Dynamic Error: ${r.meanDynamicError?.let { "%.2fg".format(it) } ?: "—"}")
                            Text("Sample Standard Deviation (s): ${r.sampleStdDev?.let { "%.3f".format(it) } ?: "—"}")
                            Text("Six Sigma (6s / ZOI): ${r.sixSigmaZoi?.let { "%.2fg".format(it) } ?: "—"}")
                            Text("TNE: ${"%.2f".format(r.tne)}g (20% Limit: ${"%.2f".format(r.benchmark20PercentTne)}g, 25% Limit: ${"%.2f".format(r.benchmark25PercentTne)}g)")
                            Text("Overall Result: ${r.overallStatus.name}", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                val eccResult by viewModel.evaluatedDynamicEccentricityResultAsFound.collectAsState()
                if (eccResult != null) {
                    val e = eccResult!!
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Dynamic Eccentricity (Off-Centre Weighing)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Test Load: ${"%.2f".format(e.referenceWeight)}g")
                            Text("Front Edge Pass: ${"%.2f".format(e.frontReading)}g (Error: ${"%+.2f".format(e.frontError)}g)")
                            Text("Back Edge Pass: ${"%.2f".format(e.backReading)}g (Error: ${"%+.2f".format(e.backError)}g)")
                            Text("Max Error: ${"%.2f".format(e.maxError)}g (Allowed Limit: ±${"%.2f".format(e.mpe)}g)")
                            Text("Tolerance Rule: ${e.toleranceDescription.ifBlank { "OIML R 51" }}")
                            Text("Assessment Result: ${if (e.isPass) "PASS" else "FAIL"}", fontWeight = FontWeight.Bold, color = if (e.isPass) Color(0xFF2E7D32) else Color(0xFFC62828))
                        }
                    }
                }

                val evaluatedStaticSets by viewModel.evaluatedStaticTestSetsAsFound.collectAsState()

                if (evaluatedStaticSets.isNotEmpty()) {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Static Weighing Test Results (${evaluatedStaticSets.size} Test${if (evaluatedStaticSets.size > 1) "s" else ""})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            evaluatedStaticSets.forEach { item ->
                                val set = item.testSet
                                val off = item.offCentreResult
                                val rep = item.repeatabilityResult
                                val weightStr = set.testWeight.ifBlank { "N/A" }

                                AppCard(
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Test #${set.id} - Target Weight: ${weightStr}g", fontWeight = FontWeight.Bold)
                                        if (off != null) {
                                            Text("• Off Centre Max Dev: ${"%.2fg".format(off.maxDeviation)} (${off.maxCornerName}) - ${if (off.isPass) "PASS" else "FAIL"}")
                                        }
                                        if (rep != null) {
                                            Text("• Repeatability Mean: ${"%.2fg".format(rep.mean)} | Range: ${"%.2fg".format(rep.range)} - ${if (rep.isPass) "PASS" else "FAIL"}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Reject Verification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("General Reject Test: ${viewModel.generalRejectTestResult.value}")
                        if (viewModel.generalRejectNotes.value.isNotBlank()) {
                            Text("Notes: ${viewModel.generalRejectNotes.value}")
                        }
                    }
                }

                if (isAssessmentBlocked) {
                    Surface(color = Color(0xFFFFEBEE), shape = MaterialTheme.shapes.medium) {
                        Text(
                            "❌ Cannot complete calibration: 6σ exceeds 25% TNE threshold and set-point assessment is not resolved.",
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (submitStatusMessage != null) {
                    Text(
                        text = submitStatusMessage!!,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = {
                        isSubmitting = true
                        scope.launch {
                            viewModel.finaliseCalibrationAndUpload(context, apiService) { msg ->
                                isSubmitting = false
                                submitStatusMessage = msg
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = isCanSubmit,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Finalise & Sync Calibration")
                    }
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
