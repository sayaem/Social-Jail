package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.domain.model.Schedule
import com.example.service.ScheduleTriggerReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

object ScheduleManager {

    suspend fun refreshNextScheduleAlarm(context: Context) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val enabledSchedules = db.scheduleDao().getEnabledSchedules().map { Schedule.fromEntity(it) }
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return@withContext

            val now = System.currentTimeMillis()
            var earliestTriggerTime = Long.MAX_VALUE
            var targetSchedule: Schedule? = null

            for (schedule in enabledSchedules) {
                val nextTrigger = getNextTriggerMillis(schedule, now)
                if (nextTrigger in (now + 1000)..earliestTriggerTime) {
                    earliestTriggerTime = nextTrigger
                    targetSchedule = schedule
                }
            }

            val intent = Intent(context, ScheduleTriggerReceiver::class.java)
            if (targetSchedule != null && earliestTriggerTime != Long.MAX_VALUE) {
                intent.putExtra(ScheduleTriggerReceiver.EXTRA_SCHEDULE_ID, targetSchedule.id)
                val pending = PendingIntent.getBroadcast(
                    context,
                    9901,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, earliestTriggerTime, pending)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, earliestTriggerTime, pending)
                }
            } else {
                val pending = PendingIntent.getBroadcast(
                    context,
                    9901,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
                )
                if (pending != null) {
                    alarmManager.cancel(pending)
                }
            }
        }
    }

    private fun getNextTriggerMillis(schedule: Schedule, now: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        for (dayOffset in 0..7) {
            val checkCal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, schedule.startHour)
                set(Calendar.MINUTE, schedule.startMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // Calendar: 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
            // In our system: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
            val dayOfWeek = when (checkCal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }

            if (schedule.daysOfWeek.contains(dayOfWeek)) {
                if (checkCal.timeInMillis > now + 30000) { // at least 30s in the future
                    return checkCal.timeInMillis
                }
            }
        }
        return Long.MAX_VALUE
    }
}
