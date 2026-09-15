package com.example.domain.model

data class Milestone(
    val id: String,
    val title: String,
    val requirementText: String,
    val isUnlocked: Boolean,
    val icon: String = "🏅"
)

data class StatisticsData(
    val totalProtectedMillis: Long = 0L,
    val thisWeekProtectedMillis: Long = 0L,
    val completedSessionsCount: Int = 0,
    val totalBlockAttempts: Int = 0,
    val currentStreakDays: Int = 0,
    val mostAttemptedAppName: String? = null,
    val mostAttemptedCount: Int = 0,
    val averageSessionDurationMillis: Long = 0L,
    val milestones: List<Milestone> = emptyList()
) {
    companion object {
        fun formatDurationHoursMinutes(millis: Long): String {
            val totalMinutes = millis / 60000
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            return if (hours > 0) {
                "${hours}h ${mins}m"
            } else {
                "${mins}m"
            }
        }
    }
}
