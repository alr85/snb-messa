package com.snb.inspect.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.snb.inspect.dataClasses.MetalDetectorConveyorCalibrationLocal
import com.snb.inspect.formatDate
import com.snb.inspect.ui.theme.SnbRed

@Composable
fun CalibrationDetailDialog(
    calibration: GenericCalibration,
    customerName: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Calibration View",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = customerName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .background(Color(0xFFF8F9FA)) // Light grey background like My Calibrations
                        .padding(16.dp)
                ) {
                    when (calibration.type) {
                        SystemCategory.METAL_DETECTOR -> {
                            calibration.rawMD?.let { 
                                MetalDetectorCalibrationDetailsView(it, customerName)
                            } ?: Text("Error: MD data missing")
                        }
                        SystemCategory.CHECKWEIGHER -> {
                            Text("Checkweigher details coming soon...")
                        }
                        else -> {
                            Text("Details for ${calibration.type.label} not implemented.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetalDetectorCalibrationDetailsView(
    cal: MetalDetectorConveyorCalibrationLocal,
    customerName: String
) {
    fun formatMm(value: String): String {
        return if (value.isNotBlank() && value != "N/A") "${value}mm" else value
    }

    val canPerform = cal.canPerformCalibration.lowercase().let { it == "true" || it == "yes" }

    DetailSection(title = "Calibration Details") {
        DetailRow(label = "Calibration ID", value = cal.calibrationId)
        DetailRow(label = "Engineer ID", value = cal.engineerId.toString())
        DetailRow(label = "Start Time", value = try { formatDate(cal.startDate) } catch (_: Exception) { cal.startDate })
    }

    DetailSection(title = "System Details") {
        DetailRow(label = "Customer", value = customerName)
        DetailRow(label = "Serial Number", value = cal.serialNumber)
        DetailRow(label = "System Location", value = cal.lastLocation)
        DetailRow(label = "New Location", value = cal.newLocation)
        DetailRow(label = "PV required", value = if (cal.pvRequired) "Yes" else "No")
        DetailRow(label = "Able to calibrate", value = cal.canPerformCalibration)
        
        if (!canPerform && cal.reasonForNotCalibrating.isNotBlank()) {
            DetailRow(label = "Reason", value = cal.reasonForNotCalibrating)
        }
    }

    if (canPerform) {
        DetailSection(title = "Conveyor Details") {
            DetailRow(label = "Belt Speed", value = cal.beltSpeed)
            val rejectDeviceDisplay = if (cal.rejectDevice == "Other" && cal.rejectDeviceOther.isNotBlank()) {
                cal.rejectDeviceOther
            } else {
                cal.rejectDevice
            }
            DetailRow(label = "Reject System", value = rejectDeviceDisplay)
            DetailRow(label = "Engineer Notes", value = cal.conveyorDetailsEngineerNotes)
        }

        DetailSection(title = "System Checklist") {
            DetailRow(label = "Conveyor Belt", value = "${cal.beltCondition} - ${cal.beltConditionComments}")
            DetailRow(label = "Guarding", value = "${cal.guardCondition} - ${cal.guardConditionComments}")
            DetailRow(label = "Safety Circuit", value = "${cal.safetyCircuitCondition} - ${cal.safetyCircuitConditionComments}")
            DetailRow(label = "Detector Lining, gaskets and seals", value = "${cal.linerCondition} - ${cal.linerConditionComments}")
            DetailRow(label = "Cables Fittings", value = "${cal.cablesCondition} - ${cal.cablesConditionComments}")
            DetailRow(label = "Screws and Fittings", value = "${cal.screwsCondition} - ${cal.screwsConditionComments}")
            DetailRow(label = "Engineer Notes", value = cal.systemChecklistEngineerNotes)
        }

        DetailSection(title = "Indicators") {
            DetailRow(label = "Indicator 6", value = "${cal.indicator6label} (${cal.indicator6colour})")
            DetailRow(label = "Indicator 5", value = "${cal.indicator5label} (${cal.indicator5colour})")
            DetailRow(label = "Indicator 4", value = "${cal.indicator4label} (${cal.indicator4colour})")
            DetailRow(label = "Indicator 3", value = "${cal.indicator3label} (${cal.indicator3colour})")
            DetailRow(label = "Indicator 2", value = "${cal.indicator2label} (${cal.indicator2colour})")
            DetailRow(label = "Indicator 1", value = "${cal.indicator1label} (${cal.indicator1colour})")
            DetailRow(label = "Engineer Notes", value = cal.indicatorsEngineerNotes)
        }

        DetailSection(title = "Product Details") {
            DetailRow(label = "Product Description", value = cal.productDescription)
            DetailRow(label = "Product Library Reference", value = cal.productLibraryReference)
            DetailRow(label = "Product Library Number", value = cal.productLibraryNumber)
            DetailRow(label = "Product Length", value = cal.productLength)
            DetailRow(label = "Product Width", value = cal.productWidth)
            DetailRow(label = "Product Height", value = cal.productHeight)
            DetailRow(label = "Engineer Notes", value = cal.productDetailsEngineerNotes)
        }

        DetailSection(title = "Ferrous Sensitivity (As Found)") {
            DetailRow(label = "Sensitivity", value = formatMm(cal.sensitivityAsFoundFerrous))
            DetailRow(label = "Cert Number", value = cal.sampleCertificateNumberAsFoundFerrous)
            DetailRow(label = "Detect/Reject Leading", value = "${cal.detectRejectAsFoundFerrousLeading} (${cal.peakSignalAsFoundFerrousLeading})")
            DetailRow(label = "Detect/Reject Middle", value = "${cal.detectRejectAsFoundFerrousMiddle} (${cal.peakSignalAsFoundFerrousMiddle})")
            DetailRow(label = "Detect/Reject Trailing", value = "${cal.detectRejectAsFoundFerrousTrailing} (${cal.peakSignalAsFoundFerrousTrailing})")
            DetailRow(label = "Engineer Notes", value = cal.ferrousAsFoundEngineerNotes)
        }

        DetailSection(title = "Non-Ferrous Sensitivity (As Found)") {
            DetailRow(label = "Sensitivity", value = formatMm(cal.sensitivityAsFoundNonFerrous))
            DetailRow(label = "Cert Number", value = cal.sampleCertificateNumberAsFoundNonFerrous)
            DetailRow(label = "Detect/Reject Leading", value = "${cal.detectRejectAsFoundNonFerrousLeading} (${cal.peakSignalAsFoundNonFerrousLeading})")
            DetailRow(label = "Detect/Reject Middle", value = "${cal.detectRejectAsFoundNonFerrousMiddle} (${cal.peakSignalAsFoundNonFerrousMiddle})")
            DetailRow(label = "Detect/Reject Trailing", value = "${cal.detectRejectAsFoundNonFerrousTrailing} (${cal.peakSignalAsFoundNonFerrousTrailing})")
            DetailRow(label = "Engineer Notes", value = cal.nonFerrousAsFoundEngineerNotes)
        }

        DetailSection(title = "Stainless Sensitivity (As Found)") {
            DetailRow(label = "Sensitivity", value = formatMm(cal.sensitivityAsFoundStainless))
            DetailRow(label = "Cert Number", value = cal.sampleCertificateNumberAsFoundStainless)
            DetailRow(label = "Detect/Reject Leading", value = "${cal.detectRejectAsFoundStainlessLeading} (${cal.peakSignalAsFoundStainlessLeading})")
            DetailRow(label = "Detect/Reject Middle", value = "${cal.detectRejectAsFoundStainlessMiddle} (${cal.peakSignalAsFoundStainlessMiddle})")
            DetailRow(label = "Detect/Reject Trailing", value = "${cal.detectRejectAsFoundStainlessTrailing} (${cal.peakSignalAsFoundStainlessTrailing})")
            DetailRow(label = "Engineer Notes", value = cal.stainlessAsFoundEngineerNotes)
        }

        DetailSection(title = "Detection Settings (As Found)") {
            DetailRow(label = cal.detectionSetting1label, value = cal.detectionSettingAsFound1)
            DetailRow(label = cal.detectionSetting2label, value = cal.detectionSettingAsFound2)
            DetailRow(label = cal.detectionSetting3label, value = cal.detectionSettingAsFound3)
            DetailRow(label = cal.detectionSetting4label, value = cal.detectionSettingAsFound4)
            DetailRow(label = cal.detectionSetting5label, value = cal.detectionSettingAsFound5)
            DetailRow(label = cal.detectionSetting6label, value = cal.detectionSettingAsFound6)
            DetailRow(label = cal.detectionSetting7label, value = cal.detectionSettingAsFound7)
            DetailRow(label = cal.detectionSetting8label, value = cal.detectionSettingAsFound8)
            DetailRow(label = "Access Restriction", value = cal.sensitivityAccessRestriction)
            DetailRow(label = "P.V. Result", value = cal.detectionSettingPvResult)
            DetailRow(label = "Engineer Notes", value = cal.detectionSettingAsFoundEngineerNotes)
        }

        DetailSection(title = "Ferrous Sensitivity (As Left)") {
            DetailRow(label = "Sensitivity", value = formatMm(cal.sensitivityAsLeftFerrous))
            DetailRow(label = "Cert Number", value = cal.sampleCertificateNumberFerrous)
            DetailRow(label = "Detect/Reject Leading", value = "${cal.detectRejectFerrousLeading} (${cal.detectRejectFerrousLeadingPeakSignal})")
            DetailRow(label = "Detect/Reject Middle", value = "${cal.detectRejectFerrousMiddle} (${cal.detectRejectFerrousMiddlePeakSignal})")
            DetailRow(label = "Detect/Reject Trailing", value = "${cal.detectRejectFerrousTrailing} (${cal.detectRejectFerrousTrailingPeakSignal})")
            DetailRow(label = "P.V. Result", value = cal.ferrousTestPvResult)
            DetailRow(label = "Engineer Notes", value = cal.ferrousTestEngineerNotes)
        }

        DetailSection(title = "Non-Ferrous Sensitivity (As Left)") {
            DetailRow(label = "Sensitivity", value = formatMm(cal.sensitivityAsLeftNonFerrous))
            DetailRow(label = "Cert Number", value = cal.sampleCertificateNumberNonFerrous)
            DetailRow(label = "Detect/Reject Leading", value = "${cal.detectRejectNonFerrousLeading} (${cal.detectRejectNonFerrousLeadingPeakSignal})")
            DetailRow(label = "Detect/Reject Middle", value = "${cal.detectRejectNonFerrousMiddle} (${cal.detectRejectNonFerrousMiddlePeakSignal})")
            DetailRow(label = "Detect/Reject Trailing", value = "${cal.detectRejectNonFerrousTrailing} (${cal.detectRejectNonFerrousTrailingPeakSignal})")
            DetailRow(label = "P.V. Result", value = cal.nonFerrousTestPvResult)
            DetailRow(label = "Engineer Notes", value = cal.nonFerrousTestEngineerNotes)
        }

        DetailSection(title = "Stainless Sensitivity (As Left)") {
            DetailRow(label = "Sensitivity", value = formatMm(cal.sensitivityAsLeftStainless))
            DetailRow(label = "Cert Number", value = cal.sampleCertificateNumberStainless)
            DetailRow(label = "Detect/Reject Leading", value = "${cal.detectRejectStainlessLeading} (${cal.detectRejectStainlessLeadingPeakSignal})")
            DetailRow(label = "Detect/Reject Middle", value = "${cal.detectRejectStainlessMiddle} (${cal.detectRejectStainlessMiddlePeakSignal})")
            DetailRow(label = "Detect/Reject Trailing", value = "${cal.detectRejectStainlessTrailing} (${cal.detectRejectStainlessTrailingPeakSignal})")
            DetailRow(label = "P.V. Result", value = cal.stainlessTestPvResult)
            DetailRow(label = "Engineer Notes", value = cal.stainlessTestEngineerNotes)
        }

        DetailSection(title = "Detection Settings (As Left)") {
            DetailRow(label = cal.detectionSetting1label, value = cal.detectionSettingAsLeft1)
            DetailRow(label = cal.detectionSetting2label, value = cal.detectionSettingAsLeft2)
            DetailRow(label = cal.detectionSetting3label, value = cal.detectionSettingAsLeft3)
            DetailRow(label = cal.detectionSetting4label, value = cal.detectionSettingAsLeft4)
            DetailRow(label = cal.detectionSetting5label, value = cal.detectionSettingAsLeft5)
            DetailRow(label = cal.detectionSetting6label, value = cal.detectionSettingAsLeft6)
            DetailRow(label = cal.detectionSetting7label, value = cal.detectionSettingAsLeft7)
            DetailRow(label = cal.detectionSetting8label, value = cal.detectionSettingAsLeft8)
            DetailRow(label = "Product Peak Signal", value = cal.productPeakSignalAsLeft)
            DetailRow(label = "Engineer Notes", value = cal.detectionSettingAsLeftEngineerNotes)
        }

        DetailSection(title = "Reject Settings") {
            DetailRow(label = "Reject Delay", value = "${cal.rejectDelaySetting} ${cal.rejectDelayUnits}")
            DetailRow(label = "Reject Duration", value = "${cal.rejectDurationSetting} ${cal.rejectDurationUnits}")
            DetailRow(label = "Confirm Window", value = "${cal.rejectConfirmWindowSetting} ${cal.rejectConfirmWindowUnits}")
            DetailRow(label = "Engineer Notes", value = cal.rejectSettingsEngineerNotes)
        }

        DetailSection(title = "Large Metal Test") {
            DetailRow(label = "Result", value = cal.detectRejectLargeMetal)
            DetailRow(label = "Cert Number", value = cal.sampleCertificateNumberLargeMetal)
            DetailRow(label = "P.V. Result", value = cal.largeMetalTestPvResult)
            DetailRow(label = "Engineer Notes", value = cal.largeMetalTestEngineerNotes)
        }

        DetailSection(title = "Infeed Sensor") {
            DetailRow(label = "Fitted", value = cal.infeedSensorFitted)
            val infeedTestMethodDisplay = if (cal.infeedSensorTestMethod == "Other" && cal.infeedSensorTestMethodOther.isNotBlank()) {
                cal.infeedSensorTestMethodOther
            } else {
                cal.infeedSensorTestMethod
            }
            DetailRow(label = "Test Method", value = infeedTestMethodDisplay)
            DetailRow(label = "Test Result", value = cal.infeedSensorTestResult)
            DetailRow(label = "Latched", value = cal.infeedSensorLatched)
            DetailRow(label = "Controlled Restart", value = cal.infeedSensorCR)
            DetailRow(label = "P.V. Result", value = cal.infeedSensorTestPvResult)
            DetailRow(label = "Engineer Notes", value = cal.infeedSensorEngineerNotes)
        }

        DetailSection(title = "Reject Confirm Sensor") {
            DetailRow(label = "Fitted", value = cal.rejectConfirmSensorFitted)
            val rejectConfirmTestMethodDisplay = if (cal.rejectConfirmSensorTestMethod == "Other" && cal.rejectConfirmSensorTestMethodOther.isNotBlank()) {
                cal.rejectConfirmSensorTestMethodOther
            } else {
                cal.rejectConfirmSensorTestMethod
            }
            DetailRow(label = "Test Method", value = rejectConfirmTestMethodDisplay)
            DetailRow(label = "Test Result", value = cal.rejectConfirmSensorTestResult)
            DetailRow(label = "Stop Position", value = cal.rejectConfirmSensorStopPosition)
            DetailRow(label = "Latched", value = cal.rejectConfirmSensorLatched)
            DetailRow(label = "Controlled Restart", value = cal.rejectConfirmSensorCR)
            DetailRow(label = "P.V. Result", value = cal.rejectConfirmSensorTestPvResult)
            DetailRow(label = "Engineer Notes", value = cal.rejectConfirmSensorEngineerNotes)
        }

        DetailSection(title = "Bin Full Sensor") {
            DetailRow(label = "Fitted", value = cal.binFullSensorFitted)
            val binFullTestMethodDisplay = if (cal.binFullSensorTestMethod == "Other" && cal.binFullSensorTestMethodOther.isNotBlank()) {
                cal.binFullSensorTestMethodOther
            } else {
                cal.binFullSensorTestMethod
            }
            DetailRow(label = "Test Method", value = binFullTestMethodDisplay)
            DetailRow(label = "Test Result", value = cal.binFullSensorTestResult)
            DetailRow(label = "Latched", value = cal.binFullSensorLatched)
            DetailRow(label = "Controlled Restart", value = cal.binFullSensorCR)
            DetailRow(label = "P.V. Result", value = cal.binFullSensorTestPvResult)
            DetailRow(label = "Engineer Notes", value = cal.binFullSensorEngineerNotes)
        }

        DetailSection(title = "Air Pressure Sensor") {
            DetailRow(label = "Fitted", value = cal.airPressureSensorFitted)
            val airPressureTestMethodDisplay = if (cal.airPressureSensorTestMethod == "Other" && cal.airPressureSensorTestMethodOther.isNotBlank()) {
                cal.airPressureSensorTestMethodOther
            } else {
                cal.airPressureSensorTestMethod
            }
            DetailRow(label = "Test Method", value = airPressureTestMethodDisplay)
            DetailRow(label = "Test Result", value = cal.airPressureSensorTestResult)
            DetailRow(label = "Latched", value = cal.airPressureSensorLatched)
            DetailRow(label = "Controlled Restart", value = cal.airPressureSensorCR)
            DetailRow(label = "P.V. Result", value = cal.airPressureSensorTestPvResult)
            DetailRow(label = "Engineer Notes", value = cal.airPressureSensorEngineerNotes)
        }

        DetailSection(title = "Bin Door Monitor") {
            DetailRow(label = "Fitted", value = cal.binDoorMonitorFitted)
            DetailRow(label = "Status As Found", value = cal.binDoorStatusAsFound)
            DetailRow(label = "Open Indication", value = cal.binDoorOpenIndication)
            DetailRow(label = "Unlocked Indication", value = cal.binDoorUnlockedIndication)
            DetailRow(label = "Timeout Timer", value = cal.binDoorTimeoutTimer)
            DetailRow(label = "Timeout Result", value = cal.binDoorTimeoutResult)
            DetailRow(label = "Latched", value = cal.binDoorLatched)
            DetailRow(label = "Controlled Restart", value = cal.binDoorCR)
            DetailRow(label = "P.V. Result", value = cal.binDoorMonitorTestPvResult)
            DetailRow(label = "Engineer Notes", value = cal.binDoorEngineerNotes)
        }

        DetailSection(title = "Backup Sensor") {
            DetailRow(label = "Fitted", value = cal.backupSensorFitted)
            val backupTestMethodDisplay = if (cal.backupSensorTestMethod == "Other" && cal.backupSensorTestMethodOther.isNotBlank()) {
                cal.backupSensorTestMethodOther
            } else {
                cal.backupSensorTestMethod
            }
            DetailRow(label = "Test Method", value = backupTestMethodDisplay)
            DetailRow(label = "Test Result", value = cal.backupSensorTestResult)
            DetailRow(label = "Latched", value = cal.backupSensorLatched)
            DetailRow(label = "Controlled Restart", value = cal.backupSensorCR)
            DetailRow(label = "P.V. Result", value = cal.backupSensorTestPvResult)
            DetailRow(label = "Engineer Notes", value = cal.backupSensorEngineerNotes)
        }

        DetailSection(title = "Pack Check Sensor") {
            DetailRow(label = "Fitted", value = cal.packCheckSensorFitted)
            val packCheckTestMethodDisplay = if (cal.packCheckSensorTestMethod == "Other" && cal.packCheckSensorTestMethodOther.isNotBlank()) {
                cal.packCheckSensorTestMethodOther
            } else {
                cal.packCheckSensorTestMethod
            }
            DetailRow(label = "Test Method", value = packCheckTestMethodDisplay)
            DetailRow(label = "Test Result", value = cal.packCheckSensorTestResult)
            DetailRow(label = "Latched", value = cal.packCheckSensorLatched)
            DetailRow(label = "Controlled Restart", value = cal.packCheckSensorCR)
            DetailRow(label = "P.V. Result", value = cal.packCheckSensorTestPvResult)
            DetailRow(label = "Engineer Notes", value = cal.packCheckSensorEngineerNotes)
        }

        DetailSection(title = "Speed Sensor") {
            DetailRow(label = "Fitted", value = cal.speedSensorFitted)
            val speedTestMethodDisplay = if (cal.speedSensorTestMethod == "Other" && cal.speedSensorTestMethodOther.isNotBlank()) {
                cal.speedSensorTestMethodOther
            } else {
                cal.speedSensorTestMethod
            }
            DetailRow(label = "Test Method", value = speedTestMethodDisplay)
            DetailRow(label = "Test Result", value = cal.speedSensorTestResult)
            DetailRow(label = "P.V. Result", value = cal.speedSensorTestPvResult)
            DetailRow(label = "Engineer Notes", value = cal.speedSensorEngineerNotes)
        }

        DetailSection(title = "Detect Notification") {
            DetailRow(label = "Result", value = cal.detectNotificationResult)
            DetailRow(label = "P.V. Result", value = cal.detectNotificationTestPvResult)
            DetailRow(label = "Engineer Notes", value = cal.detectNotificationEngineerNotes)
        }

        DetailSection(title = "Operator Test") {
            DetailRow(label = "Witnessed", value = cal.operatorTestWitnessed)
            DetailRow(label = "Operator Name", value = cal.operatorName)
            DetailRow(label = "Ferrous Cert", value = cal.operatorTestResultCertNumberFerrous)
            DetailRow(label = "Ferrous Result", value = formatMm(cal.operatorTestResultFerrous))
            DetailRow(label = "Non-Ferrous Cert", value = cal.operatorTestResultCertNumberNonFerrous)
            DetailRow(label = "Non-Ferrous Result", value = formatMm(cal.operatorTestResultNonFerrous))
            DetailRow(label = "Stainless Cert", value = cal.operatorTestResultCertNumberStainless)
            DetailRow(label = "Stainless Result", value = formatMm(cal.operatorTestResultStainless))
            DetailRow(label = "Large Metal Cert", value = cal.operatorTestResultCertNumberLargeMetal)
            DetailRow(label = "Large Metal Result", value = formatMm(cal.operatorTestResultLargeMetal))
            DetailRow(label = "SME Name", value = cal.smeName)
            DetailRow(label = "Engineer Notes", value = cal.smeEngineerNotes)
            DetailRow(label = "P.V. Result", value = cal.smeTestPvResult)
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = SnbRed,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.ifBlank { "N/A" },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
