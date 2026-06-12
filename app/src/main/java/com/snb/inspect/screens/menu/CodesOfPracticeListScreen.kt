package com.snb.inspect.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.snb.inspect.AppChromeViewModel
import com.snb.inspect.TopBarState
import com.snb.inspect.calibrationViewModels.CodesOfPracticeViewModel
import com.snb.inspect.dataClasses.CodeOfPracticeLocal
import com.snb.inspect.ui.theme.SnbRed
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodesOfPracticeListScreen(
    viewModel: CodesOfPracticeViewModel,
    navController: androidx.navigation.NavHostController,
    chromeVm: AppChromeViewModel
) {
    val codes by viewModel.filteredCodes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    // Example categories - these could be dynamic based on data
    val categories = listOf(
        "All" to null,
        "Retailer" to "Retailer",
        "Industry" to "Industry",
        "Legal" to "Legal"
    )

    LaunchedEffect(Unit) {
        chromeVm.applyRouteChrome(
            TopBarState(
                title = "Codes of Practice",
                showBack = true,
                showMenu = false,
                showCall = false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search codes...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { (displayName, category) ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { viewModel.setSelectedCategory(category) },
                    label = { Text(displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Codes List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(codes) { code ->
                CodeOfPracticeItemCard(code) {
                    val encodedUrl = URLEncoder.encode(code.url, StandardCharsets.UTF_8.toString())
                    // Reusing manualViewer for PDF viewing as it handles offline download/storage
                    navController.navigate("manualViewer/${code.title}/$encodedUrl")
                }
            }
            
            if (codes.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No documents found", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun CodeOfPracticeItemCard(code: CodeOfPracticeLocal, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SnbRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = SnbRed)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = code.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = code.category ?: "General",
                    style = MaterialTheme.typography.bodySmall,
                    color = SnbRed
                )
                if (!code.description.isNullOrBlank()) {
                    Text(
                        text = code.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
