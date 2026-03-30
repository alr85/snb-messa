package com.snb.inspect.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snb.inspect.PreferencesHelper
import com.snb.inspect.calibrationViewModels.WeekendRotaViewModel
import com.snb.inspect.dataClasses.WeekendRotaResponse
import com.snb.inspect.ui.theme.SnbRed
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WeekendRotaScreen(
    viewModel: WeekendRotaViewModel
) {
    val rotaData by viewModel.rotaData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val (_, _, currentEngineerId) = PreferencesHelper.getCredentials(context)

    LaunchedEffect(Unit) {
        viewModel.fetchRota()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = SnbRed
            )
        } else if (error != null) {
            Text(
                text = error ?: "Unknown error",
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        } else if (rotaData.isEmpty()) {
            Text(
                text = "No rota data found",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(rotaData) { item ->
                    RotaItem(
                        item = item,
                        isHighlighted = item.engineerId == currentEngineerId
                    )
                }
            }
        }
    }
}

@Composable
fun RotaItem(item: WeekendRotaResponse, isHighlighted: Boolean) {
    val backgroundColor = if (isHighlighted) Color(0xFFFFEBEE) else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .then(
                if (isHighlighted) Modifier.background(
                    SnbRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = SnbRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatRotaDate(item.rotaDate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) SnbRed else Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFFEEEEEE))
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.engineerName ?: "Unknown Engineer",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isHighlighted) SnbRed else Color.DarkGray,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

fun formatRotaDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "TBC"
    
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "MM/dd/yyyy"
    )
    
    for (pattern in formats) {
        try {
            val inputFormat = SimpleDateFormat(pattern, Locale.getDefault())
            val date = inputFormat.parse(dateString)
            if (date != null) {
                val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
                return outputFormat.format(date)
            }
        } catch (e: Exception) {
            // Try next pattern
        }
    }
    
    // If no pattern matches, return the raw string so we can see what it is
    return dateString
}
