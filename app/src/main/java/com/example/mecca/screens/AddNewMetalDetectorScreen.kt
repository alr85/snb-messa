@file:Suppress("SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection")

package com.example.mecca.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.AppChromeViewModel
import com.example.mecca.FetchResult
import com.example.mecca.TopBarState
import com.example.mecca.repositories.MetalDetectorModelsRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.repositories.SystemTypeRepository
import com.example.mecca.dataClasses.MdModelsLocal
import com.example.mecca.dataClasses.SystemTypeLocal
import com.example.mecca.formModules.LabeledDualNumberInputsWithHelp
import com.example.mecca.formModules.LabeledObjectDropdownWithHelp
import com.example.mecca.formModules.LabeledReadOnlyField
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.util.InAppLogger
import com.example.mecca.util.SerialCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Capitalization transformation
object UppercaseTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(AnnotatedString(text.text.uppercase()), OffsetMapping.Identity)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewMetalDetectorScreen(
    navController: NavHostController,
    systemTypeRepository: SystemTypeRepository,
    mdModelsRepository: MetalDetectorModelsRepository,
    mdSystemsRepository: MetalDetectorSystemsRepository,
    customerID: Int,
    customerName: String,
    chromeVm: AppChromeViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
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

    val scrollState = rememberScrollState()

    // Load dropdown options
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            systemTypes = systemTypeRepository.getSystemTypesFromDb()
                .filter { it.systemType.contains("Metal", ignoreCase = true) }

            mdModels = mdModelsRepository.getMdModelsFromDb()

            Log.d("DatabaseDebug", "System types: $systemTypes")
            Log.d("DatabaseDebug", "MdModels: $mdModels")
        }
    }

    // Helpers (keep inputs consistent)
    fun sanitiseUpper(value: String): String =
        value.replace(Regex("[^A-Za-z0-9 _.-]"), "").uppercase()

    fun digitsOnly(value: String): String =
        value.filter { it.isDigit() }

    val isFormValid =
        serialNumber.isNotBlank() &&
                apertureWidth.isNotBlank() &&
                apertureHeight.isNotBlank() &&
                selectedSystemType != null &&
                selectedMdModel != null &&
                lastLocation.isNotBlank()

    LaunchedEffect(Unit) {
        chromeVm.setTopBar(
            TopBarState(
                title = "Add New Metal Detector",
                showBack = true,
                showCall = false,
                showMenu = false
            )
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(scrollState)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                    text = "Add New Metal Detector",
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
            )

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
                    coroutineScope.launch {
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
                                navController = navController
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
    navController: NavHostController
) {
    InAppLogger.d("Adding new MD system...")
    // Ask cloud if online; else check local cache
    when (val status = mdSystemsRepository.checkSerialNumberStatus(context, serialNumber)) {
        SerialCheckResult.Exists -> {
            snackbarHostState.showSnackbar("This serial number was found in the cloud database - unable to create new system.")
            InAppLogger.d("Serial already exists in cloud - Abort.")
            return
        }
        SerialCheckResult.NotFound -> {
            InAppLogger.d("Serial not found in cloud - creating new system...")
            createInCloud(
                repo = mdSystemsRepository,
                customerID = customerID,
                serialNumber = serialNumber,
                apertureWidth = apertureWidth,
                apertureHeight = apertureHeight,
                systemTypeId = systemTypeId,
                modelId = modelId,
                lastLocation = lastLocation,
                snackbar = snackbarHostState,
                navController = navController
            )
        }
        SerialCheckResult.ExistsLocalOffline -> {
            snackbarHostState.showSnackbar("Offline: serial exists locally. Cannot create.")
            return
        }
        SerialCheckResult.NotFoundLocalOffline -> {
            createInLocal(
                repo = mdSystemsRepository,
                customerID = customerID,
                serialNumber = serialNumber,
                apertureWidth = apertureWidth,
                apertureHeight = apertureHeight,
                systemTypeId = systemTypeId,
                modelId = modelId,
                lastLocation = lastLocation,
                snackbar = snackbarHostState,
                navController = navController
            )
        }
        is SerialCheckResult.Error -> {
            snackbarHostState.showSnackbar("Couldn’t verify serial: ${status.message ?: "network error"}")
            return
        }
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
                snackbar.showSnackbar("System added to cloud")
                navController.popBackStack()
            }
        } else {
            withContext(Dispatchers.Main) { snackbar.showSnackbar("Failed to add system to cloud.") }
        }
    } catch (e: Exception) {
        Log.e("NewMD", "Cloud create failed", e)
        withContext(Dispatchers.Main) {
            snackbar.showSnackbar("Failed to add system to cloud. Try again later.")
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
            calibrationInterval = 0,
            lastLocation = lastLocation
        )
        Log.d("NewMD", "Local Room insert OK.")
        withContext(Dispatchers.Main) {
            snackbar.showSnackbar("No network. System added locally.")
            navController.popBackStack()
        }
    } catch (e: Exception) {
        Log.e("NewMD", "Local create failed", e)
        withContext(Dispatchers.Main) {
            snackbar.showSnackbar("Failed to add system locally.")
        }
    }
}

