package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.MainViewModel
import com.example.ui.apps.AppSelectionScreen
import com.example.ui.completion.SessionCompleteDialog
import com.example.ui.home.HomeScreen
import com.example.ui.lockflow.LockConfirmationDialog
import com.example.ui.lockflow.LockingCountdownOverlay
import com.example.ui.navigation.Screen
import com.example.ui.permissions.PermissionsScreen
import com.example.ui.profiles.ProfilesScreen
import com.example.ui.schedule.ScheduleScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.statistics.StatisticsScreen
import com.example.ui.theme.JailBlack
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

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

                var isSplashActive by rememberSaveable { mutableStateOf(true) }

                Crossfade(
                    targetState = isSplashActive,
                    label = "splash_crossfade"
                ) { showSplash ->
                    if (showSplash) {
                        SplashScreen(
                            onSplashFinished = { isSplashActive = false }
                        )
                    } else {
                        SocialJailApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun SocialJailApp(viewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val screenStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentScreen = screenStack.lastOrNull() ?: Screen.Home

    val activeSession by viewModel.activeSession.collectAsState()
    val scheduledSessions by viewModel.scheduledSessions.collectAsState()
    val completedSessions by viewModel.completedSessions.collectAsState()
    val remainingMillis by viewModel.activeRemainingMillis.collectAsState()
    val permissionStatus by viewModel.permissionStatus.collectAsState()
    val diagnosticReport by viewModel.diagnosticReport.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isLoadingApps by viewModel.isLoadingApps.collectAsState()
    val selectedPackages by viewModel.selectedPackages.collectAsState()
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val defaultDurationMinutes by viewModel.defaultDurationMinutes.collectAsState()
    val isGoalPromptEnabled by viewModel.isGoalPromptEnabled.collectAsState()
    val completionSession by viewModel.completionSession.collectAsState()
    val completionAttempts by viewModel.completionAttempts.collectAsState()

    // Lock Flow State
    var showLockConfirmationDialog by remember { mutableStateOf(false) }
    var confirmationInitialDuration by remember { mutableIntStateOf(defaultDurationMinutes) }
    var showLockingOverlay by remember { mutableStateOf(false) }
    var pendingDurationMinutes by remember { mutableIntStateOf(120) }
    var pendingGoalText by remember { mutableStateOf<String?>(null) }

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
                        remainingMillis = remainingMillis,
                        selectedPackagesCount = selectedPackages.size,
                        selectedProfile = selectedProfile,
                        defaultDurationMinutes = defaultDurationMinutes,
                        permissionStatus = permissionStatus,
                        diagnosticReport = diagnosticReport,
                        onStartSessionClick = {
                            if (selectedPackages.isEmpty()) {
                                screenStack.add(Screen.AppSelection)
                            } else {
                                confirmationInitialDuration = defaultDurationMinutes
                                showLockConfirmationDialog = true
                            }
                        },
                        onQuickJailClick = { minutes ->
                            if (selectedPackages.isEmpty()) {
                                screenStack.add(Screen.AppSelection)
                            } else {
                                confirmationInitialDuration = minutes
                                showLockConfirmationDialog = true
                            }
                        },
                        onManageAppsClick = {
                            viewModel.loadInstalledApps()
                            screenStack.add(Screen.AppSelection)
                        },
                        onProfilesClick = {
                            viewModel.loadInstalledApps()
                            screenStack.add(Screen.Profiles)
                        },
                        onScheduleClick = {
                            screenStack.add(Screen.Schedule)
                        },
                        onStatisticsClick = {
                            screenStack.add(Screen.Statistics)
                        },
                        onSettingsClick = {
                            screenStack.add(Screen.Settings)
                        },
                        onPermissionsClick = {
                            screenStack.add(Screen.Permissions)
                        }
                    )
                }

                Screen.AppSelection -> {
                    AppSelectionScreen(
                        installedApps = installedApps,
                        selectedPackages = selectedPackages,
                        isLoading = isLoadingApps,
                        onToggleApp = { pkg -> viewModel.toggleAppSelection(pkg) },
                        onSelectAll = { viewModel.selectAllApps(installedApps) },
                        onDeselectAll = { viewModel.deselectAllApps() },
                        onBack = {
                            if (screenStack.size > 1) screenStack.removeAt(screenStack.size - 1)
                        }
                    )
                }

                Screen.Profiles -> {
                    ProfilesScreen(
                        profiles = profiles,
                        activeProfile = selectedProfile,
                        installedApps = installedApps,
                        onSelectProfile = { p ->
                            viewModel.applyProfile(p)
                            if (screenStack.size > 1) screenStack.removeAt(screenStack.size - 1)
                        },
                        onSaveProfile = { p -> viewModel.saveProfile(p) },
                        onDeleteProfile = { id -> viewModel.deleteProfile(id) },
                        onBack = {
                            if (screenStack.size > 1) screenStack.removeAt(screenStack.size - 1)
                        }
                    )
                }

                Screen.Schedule -> {
                    ScheduleScreen(
                        schedules = schedules,
                        profiles = profiles,
                        onToggleSchedule = { id, enabled -> viewModel.toggleSchedule(id, enabled) },
                        onSaveSchedule = { s -> viewModel.saveSchedule(s) },
                        onDeleteSchedule = { id -> viewModel.deleteSchedule(id) },
                        onBack = {
                            if (screenStack.size > 1) screenStack.removeAt(screenStack.size - 1)
                        }
                    )
                }

                Screen.Statistics -> {
                    StatisticsScreen(
                        statistics = statistics,
                        completedSessions = completedSessions,
                        onBack = {
                            if (screenStack.size > 1) screenStack.removeAt(screenStack.size - 1)
                        }
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        defaultDurationMinutes = defaultDurationMinutes,
                        isGoalPromptEnabled = isGoalPromptEnabled,
                        permissionStatus = permissionStatus,
                        onSetDefaultDuration = { mins -> viewModel.setDefaultDuration(mins) },
                        onSetGoalPromptEnabled = { enabled -> viewModel.setGoalPromptEnabled(enabled) },
                        onOpenPermissions = { screenStack.add(Screen.Permissions) },
                        onClearHistory = { viewModel.clearSessionHistory() },
                        onClearStatistics = { viewModel.clearStatistics() },
                        onBack = {
                            if (screenStack.size > 1) screenStack.removeAt(screenStack.size - 1)
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

                else -> {
                    // Fallback to Home
                    HomeScreen(
                        activeSession = activeSession,
                        remainingMillis = remainingMillis,
                        selectedPackagesCount = selectedPackages.size,
                        selectedProfile = selectedProfile,
                        defaultDurationMinutes = defaultDurationMinutes,
                        permissionStatus = permissionStatus,
                        diagnosticReport = diagnosticReport,
                        onStartSessionClick = { showLockConfirmationDialog = true },
                        onQuickJailClick = { mins ->
                            confirmationInitialDuration = mins
                            showLockConfirmationDialog = true
                        },
                        onManageAppsClick = { screenStack.add(Screen.AppSelection) },
                        onProfilesClick = { screenStack.add(Screen.Profiles) },
                        onScheduleClick = { screenStack.add(Screen.Schedule) },
                        onStatisticsClick = { screenStack.add(Screen.Statistics) },
                        onSettingsClick = { screenStack.add(Screen.Settings) },
                        onPermissionsClick = { screenStack.add(Screen.Permissions) }
                    )
                }
            }

            // Lock Confirmation Dialog Flow (3/4 deliberate steps)
            if (showLockConfirmationDialog) {
                LockConfirmationDialog(
                    selectedPackages = selectedPackages.toList(),
                    allApps = installedApps,
                    initialDurationMinutes = confirmationInitialDuration,
                    activeProfile = selectedProfile,
                    isGoalPromptEnabled = isGoalPromptEnabled,
                    onDismiss = { showLockConfirmationDialog = false },
                    onConfirmLock = { duration, goal ->
                        showLockConfirmationDialog = false
                        pendingDurationMinutes = duration
                        pendingGoalText = goal
                        showLockingOverlay = true
                    }
                )
            }

            // 3-2-1 Locking Countdown Overlay
            if (showLockingOverlay) {
                LockingCountdownOverlay(
                    onFinished = {
                        showLockingOverlay = false
                        coroutineScope.launch {
                            viewModel.startSession(
                                packages = selectedPackages.toList(),
                                durationMinutes = pendingDurationMinutes,
                                goalText = pendingGoalText,
                                profileName = selectedProfile?.name
                            )
                            screenStack.clear()
                            screenStack.add(Screen.Home)
                        }
                    }
                )
            }

            // End of Session Completion Dialog
            completionSession?.let { session ->
                SessionCompleteDialog(
                    session = session,
                    blockedAttemptsCount = completionAttempts,
                    onDismiss = { viewModel.dismissCompletionDialog() }
                )
            }
        }
    }
}
