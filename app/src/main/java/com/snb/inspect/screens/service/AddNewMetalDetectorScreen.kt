package com.snb.inspect.screens.service

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.snb.inspect.FetchResult
import com.snb.inspect.repositories.MetalDetectorModelsRepository
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.repositories.SystemTypeRepository
import com.snb.inspect.dataClasses.MdModelsLocal
import com.snb.inspect.dataClasses.SystemTypeLocal
import com.snb.inspect.dataClasses.MetalDetectorWithFullDetails
import com.snb.inspect.formModules.LabeledDualNumberInputsWithHelp
import com.snb.inspect.formModules.LabeledObjectDropdownWithHelp
import com.snb.inspect.formModules.LabeledReadOnlyField
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.util.InAppLogger
import com.snb.inspect.util.SerialCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewMetalDetectorScreen(
    navController: NavHostController,
    systemTypeRepository: SystemTypeRepository,
    mdModelsRepository: MetalDetectorModelsRepository,
    mdSystemsRepository: MetalDetectorSystemsRepository,
    customerID: Int,
    customerName: String,
    snackbarHostState: SnackbarHostState

) {
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // Inputs
    var serialNumber by remember { mutableStateOf("") }
    var apertureWidth by remember { mutableStateOf("") }
    var apertureHeight by remember { mutableStateOf("") }
    var lastLocation by remember { mutableStateOf("") }

    // Dropdown data
    var systemTypes by remember { mutableStateOf<List<SystemTypeLocal>>(emptyList()) }
    var selectedSystemType by remember { mutableStateOf<SystemTypeLocal?>(null) }

    var mdModels by remember { mutableStateOf<List<MdModelsLocal>>(emptyList()) }
    var selectedMdModel by remember { mutableStateOf<MdModelsLocal?>(null) }

    // Submit state
    var isProcessing by remember { mutableStateOf(false) }
    var duplicateSystem by remember { mutableStateOf<MetalDetectorWithFullDetails?>(null) }
    var fuzzyMatchSystem by remember { mutableStateOf<MetalDetectorWithFullDetails?>(null) }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {

        systemTypes = systemTypeRepository.getSystemTypesFromDb()
            .filter { it.systemType.contains("MD", ignoreCase = true) }
            .sortedBy { it.systemType }

        mdModels = mdModelsRepository.getMdModelsFromDb()
            .sortedBy { it.modelDescription }
    }

    val isFormValid by remember(
        serialNumber,
        apertureWidth,
        apertureHeight,
        selectedSystemType,
        selectedMdModel,
        lastLocation
    ) {
        derivedStateOf {
            serialNumber.isNotBlank() &&
                    apertureWidth.isNotBlank() &&
                    apertureHeight.isNotBlank() &&
                    selectedSystemType != null &&
                    selectedMdModel != null &&
                    lastLocation.isNotBlank()
        }
    }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            LabeledReadOnlyField(
                label = "Customer",
                value = customerName,
                helpText = "This system will be added to the selected customer and cannot be changed here."
            )


            LabeledObjectDropdownWithHelp(
                label = "System Type",
                options = systemTypes,
                selectedOption = selectedSystemType,
                onSelectionChange = { selectedSystemType = it },
                optionLabel = { it.systemType },
                helpText = "Select the metal detector system type.",
                placeholder = "Select..."
            )

            LabeledObjectDropdownWithHelp(
                label = "Make/Model",
                options = mdModels,
                selectedOption = selectedMdModel,
                onSelectionChange = { selectedMdModel = it },
                optionLabel = { it.modelDescription },
                helpText = "Select the manufacturer and model.",
                placeholder = "Select..."
            )

            LabeledTextFieldWithHelp(
                label = "Serial Number",
                value = serialNumber,
                onValueChange = { raw ->
                    serialNumber = raw.replace(Regex("[^A-Za-z0-9 _.-]"), "").uppercase()
                },
                helpText = "Enter the serial number (A-Z / 0-9 / space / _ . -).",
                keyboardType = KeyboardType.Text,
                isNAToggleEnabled = false
            )

            LabeledDualNumberInputsWithHelp(
                label = "Aperture Size (mm)",
                firstLabel = "Width",
                firstValue = apertureWidth,
                onFirstValueChange = { apertureWidth = it.filter(Char::isDigit) },
                secondLabel = "Height",
                secondValue = apertureHeight,
                onSecondValueChange = { apertureHeight = it.filter(Char::isDigit) },
                helpText = "Enter aperture width and height in millimetres."
            )

            LabeledTextFieldWithHelp(
                label = "Location Ref",
                value = lastLocation,
                onValueChange = { raw ->
                    lastLocation = raw.replace(Regex("[^A-Za-z0-9 _.-]"), "").uppercase()
                },
                helpText = "Enter the site location reference.",
                keyboardType = KeyboardType.Text,
                isNAToggleEnabled = false
            )


            Spacer(modifier = Modifier.height(8.dp))

            // Submit
            Button(
                onClick = {
                    keyboardController?.hide()
                    if (!isFormValid || isProcessing) return@Button

                    isProcessing = true
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            submitNewMdSystem(
                                context = context,
                                customerID = customerID,
                                serialNumber = serialNumber,
                                apertureWidth = apertureWidth.toInt(),
                                apertureHeight = apertureHeight.toInt(),
                                systemTypeId = selectedSystemType!!.id,
                                modelId = selectedMdModel!!.meaId,
                                lastLocation = lastLocation,
                                mdSystemsRepository = mdSystemsRepository,
                                snackbarHostState = snackbarHostState,
                                navController = navController,
                                onDuplicateFound = { duplicateSystem = it },
                                onFuzzyMatchFound = { fuzzyMatchSystem = it }
                            )
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing && isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Add Metal Detector")
                }
            }
        }

    // Exact Duplicate Serial Dialog
    duplicateSystem?.let { system ->
        AlertDialog(
            onDismissRequest = { duplicateSystem = null },
            title = { Text("Serial Number Already Exists") },
            text = {
                Column {
                    Text("The serial number ")
                    Text(text = system.serialNumber, fontWeight = FontWeight.Bold)
                    Text(" is already assigned to:")
                    Spacer(Modifier.height(8.dp))
                    Text("Customer: ", fontWeight = FontWeight.Bold)
                    Text(system.customerName)
                    Spacer(Modifier.height(4.dp))
                    Text("Location: ", fontWeight = FontWeight.Bold)
                    Text(system.lastLocation)
                    Spacer(Modifier.height(12.dp))
                    Text("Please contact the office if this machine needs to be moved to a different customer.")
                }
            },
            confirmButton = {
                TextButton(onClick = { duplicateSystem = null }) {
                    Text("OK")
                }
            }
        )
    }

    // Fuzzy Match Dialog
    fuzzyMatchSystem?.let { system ->
        AlertDialog(
            onDismissRequest = { fuzzyMatchSystem = null },
            title = { Text("Similar Serial Found") },
            text = {
                Column {
                    Text("The serial you entered is very similar to an existing one:")
                    Spacer(Modifier.height(8.dp))
                    Text("Existing Serial: ", fontWeight = FontWeight.Bold)
                    Text(system.serialNumber)
                    Text("Customer: ", fontWeight = FontWeight.Bold)
                    Text(system.customerName)
                    Text("Location: ", fontWeight = FontWeight.Bold)
                    Text(system.lastLocation)
                    Spacer(Modifier.height(12.dp))
                    Text("Is this the same machine? (e.g. a typo in the serial number)")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // User confirms it IS the same machine -> Block
                        fuzzyMatchSystem = null
                    }
                ) {
                    Text("Yes, this is the same machine")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // User says it's different -> Proceed
                        val sys = fuzzyMatchSystem!!
                        fuzzyMatchSystem = null
                        coroutineScope.launch(Dispatchers.IO) {
                            proceedWithCreation(
                                context = context,
                                repo = mdSystemsRepository,
                                customerID = customerID,
                                serialNumber = serialNumber,
                                apertureWidth = apertureWidth.toInt(),
                                apertureHeight = apertureHeight.toInt(),
                                systemTypeId = selectedSystemType!!.id,
                                modelId = selectedMdModel!!.meaId,
                                lastLocation = lastLocation,
                                snackbar = snackbarHostState,
                                navController = navController
                            )
                        }
                    }
                ) {
                    Text("No, this is a different machine")
                }
            }
        )
    }
}



suspend fun submitNewMdSystem(
    context: Context,
    customerID: Int,
    serialNumber: String,
    apertureWidth: Int,
    apertureHeight: Int,
    systemTypeId: Int,
    modelId: Int,
    lastLocation: String,
    mdSystemsRepository: MetalDetectorSystemsRepository,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    onDuplicateFound: (MetalDetectorWithFullDetails?) -> Unit,
    onFuzzyMatchFound: (MetalDetectorWithFullDetails?) -> Unit
) {
    InAppLogger.d("Adding new MD system...")
    when (val status = mdSystemsRepository.checkSerialNumberStatus(context, serialNumber)) {
        is SerialCheckResult.Exists -> {
            withContext(Dispatchers.Main) {
                onDuplicateFound(status.system)
                snackbarHostState.showSnackbar("⚠️ This serial number already exists.")
            }
            return
        }
        is SerialCheckResult.FuzzyMatch -> {
            withContext(Dispatchers.Main) {
                onFuzzyMatchFound(status.system)
            }
            return
        }
        SerialCheckResult.NotFound, SerialCheckResult.NotFoundLocalOffline -> {
            proceedWithCreation(
                context, mdSystemsRepository, customerID, serialNumber,
                apertureWidth, apertureHeight, systemTypeId, modelId, lastLocation,
                snackbarHostState, navController
            )
        }
        is SerialCheckResult.ExistsLocalOffline -> {
            withContext(Dispatchers.Main) {
                onDuplicateFound(status.system)
                snackbarHostState.showSnackbar("⚠️ Offline: serial exists locally.")
            }
            return
        }
        is SerialCheckResult.Error -> {
            snackbarHostState.showSnackbar("⚠️ Error: ${status.message ?: "network error"}")
        }
    }
}

suspend fun proceedWithCreation(
    context: Context,
    repo: MetalDetectorSystemsRepository,
    customerID: Int,
    serialNumber: String,
    apertureWidth: Int,
    apertureHeight: Int,
    systemTypeId: Int,
    modelId: Int,
    lastLocation: String,
    snackbar: SnackbarHostState,
    navController: NavHostController
) {
    if (com.snb.inspect.network.isNetworkAvailable(context)) {
        createInCloud(
            repo = repo,
            customerID = customerID,
            serialNumber = serialNumber,
            apertureWidth = apertureWidth,
            apertureHeight = apertureHeight,
            systemTypeId = systemTypeId,
            modelId = modelId,
            lastLocation = lastLocation,
            snackbar = snackbar,
            navController = navController
        )
    } else {
        createInLocal(
            repo = repo,
            customerID = customerID,
            serialNumber = serialNumber,
            apertureWidth = apertureWidth,
            apertureHeight = apertureHeight,
            systemTypeId = systemTypeId,
            modelId = modelId,
            lastLocation = lastLocation,
            snackbar = snackbar,
            navController = navController
        )
    }
}

suspend fun createInCloud(
    repo: MetalDetectorSystemsRepository,
    customerID: Int,
    serialNumber: String,
    apertureWidth: Int,
    apertureHeight: Int,
    systemTypeId: Int,
    modelId: Int,
    lastLocation: String,
    snackbar: SnackbarHostState,
    navController: NavHostController
) = withContext(Dispatchers.IO) {
    try {
        val newCloudId = repo.addMetalDetectorToCloud(
            customerID = customerID,
            serialNumber = serialNumber,
            apertureWidth = apertureWidth,
            apertureHeight = apertureHeight,
            systemTypeId = systemTypeId,
            modelId = modelId,
            calibrationInterval = 0,
            lastLocation = lastLocation
        )

        if (newCloudId != null && newCloudId != 0) {
            when (val result = repo.fetchAndStoreMdSystems()) {
                is FetchResult.Success -> Log.d("NewMD", "Cloud sync OK: ${result.message}")
                is FetchResult.Failure -> Log.e("NewMD", "Cloud sync failed: ${result.errorMessage}")
            }
            withContext(Dispatchers.Main) {
                snackbar.showSnackbar("✅ System added to cloud")
                navController.popBackStack()
            }
        } else {
            withContext(Dispatchers.Main) { snackbar.showSnackbar("❌ Failed to add system to cloud.") }
        }
    } catch (e: Exception) {
        Log.e("NewMD", "Cloud create failed", e)
        withContext(Dispatchers.Main) {
            snackbar.showSnackbar("❌ Failed to add system to cloud. Try again later.")
        }
    }
}

suspend fun createInLocal(
    repo: MetalDetectorSystemsRepository,
    customerID: Int,
    serialNumber: String,
    apertureWidth: Int,
    apertureHeight: Int,
    systemTypeId: Int,
    modelId: Int,
    lastLocation: String,
    snackbar: SnackbarHostState,
    navController: NavHostController
) = withContext(Dispatchers.IO) {
    try {
        repo.addMetalDetectorToLocalDb(
            customerID = customerID,
            serialNumber = serialNumber,
            apertureWidth = apertureWidth,
            apertureHeight = apertureHeight,
            systemTypeId = systemTypeId,
            modelId = modelId,
            lastLocation = lastLocation,
            calibrationInterval = 0
        )
        withContext(Dispatchers.Main) {
            snackbar.showSnackbar("✅ System added locally (offline)")
            navController.popBackStack()
        }
    } catch (e: Exception) {
        Log.e("NewMD", "Local create failed", e)
        withContext(Dispatchers.Main) {
            snackbar.showSnackbar("❌ Failed to add system locally.")
        }
    }
}
