package com.snb.inspect.screens.service

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import com.snb.inspect.AppChromeViewModel
import com.snb.inspect.FetchResult
import com.snb.inspect.PreferencesHelper
import com.snb.inspect.activities.CheckweigherCalibrationActivity
import com.snb.inspect.dataClasses.CheckweigherWithFullDetails
import com.snb.inspect.dataClasses.CwModelsLocal
import com.snb.inspect.dataClasses.CwSystemNoteLocal
import com.snb.inspect.formatDate
import com.snb.inspect.repositories.CheckweigherSystemsRepository
import com.snb.inspect.repositories.CwSystemNotesRepository
import com.snb.inspect.ui.theme.DetailItem
import com.snb.inspect.ui.theme.ExpandableSection
import com.snb.inspect.ui.theme.SnbDarkGrey
import com.snb.inspect.ui.theme.SnbRed
import com.snb.inspect.util.InAppLogger
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun CheckweigherSystemScreen(
    navController: NavHostController,
    repositoryCW: CheckweigherSystemsRepository,
    notesRepository: CwSystemNotesRepository,
    systemId: Int,
    chromeVm: AppChromeViewModel,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cwSystem by remember { mutableStateOf<CheckweigherWithFullDetails?>(null) }
    var modelDetails by remember { mutableStateOf<CwModelsLocal?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    var showActions by rememberSaveable { mutableStateOf(false) }

    val notes by notesRepository.observeNotesForSystem(systemId).collectAsState(initial = emptyList())
    var authorNames by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(notes) {
        val newNames = authorNames.toMutableMap()
        var changed = false
        notes.forEach { note ->
            if (!newNames.containsKey(note.addedBy)) {
                newNames[note.addedBy] = notesRepository.getAuthorName(note.addedBy)
                changed = true
            }
        }
        if (changed) authorNames = newNames
    }

    suspend fun refresh() {
        cwSystem = repositoryCW
            .getCheckweigherWithFullDetailsUsingLocalId(systemId)

        modelDetails = cwSystem?.modelId
            ?.takeIf { it != 0 }
            ?.let { repositoryCW.getCwModelDetails(it) }

        // Sync notes when refreshing
        cwSystem?.let {
            notesRepository.syncNotes(context, systemId, it.cloudId)
        }
    }

    // Initial load
    LaunchedEffect(systemId) { refresh() }

    // Refresh when returning from calibration
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch { refresh() }
    }

    LaunchedEffect(Unit) {
        chromeVm.setMenuAction { showActions = !showActions }
    }

    suspend fun syncThisSystem() {
        val system = cwSystem ?: return

        try {
            isUploading = true

            // 1. Upload the system itself if unsynced
            val uploadResult = repositoryCW.uploadUnsyncedSystems(context)
            
            // 2. Refresh local data to get latest cloudId if it just got one
            refresh()
            val updatedSystem = cwSystem ?: return

            // 3. Sync notes (this now handles both push and pull)
            notesRepository.syncNotes(context, systemId, updatedSystem.cloudId)

            if (uploadResult is FetchResult.Success) {
                snackbarHostState.showSnackbar("✅ System and notes synced successfully.")
            } else {
                snackbarHostState.showSnackbar("⚠️ Sync completed with issues: ${uploadResult}")
            }

        } catch (e: Exception) {
            InAppLogger.e("CW Sync crashed: ${e.message}")
            snackbarHostState.showSnackbar("⚠️ Sync failed.")
        } finally {
            isUploading = false
        }
    }

    val formattedLastCalibrationDate =
        runCatching { formatDate(cwSystem?.lastCalibration) }.getOrElse { "Invalid date" }

    val formattedAddedDate =
        runCatching { formatDate(cwSystem?.addedDate) }.getOrElse { "Invalid date" }

    fun startCalibration() {
        val system = cwSystem ?: return
        val newCalibrationId = System.currentTimeMillis().toString(36) + "-" + (100..999).random()
        val (_, _, engineerId) = PreferencesHelper.getCredentials(context)

        val intent = Intent(context, CheckweigherCalibrationActivity::class.java).apply {
            putExtra("CALIBRATION_ID", newCalibrationId)
            putExtra("SYSTEM_FULL_DETAILS", system)
            putExtra("ENGINEER_ID", engineerId ?: 0)
        }
        context.startActivity(intent)
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            item {
                ExpandableSection("System Details", true) {
                    DetailItem("System Type", cwSystem?.systemType ?: "?")
                    DetailItem("Serial Number", cwSystem?.serialNumber ?: "?")
                    DetailItem("Customer", cwSystem?.customerName ?: "?")
                    DetailItem("Model", cwSystem?.modelDescription ?: "?")
                    DetailItem("Location", cwSystem?.lastLocation ?: "?")
                    DetailItem("Last Calibrated", formattedLastCalibrationDate)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                ExpandableSection("Notes (${notes.size})", true) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (notes.isEmpty()) {
                            Text(
                                "No notes found for this system.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            notes.forEach { note ->
                                CwNoteItem(note, authorNames[note.addedBy] ?: "...")
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                ExpandableSection("Database Info", false) {
                    DetailItem("Cloud Synced", if (cwSystem?.isSynced == true) "Yes" else "No")
                    DetailItem("Local ID", cwSystem?.id?.toString() ?: "?")
                    DetailItem("Cloud ID", cwSystem?.cloudId?.toString() ?: "?")
                    DetailItem("Temp ID", (cwSystem?.tempId ?: 0).toString())
                    DetailItem("Date added", formattedAddedDate)
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (showActions) 45f else 0f,
                label = "rotation"
            )

            Column(
                modifier = Modifier.width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(
                    visible = showActions,
                    enter = fadeIn() + expandIn(expandFrom = Alignment.BottomEnd),
                    exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.BottomEnd)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FloatingActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                showActions = false
                                startCalibration()
                            },
                            containerColor = Color.White,
                            contentColor = SnbRed,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Tune, "Calibration", modifier = Modifier.size(24.dp))
                                Text("New Calibration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SnbDarkGrey)
                            }
                        }

                        if (!modelDetails?.manualUrl.isNullOrEmpty()) {
                            FloatingActionButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    showActions = false
                                    val encodedUrl = URLEncoder.encode(modelDetails?.manualUrl ?: "", StandardCharsets.UTF_8.toString())
                                    navController.navigate("manualViewer/${modelDetails?.modelDescription}/$encodedUrl")
                                },
                                containerColor = Color.White,
                                contentColor = SnbRed,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Description, "Manual", modifier = Modifier.size(24.dp))
                                    Text("View Technical Manual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SnbDarkGrey)
                                }
                            }
                        }

                        // Add Note
                        FloatingActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                showActions = false
                                showAddNoteDialog = true
                            },
                            containerColor = Color.White,
                            contentColor = SnbRed,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.NoteAdd, "Add Note", modifier = Modifier.size(24.dp))
                                Text(
                                    text = "Add System Note",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    color = SnbDarkGrey
                                )
                            }
                        }

                        // Sync with Cloud
                        FloatingActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                showActions = false
                                scope.launch { syncThisSystem() }
                            },
                            containerColor = Color.White,
                            contentColor = SnbRed,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isUploading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = SnbRed,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = "Sync",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = if (cwSystem?.isSynced == true) "Synced" else "Sync to Cloud",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    color = SnbDarkGrey
                                )
                            }
                        }
                    }
                }

                ExtendedFloatingActionButton(
                    text = { Text(if (showActions) "Close" else "Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Add, null, modifier = Modifier.rotate(rotation).size(28.dp)) },
                    onClick = { showActions = !showActions },
                    expanded = !showActions,
                    containerColor = if (showActions) SnbDarkGrey else SnbRed,
                    contentColor = Color.White
                )
            }
        }
    }

    if (showAddNoteDialog) {
        CwAddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { noteText, isImportant ->
                scope.launch {
                    val (_, _, engineerId) = PreferencesHelper.getCredentials(context)
                    notesRepository.addNote(
                        systemId = systemId,
                        cloudSystemId = cwSystem?.cloudId,
                        addedBy = engineerId ?: 0,
                        noteText = noteText,
                        isImportant = isImportant
                    )
                    showAddNoteDialog = false
                    // Trigger sync
                    notesRepository.syncNotes(context, systemId, cwSystem?.cloudId)
                }
            }
        )
    }
}

@Composable
fun CwNoteItem(note: CwSystemNoteLocal, authorName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (note.isImportant) Color(0xFFFFF4F4) else Color(0xFFF8F9FA)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (note.isImportant) BorderStroke(1.dp, SnbRed.copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = authorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (note.isImportant) SnbRed else SnbDarkGrey,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = runCatching { formatDate(note.addedDate ?: "") }.getOrElse { note.addedDate ?: "" },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    if (note.isImportant) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = "Important",
                            tint = SnbRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = note.noteText ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = SnbDarkGrey,
                lineHeight = 20.sp
            )

            if (!note.isSynced) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Pending Sync",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun CwAddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit
) {
    var noteText by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add System Note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isImportant,
                        onCheckedChange = { isImportant = it }
                    )
                    Text("Mark as Important", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (noteText.isNotBlank()) onConfirm(noteText, isImportant) },
                colors = ButtonDefaults.buttonColors(containerColor = SnbRed)
            ) {
                Text("Add Note")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
