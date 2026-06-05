package com.snb.inspect.screens.menu

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.snb.inspect.activities.SensitivityOptimisationValidationActivity
import com.snb.inspect.daos.SensitivityOptimisationValidationDAO
import com.snb.inspect.dataClasses.SensitivityOptimisationValidationLocal
import com.snb.inspect.formatDate
import com.snb.inspect.repositories.CustomerRepository
import com.snb.inspect.repositories.MetalDetectorSystemsRepository
import com.snb.inspect.repositories.SensitivityOptimisationValidationRepository
import com.snb.inspect.ui.theme.SnbRed
import com.snb.inspect.util.InAppLogger
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyValidationsScreen(
    dao: SensitivityOptimisationValidationDAO,
    customerRepository: CustomerRepository,
    systemsRepository: MetalDetectorSystemsRepository,
    sovRepository: SensitivityOptimisationValidationRepository,
    apiService: ApiService,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val unfinishedSovs by dao.getAllUnfinished().collectAsState(initial = emptyList())
    val pendingSovs by dao.getAllPending().collectAsState(initial = emptyList())
    val completedSovs by dao.getAllCompleted().collectAsState(initial = emptyList())

    var uploadingSovIds by remember { mutableStateOf(setOf<String>()) }
    var customerNameCache by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    LaunchedEffect(unfinishedSovs.size, pendingSovs.size, completedSovs.size) {
        val allCustomerIds = (unfinishedSovs + pendingSovs + completedSovs)
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
        if (unfinishedSovs.isNotEmpty()) {
            item { SectionHeader("Incomplete", Icons.Default.PendingActions) }
            items(unfinishedSovs) { sov ->
                ModernSovItem(
                    sov = sov,
                    status = "Incomplete",
                    customerName = customerNameCache[sov.customerId] ?: "Loading…",
                    onClick = {
                        coroutineScope.launch {
                            val system = systemsRepository
                                .getMetalDetectorsWithFullDetailsUsingLocalId(sov.systemId)
                                .firstOrNull()

                            if (system == null) {
                                snackbarHostState.showSnackbar("⚠️ Error: System details missing.")
                                return@launch
                            }

                            val (_, _, engineerId) = PreferencesHelper.getCredentials(context)
                            val intent = Intent(context, SensitivityOptimisationValidationActivity::class.java).apply {
                                putExtra("SOV_ID", sov.sovId)
                                putExtra("SYSTEM_FULL_DETAILS", system)
                                putExtra("ENGINEER_ID", engineerId ?: 0)
                            }
                            context.startActivity(intent)
                        }
                    }
                )
            }
        }

        if (pendingSovs.isNotEmpty()) {
            item { SectionHeader("Pending Upload", Icons.Default.CloudUpload) }
            items(pendingSovs) { sov ->
                val isUploading = uploadingSovIds.contains(sov.sovId)
                ModernSovItem(
                    sov = sov,
                    status = "Pending",
                    customerName = customerNameCache[sov.customerId] ?: "Loading…",
                    isUploading = isUploading,
                    onClick = {
                        if (isUploading) return@ModernSovItem
                        coroutineScope.launch {
                            uploadingSovIds = uploadingSovIds + sov.sovId
                            try {
                                val result = sovRepository.uploadUnsynced(
                                    context = context,
                                    apiService = apiService,
                                    specificId = sov.sovId
                                )
                                snackbarHostState.showSnackbar(result.toString())
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("⚠️ An error occurred during upload.")
                            } finally {
                                uploadingSovIds = uploadingSovIds - sov.sovId
                            }
                        }
                    }
                )
            }
        }

        if (completedSovs.isNotEmpty()) {
            item { SectionHeader("Recently Completed", Icons.Default.History) }
            items(completedSovs) { sov ->
                ModernSovItem(
                    sov = sov,
                    status = "Completed",
                    customerName = customerNameCache[sov.customerId] ?: "Loading…",
                    onClick = {}
                )
            }
        }

        if (unfinishedSovs.isEmpty() && pendingSovs.isEmpty() && completedSovs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No validations found", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
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
fun ModernSovItem(
    sov: SensitivityOptimisationValidationLocal,
    status: String,
    customerName: String,
    isUploading: Boolean = false,
    onClick: () -> Unit
) {
    val formattedDate = try { formatDate(sov.startDate) } catch (_: Exception) { "Invalid date" }
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
                    Text(text = customerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "SN: ${sov.serialNumber}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                StatusBadge(text = status, color = statusColor, isUploading = isUploading)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(text = sov.lastLocation, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(text = formattedDate, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "ID: ${sov.sovId}", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                if (status != "Completed") {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = SnbRed, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color, isUploading: Boolean) {
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
