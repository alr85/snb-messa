package com.snb.inspect.screens.service

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.snb.inspect.PreferencesHelper
import com.snb.inspect.activities.MetalDetectorConveyorCalibrationActivity
import com.snb.inspect.activities.SensitivityOptimisationValidationActivity
import com.snb.inspect.daos.MetalDetectorConveyorCalibrationDAO
import com.snb.inspect.dataClasses.MdModelsLocal
import com.snb.inspect.dataClasses.MdSystemNoteLocal
import com.snb.inspect.dataClasses.MetalDetectorWithFullDetails
import com.snb.inspect.formatDate
import com.snb.inspect.repositories.MdSystemNotesRepository
import com.snb.inspect.repositories.MetalDetectorModelsRepository
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.ui.theme.DetailItem
import com.snb.inspect.ui.theme.ExpandableSection
import com.snb.inspect.ui.theme.SnbDarkGrey
import com.snb.inspect.ui.theme.SnbRed
import com.snb.inspect.util.InAppLogger
import com.snb.inspect.util.SerialCheckResult
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MetalDetectorConveyorSystemScreen(
    navController: NavHostController,
    repositoryMD: MetalDetectorSystemsRepository,
    dao: MetalDetectorConveyorCalibrationDAO,
    repositoryModels: MetalDetectorModelsRepository,
    notesRepository: MdSystemNotesRepository,
    systemId: Int,
    chromeVm: AppChromeViewModel,
    snackbarHostState: SnackbarHostState
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mdSystem by remember { mutableStateOf<MetalDetectorWithFullDetails?>(null) }
    var modelDetails by remember { mutableStateOf<MdModelsLocal?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    var showActions by rememberSaveable { mutableStateOf(false) }

    val notes by notesRepository.observeNotesForSystem(systemId).collectAsState(initial = emptyList())
    var authorNames by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

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
        mdSystem = repositoryMD
            .getMetalDetectorsWithFullDetailsUsingLocalId(systemId)
            .firstOrNull()

        modelDetails = mdSystem?.modelId
            ?.takeIf { it != 0 }
            ?.let { repositoryModels.getMdModelDetails(it) }

        // Sync notes when refreshing
        mdSystem?.let {
            notesRepository.syncNotes(context, systemId, it.cloudId)
        }
    }

    // Initial load
    LaunchedEffect(systemId) { refresh() }

    // Refresh when returning from calibration
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch { refresh() }
    }

    /**
     * MENU WIRING
     *
     * Root scaffold owns the icon.
     * This screen only supplies the click behaviour.
     */
    LaunchedEffect(Unit) {
        chromeVm.setMenuAction { showActions = !showActions }
    }

    val formattedLastCalibrationDate =
        runCatching { formatDate(mdSystem?.lastCalibration) }.getOrElse { "Invalid date" }

    val formattedAddedDate =
        runCatching { formatDate(mdSystem?.addedDate) }.getOrElse { "Invalid date" }

    fun startCalibration() {
        val system = mdSystem ?: return

        val newCalibrationId =
            System.currentTimeMillis().toString(36) + "-" + (100..999).random()

        val (_, _, engineerId) = PreferencesHelper.getCredentials(context)

        val intent = Intent(context, MetalDetectorConveyorCalibrationActivity::class.java).apply {
            putExtra("CALIBRATION_ID", newCalibrationId)
            putExtra("SYSTEM_FULL_DETAILS", system)
            putExtra("ENGINEER_ID", engineerId ?: 0)
            // detection labels
            putExtra("DETECTION_SETTING_1_LABEL", modelDetails?.detectionSetting1)
            putExtra("DETECTION_SETTING_2_LABEL", modelDetails?.detectionSetting2)
            putExtra("DETECTION_SETTING_3_LABEL", modelDetails?.detectionSetting3)
            putExtra("DETECTION_SETTING_4_LABEL", modelDetails?.detectionSetting4)
            putExtra("DETECTION_SETTING_5_LABEL", modelDetails?.detectionSetting5)
            putExtra("DETECTION_SETTING_6_LABEL", modelDetails?.detectionSetting6)
            putExtra("DETECTION_SETTING_7_LABEL", modelDetails?.detectionSetting7)
            putExtra("DETECTION_SETTING_8_LABEL", modelDetails?.detectionSetting8)
        }

        context.startActivity(intent)
    }

    fun startSov() {
        val system = mdSystem ?: return
        val newSovId = System.currentTimeMillis().toString(36) + "-" + (100..999).random()
        val (_, _, engineerId) = PreferencesHelper.getCredentials(context)

        val intent = Intent(context, SensitivityOptimisationValidationActivity::class.java).apply {
            putExtra("SOV_ID", newSovId)
            putExtra("SYSTEM_FULL_DETAILS", system)
            putExtra("ENGINEER_ID", engineerId ?: 0)
            // detection labels
            putExtra("DETECTION_SETTING_1_LABEL", modelDetails?.detectionSetting1)
            putExtra("DETECTION_SETTING_2_LABEL", modelDetails?.detectionSetting2)
            putExtra("DETECTION_SETTING_3_LABEL", modelDetails?.detectionSetting3)
            putExtra("DETECTION_SETTING_4_LABEL", modelDetails?.detectionSetting4)
            putExtra("DETECTION_SETTING_5_LABEL", modelDetails?.detectionSetting5)
            putExtra("DETECTION_SETTING_6_LABEL", modelDetails?.detectionSetting6)
            putExtra("DETECTION_SETTING_7_LABEL", modelDetails?.detectionSetting7)
            putExtra("DETECTION_SETTING_8_LABEL", modelDetails?.detectionSetting8)
        }
        context.startActivity(intent)
    }

    suspend fun syncThisSystem() {

        val system = mdSystem ?: return

        try {
            isUploading = true

            if (system.cloudId != 0) {

                repositoryMD.updateSystem(
                    context = context,
                    cloudId = system.cloudId,
                    localId = system.id,
                    tempId = system.tempId
                )

                snackbarHostState.showSnackbar("✅ System updated successfully.")
                refresh()
                return
            }

            when (val status =
                repositoryMD.checkSerialNumberStatus(context, system.serialNumber)) {

                is SerialCheckResult.Exists ->
                    snackbarHostState.showSnackbar("⚠️ Serial already exists in cloud.")

                is SerialCheckResult.FuzzyMatch ->
                    snackbarHostState.showSnackbar("⚠️ Serial fuzzy match found in cloud.")

                SerialCheckResult.NotFound -> {

                    val newCloudId = repositoryMD.addMetalDetectorToCloud(
                        customerID = system.customerId,
                        serialNumber = system.serialNumber,
                        apertureWidth = system.apertureWidth,
                        apertureHeight = system.apertureHeight,
                        systemTypeId = system.systemTypeId,
                        modelId = system.modelId,
                        lastLocation = system.lastLocation,
                        calibrationInterval = 0
                    )

                    if (newCloudId != null && newCloudId != 0) {

                        dao.updateCalibrationWithCloudId(system.tempId, newCloudId)

                        // SYNC NOTES: Ensure notes written while machine was local-only are uploaded
                        notesRepository.syncNotes(context, system.id, newCloudId)

                        repositoryMD.fetchAndStoreMdSystems()

                        snackbarHostState.showSnackbar("✅ System added to cloud.")
                        navController.popBackStack()
                    } else {
                        snackbarHostState.showSnackbar("⚠️ Failed to add system.")
                    }
                }

                is SerialCheckResult.ExistsLocalOffline ->
                    snackbarHostState.showSnackbar("⚠️ Offline: serial exists locally.")

                SerialCheckResult.NotFoundLocalOffline ->
                    snackbarHostState.showSnackbar("⚠️ No network. Sync later.")

                is SerialCheckResult.Error ->
                    snackbarHostState.showSnackbar("⚠️ Error: ${status.message}")
            }

        } catch (e: Exception) {

            InAppLogger.e("Sync crashed: ${e.message}")
            snackbarHostState.showSnackbar("⚠️ Sync failed.")

        } finally {
            isUploading = false
        }
    }

    // ---------- CONTENT ----------
    Box(Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {

            item {
                ExpandableSection("System Details", true) {

                    DetailItem("System Type", mdSystem?.systemType ?: "?")
                    DetailItem("Serial Number", mdSystem?.serialNumber ?: "?")
                    DetailItem("Customer", mdSystem?.customerName ?: "?")
                    DetailItem("Model", mdSystem?.modelDescription ?: "?")
                    DetailItem("Aperture Width", "${mdSystem?.apertureWidth ?: "?"} mm")
                    DetailItem("Aperture Height", "${mdSystem?.apertureHeight ?: "?"} mm")
                    DetailItem("Location", mdSystem?.lastLocation ?: "?")
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
                                NoteItem(note, authorNames[note.addedBy] ?: "...")
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                ExpandableSection("Database Info", false) {

                    DetailItem("Cloud Synced", if (mdSystem?.isSynced == true) "Yes" else "No")
                    DetailItem("Local ID", mdSystem?.id?.toString() ?: "?")
                    DetailItem("Cloud ID", mdSystem?.cloudId?.toString() ?: "?")
                    DetailItem("Temp ID", (mdSystem?.tempId ?: 0).toString())
                    DetailItem("Fusion Customer ID", mdSystem?.fusionID?.toString() ?: "?")
                    DetailItem("Date added", formattedAddedDate)
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }

        // Animated Expressive FAB replacement
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
                // Secondary action items that appear above the FAB
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
                        // Start Calibration
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
                                Text(
                                    text = "New Calibration",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    color = SnbDarkGrey
                                )
                            }
                        }

                        // Start SOV (Coming Soon)
                        val isSovComingSoon = false
                        FloatingActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (!isSovComingSoon) {
                                    showActions = false
                                    startSov()
                                }
                            },
                            containerColor = if (isSovComingSoon) Color(0xFFF5F5F5) else Color.White,
                            contentColor = if (isSovComingSoon) Color.Gray else SnbRed,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Validation",
                                    modifier = Modifier.size(24.dp),
                                    tint = if (isSovComingSoon) Color.Gray else SnbRed
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "New Validation",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSovComingSoon) Color.Gray else SnbDarkGrey
                                    )
                                    if (isSovComingSoon) {
                                        Text(
                                            text = "COMING SOON",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // View Manual
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
                                    Text(
                                        text = "View Technical Manual",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                        color = SnbDarkGrey
                                    )
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

                        // Report Data Issue
                        FloatingActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                showActions = false
                                showReportDialog = true
                            },
                            containerColor = Color.White,
                            contentColor = SnbRed,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, "Report Issue", modifier = Modifier.size(24.dp))
                                Text(
                                    text = "Report Data Issue",
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
                                    text = if (mdSystem?.isSynced == true) "Synced" else "Sync to Cloud",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    color = SnbDarkGrey
                                )
                            }
                        }
                    }
                }

                // Main FAB to toggle actions
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            if (showActions) "Close" else "Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.rotate(rotation).size(28.dp)
                        )
                    },
                    onClick = { showActions = !showActions },
                    expanded = !showActions,
                    containerColor = if (showActions) SnbDarkGrey else SnbRed,
                    contentColor = Color.White
                )
            }
        }
    }

    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { noteText, isImportant ->
                scope.launch {
                    val (_, _, engineerId) = PreferencesHelper.getCredentials(context)
                    notesRepository.addNote(
                        systemId = systemId,
                        cloudSystemId = mdSystem?.cloudId,
                        addedBy = engineerId ?: 0,
                        noteText = noteText,
                        isImportant = isImportant
                    )
                    showAddNoteDialog = false
                    // Trigger sync
                    notesRepository.syncNotes(context, systemId, mdSystem?.cloudId)
                }
            }
        )
    }

    if (showReportDialog && mdSystem != null) {
        ReportDiscrepancyDialog(
            system = mdSystem!!,
            onDismiss = { showReportDialog = false },
            onConfirm = { reportText ->
                val (username, _, _) = PreferencesHelper.getCredentials(context)
                sendReportEmail(context, mdSystem!!, reportText, username ?: "Unknown Engineer")
                showReportDialog = false
            }
        )
    }
}

@Composable
fun NoteItem(note: MdSystemNoteLocal, authorName: String) {
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
fun AddNoteDialog(
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

@Composable
fun ReportDiscrepancyDialog(
    system: MetalDetectorWithFullDetails,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reportText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Data Issue") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Describe what information is incorrect (e.g., wrong serial, model, or dimensions). The office will be notified via email.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = reportText,
                    onValueChange = { reportText = it },
                    label = { Text("Correct information / Details") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (reportText.isNotBlank()) onConfirm(reportText) },
                colors = ButtonDefaults.buttonColors(containerColor = SnbRed)
            ) {
                Text("Send Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun sendReportEmail(
    context: android.content.Context,
    system: MetalDetectorWithFullDetails,
    reportText: String,
    engineerName: String
) {
    val subject = "SNB Inspect app data discrepancy"
    val body = """
        
        Engineer: $engineerName
        Serial Number: ${system.serialNumber}
        Customer: ${system.customerName}
        
        REPORTED ISSUE: $reportText
        
        --- Current Database Info ---
        Model: ${system.modelDescription}
        Aperture: ${system.apertureWidth}mm x ${system.apertureHeight}mm
        Location: ${system.lastLocation}
        Local ID: ${system.id}
        Cloud ID: ${system.cloudId ?: "Not Synced"}
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("snb@metaldetector-rentals.co.uk"))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Choose Email Client"))
    } catch (e: Exception) {
        // Handle case where no email app is installed
    }
}
