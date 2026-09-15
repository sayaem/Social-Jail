package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.data.repository.LockRepository
import com.example.domain.model.InstalledAppInfo
import com.example.domain.model.LockMode
import com.example.domain.model.LockSession
import com.example.domain.model.Profile
import com.example.domain.model.Schedule
import com.example.domain.model.StatisticsData
import com.example.util.PackageUtils
import com.example.util.PermissionStatus
import com.example.util.PermissionUtils
import com.example.util.ScheduleManager
import com.example.util.SocialJailDiagnostics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LockRepository(application)
    private val preferences = AppPreferences(application)

    val activeSession: StateFlow<LockSession?> = repository.activeSessionFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val scheduledSessions: StateFlow<List<LockSession>> = repository.scheduledSessionsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedSessions: StateFlow<List<LockSession>> = repository.completedSessionsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val profiles: StateFlow<List<Profile>> = repository.profilesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val schedules: StateFlow<List<Schedule>> = repository.schedulesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val statistics: StateFlow<StatisticsData> = repository.statisticsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticsData()
        )

    private val _permissionStatus = MutableStateFlow(PermissionUtils.getPermissionStatus(application))
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _activeRemainingMillis = MutableStateFlow(0L)
    val activeRemainingMillis: StateFlow<Long> = _activeRemainingMillis.asStateFlow()

    private val _selectedPackages = MutableStateFlow<Set<String>>(emptySet())
    val selectedPackages: StateFlow<Set<String>> = _selectedPackages.asStateFlow()

    private val _selectedProfile = MutableStateFlow<Profile?>(null)
    val selectedProfile: StateFlow<Profile?> = _selectedProfile.asStateFlow()

    private val _defaultDurationMinutes = MutableStateFlow(preferences.defaultDurationMinutes)
    val defaultDurationMinutes: StateFlow<Int> = _defaultDurationMinutes.asStateFlow()

    private val _isGoalPromptEnabled = MutableStateFlow(preferences.isGoalPromptEnabled)
    val isGoalPromptEnabled: StateFlow<Boolean> = _isGoalPromptEnabled.asStateFlow()

    private val _themeMode = MutableStateFlow(preferences.themeMode)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Completion Dialog State
    private val _completionSession = MutableStateFlow<LockSession?>(null)
    val completionSession: StateFlow<LockSession?> = _completionSession.asStateFlow()

    private val _completionAttempts = MutableStateFlow(0)
    val completionAttempts: StateFlow<Int> = _completionAttempts.asStateFlow()

    private val _diagnosticReport = MutableStateFlow(
        SocialJailDiagnostics.evaluateDiagnosticStatus(
            application,
            isLockActive = false
        )
    )
    val diagnosticReport: StateFlow<SocialJailDiagnostics.DiagnosticReport> = _diagnosticReport.asStateFlow()

    private var previousActiveSessionId: Long? = null

    init {
        refreshPermissions()
        loadInstalledApps()

        viewModelScope.launch {
            repository.ensureDefaultProfilesSeeded()
            ScheduleManager.refreshNextScheduleAlarm(getApplication())
        }

        // 1-second countdown ticker for active session & diagnostic polling
        viewModelScope.launch {
            while (true) {
                val current = activeSession.value
                val remaining = if (current != null) current.remainingMillis() else 0L
                _activeRemainingMillis.value = remaining

                val isLockActive = current != null && remaining > 0
                _diagnosticReport.value = SocialJailDiagnostics.evaluateDiagnosticStatus(
                    getApplication(),
                    isLockActive
                )

                if (current != null) {
                    previousActiveSessionId = current.id
                    if (remaining <= 0) {
                        handleSessionCompleted(current)
                    }
                }
                delay(1000L)
            }
        }
    }

    private suspend fun handleSessionCompleted(session: LockSession) {
        val attempts = repository.getAttemptsForSession(session.id)
        repository.completeExpiredSession(session.id)
        if (preferences.lastSeenCompletedSessionId != session.id) {
            preferences.lastSeenCompletedSessionId = session.id
            _completionSession.value = session
            // Attempt count will be collected
        }
    }

    fun dismissCompletionDialog() {
        _completionSession.value = null
    }

    fun refreshPermissions() {
        _permissionStatus.value = PermissionUtils.getPermissionStatus(getApplication())
        val isLockActive = (activeSession.value?.remainingMillis() ?: 0L) > 0
        _diagnosticReport.value = SocialJailDiagnostics.evaluateDiagnosticStatus(
            getApplication(),
            isLockActive
        )
    }

    fun loadInstalledApps() {
        if (_installedApps.value.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingApps.value = true
            try {
                val apps = PackageUtils.getInstalledApps(getApplication())
                _installedApps.value = apps
                // If selected packages are empty, initialize with any default profile
                if (_selectedPackages.value.isEmpty()) {
                    val defaultStudy = Profile.getDefaultPresets().firstOrNull()
                    if (defaultStudy != null) {
                        val matchingPackages = apps.map { it.packageName }.toSet()
                        val validPresets = defaultStudy.packageNames.filter { matchingPackages.contains(it) }.toSet()
                        if (validPresets.isNotEmpty()) {
                            _selectedPackages.value = validPresets
                            _selectedProfile.value = defaultStudy
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingApps.value = false
            }
        }
    }

    // App Selection
    fun toggleAppSelection(packageName: String) {
        val current = _selectedPackages.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _selectedPackages.value = current
        _selectedProfile.value = null // Cleared profile since selection was customized
    }

    fun selectAllApps(apps: List<InstalledAppInfo>) {
        val selectable = apps.filter { !it.isEssential }.map { it.packageName }.toSet()
        _selectedPackages.value = selectable
        _selectedProfile.value = null
    }

    fun deselectAllApps() {
        _selectedPackages.value = emptySet()
        _selectedProfile.value = null
    }

    fun applyProfile(profile: Profile) {
        _selectedProfile.value = profile
        _selectedPackages.value = profile.packageNames.toSet()
        _defaultDurationMinutes.value = profile.defaultDurationMinutes
    }

    // Start Session
    suspend fun startSession(
        packages: List<String>,
        durationMinutes: Int,
        goalText: String?,
        profileName: String?
    ): Result<Long> {
        val pm = getApplication<Application>().packageManager
        val appNames = packages.map { pkg ->
            try {
                val info = pm.getApplicationInfo(pkg, 0)
                pm.getApplicationLabel(info).toString()
            } catch (e: Exception) {
                pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            }
        }
        val durationMillis = durationMinutes * 60 * 1000L
        return repository.startImmediateLock(
            packages = packages,
            appNames = appNames,
            durationMillis = durationMillis,
            mode = LockMode.HARDCORE,
            goalText = goalText,
            profileName = profileName
        )
    }

    // Profiles Management
    fun saveProfile(profile: Profile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch {
            repository.deleteProfile(id)
            if (_selectedProfile.value?.id == id) {
                _selectedProfile.value = null
            }
        }
    }

    // Schedules Management
    fun saveSchedule(schedule: Schedule) {
        viewModelScope.launch {
            repository.saveSchedule(schedule)
            ScheduleManager.refreshNextScheduleAlarm(getApplication())
        }
    }

    fun toggleSchedule(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setScheduleEnabled(id, isEnabled)
            ScheduleManager.refreshNextScheduleAlarm(getApplication())
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            repository.deleteSchedule(id)
            ScheduleManager.refreshNextScheduleAlarm(getApplication())
        }
    }

    // Preferences & Settings
    fun setDefaultDuration(minutes: Int) {
        _defaultDurationMinutes.value = minutes
        preferences.defaultDurationMinutes = minutes
    }

    fun setGoalPromptEnabled(enabled: Boolean) {
        _isGoalPromptEnabled.value = enabled
        preferences.isGoalPromptEnabled = enabled
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        preferences.themeMode = mode
    }

    fun clearSessionHistory() {
        viewModelScope.launch {
            repository.clearSessionHistory()
        }
    }

    fun clearStatistics() {
        viewModelScope.launch {
            repository.clearAllStatistics()
        }
    }
}
