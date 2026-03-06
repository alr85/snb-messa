package com.example.mecca.screens.menu

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
import com.example.mecca.ApiService
import com.example.mecca.PreferencesHelper
import com.example.mecca.daos.MetalDetectorConveyorCalibrationDAO
import com.example.mecca.repositories.CustomerRepository
import com.example.mecca.repositories.MetalDetectorSystemsRepository
import com.example.mecca.repositories.MetalDetectorConveyorCalibrationRepository
import com.example.mecca.activities.MetalDetectorConveyorCalibrationActivity
import com.example.mecca.dataClasses.MetalDetectorConveyorCalibrationLocal
import com.example.mecca.formatDate
import com.example.mecca.ui.theme.SnbRed
import com.example.mecca.util.InAppLogger
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCalibrationsScreen(
    dao: MetalDetectorConveyorCalibrationDAO,
    customerRepository: CustomerRepository,
    systemsRepository: MetalDetectorSystemsRepository,
    calibrationRepository: MetalDetectorConveyorCalibrationRepository,
    apiService: ApiService,
    snackbarHostState: SnackbarHostState
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val unfinishedCalibrations by dao.getAllUnfinishedCalibrations()
        .collectAsState(initial = emptyList())

    val pendingCalibrations by dao.getAllPendingCalibrations()
        .collectAsState(initial = emptyList())

    val completedCalibrations by dao.getAllCompletedCalibrations()
        .collectAsState(initial = emptyList())

    // Safeguard for duplicate uploads
    var uploadingCalibrationIds by remember { mutableStateOf(setOf<String>()) }

    // ---------------- Customer Cache ----------------

    var customerNameCache by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    LaunchedEffect(
        unfinishedCalibrations.size,
        pendingCalibrations.size,
        completedCalibrations.size
    ) {

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

    // ---------------- UI ----------------

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)), // Light grey background
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- INCOMPLETE SECTION ---
        if (unfinishedCalibrations.isNotEmpty()) {
            item { SectionHeader("Incomplete", Icons.Default.PendingActions) }
            items(unfinishedCalibrations) { calibration ->
                ModernCalibrationItem(
                    calibration = calibration,
                    status = "Incomplete",
                    customerName = customerNameCache[calibration.customerId] ?: "Loading…",
                    onClick = {
                        coroutineScope.launch {
                            val system = systemsRepository
                                .getMetalDetectorsWithFullDetailsUsingLocalId(calibration.systemId)
                                .firstOrNull()

                            if (system == null) {
                                InAppLogger.e("Could not find system details for calibration: ${calibration.calibrationId}")
                                snackbarHostState.showSnackbar("⚠️ Error: System details missing.")
                                return@launch
                            }

                            val (_, _, engineerId) = PreferencesHelper.getCredentials(context)

                            val intent = Intent(
                                context,
                                MetalDetectorConveyorCalibrationActivity::class.java
                            ).apply {
                                putExtra("CALIBRATION_ID", calibration.calibrationId)
                                putExtra("SYSTEM_FULL_DETAILS", system)
                                putExtra("ENGINEER_ID", engineerId ?: 0)
                            }

                            context.startActivity(intent)
                        }
                    }
                )
            }
        }

        // --- PENDING SECTION ---
        if (pendingCalibrations.isNotEmpty()) {
            item { SectionHeader("Pending Upload", Icons.Default.CloudUpload) }
            items(pendingCalibrations) { calibration ->
                val isUploading = uploadingCalibrationIds.contains(calibration.calibrationId)
                ModernCalibrationItem(
                    calibration = calibration,
                    status = "Pending",
                    customerName = customerNameCache[calibration.customerId] ?: "Loading…",
                    isUploading = isUploading,
                    onClick = {
                        if (isUploading) return@ModernCalibrationItem
                        coroutineScope.launch {
                            uploadingCalibrationIds = uploadingCalibrationIds + calibration.calibrationId
                            try {
                                val result = calibrationRepository.uploadUnsyncedCalibrations(context, apiService)
                                snackbarHostState.showSnackbar(result.toString())
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("⚠️ An error occurred during upload.")
                            } finally {
                                uploadingCalibrationIds = uploadingCalibrationIds - calibration.calibrationId
                            }
                        }
                    }
                )
            }
        }

        // --- COMPLETED SECTION ---
        if (completedCalibrations.isNotEmpty()) {
            item { SectionHeader("Recently Completed", Icons.Default.History) }
            items(completedCalibrations) { calibration ->
                ModernCalibrationItem(
                    calibration = calibration,
                    status = "Completed",
                    customerName = customerNameCache[calibration.customerId] ?: "Loading…",
                    onClick = {} // Could navigate to a read-only summary if needed
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

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SnbRed,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ModernCalibrationItem(
    calibration: MetalDetectorConveyorCalibrationLocal,
    status: String,
    customerName: String,
    isUploading: Boolean = false,
    onClick: () -> Unit
) {
    val formattedDate = try {
        formatDate(calibration.startDate)
    } catch (_: Exception) {
        "Invalid date"
    }

    val statusColor = when (status) {
        "Incomplete" -> Color(0xFFFFA000) // Amber
        "Pending" -> Color(0xFF2196F3)    // Blue
        "Completed" -> Color(0xFF4CAF50)  // Green
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isUploading) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "SN: ${calibration.serialNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                StatusBadge(text = status, color = statusColor, isUploading = isUploading)
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(text = calibration.lastLocation, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(text = formattedDate, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ID: ${calibration.calibrationId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )

                if (status != "Completed") {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = SnbRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color, isUploading: Boolean) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = color
                )
                Spacer(Modifier.width(6.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
