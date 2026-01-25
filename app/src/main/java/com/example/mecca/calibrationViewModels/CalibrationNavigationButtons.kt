import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.ui.theme.FormBackground

@Composable
fun CalibrationNavigationButtons(
    viewModel: CalibrationMetalDetectorConveyorViewModel,
    onPreviousClick: () -> Unit,
    onCancelClick: () -> Unit,
    onNextClick: () -> Unit,
    onSaveAndExitClick: () -> Unit,
    isNextEnabled: Boolean,
    isFirstStep: Boolean,
    windowSizeClass: WindowSizeClass,
) {
    var showDialog by remember { mutableStateOf(false) }

    val activity = LocalActivity.current
    val showText = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FormBackground)
            .navigationBarsPadding()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onPreviousClick,
            enabled = !isFirstStep,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            if (showText) {
                Spacer(Modifier.width(8.dp))
                Text("Back", maxLines = 1, softWrap = false)
            }
        }

        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Close, contentDescription = null)
            if (showText) {
                Spacer(Modifier.width(8.dp))
                Text("Cancel", maxLines = 1, softWrap = false)
            }
        }

        OutlinedButton(
            onClick = {
                onSaveAndExitClick()
                activity?.finish()
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            if (showText) {
                Spacer(Modifier.width(8.dp))
                Text("Exit", maxLines = 1, softWrap = false)
            }
        }

        Button(
            onClick = onNextClick,
            enabled = isNextEnabled,
            modifier = Modifier.weight(1f)
        ) {
            if (showText) {
                Text("Next", maxLines = 1, softWrap = false)
                Spacer(Modifier.width(8.dp))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Are you sure?") },
            text = { Text("Cancelling will discard all calibration data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        viewModel.clearCalibrationData()
                        viewModel.deleteCalibration(viewModel.calibrationId.value)
                        onCancelClick()
                        activity?.finish()
                    }
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("No") }
            }
        )
    }
}
