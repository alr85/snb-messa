
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel

@Composable
fun CalibrationNavigationButtons(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel,
    onPreviousClick: () -> Unit,
    onCancelClick: () -> Unit,
    onNextClick: () -> Unit,
    onSaveAndExitClick: () -> Unit,
    isNextEnabled: Boolean,
    isFirstStep: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }
    val activity = LocalActivity.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Previous
        OutlinedButton(
            onClick = onPreviousClick,
            enabled = !isFirstStep,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            Spacer(Modifier.width(8.dp))
            Text("Back")
        }

        // Cancel
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Close, null)
            Spacer(Modifier.width(8.dp))
            Text("Cancel")
        }

        // Save + Exit
        OutlinedButton(
            onClick = {
                onSaveAndExitClick()

                activity?.finish()
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
            Spacer(Modifier.width(8.dp))
            Text("Exit")
        }

        // Next
        Button(
            onClick = onNextClick,
            enabled = isNextEnabled,
            modifier = Modifier.weight(1f)
        ) {
            Text("Next")
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
        }
    }

    // Cancel confirmation dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Are you sure?") },
            text = { Text("Cancelling will discard all calibration data.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    viewModel.clearCalibrationData()
                    viewModel.deleteCalibration(viewModel.calibrationId.value)
                    onCancelClick()
                    activity?.finish()
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("No") }
            }
        )
    }
}