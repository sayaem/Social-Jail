package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.example.data.local.AppDatabase
import com.example.domain.model.LockSession
import com.example.domain.model.SessionStatus
import com.example.ui.blocking.BlockingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppBlockingAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastBlockedLaunchTime: Long = 0L
    private var lastBlockedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        // Restore active session state from DB on service connect
        serviceScope.launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val active = db.lockSessionDao().getActiveSession()
                if (active != null) {
                    val session = LockSession.fromEntity(active)
                    if (session.remainingMillis() > 0) {
                        updateBlockedPackages(session.blockedPackageNames.toSet(), session)
                    } else {
                        db.lockSessionDao().updateSessionStatus(session.id, SessionStatus.COMPLETED.name, System.currentTimeMillis())
                        clearBlockedPackages()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return

        // Skip our own application package
        if (packageName == applicationContext.packageName) return

        if (activeBlockedPackages.contains(packageName)) {
            val now = SystemClock.uptimeMillis()
            // 600ms debounce per package to prevent intent loops
            if (packageName == lastBlockedPackage && (now - lastBlockedLaunchTime) < 600L) {
                return
            }
            lastBlockedPackage = packageName
            lastBlockedLaunchTime = now

            // Immediately kick back to Home screen
            performGlobalAction(GLOBAL_ACTION_HOME)

            // Launch blocking UI
            val intent = Intent(applicationContext, BlockingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(BlockingActivity.EXTRA_PACKAGE_NAME, packageName)
                putExtra(BlockingActivity.EXTRA_END_TIME, currentActiveSession?.endTime ?: 0L)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
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
