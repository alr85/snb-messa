package com.example.mecca.screens.mainmenu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.ApiService
import com.example.mecca.AppChromeViewModel
import com.example.mecca.AppDatabase
import com.example.mecca.MyApplication
import com.example.mecca.TopBarState
import com.example.mecca.calibrationViewModels.NoticeViewModel
import com.example.mecca.calibrationViewModels.NoticeViewModelFactory
import com.example.mecca.repositories.NoticeRepository
import kotlin.collections.emptyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen(navController: NavHostController,
                       scrollBehavior: TopAppBarScrollBehavior? = null,
                       chromeVm: AppChromeViewModel,

) {

    val app = LocalContext.current.applicationContext as MyApplication


    val repository = remember {
        NoticeRepository(app.apiService, app.database)
    }

    val viewModel: NoticeViewModel = viewModel(
        factory = NoticeViewModelFactory(repository)
    )

    LaunchedEffect(true) {
        viewModel.syncNotices()
    }

    val notices by viewModel.notices.collectAsState(emptyList())




    LaunchedEffect(Unit) {
        chromeVm.setTopBar(
            TopBarState(
                title = "",
                showBack = false,
                showCall = true,
                showMenu = false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        notices.forEach { notice ->

            Text(
                text = notice.title,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notice.body,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

}
