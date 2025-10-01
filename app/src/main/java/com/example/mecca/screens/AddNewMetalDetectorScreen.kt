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
import com.example.mecca.DataClasses.MdModelsLocal
import com.example.mecca.DataClasses.SystemTypeLocal
import com.example.mecca.FetchResult
import com.example.mecca.Network.isNetworkAvailable
import com.example.mecca.Repositories.MetalDetectorModelsRepository
import com.example.mecca.Repositories.MetalDetectorSystemsRepository
import com.example.mecca.Repositories.SystemTypeRepository
import kotlinx.coroutines.launch

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

    // Function to check for network availability
//    fun isNetworkAvailable(context: Context): Boolean {
//        val connectivityManager =
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = connectivityManager.activeNetwork ?: return false
//        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
//        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//    }

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
                                .menuAnchor()
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
                                .menuAnchor()
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


                }
                Spacer(modifier = Modifier.height(15.dp))
                val context = LocalContext.current  // Retrieve the context using LocalContext

                // Submit Button

                Button(
                    onClick = {
                        //hide keyboard
                        keyboardController?.hide()

                        // Check if fields are valid
                        if (serialNumber.isNotBlank() && apertureWidth.isNotBlank() && apertureHeight.isNotBlank()
                            && selectedSystemType != null && selectedMdModel != null && lastLocation.isNotBlank()
                        ) {
                            // Disable the button while processing
                            isProcessing = true

                            coroutineScope.launch { // Start a coroutine here

                                try {
                                    // Check if the serial number already exists (local or cloud based on network)
                                    val serialExists = if (isNetworkAvailable(context)) {
                                        // Perform cloud check if connected
                                        mdSystemsRepository.isSerialNumberExistsInCloud(serialNumber)
                                    } else {
                                        // Perform local check if no network
                                        mdSystemsRepository.isSerialNumberExists(serialNumber)
                                    }
                                    Log.d("NewMD", "Serial number exists: $serialExists")

                                    if (serialExists) {
                                        // Serial number already exists, show a warning
                                        snackbarHostState.showSnackbar("Serial number already exists!")
                                        Log.d(
                                            "NewMD",
                                            "Serial number exists, cannot add duplicate."
                                        )
                                    } else {
                                        // Serial number does not exist, proceed with adding the system
                                        Log.d("NewMD", "All Fields ok")

                                        if (isNetworkAvailable(context)) {
                                            // Network available, try to upload to cloud
                                            Log.d("NewMD", "Network check OK")
                                            try {
                                                Log.d("NewMD", "CustomerID = $customerID.")
                                                mdSystemsRepository.addMetalDetectorToCloud(
                                                    customerID = customerID,
                                                    serialNumber = serialNumber,
                                                    apertureWidth = apertureWidth.toInt(),
                                                    apertureHeight = apertureHeight.toInt(),
                                                    systemTypeId = selectedSystemType!!.id,
                                                    modelId = selectedMdModel!!.meaId,
                                                    calibrationInterval = 0,
                                                    lastLocation = lastLocation
                                                )
                                                Log.d("NewMD", "Data uploaded to cloud.")

                                                // sync local to cloud

                                                when (val result = mdSystemsRepository.fetchAndStoreMdSystems()) {
                                                    is FetchResult.Success -> {
                                                        Log.d("SyncMetalDetectors", "Full database sync successful: ${result.message}")
                                                    }
                                                    is FetchResult.Failure -> {
                                                        Log.e("SyncMetalDetectors", "Full database sync failed: ${result.errorMessage}")

                                                    }
                                                }

                                                snackbarHostState.showSnackbar("System added to cloud")
                                                // Go back to the previous screen
                                                navController.popBackStack()
                                            } catch (e: Exception) {
                                                // Handle network or server failure
                                                Log.e(
                                                    "NewMD",
                                                    "Failed to upload to cloud: ${e.message}"
                                                )
                                                snackbarHostState.showSnackbar("Failed to add system to cloud. Try again later.")
                                                return@launch // Exit early since the upload failed
                                            }
                                        } else {
                                            // No network, store in Room
                                            Log.d("NewMD", "Network check Fail")
                                            Log.d("NewMD", "CustomerID = $customerID.")
                                            mdSystemsRepository.addMetalDetectorToLocalDb(
                                                customerID = customerID,
                                                serialNumber = serialNumber,
                                                apertureWidth = apertureWidth.toInt(),
                                                apertureHeight = apertureHeight.toInt(),
                                                systemTypeId = selectedSystemType!!.id,
                                                modelId = selectedMdModel!!.meaId,
                                                calibrationInterval = 0,
                                                lastLocation = lastLocation
                                            )
                                            Log.d("NewMD", "Data added to Room.")
                                            snackbarHostState.showSnackbar("No Network Connection. System Added to Local Database")
                                            // Go back to the previous screen
                                            navController.popBackStack()
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Catch any unexpected errors
                                    Log.e("NewMD", "Error checking serial number: ${e.message}")
                                    snackbarHostState.showSnackbar("Failed to check serial number. Please try again.")
                                } finally {
                                    // Enable the button again after processing is complete
                                    isProcessing = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing, // Disable button if processing is ongoing
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,  // Background color
                        contentColor = Color.White           // Text color
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
    )
}
