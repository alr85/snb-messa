package com.snb.inspect.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snb.inspect.ui.theme.SnbDarkGrey
import com.snb.inspect.ui.theme.SnbRed
import java.util.Locale

@Composable
fun CheckweigherSpeedCalculatorScreen() {
    var platformLength by remember { mutableStateOf("") }
    var productLength by remember { mutableStateOf("") }
    var packsPerMinute by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Results container
    var calculatedBeltSpeed by remember { mutableStateOf<Double?>(null) }
    var sampleDelayMs by remember { mutableStateOf<Double?>(null) }
    var sampleTimeMs by remember { mutableStateOf<Double?>(null) }
    var productPitchVal by remember { mutableStateOf<Double?>(null) }

    // Update calculations reactively
    LaunchedEffect(platformLength, productLength, packsPerMinute) {
        val platformLenVal = platformLength.toDoubleOrNull()
        val productLenVal = productLength.toDoubleOrNull()
        val ppmVal = packsPerMinute.toDoubleOrNull()

        if (platformLenVal != null && productLenVal != null && ppmVal != null) {
            val pitch = platformLenVal + productLenVal
            val speed = (pitch * ppmVal) / 1000.0
            
            productPitchVal = pitch
            calculatedBeltSpeed = speed

            if (speed > 0) {
                val speedMmPerSec = (speed * 1000.0) / 60.0
                sampleDelayMs = (productLenVal / speedMmPerSec) * 1000.0
                
                val distanceForFullPack = platformLenVal - productLenVal
                sampleTimeMs = if (distanceForFullPack > 0) {
                    (distanceForFullPack / speedMmPerSec) * 1000.0
                } else 0.0
            }
        } else {
            calculatedBeltSpeed = null
            sampleDelayMs = null
            sampleTimeMs = null
            productPitchVal = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Speed, null, tint = SnbRed, modifier = Modifier.size(32.dp))
            Text("Speed Calculator", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "Enter parameters below to calculate speed and timings.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFFBFBFB))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SpeedInputField("Platform Length (mm)", platformLength) { platformLength = it.filterNumberish() }
                SpeedInputField("Product Length (mm)", productLength) { productLength = it.filterNumberish() }
                SpeedInputField("Packs Per Minute (PPM)", packsPerMinute) { packsPerMinute = it.filterNumberish() }
            }
        }

        if (calculatedBeltSpeed != null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFF0F4F8))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Calculation Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    ResultRow("Calculated Belt Speed", String.format(Locale.US, "%.2f m/m", calculatedBeltSpeed))
                    
                    sampleDelayMs?.let { delay ->
                        Column {
                            ResultRow("Theoretical Sample Delay", String.format(Locale.US, "%.0f ms", delay))
                            Text(
                                String.format(Locale.US, "incl. +10%% Tolerance: %.0f ms", delay * 1.1),
                                style = MaterialTheme.typography.bodySmall, color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End
                            )
                        }
                    }

                    sampleTimeMs?.let { time ->
                        Column {
                            ResultRow("Theoretical Sample Time", String.format(Locale.US, "%.0f ms", time))
                            Text(
                                String.format(Locale.US, "incl. -10%% Tolerance: %.0f ms", time * 0.9),
                                style = MaterialTheme.typography.bodySmall, color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End
                            )
                        }
                        
                        val platLenVal = platformLength.toDoubleOrNull() ?: 0.0
                        val prodLenVal = productLength.toDoubleOrNull() ?: 0.0
                        if (time <= 0 && platLenVal > 0 && prodLenVal > 0) {
                            Text(
                                "⚠️ Product (${prodLenVal.toInt()}mm) is longer than platform (${platLenVal.toInt()}mm)!",
                                color = SnbRed, style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    productPitchVal?.let { ResultRow("Product Pitch", "${it.toInt()} mm") }
                }
            }
        } else {
            Surface(
                color = Color(0xFFF5F5F5),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Enter all 3 fields to see results",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = "Note: These calculations do not consider transfer problems, belt slippage, settling times, or filter lag and should be used as a guide only.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontStyle = FontStyle.Italic, lineHeight = 14.sp),
            color = Color.Gray, modifier = Modifier.padding(horizontal = 4.dp)
        )

        OutlinedButton(
            onClick = {
                platformLength = ""
                productLength = ""
                packsPerMinute = ""
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SnbRed)
        ) {
            Icon(Icons.Default.Clear, null)
            Spacer(Modifier.width(8.dp))
            Text("Clear All Fields")
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SnbRed)
    }
}

@Composable
private fun SpeedInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true, modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SnbRed, unfocusedBorderColor = SnbDarkGrey,
            focusedLabelColor = SnbRed, unfocusedLabelColor = SnbDarkGrey, cursorColor = SnbRed
        )
    )
}

private fun String.filterNumberish(): String {
    val trimmed = this.trim()
    if (trimmed.isEmpty()) return ""
    var dotUsed = false
    val sb = StringBuilder()
    trimmed.forEach { c ->
        if (c.isDigit()) sb.append(c)
        else if (c == '.' && !dotUsed) { dotUsed = true; sb.append(c) }
    }
    return sb.toString()
}
