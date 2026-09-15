package com.example.util

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
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
}
