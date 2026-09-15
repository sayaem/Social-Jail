package com.example.domain.model

data class TemptationAnalytics(
    val attemptsToday: Int = 0,
    val attemptsThisWeek: Int = 0,
    val totalAttemptsAllTime: Int = 0,
    val topAppsToday: List<AppAttemptStat> = emptyList(),
    val topAppsThisWeek: List<AppAttemptStat> = emptyList(),
    val topAppsAllTime: List<AppAttemptStat> = emptyList(),
    val heatmap: HeatmapData = HeatmapData(),
    val personalInsights: List<RiskInsight> = emptyList(),
    val streakInfo: StreakInfo = StreakInfo(),
    val weeklyReport: WeeklyReportData = WeeklyReportData()
)

data class AppAttemptStat(
    val packageName: String,
    val appName: String,
    val count: Int,
    val percentage: Float = 0f
)

data class HeatmapData(
    // 7 days (0 = Sun/Mon depending on locale, let's use 0=Mon, 1=Tue, ..., 6=Sun)
    val dayCounts: Map<Int, Int> = emptyMap(),
    // 24 hours (0..23)
    val hourCounts: Map<Int, Int> = emptyMap(),
    val peakRiskHourStart: Int? = null,
    val peakRiskHourEnd: Int? = null,
    val highestRiskDayName: String? = null,
    val hasEnoughData: Boolean = false
)

data class RiskInsight(
    val id: String,
    val title: String,
    val description: String,
    val iconType: String = "ALERT", // "ALERT", "SUCCESS", "TREND", "INFO"
    val severity: String = "MEDIUM" // "HIGH", "MEDIUM", "LOW"
)

data class StreakInfo(
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val totalProtectedHours: Int = 0,
    val sessionsCompletedCount: Int = 0
)

data class WeeklyReportData(
    val protectedMinutesThisWeek: Long = 0L,
    val completedSessionsThisWeek: Int = 0,
    val totalAttemptsThisWeek: Int = 0,
    val mostDangerousAppName: String? = null,
    val mostDangerousAppAttempts: Int = 0,
    val highestRiskPeriod: String? = null,
    val strongestDayName: String? = null,
    val hasData: Boolean = false
)
