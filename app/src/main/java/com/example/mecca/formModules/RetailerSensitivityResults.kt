package com.example.mecca.formModules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max


//@file:Suppress("FunctionName")




enum class SensitivityStatus { PASS, AT_RISK, FAIL, NA }

data class MetalSpec(
    val label: String,
    val achievedMM: Double?,    // null means N/A
    val targetMM: Double,
    val maxMM: Double
)

@Composable
fun CalibrationSensitivityPanel(
    productHeightMM: Double,
    ferrous: MetalSpec,
    nonFerrous: MetalSpec,
    ss316: MetalSpec,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Detection Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                AssistChip(
                    onClick = { },
                    label = { Text("Product height: ${productHeightMM.clean()} mm") }
                )
            }
            Spacer(Modifier.height(12.dp))

            SensitivityRow(spec = ferrous)
            Divider(Modifier.padding(vertical = 8.dp))
            SensitivityRow(spec = nonFerrous)
            Divider(Modifier.padding(vertical = 8.dp))
            SensitivityRow(spec = ss316)

            Spacer(Modifier.height(12.dp))
            OverallBanner(ferrous, nonFerrous, ss316)
        }
    }
}

@Composable
private fun SensitivityRow(spec: MetalSpec) {
    val status = statusFor(spec.achievedMM, spec.targetMM, spec.maxMM)
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            spec.label,
            modifier = Modifier.width(110.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Column(Modifier.weight(1f)) {
            // Comparison bar: 0..Max with target band and achieved marker
            ComparisonBar(
                achieved = spec.achievedMM,
                target = spec.targetMM,
                max = spec.maxMM
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Target ≤ ${spec.targetMM.clean()} mm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Max ≤ ${spec.maxMM.clean()} mm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(12.dp))
        StatusPill(status = status, achieved = spec.achievedMM)
    }
}

@Composable
private fun ComparisonBar(achieved: Double?, target: Double, max: Double) {
    val barHeight = 12.dp
    val total = max(max, target)
    Box(
        Modifier
            .fillMaxWidth()
            .height(barHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Target band (Target..Max)
        val targetStartFrac = (target / total).safeFrac()
        val maxFrac = (max / total).safeFrac()
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = maxFrac)
                .background(Color.Transparent)
        )
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = maxFrac)
                .padding(start = (targetStartFrac * 1f).dpWidth())
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
        )

        // Achieved marker
        achieved?.let {
            val frac = (it / total).safeFrac()
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = (frac * 1f).dpWidth())
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun StatusPill(status: SensitivityStatus, achieved: Double?) {
    val (bg, fg, label) = when (status) {
        SensitivityStatus.PASS    -> Triple(Color(0xFFE6F4EA), Color(0xFF1E7A33), "PASS")
        SensitivityStatus.AT_RISK -> Triple(Color(0xFFFFF4E5), Color(0xFF8A5A00), "AT-RISK")
        SensitivityStatus.FAIL    -> Triple(Color(0xFFFFE8E6), Color(0xFF8A1C1C), "FAIL")
        SensitivityStatus.NA      -> Triple(Color(0xFFE6E6E6), Color(0xFF555555), "N/A")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, color = fg, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        achieved?.let {
            Spacer(Modifier.width(6.dp))
            Text("${it.clean()} mm", color = fg, style = MaterialTheme.typography.labelMedium)
        }
    }
}

private fun statusFor(achieved: Double?, target: Double, max: Double): SensitivityStatus {
    if (achieved == null) return SensitivityStatus.NA
    return when {
        achieved <= target -> SensitivityStatus.PASS
        achieved <= max    -> SensitivityStatus.AT_RISK
        else               -> SensitivityStatus.FAIL
    }
}

@Composable
private fun OverallBanner(vararg specs: MetalSpec) {
    val statuses = specs.map { statusFor(it.achievedMM, it.targetMM, it.maxMM) }
    val worst = when {
        SensitivityStatus.FAIL in statuses -> SensitivityStatus.FAIL
        SensitivityStatus.AT_RISK in statuses -> SensitivityStatus.AT_RISK
        SensitivityStatus.NA in statuses && statuses.all { it == SensitivityStatus.NA } -> SensitivityStatus.NA
        else -> SensitivityStatus.PASS
    }

    val text = when (worst) {
        SensitivityStatus.PASS -> "All metals meet target"
        SensitivityStatus.AT_RISK -> "Within max on at least one metal"
        SensitivityStatus.FAIL -> "One or more metals exceed max"
        SensitivityStatus.NA -> "No measurements entered"
    }

    val color = when (worst) {
        SensitivityStatus.PASS -> Color(0xFFE6F4EA)
        SensitivityStatus.AT_RISK -> Color(0xFFFFF4E5)
        SensitivityStatus.FAIL -> Color(0xFFFFE8E6)
        SensitivityStatus.NA -> Color(0xFFE6E6E6)
    }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(12.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

/* ---------- tiny helpers ---------- */

private fun Double.clean(): String {
    val v = this
    return if (v % 1.0 == 0.0) v.toInt().toString() else String.format("%.1f", v)
}

// convert a fraction [0..1] into dp offset using the parent width later
private fun Double.safeFrac(): Float = this.coerceIn(0.0, 1.0).toFloat()

// poor-man's fraction->dp for Box offsets; you can replace with Layout if you want exact measures
@Composable
private fun Float.dpWidth(): Dp = (this * 240f).dp // assumes bar ~240dp wide via fillMaxWidth; visually fine
