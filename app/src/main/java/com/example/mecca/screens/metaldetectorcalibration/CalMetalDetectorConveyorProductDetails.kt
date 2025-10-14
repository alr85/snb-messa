package com.example.mecca.screens.metaldetectorcalibration

import com.example.mecca.CalibrationBanner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
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

    val progress = viewModel.progress

// Get and update data in the ViewModel
    val productDescription by viewModel.productDescription
    val productLibraryReference by viewModel.productLibraryReference
    val productLibraryNumber by viewModel.productLibraryNumber
    val productLength by viewModel.productLength
    val productWidth by viewModel.productWidth
    val productHeight by viewModel.productHeight
    val productDetailsEngineerNotes by viewModel.productDetailsEngineerNotes



    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled =
        productDescription.isNotBlank() &&
                productLibraryReference.isNotBlank() &&
                productLibraryNumber.isNotBlank() &&
                productLength.isNotBlank() &&
                productLength.isNotBlank() &&
                productWidth.isNotBlank() &&
                productHeight.isNotBlank()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
            // Display the consistent banner at the top
            CalibrationBanner(
                progress = progress,
                viewModel = viewModel

            )

        //Spacer(modifier = Modifier.height(4.dp))

        // Navigation Buttons
        CalibrationNavigationButtons(
            onPreviousClick = { viewModel.updateProductDetails() },
            onCancelClick = { viewModel.updateProductDetails() },
            onNextClick = {
                viewModel.updateProductDetails()
                navController.navigate("CalMetalDetectorConveyorDetectionSettingsAsFound")
                          },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false, // Indicates this is the first step and disables the Previous button
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                //viewModel.saveCalibrationData() // Custom save logic here
            },

        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Product Details")

        Spacer(modifier = Modifier.height(20.dp))

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
            helpText = "Enter the height of the product in mm",
            keyboardType = KeyboardType.Number
        )

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

