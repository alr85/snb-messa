package com.snb.inspect.screens.service.mdCalibration

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSummaryDetails(
    viewModel: CalibrationMetalDetectorConveyorViewModel,
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
            text = "Calibration Summary",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Helper function to display a section
        @Composable
        fun Section(
            title: String, 
            forceShowCheckbox: Boolean = true, // default true, set false to hide
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
                                onCheckedChange = null // Handled by Row click for a larger tap target
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

        // Collect all StateFlows properly
        val infeedSensorTestResult by viewModel.infeedSensorTestResult.collectAsState()
        val rejectConfirmSensorTestResult by viewModel.rejectConfirmSensorTestResult.collectAsState()
        val binFullSensorTestResult by viewModel.binFullSensorTestResult.collectAsState()
        val backupSensorTestResult by viewModel.backupSensorTestResult.collectAsState()
        val airPressureSensorTestResult by viewModel.airPressureSensorTestResult.collectAsState()
        val packCheckSensorTestResult by viewModel.packCheckSensorTestResult.collectAsState()
        val speedSensorTestResult by viewModel.speedSensorTestResult.collectAsState()
        val binDoorOpenIndication by viewModel.binDoorOpenIndication.collectAsState()
        val binDoorUnlockedIndication by viewModel.binDoorUnlockedIndication.collectAsState()
        val binDoorTimeoutResult by viewModel.binDoorTimeoutResult.collectAsState()
        val detectNotificationResult by viewModel.detectNotificationResult.collectAsState()
        val sensitivityData by viewModel.sensitivityData


        Section(title = "Calibration Details", forceShowCheckbox = false) {
            SummaryItem(label = "Calibration ID", value = viewModel.calibrationId.value)
            SummaryItem(label = "Engineer ID", value = viewModel.engineerId.toString())
            SummaryItem(label = "Calibration Start Time", value = viewModel.calibrationStartTime.value)
        }


        Section(title = "System Details") {
            SummaryItem(label = "Customer Name", value = viewModel.customerName.value)
            SummaryItem(label = "Model Description", value = viewModel.modelDescription.value)
            SummaryItem(label = "Aperture Width", value = "${viewModel.apertureWidth.value}mm")
            SummaryItem(label = "Aperture Height", value = "${viewModel.apertureHeight.value}mm")
            SummaryItem(label = "Serial Number", value = viewModel.serialNumber.value)
            SummaryItem(label = "System Location", value = viewModel.lastLocation.value)
            SummaryItem(label = "New Location", value = viewModel.newLocation.value)
            SummaryItem(label = "PV required", value = if (viewModel.pvRequired.value) "Yes" else "No")
            SummaryItem(label = "Able to calibrate", value = if (viewModel.canPerformCalibration.value) "Yes" else "No")
            
            if (!viewModel.canPerformCalibration.value) {
                SummaryItem(label = "Reason for not calibrating", value = viewModel.reasonForNotCalibrating.value)
            }
        }

        // Only show the detailed calibration data if the engineer was ABLE to calibrate
        if (viewModel.canPerformCalibration.value) {

            Section(title = "Conveyor Details") {
                SummaryItem(label = "In-feed Belt Height", value = viewModel.infeedBeltHeight.value)
                SummaryItem(label = "Out-feed Belt Height", value = viewModel.outfeedBeltHeight.value)
                SummaryItem(label = "Conveyor Length", value = viewModel.conveyorLength.value)
                SummaryItem(label = "Conveyor Handing", value = viewModel.conveyorHanding.value)
                SummaryItem(label = "Belt Speed", value = viewModel.beltSpeed.value)
                SummaryItem(label = "Reject System", value = viewModel.rejectDevice.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.conveyorDetailsEngineerNotes.value)
            }

            Section(title = "System Checklist") {
                SummaryItem(label = "Conveyor Belt", value = viewModel.beltCondition.value.toString())
                SummaryItem(label = "Conveyor Belt Comments", value = viewModel.beltConditionComments.value)
                SummaryItem(label = "Guarding", value = viewModel.guardCondition.value.toString())
                SummaryItem(label = "Conveyor Comments", value = viewModel.guardConditionComments.value)
                SummaryItem(label = "Safety Circuit", value = viewModel.safetyCircuitCondition.value.toString())
                SummaryItem(label = "Safety Circuit Comments", value = viewModel.safetyCircuitConditionComments.value)
                SummaryItem(label = "Detector Lining, gaskets and seals", value = viewModel.linerCondition.value.toString())
                SummaryItem(label = "Detector Lining, gaskets and seals Comments", value = viewModel.linerConditionComments.value)
                SummaryItem(label = "Screws and Fittings", value = viewModel.screwsCondition.value.toString())
                SummaryItem(label = "Screws and Fittings Comments", value = viewModel.screwsConditionComments.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.systemChecklistEngineerNotes.value)
            }

            Section(title = "Indicators") {
                SummaryItem(label = "Indicator 6 colour", value = viewModel.indicator6colour.value)
                SummaryItem(label = "Indicator 6 label", value = viewModel.indicator6label.value)
                SummaryItem(label = "Indicator 5 colour", value = viewModel.indicator5colour.value)
                SummaryItem(label = "Indicator 5 label", value = viewModel.indicator5label.value)
                SummaryItem(label = "Indicator 4 colour", value = viewModel.indicator4colour.value)
                SummaryItem(label = "Indicator 4 label", value = viewModel.indicator4label.value)
                SummaryItem(label = "Indicator 3 colour", value = viewModel.indicator3colour.value)
                SummaryItem(label = "Indicator 3 label", value = viewModel.indicator3label.value)
                SummaryItem(label = "Indicator 2 colour", value = viewModel.indicator2colour.value)
                SummaryItem(label = "Indicator 2 label", value = viewModel.indicator2label.value)
                SummaryItem(label = "Indicator 1 colour", value = viewModel.indicator1colour.value)
                SummaryItem(label = "Indicator 1 label", value = viewModel.indicator1label.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.indicatorsEngineerNotes.value)
            }

            Section(title = "Product Details") {
                SummaryItem(label = "Product Description", value = viewModel.productDescription.value)
                SummaryItem(label = "Product Library Reference", value = viewModel.productLibraryReference.value)
                SummaryItem(label = "Product Library Number", value = viewModel.productLibraryNumber.value)
                SummaryItem(label = "Product Length", value = viewModel.productLength.value)
                SummaryItem(label = "Product Width", value = viewModel.productWidth.value)
                SummaryItem(label = "Product Height", value = viewModel.productHeight.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.productDetailsEngineerNotes.value)
            }

            Section(title = "M&S Sensitivity Requirements") {
                SummaryItem(
                    label = "Ferrous Target",
                    value = "${sensitivityData?.ferrousTargetMM ?: "N/A"}mm (Max ${sensitivityData?.ferrousMaxMM ?: "N/A"}mm)"
                )
                SummaryItem(
                    label = "Non-Ferrous Target",
                    value = "${sensitivityData?.nonFerrousTargetMM ?: "N/A"}mm (Max ${sensitivityData?.nonFerrousMaxMM ?: "N/A"}mm)"
                )
                SummaryItem(
                    label = "Stainless Target",
                    value = "${sensitivityData?.stainless316TargetMM ?: "N/A"}mm (Max ${sensitivityData?.stainless316MaxMM ?: "N/A"}mm)"
                )
            }

            Section(title = "Customer Sensitivity Requirements") {
                SummaryItem(label = "Ferrous Requirement", value = viewModel.sensitivityRequirementFerrous.value)
                SummaryItem(label = "Non-Ferrous Requirement", value = viewModel.sensitivityRequirementNonFerrous.value)
                SummaryItem(label = "Stainless Requirement", value = viewModel.sensitivityRequirementStainless.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.sensitivityRequirementEngineerNotes.value)
            }

            Section(title = "Detection Settings (As Found)") {
                SummaryItem(label = viewModel.detectionSetting1label.value, value = viewModel.detectionSettingAsFound1.value)
                SummaryItem(label = viewModel.detectionSetting2label.value, value = viewModel.detectionSettingAsFound2.value)
                SummaryItem(label = viewModel.detectionSetting3label.value, value = viewModel.detectionSettingAsFound3.value)
                SummaryItem(label = viewModel.detectionSetting4label.value, value = viewModel.detectionSettingAsFound4.value)
                SummaryItem(label = viewModel.detectionSetting5label.value, value = viewModel.detectionSettingAsFound5.value)
                SummaryItem(label = viewModel.detectionSetting6label.value, value = viewModel.detectionSettingAsFound6.value)
                SummaryItem(label = viewModel.detectionSetting7label.value, value = viewModel.detectionSettingAsFound7.value)
                SummaryItem(label = viewModel.detectionSetting8label.value, value = viewModel.detectionSettingAsFound8.value)
                SummaryItem(label = "Access Restriction", value = viewModel.sensitivityAccessRestriction.value)
                SummaryItem(label = "P.V. Result", value = viewModel.detectionSettingPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.detectionSettingAsFoundEngineerNotes.value)
            }

            Section(title = "Sensitivities As Found") {
                SummaryItem(label = "Ferrous Sensitivity", value = viewModel.sensitivityAsFoundFerrous.value)
                SummaryItem(label = "Non-Ferrous Sensitivity", value = viewModel.sensitivityAsFoundNonFerrous.value)
                SummaryItem(label = "Stainless Sensitivity", value = viewModel.sensitivityAsFoundStainless.value)
                SummaryItem(label = "Product Peak Signal", value = viewModel.productPeakSignalAsFound.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.sensitivityAsFoundEngineerNotes.value)
            }

            Section(title = "Ferrous Sensitivity (As Left)") {
                SummaryItem(label = "Ferrous Sensitivity", value = viewModel.sensitivityAsLeftFerrous.value)
                SummaryItem(label = "Sample Certificate Number", value = viewModel.sampleCertificateNumberFerrous.value)
                SummaryItem(label = "Detect/Reject Leading", value = "${viewModel.detectRejectFerrousLeading.value} (${viewModel.peakSignalFerrousLeading.value})")
                SummaryItem(label = "Detect/Reject Middle", value = "${viewModel.detectRejectFerrousMiddle.value} (${viewModel.peakSignalFerrousMiddle.value})")
                SummaryItem(label = "Detect/Reject Trailing", value = "${viewModel.detectRejectFerrousTrailing.value} (${viewModel.peakSignalFerrousTrailing.value})")
                SummaryItem(label = "P.V. Result", value = viewModel.ferrousTestPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.ferrousTestEngineerNotes.value)
            }

            Section(title = "Non-Ferrous Sensitivity (As Left)") {
                SummaryItem(label = "Non-Ferrous Sensitivity", value = viewModel.sensitivityAsLeftNonFerrous.value)
                SummaryItem(label = "Sample Certificate Number", value = viewModel.sampleCertificateNumberNonFerrous.value)
                SummaryItem(label = "Detect/Reject Leading", value = "${viewModel.detectRejectNonFerrousLeading.value} (${viewModel.peakSignalNonFerrousLeading.value})")
                SummaryItem(label = "Detect/Reject Middle", value = "${viewModel.detectRejectNonFerrousMiddle.value} (${viewModel.peakSignalNonFerrousMiddle.value})")
                SummaryItem(label = "Detect/Reject Trailing", value = "${viewModel.detectRejectNonFerrousTrailing.value} (${viewModel.peakSignalNonFerrousTrailing.value})")
                SummaryItem(label = "P.V. Result", value = viewModel.nonFerrousTestPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.nonFerrousTestEngineerNotes.value)
            }

            Section(title = "Stainless Sensitivity (As Left)") {
                SummaryItem(label = "Stainless Sensitivity", value = viewModel.sensitivityAsLeftStainless.value)
                SummaryItem(label = "Sample Certificate Number", value = viewModel.sampleCertificateNumberStainless.value)
                SummaryItem(label = "Detect/Reject Leading", value = "${viewModel.detectRejectStainlessLeading.value} (${viewModel.peakSignalStainlessLeading.value})")
                SummaryItem(label = "Detect/Reject Middle", value = "${viewModel.detectRejectStainlessMiddle.value} (${viewModel.peakSignalStainlessMiddle.value})")
                SummaryItem(label = "Detect/Reject Trailing", value = "${viewModel.detectRejectStainlessTrailing.value} (${viewModel.peakSignalStainlessTrailing.value})")
                SummaryItem(label = "P.V. Result", value = viewModel.stainlessTestPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.stainlessTestEngineerNotes.value)
            }

            Section(title = "Operator Test") {
                SummaryItem(label = "Operator Test Witnessed", value = viewModel.operatorTestWitnessed.value.toString())
                SummaryItem(label = "Operator Name", value = viewModel.operatorName.value)
                SummaryItem(label = "Ferrous Cert", value = viewModel.operatorTestResultCertNumberFerrous.value)
                SummaryItem(label = "Ferrous Result", value = viewModel.operatorTestResultFerrous.value)
                SummaryItem(label = "Non-Ferrous Cert", value = viewModel.operatorTestResultCertNumberNonFerrous.value)
                SummaryItem(label = "Non-Ferrous Result", value = viewModel.operatorTestResultNonFerrous.value)
                SummaryItem(label = "Stainless Cert", value = viewModel.operatorTestResultCertNumberStainless.value)
                SummaryItem(label = "Stainless Result", value = viewModel.operatorTestResultStainless.value)
                SummaryItem(label = "Large Metal Cert", value = viewModel.operatorTestResultCertNumberLargeMetal.value)
                SummaryItem(label = "Large Metal Result", value = viewModel.operatorTestResultLargeMetal.value)
                SummaryItem(label = "SME Name", value = viewModel.smeName.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.smeEngineerNotes.value)
                SummaryItem(label = "P.V. Result", value = viewModel.smeTestPvResult.value)
            }

            Section(title = "Detection Settings (As Left)") {
                SummaryItem(label = viewModel.detectionSetting1label.value, value = viewModel.detectionSettingAsLeft1.value)
                SummaryItem(label = viewModel.detectionSetting2label.value, value = viewModel.detectionSettingAsLeft2.value)
                SummaryItem(label = viewModel.detectionSetting3label.value, value = viewModel.detectionSettingAsLeft3.value)
                SummaryItem(label = viewModel.detectionSetting4label.value, value = viewModel.detectionSettingAsLeft4.value)
                SummaryItem(label = viewModel.detectionSetting5label.value, value = viewModel.detectionSettingAsLeft5.value)
                SummaryItem(label = viewModel.detectionSetting6label.value, value = viewModel.detectionSettingAsLeft6.value)
                SummaryItem(label = viewModel.detectionSetting7label.value, value = viewModel.detectionSettingAsLeft7.value)
                SummaryItem(label = viewModel.detectionSetting8label.value, value = viewModel.detectionSettingAsLeft8.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.detectionSettingAsLeftEngineerNotes.value)
            }

            Section(title = "Reject Settings") {
                SummaryItem(label = "Reject Synchronisation", value = viewModel.rejectSynchronisationSetting.value.toString())
                SummaryItem(label = "Synchronisation Detail", value = viewModel.rejectSynchronisationDetail.value)
                SummaryItem(label = "Reject Delay", value = "${viewModel.rejectDelaySetting.value} ${viewModel.rejectDelayUnits.value}")
                SummaryItem(label = "Reject Duration", value = "${viewModel.rejectDurationSetting.value} ${viewModel.rejectDurationUnits.value}")
                SummaryItem(label = "Confirm Window", value = "${viewModel.rejectConfirmWindowSetting.value} ${viewModel.rejectConfirmWindowUnits.value}")
                SummaryItem(label = "Engineer Notes", value = viewModel.rejectSettingsEngineerNotes.value)
            }

            Section(title = "Infeed PEC") {
                SummaryItem(label = "Fitted", value = viewModel.infeedSensorFitted.value.toString())
                SummaryItem(label = "Detail", value = viewModel.infeedSensorDetail.value)
                SummaryItem(label = "Test Method", value = "${viewModel.infeedSensorTestMethod.value} ")
                SummaryItem(label = "Test Method (Other)", value = "${viewModel.infeedSensorTestMethodOther.value} ")
                SummaryItem(label = "Test Result", value = infeedSensorTestResult.joinToString(", "))
                SummaryItem(label = "Latched", value = viewModel.infeedSensorLatched.value.toString())
                SummaryItem(label = "Critical", value = viewModel.infeedSensorCR.value.toString())
                SummaryItem(label = "P.V. Result", value = viewModel.infeedSensorTestPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.infeedSensorEngineerNotes.value)
            }

            Section(title = "Large Metal Test") {
                SummaryItem(label = "Result", value = viewModel.detectRejectLargeMetal.value.toString())
                SummaryItem(label = "Cert Number", value = viewModel.sampleCertificateNumberLargeMetal.value)
                SummaryItem(label = "P.V. Result", value = viewModel.largeMetalTestPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.largeMetalTestEngineerNotes.value)
            }

            Section(title = "Reject Confirm PEC") {
                SummaryItem(label = "Fitted", value = viewModel.rejectConfirmSensorFitted.value.toString())
                SummaryItem(label = "Detail", value = viewModel.rejectConfirmSensorDetail.value)
                SummaryItem(label = "Test Method", value = viewModel.rejectConfirmSensorTestMethod.value)
                SummaryItem(label = "Test Method (Other)", value = viewModel.rejectConfirmSensorTestMethodOther.value)
                SummaryItem(label = "Test Result", value = rejectConfirmSensorTestResult.joinToString(", "))
                SummaryItem(label = "Stop Position", value = viewModel.rejectConfirmSensorStopPosition.value)
                SummaryItem(label = "Latched", value = viewModel.rejectConfirmSensorLatched.value.toString())
                SummaryItem(label = "Critical", value = viewModel.rejectConfirmSensorCR.value.toString())
                SummaryItem(label = "P.V. Result", value = viewModel.rejectConfirmSensorTestPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.rejectConfirmSensorEngineerNotes.value)
            }

            Section(title = "Bin Full PEC") {
                SummaryItem(label = "Fitted", value = viewModel.binFullSensorFitted.value.toString())
                SummaryItem(label = "Detail", value = viewModel.binFullSensorDetail.value)
                SummaryItem(label = "Test Method", value = viewModel.binFullSensorTestMethod.value)
                SummaryItem(label = "Test Method (Other)", value = viewModel.binFullSensorTestMethodOther.value)
                SummaryItem(label = "Test Result", value = binFullSensorTestResult.joinToString(", "))
                SummaryItem(label = "Latched", value = viewModel.binFullSensorLatched.value.toString())
                SummaryItem(label = "Critical", value = viewModel.binFullSensorCR.value.toString())
                SummaryItem(label = "P.V. Result", value = viewModel.binFullSensorTestPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.binFullSensorEngineerNotes.value)
            }

            Section(title = "Backup PEC") {
                SummaryItem(label = "Fitted", value = viewModel.backupSensorFitted.value.toString())
                SummaryItem(label = "Detail", value = viewModel.backupSensorDetail.value)
                SummaryItem(label = "Test Method", value = viewModel.backupSensorTestMethod.value)
                SummaryItem(label = "Test Method (Other)", value = viewModel.backupSensorTestMethodOther.value)
                SummaryItem(label = "Test Result", value = backupSensorTestResult.joinToString(", "))
                SummaryItem(label = "Latched", value = viewModel.backupSensorLatched.value.toString())
                SummaryItem(label = "Critical", value = viewModel.backupSensorCR.value.toString())
                SummaryItem(label = "P.V. Result", value = viewModel.backupSensorTestPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.backupSensorEngineerNotes.value)
            }

            Section(title = "Air Pressure Sensor") {
                SummaryItem(label = "Fitted", value = viewModel.airPressureSensorFitted.value.toString())
                SummaryItem(label = "Detail", value = viewModel.airPressureSensorDetail.value)
                SummaryItem(label = "Test Method", value = viewModel.airPressureSensorTestMethod.value)
                SummaryItem(label = "Test Method (Other)", value = viewModel.airPressureSensorTestMethodOther.value)
                SummaryItem(label = "Test Result", value = airPressureSensorTestResult.joinToString(", "))
                SummaryItem(label = "Latched", value = viewModel.airPressureSensorLatched.value.toString())
                SummaryItem(label = "Critical", value = viewModel.airPressureSensorCR.value.toString())
                SummaryItem(label = "P.V. Result", value = viewModel.airPressureSensorTestPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.airPressureSensorEngineerNotes.value)
            }

            Section(title = "Pack Check Sensor") {
                SummaryItem(label = "Fitted", value = viewModel.packCheckSensorFitted.value.toString())
                SummaryItem(label = "Detail", value = viewModel.packCheckSensorDetail.value)
                SummaryItem(label = "Test Method", value = viewModel.packCheckSensorTestMethod.value)
                SummaryItem(label = "Test Method (Other)", value = viewModel.packCheckSensorTestMethodOther.value)
                SummaryItem(label = "Test Result", value = packCheckSensorTestResult.joinToString(", "))
                SummaryItem(label = "Latched", value = viewModel.packCheckSensorLatched.value.toString())
                SummaryItem(label = "Critical", value = viewModel.packCheckSensorCR.value.toString())
                SummaryItem(label = "P.V. Result", value = viewModel.packCheckSensorTestPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.packCheckSensorEngineerNotes.value)
            }

            Section(title = "Speed Sensor") {
                SummaryItem(label = "Fitted", value = viewModel.speedSensorFitted.value.toString())
                SummaryItem(label = "Detail", value = viewModel.speedSensorDetail.value)
                SummaryItem(label = "Test Method", value = viewModel.speedSensorTestMethod.value)
                SummaryItem(label = "Test Method (Other)", value = viewModel.speedSensorTestMethodOther.value)
                SummaryItem(label = "Test Result", value = speedSensorTestResult.joinToString(", "))
                SummaryItem(label = "Latched", value = viewModel.speedSensorLatched.value.toString())
                SummaryItem(label = "Critical", value = viewModel.speedSensorCR.value.toString())
                SummaryItem(label = "P.V. Result", value = viewModel.speedSensorTestPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.speedSensorEngineerNotes.value)
            }

            Section(title = "Bin Door Monitor") {
                SummaryItem(label = "Fitted", value = viewModel.binDoorMonitorFitted.value.toString())
                SummaryItem(label = "Detail", value = viewModel.binDoorMonitorDetail.value)
                SummaryItem(label = "Status As Found", value = viewModel.binDoorStatusAsFound.value)
                SummaryItem(label = "Open Indication", value = binDoorOpenIndication.joinToString(", "))
                SummaryItem(label = "Unlocked Indication", value = binDoorUnlockedIndication.joinToString(", "))
                SummaryItem(label = "Timeout Timer", value = viewModel.binDoorTimeoutTimer.value)
                SummaryItem(label = "Timeout Result", value = binDoorTimeoutResult.joinToString(", "))
                SummaryItem(label = "Latched", value = viewModel.binDoorLatched.value.toString())
                SummaryItem(label = "Critical", value = viewModel.binDoorCR.value.toString())
                SummaryItem(label = "P.V. Result", value = viewModel.binDoorMonitorTestPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.binDoorEngineerNotes.value)
            }

            Section(title = "Detect Notification") {
                SummaryItem(label = "Result", value = detectNotificationResult.joinToString(", "))
                SummaryItem(label = "P.V. Result", value = viewModel.detectNotificationTestPvResult.value)
                SummaryItem(label = "Engineer Notes", value = viewModel.detectNotificationEngineerNotes.value)
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
            text = "$label:",
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
