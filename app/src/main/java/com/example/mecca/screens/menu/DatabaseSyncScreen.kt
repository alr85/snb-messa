package com.example.mecca.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mecca.FetchResult
import com.example.mecca.repositories.*
import com.example.mecca.util.InAppLogger
import com.example.mecca.util.SyncPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class SyncStatus {
    Idle, Running, Success, Failure
}

private data class TaskUiState(
    val status: SyncStatus = SyncStatus.Idle,
    val lastMessage: String = ""
)

data class SyncTask(
    val name: String,
    val run: suspend () -> String
)

@Composable
fun DatabaseSyncScreen(
    repositoryCustomer: CustomerRepository,
    repositoryMdModels: MetalDetectorModelsRepository,
    repositoryMdSystems: MetalDetectorSystemsRepository,
    repositorySystemTypes: SystemTypeRepository,
    detectionRepo: RetailerSensitivitiesRepository,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val syncPrefs = remember { SyncPreferences(context) }
    var isAutoSyncEnabled by remember { mutableStateOf(syncPrefs.isAutoSyncEnabled()) }

    val tasks: List<SyncTask> = remember(context) {
        listOf(
            SyncTask("Customers") {
                when (val r = repositoryCustomer.fetchAndStoreCustomers()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> "Failed: ${r.errorMessage}"
                }
            },
            SyncTask("System Types") {
                when (val r = repositorySystemTypes.fetchAndStoreSystemTypes()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> "Failed: ${r.errorMessage}"
                }
            },
            SyncTask("Metal Detector Models") {
                when (val r = repositoryMdModels.fetchAndStoreMdModels()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> "Failed: ${r.errorMessage}"
                }
            },
            SyncTask("Upload offline MD Systems") {
                when (val r = repositoryMdSystems.uploadUnsyncedSystems(context)) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> "Failed: ${r.errorMessage}"
                }
            },
            SyncTask("Metal Detector Systems") {
                when (val r = repositoryMdSystems.fetchAndStoreMdSystems()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> "Failed: ${r.errorMessage}"
                }
            },
            SyncTask("M&S Target Sensitivities (Conveyor)") {
                when (val r = detectionRepo.fetchAndStoreConveyor()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> "Failed: ${r.errorMessage}"
                }
            },
            SyncTask("M&S Target Sensitivities (Freefall)") {
                when (val r = detectionRepo.fetchAndStoreFreefall()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> "Failed: ${r.errorMessage}"
                }
            },
            SyncTask("M&S Target Sensitivities (Pipeline)") {
                when (val r = detectionRepo.fetchAndStorePipeline()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> "Failed: ${r.errorMessage}"
                }
            }
        )
    }

    val scope = rememberCoroutineScope()
    var isSyncingAll by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var overallMessage by remember { mutableStateOf("") }
    var taskStates by remember { mutableStateOf(tasks.associate { it.name to TaskUiState() }) }

    val total = tasks.size
    val progress = currentIndex.coerceIn(0, total).toFloat() / total.toFloat()

    fun setTaskState(name: String, state: TaskUiState) {
        taskStates = taskStates.toMutableMap().apply { put(name, state) }
    }

    suspend fun runSingleTask(task: SyncTask): Boolean {
        withContext(Dispatchers.Main) {
            setTaskState(task.name, TaskUiState(SyncStatus.Running, "Syncing..."))
        }
        val msg = runCatching { task.run() }.getOrElse { "Failed: ${it.message ?: it.javaClass.simpleName}" }
        val isFailure = msg.startsWith("Failed", true)
        val status = if (isFailure) SyncStatus.Failure else SyncStatus.Success
        withContext(Dispatchers.Main) {
            setTaskState(task.name, TaskUiState(status, msg))
            if (isFailure) snackbarHostState.showSnackbar("⚠️ ${task.name} failed")
        }
        return !isFailure
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Database Sync", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Manage your local data synchronization.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(Modifier.height(16.dp))
                
                // Moved Auto Sync here
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Automatic Sync", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("Sync work automatically when online", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = isAutoSyncEnabled,
                        onCheckedChange = {
                            isAutoSyncEnabled = it
                            syncPrefs.setAutoSyncEnabled(it)
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(16.dp))

                Row {
                    Button(
                        onClick = {
                            if (isSyncingAll) return@Button
                            isSyncingAll = true
                            currentIndex = 0
                            overallMessage = ""
                            scope.launch(Dispatchers.IO) {
                                for ((i, task) in tasks.withIndex()) {
                                    withContext(Dispatchers.Main) {
                                        currentIndex = i
                                        overallMessage = "Syncing ${i + 1}/$total: ${task.name}"
                                    }
                                    val ok = runSingleTask(task)
                                    if (!ok) {
                                        withContext(Dispatchers.Main) {
                                            overallMessage = "Stopped: ${task.name} failed"
                                            isSyncingAll = false
                                        }
                                        return@launch
                                    }
                                    withContext(Dispatchers.Main) { currentIndex = i + 1 }
                                }
                                withContext(Dispatchers.Main) {
                                    overallMessage = "Sync complete"
                                    isSyncingAll = false
                                    snackbarHostState.showSnackbar("✅ Database sync complete")
                                }
                            }
                        },
                        enabled = !isSyncingAll
                    ) {
                        Text(if (isSyncingAll) "Syncing..." else "Sync Everything Now")
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { taskStates = tasks.associate { it.name to TaskUiState() } },
                        enabled = !isSyncingAll
                    ) {
                        Text("Reset Status")
                    }
                }

                if (isSyncingAll) {
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Text(overallMessage, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tasks, key = { it.name }) { task ->
                val state = taskStates[task.name]!!
                val anyRunning = isSyncingAll || taskStates.values.any { it.status == SyncStatus.Running }
                Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when (state.status) {
                                SyncStatus.Success -> Icons.Default.CheckCircle
                                SyncStatus.Failure -> Icons.Default.Error
                                SyncStatus.Running -> Icons.Default.Sync
                                else -> Icons.Default.CloudQueue
                            }
                            val tint = when (state.status) {
                                SyncStatus.Success -> MaterialTheme.colorScheme.primary
                                SyncStatus.Failure -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Icon(icon, null, tint = tint)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(task.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    when (state.status) {
                                        SyncStatus.Idle -> "Waiting..."
                                        SyncStatus.Running -> "Syncing..."
                                        SyncStatus.Success -> "Up to date"
                                        SyncStatus.Failure -> "Failed"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { scope.launch(Dispatchers.IO) { runSingleTask(task) } },
                                enabled = !anyRunning && state.status != SyncStatus.Running
                            ) {
                                Text("Sync")
                            }
                        }
                        if (state.status == SyncStatus.Running) {
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        if (state.lastMessage.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                state.lastMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.status == SyncStatus.Failure) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
