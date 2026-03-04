package com.example.mecca.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mecca.calibrationViewModels.CheckweigherAccuracyViewModel
import com.example.mecca.ui.theme.SnbDarkGrey
import com.example.mecca.ui.theme.SnbRed
import com.example.mecca.util.CheckweigherAccuracyResult
import com.example.mecca.util.PassFail

@Composable
fun CheckweigherAccuracyScreen(
    vm: CheckweigherAccuracyViewModel = viewModel()
) {
    val inputs by vm.inputs.collectAsState()
    val result by vm.result.collectAsState()

    val scroll = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Header()

        NominalInputCard(
            tNomText = inputs.tNomText,
            onTNomChange = vm::setTNom,
            result = result
        )

        ZoiSection(
            passTexts = inputs.passTexts,
            onPassChange = vm::setPass,
            result = result
        )

        KnownMassSection(
            staticScale = inputs.staticScaleText,
            checkweigher = inputs.checkweigherText,
            onStaticChange = vm::setStaticScale,
            onCheckweigherChange = vm::setCheckweigher,
            result = result
        )

        FooterActions(
            onClear = vm::clearAll
        )
    }
}

@Composable
private fun Header() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(Modifier.weight(1f)) {
            Text(
                text = "Dynamic Accuracy Test",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun NominalInputCard(
    tNomText: String,
    onTNomChange: (String) -> Unit,
    result: CheckweigherAccuracyResult?
) {
    val tNomVal = tNomText.toDoubleOrNull() ?: 0.0
    val isTooLow = tNomText.isNotBlank() && tNomVal < 5.0

    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Nominal Quantity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.Calculate, contentDescription = null)
            }

            OutlinedTextField(
                value = tNomText,
                onValueChange = { onTNomChange(it.filterNumberish()) },
                label = { Text("T-Nom (g / ml)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = isTooLow,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SnbRed,
                    unfocusedBorderColor = SnbDarkGrey,
                    focusedLabelColor = SnbRed,
                    unfocusedLabelColor = SnbDarkGrey,
                    cursorColor = SnbRed,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error
                )
            )

            if (isTooLow) {
                Assistive("⚠️ Nominal weight must be 5g or greater.", color = MaterialTheme.colorScheme.error)
            } else if (result == null) {
                Assistive("Enter a valid T-Nom to compute TNE/T1/T2.")
            } else {
                ResultGrid(
                    tne = result.tne,
                    t1 = result.t1,
                    t2 = result.t2,
                    ruleText = safeRuleText(result.tNom)
                )
            }
        }
    }
}

@Composable
private fun ResultGrid(
    tne: Double,
    t1: Double,
    t2: Double,
    ruleText: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ValuePill(title = "TNE", value = "${tne}g", modifier = Modifier.weight(1f))
            ValuePill(title = "T1", value = "${t1}g", modifier = Modifier.weight(1f))
            ValuePill(title = "T2", value = "${t2}g", modifier = Modifier.weight(1f))
        }
        Assistive(ruleText)
    }
}

@Composable
private fun ValuePill(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ZoiSection(
    passTexts: List<String>,
    onPassChange: (Int, String) -> Unit,
    result: CheckweigherAccuracyResult?
) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    "Zone of Indecision (ZOI)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Enter 10 dynamic passes (g/ml)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (i in 0 until 5) {
                        PassField(
                            label = "Pass ${i + 1}",
                            value = passTexts[i],
                            onChange = { onPassChange(i, it.filterNumberish()) }
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (i in 5 until 10) {
                        PassField(
                            label = "Pass ${i + 1}",
                            value = passTexts[i],
                            onChange = { onPassChange(i, it.filterNumberish()) }
                        )
                    }
                }
            }

            if (result != null) {
                ZoiSummary(result)
            } else {
                BigResultBanner(null, "ZOI results will appear once 10 passes are entered.")
            }
        }
    }
}

@Composable
private fun PassField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SnbRed,
            unfocusedBorderColor = SnbDarkGrey,
            focusedLabelColor = SnbRed,
            unfocusedLabelColor = SnbDarkGrey,
            cursorColor = SnbRed
        )
    )
}

@Composable
private fun ZoiSummary(r: CheckweigherAccuracyResult) {
    val sdText = r.sd?.toString() ?: "—"
    val zoiText = r.zoi?.let { roundToUi(it, 2) } ?: "—"
    
    val threshold = r.tne * 0.25
    val pass = r.zoiPassFail

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ValueMini(title = "SD", value = sdText, modifier = Modifier.weight(1f))
            ValueMini(title = "ZOI (SD×6)", value = zoiText, modifier = Modifier.weight(1f))
            ValueMini(title = "Limit (TNE×0.25)", value = roundToUi(threshold, 2), modifier = Modifier.weight(1f))
        }

        BigResultBanner(
            passFail = pass,
            awaitingText = "Complete all 10 passes to see ZOI result.",
            passText = "ZOI is within the allowable limit.",
            failText = "ZOI exceeds the 0.25 × TNE limit."
        )
    }
}

@Composable
private fun ValueMini(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun KnownMassSection(
    staticScale: String,
    checkweigher: String,
    onStaticChange: (String) -> Unit,
    onCheckweigherChange: (String) -> Unit,
    result: CheckweigherAccuracyResult?
) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Known Mass Check", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = staticScale,
                    onValueChange = { onStaticChange(it.filterNumberish()) },
                    label = { Text("Static scale (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SnbRed,
                        unfocusedBorderColor = SnbDarkGrey,
                        focusedLabelColor = SnbRed,
                        unfocusedLabelColor = SnbDarkGrey,
                        cursorColor = SnbRed
                    )
                )
                OutlinedTextField(
                    value = checkweigher,
                    onValueChange = { onCheckweigherChange(it.filterNumberish()) },
                    label = { Text("Checkweigher (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SnbRed,
                        unfocusedBorderColor = SnbDarkGrey,
                        focusedLabelColor = SnbRed,
                        unfocusedLabelColor = SnbDarkGrey,
                        cursorColor = SnbRed
                    )
                )
            }

            if (result != null) {
                KnownMassSummary(result)
            } else {
                BigResultBanner(null, "Awaiting valid T-Nom.")
            }
        }
    }
}

@Composable
private fun KnownMassSummary(r: CheckweigherAccuracyResult) {
    val pass = r.knownMassPassFail
    val threshold = r.tne / 5.0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ValueMini(title = "Limit (TNE ÷ 5)", value = "±${roundToUi(threshold, 2)}g", modifier = Modifier.weight(1f))
        }

        BigResultBanner(
            passFail = pass,
            awaitingText = "Enter both scale values to see result.",
            passText = "Scale difference is within allowable limit.",
            failText = "Scale difference exceeds the TNE ÷ 5 limit."
        )
    }
}

@Composable
private fun BigResultBanner(
    passFail: PassFail?,
    awaitingText: String,
    passText: String = "",
    failText: String = ""
) {
    val bgColor = when (passFail) {
        PassFail.Pass -> Color(0xFFE8F5E9) // Very light green
        PassFail.Fail -> Color(0xFFFFEBEE) // Very light red
        null -> Color(0xFFF5F5F5)          // Very light grey
    }
    
    val contentColor = when (passFail) {
        PassFail.Pass -> Color(0xFF2E7D32) // Dark green
        PassFail.Fail -> Color(0xFFC62828) // Dark red
        null -> Color.Gray
    }

    val label = when (passFail) {
        PassFail.Pass -> "✅ TEST PASSED"
        PassFail.Fail -> "❌ TEST FAILED"
        null -> "AWAITING DATA"
    }

    val description = when (passFail) {
        PassFail.Pass -> passText
        PassFail.Fail -> failText
        null -> awaitingText
    }

    Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium
            )
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    color = contentColor,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FooterActions(
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onClear,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Clear, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Clear All")
        }
    }
}

@Composable
private fun Assistive(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color
    )
}

/* ---------------------------
   Helpers
---------------------------- */

private fun String.filterNumberish(): String {
    val trimmed = this.trim()
    if (trimmed.isEmpty()) return ""
    var dotUsed = false
    val sb = StringBuilder()
    trimmed.forEachIndexed { idx, c ->
        when {
            c.isDigit() -> sb.append(c)
            c == '.' && !dotUsed -> {
                dotUsed = true
                sb.append(c)
            }
            c == '-' && idx == 0 -> sb.append(c)
        }
    }
    return sb.toString()
}

private fun safeRuleText(tNom: Double): String {
    val calc = com.example.mecca.util.CheckweigherAccuracyCalculator
    return calc.tneRuleText(tNom)
}

private fun roundToUi(x: Double, dp: Int): String {
    return "%.${dp}f".format(x)
}
