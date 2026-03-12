package com.example.mecca.screens.service.mdCalibration


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
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getLargeMetalPvRules
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getSpeedSensorPvRules
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledFourOptionRadioWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.LabeledYesNoSegmentedSwitchAndTextInputWithHelp
import com.example.mecca.formModules.YesNoState
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

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

        // PV rules
        val rules = remember(
            dr,
            certNo,
            pvRequired

        ) {
            viewModel.getLargeMetalPvRules()
        }
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
                    label = "Detected & Rejected OK",
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
                    pvStatus = if (pvRequired) rules.find { it.ruleId == "SPEED_RESULT" }?.status?.name else null,
                    pvRules = if (pvRequired) rules.filter { it.ruleId == "SPEED_RESULT" } else emptyList()
                )

                FormSpacer()

//                LabeledTriStateSwitchWithHelp(
//                    label = "Detected & Rejected OK",
//                    currentState = dr,
//                    onStateChange = { newState ->
//                        viewModel.setDetectRejectLargeMetal(newState)
//
//                        // Optional but sensible: autofill fields for N/A
//                        if (newState == YesNoState.NA) {
//                            viewModel.setSampleCertificateNumberLargeMetal("N/A")
//                        } else if (certNo == "N/A") {
//                            viewModel.setSampleCertificateNumberLargeMetal("")
//                        }
//                    },
//                    helpText = "Select if there was satisfactory Detection and Rejection of the metal sample: Yes, No, or N/A."
//                )
//
//                FormSpacer()
//
//                LabeledTextFieldWithHelp(
//                    label = "Sample Certificate No.",
//                    value = certNo,
//                    onValueChange = viewModel::setSampleCertificateNumberLargeMetal,
//                    helpText = "Enter the metal test sample certificate number (usually on the test piece).",
//                    isNAToggleEnabled = false,
//                    maxLength = 12
//                )
//
//                FormSpacer()

                // -----------------------------------------------------
                // PV RESULT (only when required)
                // -----------------------------------------------------
                if (pvRequired) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        value = viewModel.largeMetalTestPvResult.value,
                        onValueChange = viewModel::setLargeMetalTestPvResult,
                        helpText = """
                        Auto-Pass rules:
                          • Det. & Rej. OK = Yes
                          • Certificate number entered

                        If D&R OK = No → auto-fail.
                        If D&R OK = N/A → PV = N/A.
                        You may override manually.
                    """.trimIndent(),
                        showNotFittedOption = false,
                        notFittedEnabled = false
                    )

                    FormSpacer()
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