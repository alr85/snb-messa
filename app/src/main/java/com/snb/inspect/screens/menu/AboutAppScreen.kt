package com.snb.inspect.screens.menu


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snb.inspect.AppChromeViewModel
import com.snb.inspect.R
import com.snb.inspect.screens.getAppVersion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(
    chromeVm: AppChromeViewModel
) {

    val context = LocalContext.current
    val appVersion = getAppVersion()  // Get the app version

    var showChangelogDialog by remember { mutableStateOf(false) }

    // Automatically load the latest changes from the raw/changelog.md file
    val fullChangelog = remember {
        try {
            val rawText = context.resources.openRawResource(R.raw.changelog).bufferedReader().use { it.readText() }
            formatChangelog(rawText)
        } catch (e: Exception) {
            buildAnnotatedString { append("Error loading changelog.") }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        // Documentation / About Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    Color(0xFFF9F9F9),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(16.dp)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {


                Image(
                    painter = painterResource(id = R.drawable.inspect_logo),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit, // Ensures the image fits within the bounds
                    contentDescription = "Company Logo 2"
                )


                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "v$appVersion",
                    color = Color.Black, // You can use different color for the second line
                    fontSize = 16.sp, // Adjust font size for the description text
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showChangelogDialog = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View Change Log", color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))


                // Section: About
                Text(
                    text = "About SNB INSPECT",
                    color = Color(0xFFB71C1C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
                Text(
                    text = "SNB INSPECT is being developed to simplify the process of recording, managing, and syncing calibration and service data directly from the field",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )

                // Section: How It Works
                Text(
                    text = "How It Works",
                    color = Color(0xFFB71C1C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
                Text(
                    text = """
                        • Engineers log in using their assigned credentials.
                        • Customer and system data is synced from the head office database.
                        • Engineers perform paperless calibration procedures, inputting data via a touch screen.
                        • Calibrations can be performed fully offline — results are stored locally.
                        • When reconnected, calibration data syncs to the office database.
                    """.trimIndent(),
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )

                // Section: Features
                Text(
                    text = "Key Features",
                    color = Color(0xFFB71C1C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
                Text(
                    text = """
                        • Offline data storage with automatic cloud syncing.
                        • CSV export for calibration reports.
                        • Secure data handling and restricted user access.
                    """.trimIndent(),
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )

                // Section: Future Plans
                Text(
                    text = "Planned Improvements",
                    color = Color(0xFFB71C1C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
                Text(
                    text = """
                        • Background cloud sync for seamless operation.
                        • Integration with multiple system types (checkweighers, static scales and X-ray systems).
                        • 'Live' diary/service scheduling with notifications.
                        • Service Engineer Knowledge Base
                        • Enhanced user experience.
                        • Engineer feedback and suggestion portal within the app.
                    """.trimIndent(),
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )

                // Section: Feedback
                Text(
                    text = "Feedback & Support",
                    color = Color(0xFFB71C1C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
                Text(
                    text = """
                        If you encounter issues, spot something odd, or have ideas that would make life easier, 
                        please report them to your manager. Your feedback directly shapes how INSPECT evolves.
                    """.trimIndent(),
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )
            }
        }
    }

    if (showChangelogDialog) {
        AlertDialog(
            onDismissRequest = { showChangelogDialog = false },
            title = { Text("Full Change Log") },
            text = {
                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    val dialogScrollState = rememberScrollState()
                    Column(modifier = Modifier.verticalScroll(dialogScrollState)) {
                        Text(
                            text = fullChangelog,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 20.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChangelogDialog = false }) {
                    Text("Close", color = Color(0xFFB71C1C))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Simple parser to convert Markdown-style changelog into an AnnotatedString
 * that mimics a rendered preview (no ## or ### markers).
 */
fun formatChangelog(rawText: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = rawText.lines()
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                // Main Header (e.g. # Changelog)
                trimmed.startsWith("# ") -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)) {
                        append(trimmed.removePrefix("# ").trim())
                    }
                    append("\n")
                }
                // Version Header (e.g. ## [1.2.0])
                trimmed.startsWith("## ") -> {
                    append("\n")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFB71C1C))) {
                        append(trimmed.removePrefix("## ").trim())
                    }
                    append("\n")
                }
                // Category Header (e.g. ### Fixed)
                trimmed.startsWith("### ") -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                        append(trimmed.removePrefix("### ").trim())
                    }
                    append("\n")
                }
                // Bullet points
                trimmed.startsWith("- ") -> {
                    append("  • ")
                    append(trimmed.removePrefix("- ").trim())
                    append("\n")
                }
                // Empty lines or normal text
                else -> {
                    append(line)
                    append("\n")
                }
            }
        }
    }
}
