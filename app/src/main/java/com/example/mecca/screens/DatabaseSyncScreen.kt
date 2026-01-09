package com.example.mecca.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

data class SyncTask(
    val name: String,
    val run: suspend () -> String
)

data class SyncLogItem(
    val name: String,
    val message: String,
    val isSuccess: Boolean? = null // optional if you want to color-code later
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
    val tasks = remember {
        listOf(
            SyncTask("Customers") {
                when (val r = repositoryCustomer.fetchAndStoreCustomers()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> r.errorMessage
                }
            },
            SyncTask("System Types") {
                when (val r = repositorySystemTypes.fetchAndStoreSystemTypes()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> r.errorMessage
                }
            },
            SyncTask("Metal Detector Models") {
                when (val r = repositoryMdModels.fetchAndStoreMdModels()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> r.errorMessage
                }
            },
            SyncTask("Metal Detector Systems") {
                when (val r = repositoryMdSystems.fetchAndStoreMdSystems()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> r.errorMessage
                }
            },
            SyncTask("M&S Target Sensitivities (Conveyor)") {
                when (val r = detectionRepo.fetchAndStoreConveyor()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> r.errorMessage
                }
            },
            SyncTask("M&S Target Sensitivities (Freefall)") {
                when (val r = detectionRepo.fetchAndStoreFreefall()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> r.errorMessage
                }
            },
            SyncTask("M&S Target Sensitivities (Pipeline)") {
                when (val r = detectionRepo.fetchAndStorePipeline()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> r.errorMessage
                }
            }
        )
    }

    val scope = rememberCoroutineScope()

    var isSyncing by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var currentTaskName by remember { mutableStateOf<String?>(null) }
    var overallMessage by remember { mutableStateOf("") }

    val logs = remember { mutableStateListOf<SyncLogItem>() }

    val total = tasks.size
    val progress = if (total == 0) 0f else (currentIndex.coerceIn(0, total).toFloat() / total.toFloat())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(12.dp)
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
                    .padding(vertical = 8.dp),
                style = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center)
            )

            // One button to rule them all
            Button(
                onClick = {
                    if (isSyncing) return@Button

                    isSyncing = true
                    currentIndex = 0
                    logs.clear()
                    overallMessage = ""
                    currentTaskName = null

                    scope.launch(Dispatchers.IO) {
                        for ((i, task) in tasks.withIndex()) {
                            withContext(Dispatchers.Main) {
                                currentIndex = i
                                currentTaskName = task.name
                                overallMessage = "Syncing ${i + 1}/$total: ${task.name}"
                            }

                            val msg = runCatching { task.run() }
                                .getOrElse { e -> "Failed: ${e.message ?: e.javaClass.simpleName}" }

                            withContext(Dispatchers.Main) {
                                logs.add(SyncLogItem(task.name, msg))
                                currentIndex = i + 1
                            }
                        }

                        withContext(Dispatchers.Main) {
                            currentTaskName = null
                            overallMessage = "Sync complete."
                            isSyncing = false
                        }
                    }
                },
                enabled = !isSyncing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSyncing) Color.DarkGray else Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Text(text = if (isSyncing) "Syncing..." else "Sync Everything")
            }

            // Feedback area
            if (isSyncing) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = overallMessage.ifBlank { "Syncing..." },
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (overallMessage.isNotBlank()) {
                Text(
                    text = overallMessage,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Per-table log output as it completes
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(logs) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = item.message,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Divider(modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}
