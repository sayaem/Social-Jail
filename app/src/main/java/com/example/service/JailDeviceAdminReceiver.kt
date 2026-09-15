package com.example.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class JailDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "WARNING: Deactivating this will allow you to bypass your active Social Jail discipline sessions. Are you absolutely sure you want to break your commitment?"
    }
}
