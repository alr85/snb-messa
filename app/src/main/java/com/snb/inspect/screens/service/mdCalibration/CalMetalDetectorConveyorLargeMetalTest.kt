package com.snb.inspect.screens.service.mdCalibration


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationLogic.metalDetectorConveyor.autoUpdateLargeMetalPvResult
import com.snb.inspect.calibrationLogic.metalDetectorConveyor.getLargeMetalPvRules
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.formModules.LabeledYesNoSegmentedSwitchAndTextInputWithHelp
import com.snb.inspect.formModules.PvSectionSummaryCard
import com.snb.inspect.formModules.YesNoState
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorLargeMetalTest(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {

    val dr by viewModel.detectRejectLargeMetal
    val certNo by viewModel.sampleCertificateNumberLargeMetal
    val notes by viewModel.largeMetalTestEngineerNotes
    val pvRequired by viewModel.pvRequired

    // Next enabled rules:
    // - If N/A: allow Next
    // - If YES or NO: require cert number
    val isNextStepEnabled = when (dr) {
        YesNoState.NA -> true
        YesNoState.YES, YesNoState.NO -> certNo.isNotBlank()
        else -> false
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    // Auto-update PV whenever relevant fields change (cleaner than sprinkling calls everywhere)
    LaunchedEffect(pvRequired, dr, certNo) {
        if (!pvRequired) {
            viewModel.setLargeMetalTestPvResult("N/A")
            return@LaunchedEffect
        }
        viewModel.autoUpdateLargeMetalPvResult()
    }

    // PV rules
    val rules = remember(
        dr,
        certNo,
        pvRequired

    ) {
        viewModel.getLargeMetalPvRules()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Failsafe Tests - Large Metal Test")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {

            Column {


                LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                    label = "20mm Ferrous Detected & Rejected OK",
                    currentState = dr,
                    onStateChange = { newState ->
                        viewModel.setDetectRejectLargeMetal(newState)

                        // Optional but sensible: autofill fields for N/A
                        if (newState == YesNoState.NA) {
                            viewModel.setSampleCertificateNumberLargeMetal("N/A")
                        } else if (certNo == "N/A") {
                            viewModel.setSampleCertificateNumberLargeMetal("")
                        }
                    },
                    helpText = "Select if there was satisfactory Detection and Rejection of the metal sample and note the certificate number",
                    inputLabel = "Cert Number",
                    inputValue = certNo,
                    onInputValueChange = viewModel::setSampleCertificateNumberLargeMetal,
                    inputMaxLength = 12,
                    inputKeyboardType = KeyboardType.Text,
                    pvStatus = if (pvRequired) rules.find { it.ruleId == "LARGE_METAL_DETECT_REJECT" }?.status?.name else null,
                    pvRules = if (pvRequired) rules.filter { it.ruleId == "LARGE_METAL_DETECT_REJECT" } else emptyList()
                )

                FormSpacer()

                // -----------------------------------------------------
                // PV RESULT (only when required)
                // -----------------------------------------------------
                if (pvRequired) {
                     PvSectionSummaryCard(
                            title = "Large metal test P.V. Summary",
                            rules = rules
                        )

                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = notes,
                    onValueChange = viewModel::setLargeMetalTestEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
