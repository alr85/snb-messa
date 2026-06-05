package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.snb.inspect.R
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.ui.theme.FormBackground
import com.snb.inspect.ui.theme.SnbRed

@Composable
fun SovBanner(
    progress: Float,
    viewModel: SensitivityOptimisationValidationViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FormBackground)
            .padding(10.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(150.dp).padding(start = 2.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.logo_electronics),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Company Logo"
                )
            }
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Validation", style = MaterialTheme.typography.titleMedium)
                    Text(text = "ID: ${viewModel.sovId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "${viewModel.system.modelDescription} ( ${viewModel.system.serialNumber} )",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = SnbRed,
            trackColor = Color.LightGray,
            modifier = Modifier.fillMaxWidth().height(12.dp)
        )
    }
}
