package com.example.mecca.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mecca.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(navController: NavHostController, scrollBehavior: TopAppBarScrollBehavior? = null) {

    val appVersion = getAppVersion()  // Get the app version

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Spacer(modifier = Modifier.height(25.dp))

        Image(
            painter = painterResource(id = R.drawable.logo_electronics),
            contentScale = ContentScale.Fit, // Ensures the image fits within the bounds
            contentDescription = "Company Logo"
        )

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "MESA",
            color = Color.Black,
            fontSize = 24.sp, // Adjust font size for "MESSA"
            fontWeight = FontWeight.Bold, // Optional: make it bold
            maxLines = 1, // Ensure each Text takes only one line
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center // Center-align the text
        )

        Text(
            text = "Mobile Engineering Service Application",
            color = Color.Black, // You can use different color for the second line
            fontSize = 14.sp, // Adjust font size for the description text
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Version: $appVersion",
            color = Color.Black, // You can use different color for the second line
            fontSize = 16.sp, // Adjust font size for the description text
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

    }
}



