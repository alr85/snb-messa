package com.example.mecca.screens.metaldetectorcalibration


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.ConditionState
import com.example.mecca.formModules.LabeledTextFieldWithConditionToggle
import com.example.mecca.formModules.LabeledTextFieldWithHelp

@Composable
fun CalMetalDetectorConveyorSystemChecklist(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val scrollState = rememberScrollState()

    val beltCondition by viewModel.beltCondition
    val beltConditionComments by viewModel.beltConditionComments

    val guardCondition by viewModel.guardCondition
    val guardConditionComments by viewModel.guardConditionComments

    val safetyCircuitCondition by viewModel.safetyCircuitCondition
    val safetyCircuitConditionComments by viewModel.safetyCircuitConditionComments

    val linerCondition by viewModel.linerCondition
    val linerConditionComments by viewModel.linerConditionComments

    val cablesCondition by viewModel.cablesCondition
    val cablesConditionComments by viewModel.cablesConditionComments

    val screwsCondition by viewModel.screwsCondition
    val screwsConditionComments by viewModel.screwsConditionComments

    val systemChecklistEngineerNotes by viewModel.systemChecklistEngineerNotes

    // Next enabled
    val isNextStepEnabled =
        beltCondition != ConditionState.UNSPECIFIED &&
                guardCondition != ConditionState.UNSPECIFIED &&
                safetyCircuitCondition != ConditionState.UNSPECIFIED &&
                linerCondition != ConditionState.UNSPECIFIED &&
                cablesCondition != ConditionState.UNSPECIFIED &&
                screwsCondition != ConditionState.UNSPECIFIED

    // Tell wrapper
    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("System Checklist")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
                .imePadding()
        ) {

            LabeledTextFieldWithConditionToggle(
                title = "Conveyor Belt",
                label = "Comments",
                value = beltConditionComments,
                onValueChange = viewModel::setBeltConditionComments,
                helpText = "Enter the condition and any extra comments regarding the Conveyor Belt. Comments should include belt type and any defects that need to be fixed.",
                conditionLabel = "Condition",
                currentCondition = beltCondition,
                onConditionChange = viewModel::setBeltCondition,
                keyboardType = KeyboardType.Text,
                isNAToggleEnabled = true,
                helper = "Conveyor belts should be clean and free from tears or missing sections. Check correct tracking alignment."
            )

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextFieldWithConditionToggle(
                title = "Guarding",
                label = "Comments",
                value = guardConditionComments,
                onValueChange = viewModel::setGuardConditionComments,
                helpText = "Enter the condition and any extra comments regarding the Guarding. Comments should include guarding type and any defects that need to be fixed.",
                conditionLabel = "Condition",
                currentCondition = guardCondition,
                onConditionChange = viewModel::setGuardCondition,
                keyboardType = KeyboardType.Text,
                isNAToggleEnabled = true,
                helper = "Guarding should be intact and cover from the search head to the end of the belt as a minimum. Access hatches should be interlocked as part of the safety circuit."
            )

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextFieldWithConditionToggle(
                title = "Safety Circuit",
                label = "Comments",
                value = safetyCircuitConditionComments,
                onValueChange = viewModel::setSafetyCircuitConditionComments,
                helpText = "Enter the condition and any extra comments regarding the Safety Circuit. Include any defects that need to be fixed.",
                conditionLabel = "Condition",
                currentCondition = safetyCircuitCondition,
                onConditionChange = viewModel::setSafetyCircuitCondition,
                keyboardType = KeyboardType.Text,
                isNAToggleEnabled = true,
                helper = "Check all emergency stops, guard interlocks etc for correct operation."
            )

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextFieldWithConditionToggle(
                title = "Detector Liner, Gaskets and Seals",
                label = "Comments",
                value = linerConditionComments,
                onValueChange = viewModel::setLinerConditionComments,
                helpText = "Enter the condition and any extra comments regarding the Detector Liner, Gaskets and Seals. Include any defects that need to be fixed.",
                conditionLabel = "Condition",
                currentCondition = linerCondition,
                onConditionChange = viewModel::setLinerCondition,
                keyboardType = KeyboardType.Text,
                isNAToggleEnabled = true,
                helper = "Check the search head liner (chute) for damage and wear. Check gaskets/seals on control panels and electrical enclosures."
            )

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextFieldWithConditionToggle(
                title = "Cable Fittings",
                label = "Comments",
                value = cablesConditionComments,
                onValueChange = viewModel::setCablesConditionComments,
                helpText = "Enter the condition and any extra comments regarding Cable Fittings. Include any defects that need to be fixed.",
                conditionLabel = "Condition",
                currentCondition = cablesCondition,
                onConditionChange = viewModel::setCablesCondition,
                keyboardType = KeyboardType.Text,
                isNAToggleEnabled = true,
                helper = "Check all cables and fittings for damage and wear."
            )

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextFieldWithConditionToggle(
                title = "Screws and Fittings",
                label = "Comments",
                value = screwsConditionComments,
                onValueChange = viewModel::setScrewsConditionComments,
                helpText = "Enter the condition and any extra comments regarding Screws and Fittings. Include any defects that need to be fixed.",
                conditionLabel = "Condition",
                currentCondition = screwsCondition,
                onConditionChange = viewModel::setScrewsCondition,
                keyboardType = KeyboardType.Text,
                isNAToggleEnabled = true,
                helper = "Check all screws and fittings for damage and wear. Screws must be tight."
            )

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextFieldWithHelp(
                label = "Engineer Notes",
                value = systemChecklistEngineerNotes,
                onValueChange = viewModel::setSystemChecklistEngineerNotes,
                helpText = "Enter any notes relevant to this section",
                isNAToggleEnabled = false
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

