package com.example.mecca

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.core.net.toUri


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(navController: NavHostController, scrollBehavior: TopAppBarScrollBehavior? = null) {
    val topAppBarColor = Color(0xFF605F5F)

    val context = LocalContext.current // Get the current context for starting the phone call intent

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = topAppBarColor,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color.White
        ),
        title = {
            Text(text = "")
        },
        actions = {
            // Phone button in the top right
            IconButton(onClick = {
                // Create an intent to call the number
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = "tel:01977689555".toUri()

                }
                context.startActivity(intent)
            }) {
                Icon(
                    imageVector = Icons.Filled.Call, // Use a telephone icon
                    contentDescription = "Call"
                )
            }
        },
        navigationIcon = {
            // Company logo on the left with original colors
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .padding(start = 8.dp) // Add left padding here
            ) {
                Image(
                    painter = painterResource(id = R.drawable.snb_crop),
                    contentScale = ContentScale.Fit, // Ensures the image fits within the bounds
                    contentDescription = "Company Logo"
                )

            }
        }
    )
}
