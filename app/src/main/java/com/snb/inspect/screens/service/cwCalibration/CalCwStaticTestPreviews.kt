package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snb.inspect.ui.theme.AppCard
import com.snb.inspect.ui.theme.MyAppTheme
import com.snb.inspect.ui.theme.SnbRed

/**
 * Option 1: Visual Scale Platter Layout with Direction Badges & Active Corner Highlight
 */
@Preview(showBackground = true)
@Composable
fun OffCentreScalePlatterPreview() {
    MyAppTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CompassCalibration,
                            contentDescription = null,
                            tint = SnbRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Off Centre Loading Test",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Scale Platter Position Mapping",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        Surface(
                            color = Color(0xFFF1F8E9),
                            shape = CircleShape
                        ) {
                            Text(
                                "5 Positions",
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Direct Position Inputs (no middle extra surface layer)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CornerInputField(
                            cornerBadge = "↖ BL",
                            title = "Back Left",
                            value = "100.02",
                            delta = "+0.02g",
                            modifier = Modifier.weight(1f)
                        )
                        CornerInputField(
                            cornerBadge = "↗ BR",
                            title = "Back Right",
                            value = "100.01",
                            delta = "+0.01g",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Center Platter Focus Position
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CenterInputField(
                            value = "100.00",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Front Corners Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CornerInputField(
                            cornerBadge = "↙ FL",
                            title = "Front Left",
                            value = "99.98",
                            delta = "-0.02g",
                            modifier = Modifier.weight(1f)
                        )
                        CornerInputField(
                            cornerBadge = "↘ FR",
                            title = "Front Right",
                            value = "100.03",
                            delta = "+0.03g",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Incorporated Evaluation Summary
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Off Centre Evaluation Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8F9FA),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Centre Reading", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text("100.00g", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8F9FA),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Max Deviation", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text("0.03g", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Option 2: 3x3 Platter Matrix with Position Badges & Live Deviation
 */
@Preview(showBackground = true)
@Composable
fun OffCentreMatrixPreview() {
    MyAppTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GridView, contentDescription = null, tint = SnbRed)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Off Centre Platter Matrix",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 3x3 Platter Matrix Layout
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top Row
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MatrixCornerBox(label = "Back Left", code = "BL", value = "100.05g", delta = "+0.05", modifier = Modifier.weight(1f))
                                MatrixCornerBox(label = "Back Right", code = "BR", value = "100.01g", delta = "+0.01", modifier = Modifier.weight(1f))
                            }

                            // Center Platter Focus
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(2.dp, SnbRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = SnbRed,
                                        shape = CircleShape,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("C", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Centre Reference Value", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text("100.00g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(6.dp)) {
                                        Text("TARGET", color = Color(0xFF2E7D32), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Bottom Row
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MatrixCornerBox(label = "Front Left", code = "FL", value = "99.97g", delta = "-0.03", modifier = Modifier.weight(1f))
                                MatrixCornerBox(label = "Front Right", code = "FR", value = "100.02g", delta = "+0.02", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CornerInputField(
    cornerBadge: String,
    title: String,
    value: String,
    delta: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        cornerBadge,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    delta,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (delta.startsWith("+")) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.SemiBold
                )
            }
            OutlinedTextField(
                value = value,
                onValueChange = {},
                label = { Text(title) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CenterInputField(
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = SnbRed
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = SnbRed,
                    shape = CircleShape,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⦿", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Centre Platter Position (Reference)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = SnbRed
                )
            }
            OutlinedTextField(
                value = value,
                onValueChange = {},
                label = { Text("Centre Value (g)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MatrixCornerBox(
    label: String,
    code: String,
    value: String,
    delta: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(code, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = SnbRed)
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f))
                Text(delta, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
            OutlinedTextField(
                value = value,
                onValueChange = {},
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
