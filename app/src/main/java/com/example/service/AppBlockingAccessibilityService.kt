package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.data.local.AppDatabase
import com.example.domain.model.LockSession
import com.example.domain.model.SessionStatus
import com.example.ui.blocking.BlockingActivity
import com.example.util.PackageUtils
import com.example.util.PermissionUtils
import com.example.util.SocialJailDiagnostics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Hardcore enforcement mechanism for Social Jail.
 *
 * Rules:
 * 1. Uses AccessibilityService window events exclusively.
 * 2. Does NOT inspect screen contents (zero view hierarchy scraping).
 * 3. Never blocks critical emergency/phone/telecom/safety system services.
 * 4. Idempotent re-entry protection prevents activity-launch loops.
 * 5. Strictly enforces persisted session endTime with no early escape.
 * 6. Logs structured diagnostics for all 5 operational cases.
 */
class AppBlockingAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastBlockedLaunchTime: Long = 0L
    private var lastBlockedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        SocialJailDiagnostics.wasEverConnected = true
        Log.i(
            "SocialJail-Diagnostic",
            "[SERVICE_LIFECYCLE] onServiceConnected: AccessibilityService bound and ready."
        )

        // Restore active session state from SQLite Room DB upon service connection
        restoreSessionFromDatabase()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            processAccessibilityEvent(event)
        } catch (t: Throwable) {
            SocialJailDiagnostics.logServiceCrash("onAccessibilityEvent", t)
        }
    }

    private fun processAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Filter only window change events
        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }

        // 1. Obtain foreground package name
        val packageName = event.packageName?.toString() ?: return

        // 2. Ignore Social Jail's own package and BlockingActivity (prevents re-entry loop)
        if (packageName == applicationContext.packageName) {
            return
        }

        // 3. CRITICAL SAFETY RULE: Never block emergency functionality or essential phone/telecom
        if (PackageUtils.isCriticalSystemSafetyPackage(packageName, applicationContext.packageName)) {
            return
        }

        // 4. Determine whether a hardcore session is active
        val session = currentActiveSession
        val now = System.currentTimeMillis()

        if (session != null) {
            // Check for natural session expiration
            if (session.endTime <= now) {
                Log.i(
                    "SocialJail-Diagnostic",
                    "[SESSION_EXPIRED] Hardcore session #${session.id} endTime reached naturally. Clearing block state."
                )
                clearBlockedPackages()
                serviceScope.launch {
                    try {
                        val db = AppDatabase.getInstance(applicationContext)
                        db.lockSessionDao().updateSessionStatus(
                            id = session.id,
                            status = SessionStatus.COMPLETED.name,
                            completedAt = now
                        )
                    } catch (e: Exception) {
                        SocialJailDiagnostics.logServiceCrash("updateSessionStatus", e)
                    }
                }
                return
            }

            // 5. Determine whether the package is in the locked-package set
            if (activeBlockedPackages.contains(packageName)) {
                val uptimeNow = SystemClock.uptimeMillis()
                val isSamePackage = (packageName == lastBlockedPackage)
                val timeSinceLastLaunch = uptimeNow - lastBlockedLaunchTime

                // Immediate action: kick to Home screen to collapse the locked window
                performGlobalAction(GLOBAL_ACTION_HOME)

                // Idempotency: throttle BlockingActivity launches to prevent task floods (min 400ms debounce)
                if (!isSamePackage || timeSinceLastLaunch > 400L) {
                    lastBlockedPackage = packageName
                    lastBlockedLaunchTime = uptimeNow

                    // CASE 1: Log blocker decision
                    SocialJailDiagnostics.logAppBlocked(packageName, session.id, session.endTime - now)

                    // Record attempt locally & handle escalation
                    serviceScope.launch {
                        try {
                            val db = AppDatabase.getInstance(applicationContext)
                            val pm = applicationContext.packageManager
                            val resolvedName = try {
                                val info = pm.getApplicationInfo(packageName, 0)
                                pm.getApplicationLabel(info).toString()
                            } catch (e: Exception) {
                                packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
                            }
                            db.blockAttemptDao().recordAttempt(
                                com.example.data.local.entity.BlockAttemptEntity(
                                    sessionId = session.id,
                                    packageName = packageName,
                                    appName = resolvedName,
                                    timestamp = System.currentTimeMillis()
                                )
                            )

                            // Escalation Mode check
                            if (session.escalationEnabled && !session.escalationTriggered) {
                                val currentCount = db.blockAttemptDao().getSessionAttemptCountNow(session.id)
                                if (currentCount >= session.escalationAttemptTrigger) {
                                    val repo = com.example.data.repository.LockRepository(applicationContext)
                                    repo.triggerEscalation(session.id)
                                }
                            }
                        } catch (e: Exception) {
                            // Non-critical background telemetry
                        }
                    }

                    val intent = Intent(applicationContext, BlockingActivity::class.java).apply {
                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                        )
                        putExtra(BlockingActivity.EXTRA_PACKAGE_NAME, packageName)
                        putExtra(BlockingActivity.EXTRA_END_TIME, session.endTime)
                        putExtra(BlockingActivity.EXTRA_GOAL_TEXT, session.goalText)
                    }
                    startActivity(intent)
                }
            }
        }
        // 6. Do nothing for packages that are not locked
    }

    override fun onInterrupt() {
        Log.w("SocialJail-Diagnostic", "[SERVICE_LIFECYCLE] onInterrupt called by system.")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isServiceRunning = false
        val isSettingStillEnabled = PermissionUtils.isAccessibilityServiceEnabled(applicationContext)
        if (!isSettingStillEnabled) {
            // CASE 4: The user manually disabled Accessibility access in Android Settings
            SocialJailDiagnostics.logDisabledByUserInSettings()
        } else {
            // CASE 3: Android system disconnected/unbound the AccessibilityService
            SocialJailDiagnostics.logDisconnectedByAndroid(
                "onUnbind called while Settings permission is still enabled (OS reclaim or low memory)."
            )
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        Log.w("SocialJail-Diagnostic", "[SERVICE_LIFECYCLE] onDestroy called.")
    }

    private fun restoreSessionFromDatabase() {
        serviceScope.launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val active = db.lockSessionDao().getActiveSession()
                val now = System.currentTimeMillis()
                if (active != null) {
                    val session = LockSession.fromEntity(active)
                    if (session.remainingMillis(now) > 0) {
                        updateBlockedPackages(session.blockedPackageNames.toSet(), session)
                        Log.i(
                            "SocialJail-Diagnostic",
                            "[RESTORE_SUCCESS] Active Hardcore Session #${session.id} restored. ${session.remainingMillis(now) / 1000}s remaining. Guarding ${session.blockedPackageNames.size} packages."
                        )
                    } else {
                        db.lockSessionDao().updateSessionStatus(session.id, SessionStatus.COMPLETED.name, now)
                        clearBlockedPackages()
                    }
                }
            } catch (e: Exception) {
                SocialJailDiagnostics.logServiceCrash("restoreSessionFromDatabase", e)
            }
        }
    }

    companion object {
        @Volatile
        var isServiceRunning: Boolean = false
            private set

        @Volatile
        var activeBlockedPackages: Set<String> = emptySet()
            private set

        @Volatile
        var currentActiveSession: LockSession? = null
            private set

        fun updateBlockedPackages(packages: Set<String>, session: LockSession?) {
            activeBlockedPackages = packages
            currentActiveSession = session
        }

        fun clearBlockedPackages() {
            activeBlockedPackages = emptySet()
            currentActiveSession = null
        }
    }
}
