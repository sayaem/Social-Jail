package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.domain.model.LockSession
import com.example.domain.model.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != Intent.ACTION_TIME_CHANGED &&
            action != Intent.ACTION_TIMEZONE_CHANGED
        ) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val activeEntity = db.lockSessionDao().getActiveSession()
                val now = System.currentTimeMillis()

                if (activeEntity != null) {
                    val session = LockSession.fromEntity(activeEntity)
                    if (session.remainingMillis(now) > 0) {
                        // Restore active lock enforcement immediately
                        AppBlockingAccessibilityService.updateBlockedPackages(
                            session.blockedPackageNames.toSet(),
                            session
                        )
                        LockEnforcementService.start(context)
                        android.util.Log.i(
                            "SocialJail-Diagnostic",
                            "[REBOOT_RESTORE] Device boot action '$action' handled. Hardcore Session #${session.id} successfully restored from persistent storage. ${session.remainingMillis(now) / 1000}s remaining."
                        )
                    } else {
                        // Expired while phone was off
                        db.lockSessionDao().updateSessionStatus(
                            id = session.id,
                            status = SessionStatus.COMPLETED.name,
                            completedAt = now
                        )
                        AppBlockingAccessibilityService.clearBlockedPackages()
                        android.util.Log.i(
                            "SocialJail-Diagnostic",
                            "[REBOOT_EXPIRED] Hardcore Session #${session.id} expired while device was powered off. Marked COMPLETED."
                        )
                    }
                }

                // Check scheduled sessions
                val scheduledList = db.lockSessionDao().getScheduledSessionsList()
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

                for (scheduled in scheduledList) {
                    if (scheduled.startTime <= now && scheduled.endTime > now) {
                        // Start immediately as active
                        db.lockSessionDao().updateSessionStatus(scheduled.id, SessionStatus.ACTIVE.name, null)
                        val activeSession = LockSession.fromEntity(scheduled.copy(status = SessionStatus.ACTIVE.name))
                        AppBlockingAccessibilityService.updateBlockedPackages(
                            activeSession.blockedPackageNames.toSet(),
                            activeSession
                        )
                        LockEnforcementService.start(context)
                    } else if (scheduled.startTime > now && alarmManager != null) {
                        // Schedule alarm
                        val alarmIntent = Intent(context, LockAlarmReceiver::class.java).apply {
                            putExtra(LockAlarmReceiver.EXTRA_SESSION_ID, scheduled.id)
                        }
                        val pending = PendingIntent.getBroadcast(
                            context,
                            scheduled.id.toInt(),
                            alarmIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                scheduled.startTime,
                                pending
                            )
                        } else {
                            alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                scheduled.startTime,
                                pending
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
