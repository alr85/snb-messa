package com.example.mecca.screens.mainmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.AppChromeViewModel
import com.example.mecca.MyApplication
import com.example.mecca.TopBarState
import com.example.mecca.calibrationViewModels.NoticeViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen(
    navController: NavHostController,
    chromeVm: AppChromeViewModel,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    noticeViewModel: NoticeViewModel
) {


    val isRefreshing by noticeViewModel.isRefreshing.collectAsState()

    val notices by noticeViewModel.notices.collectAsState(initial = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Listen for refresh messages
    LaunchedEffect(Unit) {
        noticeViewModel.events.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(notices.isEmpty()) {
        if (notices.isEmpty()) {
            noticeViewModel.syncNotices(force = true)
        }
    }


    // Top bar state
    LaunchedEffect(Unit) {
        chromeVm.setTopBar(
            TopBarState(
                title = "Notices", // <- rename this. Trust me.
                showBack = false,
                showCall = true,
                showMenu = false
            )
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notices",
                    style = MaterialTheme.typography.titleMedium
                )

                FilledTonalIconButton(
                    onClick = { noticeViewModel.syncNotices(force = true) },
                    enabled = !isRefreshing
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (notices.isEmpty()) {
                EmptyNoticesState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notices, key = { it.noticeId }) { notice ->
                        NoticeCard(
                            title = notice.title,
                            body = notice.body,
                            createdBy = notice.createdBy,
                            dateAdded = notice.dateAdded,
                            isPinned = notice.isPinned
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun EmptyNoticesState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Nothing new",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "When the office posts updates, they’ll show up here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoticeCard(
    title: String,
    body: String,
    createdBy: String?,
    dateAdded: String?,
    isPinned: Boolean
) {

    val dateText = remember(dateAdded) { dateAdded?.let(::formatIsoDateTime) ?: "" }

    val containerColor =
        if (isPinned)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface

    val elevation =
        if (isPinned) 6.dp else 2.dp

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = elevation
        )
    ) {

        Row {
            // ⭐ The Accent Stripe (Slick Level: High)
            if (isPinned) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {

                    Text(
                        text = title.ifBlank { "Notice" },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isPinned) {
                        Spacer(Modifier.width(8.dp))

                        AssistChip(
                            onClick = {},
                            label = { Text("Pinned") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "Pinned",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                val meta = buildString {
                    if (!createdBy.isNullOrBlank()) append("Posted by $createdBy")
                    if (dateText.isNotBlank()) {
                        if (isNotEmpty()) append(" • ")
                        append(dateText)
                    }
                }

                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


private fun formatIsoDateTime(raw: String): String {
    // Accepts "2026-02-12T20:23:24.2236223" or "...Z"
    return try {
        val dt = when {
            raw.endsWith("Z", ignoreCase = true) -> OffsetDateTime.parse(raw)
            raw.contains("+") -> OffsetDateTime.parse(raw)
            else -> OffsetDateTime.parse(raw + "Z") // assume UTC if no offset
        }

        val fmt = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.UK)
        dt.format(fmt)
    } catch (_: Exception) {
        raw.take(19).replace('T', ' ')
    }
}
