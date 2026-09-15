package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.data.repository.LockRepository
import com.example.domain.model.DeviceLockSession
import com.example.domain.model.DeviceLockStats
import com.example.domain.model.ExamPlan
import com.example.domain.model.GoalStatus
import com.example.domain.model.InstalledAppInfo
import com.example.domain.model.LockMode
import com.example.domain.model.LockSession
import com.example.domain.model.Profile
import com.example.domain.model.Schedule
import com.example.domain.model.SmartPreset
import com.example.domain.model.StatisticsData
import com.example.domain.model.TemptationAnalytics
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

    val activeDeviceLock: StateFlow<DeviceLockSession?> = repository.activeDeviceLockFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val completedDeviceLocks: StateFlow<List<DeviceLockSession>> = repository.completedDeviceLocksFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val deviceLockStats: StateFlow<DeviceLockStats> = repository.deviceLockStatsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DeviceLockStats()
        )

    val profiles: StateFlow<List<Profile>> = repository.profilesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val smartPresets: StateFlow<List<SmartPreset>> = repository.smartPresetsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SmartPreset.getDefaultPresets()
        )

    val examPlans: StateFlow<List<ExamPlan>> = repository.examPlansFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeExamPlan: StateFlow<ExamPlan?> = repository.activeExamPlanFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
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

    val temptationAnalytics: StateFlow<TemptationAnalytics> = repository.temptationAnalyticsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TemptationAnalytics()
        )

    private val _permissionStatus = MutableStateFlow(PermissionUtils.getPermissionStatus(application))
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _activeRemainingMillis = MutableStateFlow(0L)
    val activeRemainingMillis: StateFlow<Long> = _activeRemainingMillis.asStateFlow()

    private val _activeDeviceLockRemainingMillis = MutableStateFlow(0L)
    val activeDeviceLockRemainingMillis: StateFlow<Long> = _activeDeviceLockRemainingMillis.asStateFlow()

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

    private val _completionDeviceLockSession = MutableStateFlow<DeviceLockSession?>(null)
    val completionDeviceLockSession: StateFlow<DeviceLockSession?> = _completionDeviceLockSession.asStateFlow()

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
    private var previousActiveDeviceLockId: Long? = null

    init {
        refreshPermissions()
        loadInstalledApps()

        viewModelScope.launch {
            repository.ensureDefaultProfilesSeeded()
            ScheduleManager.refreshNextScheduleAlarm(getApplication())
        }

        // 1-second countdown ticker for active sessions & diagnostic polling
        viewModelScope.launch {
            while (true) {
                val currentApp = activeSession.value
                val remainingApp = if (currentApp != null) currentApp.remainingMillis() else 0L
                _activeRemainingMillis.value = remainingApp

                val currentDevice = activeDeviceLock.value
                val remainingDevice = if (currentDevice != null) currentDevice.remainingMillis() else 0L
                _activeDeviceLockRemainingMillis.value = remainingDevice

                val isLockActive = (currentApp != null && remainingApp > 0) || (currentDevice != null && remainingDevice > 0)
                _diagnosticReport.value = SocialJailDiagnostics.evaluateDiagnosticStatus(
                    getApplication(),
                    isLockActive
                )

                if (currentApp != null) {
                    previousActiveSessionId = currentApp.id
                    if (remainingApp <= 0) {
                        handleSessionCompleted(currentApp)
                    }
                }

                if (currentDevice != null) {
                    previousActiveDeviceLockId = currentDevice.id
                    if (remainingDevice <= 0) {
                        handleDeviceLockCompleted(currentDevice)
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
        }
    }

    private suspend fun handleDeviceLockCompleted(session: DeviceLockSession) {
        repository.completeExpiredDeviceLock(session.id)
        _completionDeviceLockSession.value = session
    }

    fun dismissCompletionDialog() {
        _completionSession.value = null
    }

    fun dismissDeviceLockCompletionDialog() {
        _completionDeviceLockSession.value = null
    }

    fun refreshPermissions() {
        _permissionStatus.value = PermissionUtils.getPermissionStatus(getApplication())
        val isLockActive = ((activeSession.value?.remainingMillis() ?: 0L) > 0) || ((activeDeviceLock.value?.remainingMillis() ?: 0L) > 0)
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

    fun applySmartPreset(preset: SmartPreset) {
        _defaultDurationMinutes.value = preset.durationMinutes
        if (preset.blockedPackageNames.isNotEmpty()) {
            _selectedPackages.value = preset.blockedPackageNames.toSet()
        }
        val matchingProfile = profiles.value.firstOrNull { it.name == preset.profileName }
        if (matchingProfile != null) {
            _selectedProfile.value = matchingProfile
            _selectedPackages.value = matchingProfile.packageNames.toSet()
        }
    }

    // Start Session
    suspend fun startSession(
        packages: List<String>,
        durationMinutes: Int,
        goalText: String?,
        profileName: String?,
        escalationEnabled: Boolean = false,
        escalationAttemptTrigger: Int = 3,
        escalationAction: String = "DEVICE_LOCK"
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
            profileName = profileName,
            escalationEnabled = escalationEnabled,
            escalationAttemptTrigger = escalationAttemptTrigger,
            escalationAction = escalationAction
        )
    }

    suspend fun startDeviceLock(
        durationMinutes: Int,
        goalText: String?
    ): Result<Long> {
        return repository.startImmediateDeviceLock(
            durationMinutes = durationMinutes,
            goalText = goalText
        )
    }

    fun submitSessionReview(sessionId: Long, goalStatus: GoalStatus?, note: String?) {
        viewModelScope.launch {
            repository.updateSessionReview(sessionId, goalStatus, note)
            dismissCompletionDialog()
        }
    }

    fun submitPostSessionReview(goalStatus: GoalStatus, note: String?) {
        val session = _completionSession.value ?: return
        submitSessionReview(session.id, goalStatus, note)
    }

    suspend fun startExamBlock(plan: ExamPlan): Result<Long> {
        val targetProfile = profiles.value.firstOrNull { it.name == plan.profileName }
            ?: profiles.value.firstOrNull()
        val packages = targetProfile?.packageNames ?: selectedPackages.value.toList()
        return startSession(
            packages = packages,
            durationMinutes = plan.targetDailyHours * 60,
            goalText = "Exam prep: ${plan.title}",
            profileName = plan.profileName,
            escalationEnabled = true,
            escalationAttemptTrigger = 2
        )
    }

    fun submitDeviceLockReview(sessionId: Long, goalStatus: GoalStatus?, note: String?) {
        viewModelScope.launch {
            repository.updateDeviceLockReview(sessionId, goalStatus, note)
            dismissDeviceLockCompletionDialog()
        }
    }

    fun clearCompletedDeviceLocks() {
        viewModelScope.launch {
            repository.clearCompletedDeviceLocks()
        }
    }

    // Smart Presets Management
    fun savePreset(preset: SmartPreset) {
        viewModelScope.launch {
            repository.savePreset(preset)
        }
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch {
            repository.deletePreset(id)
        }
    }

    // Exam Plans Management
    fun saveExamPlan(plan: ExamPlan) {
        viewModelScope.launch {
            repository.saveExamPlan(plan)
        }
    }

    fun deleteExamPlan(id: Long) {
        viewModelScope.launch {
            repository.deleteExamPlan(id)
        }
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
