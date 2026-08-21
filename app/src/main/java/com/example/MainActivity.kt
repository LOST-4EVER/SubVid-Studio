package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.model.AppTab
import com.example.ui.screens.BatchHubScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioNavBar
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.SubVidStudioTheme
import com.example.viewmodel.MainViewModel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubVidStudioTheme {
                StudioApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StudioApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isFullscreenVideo by viewModel.isFullscreenVideo.collectAsState()
    val videoMetadata by viewModel.videoMetadata.collectAsState()
    val subtitleTrack by viewModel.subtitleTrack.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Pause player and save project on lifecycle pause/stop
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.playerController.pause()
                viewModel.saveCurrentProject()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Synchronize system bars visibility with Fullscreen Video state
    DisposableEffect(isFullscreenVideo) {
        val window = (context as? Activity)?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isFullscreenVideo) {
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            val currentWindow = (context as? Activity)?.window
            if (currentWindow != null) {
                val insetsController = WindowCompat.getInsetsController(currentWindow, currentWindow.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val hasActiveSession = videoMetadata.uriString.isNotEmpty() || subtitleTrack.cues.isNotEmpty()

    if (isFullscreenVideo) {
        // True Edge-to-Edge Fullscreen Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ImmersiveBg)
        ) {
            EditorScreen(viewModel = viewModel)
        }
    } else {
        Scaffold(
            containerColor = ImmersiveBg,
            bottomBar = {
                StudioNavBar(
                    currentTab = currentTab,
                    onTabSelected = { tab -> viewModel.setTab(tab) },
                    hasActiveProject = hasActiveSession
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ImmersiveBg)
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    AppTab.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateTab = { tab -> viewModel.setTab(tab) }
                        )
                    }
                    AppTab.EDITOR -> {
                        EditorScreen(
                            viewModel = viewModel
                        )
                    }
                    AppTab.BATCH -> {
                        BatchHubScreen(
                            viewModel = viewModel
                        )
                    }
                    AppTab.SETTINGS -> {
                        SettingsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
