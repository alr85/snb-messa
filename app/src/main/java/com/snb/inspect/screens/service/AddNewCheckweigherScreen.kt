package com.snb.inspect.screens.service

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.snb.inspect.dataClasses.CheckweigherWithFullDetails
import com.snb.inspect.dataClasses.CwModelsLocal
import com.snb.inspect.dataClasses.SystemTypeLocal
import com.snb.inspect.formModules.LabeledObjectDropdownWithHelp
import com.snb.inspect.formModules.LabeledReadOnlyField
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.repositories.CheckweigherSystemsRepository
import com.snb.inspect.repositories.SystemTypeRepository
import com.snb.inspect.util.InAppLogger
import com.snb.inspect.util.SerialCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewCheckweigherScreen(
    navController: NavHostController,
    systemTypeRepository: SystemTypeRepository,
    cwSystemsRepository: CheckweigherSystemsRepository,
    customerID: Int,
    customerName: String,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    var serialNumber by remember { mutableStateOf("") }
    var lastLocation by remember { mutableStateOf("") }

    var cwModels by remember { mutableStateOf<List<CwModelsLocal>>(emptyList()) }
    var selectedCwModel by remember { mutableStateOf<CwModelsLocal?>(null) }

    var isProcessing by remember { mutableStateOf(false) }
    var duplicateSystem by remember { mutableStateOf<CheckweigherWithFullDetails?>(null) }
    var fuzzyMatchSystem by remember { mutableStateOf<CheckweigherWithFullDetails?>(null) }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        // If models are empty, try to fetch them once
        if (cwSystemsRepository.getAllModels().isEmpty()) {
            cwSystemsRepository.fetchAndStoreCwModels()
        }
        cwModels = cwSystemsRepository.getAllModels()
            .sortedBy { it.modelDescription }
    }

    val isFormValid by remember(serialNumber, selectedCwModel, lastLocation) {
        derivedStateOf {
            serialNumber.isNotBlank() &&
            selectedCwModel != null &&
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
            helpText = "Customer for this system."
        )

        LabeledObjectDropdownWithHelp(
            label = "Make/Model",
            options = cwModels,
            selectedOption = selectedCwModel,
            onSelectionChange = { selectedCwModel = it },
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
            helpText = "Enter the serial number.",
            keyboardType = KeyboardType.Text,
            isNAToggleEnabled = false
        )

        LabeledTextFieldWithHelp(
            label = "Location Ref",
            value = lastLocation,
            onValueChange = { raw ->
                lastLocation = raw.replace(Regex("[^A-Za-z0-9 _.-]"), "").uppercase()
            },
            helpText = "Enter the site location reference.",
            keyboardType = KeyboardType.Text,
            isNAToggleEnabled = false,
            maxLength = 30
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                if (!isFormValid || isProcessing) return@Button
                isProcessing = true
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val cwSystemType = systemTypeRepository.getSystemTypesFromDb()
                            .firstOrNull { it.systemType.contains("CW", ignoreCase = true) }
                        
                        submitNewCwSystem(
                            context = context,
                            customerID = customerID,
                            serialNumber = serialNumber,
                            systemTypeId = cwSystemType?.id ?: 0,
                            modelId = selectedCwModel!!.meaId,
                            lastLocation = lastLocation,
                            cwSystemsRepository = cwSystemsRepository,
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
                Text("Add Checkweigher")
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
                TextButton(onClick = {
                    fuzzyMatchSystem = null
                    // User says no, it's a new machine - continue submission bypassing check
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            isProcessing = true
                            forceSubmitNewCwSystem(
                                context = context,
                                customerID = customerID,
                                serialNumber = serialNumber,
                                systemTypeId = selectedCwModel?.let {
                                    systemTypeRepository.getSystemTypesFromDb()
                                        .firstOrNull { it.systemType.contains("CW", ignoreCase = true) }?.id
                                } ?: 0,
                                modelId = selectedCwModel!!.meaId,
                                lastLocation = lastLocation,
                                cwSystemsRepository = cwSystemsRepository,
                                snackbarHostState = snackbarHostState,
                                navController = navController
                            )
                        } finally {
                            isProcessing = false
                        }
                    }
                }) {
                    Text("No, Add as New")
                }
            },
            dismissButton = {
                TextButton(onClick = { fuzzyMatchSystem = null }) {
                    Text("Yes, Cancel")
                }
            }
        )
    }
}

suspend fun submitNewCwSystem(
    context: Context,
    customerID: Int,
    serialNumber: String,
    systemTypeId: Int,
    modelId: Int,
    lastLocation: String,
    cwSystemsRepository: CheckweigherSystemsRepository,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    onDuplicateFound: (CheckweigherWithFullDetails?) -> Unit,
    onFuzzyMatchFound: (CheckweigherWithFullDetails?) -> Unit
) {
    InAppLogger.d("Adding new CW system...")
    when (val status = cwSystemsRepository.checkSerialNumberStatus(context, serialNumber)) {
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
        is SerialCheckResult.ExistsLocalOffline -> {
            withContext(Dispatchers.Main) {
                onDuplicateFound(status.system)
                snackbarHostState.showSnackbar("⚠️ Offline: Serial exists locally.")
            }
            return
        }
        is SerialCheckResult.Error -> {
            withContext(Dispatchers.Main) {
                snackbarHostState.showSnackbar("⚠️ Serial check error: ${status.message}")
            }
            // Proceed anyway if check fails? MD screen usually lets them proceed or shows error.
            // For now, let's proceed to allow adding if API is just down but system is unique.
        }
        SerialCheckResult.NotFound, SerialCheckResult.NotFoundLocalOffline -> {
            // OK to proceed
        }
    }

    forceSubmitNewCwSystem(
        context,
        customerID,
        serialNumber,
        systemTypeId,
        modelId,
        lastLocation,
        cwSystemsRepository,
        snackbarHostState,
        navController
    )
}

suspend fun forceSubmitNewCwSystem(
    context: Context,
    customerID: Int,
    serialNumber: String,
    systemTypeId: Int,
    modelId: Int,
    lastLocation: String,
    cwSystemsRepository: CheckweigherSystemsRepository,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController
) {
    if (com.snb.inspect.network.isNetworkAvailable(context)) {
        val success = createCwInCloud(
            repo = cwSystemsRepository,
            customerId = customerID,
            serialNumber = serialNumber,
            systemTypeId = systemTypeId,
            modelId = modelId,
            lastLocation = lastLocation,
            snackbar = snackbarHostState,
            navController = navController
        )
        if (success) return
        InAppLogger.d("Cloud CW creation failed, falling back to local...")
    }

    createCwInLocal(
        repo = cwSystemsRepository,
        customerId = customerID,
        serialNumber = serialNumber,
        systemTypeId = systemTypeId,
        modelId = modelId,
        lastLocation = lastLocation,
        snackbar = snackbarHostState,
        navController = navController
    )
}

suspend fun createCwInCloud(
    repo: CheckweigherSystemsRepository,
    customerId: Int,
    serialNumber: String,
    systemTypeId: Int,
    modelId: Int,
    lastLocation: String,
    snackbar: SnackbarHostState,
    navController: NavHostController
): Boolean = withContext(Dispatchers.IO) {
    try {
        val newCloudId = repo.addCheckweigherToCloud(
            customerId = customerId,
            serialNumber = serialNumber,
            systemTypeId = systemTypeId,
            modelId = modelId,
            calibrationInterval = 0,
            lastLocation = lastLocation
        )
        if (newCloudId != null && newCloudId != 0) {
            repo.fetchAndStoreCwSystems()
            withContext(Dispatchers.Main) {
                snackbar.showSnackbar("✅ Checkweigher added to cloud")
                navController.popBackStack()
            }
            return@withContext true
        } else {
            return@withContext false
        }
    } catch (e: Exception) {
        InAppLogger.e("Cloud create CW failed: ${e.message}")
        return@withContext false
    }
}

suspend fun createCwInLocal(
    repo: CheckweigherSystemsRepository,
    customerId: Int,
    serialNumber: String,
    systemTypeId: Int,
    modelId: Int,
    lastLocation: String,
    snackbar: SnackbarHostState,
    navController: NavHostController
) = withContext(Dispatchers.IO) {
    try {
        repo.addCheckweigherToLocalDb(
            customerId = customerId,
            serialNumber = serialNumber,
            lastLocation = lastLocation,
            systemTypeId = systemTypeId,
            modelId = modelId,
            calibrationInterval = 0
        )
        withContext(Dispatchers.Main) {
            snackbar.showSnackbar("✅ Checkweigher added locally (offline)")
            navController.popBackStack()
        }
    } catch (e: Exception) {
        Log.e("NewCW", "Local create failed", e)
        withContext(Dispatchers.Main) {
            snackbar.showSnackbar("❌ Failed to add system locally.")
        }
    }
}
