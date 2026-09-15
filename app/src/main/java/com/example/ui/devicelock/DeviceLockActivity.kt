package com.example.ui.devicelock

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.domain.model.DeviceLockSession
import com.example.domain.model.DeviceLockStatus
import com.example.domain.model.LockSession
import com.example.service.LockEnforcementService
import com.example.ui.common.RichCircularTimerRing
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.Spacing
import com.example.ui.theme.SteelGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.SocialJailPolicyManager
import com.example.util.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeviceLockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep screen on when locked in focus
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Engage Kiosk Lock Task mode if Device Owner is enabled
        SocialJailPolicyManager.enableKioskLockdown(this)

        setContent {
            MyApplicationTheme {
                DeviceLockPortalScreen(
                    onEmergencyDial = {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(dialIntent)
                    },
                    onSessionFinished = {
                        SocialJailPolicyManager.disableKioskLockdown(this)
                        finish()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SocialJailPolicyManager.enableKioskLockdown(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        SocialJailPolicyManager.disableKioskLockdown(this)
    }
}

@Composable
fun DeviceLockPortalScreen(
    onEmergencyDial: () -> Unit,
    onSessionFinished: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var activeSession by remember { mutableStateOf<DeviceLockSession?>(null) }
    var remainingMillis by remember { mutableLongStateOf(0L) }
    var isCompleted by remember { mutableStateOf(false) }

    // Intercept back button during device lock
    BackHandler(enabled = !isCompleted) {
        // Deliberately do nothing: device is locked
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        val db = AppDatabase.getInstance(context)
        while (true) {
            val entity = db.deviceLockSessionDao().getActiveSession()
            if (entity != null) {
                val session = DeviceLockSession.fromEntity(entity)
                activeSession = session
                val remaining = session.remainingMillis()
                remainingMillis = remaining

                if (remaining <= 0) {
                    db.deviceLockSessionDao().updateSessionStatus(
                        id = session.id,
                        status = DeviceLockStatus.COMPLETED.name,
                        completedAt = System.currentTimeMillis()
                    )
                    isCompleted = true
                    LockEnforcementService.stop(context)
                    break
                }
            } else {
                // No active session found
                isCompleted = true
                break
            }
            delay(1000L)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "portal_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E0A12), // Deep crimson dark core
                        Color(0xFF0F0810),
                        Color(0xFF060308)  // Pure OLED black perimeter
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .testTag("device_lock_activity_screen"),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            // Completion View
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = DisciplineGreen.copy(alpha = 0.15f),
                    border = BorderStroke(2.dp, DisciplineGreen),
                    modifier = Modifier.size(90.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "✓", fontSize = 44.sp, color = DisciplineGreen, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "DEVICE LOCK COMPLETE",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Serif,
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your committed focus period has concluded. Well done!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DisciplineGreen,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = onSessionFinished,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DisciplineGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(Spacing.pillCorner),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(52.dp)
                        .testTag("device_lock_done_button")
                ) {
                    Text(
                        text = "RETURN TO SOCIAL JAIL",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        } else {
            // Active Lockdown View
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Surface(
                        color = Color(0xFF2C0B12),
                        shape = RoundedCornerShape(Spacing.pillCorner),
                        border = BorderStroke(1.dp, LockCrimson.copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = LockCrimsonBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HARDCORE DEVICE LOCK",
                                style = MaterialTheme.typography.labelSmall,
                                color = LockCrimsonBright,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Social Jail",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Serif,
                        color = TextWhite
                    )

                    activeSession?.goalText?.let { goal ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFF140D18),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF2C1930))
                        ) {
                            Text(
                                text = "🎯 $goal",
                                color = SkyBlue,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Central Countdown Ring
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progress = activeSession?.let {
                        val total = if (it.expectedDurationMillis > 0) it.expectedDurationMillis else (it.durationMinutes * 60 * 1000L)
                        (remainingMillis.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    } ?: 1f

                    RichCircularTimerRing(
                        progress = progress,
                        timeText = LockSession.formatCountdown(remainingMillis),
                        topStatusText = "Phone Locked",
                        subtitleText = activeSession?.let { "Ends at ${TimeUtils.formatEndTime(it.endTime)}" } ?: "Focus Active",
                        ringColor = LockCrimson,
                        size = 240.dp
                    )
                }

                // Bottom Emergency SOS and Warning
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Device is physically restricted to prevent distraction.\nStay committed until timer elapses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Emergency Phone Call Button (Always accessible for safety)
                    Button(
                        onClick = onEmergencyDial,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1C1318),
                            contentColor = Color(0xFFEF4444)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF3F1922)),
                        shape = RoundedCornerShape(Spacing.pillCorner),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(46.dp)
                            .testTag("device_lock_emergency_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Emergency Call",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EMERGENCY CALL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
