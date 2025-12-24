package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSummaryDetails(
    viewModel: CalibrationMetalDetectorConveyorViewModel,
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            //.verticalScroll(scrollState)
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
        fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
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
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Section content
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        content = content
                    )
                }
            }
        }

        val desiredCop by viewModel.desiredCop.collectAsState()
        val infeedSensorTestResult by viewModel.infeedSensorTestResult.collectAsState()
        val rejectConfirmSensorTestResult by viewModel.rejectConfirmSensorTestResult.collectAsState()
        val binFullSensorTestResult by viewModel.binFullSensorTestResult.collectAsState()
        val backupSensorTestResult by viewModel.backupSensorTestResult.collectAsState()
        val airPressureSensorTestResult by viewModel.airPressureSensorTestResult.collectAsState()
        val speedSensorTestResult by viewModel.speedSensorTestResult.collectAsState()
        val binDoorOpenIndication by viewModel.binDoorOpenIndication.collectAsState()
        val binDoorUnlockedIndication by viewModel.binDoorUnlockedIndication.collectAsState()
        val binDoorTimeoutResult by viewModel.binDoorTimeoutResult.collectAsState()
        val detectNotificationResult by viewModel.detectNotificationResult.collectAsState()


        Section(title = "System Details") {
            SummaryItem(label = "Customer Name", value = viewModel.customerName.value)
            SummaryItem(label = "Model Description", value = viewModel.modelDescription.value)
            SummaryItem(label = "Serial Number", value = viewModel.serialNumber.value)
            SummaryItem(label = "System Location", value = viewModel.lastLocation.value)
            SummaryItem(label = "New Location", value = viewModel.newLocation.value)
            SummaryItem(
                label = "PV required",
                value = if (viewModel.pvRequired.value) "Yes" else "No"
            )

        }

        Section(title = "M&S Sensitivity Requirements") {
            SummaryItem(
                label = "Ferrous Target",
                value = "${viewModel.sensitivityData.value?.FerrousTargetMM ?: "N/A"}mm " +
                        "(Max ${viewModel.sensitivityData.value?.FerrousMaxMM ?: "N/A"}mm)"
            )

            SummaryItem(
                label = "Non-Ferrous Target",
                value = "${viewModel.sensitivityData.value?.NonFerrousTargetMM ?: "N/A"}mm " +
                        "(Max ${viewModel.sensitivityData.value?.NonFerrousMaxMM ?: "N/A"}mm)"
            )

            SummaryItem(
                label = "Stainless Target",
                value = "${viewModel.sensitivityData.value?.Stainless316TargetMM ?: "N/A"}mm " +
                        "(Max ${viewModel.sensitivityData.value?.Stainless316MaxMM ?: "N/A"}mm)"
            )


        }


        Section(title = "Customer Sensitivity Requirements") {
            //SummaryItem(label = "Desired COP", value = desiredCop.joinToString(" | "))
            SummaryItem(label = "Ferrous Requirement", value = viewModel.sensitivityRequirementFerrous.value)
            SummaryItem(label = "Non-Ferrous Requirement", value = viewModel.sensitivityRequirementNonFerrous.value)
            SummaryItem(label = "Stainless Requirement", value = viewModel.sensitivityRequirementStainless.value)
            SummaryItem(label = "Engineer Notes", value = viewModel.sensitivityRequirementEngineerNotes.value)

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
        SummaryItem(
            label = "Non-Ferrous Sensitivity",
            value = viewModel.sensitivityAsLeftNonFerrous.value
        )
        SummaryItem(
            label = "Sample Certificate Number",
            value = viewModel.sampleCertificateNumberNonFerrous.value
        )
        SummaryItem(
            label = "Detect/Reject Leading",
            value = viewModel.detectRejectNonFerrousLeading.value.toString()
        )
        SummaryItem(
            label = "Detect/Reject Middle",
            value = viewModel.detectRejectNonFerrousMiddle.value.toString()
        )
        SummaryItem(
            label = "Detect/Reject Trailing",
            value = viewModel.detectRejectNonFerrousTrailing.value.toString()
        )
        SummaryItem(label = "Engineer Notes", value = viewModel.nonFerrousTestEngineerNotes.value)
    }

    Section(title = "Stainless Sensitivity (As Left)") {
        SummaryItem(
            label = "Stainless Sensitivity",
            value = viewModel.sensitivityAsLeftStainless.value
        )
        SummaryItem(
            label = "Sample Certificate Number",
            value = viewModel.sampleCertificateNumberStainless.value
        )
        SummaryItem(
            label = "Detect/Reject Leading",
            value = viewModel.detectRejectStainlessLeading.value.toString()
        )
        SummaryItem(
            label = "Detect/Reject Middle",
            value = viewModel.detectRejectStainlessMiddle.value.toString()
        )
        SummaryItem(
            label = "Detect/Reject Trailing",
            value = viewModel.detectRejectStainlessTrailing.value.toString()
        )
        SummaryItem(label = "Engineer Notes", value = viewModel.stainlessTestEngineerNotes.value)
    }

    Section(title = "Large Metal (20mm) Test") {
        SummaryItem(
            label = "Sample Certificate Number",
            value = viewModel.sampleCertificateNumberLargeMetal.value
        )
        SummaryItem(
            label = "Detect/Reject",
            value = viewModel.detectRejectLargeMetal.value.toString()
        )
        SummaryItem(label = "Engineer Notes", value = viewModel.largeMetalTestEngineerNotes.value)
    }

    Section(title = "Detection Settings (As Left)") {
        SummaryItem(
            label = viewModel.detectionSetting1label.value,
            value = viewModel.detectionSettingAsLeft1.value
        )
        SummaryItem(
            label = viewModel.detectionSetting2label.value,
            value = viewModel.detectionSettingAsLeft2.value
        )
        SummaryItem(
            label = viewModel.detectionSetting3label.value,
            value = viewModel.detectionSettingAsLeft3.value
        )
        SummaryItem(
            label = viewModel.detectionSetting4label.value,
            value = viewModel.detectionSettingAsLeft4.value
        )
        SummaryItem(
            label = viewModel.detectionSetting5label.value,
            value = viewModel.detectionSettingAsLeft5.value
        )
        SummaryItem(
            label = viewModel.detectionSetting6label.value,
            value = viewModel.detectionSettingAsLeft6.value
        )
        SummaryItem(
            label = viewModel.detectionSetting7label.value,
            value = viewModel.detectionSettingAsLeft7.value
        )
        SummaryItem(
            label = viewModel.detectionSetting8label.value,
            value = viewModel.detectionSettingAsLeft8.value
        )
        SummaryItem(
            label = "Engineer Notes",
            value = viewModel.detectionSettingAsLeftEngineerNotes.value
        )
    }

    Section(title = "Reject Settings") {
        SummaryItem(
            label = "Reject Synchronisation",
            value = viewModel.rejectSynchronisationSetting.value.toString()
        )
        SummaryItem(
            label = "Reject Synchronisation Details",
            value = viewModel.rejectSynchronisationDetail.value
        )
        SummaryItem(label = "Reject Delay", value = viewModel.rejectDelaySetting.value)
        SummaryItem(label = "Reject Delay", value = viewModel.rejectDelaySetting.value)
        SummaryItem(
            label = "Reject Confirm Window",
            value = viewModel.rejectConfirmWindowSetting.value
        )
        SummaryItem(label = "Engineer Notes", value = viewModel.rejectSettingsEngineerNotes.value)
    }

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
        SummaryItem(
            label = "Safety Circuit",
            value = viewModel.safetyCircuitCondition.value.toString()
        )
        SummaryItem(
            label = "Safety Circuit Comments",
            value = viewModel.safetyCircuitConditionComments.value
        )
        SummaryItem(
            label = "Detector Lining, gaskets and seals",
            value = viewModel.linerCondition.value.toString()
        )
        SummaryItem(
            label = "Detector Lining, gaskets and seals Comments",
            value = viewModel.linerConditionComments.value
        )
        SummaryItem(
            label = "Screws and Fittings",
            value = viewModel.screwsCondition.value.toString()
        )
        SummaryItem(
            label = "Screws and Fittings Comments",
            value = viewModel.screwsConditionComments.value
        )
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

    Section(title = "In-feed sensor") {
        SummaryItem(
            label = "Infeed Sensor Fitted",
            value = viewModel.infeedSensorFitted.value.toString()
        )
        SummaryItem(label = "Infeed Sensor Detail", value = viewModel.infeedSensorDetail.value)
        SummaryItem(
            label = "Infeed Sensor Test method",
            value = viewModel.infeedSensorTestMethod.value
        )
        SummaryItem(
            label = "Infeed Sensor Test result",
            value = infeedSensorTestResult.joinToString(" | ")
        )
        SummaryItem(
            label = "Infeed Sensor Fault Latched",
            value = viewModel.infeedSensorLatched.value.toString()
        )
        SummaryItem(
            label = "Infeed Sensor Fault Controlled Restart",
            value = viewModel.infeedSensorCR.value.toString()
        )
        SummaryItem(label = "Engineer Notes", value = viewModel.infeedSensorEngineerNotes.value)
    }

    Section(title = "Reject Confirm Sensor") {
        SummaryItem(
            label = "Reject Confirm Sensor Fitted",
            value = viewModel.rejectConfirmSensorFitted.value.toString()
        )
        SummaryItem(
            label = "Reject Confirm Sensor Detail",
            value = viewModel.rejectConfirmSensorDetail.value
        )
        SummaryItem(
            label = "Reject Confirm Sensor Test method",
            value = viewModel.rejectConfirmSensorTestMethod.value
        )
        SummaryItem(
            label = "Reject Confirm Sensor Test result",
            value = rejectConfirmSensorTestResult.joinToString(" | ")
        )
        SummaryItem(
            label = "Reject Confirm Sensor Fault Latched",
            value = viewModel.rejectConfirmSensorLatched.value.toString()
        )
        SummaryItem(
            label = "Reject Confirm Sensor Fault Controlled Restart",
            value = viewModel.rejectConfirmSensorCR.value.toString()
        )
        SummaryItem(
            label = "Engineer Notes",
            value = viewModel.rejectConfirmSensorEngineerNotes.value
        )
    }

    Section(title = "Bin Full Sensor") {
        SummaryItem(
            label = "Bin Full Sensor Fitted",
            value = viewModel.binFullSensorFitted.value.toString()
        )
        SummaryItem(label = "Bin Full Sensor Detail", value = viewModel.binFullSensorDetail.value)
        SummaryItem(
            label = "Bin Full Sensor Test method",
            value = viewModel.binFullSensorTestMethod.value
        )
        SummaryItem(
            label = "Bin Full Sensor Test result",
            value = binFullSensorTestResult.joinToString(" | ")
        )
        SummaryItem(
            label = "Bin Full Sensor Fault Latched",
            value = viewModel.binFullSensorLatched.value.toString()
        )
        SummaryItem(
            label = "Bin Full Sensor Fault Controlled Restart",
            value = viewModel.binFullSensorCR.value.toString()
        )
        SummaryItem(label = "Engineer Notes", value = viewModel.binFullSensorEngineerNotes.value)
    }

    Section(title = "Backup Sensor") {
        SummaryItem(
            label = "Backup Sensor Fitted",
            value = viewModel.backupSensorFitted.value.toString()
        )
        SummaryItem(label = "Backup Sensor Detail", value = viewModel.backupSensorDetail.value)
        SummaryItem(
            label = "Backup Sensor Test method",
            value = viewModel.backupSensorTestMethod.value
        )
        SummaryItem(
            label = "Backup Sensor Test result",
            value = backupSensorTestResult.joinToString(" | ")
        )
        SummaryItem(
            label = "Backup Sensor Fault Latched",
            value = viewModel.backupSensorLatched.value.toString()
        )
        SummaryItem(
            label = "Backup Sensor Fault Controlled Restart",
            value = viewModel.backupSensorCR.value.toString()
        )
        SummaryItem(label = "Engineer Notes", value = viewModel.backupSensorEngineerNotes.value)
    }

    Section(title = "Air Pressure Sensor") {
        SummaryItem(
            label = "Air Pressure Sensor Fitted",
            value = viewModel.airPressureSensorFitted.value.toString()
        )
        SummaryItem(
            label = "Air Pressure Sensor Detail",
            value = viewModel.airPressureSensorDetail.value
        )
        SummaryItem(
            label = "Air Pressure Sensor Test method",
            value = viewModel.airPressureSensorTestMethod.value
        )
        SummaryItem(
            label = "Air Pressure Sensor Test result",
            value = airPressureSensorTestResult.joinToString(" | ")
        )
        SummaryItem(
            label = "Air Pressure Sensor Fault Latched",
            value = viewModel.airPressureSensorLatched.value.toString()
        )
        SummaryItem(
            label = "Air Pressure Sensor Fault Controlled Restart",
            value = viewModel.airPressureSensorCR.value.toString()
        )
        SummaryItem(
            label = "Engineer Notes",
            value = viewModel.airPressureSensorEngineerNotes.value
        )

    }

    Section(title = "Speed Sensor") {
        SummaryItem(
            label = "Speed Sensor Fitted",
            value = viewModel.speedSensorFitted.value.toString()
        )
        SummaryItem(label = "Speed Sensor Detail", value = viewModel.speedSensorDetail.value)
        SummaryItem(
            label = "Speed Sensor Test method",
            value = viewModel.speedSensorTestMethod.value
        )
        SummaryItem(
            label = "Speed Sensor Test result",
            value = speedSensorTestResult.joinToString(" | ")
        )
        SummaryItem(
            label = "Speed Sensor Fault Latched",
            value = viewModel.speedSensorLatched.value.toString()
        )
        SummaryItem(
            label = "Speed Sensor Fault Controlled Restart",
            value = viewModel.speedSensorCR.value.toString()
        )
        SummaryItem(label = "Engineer Notes", value = viewModel.speedSensorEngineerNotes.value)
    }

    Section(title = "Detect Notification") {
        SummaryItem(
            label = "Detect Notification",
            value = detectNotificationResult.joinToString(" | ")
        )
        SummaryItem(
            label = "Engineer Notes",
            value = viewModel.detectNotificationEngineerNotes.value
        )
    }

    Section(title = "Bin Door Monitor") {
        SummaryItem(
            label = "Bin Door Monitor Fitted",
            value = viewModel.binDoorMonitorFitted.value.toString()
        )
        SummaryItem(
            label = "Bin Door Monitor Details",
            value = viewModel.binDoorMonitorDetail.value
        )
        SummaryItem(
            label = "Bin Door Monitor Status As Found",
            value = viewModel.binDoorStatusAsFound.value
        )
        SummaryItem(
            label = "Bin Door Monitor Open Indication",
            value = binDoorOpenIndication.joinToString(" | ")
        )
        SummaryItem(
            label = "Bin Door Monitor Unlocked Indication",
            value = binDoorUnlockedIndication.joinToString(" | ")
        )
        SummaryItem(
            label = "Bin Door Monitor Timeout Value",
            value = viewModel.binDoorTimeoutTimer.value
        )
        SummaryItem(
            label = "Bin Door Monitor Timeout Result",
            value = binDoorTimeoutResult.joinToString(" | ")
        )
        SummaryItem(
            label = "Bin Door Monitor Fault Latched",
            value = viewModel.binDoorLatched.value.toString()
        )
        SummaryItem(
            label = "Bin Door Monitor Fault Controlled Restart",
            value = viewModel.binDoorCR.value.toString()
        )
        SummaryItem(label = "Engineer Notes", value = viewModel.binDoorEngineerNotes.value)
    }

    Section(title = "Operator Test") {
        SummaryItem(
            label = "Operator Test Witnessed",
            value = viewModel.operatorTestWitnessed.value.toString()
        )
        SummaryItem(label = "Operator Name", value = viewModel.operatorName.value)
        SummaryItem(
            label = "Ferrous Sample Size",
            value = viewModel.operatorTestResultFerrous.value
        )
        SummaryItem(
            label = "Ferrous Certificate Number",
            value = viewModel.operatorTestResultCertNumberFerrous.value
        )
        SummaryItem(
            label = "Non-Ferrous Sample Size",
            value = viewModel.operatorTestResultNonFerrous.value
        )
        SummaryItem(
            label = "Non-Ferrous Certificate Number",
            value = viewModel.operatorTestResultCertNumberNonFerrous.value
        )
        SummaryItem(
            label = "Stainless Sample Size",
            value = viewModel.operatorTestResultStainless.value
        )
        SummaryItem(
            label = "Stainless Certificate Number",
            value = viewModel.operatorTestResultCertNumberStainless.value
        )
        SummaryItem(
            label = "Large Metal Sample Size",
            value = viewModel.operatorTestResultLargeMetal.value
        )
        SummaryItem(
            label = "Large Metal Certificate Number",
            value = viewModel.operatorTestResultCertNumberLargeMetal.value
        )
        SummaryItem(label = "On-Site SME", value = viewModel.smeName.value)
        SummaryItem(label = "Engineer Notes", value = viewModel.smeEngineerNotes.value)
    }

//    Section(title = "Retailer Compliance Confirmation") {
//        SummaryItem(
//            label = "Sensitivity Compliance",
//            value = viewModel.sensitivityCompliance.value.toString()
//        )
//        SummaryItem(
//            label = "Essential Compliance",
//            value = viewModel.essentialRequirementCompliance.value.toString()
//        )
//        SummaryItem(
//            label = "Failsafe Compliance",
//            value = viewModel.failsafeCompliance.value.toString()
//        )
//        SummaryItem(
//            label = "Best Sensitivity Report",
//            value = viewModel.bestSensitivityCompliance.value.toString()
//        )
//        SummaryItem(
//            label = "Sensitivity Recommendations",
//            value = viewModel.sensitivityRecommendations.value
//        )
//        SummaryItem(
//            label = "Performance Validation",
//            value = viewModel.performanceValidationIssued.value.toString()
//        )
//
//    }

        Spacer(modifier = Modifier.weight(22f))

        Text(
            text = "ATTENTION! YOU MUST INFORM THE CUSTOMER OF ANY DEFECTS OR IF ADJUSTMENTS HAVE BEEN MADE. USE YOUR SERVICE REPORT!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.weight(22f))


    }
}

@Composable
fun SummaryItem(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1.5f)
        )
        Text(
            text = value ?: "N/A",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(2f)
        )
    }
}
