package com.example.util

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.service.JailDeviceAdminReceiver

object SocialJailPolicyManager {
    private const val TAG = "SocialJailPolicy"

    fun getAdminComponent(context: Context): ComponentName {
        return ComponentName(context, JailDeviceAdminReceiver::class.java)
    }

    fun isDeviceAdmin(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isAdminActive(getAdminComponent(context))
    }

    fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isDeviceOwnerApp(context.packageName)
    }

    fun applyUninstallProtection(context: Context, active: Boolean) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val component = getAdminComponent(context)
        
        if (dpm.isDeviceOwnerApp(context.packageName)) {
            try {
                dpm.setUninstallBlocked(component, context.packageName, active)
                Log.i(TAG, "Uninstall protection set to: $active")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to apply uninstall protection", e)
            }
        } else {
            Log.w(TAG, "Cannot apply uninstall protection: Application is not Device Owner")
        }
    }

    /**
     * Instantly puts screen to sleep and engages the secure keyguard.
     * Works with standard Device Admin activation.
     */
    fun lockNow(context: Context): Boolean {
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            if (isDeviceAdmin(context)) {
                dpm.lockNow()
                true
            } else {
                Log.w(TAG, "Cannot lockNow: Device admin not active")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute lockNow()", e)
            false
        }
    }

    /**
     * Configures and starts Lock Task Mode (Kiosk Lockdown) if Device Owner is present.
     */
    fun enableKioskLockdown(activity: Activity) {
        val context = activity.applicationContext
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val component = getAdminComponent(context)

        if (dpm.isDeviceOwnerApp(context.packageName)) {
            try {
                // Whitelist self for lock task
                dpm.setLockTaskPackages(component, arrayOf(context.packageName))
                
                // Disable status bar shade & notifications
                dpm.setStatusBarDisabled(component, true)

                // Start kiosk pinning
                activity.startLockTask()
                Log.i(TAG, "Kiosk Lock Task Mode enabled successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to enable Kiosk Lock Task Mode", e)
            }
        } else {
            // Standard Lock Task mode (user confirmation required by OS)
            try {
                activity.startLockTask()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start standard lock task", e)
            }
        }
    }

    /**
     * Releases Lock Task Mode and re-enables system status bar.
     */
    fun disableKioskLockdown(activity: Activity) {
        val context = activity.applicationContext
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val component = getAdminComponent(context)

        try {
            activity.stopLockTask()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop lock task", e)
        }

        if (dpm.isDeviceOwnerApp(context.packageName)) {
            try {
                dpm.setStatusBarDisabled(component, false)
                Log.i(TAG, "Status bar restored")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore status bar", e)
            }
        }
    }

    fun getAdbDeviceOwnerCommand(context: Context): String {
        return "adb shell dpm set-device-owner ${context.packageName}/${JailDeviceAdminReceiver::class.java.name}"
    }
}

