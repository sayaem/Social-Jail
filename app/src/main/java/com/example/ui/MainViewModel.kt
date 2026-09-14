package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.LockRepository
import com.example.domain.model.InstalledAppInfo
import com.example.domain.model.LockMode
import com.example.domain.model.LockSession
import com.example.util.PackageUtils
import com.example.util.PermissionStatus
import com.example.util.PermissionUtils
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

    private val _permissionStatus = MutableStateFlow(PermissionUtils.getPermissionStatus(application))
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _activeRemainingMillis = MutableStateFlow(0L)
    val activeRemainingMillis: StateFlow<Long> = _activeRemainingMillis.asStateFlow()

    init {
        refreshPermissions()
        loadInstalledApps()

        // 1-second countdown ticker for active session
        viewModelScope.launch {
            while (true) {
                val current = activeSession.value
                if (current != null) {
                    val remaining = current.remainingMillis()
                    _activeRemainingMillis.value = remaining
                    if (remaining <= 0) {
                        repository.completeExpiredSession(current.id)
                    }
                } else {
                    _activeRemainingMillis.value = 0L
                }
                delay(1000L)
            }
        }
    }

    fun refreshPermissions() {
        _permissionStatus.value = PermissionUtils.getPermissionStatus(getApplication())
    }

    fun loadInstalledApps() {
        if (_installedApps.value.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingApps.value = true
            try {
                val apps = PackageUtils.getInstalledApps(getApplication())
                _installedApps.value = apps
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingApps.value = false
            }
        }
    }

    suspend fun startImmediateLock(
        packages: List<String>,
        appNames: List<String>,
        durationMillis: Long,
        mode: LockMode = LockMode.HARDCORE
    ): Result<Long> {
        return repository.startImmediateLock(packages, appNames, durationMillis, mode)
    }

    suspend fun scheduleFutureLock(
        packages: List<String>,
        appNames: List<String>,
        startTime: Long,
        endTime: Long,
        mode: LockMode = LockMode.HARDCORE
    ): Result<Long> {
        return repository.scheduleFutureLock(packages, appNames, startTime, endTime, mode)
    }

    fun cancelScheduledSession(sessionId: Long) {
        viewModelScope.launch {
            repository.cancelScheduledSession(sessionId)
        }
    }
}
