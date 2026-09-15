package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.repository.LockRepository
import com.example.domain.model.LockMode
import com.example.domain.model.Schedule
import com.example.util.ScheduleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduleTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val scheduleId = intent?.getLongExtra(EXTRA_SCHEDULE_ID, -1L) ?: return
        if (scheduleId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val entity = db.scheduleDao().getScheduleById(scheduleId)
                if (entity != null && entity.isEnabled) {
                    val schedule = Schedule.fromEntity(entity)
                    val repo = LockRepository(context)
                    val durationMillis = schedule.durationMinutes * 60 * 1000L

                    // Resolve app names
                    val pm = context.packageManager
                    val appNames = schedule.blockedPackageNames.map { pkg ->
                        try {
                            val info = pm.getApplicationInfo(pkg, 0)
                            pm.getApplicationLabel(info).toString()
                        } catch (e: Exception) {
                            pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
                        }
                    }

                    repo.startImmediateLock(
                        packages = schedule.blockedPackageNames,
                        appNames = appNames,
                        durationMillis = durationMillis,
                        mode = LockMode.HARDCORE,
                        goalText = "Scheduled: ${schedule.title}",
                        profileName = schedule.profileName
                    )
                }

                // Schedule next recurring alarm
                ScheduleManager.refreshNextScheduleAlarm(context)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
    }
}
