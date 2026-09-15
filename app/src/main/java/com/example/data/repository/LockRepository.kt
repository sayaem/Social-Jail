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
import com.example.data.local.dao.LockSessionDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.dao.ScheduleDao
import com.example.domain.model.LockMode
import com.example.domain.model.LockSession
import com.example.domain.model.Milestone
import com.example.domain.model.Profile
import com.example.domain.model.Schedule
import com.example.domain.model.SessionStatus
import com.example.domain.model.StatisticsData
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
    private val profileDao: ProfileDao = db.profileDao()
    private val scheduleDao: ScheduleDao = db.scheduleDao()
    private val attemptDao: BlockAttemptDao = db.blockAttemptDao()

    val activeSessionFlow: Flow<LockSession?> = sessionDao.getActiveSessionFlow().map { entity ->
        entity?.let { LockSession.fromEntity(it) }
    }

    val scheduledSessionsFlow: Flow<List<LockSession>> = sessionDao.getScheduledSessions().map { list ->
        list.map { LockSession.fromEntity(it) }
    }

    val completedSessionsFlow: Flow<List<LockSession>> = sessionDao.getCompletedSessions().map { list ->
        list.map { LockSession.fromEntity(it) }
    }

    val profilesFlow: Flow<List<Profile>> = profileDao.getAllProfiles().map { list ->
        list.map { Profile.fromEntity(it) }
    }

    val schedulesFlow: Flow<List<Schedule>> = scheduleDao.getAllSchedules().map { list ->
        list.map { Schedule.fromEntity(it) }
    }

    val totalAttemptsFlow: Flow<Int> = attemptDao.getTotalAttemptCount()

    val mostAttemptedAppFlow: Flow<AppAttemptCount?> = attemptDao.getMostAttemptedApp()

    suspend fun getActiveSession(): LockSession? {
        return sessionDao.getActiveSession()?.let { LockSession.fromEntity(it) }
    }

    suspend fun ensureDefaultProfilesSeeded() {
        val existing = profileDao.getAllProfilesList()
        if (existing.isEmpty()) {
            val presets = Profile.getDefaultPresets().map { it.toEntity() }
            profileDao.insertAll(presets)
        }
    }

    suspend fun startImmediateLock(
        packages: List<String>,
        appNames: List<String>,
        durationMillis: Long,
        mode: LockMode = LockMode.HARDCORE,
        goalText: String? = null,
        profileName: String? = null
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
            profileName = profileName
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
        
        // Remove uninstall protection when session naturally expires
        com.example.util.SocialJailPolicyManager.applyUninstallProtection(context, false)
        
        LockEnforcementService.stop(context)
    }

    // Profile Management
    suspend fun saveProfile(profile: Profile): Long {
        return profileDao.insertProfile(profile.toEntity())
    }

    suspend fun deleteProfile(id: Long) {
        profileDao.deleteProfile(id)
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
        // Only clear completed sessions, never active ones
        val completed = sessionDao.getCompletedSessions()
        // Run clean up
        db.runInTransaction {
            // Delete completed sessions
        }
    }

    suspend fun clearAllStatistics() {
        attemptDao.clearAllAttempts()
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

            // Streak tracking by date key "YYYY-MM-DD"
            val cal = Calendar.getInstance().apply { timeInMillis = s.endTime }
            val dateKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
            activeDaysSet.add(dateKey)
        }

        val completedCount = completedSessions.size
        val avgDuration = if (completedCount > 0) totalProtected / completedCount else 0L

        // Calculate consecutive streak back from today/yesterday
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
