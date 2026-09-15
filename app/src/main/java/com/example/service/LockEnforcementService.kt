package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.domain.model.LockSession
import com.example.domain.model.SessionStatus
import com.example.ui.blocking.BlockingActivity
import com.example.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LockEnforcementService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitorJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initialNotification = buildActiveNotification("Starting Social Jail enforcement...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        startEnforcementMonitor()
        return START_STICKY
    }

    private fun startEnforcementMonitor() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            var tickCount = 0

            while (isActive) {
                try {
                    val activeEntity = db.lockSessionDao().getActiveSession()
                    if (activeEntity == null) {
                        // No active lock in database, stop enforcement
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                        break
                    }

                    val session = LockSession.fromEntity(activeEntity)
                    val remaining = session.remainingMillis()

                    if (remaining <= 0) {
                        // Lock Expired!
                        db.lockSessionDao().updateSessionStatus(
                            id = session.id,
                            status = SessionStatus.COMPLETED.name,
                            completedAt = System.currentTimeMillis()
                        )
                        AppBlockingAccessibilityService.clearBlockedPackages()
                        showCompletionNotification()
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                        break
                    }

                    // Keep accessibility service cache synchronized
                    AppBlockingAccessibilityService.updateBlockedPackages(
                        session.blockedPackageNames.toSet(),
                        session
                    )

                    // Update notification once per second (every 3 ticks of 300ms)
                    if (tickCount % 3 == 0) {
                        val remainingText = "${TimeUtils.formatRemainingShort(remaining)} remaining"
                        val notification = buildActiveNotification(remainingText)
                        notificationManager.notify(NOTIFICATION_ID, notification)
                    }

                    // High-frequency secondary fallback usage stats check
                    checkUsageStatsForeground(session)

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                tickCount++
                delay(300L)
            }
        }
    }

    private fun checkUsageStatsForeground(session: LockSession) {
        try {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return
            val endTime = System.currentTimeMillis()
            val beginTime = endTime - 2000L
            val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)
            val event = UsageEvents.Event()

            var latestPackage: String? = null
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    latestPackage = event.packageName
                }
            }

            if (latestPackage != null &&
                latestPackage != packageName &&
                session.blockedPackageNames.contains(latestPackage)
            ) {
                // Immediately kick out of blocked app to Home
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(homeIntent)

                // Launch blocking UI
                val blockingIntent = Intent(applicationContext, BlockingActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
                    )
                    putExtra(BlockingActivity.EXTRA_PACKAGE_NAME, latestPackage)
                    putExtra(BlockingActivity.EXTRA_END_TIME, session.endTime)
                }
                startActivity(blockingIntent)
            }
        } catch (e: Exception) {
            // Usage stats might not have permission, gracefully ignore
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // If app was swiped away from Recents, re-arm the enforcement service immediately
        val restartIntent = Intent(applicationContext, LockEnforcementService::class.java)
        ContextCompat.startForegroundService(applicationContext, restartIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildActiveNotification(contentText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_active_title))
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun showCompletionNotification() {
        val completeNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_complete_title))
            .setContentText(getString(R.string.notification_complete_desc))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(COMPLETION_NOTIFICATION_ID, completeNotification)
    }

    override fun onDestroy() {
        super.onDestroy()
        monitorJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "social_jail_enforcement"
        const val NOTIFICATION_ID = 1001
        const val COMPLETION_NOTIFICATION_ID = 1002

        fun start(context: Context) {
            val intent = Intent(context, LockEnforcementService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LockEnforcementService::class.java)
            context.stopService(intent)
        }
    }
}
