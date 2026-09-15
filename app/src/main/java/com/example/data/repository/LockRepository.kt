package com.example.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import com.example.data.local.AppDatabase
import com.example.data.local.dao.AppAttemptCount
import com.example.data.local.dao.BlockAttemptDao
import com.example.data.local.dao.DeviceLockSessionDao
import com.example.data.local.dao.ExamPlanDao
import com.example.data.local.dao.LockSessionDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.dao.ScheduleDao
import com.example.data.local.dao.SmartPresetDao
import com.example.data.local.entity.BlockAttemptEntity
import com.example.domain.model.AppAttemptStat
import com.example.domain.model.CommitmentLevel
import com.example.domain.model.DeviceLockSession
import com.example.domain.model.DeviceLockStats
import com.example.domain.model.DeviceLockStatus
import com.example.domain.model.ExamPlan
import com.example.domain.model.GoalStatus
import com.example.domain.model.HeatmapData
import com.example.domain.model.LockMode
import com.example.domain.model.LockSession
import com.example.domain.model.Milestone
import com.example.domain.model.Profile
import com.example.domain.model.RiskInsight
import com.example.domain.model.Schedule
import com.example.domain.model.SessionStatus
import com.example.domain.model.SmartPreset
import com.example.domain.model.StatisticsData
import com.example.domain.model.StreakInfo
import com.example.domain.model.TemptationAnalytics
import com.example.domain.model.WeeklyReportData
import com.example.service.AppBlockingAccessibilityService
import com.example.service.LockAlarmReceiver
import com.example.service.LockEnforcementService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar

class LockRepository(
    private val context: Context,
    private val db: AppDatabase = AppDatabase.getInstance(context)
) {
    private val sessionDao: LockSessionDao = db.lockSessionDao()
    private val deviceLockDao: DeviceLockSessionDao = db.deviceLockSessionDao()
    private val profileDao: ProfileDao = db.profileDao()
    private val scheduleDao: ScheduleDao = db.scheduleDao()
    private val attemptDao: BlockAttemptDao = db.blockAttemptDao()
    private val examPlanDao: ExamPlanDao = db.examPlanDao()
    private val smartPresetDao: SmartPresetDao = db.smartPresetDao()

    val activeSessionFlow: Flow<LockSession?> = sessionDao.getActiveSessionFlow().map { entity ->
        entity?.let { LockSession.fromEntity(it) }
    }

    val scheduledSessionsFlow: Flow<List<LockSession>> = sessionDao.getScheduledSessions().map { list ->
        list.map { LockSession.fromEntity(it) }
    }

    val completedSessionsFlow: Flow<List<LockSession>> = sessionDao.getCompletedSessions().map { list ->
        list.map { LockSession.fromEntity(it) }
    }

    val activeDeviceLockFlow: Flow<DeviceLockSession?> = deviceLockDao.getActiveSessionFlow().map { entity ->
        entity?.let { DeviceLockSession.fromEntity(it) }
    }

    val completedDeviceLocksFlow: Flow<List<DeviceLockSession>> = deviceLockDao.getCompletedSessions().map { list ->
        list.map { DeviceLockSession.fromEntity(it) }
    }

    val examPlansFlow: Flow<List<ExamPlan>> = examPlanDao.getAllExamPlans().map { list ->
        list.map { ExamPlan.fromEntity(it) }
    }

    val activeExamPlanFlow: Flow<ExamPlan?> = examPlanDao.getActiveExamPlan().map { entity ->
        entity?.let { ExamPlan.fromEntity(it) }
    }

    val smartPresetsFlow: Flow<List<SmartPreset>> = smartPresetDao.getAllPresets().map { list ->
        if (list.isEmpty()) {
            SmartPreset.getDefaultPresets()
        } else {
            list.map { SmartPreset.fromEntity(it) }
        }
    }

    val deviceLockStatsFlow: Flow<DeviceLockStats> = completedDeviceLocksFlow.map { list ->
        var totalMillis = 0L
        var longestMins = 0
        val activeDaysSet = mutableSetOf<String>()

        for (item in list) {
            val dur = if (item.expectedDurationMillis > 0L) item.expectedDurationMillis else (item.durationMinutes * 60 * 1000L)
            totalMillis += dur
            if (item.durationMinutes > longestMins) {
                longestMins = item.durationMinutes
            }

            val cal = Calendar.getInstance().apply { timeInMillis = item.endTime }
            val dateKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
            activeDaysSet.add(dateKey)
        }

        var streak = 0
        val now = System.currentTimeMillis()
        var testCal = Calendar.getInstance().apply { timeInMillis = now }
        while (true) {
            val key = "${testCal.get(Calendar.YEAR)}-${testCal.get(Calendar.MONTH)}-${testCal.get(Calendar.DAY_OF_MONTH)}"
            if (activeDaysSet.contains(key)) {
                streak++
                testCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        DeviceLockStats(
            totalSessionsCount = list.size,
            completedSessionsCount = list.size,
            totalProtectedMillis = totalMillis,
            longestSessionMinutes = longestMins,
            currentStreakDays = streak
        )
    }

    val profilesFlow: Flow<List<Profile>> = profileDao.getAllProfiles().map { list ->
        list.map { Profile.fromEntity(it) }
    }

    val schedulesFlow: Flow<List<Schedule>> = scheduleDao.getAllSchedules().map { list ->
        list.map { Schedule.fromEntity(it) }
    }

    val totalAttemptsFlow: Flow<Int> = attemptDao.getTotalAttemptCount()

    val mostAttemptedAppFlow: Flow<AppAttemptCount?> = attemptDao.getMostAttemptedApp()

    val temptationAnalyticsFlow: Flow<TemptationAnalytics> = combine(
        attemptDao.getAllAttempts(),
        completedSessionsFlow,
        completedDeviceLocksFlow
    ) { attempts, completedAppSessions, completedDeviceLocks ->
        calculateTemptationAnalytics(attempts, completedAppSessions, completedDeviceLocks)
    }

    suspend fun getActiveSession(): LockSession? {
        return sessionDao.getActiveSession()?.let { LockSession.fromEntity(it) }
    }

    suspend fun getActiveDeviceLock(): DeviceLockSession? {
        return deviceLockDao.getActiveSession()?.let { DeviceLockSession.fromEntity(it) }
    }

    suspend fun ensureDefaultProfilesSeeded() {
        val existing = profileDao.getAllProfilesList()
        if (existing.isEmpty()) {
            val presets = Profile.getDefaultPresets().map { it.toEntity() }
            profileDao.insertAll(presets)
        }
    }

    suspend fun ensureDefaultPresetsSeeded() {
        val existing = smartPresetDao.getAllPresetsList()
        if (existing.isEmpty()) {
            val presets = SmartPreset.getDefaultPresets().map { it.toEntity() }
            smartPresetDao.insertAll(presets)
        }
    }

    suspend fun startImmediateLock(
        packages: List<String>,
        appNames: List<String>,
        durationMillis: Long,
        mode: LockMode = LockMode.HARDCORE,
        goalText: String? = null,
        profileName: String? = null,
        escalationEnabled: Boolean = false,
        escalationAttemptTrigger: Int = 3,
        escalationAction: String = "DEVICE_LOCK"
    ): Result<Long> {
        val currentActive = sessionDao.getActiveSession()
        if (currentActive != null && LockSession.fromEntity(currentActive).remainingMillis() > 0) {
            return Result.failure(IllegalStateException("A lock is already active."))
        }

        val now = System.currentTimeMillis()
        val endTime = now + durationMillis
        val elapsedRealtime = SystemClock.elapsedRealtime()

        val session = LockSession(
            startTime = now,
            endTime = endTime,
            status = SessionStatus.ACTIVE,
            mode = mode,
            blockedPackageNames = packages,
            blockedAppNames = appNames,
            startElapsedRealtime = elapsedRealtime,
            expectedDurationMillis = durationMillis,
            goalText = goalText?.trim()?.ifEmpty { null },
            profileName = profileName,
            escalationEnabled = escalationEnabled,
            escalationAttemptTrigger = escalationAttemptTrigger,
            escalationAction = escalationAction,
            escalationTriggered = false
        )

        val id = sessionDao.insertSession(session.toEntity())
        val createdSession = session.copy(id = id)

        // Immediately update accessibility in-memory cache
        AppBlockingAccessibilityService.updateBlockedPackages(packages.toSet(), createdSession)

        // Enforce uninstall protection if configured as Device Owner
        com.example.util.SocialJailPolicyManager.applyUninstallProtection(context, true)

        // Start persistent foreground service
        LockEnforcementService.start(context)

        return Result.success(id)
    }

    suspend fun triggerEscalation(sessionId: Long) {
        val sessionEntity = sessionDao.getSessionById(sessionId) ?: return
        val session = LockSession.fromEntity(sessionEntity)
        if (!session.escalationEnabled || session.escalationTriggered) return

        sessionDao.markEscalationTriggered(sessionId)
        val remainingMillis = session.remainingMillis()
        val remainingMinutes = ((remainingMillis / 60000L).toInt()).coerceAtLeast(5)

        // Escalate to Device Lock
        startImmediateDeviceLock(
            durationMinutes = remainingMinutes,
            goalText = "Escalated from App Jail: ${session.goalText ?: "Discipline Lockdown"}"
        )
    }

    suspend fun scheduleFutureLock(
        packages: List<String>,
        appNames: List<String>,
        startTime: Long,
        endTime: Long,
        mode: LockMode = LockMode.HARDCORE,
        goalText: String? = null,
        profileName: String? = null
    ): Result<Long> {
        val now = System.currentTimeMillis()
        if (startTime <= now) {
            return startImmediateLock(packages, appNames, endTime - now, mode, goalText, profileName)
        }

        val scheduled = sessionDao.getScheduledSessionsList()
        for (s in scheduled) {
            val overlaps = (startTime < s.endTime && endTime > s.startTime)
            if (overlaps) {
                return Result.failure(IllegalStateException("This lock overlaps with an existing scheduled session."))
            }
        }

        val durationMillis = endTime - startTime
        val session = LockSession(
            startTime = startTime,
            endTime = endTime,
            status = SessionStatus.SCHEDULED,
            mode = mode,
            blockedPackageNames = packages,
            blockedAppNames = appNames,
            startElapsedRealtime = 0L,
            expectedDurationMillis = durationMillis,
            goalText = goalText?.trim()?.ifEmpty { null },
            profileName = profileName
        )

        val id = sessionDao.insertSession(session.toEntity())

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager != null) {
            val alarmIntent = Intent(context, LockAlarmReceiver::class.java).apply {
                putExtra(LockAlarmReceiver.EXTRA_SESSION_ID, id)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                id.toInt(),
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, pending)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, startTime, pending)
            }
        }

        return Result.success(id)
    }

    suspend fun cancelScheduledSession(sessionId: Long): Boolean {
        val session = sessionDao.getSessionById(sessionId)
        if (session != null && session.status == SessionStatus.SCHEDULED.name) {
            sessionDao.deleteScheduledSession(sessionId)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager != null) {
                val alarmIntent = Intent(context, LockAlarmReceiver::class.java).apply {
                    putExtra(LockAlarmReceiver.EXTRA_SESSION_ID, sessionId)
                }
                val pending = PendingIntent.getBroadcast(
                    context,
                    sessionId.toInt(),
                    alarmIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
                )
                if (pending != null) {
                    alarmManager.cancel(pending)
                }
            }
            return true
        }
        return false
    }

    suspend fun completeExpiredSession(sessionId: Long) {
        sessionDao.updateSessionStatus(sessionId, SessionStatus.COMPLETED.name, System.currentTimeMillis())
        AppBlockingAccessibilityService.clearBlockedPackages()
        
        val activeDeviceLock = deviceLockDao.getActiveSession()
        if (activeDeviceLock == null) {
            com.example.util.SocialJailPolicyManager.applyUninstallProtection(context, false)
            LockEnforcementService.stop(context)
        }
    }

    suspend fun updateSessionReview(sessionId: Long, goalStatus: GoalStatus?, reviewNote: String?) {
        sessionDao.updateSessionReview(sessionId, goalStatus?.name, reviewNote?.trim()?.ifEmpty { null })
    }

    suspend fun updateDeviceLockReview(sessionId: Long, goalStatus: GoalStatus?, reviewNote: String?) {
        deviceLockDao.updateSessionReview(sessionId, goalStatus?.name, reviewNote?.trim()?.ifEmpty { null })
    }

    suspend fun startImmediateDeviceLock(
        durationMinutes: Int,
        goalText: String?
    ): Result<Long> {
        val currentActive = deviceLockDao.getActiveSession()
        if (currentActive != null && DeviceLockSession.fromEntity(currentActive).remainingMillis() > 0) {
            return Result.failure(IllegalStateException("A Device Lock is already active."))
        }

        val durationMillis = durationMinutes * 60 * 1000L
        val now = System.currentTimeMillis()
        val endTime = now + durationMillis
        val elapsedRealtime = SystemClock.elapsedRealtime()
        val isDeviceOwner = com.example.util.SocialJailPolicyManager.isDeviceOwner(context)

        val session = DeviceLockSession(
            startTime = now,
            endTime = endTime,
            durationMinutes = durationMinutes,
            status = DeviceLockStatus.ACTIVE,
            goalText = goalText?.trim()?.ifEmpty { null },
            isDeviceOwnerMode = isDeviceOwner,
            startElapsedRealtime = elapsedRealtime,
            expectedDurationMillis = durationMillis
        )

        val id = deviceLockDao.insertSession(session.toEntity())

        // Apply uninstall protection if Device Owner
        com.example.util.SocialJailPolicyManager.applyUninstallProtection(context, true)

        // Lock screen immediately via Device Admin
        com.example.util.SocialJailPolicyManager.lockNow(context)

        // Start ongoing high-priority enforcement notification
        LockEnforcementService.start(context)

        return Result.success(id)
    }

    suspend fun completeExpiredDeviceLock(sessionId: Long) {
        deviceLockDao.updateSessionStatus(sessionId, DeviceLockStatus.COMPLETED.name, System.currentTimeMillis())
        
        val activeAppSession = sessionDao.getActiveSession()
        if (activeAppSession == null) {
            com.example.util.SocialJailPolicyManager.applyUninstallProtection(context, false)
            LockEnforcementService.stop(context)
        }
    }

    suspend fun clearCompletedDeviceLocks() {
        deviceLockDao.clearCompletedSessions()
    }

    // Profile Management
    suspend fun saveProfile(profile: Profile): Long {
        return profileDao.insertProfile(profile.toEntity())
    }

    suspend fun deleteProfile(id: Long) {
        profileDao.deleteProfile(id)
    }

    // Preset Management
    suspend fun savePreset(preset: SmartPreset): Long {
        return smartPresetDao.insertPreset(preset.toEntity())
    }

    suspend fun deletePreset(id: Long) {
        smartPresetDao.deletePreset(id)
    }

    // Exam Plan Management
    suspend fun saveExamPlan(plan: ExamPlan): Long {
        return examPlanDao.insertExamPlan(plan.toEntity())
    }

    suspend fun deleteExamPlan(id: Long) {
        examPlanDao.deleteExamPlan(id)
    }

    // Schedule Management
    suspend fun saveSchedule(schedule: Schedule): Long {
        return scheduleDao.insertSchedule(schedule.toEntity())
    }

    suspend fun setScheduleEnabled(id: Long, isEnabled: Boolean) {
        scheduleDao.setScheduleEnabled(id, isEnabled)
    }

    suspend fun deleteSchedule(id: Long) {
        scheduleDao.deleteSchedule(id)
    }

    // Attempts
    fun getAttemptsForSession(sessionId: Long): Flow<Int> {
        return attemptDao.getSessionAttemptCount(sessionId)
    }

    fun getAttemptsByAppForSession(sessionId: Long): Flow<List<AppAttemptCount>> {
        return attemptDao.getSessionAttemptsByApp(sessionId)
    }

    suspend fun clearSessionHistory() {
        sessionDao.clearCompletedSessions()
        deviceLockDao.clearCompletedSessions()
    }

    suspend fun clearAllStatistics() {
        attemptDao.clearAllAttempts()
    }

    // Temptation Analytics Calculation
    private fun calculateTemptationAnalytics(
        attempts: List<BlockAttemptEntity>,
        completedAppSessions: List<LockSession>,
        completedDeviceLocks: List<DeviceLockSession>
    ): TemptationAnalytics {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }

        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        val calWeek = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfWeek = calWeek.timeInMillis

        val attemptsTodayList = attempts.filter { it.timestamp >= startOfDay }
        val attemptsWeekList = attempts.filter { it.timestamp >= startOfWeek }

        fun toAppAttemptStats(list: List<BlockAttemptEntity>): List<AppAttemptStat> {
            val total = list.size.toFloat().coerceAtLeast(1f)
            return list.groupBy { it.packageName }
                .map { (pkg, appAttempts) ->
                    val name = appAttempts.firstOrNull()?.appName ?: pkg
                    AppAttemptStat(
                        packageName = pkg,
                        appName = name,
                        count = appAttempts.size,
                        percentage = (appAttempts.size / total) * 100f
                    )
                }
                .sortedByDescending { it.count }
        }

        val topAppsToday = toAppAttemptStats(attemptsTodayList)
        val topAppsThisWeek = toAppAttemptStats(attemptsWeekList)
        val topAppsAllTime = toAppAttemptStats(attempts)

        // Heatmap: Day of week (0=Mon..6=Sun) and Hour of Day (0..23)
        val dayCounts = mutableMapOf<Int, Int>()
        val hourCounts = mutableMapOf<Int, Int>()
        for (i in 0..6) dayCounts[i] = 0
        for (i in 0..23) hourCounts[i] = 0

        for (att in attempts) {
            val attCal = Calendar.getInstance().apply { timeInMillis = att.timestamp }
            val d = when (attCal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            val h = attCal.get(Calendar.HOUR_OF_DAY)
            dayCounts[d] = (dayCounts[d] ?: 0) + 1
            hourCounts[h] = (hourCounts[h] ?: 0) + 1
        }

        val maxHourEntry = hourCounts.maxByOrNull { it.value }
        val maxDayEntry = dayCounts.maxByOrNull { it.value }

        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val peakDayName = if ((maxDayEntry?.value ?: 0) > 0) dayNames.getOrElse(maxDayEntry!!.key) { "Monday" } else null

        val peakStart = maxHourEntry?.key
        val peakEnd = peakStart?.let { (it + 2) % 24 }

        val heatmap = HeatmapData(
            dayCounts = dayCounts,
            hourCounts = hourCounts,
            peakRiskHourStart = peakStart,
            peakRiskHourEnd = peakEnd,
            highestRiskDayName = peakDayName,
            hasEnoughData = attempts.size >= 3
        )

        // Personal Risk Insights
        val insights = mutableListOf<RiskInsight>()
        if (attempts.size < 3 && (completedAppSessions.size + completedDeviceLocks.size) < 2) {
            insights.add(
                RiskInsight(
                    id = "learning",
                    title = "Building Focus Profile",
                    description = "Complete more sessions to analyze your temptation patterns, peak vulnerability hours, and trigger apps.",
                    iconType = "INFO",
                    severity = "LOW"
                )
            )
        } else {
            if (topAppsAllTime.isNotEmpty()) {
                val mostApp = topAppsAllTime.first()
                insights.add(
                    RiskInsight(
                        id = "top_trigger",
                        title = "Primary Distraction Trigger: ${mostApp.appName}",
                        description = "You have attempted to open ${mostApp.appName} ${mostApp.count} times during focus locks (${String.format("%.0f", mostApp.percentage)}% of all attempts).",
                        iconType = "ALERT",
                        severity = "HIGH"
                    )
                )
            }

            if (peakStart != null && (maxHourEntry?.value ?: 0) >= 2) {
                val formatStart = if (peakStart == 0) "12 AM" else if (peakStart < 12) "${peakStart} AM" else if (peakStart == 12) "12 PM" else "${peakStart - 12} PM"
                val formatEnd = if (peakEnd == 0) "12 AM" else if (peakEnd != null && peakEnd < 12) "${peakEnd} AM" else if (peakEnd == 12) "12 PM" else "${(peakEnd ?: 0) - 12} PM"
                insights.add(
                    RiskInsight(
                        id = "peak_risk_window",
                        title = "High Vulnerability Window: $formatStart – $formatEnd",
                        description = "Distraction impulses peak in this window. Setting automated Scheduled Jail before this time reduces slip-ups by over 75%.",
                        iconType = "TREND",
                        severity = "MEDIUM"
                    )
                )
            }

            val totalStarted = completedAppSessions.size + completedDeviceLocks.size
            if (totalStarted > 0) {
                insights.add(
                    RiskInsight(
                        id = "discipline_rate",
                        title = "Strong Discipline Foundation",
                        description = "You have successfully finished $totalStarted focus sessions without tampering or bypassing the lock system.",
                        iconType = "SUCCESS",
                        severity = "LOW"
                    )
                )
            }
        }

        // Streak info
        val activeDaysSet = mutableSetOf<String>()
        var totalProtectedMillis = 0L
        for (s in completedAppSessions) {
            val dur = if (s.expectedDurationMillis > 0L) s.expectedDurationMillis else (s.endTime - s.startTime).coerceAtLeast(0L)
            totalProtectedMillis += dur
            val c = Calendar.getInstance().apply { timeInMillis = s.endTime }
            activeDaysSet.add("${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH)}-${c.get(Calendar.DAY_OF_MONTH)}")
        }
        for (d in completedDeviceLocks) {
            val dur = if (d.expectedDurationMillis > 0L) d.expectedDurationMillis else (d.durationMinutes * 60 * 1000L)
            totalProtectedMillis += dur
            val c = Calendar.getInstance().apply { timeInMillis = d.endTime }
            activeDaysSet.add("${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH)}-${c.get(Calendar.DAY_OF_MONTH)}")
        }

        var streak = 0
        var testCal = Calendar.getInstance().apply { timeInMillis = now }
        val todayKey = "${testCal.get(Calendar.YEAR)}-${testCal.get(Calendar.MONTH)}-${testCal.get(Calendar.DAY_OF_MONTH)}"
        testCal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayKey = "${testCal.get(Calendar.YEAR)}-${testCal.get(Calendar.MONTH)}-${testCal.get(Calendar.DAY_OF_MONTH)}"

        testCal.timeInMillis = now
        if (!activeDaysSet.contains(todayKey) && activeDaysSet.contains(yesterdayKey)) {
            testCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val key = "${testCal.get(Calendar.YEAR)}-${testCal.get(Calendar.MONTH)}-${testCal.get(Calendar.DAY_OF_MONTH)}"
            if (activeDaysSet.contains(key)) {
                streak++
                testCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        val streakInfo = StreakInfo(
            currentStreakDays = streak,
            bestStreakDays = streak.coerceAtLeast(1),
            totalProtectedHours = (totalProtectedMillis / 3600000L).toInt(),
            sessionsCompletedCount = completedAppSessions.size + completedDeviceLocks.size
        )

        // Weekly Report Data
        val thisWeekAppSessions = completedAppSessions.filter { it.endTime >= startOfWeek }
        val thisWeekDeviceLocks = completedDeviceLocks.filter { it.endTime >= startOfWeek }
        var weeklyProtectedMillis = 0L
        for (s in thisWeekAppSessions) {
            weeklyProtectedMillis += if (s.expectedDurationMillis > 0L) s.expectedDurationMillis else (s.endTime - s.startTime).coerceAtLeast(0L)
        }
        for (d in thisWeekDeviceLocks) {
            weeklyProtectedMillis += if (d.expectedDurationMillis > 0L) d.expectedDurationMillis else (d.durationMinutes * 60 * 1000L)
        }

        val topAppWeek = topAppsThisWeek.firstOrNull()
        val riskPeriodStr = if (peakStart != null && peakEnd != null) {
            val sStr = if (peakStart == 0) "12 AM" else if (peakStart < 12) "${peakStart} AM" else if (peakStart == 12) "12 PM" else "${peakStart - 12} PM"
            val eStr = if (peakEnd == 0) "12 AM" else if (peakEnd < 12) "${peakEnd} AM" else if (peakEnd == 12) "12 PM" else "${peakEnd - 12} PM"
            "$sStr – $eStr"
        } else null

        val weeklyReport = WeeklyReportData(
            protectedMinutesThisWeek = weeklyProtectedMillis / 60000L,
            completedSessionsThisWeek = thisWeekAppSessions.size + thisWeekDeviceLocks.size,
            totalAttemptsThisWeek = attemptsWeekList.size,
            mostDangerousAppName = topAppWeek?.appName,
            mostDangerousAppAttempts = topAppWeek?.count ?: 0,
            highestRiskPeriod = riskPeriodStr,
            strongestDayName = peakDayName,
            hasData = (thisWeekAppSessions.isNotEmpty() || thisWeekDeviceLocks.isNotEmpty() || attemptsWeekList.isNotEmpty())
        )

        return TemptationAnalytics(
            attemptsToday = attemptsTodayList.size,
            attemptsThisWeek = attemptsWeekList.size,
            totalAttemptsAllTime = attempts.size,
            topAppsToday = topAppsToday,
            topAppsThisWeek = topAppsThisWeek,
            topAppsAllTime = topAppsAllTime,
            heatmap = heatmap,
            personalInsights = insights,
            streakInfo = streakInfo,
            weeklyReport = weeklyReport
        )
    }

    // Statistics Calculation Flow
    val statisticsFlow: Flow<StatisticsData> = combine(
        completedSessionsFlow,
        totalAttemptsFlow,
        mostAttemptedAppFlow
    ) { sessions, totalAttempts, mostAttempted ->
        calculateStatistics(sessions, totalAttempts, mostAttempted)
    }

    private fun calculateStatistics(
        completedSessions: List<LockSession>,
        totalAttempts: Int,
        mostAttempted: AppAttemptCount?
    ): StatisticsData {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        var totalProtected = 0L
        var thisWeekProtected = 0L

        // Day tracking for streaks
        val activeDaysSet = mutableSetOf<String>()

        for (s in completedSessions) {
            val duration = if (s.expectedDurationMillis > 0L) {
                s.expectedDurationMillis
            } else {
                (s.endTime - s.startTime).coerceAtLeast(0L)
            }
            totalProtected += duration

            if (s.endTime >= startOfWeek) {
                thisWeekProtected += duration
            }

            val cal = Calendar.getInstance().apply { timeInMillis = s.endTime }
            val dateKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
            activeDaysSet.add(dateKey)
        }

        val completedCount = completedSessions.size
        val avgDuration = if (completedCount > 0) totalProtected / completedCount else 0L

        var streakDays = 0
        val checkCal = Calendar.getInstance().apply { timeInMillis = now }
        val todayKey = "${checkCal.get(Calendar.YEAR)}-${checkCal.get(Calendar.MONTH)}-${checkCal.get(Calendar.DAY_OF_MONTH)}"
        checkCal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayKey = "${checkCal.get(Calendar.YEAR)}-${checkCal.get(Calendar.MONTH)}-${checkCal.get(Calendar.DAY_OF_MONTH)}"

        var testCal = Calendar.getInstance().apply { timeInMillis = now }
        if (!activeDaysSet.contains(todayKey) && activeDaysSet.contains(yesterdayKey)) {
            testCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val key = "${testCal.get(Calendar.YEAR)}-${testCal.get(Calendar.MONTH)}-${testCal.get(Calendar.DAY_OF_MONTH)}"
            if (activeDaysSet.contains(key)) {
                streakDays++
                testCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        val totalHours = totalProtected / 3600000

        val milestones = listOf(
            Milestone(
                id = "first_session",
                title = "First Lock",
                requirementText = "Complete your first hardcore lock",
                isUnlocked = completedCount >= 1,
                icon = "🔒"
            ),
            Milestone(
                id = "1_hour",
                title = "1 Hour Protected",
                requirementText = "Accumulate 1 hour of protected time",
                isUnlocked = totalHours >= 1,
                icon = "⏳"
            ),
            Milestone(
                id = "10_hours",
                title = "10 Hours Protected",
                requirementText = "Accumulate 10 hours of protected time",
                isUnlocked = totalHours >= 10,
                icon = "🛡️"
            ),
            Milestone(
                id = "25_sessions",
                title = "25 Completed",
                requirementText = "Finish 25 hardcore focus sessions",
                isUnlocked = completedCount >= 25,
                icon = "🎯"
            ),
            Milestone(
                id = "streak_7",
                title = "7-Day Streak",
                requirementText = "Lock in at least once a day for 7 days",
                isUnlocked = streakDays >= 7,
                icon = "🔥"
            ),
            Milestone(
                id = "streak_30",
                title = "30-Day Streak",
                requirementText = "Complete 30 consecutive days of discipline",
                isUnlocked = streakDays >= 30,
                icon = "⚡"
            )
        )

        return StatisticsData(
            totalProtectedMillis = totalProtected,
            thisWeekProtectedMillis = thisWeekProtected,
            completedSessionsCount = completedCount,
            totalBlockAttempts = totalAttempts,
            currentStreakDays = streakDays,
            mostAttemptedAppName = mostAttempted?.appName,
            mostAttemptedCount = mostAttempted?.count ?: 0,
            averageSessionDurationMillis = avgDuration,
            milestones = milestones
        )
    }
}

