package com.example.mecca

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(
    navController: NavHostController,
    title: String? = null,
    showBack: Boolean = false,
    showCall: Boolean = true,
    onMenuClick: (() -> Unit)? = null, // if provided, shows pill menu button on right
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val topAppBarColor = Color(0xFF605F5F)
    val context = LocalContext.current

    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = topAppBarColor,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color.White
        ),
        title = {
            if (!title.isNullOrBlank()) {
                Text(text = title, maxLines = 1)
            }
        },
        navigationIcon = {
            if (showBack) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(40.dp)
                        .width(52.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .padding(start = 8.dp)

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.snb_logo_transparent),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Company Logo"
                    )
                }
            }
        },
        actions = {
            // Optional pill menu button
            if (onMenuClick != null) {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(52.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.14f)) // subtle "tonal" look on dark app bar
                        .clickable { onMenuClick() },
                    contentAlignment = Alignment.Center
                ) {
                    // Use whatever icon you want here
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Optional call button
            if (showCall) {
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = "tel:01977689555".toUri()
                    }
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Filled.Call, contentDescription = "Call")
                }
            }
        }
    )
}
