package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("social_jail_prefs", Context.MODE_PRIVATE)

    var defaultDurationMinutes: Int
        get() = prefs.getInt(KEY_DEFAULT_DURATION, 120)
        set(value) = prefs.edit().putInt(KEY_DEFAULT_DURATION, value).apply()

    var isGoalPromptEnabled: Boolean
        get() = prefs.getBoolean(KEY_GOAL_PROMPT, true)
        set(value) = prefs.edit().putBoolean(KEY_GOAL_PROMPT, value).apply()

    var isNotificationEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    var themeMode: String // "DARK", "SYSTEM", "LIGHT"
        get() = prefs.getString(KEY_THEME_MODE, "DARK") ?: "DARK"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var selectedProfileId: Long
        get() = prefs.getLong(KEY_SELECTED_PROFILE_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_SELECTED_PROFILE_ID, value).apply()

    var lastSeenCompletedSessionId: Long
        get() = prefs.getLong(KEY_LAST_SEEN_COMPLETED_ID, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SEEN_COMPLETED_ID, value).apply()

    companion object {
        private const val KEY_DEFAULT_DURATION = "key_default_duration"
        private const val KEY_GOAL_PROMPT = "key_goal_prompt"
        private const val KEY_NOTIFICATIONS = "key_notifications"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_SELECTED_PROFILE_ID = "key_selected_profile_id"
        private const val KEY_LAST_SEEN_COMPLETED_ID = "key_last_seen_completed_id"
    }
}
