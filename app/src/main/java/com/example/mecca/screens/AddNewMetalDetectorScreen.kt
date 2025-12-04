@file:Suppress("SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection")

package com.example.mecca.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.mecca.FetchResult
import com.example.mecca.repositories.MetalDetectorModelsRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.repositories.SystemTypeRepository
import com.example.mecca.dataClasses.MdModelsLocal
import com.example.mecca.dataClasses.SystemTypeLocal
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
    customerName: String
) {
    val coroutineScope = rememberCoroutineScope()

    // States for input fields
    var serialNumber by remember { mutableStateOf("") }
    var apertureWidth by remember { mutableStateOf("") }
    var apertureHeight by remember { mutableStateOf("") }
    var lastLocation by remember { mutableStateOf("") }


    // States for dropdowns
    var systemTypes by remember { mutableStateOf<List<SystemTypeLocal>>(emptyList()) }
    var expandedSystemType by remember { mutableStateOf(false) }
    var selectedSystemType by remember { mutableStateOf<SystemTypeLocal?>(null) }

    var mdModels by remember { mutableStateOf<List<MdModelsLocal>>(emptyList()) }
    var expandedMdModel by remember { mutableStateOf(false) }
    var selectedMdModel by remember { mutableStateOf<MdModelsLocal?>(null) }

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    // State to track whether the submit button is enabled or disabled
    var isProcessing by remember { mutableStateOf(false) }

    // Keyboard controller to close keyboard on submit
    val keyboardController = LocalSoftwareKeyboardController.current


    // Fetch system types and MdModels when the composable is launched
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            systemTypes = systemTypeRepository.getSystemTypesFromDb()
                .filter { it.systemType.contains("Metal", ignoreCase = true) } // Filter for "Metal"
            mdModels = mdModelsRepository.getMdModelsFromDb()
            Log.d("DatabaseDebug", "System types: $systemTypes")
            Log.d("DatabaseDebug", "MdModels: $mdModels")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .background(Color.White),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Customer Name
                Text(
                    text = customerName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Left,
                )

                Spacer(modifier = Modifier.height(5.dp))

                // System Type Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), // Adjust padding as needed
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "System Type:",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp) // Add some space between the label and field
                    )

                    // System Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedSystemType,
                        onExpandedChange = { expandedSystemType = !expandedSystemType },
                        modifier = Modifier.weight(2f) // Adjust weight to control spacing
                    ) {
                        OutlinedTextField(
                            value = selectedSystemType?.systemType ?: "Select...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSystemType)
                            },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Red,
                                unfocusedIndicatorColor = Color.Gray,
                                focusedLabelColor = Color.DarkGray,
                                unfocusedLabelColor = Color.Gray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSystemType,
                            onDismissRequest = { expandedSystemType = false }
                        ) {
                            systemTypes.forEach { systemType ->
                                DropdownMenuItem(
                                    text = { Text(systemType.systemType) },
                                    onClick = {
                                        selectedSystemType = systemType
                                        expandedSystemType = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Make/Model Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), // Adjust padding as needed
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Make/Model:",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp) // Add space between the label and the field
                    )

                    // MdModels Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedMdModel,
                        onExpandedChange = { expandedMdModel = !expandedMdModel },
                        modifier = Modifier.weight(2f) // Adjust weight to control spacing
                    ) {
                        OutlinedTextField(
                            value = selectedMdModel?.modelDescription ?: "Select Make/Model",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMdModel)
                            },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Red,
                                unfocusedIndicatorColor = Color.Gray,
                                focusedLabelColor = Color.DarkGray,
                                unfocusedLabelColor = Color.Gray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMdModel,
                            onDismissRequest = { expandedMdModel = false }
                        ) {
                            mdModels.forEach { mdModel ->
                                DropdownMenuItem(
                                    text = { Text(mdModel.modelDescription) },
                                    onClick = {
                                        selectedMdModel = mdModel
                                        expandedMdModel = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Serial Number Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), // Adjust padding as needed
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Serial Number:",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp) // Add space between the label and the field
                    )

                    // Serial Number Input (Capitalized)
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = {
                            // Allow A-Z, a-z, 0-9, space, hyphen, underscore
                            // The ^ inside [] means "not these characters"
                            // So, replace anything that is NOT in the allowed set.
                            serialNumber = it.replace(Regex("[^A-Za-z0-9 _.-]"), "").uppercase()
                        },
                        modifier = Modifier.weight(2f),
                        visualTransformation = UppercaseTransformation,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Red,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color.DarkGray,
                            unfocusedLabelColor = Color.Gray,
                        )
                    )

                }

                // Aperture Size Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), // Adjust padding as needed
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aperture Size (mm):",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp) // Add space between the label and the field
                    )

                    OutlinedTextField(
                        value = apertureWidth,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) apertureWidth = it
                        },
                        label = { Text("Width") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f) // Adjust weight to control spacing
                            .padding(end = 8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Red,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color.DarkGray,
                            unfocusedLabelColor = Color.Gray,
                        )
                    )

                    Text(
                        text = "x",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    OutlinedTextField(
                        value = apertureHeight,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) apertureHeight = it
                        },
                        label = { Text("Height") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Red,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color.DarkGray,
                            unfocusedLabelColor = Color.Gray,
                        )
                    )


                }

                // Site Location Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), // Adjust padding as needed
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Location Ref:",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp) // Add space between the label and the field
                    )

                    //Location Input (Capitalized)
                    OutlinedTextField(
                        value = lastLocation,
                        onValueChange = {
                            // Allow A-Z, a-z, 0-9, space, hyphen, underscore
                            // The ^ inside [] means "not these characters"
                            // So, replace anything that is NOT in the allowed set.
                            lastLocation = it.replace(Regex("[^A-Za-z0-9 _.-]"), "").uppercase()
                        },
                        modifier = Modifier.weight(2f),
                        visualTransformation = UppercaseTransformation,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Red,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color.DarkGray,
                            unfocusedLabelColor = Color.Gray,
                        )
                    )

                }
                Spacer(modifier = Modifier.height(15.dp))
                val context = LocalContext.current  // Retrieve the context using LocalContext

                // Submit Button

                Button(
                    onClick = {
                        keyboardController?.hide()

                        val valid = serialNumber.isNotBlank() &&
                                apertureWidth.isNotBlank() &&
                                apertureHeight.isNotBlank() &&
                                selectedSystemType != null &&
                                selectedMdModel != null &&
                                lastLocation.isNotBlank()

                        if (!valid) return@Button

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
                    enabled = !isProcessing,
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
        })
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

