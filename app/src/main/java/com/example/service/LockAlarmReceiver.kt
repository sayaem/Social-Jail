package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.domain.model.LockSession
import com.example.domain.model.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LockAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val sessionId = intent?.getLongExtra(EXTRA_SESSION_ID, -1L) ?: return
        if (sessionId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val scheduled = db.lockSessionDao().getSessionById(sessionId)
                if (scheduled != null && scheduled.status == SessionStatus.SCHEDULED.name) {
                    val now = System.currentTimeMillis()
                    if (scheduled.endTime > now) {
                        db.lockSessionDao().updateSessionStatus(sessionId, SessionStatus.ACTIVE.name, null)
                        val activeSession = LockSession.fromEntity(scheduled.copy(status = SessionStatus.ACTIVE.name))
                        AppBlockingAccessibilityService.updateBlockedPackages(
                            activeSession.blockedPackageNames.toSet(),
                            activeSession
                        )
                        LockEnforcementService.start(context)
                    } else {
                        db.lockSessionDao().updateSessionStatus(sessionId, SessionStatus.COMPLETED.name, now)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_SESSION_ID = "extra_session_id"
    }
}
