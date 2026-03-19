package com.snb.inspect.formModules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snb.inspect.ui.theme.SnbRed

@Composable
fun PvSectionSummaryCard(
    title: String = "Section P.V. Summary",
    rules: List<PvRule>
) {
    val overallStatus = when {
        rules.any { it.status == PvRuleStatus.Fail } -> "Fail"
        rules.any { it.status == PvRuleStatus.Warning || it.status == PvRuleStatus.Incomplete } -> "Warning"
        rules.all { it.status == PvRuleStatus.NA } -> "N/A"
        rules.all { it.status == PvRuleStatus.Pass || it.status == PvRuleStatus.NA } -> "Pass"
        else -> "N/A"
    }

    val statusColor = when (overallStatus) {
        "Pass" -> Color(0xFF4CAF50)
        "Fail" -> SnbRed
        "Warning" -> Color(0xFFFFA000)
        else -> Color.Gray
    }

    Spacer(Modifier.height(12.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                
                StatusBadge(status = overallStatus, color = statusColor)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rules.forEach { rule ->
                    SummaryRuleItem(rule)
                }
            }

        }
    }
    Spacer(Modifier.height(12.dp))

}

@Composable
private fun StatusBadge(status: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = status.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun SummaryRuleItem(rule: PvRule) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = when (rule.status) {
                PvRuleStatus.Pass -> Icons.Default.CheckCircle
                PvRuleStatus.Fail -> Icons.Default.Cancel
                PvRuleStatus.Warning -> Icons.Default.Warning
                PvRuleStatus.Incomplete -> Icons.AutoMirrored.Filled.Help
                PvRuleStatus.NA -> Icons.Default.RemoveCircle
            },
            contentDescription = null,
            tint = when (rule.status) {
                PvRuleStatus.Pass -> Color(0xFF4CAF50)
                PvRuleStatus.Fail -> SnbRed
                PvRuleStatus.Warning -> Color(0xFFFFA000)
                PvRuleStatus.Incomplete -> Color(0xFFFFA000)
                PvRuleStatus.NA -> Color.Gray
            },
            modifier = Modifier.size(20.dp)
        )
        
        Text(
            text = rule.description,
            style = MaterialTheme.typography.bodySmall,
            color = if (rule.status == PvRuleStatus.NA) Color.Gray else Color.Black,
            lineHeight = 16.sp
        )
    }
}
