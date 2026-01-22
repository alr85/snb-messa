package com.example.mecca.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mecca.AppChromeViewModel
import com.example.mecca.FetchResult
import com.example.mecca.TopBarState
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
    val isSuccess: Boolean? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseSyncScreen(
    repositoryCustomer: CustomerRepository,
    repositoryMdModels: MetalDetectorModelsRepository,
    repositoryMdSystems: MetalDetectorSystemsRepository,
    repositorySystemTypes: SystemTypeRepository,
    detectionRepo: RetailerSensitivitiesRepository,
    chromeVm: AppChromeViewModel
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

    LaunchedEffect(Unit) {
        chromeVm.setTopBar(
            TopBarState(
                title = "Database Sync",
                showBack = true,
                showCall = false
            )
        )
    }

    Scaffold(
//        topBar = {
//            MyTopAppBar(
//                navController = navController,
//                title = "Database Sync",
//                showBack = true,
//                showCall = false
//            )
//        },
        containerColor = Color.White
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isSyncing) "Syncing..." else "Sync Everything")
            }

            // Feedback area
            if (isSyncing) {
                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = overallMessage.ifBlank { "Syncing..." },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (overallMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = overallMessage,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(logs) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = item.message,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Divider(modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}
