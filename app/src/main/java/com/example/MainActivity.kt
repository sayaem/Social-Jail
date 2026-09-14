package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.MainViewModel
import com.example.ui.create.CreateLockScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.navigation.Screen
import com.example.ui.permissions.PermissionsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.JailBlack
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.refreshPermissions()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                SocialJailApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SocialJailApp(viewModel: MainViewModel) {
    val screenStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentScreen = screenStack.lastOrNull() ?: Screen.Home

    val activeSession by viewModel.activeSession.collectAsState()
    val scheduledSessions by viewModel.scheduledSessions.collectAsState()
    val completedSessions by viewModel.completedSessions.collectAsState()
    val remainingMillis by viewModel.activeRemainingMillis.collectAsState()
    val permissionStatus by viewModel.permissionStatus.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isLoadingApps by viewModel.isLoadingApps.collectAsState()

    BackHandler(enabled = screenStack.size > 1) {
        screenStack.removeAt(screenStack.size - 1)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(JailBlack)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Home -> {
                    HomeScreen(
                        activeSession = activeSession,
                        scheduledSessions = scheduledSessions,
                        completedSessions = completedSessions,
                        remainingMillis = remainingMillis,
                        permissionStatus = permissionStatus,
                        onCreateLockClick = {
                            viewModel.loadInstalledApps()
                            screenStack.add(Screen.CreateLock)
                        },
                        onPermissionsClick = {
                            screenStack.add(Screen.Permissions)
                        },
                        onHistoryClick = {
                            screenStack.add(Screen.History)
                        },
                        onSettingsClick = {
                            screenStack.add(Screen.Settings)
                        },
                        onCancelScheduled = { id ->
                            viewModel.cancelScheduledSession(id)
                        }
                    )
                }

                Screen.CreateLock -> {
                    CreateLockScreen(
                        installedApps = installedApps,
                        isLoadingApps = isLoadingApps,
                        permissionStatus = permissionStatus,
                        onBackClick = {
                            if (screenStack.size > 1) screenStack.removeAt(screenStack.size - 1)
                        },
                        onOpenPermissions = {
                            screenStack.add(Screen.Permissions)
                        },
                        onStartLock = { pkgs, names, duration, mode ->
                            viewModel.startImmediateLock(pkgs, names, duration, mode)
                        },
                        onScheduleLock = { pkgs, names, start, end, mode ->
                            viewModel.scheduleFutureLock(pkgs, names, start, end, mode)
                        },
                        onSuccess = {
                            screenStack.clear()
                            screenStack.add(Screen.Home)
                        }
                    )
                }

                Screen.Permissions -> {
                    PermissionsScreen(
                        permissionStatus = permissionStatus,
                        onRefresh = { viewModel.refreshPermissions() },
                        onBackClick = {
                            if (screenStack.size > 1) screenStack.removeAt(screenStack.size - 1)
                        }
                    )
                }

                Screen.History -> {
                    HistoryScreen(
                        completedSessions = completedSessions,
                        onBackClick = {
                            if (screenStack.size > 1) screenStack.removeAt(screenStack.size - 1)
                        }
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        isLockActive = activeSession != null && remainingMillis > 0,
                        permissionStatus = permissionStatus,
                        onOpenPermissions = {
                            screenStack.add(Screen.Permissions)
                        },
                        onBackClick = {
                            if (screenStack.size > 1) screenStack.removeAt(screenStack.size - 1)
                        }
                    )
                }
            }
        }
    }
}
