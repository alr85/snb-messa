package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorProductDetails(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel()
) {
    // Stops the next button from being pressed until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }

    //val progress = viewModel.progress
    val scrollState = rememberScrollState()
    val sensitivityData = viewModel.sensitivityData.value

// Get and update data in the ViewModel
    val productDescription by viewModel.productDescription
    val productLibraryReference by viewModel.productLibraryReference
    val productLibraryNumber by viewModel.productLibraryNumber
    val productLength by viewModel.productLength
    val productWidth by viewModel.productWidth
    val productHeight by viewModel.productHeight
    val productDetailsEngineerNotes by viewModel.productDetailsEngineerNotes

    // State to control the visibility of the expandable section
    var expanded by remember { mutableStateOf(false) }

    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled =
        productDescription.isNotBlank() &&
                productLibraryReference.isNotBlank() &&
                productLibraryNumber.isNotBlank() &&
                productLength.isNotBlank() &&
                productLength.isNotBlank() &&
                productWidth.isNotBlank() &&
                productHeight.isNotBlank()


    Column(modifier = Modifier.fillMaxSize()) {

//        CalibrationBanner(
//            progress = progress,
//            viewModel = viewModel
//        )

//        CalibrationNavigationButtons(
//            onPreviousClick = { viewModel.updateProductDetails() },
//            onCancelClick = { viewModel.updateProductDetails() },
//            onNextClick = {
//                viewModel.updateProductDetails()
//                navController.navigate("CalMetalDetectorConveyorConveyorDetails")
//            },
//            isNextEnabled = isNextStepEnabled,
//            isFirstStep = false, // Indicates this is the first step and disables the Previous button
//            navController = navController,
//            viewModel = viewModel,
//            onSaveAndExitClick = {
//                //viewModel.saveCalibrationData() // Custom save logic here
//            })

        CalibrationHeader("Product Details")

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .imePadding()
        ) {


            // Product Description Row
            LabeledTextFieldWithHelp(
                label = "Product Description",
                value = productDescription,
                onValueChange = { newValue -> viewModel.setProductDescription(newValue) },
                helpText = "Enter the details of the product (e.g., 'GOLD BARS')"
            )

            LabeledTextFieldWithHelp(
                label = "Product Library Reference",
                value = productLibraryReference,
                onValueChange = { newValue -> viewModel.setProductLibraryReference(newValue) },
                helpText = "There is usually a 'Product Name' in the metal detector library. Enter this here"
            )

            LabeledTextFieldWithHelp(
                label = "Product Library Number",
                value = productLibraryNumber,
                onValueChange = { newValue -> viewModel.setProductLibraryNumber(newValue) },
                helpText = "There is usually a number in reference to the running product in the metal detector library. Enter this here"
            )

            LabeledTextFieldWithHelp(
                label = "Product Length (mm)",
                value = productLength,
                onValueChange = { newValue -> viewModel.setProductLength(newValue) },
                helpText = "Enter the length of the product in mm",
                keyboardType = KeyboardType.Number
            )

            LabeledTextFieldWithHelp(
                label = "Product Width (mm)",
                value = productWidth,
                onValueChange = { newValue -> viewModel.setProductWidth(newValue) },
                helpText = "Enter the width of the product in mm",
                keyboardType = KeyboardType.Number
            )
            LabeledTextFieldWithHelp(
                label = "Product Height (mm)",
                value = productHeight,
                onValueChange = { newValue -> viewModel.setProductHeight(newValue) },
                helpText = "Enter the height of the product in mm. This is required for PV calibration",
                keyboardType = KeyboardType.Number,
                isNAToggleEnabled = viewModel.pvRequired.value != true

            )

//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Expandable Section for Sensitivity Data
//            if (sensitivityData != null) {
//                IconButton(
//                    onClick = { expanded = !expanded },
//                    modifier = Modifier.padding(top = 8.dp)
//                ) {
//                    Icon(
//                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
//                        contentDescription = if (expanded) "Hide Sensitivities" else "Show Sensitivities"
//                    )
//                }
//
//                AnimatedVisibility(visible = expanded) {
//                    Column(modifier = Modifier.padding(top = 8.dp)) {
//                        Text("Ferrous Target: ${sensitivityData.FerrousTargetMM} mm")
//                        Text("Ferrous Max: ${sensitivityData.FerrousMaxMM} mm")
//                        Text("Non-Ferrous Target: ${sensitivityData.NonFerrousTargetMM} mm")
//                        Text("Non-Ferrous Max: ${sensitivityData.NonFerrousMaxMM} mm")
//                        Text("Stainless 316 Target: ${sensitivityData.Stainless316TargetMM} mm")
//                        Text("Stainless 316 Max: ${sensitivityData.Stainless316MaxMM} mm")
//                        Text("X-ray Stainless 316 Max: ${sensitivityData.XrayStainless316MaxMM} mm")
//                    }
//                }
//            } else {
//                Text("Enter a product height to view M&S Target Sensitivities")
//            }

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextFieldWithHelp(
                label = "Engineer Comments",
                value = productDetailsEngineerNotes,
                onValueChange = { newValue -> viewModel.setProductDetailsEngineerNotes(newValue) },
                helpText = "Enter any notes relevant to this section",
                isNAToggleEnabled = false
            )


        }

    }

}

