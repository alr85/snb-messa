package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.SnbDarkGrey
import com.snb.inspect.ui.theme.SnbRed
import com.snb.inspect.ui.theme.ScrollableWithScrollbar
import com.snb.inspect.util.CheckweigherAccuracyCalculator
import com.snb.inspect.util.CheckweigherAccuracyResult
import com.snb.inspect.util.PassFail

@Composable
fun CalCwDynamicTestAsFound(viewModel: CalibrationCheckweigherViewModel) {
    val result by viewModel.dynamicAccuracyResultAsFound.collectAsState(initial = null)
    val passes = viewModel.dynamicPassesAsFound.value

    val isNextStepEnabled = result?.zoiPassFail == PassFail.Pass && result?.knownMassPassFail == PassFail.Pass

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Dynamic Test (As Found)", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Spacer(Modifier.height(4.dp))

                NominalInputCard(
                    tNomText = viewModel.nominalQuantityAsFound.value,
                    onTNomChange = viewModel::setNominalQuantityAsFound,
                    result = result
                )

                ZoiSection(
                    passTexts = passes,
                    onPassChange = viewModel::setDynamicPassAsFound,
                    result = result
                )

                KnownMassSection(
                    staticScale = viewModel.staticScaleWeightAsFound.value,
                    checkweigher = viewModel.checkweigherWeightAsFound.value,
                    onStaticChange = viewModel::setStaticScaleWeightAsFound,
                    onCheckweigherChange = viewModel::setCheckweigherWeightAsFound,
                    result = result
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

// Reused components from CheckweigherAccuracyScreen adapted for Calibration

@Composable
private fun NominalInputCard(tNomText: String, onTNomChange: (String) -> Unit, result: CheckweigherAccuracyResult?) {
    val tNomVal = tNomText.toDoubleOrNull() ?: 0.0
    val isTooLow = tNomText.isNotBlank() && tNomVal < 5.0
    ElevatedCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Nominal Quantity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Calculate, contentDescription = null)
            }
            OutlinedTextField(
                value = tNomText,
                onValueChange = onTNomChange,
                label = { Text("T-Nom (g / ml)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = isTooLow,
                modifier = Modifier.fillMaxWidth()
            )
            if (isTooLow) Text("⚠️ Nominal weight must be 5g or greater.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            else if (result != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ValuePill(title = "TNE", value = "${result.tne}g", modifier = Modifier.weight(1f))
                    ValuePill(title = "T1", value = "${result.t1}g", modifier = Modifier.weight(1f))
                    ValuePill(title = "T2", value = "${result.t2}g", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ZoiSection(passTexts: List<String>, onPassChange: (Int, String) -> Unit, result: CheckweigherAccuracyResult?) {
    ElevatedCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Zone of Indecision (ZOI)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Enter 10 dynamic passes (g/ml)", style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (i in 0 until 5) PassField(label = "Pass ${i + 1}", value = passTexts[i], onChange = { onPassChange(i, it) })
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (i in 5 until 10) PassField(label = "Pass ${i + 1}", value = passTexts[i], onChange = { onPassChange(i, it) })
                }
            }
            if (result != null) ZoiSummary(result)
        }
    }
}

@Composable
private fun KnownMassSection(staticScale: String, checkweigher: String, onStaticChange: (String) -> Unit, onCheckweigherChange: (String) -> Unit, result: CheckweigherAccuracyResult?) {
    ElevatedCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Known Mass Check", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = staticScale, onValueChange = onStaticChange, label = { Text("Static (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                OutlinedTextField(value = checkweigher, onValueChange = onCheckweigherChange, label = { Text("CW (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
            }
            if (result != null) KnownMassSummary(result)
        }
    }
}

@Composable
private fun PassField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun ValuePill(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(tonalElevation = 2.dp, shape = MaterialTheme.shapes.large, modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ZoiSummary(r: CheckweigherAccuracyResult) {
    val threshold = r.tne * 0.25
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ValueMini(title = "SD", value = r.sd?.toString() ?: "—", modifier = Modifier.weight(1f))
            ValueMini(title = "ZOI", value = r.zoi?.let { "%.2f".format(it) } ?: "—", modifier = Modifier.weight(1f))
            ValueMini(title = "Limit", value = "%.2f".format(threshold), modifier = Modifier.weight(1f))
        }
        BigResultBanner(r.zoiPassFail, "ZOI result")
    }
}

@Composable
private fun KnownMassSummary(r: CheckweigherAccuracyResult) {
    val threshold = r.tne / 5.0
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ValueMini(title = "Limit (±)", value = "%.2f".format(threshold), modifier = Modifier.fillMaxWidth())
        BigResultBanner(r.knownMassPassFail, "Known Mass result")
    }
}

@Composable
private fun ValueMini(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(tonalElevation = 1.dp, shape = MaterialTheme.shapes.large, modifier = modifier) {
        Column(Modifier.padding(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BigResultBanner(passFail: PassFail?, text: String) {
    val bgColor = when (passFail) {
        PassFail.Pass -> Color(0xFFE8F5E9)
        PassFail.Fail -> Color(0xFFFFEBEE)
        null -> Color(0xFFF5F5F5)
    }
    val contentColor = when (passFail) {
        PassFail.Pass -> Color(0xFF2E7D32)
        PassFail.Fail -> Color(0xFFC62828)
        null -> Color.Gray
    }
    Surface(color = bgColor, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Text(text = when(passFail) {
            PassFail.Pass -> "✅ $text Passed"
            PassFail.Fail -> "❌ $text Failed"
            null -> "Awaiting Data"
        }, color = contentColor, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    }
}
