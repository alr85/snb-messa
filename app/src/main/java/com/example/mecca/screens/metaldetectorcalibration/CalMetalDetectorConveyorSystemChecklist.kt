package com.example.mecca.screens.metaldetectorcalibration


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.CalibrationBanner
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.ConditionState
import com.example.mecca.formModules.LabeledTextFieldWithConditionToggle
import com.example.mecca.formModules.LabeledTextFieldWithHelp

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSystemChecklist(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel()
) {
    // Stops the next button from being pressed until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }

    val progress = viewModel.progress
    val scrollState = rememberScrollState()

    // Get and update data in the ViewModel

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


    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled =
        beltCondition != ConditionState.UNSPECIFIED &&
                guardCondition != ConditionState.UNSPECIFIED &&
                safetyCircuitCondition != ConditionState.UNSPECIFIED &&
                linerCondition != ConditionState.UNSPECIFIED &&
                cablesCondition != ConditionState.UNSPECIFIED &&
                screwsCondition != ConditionState.UNSPECIFIED


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        CalibrationBanner(
            progress = progress,
            viewModel = viewModel
        )

        // Navigation Buttons
        CalibrationNavigationButtons(
            onPreviousClick = { viewModel.updateSystemChecklist() },
            onCancelClick = { viewModel.updateSystemChecklist() },
            onNextClick ={
            viewModel.updateSystemChecklist()
            navController.navigate("CalMetalDetectorConveyorIndicators") },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false,
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateSystemChecklist()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("System Checklist")

        Spacer(modifier = Modifier.height(20.dp))


        LabeledTextFieldWithConditionToggle(
            title = "Conveyor Belt",
            label = "Comments",
            value = beltConditionComments,
            onValueChange = { newText -> viewModel.setBeltConditionComments( newText ) },
            helpText = "Enter the condition, and any extra comments regarding the Conveyor Belt. Comments should include details about the type of Conveyor Belt, and any defects that need to be fixed.",
            conditionLabel = "Condition",
            currentCondition = beltCondition,
            onConditionChange = { newCondition -> viewModel.setBeltCondition(newCondition)},
            keyboardType = KeyboardType.Text,
            isNAToggleEnabled = true, // Set this to false if you don't need the N/A toggle
            helper = "Conveyor belts should be clean, and free from tears or missing sections. Check for correct tracking alignment"
        )

        Spacer(modifier = Modifier.padding(16.dp))

        LabeledTextFieldWithConditionToggle(
            title = "Guarding",
            label = "Comments",
            value = guardConditionComments,
            onValueChange =  { newText -> viewModel.setGuardConditionComments( newText ) },
            helpText = "Enter the condition, and any extra comments regarding the Guarding. Comments should include details about the type of Guarding, and any defects that need to be fixed.",
            conditionLabel = "Condition",
            currentCondition = guardCondition,
            onConditionChange = { newCondition -> viewModel.setGuardCondition(newCondition)},
            keyboardType = KeyboardType.Text,
            isNAToggleEnabled = true, // Set this to false if you don't need the N/A toggle
            helper = "Guarding should be intact (no cracks or damage), and cover the area from the search head to the end of the conveyor belt as a minimum. Any access hatches should be interlocked as part of the safety circuit"
        )

        Spacer(modifier = Modifier.padding(16.dp))

        LabeledTextFieldWithConditionToggle(
            title = "Safety Circuit",
            label = "Comments",
            value = safetyCircuitConditionComments,
            onValueChange = { newText -> viewModel.setSafetyCircuitConditionComments( newText ) },
            helpText = "Enter the condition, and any extra comments regarding the Safety Circuit. Comments should include details about the type of Safety Circuit, and any defects that need to be fixed.",
            conditionLabel = "Condition",
            currentCondition = safetyCircuitCondition,
            onConditionChange = { newCondition -> viewModel.setSafetyCircuitCondition(newCondition)},
            keyboardType = KeyboardType.Text,
            isNAToggleEnabled = true, // Set this to false if you don't need the N/A toggle
            helper = "Check all emergency stops, guard interlocks etc for correct operation"
        )

        Spacer(modifier = Modifier.padding(16.dp))

        LabeledTextFieldWithConditionToggle(
            title = "Detector Liner, Gaskets and Seals",
            label = "Comments",
            value = linerConditionComments,
            onValueChange = { newText -> viewModel.setLinerConditionComments( newText ) },
            helpText = "Enter the condition, and any extra comments regarding the Detector Liner, Gaskets and Seals. Comments should include details about the type of Detector Liner, Gaskets and Seals, and any defects that need to be fixed.",
            conditionLabel = "Condition",
            currentCondition = linerCondition,
            onConditionChange = { newCondition -> viewModel.setLinerCondition(newCondition)},
            keyboardType = KeyboardType.Text,
            isNAToggleEnabled = true,
            helper = "The search head liner (AKA chute) should be checked for damage and wear. Any gaskets or seals on control panels/electrical enclosures should also be checked"
        )

        Spacer(modifier = Modifier.padding(16.dp))

        LabeledTextFieldWithConditionToggle(
            title = "Cable Fittings",
            label = "Comments",
            value = cablesConditionComments,
            onValueChange = { newText -> viewModel.setCablesConditionComments( newText ) },
            helpText = "Enter the condition, and any extra comments regarding the Cable Fittings. Comments should include details about the type of Cable Fittings, and any defects that need to be fixed.",
            conditionLabel = "Condition",
            currentCondition = cablesCondition,
            onConditionChange = { newCondition -> viewModel.setCablesCondition(newCondition)},
            keyboardType = KeyboardType.Text,
            isNAToggleEnabled = true,
            helper = "Check all cables and associated fittings for damage and wear"
        )

        Spacer(modifier = Modifier.padding(16.dp))

        LabeledTextFieldWithConditionToggle(
            title = "Screws and Fittings",
            label = "Comments",
            value = screwsConditionComments,
            onValueChange = { newText -> viewModel.setScrewsConditionComments( newText ) },
            helpText = "Enter the condition, and any extra comments regarding the Screws and Fittings. Comments should include details about the type of Screws and Fittings, and any defects that need to be fixed.",
            conditionLabel = "Condition",
            currentCondition = screwsCondition,
            onConditionChange = { newCondition -> viewModel.setScrewsCondition(newCondition)},
            keyboardType = KeyboardType.Text,
            isNAToggleEnabled = true,
            helper = "Check all screws and fittings for damage and wear. Screws must be tight"
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Notes",
            value = systemChecklistEngineerNotes,
            onValueChange = { newValue -> viewModel.setSystemChecklistEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )


    }
}
