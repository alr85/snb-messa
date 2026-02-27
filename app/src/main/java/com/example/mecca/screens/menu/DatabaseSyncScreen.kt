package com.example.mecca.screens.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sync

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mecca.FetchResult
import com.example.mecca.repositories.CustomerRepository
import com.example.mecca.repositories.MetalDetectorModelsRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.repositories.RetailerSensitivitiesRepository
import com.example.mecca.repositories.SystemTypeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext

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
            SyncTask("Upload Unsynced MD Systems") {
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
            SyncTask("M&S Conveyor Targets") {
                when (val r = detectionRepo.fetchAndStoreConveyor()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> "Failed: ${r.errorMessage}"
                }
            },
            SyncTask("M&S Freefall Targets") {
                when (val r = detectionRepo.fetchAndStoreFreefall()) {
                    is FetchResult.Success -> r.message
                    is FetchResult.Failure -> "Failed: ${r.errorMessage}"
                }
            },
            SyncTask("M&S Pipeline Targets") {
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

    var taskStates by remember {
        mutableStateOf(tasks.associate { it.name to TaskUiState() })
    }

    val total = tasks.size
    val progress = currentIndex.coerceIn(0, total).toFloat() / total.toFloat()

    fun setTaskState(name: String, state: TaskUiState) {
        taskStates = taskStates.toMutableMap().apply { put(name, state) }
    }

    suspend fun runSingleTask(task: SyncTask): Boolean {

        withContext(Dispatchers.Main) {
            setTaskState(task.name, TaskUiState(SyncStatus.Running, "Syncing..."))
        }

        val msg = runCatching { task.run() }
            .getOrElse { "Failed: ${it.message ?: it.javaClass.simpleName}" }

        val isFailure = msg.startsWith("Failed", true)
        val status = if (isFailure) SyncStatus.Failure else SyncStatus.Success

        withContext(Dispatchers.Main) {
            setTaskState(task.name, TaskUiState(status, msg))
            if (isFailure) snackbarHostState.showSnackbar("${task.name} failed")
        }

        return !isFailure
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        //-----------------------------------------------------
        // HEADER CARD
        //-----------------------------------------------------

        Card {
            Column(Modifier.padding(16.dp)) {

                Text(
                    "Database Sync",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Run a full sync or update tables individually.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

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

                                    withContext(Dispatchers.Main) {
                                        currentIndex = i + 1
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    overallMessage = "Sync complete"
                                    isSyncingAll = false
                                    snackbarHostState.showSnackbar("Database sync complete")
                                }
                            }
                        },
                        enabled = !isSyncingAll
                    ) {
                        Text(if (isSyncingAll) "Syncing..." else "Sync Everything")
                    }

                    Spacer(Modifier.width(12.dp))

                    OutlinedButton(
                        onClick = {
                            taskStates =
                                tasks.associate { it.name to TaskUiState() }
                        },
                        enabled = !isSyncingAll
                    ) {
                        Text("Clear Results")
                    }
                }

                if (isSyncingAll) {

                    Spacer(Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(overallMessage)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        //-----------------------------------------------------
        // TASK LIST
        //-----------------------------------------------------

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            items(tasks, key = { it.name }) { task ->

                val state = taskStates[task.name]!!
                val anyRunning =
                    isSyncingAll || taskStates.values.any { it.status == SyncStatus.Running }

                Card {
                    Column(Modifier.padding(16.dp)) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            //-------------------------------------------------
                            // STATUS ICON
                            //-------------------------------------------------

                            val icon = when (state.status) {

                                SyncStatus.Success ->
                                    Icons.Default.CheckCircle

                                SyncStatus.Failure ->
                                    Icons.Default.Error

                                SyncStatus.Running ->
                                    Icons.Default.Sync

                                else ->
                                    Icons.Default.CloudQueue
                            }

                            val tint = when (state.status) {

                                SyncStatus.Success ->
                                    MaterialTheme.colorScheme.primary

                                SyncStatus.Failure ->
                                    MaterialTheme.colorScheme.error

                                else ->
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Icon(
                                icon,
                                contentDescription = null,
                                tint = tint
                            )

                            Spacer(Modifier.width(12.dp))

                            Column(Modifier.weight(1f)) {

                                Text(
                                    task.name,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    when (state.status) {
                                        SyncStatus.Idle -> "Not run yet"
                                        SyncStatus.Running -> "Syncing..."
                                        SyncStatus.Success -> "Success"
                                        SyncStatus.Failure -> "Failed"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        runSingleTask(task)
                                    }
                                },
                                enabled = !anyRunning && state.status != SyncStatus.Running
                            ) {
                                Text("Sync")
                            }
                        }

                        if (state.status == SyncStatus.Running) {

                            Spacer(Modifier.height(10.dp))

                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (state.lastMessage.isNotBlank()) {

                            Spacer(Modifier.height(8.dp))

                            Text(
                                state.lastMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color =
                                    if (state.status == SyncStatus.Failure)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
