package com.snb.inspect.screens.service

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.snb.inspect.AppChromeViewModel
import com.snb.inspect.TopBarState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

@Composable
fun ManualViewerScreen(
    modelName: String,
    manualUrl: String,
    chromeVm: AppChromeViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pdfBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    // Update Top Bar
    LaunchedEffect(modelName) {
        chromeVm.applyRouteChrome(
            TopBarState(
                title = "Manual: $modelName",
                showBack = true,
                showMenu = false,
                showCall = false
            )
        )
    }

    LaunchedEffect(manualUrl) {
        scope.launch(Dispatchers.IO) {
            try {
                // 1. Setup private directory
                val manualsDir = File(context.filesDir, "manuals")
                if (!manualsDir.exists()) manualsDir.mkdirs()

                // Use a hash or a safe filename based on URL
                val fileName = manualUrl.substringAfterLast("/")
                val destinationFile = File(manualsDir, fileName)

                // 2. Download if doesn't exist
                if (!destinationFile.exists()) {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(manualUrl).build()
                    val response = client.newCall(request).execute()
                    
                    if (!response.isSuccessful) throw Exception("Download failed: ${response.code}")
                    
                    response.body?.let { body ->
                        body.byteStream().use { input ->
                            FileOutputStream(destinationFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    } ?: throw Exception("Response body is null")
                }

                // 3. Render PDF pages to Bitmaps
                val fileDescriptor = ParcelFileDescriptor.open(destinationFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fileDescriptor)
                val bitmaps = mutableListOf<Bitmap>()
                
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    // Scale bitmap to screen width for better quality
                    val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    
                    // IMPORTANT: Fill bitmap with white background
                    // Many PDFs have transparent backgrounds and expect the "paper" to be white.
                    bitmap.eraseColor(android.graphics.Color.WHITE)

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                    page.close()
                }
                renderer.close()
                fileDescriptor.close()

                withContext(Dispatchers.Main) {
                    pdfBitmaps = bitmaps
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.message
                    isLoading = false
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color.White)
                Spacer(Modifier.height(16.dp))
                Text("Downloading Manual...", color = Color.White)
            }
        } else if (errorMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error loading manual", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(errorMessage ?: "Unknown error", color = Color.LightGray)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBack) { Text("Go Back") }
            }
        } else {
            // Zoom state
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            val state = rememberTransformableState { zoomChange, panChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 5f)
                if (scale > 1f) {
                    offset += panChange
                } else {
                    offset = Offset.Zero
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RectangleShape)
                    .transformable(state = state)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                scale = 1f
                                offset = Offset.Zero
                            }
                        )
                    }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(pdfBitmaps) { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "PDF Page",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            }
        }
    }
}
