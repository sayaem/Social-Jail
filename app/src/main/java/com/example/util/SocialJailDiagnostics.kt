package com.example.util

import android.content.Context
import android.util.Log
import com.example.service.AppBlockingAccessibilityService

/**
 * High-clarity diagnostics engine for Social Jail.
 * Distinguishes the 5 critical diagnostic situations:
 * 1. Our blocker deciding that an app is locked
 * 2. Our AccessibilityService crashing
 * 3. Android disconnecting/disabling the AccessibilityService
 * 4. The user manually disabling Accessibility access
 * 5. Play Protect / security software / battery saver interfering
 */
object SocialJailDiagnostics {
    private const val TAG = "SocialJail-Diagnostic"

    enum class DiagnosticCase {
        APP_BLOCKED_DECISION,
        SERVICE_CRASH_EXCEPTION,
        DISCONNECTED_BY_ANDROID_OS,
        DISABLED_BY_USER_IN_SETTINGS,
        SECURITY_OR_BATTERY_RESTRICTION,
        NORMAL_ENFORCEMENT_ACTIVE,
        IDLE_NO_LOCK
    }

    data class DiagnosticReport(
        val diagnosticCase: DiagnosticCase,
        val headline: String,
        val description: String,
        val isServiceRunning: Boolean,
        val isSettingsEnabled: Boolean,
        val isBatteryOptimizationIgnored: Boolean
    )

    @Volatile
    var lastDisconnectReason: String? = null
        private set

    @Volatile
    var wasEverConnected: Boolean = false
        internal set

    /**
     * CASE 1: Our blocker deciding that an app is locked.
     */
    fun logAppBlocked(packageName: String, sessionId: Long, remainingMillis: Long) {
        val remainingSec = (remainingMillis / 1000).coerceAtLeast(0)
        Log.i(
            TAG,
            "[DIAGNOSTIC-CASE-1: APP_BLOCKED] Target app '$packageName' intercepted by Hardcore Lock (Session #$sessionId). Time remaining: ${remainingSec}s. Invoking HOME action and presenting BlockingActivity."
        )
    }

    /**
     * CASE 2: Our AccessibilityService crashing or throwing an unexpected exception.
     */
    fun logServiceCrash(contextName: String, throwable: Throwable) {
        Log.e(
            TAG,
            "[DIAGNOSTIC-CASE-2: SERVICE_CRASH] AccessibilityService encountered an unexpected error at $contextName: ${throwable.message}",
            throwable
        )
    }

    /**
     * CASE 3: Android disconnecting/disabling the AccessibilityService (unbind from OS).
     */
    fun logDisconnectedByAndroid(reason: String) {
        lastDisconnectReason = reason
        Log.w(
            TAG,
            "[DIAGNOSTIC-CASE-3: DISCONNECTED_BY_ANDROID] AccessibilityService was unbound or stopped by the Android OS framework. Reason: $reason"
        )
    }

    /**
     * CASE 4: The user manually disabling Accessibility access in Android System Settings.
     */
    fun logDisabledByUserInSettings() {
        lastDisconnectReason = "Disabled by user in Android System Settings"
        Log.w(
            TAG,
            "[DIAGNOSTIC-CASE-4: DISABLED_BY_USER] Social Jail Accessibility Service was manually turned OFF in Android System Settings by the user."
        )
    }

    /**
     * CASE 5: Play Protect / security software / battery saver interfering.
     */
    fun logSecurityOrBatteryInterference(details: String) {
        Log.w(
            TAG,
            "[DIAGNOSTIC-CASE-5: SECURITY_OR_BATTERY_RESTRICTION] System interference or background restriction detected: $details"
        )
    }

    /**
     * Computes the current real-time diagnostic status for the UI.
     */
    fun evaluateDiagnosticStatus(context: Context, isLockActive: Boolean): DiagnosticReport {
        val isServiceRunning = AppBlockingAccessibilityService.isServiceRunning
        val isSettingsEnabled = PermissionUtils.isAccessibilityServiceEnabled(context)
        val isBatteryOptIgnored = PermissionUtils.isIgnoringBatteryOptimizations(context)

        return when {
            isLockActive && !isSettingsEnabled -> {
                logDisabledByUserInSettings()
                DiagnosticReport(
                    diagnosticCase = DiagnosticCase.DISABLED_BY_USER_IN_SETTINGS,
                    headline = "Accessibility Permission Revoked",
                    description = "Social Jail's Accessibility permission was toggled OFF in Android System Settings. Re-enable it to resume hardcore lock enforcement.",
                    isServiceRunning = false,
                    isSettingsEnabled = false,
                    isBatteryOptimizationIgnored = isBatteryOptIgnored
                )
            }
            isLockActive && isSettingsEnabled && !isServiceRunning -> {
                logDisconnectedByAndroid("Accessibility is enabled in Settings, but the service binding was terminated by Android OS (e.g. low memory or process reclaim).")
                DiagnosticReport(
                    diagnosticCase = DiagnosticCase.DISCONNECTED_BY_ANDROID_OS,
                    headline = "Service Unbound by Android OS",
                    description = "Accessibility is enabled in Settings, but the Android framework disconnected the service process (system memory pressure or battery saving).",
                    isServiceRunning = false,
                    isSettingsEnabled = true,
                    isBatteryOptimizationIgnored = isBatteryOptIgnored
                )
            }
            isLockActive && !isBatteryOptIgnored -> {
                logSecurityOrBatteryInterference("App is subject to OS battery optimization, which may throttle background enforcement.")
                DiagnosticReport(
                    diagnosticCase = DiagnosticCase.SECURITY_OR_BATTERY_RESTRICTION,
                    headline = "Battery Optimization Active",
                    description = "Android battery saver is enabled for Social Jail, which may delay window event delivery.",
                    isServiceRunning = isServiceRunning,
                    isSettingsEnabled = isSettingsEnabled,
                    isBatteryOptimizationIgnored = false
                )
            }
            isLockActive -> {
                DiagnosticReport(
                    diagnosticCase = DiagnosticCase.NORMAL_ENFORCEMENT_ACTIVE,
                    headline = "Enforcement Active & Guarded",
                    description = "Accessibility Service is active, legitimate, and actively enforcing zero-escape boundaries.",
                    isServiceRunning = true,
                    isSettingsEnabled = true,
                    isBatteryOptimizationIgnored = isBatteryOptIgnored
                )
            }
            else -> {
                DiagnosticReport(
                    diagnosticCase = DiagnosticCase.IDLE_NO_LOCK,
                    headline = "Standby",
                    description = "No active hardcore lock session.",
                    isServiceRunning = isServiceRunning,
                    isSettingsEnabled = isSettingsEnabled,
                    isBatteryOptimizationIgnored = isBatteryOptIgnored
                )
            }
        }
    }
}
