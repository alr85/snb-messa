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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mecca.FetchResult
import com.example.mecca.repositories.CustomerRepository
import com.example.mecca.repositories.MetalDetectorModelsRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.repositories.RetailerSensitivitiesRepository
import com.example.mecca.repositories.SystemTypeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SyncTableItem(
    val name: String,
    var logMessage: String = "",
    var isSuccess: Boolean = false,
    val syncFunction: suspend () -> String,
)

@Composable
fun DatabaseSyncScreen(
    navController: NavHostController,
    repositoryCustomer: CustomerRepository,
    repositoryMdModels: MetalDetectorModelsRepository,
    repositoryMdSystems: MetalDetectorSystemsRepository,
    repositorySystemTypes: SystemTypeRepository,
    detectionRepo: RetailerSensitivitiesRepository
) {
    val syncTableItems = listOf(
        SyncTableItem("Customers") {
            when (val r = repositoryCustomer.fetchAndStoreCustomers()) {
                is FetchResult.Success -> r.message
                is FetchResult.Failure -> r.errorMessage
            }
        },
        SyncTableItem("System Types") {
            when (val r = repositorySystemTypes.fetchAndStoreSystemTypes()) {
                is FetchResult.Success -> r.message
                is FetchResult.Failure -> r.errorMessage
            }
        },
        SyncTableItem("Metal Detector Models") {
            when (val r = repositoryMdModels.fetchAndStoreMdModels()) {
                is FetchResult.Success -> r.message
                is FetchResult.Failure -> r.errorMessage
            }
        },
        SyncTableItem("Metal Detector Systems") {
            when (val r = repositoryMdSystems.fetchAndStoreMdSystems()) {
                is FetchResult.Success -> r.message
                is FetchResult.Failure -> r.errorMessage
            }
        },
        SyncTableItem("M&S Target Sensitivities (Conveyor)") {
            when (val r = detectionRepo.fetchAndStoreConveyor()) {
                is FetchResult.Success -> r.message
                is FetchResult.Failure -> r.errorMessage
            }
        },
        SyncTableItem("M&S Target Sensitivities (Freefall)") {
            when (val r = detectionRepo.fetchAndStoreFreefall()) {
                is FetchResult.Success -> r.message
                is FetchResult.Failure -> r.errorMessage
            }
        },
        SyncTableItem("M&S Target Sensitivities (Pipeline)") {
            when (val r = detectionRepo.fetchAndStorePipeline()) {
                is FetchResult.Success -> r.message
                is FetchResult.Failure -> r.errorMessage
            }
        }
    )

    val syncItemsState = remember { syncTableItems.toMutableStateList() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(8.dp)
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
                    SyncSettingRow(setting) { logMessage ->
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
fun SyncSettingRow(
    setting: SyncTableItem,
    onSyncComplete: (String) -> Unit
) {
    var isSyncing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (!isSyncing) {
                        isSyncing = true
                        scope.launch(Dispatchers.IO) {
                            val logMessage = setting.syncFunction()
                            withContext(Dispatchers.Main) {
                                onSyncComplete(logMessage)
                                isSyncing = false
                            }
                        }
                    }
                },
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isSyncing) "Syncing ${setting.name}..." else "Sync ${setting.name}"
                )
            }
        }

        if (setting.logMessage.isNotEmpty()) {
            Text(
                text = setting.logMessage,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth(),
                color = Color.Gray,
                style = TextStyle(fontSize = 14.sp)
            )
        }
    }
}
