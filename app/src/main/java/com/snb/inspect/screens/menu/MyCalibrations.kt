package com.snb.inspect.screens.menu

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snb.inspect.ApiService
import com.snb.inspect.PreferencesHelper
import com.snb.inspect.activities.CheckweigherCalibrationActivity
import com.snb.inspect.activities.MetalDetectorConveyorCalibrationActivity
import com.snb.inspect.daos.CheckweigherCalibrationDAO
import com.snb.inspect.daos.MetalDetectorConveyorCalibrationDAO
import com.snb.inspect.dataClasses.CheckweigherCalibrationLocal
import com.snb.inspect.dataClasses.MetalDetectorConveyorCalibrationLocal
import com.snb.inspect.formatDate
import com.snb.inspect.repositories.CheckweigherCalibrationRepository
import com.snb.inspect.repositories.CheckweigherSystemsRepository
import com.snb.inspect.repositories.CustomerRepository
import com.snb.inspect.repositories.MetalDetectorConveyorCalibrationRepository
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.ui.theme.SnbRed
import com.snb.inspect.util.InAppLogger
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// --- INTERMEDIATE MODEL ---
data class GenericCalibration(
    val calibrationId: String,
    val systemId: Int,
    val serialNumber: String,
    val customerId: Int,
    val startDate: String,
    val lastLocation: String,
    val newLocation: String,
    val type: SystemCategory,
    val rawMD: MetalDetectorConveyorCalibrationLocal? = null,
    val rawCW: CheckweigherCalibrationLocal? = null
)

enum class SystemCategory(val label: String) {
    METAL_DETECTOR("MD"),
    CHECKWEIGHER("CW"),
    XRAY("X-RAY"),
    STATIC_SCALE("SCALE")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCalibrationsScreen(
    mdDao: MetalDetectorConveyorCalibrationDAO,
    cwDao: CheckweigherCalibrationDAO,
    customerRepository: CustomerRepository,
    mdSystemsRepository: MetalDetectorSystemsRepository,
    cwSystemsRepository: CheckweigherSystemsRepository,
    mdCalibrationRepository: MetalDetectorConveyorCalibrationRepository,
    cwCalibrationRepository: CheckweigherCalibrationRepository,
    apiService: ApiService,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Reactive streams for MD
    val mdUnfinished = mdDao.getAllUnfinishedCalibrations()
    val mdPending = mdDao.getAllPendingCalibrations()
    val mdCompleted = mdDao.getAllCompletedCalibrations()

    // 2. Reactive streams for CW
    val cwUnfinished = cwDao.getAllUnfinishedCalibrations()
    val cwPending = cwDao.getAllPendingCalibrations()
    val cwCompleted = cwDao.getAllCompletedCalibrations()

    // 3. Combine into Generic models
    val unfinishedCalibrations by combine(mdUnfinished, cwUnfinished) { md, cw ->
        val list = mutableListOf<GenericCalibration>()
        md.forEach { list.add(it.toGeneric()) }
        cw.forEach { list.add(it.toGeneric()) }
        list.sortedByDescending { it.startDate }
    }.collectAsState(initial = emptyList())

    val pendingCalibrations by combine(mdPending, cwPending) { md, cw ->
        val list = mutableListOf<GenericCalibration>()
        md.forEach { list.add(it.toGeneric()) }
        cw.forEach { list.add(it.toGeneric()) }
        list.sortedByDescending { it.startDate }
    }.collectAsState(initial = emptyList())

    val completedCalibrations by combine(mdCompleted, cwCompleted) { md, cw ->
        val list = mutableListOf<GenericCalibration>()
        md.forEach { list.add(it.toGeneric()) }
        cw.forEach { list.add(it.toGeneric()) }
        list.sortedByDescending { it.startDate }
    }.collectAsState(initial = emptyList())

    var uploadingCalibrationIds by remember { mutableStateOf(setOf<String>()) }
    var customerNameCache by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    LaunchedEffect(unfinishedCalibrations, pendingCalibrations, completedCalibrations) {
        val allCustomerIds = (unfinishedCalibrations + pendingCalibrations + completedCalibrations)
            .map { it.customerId }
            .distinct()

        val missingIds = allCustomerIds.filterNot { customerNameCache.containsKey(it) }
        if (missingIds.isEmpty()) return@LaunchedEffect

        val newEntries = missingIds.associateWith { id ->
            customerRepository.getCustomerName(fusionId = id) ?: "Unknown"
        }
        customerNameCache = customerNameCache + newEntries
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (unfinishedCalibrations.isNotEmpty()) {
            item { SectionHeader("Incomplete", Icons.Default.PendingActions) }
            items(unfinishedCalibrations) { cal ->
                ModernCalibrationItem(
                    calibration = cal,
                    status = "Incomplete",
                    customerName = customerNameCache[cal.customerId] ?: "Loading…",
                    onClick = {
                        coroutineScope.launch {
                            resumeCalibration(context, cal, mdSystemsRepository, cwSystemsRepository, snackbarHostState)
                        }
                    }
                )
            }
        }

        if (pendingCalibrations.isNotEmpty()) {
            item { SectionHeader("Pending Upload", Icons.Default.CloudUpload) }
            items(pendingCalibrations) { cal ->
                val isUploading = uploadingCalibrationIds.contains(cal.calibrationId)
                ModernCalibrationItem(
                    calibration = cal,
                    status = "Pending",
                    customerName = customerNameCache[cal.customerId] ?: "Loading…",
                    isUploading = isUploading,
                    onClick = {
                        if (isUploading) return@ModernCalibrationItem
                        coroutineScope.launch {
                            uploadingCalibrationIds = uploadingCalibrationIds + cal.calibrationId
                            try {
                                val result = when (cal.type) {
                                    SystemCategory.METAL_DETECTOR -> mdCalibrationRepository.uploadUnsyncedCalibrations(context, apiService, cal.calibrationId)
                                    SystemCategory.CHECKWEIGHER -> cwCalibrationRepository.uploadUnsyncedCalibrations(context, apiService, cal.calibrationId)
                                    else -> "Not implemented"
                                }
                                snackbarHostState.showSnackbar(result.toString())
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("⚠️ An error occurred during upload.")
                            } finally {
                                uploadingCalibrationIds = uploadingCalibrationIds - cal.calibrationId
                            }
                        }
                    }
                )
            }
        }

        if (completedCalibrations.isNotEmpty()) {
            item { SectionHeader("Recently Completed", Icons.Default.History) }
            items(completedCalibrations) { cal ->
                ModernCalibrationItem(
                    calibration = cal,
                    status = "Completed",
                    customerName = customerNameCache[cal.customerId] ?: "Loading…",
                    onClick = {}
                )
            }
        }

        if (unfinishedCalibrations.isEmpty() && pendingCalibrations.isEmpty() && completedCalibrations.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No calibrations found", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
            }
        }
    }
}

private suspend fun resumeCalibration(
    context: android.content.Context,
    cal: GenericCalibration,
    mdRepo: MetalDetectorSystemsRepository,
    cwRepo: CheckweigherSystemsRepository,
    snackbar: SnackbarHostState
) {
    val (_, _, engineerId) = PreferencesHelper.getCredentials(context)

    when (cal.type) {
        SystemCategory.METAL_DETECTOR -> {
            val system = mdRepo.getMetalDetectorsWithFullDetailsUsingLocalId(cal.systemId).firstOrNull()
            if (system == null) {
                snackbar.showSnackbar("⚠️ Error: MD system details missing.")
                return
            }
            val intent = Intent(context, MetalDetectorConveyorCalibrationActivity::class.java).apply {
                putExtra("CALIBRATION_ID", cal.calibrationId)
                putExtra("SYSTEM_FULL_DETAILS", system)
                putExtra("ENGINEER_ID", engineerId ?: 0)
            }
            context.startActivity(intent)
        }
        SystemCategory.CHECKWEIGHER -> {
            val system = cwRepo.getCheckweigherWithFullDetailsUsingLocalId(cal.systemId)
            if (system == null) {
                snackbar.showSnackbar("⚠️ Error: CW system details missing.")
                return
            }
            val intent = Intent(context, CheckweigherCalibrationActivity::class.java).apply {
                putExtra("CALIBRATION_ID", cal.calibrationId)
                putExtra("SYSTEM_FULL_DETAILS", system)
                putExtra("ENGINEER_ID", engineerId ?: 0)
            }
            context.startActivity(intent)
        }
        else -> snackbar.showSnackbar("⚠️ Resuming ${cal.type.label} not yet implemented.")
    }
}

// --- MAPPING EXTENSIONS ---

fun MetalDetectorConveyorCalibrationLocal.toGeneric() = GenericCalibration(
    calibrationId = calibrationId,
    systemId = systemId,
    serialNumber = serialNumber,
    customerId = customerId,
    startDate = startDate,
    lastLocation = lastLocation,
    newLocation = newLocation,
    type = SystemCategory.METAL_DETECTOR,
    rawMD = this
)

fun CheckweigherCalibrationLocal.toGeneric() = GenericCalibration(
    calibrationId = calibrationId,
    systemId = systemId,
    serialNumber = serialNumber,
    customerId = customerId,
    startDate = startDate,
    lastLocation = lastLocation,
    newLocation = newLocation,
    type = SystemCategory.CHECKWEIGHER,
    rawCW = this
)

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = SnbRed, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = title.uppercase(), style = MaterialTheme.typography.labelLarge, color = Color.DarkGray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
fun ModernCalibrationItem(
    calibration: GenericCalibration,
    status: String,
    customerName: String,
    isUploading: Boolean = false,
    onClick: () -> Unit
) {
    val formattedDate = try { formatDate(calibration.startDate) } catch (_: Exception) { "Invalid date" }
    val statusColor = when (status) {
        "Incomplete" -> Color(0xFFFFA000)
        "Pending" -> Color(0xFF2196F3)
        "Completed" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !isUploading) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = SnbRed.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                            Text(text = calibration.type.label, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = SnbRed, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(text = "SN: ${calibration.serialNumber}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                StatusBadge(text = status, color = statusColor, isUploading = isUploading)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(text = calibration.newLocation.ifBlank { calibration.lastLocation }, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(text = formattedDate, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "ID: ${calibration.calibrationId}", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                if (status != "Completed") {
                    Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = SnbRed, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color, isUploading: Boolean) {
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = color)
                Spacer(Modifier.width(6.dp))
            } else {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(6.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
