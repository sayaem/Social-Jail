package com.example.domain.model

data class DeviceLockStats(
    val totalSessionsCount: Int = 0,
    val completedSessionsCount: Int = 0,
    val totalProtectedMillis: Long = 0L,
    val longestSessionMinutes: Int = 0,
    val currentStreakDays: Int = 0
) {
    val totalProtectedHoursFormatted: String
        get() {
            val totalMinutes = totalProtectedMillis / 60000
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
        }
}
