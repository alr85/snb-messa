import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.mecca.ui.theme.SnbDarkGrey
import com.example.mecca.ui.theme.SnbRed

@Composable
fun MyAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = SnbRed,  // Use your defined red primary color
        secondary = SnbDarkGrey
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography, // Use the default Material3 Typography
        content = content
    )
}

