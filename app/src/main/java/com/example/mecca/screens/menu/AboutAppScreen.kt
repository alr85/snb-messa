package com.example.mecca.screens.menu


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mecca.AppChromeViewModel
import com.example.mecca.R
import com.example.mecca.TopBarState
import com.example.mecca.screens.getAppVersion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(
    chromeVm: AppChromeViewModel
) {

    val appVersion = getAppVersion()  // Get the app version
    val audiowide = FontFamily(Font(R.font.audiowide))

//    LaunchedEffect(Unit) {
//        chromeVm.setTopBar(
//            TopBarState(
//                title = "About",
//                showBack = true,
//                showCall = false
//            )
//        )
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


//        Image(
//            painter = painterResource(id = R.drawable.logo_electronics),
//            contentScale = ContentScale.Fit, // Ensures the image fits within the bounds
//            contentDescription = "Company Logo"
//        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "MESA",
            color = Color.Black,
            fontSize = 90.sp, // Adjust font size for "MESSA"
            fontWeight = FontWeight.Bold, // Optional: make it bold
            fontFamily = audiowide,
            maxLines = 1, // Ensure each Text takes only one line
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center // Center-align the text
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
        Spacer(modifier = Modifier.height(5.dp))

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
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Section: About
                Text(
                    text = "About MESA",
                    color = Color(0xFFB71C1C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
                Text(
                    text = "MESA (Mobile Engineering Service Application) is being developed to simplify the process of recording, managing, and syncing calibration and service data directly from the field",
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
                        please report them to your manager. Your feedback directly shapes how MESA evolves.
                    """.trimIndent(),
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )
            }
        }
    }

}