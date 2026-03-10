package com.example.mecca.screens.service.mdCalibration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateSmePvResult
import com.example.mecca.calibrationLogic.metalDetectorConveyor.calculateOverallStatus
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getSmePvRules
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.*
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSmeDetails(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val operatorTestWitnessed by viewModel.operatorTestWitnessed
    val operatorName by viewModel.operatorName

    val ferrousSize by viewModel.operatorTestResultFerrous
    val nonFerrousSize by viewModel.operatorTestResultNonFerrous
    val stainlessSize by viewModel.operatorTestResultStainless
    val largeMetalSize by viewModel.operatorTestResultLargeMetal

    val ferrousCert by viewModel.operatorTestResultCertNumberFerrous
    val nonFerrousCert by viewModel.operatorTestResultCertNumberNonFerrous
    val stainlessCert by viewModel.operatorTestResultCertNumberStainless
    val largeMetalCert by viewModel.operatorTestResultCertNumberLargeMetal

    val smeName by viewModel.smeName


    val infeed by viewModel.operatorTestWitnessedInfeed
    val rejectConfirm by viewModel.operatorTestWitnessedRejectConfirm
    val binFull by viewModel.operatorTestWitnessedBinFull
    val binDoor by viewModel.operatorTestWitnessedBinDoor
    val airFail by viewModel.operatorTestWitnessedAirFail




    val notes by viewModel.smeEngineerNotes

    // Warning Logic
    val ferReq = viewModel.sensitivityRequirementFerrous.value.replace(",", ".").toDoubleOrNull() ?: 0.0
    val ferAchieved = ferrousSize.replace(",", ".").toDoubleOrNull() ?: 0.0
    val ferWarning = ferAchieved > ferReq && ferAchieved > 0.0 && ferReq > 0.0

    val nferReq = viewModel.sensitivityRequirementNonFerrous.value.replace(",", ".").toDoubleOrNull() ?: 0.0
    val nferAchieved = nonFerrousSize.replace(",", ".").toDoubleOrNull() ?: 0.0
    val nferWarning = nferAchieved > nferReq && nferAchieved > 0.0 && nferReq > 0.0

    val ssReq = viewModel.sensitivityRequirementStainless.value.replace(",", ".").toDoubleOrNull() ?: 0.0
    val ssAchieved = stainlessSize.replace(",", ".").toDoubleOrNull() ?: 0.0
    val ssWarning = ssAchieved > ssReq && ssAchieved > 0.0 && ssReq > 0.0

    // Next enabled
    val isNextStepEnabled = when (operatorTestWitnessed) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> {
            operatorName.isNotBlank() &&
                    ferrousSize.isNotBlank() &&
                    nonFerrousSize.isNotBlank() &&
                    stainlessSize.isNotBlank() &&
                    largeMetalSize.isNotBlank() &&
                    ferrousCert.isNotBlank() &&
                    nonFerrousCert.isNotBlank() &&
                    stainlessCert.isNotBlank() &&
                    largeMetalCert.isNotBlank() &&
                    smeName.isNotBlank()
        }
        else -> false
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    // PV Rules Calculation
    val rules = viewModel.getSmePvRules()

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Operator Test")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                LabeledTriStateSwitchWithHelp(
                    label = "Operator Test Witnessed?",
                    currentState = operatorTestWitnessed,
                    onStateChange = { newState ->
                        viewModel.setOperatorTestWitnessed(newState)

                        if (newState == YesNoState.NA || newState == YesNoState.NO) {
                            viewModel.setOperatorName("N/A")
                            viewModel.setOperatorTestResultFerrous("N/A")
                            viewModel.setOperatorTestResultNonFerrous("N/A")
                            viewModel.setOperatorTestResultStainless("N/A")
                            viewModel.setOperatorTestResultLargeMetal("N/A")
                            viewModel.setOperatorTestResultCertNumberFerrous("N/A")
                            viewModel.setOperatorTestResultCertNumberNonFerrous("N/A")
                            viewModel.setOperatorTestResultCertNumberStainless("N/A")
                            viewModel.setOperatorTestResultCertNumberLargeMetal("N/A")
                            viewModel.setSmeName("N/A")
                        } else if (newState == YesNoState.YES) {
                            viewModel.setOperatorName("")
                            viewModel.setOperatorTestResultFerrous("")
                            viewModel.setOperatorTestResultNonFerrous("")
                            viewModel.setOperatorTestResultStainless("")
                            viewModel.setOperatorTestResultLargeMetal("20.0") // Default as requested
                            viewModel.setOperatorTestResultCertNumberFerrous("")
                            viewModel.setOperatorTestResultCertNumberNonFerrous("")
                            viewModel.setOperatorTestResultCertNumberStainless("")
                            viewModel.setOperatorTestResultCertNumberLargeMetal("")
                            viewModel.setSmeName("")
                        }

                        viewModel.autoUpdateSmePvResult()
                    },
                    helpText = "If you witnessed an operator perform a successful sensitivity check, select Yes. Otherwise select No or N/A.",
                    pvStatus = if (viewModel.pvRequired.value) {
                        rules.firstOrNull { it.description.contains("witnessed", ignoreCase = true) }?.status?.name
                    } else null,
                    pvRules = rules.filter { it.description.contains("witnessed", ignoreCase = true) }
                )

                FormSpacer()

                if (operatorTestWitnessed == YesNoState.YES) {

                    LabeledTextFieldWithHelp(
                        label = "Operator Name",
                        value = operatorName,
                        onValueChange = {
                            viewModel.setOperatorName(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        helpText = "Enter the name of the operator in charge of this system",
                        isNAToggleEnabled = false,
                        pvStatus = if (viewModel.pvRequired.value) {
                            rules.firstOrNull { it.description.contains("witnessed", ignoreCase = true) }?.status?.name
                        } else null,
                        pvRules = rules.filter { it.description.contains("witnessed", ignoreCase = true) },
                        maxLength = 25
                    )

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Ferrous Test",
                        firstInputLabel = "Size (mm)",
                        firstInputValue = ferrousSize,
                        onFirstInputValueChange = {
                            viewModel.setOperatorTestResultFerrous(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        secondInputLabel = "Certificate No.",
                        secondInputValue = ferrousCert,
                        onSecondInputValueChange = {
                            viewModel.setOperatorTestResultCertNumberFerrous(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        helpText = "Enter the operator test size and certificate number for Ferrous.",
                        firstInputKeyboardType = KeyboardType.Decimal,
                        secondInputKeyboardType = KeyboardType.Text,
                        isNAToggleEnabled = false,
                        pvStatus = if (viewModel.pvRequired.value) {
                            rules.firstOrNull { it.description.contains("Ferrous", ignoreCase = true) }?.status?.name
                        } else null,
                        pvRules = rules.filter { it.description.contains("Ferrous", ignoreCase = true) },
                        firstMaxLength = 4,
                        secondMaxLength = 12
                    )

                    if (ferWarning) {
                        SensitivityWarningBox(ferAchieved, ferReq)
                    }

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Non Ferrous Test",
                        firstInputLabel = "Size (mm)",
                        firstInputValue = nonFerrousSize,
                        onFirstInputValueChange = {
                            viewModel.setOperatorTestResultNonFerrous(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        secondInputLabel = "Certificate No.",
                        secondInputValue = nonFerrousCert,
                        onSecondInputValueChange = {
                            viewModel.setOperatorTestResultCertNumberNonFerrous(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        helpText = "Enter the operator test size and certificate number for Non-Ferrous.",
                        firstInputKeyboardType = KeyboardType.Decimal,
                        secondInputKeyboardType = KeyboardType.Text,
                        isNAToggleEnabled = false,
                        pvStatus = if (viewModel.pvRequired.value) {
                            rules.firstOrNull { it.description.contains("Non-Ferrous", ignoreCase = true) }?.status?.name
                        } else null,
                        pvRules = rules.filter { it.description.contains("Non-Ferrous", ignoreCase = true) },
                        firstMaxLength = 4,
                        secondMaxLength = 12
                    )

                    if (nferWarning) {
                        SensitivityWarningBox(nferAchieved, nferReq)
                    }

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Stainless Test",
                        firstInputLabel = "Size (mm)",
                        firstInputValue = stainlessSize,
                        onFirstInputValueChange = {
                            viewModel.setOperatorTestResultStainless(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        secondInputLabel = "Certificate No.",
                        secondInputValue = stainlessCert,
                        onSecondInputValueChange = {
                            viewModel.setOperatorTestResultCertNumberStainless(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        helpText = "Enter the operator test size and certificate number for Stainless.",
                        firstInputKeyboardType = KeyboardType.Decimal,
                        secondInputKeyboardType = KeyboardType.Text,
                        isNAToggleEnabled = false,
                        pvStatus = if (viewModel.pvRequired.value) {
                            rules.firstOrNull { it.description.contains("Stainless", ignoreCase = true) }?.status?.name
                        } else null,
                        pvRules = rules.filter { it.description.contains("Stainless", ignoreCase = true) },
                        firstMaxLength = 4,
                        secondMaxLength = 12
                    )

                    if (ssWarning) {
                        SensitivityWarningBox(ssAchieved, ssReq)
                    }

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Large Metal",
                        firstInputLabel = "Size (mm)",
                        firstInputValue = largeMetalSize,
                        onFirstInputValueChange = {
                            viewModel.setOperatorTestResultLargeMetal(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        secondInputLabel = "Certificate No.",
                        secondInputValue = largeMetalCert,
                        onSecondInputValueChange = {
                            viewModel.setOperatorTestResultCertNumberLargeMetal(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        helpText = "Enter the operator test size and certificate number for Large Metal.",
                        firstInputKeyboardType = KeyboardType.Decimal,
                        secondInputKeyboardType = KeyboardType.Text,
                        isNAToggleEnabled = false,
                        pvStatus = if (viewModel.pvRequired.value) {
                            rules.firstOrNull { it.description.contains("Large metal", ignoreCase = true) }?.status?.name
                        } else null,
                        pvRules = rules.filter { it.description.contains("Large metal", ignoreCase = true) },
                        firstMaxLength = 4,
                        secondMaxLength = 12
                    )



                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Infeed Sensor Test",
                        currentState = infeed,
                        onStateChange = { newState ->
                            viewModel.setOperatorTestWitnessedInfeed(newState)
                        },
                        isNAToggleEnabled = false,
                        helpText = "If you witnessed an operator perform a successful Infeed Sensor check, select Yes. Otherwise select No or N/A.",
                        pvStatus = if (viewModel.pvRequired.value) {
                            rules.firstOrNull { it.description.contains("Infeed", ignoreCase = true) }?.status?.name
                        } else null,
                        pvRules = rules.filter { it.description.contains("Infeed", ignoreCase = true) }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Reject Confirm Test",
                        currentState = rejectConfirm,
                        onStateChange = { newState ->
                            viewModel.setOperatorTestWitnessedRejectConfirm(newState)
                        },
                        isNAToggleEnabled = false,
                        helpText = "If you witnessed an operator perform a successful Reject Confirm check, select Yes. Otherwise select No or N/A.",
                        pvStatus = if (viewModel.pvRequired.value) {
                            rules.firstOrNull { it.description.contains("Confirm", ignoreCase = true) }?.status?.name
                        } else null,
                        pvRules = rules.filter { it.description.contains("Confirm", ignoreCase = true) }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Bin Full Test",
                        currentState = binFull,
                        onStateChange = { newState ->
                            viewModel.setOperatorTestWitnessedBinFull(newState)
                        },
                        isNAToggleEnabled = false,
                        helpText = "If you witnessed an operator perform a successful Bin Full check, select Yes. Otherwise select No or N/A.",
                        pvStatus = if (viewModel.pvRequired.value) {
                            rules.firstOrNull { it.description.contains("Full", ignoreCase = true) }?.status?.name
                        } else null,
                        pvRules = rules.filter { it.description.contains("Full", ignoreCase = true) }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Bin Door Test",
                        currentState = binDoor,
                        onStateChange = { newState ->
                            viewModel.setOperatorTestWitnessedBinDoor(newState)
                        },
                        isNAToggleEnabled = false,
                        helpText = "If you witnessed an operator perform a successful Bin Door check, select Yes. Otherwise select No or N/A.",
                        pvStatus = if (viewModel.pvRequired.value) {
                            rules.firstOrNull { it.description.contains("Door", ignoreCase = true) }?.status?.name
                        } else null,
                        pvRules = rules.filter { it.description.contains("Door", ignoreCase = true) }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Air Fail Test",
                        currentState = airFail,
                        onStateChange = { newState ->
                            viewModel.setOperatorTestWitnessedAirFail(newState)
                        },
                        isNAToggleEnabled = false,
                        helpText = "If you witnessed an operator perform a successful Air Fail check, select Yes. Otherwise select No or N/A.",
                        pvStatus = if (viewModel.pvRequired.value) {
                            rules.firstOrNull { it.description.contains("Air", ignoreCase = true) }?.status?.name
                        } else null,
                        pvRules = rules.filter { it.description.contains("Air", ignoreCase = true) }
                    )

                    FormSpacer()

                    LabeledTextFieldWithHelp(
                        label = "On Site SME Name",
                        value = smeName,
                        onValueChange = {
                            viewModel.setSmeName(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        helpText = "Enter the name of the SME currently on site",
                        isNAToggleEnabled = false,
                        pvStatus = if (viewModel.pvRequired.value) {
                            rules.firstOrNull { it.description.contains("SME", ignoreCase = true) }?.status?.name
                        } else null,
                        pvRules = rules.filter { it.description.contains("SME", ignoreCase = true) },
                        maxLength = 25
                    )
                    FormSpacer()

                }

                if (viewModel.pvRequired.value) {
                    PvSectionSummaryCard(
                        title = "Operator Test P.V. Summary",
                        rules = rules
                    )
                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setSmeEngineerNotes,
                    helpText = "Enter any notes relevant to this section",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun SensitivityWarningBox(achieved: Double, required: Double) {
    Spacer(Modifier.height(4.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.small)
            .padding(8.dp)
    ) {
        Text(
            text = "⚠️ Achieved sensitivity ($achieved mm) is worse than Customer Requirement ($required mm).",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
