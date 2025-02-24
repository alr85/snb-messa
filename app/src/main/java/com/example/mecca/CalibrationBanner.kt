import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mecca.CalibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.R

@Composable
fun CalibrationBanner(

    progress: Float, // Progress between 0.0 and 1.0
    viewModel: CalibrationMetalDetectorConveyorViewModel // Pass the view model to access details for the dialog
) {
    var showDetailsDialog by remember { mutableStateOf(false) } // Track whether to show the dialog

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White) // Or any color you want
            .padding(16.dp)
    ) {
        // Banner content

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Company logo on the left with original colors
            Box(
                modifier = Modifier
                    .width(150.dp) // Fixed width for the logo
                    .padding(start = 2.dp) // Add left padding
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_electronics),
                    contentScale = ContentScale.Fit, // Ensures the image fits within the bounds
                    contentDescription = "Company Logo"
                )
            }

            Spacer(modifier = Modifier.width(2.dp)) // Add some space between logo and text

            // Centered text in the remaining space
            Box(
                modifier = Modifier
                    .weight(1f) // Allocate the remaining space
                    .fillMaxWidth(), // Ensure it uses all the space allocated
                contentAlignment = Alignment.Center // Center content in the Box
            ) {
                Text(
                    text = "Metal Detector Calibration",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }



        Spacer(modifier = Modifier.height(12.dp))



        Text(
            text = "${viewModel.modelDescription.value} | S/N: ${viewModel.serialNumber.value} | ${viewModel.customerName.value}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Calibration ID: ${viewModel.calibrationId.value}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray, // Use a predefined grey color
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar - Clickable to show details
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDetailsDialog = true }, // Make the progress bar clickable to open the dialog
        )
    }

    // Dialog to display calibration details
    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },

            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp) // Limit height to prevent the dialog from taking up the full screen
                        .verticalScroll(rememberScrollState()) // Enable scrolling if content exceeds the limit
                        .padding(16.dp)
                ) {
                    CalMetalDetectorConveyorSummaryDetails(viewModel = viewModel)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDetailsDialog = false // Close dialog when confirmed
                    }
                ) {
                    Text("Close")
                }
            },
            containerColor = Color.White
        )
    }
}
