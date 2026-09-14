package com.example.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import com.example.data.local.AppDatabase
import com.example.data.local.dao.LockSessionDao
import com.example.domain.model.LockMode
import com.example.domain.model.LockSession
import com.example.domain.model.SessionStatus
import com.example.service.AppBlockingAccessibilityService
import com.example.service.LockAlarmReceiver
import com.example.service.LockEnforcementService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LockRepository(
    private val context: Context,
    private val dao: LockSessionDao = AppDatabase.getInstance(context).lockSessionDao()
) {

    val activeSessionFlow: Flow<LockSession?> = dao.getActiveSessionFlow().map { entity ->
        entity?.let { LockSession.fromEntity(it) }
    }

    val scheduledSessionsFlow: Flow<List<LockSession>> = dao.getScheduledSessions().map { list ->
        list.map { LockSession.fromEntity(it) }
    }

    val completedSessionsFlow: Flow<List<LockSession>> = dao.getCompletedSessions().map { list ->
        list.map { LockSession.fromEntity(it) }
    }

    suspend fun getActiveSession(): LockSession? {
        return dao.getActiveSession()?.let { LockSession.fromEntity(it) }
    }

    suspend fun startImmediateLock(
        packages: List<String>,
        appNames: List<String>,
        durationMillis: Long,
        mode: LockMode = LockMode.HARDCORE
    ): Result<Long> {
        val currentActive = dao.getActiveSession()
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
            expectedDurationMillis = durationMillis
        )

        val id = dao.insertSession(session.toEntity())
        val createdSession = session.copy(id = id)

        // Immediately update accessibility in-memory cache
        AppBlockingAccessibilityService.updateBlockedPackages(packages.toSet(), createdSession)

        // Start persistent foreground service
        LockEnforcementService.start(context)

        return Result.success(id)
    }

    suspend fun scheduleFutureLock(
        packages: List<String>,
        appNames: List<String>,
        startTime: Long,
        endTime: Long,
        mode: LockMode = LockMode.HARDCORE
    ): Result<Long> {
        val now = System.currentTimeMillis()
        if (startTime <= now) {
            return startImmediateLock(packages, appNames, endTime - now, mode)
        }

        // Check for schedule collisions
        val scheduled = dao.getScheduledSessionsList()
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
            expectedDurationMillis = durationMillis
        )

        val id = dao.insertSession(session.toEntity())

        // Set AlarmManager for start
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
        val session = dao.getSessionById(sessionId)
        if (session != null && session.status == SessionStatus.SCHEDULED.name) {
            dao.deleteScheduledSession(sessionId)
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
        dao.updateSessionStatus(sessionId, SessionStatus.COMPLETED.name, System.currentTimeMillis())
        AppBlockingAccessibilityService.clearBlockedPackages()
        LockEnforcementService.stop(context)
    }
}
