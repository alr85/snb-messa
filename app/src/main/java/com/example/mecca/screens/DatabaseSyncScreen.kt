package com.example.mecca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mecca.Repositories.CustomerRepository
import com.example.mecca.FetchResult
import com.example.mecca.Repositories.MetalDetectorModelsRepository
import com.example.mecca.Repositories.MetalDetectorSystemsRepository
import com.example.mecca.Repositories.SystemTypeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SyncTableItem(
    val name: String,
    val syncFunction: suspend () -> String, // Sync function returns log message
    var logMessage: String = "", // Holds the log message
    var isSuccess: Boolean = false // Tracks if the sync was successful
)


@Composable
fun DatabaseSyncScreen(
    navController: NavHostController,
    repositoryCustomer: CustomerRepository,
    repositoryMdModels: MetalDetectorModelsRepository,
    repositoryMdSystems: MetalDetectorSystemsRepository,
    repositorySystemTypes: SystemTypeRepository
) {
    // Define the settings list inside the composable
    val syncTableItems = listOf(
        SyncTableItem("Customers", {
            when (val result = repositoryCustomer.fetchAndStoreCustomers()) {
                is FetchResult.Success -> result.message
                is FetchResult.Failure -> result.errorMessage
            }
        }),
        SyncTableItem("System Types", {
            when (val result = repositorySystemTypes.fetchAndStoreSystemTypes()) {
                is FetchResult.Success -> result.message
                is FetchResult.Failure -> result.errorMessage
            }
        }),
        SyncTableItem("Metal Detector Models", {
            when (val result = repositoryMdModels.fetchAndStoreMdModels()) {
                is FetchResult.Success -> result.message
                is FetchResult.Failure -> result.errorMessage
            }
        }),
        SyncTableItem("Metal Detector Systems", {
            when (val result = repositoryMdSystems.fetchAndStoreMdSystems()) {
                is FetchResult.Success -> result.message
                is FetchResult.Failure -> result.errorMessage
            }
        }),

    )

    // State to hold sync items with log messages
    val syncItemsState = remember { mutableStateListOf(*syncTableItems.toTypedArray()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Database Synchronisation",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                style = TextStyle(
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            )

            LazyColumn {
                items(syncItemsState) { setting ->
                    SettingRow(setting) { logMessage ->
                        // Update the log message in the sync items list
                        val index = syncItemsState.indexOf(setting)
                        if (index != -1) {
                            syncItemsState[index] = setting.copy(logMessage = logMessage)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingRow(setting: SyncTableItem, onSyncComplete: (String) -> Unit) {
    var isSyncing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Display the button for each setting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button with full width
            androidx.compose.material3.Button(
                onClick = {
                    if (!isSyncing) {
                        isSyncing = true
                        // Perform the sync function
                        CoroutineScope(Dispatchers.IO).launch {
                            val logMessage = setting.syncFunction()
                            withContext(Dispatchers.Main) {
                                onSyncComplete(logMessage)
                                isSyncing = false
                            }
                        }
                    }
                },
                enabled = !isSyncing, // Disable button while syncing
                modifier = Modifier.fillMaxWidth(), // Make the button full width

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,  // Background color
                    contentColor = Color.White           // Text color
                )
            ) {
                if (isSyncing) {
                    Text(text = "Syncing ${setting.name}...")
                } else {
                    Text(text = "Sync ${setting.name}")
                }
            }
        }

        // Display log message after syncing
        if (setting.logMessage.isNotEmpty()) {
            Text(
                text = setting.logMessage,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth(), // Ensure the log message takes full width as well
                color = Color.Gray,
                style = TextStyle(fontSize = 14.sp)
            )
        }
    }
}

