package com.example.ui.devicelock

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DeviceLockSession
import com.example.domain.model.DeviceLockStats
import com.example.ui.common.RichCircularTimerRing
import com.example.ui.common.RichDurationPresetRow
import com.example.ui.theme.DisciplineAmber
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.JailCardBorder
import com.example.ui.theme.JailCardSurface
import com.example.ui.theme.JailDarkSurface
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.Spacing
import com.example.ui.theme.SteelGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.SocialJailPolicyManager
import com.example.util.TimeUtils

@Composable
fun DeviceLockScreen(
    activeDeviceLock: DeviceLockSession?,
    activeRemainingMillis: Long,
    deviceLockStats: DeviceLockStats,
    completedDeviceLocks: List<DeviceLockSession>,
    onInitiateLock: (durationMinutes: Int, goalText: String?) -> Unit,
    onOpenPortal: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var isDeviceAdminActive by remember {
        mutableStateOf(SocialJailPolicyManager.isDeviceAdmin(context))
    }
    var isDeviceOwnerActive by remember {
        mutableStateOf(SocialJailPolicyManager.isDeviceOwner(context))
    }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }
    var selectedDurationMinutes by remember { mutableIntStateOf(60) }

    val adminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDeviceAdminActive = SocialJailPolicyManager.isDeviceAdmin(context)
        isDeviceOwnerActive = SocialJailPolicyManager.isDeviceOwner(context)
    }

    val isLockActive = activeDeviceLock != null && activeRemainingMillis > 0

    val presets = listOf(
        15 to "15m",
        30 to "30m",
        45 to "45m",
        60 to "1h",
        120 to "2h",
        240 to "4h"
    )

    val calculatedEndTime = remember<String>(selectedDurationMinutes) {
        val end = System.currentTimeMillis() + (selectedDurationMinutes * 60 * 1000L)
        TimeUtils.formatEndTime(end)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF14080D), // Deep crimson-tinged obsidian
                        Color(0xFF0C0911),
                        Color(0xFF060408)
                    )
                )
            )
            .statusBarsPadding()
            .testTag("device_lock_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Top Bar Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("device_lock_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = TextWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "DEVICE LOCK",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                fontFamily = FontFamily.Serif,
                                color = TextWhite
                            )
                            Text(
                                text = "Full Hardware Lockdown",
                                style = MaterialTheme.typography.bodySmall,
                                color = LockCrimsonBright
                            )
                        }
                    }

                    // Security Info Action
                    IconButton(
                        onClick = { showGuideDialog = true },
                        modifier = Modifier.testTag("device_policy_guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Device Policy Info",
                            tint = SkyBlue
                        )
                    }
                }
            }

            // Distinguish Banner: App Jail vs Device Lock
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1117)),
                    border = BorderStroke(1.dp, Color(0xFF381C26)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = LockCrimson.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = LockCrimsonBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "How Device Lock Works",
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Unlike App Jail (which blocks selected apps), Device Lock locks down your entire phone using native DevicePolicyManager APIs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // If Active Device Lock: Show Live Countdown Banner & Quick Portal Link
            if (isLockActive && activeDeviceLock != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF260D12)),
                        border = BorderStroke(1.5.dp, LockCrimson),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPortal() }
                            .testTag("active_device_lock_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                color = Color(0xFF38131B),
                                shape = RoundedCornerShape(Spacing.pillCorner),
                                border = BorderStroke(1.dp, LockCrimsonBright.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = LockCrimsonBright,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PHONE CURRENTLY LOCKED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LockCrimsonBright,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = TimeUtils.formatRemainingCountdown(activeRemainingMillis),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = TextWhite
                            )

                            Text(
                                text = "Tap anywhere to view fullscreen lock portal",
                                style = MaterialTheme.typography.bodySmall,
                                color = SteelGray
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = onOpenPortal,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LockCrimson,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(Spacing.pillCorner),
                                modifier = Modifier.fillMaxWidth(0.7f)
                            ) {
                                Text(
                                    text = "OPEN LOCK PORTAL",
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // Device Policy Permissions Status Tile
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = JailCardSurface),
                        border = BorderStroke(1.dp, JailCardBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "DEVICE PRIVILEGES",
                                style = MaterialTheme.typography.labelSmall,
                                color = SteelGray,
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Device Admin Check
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isDeviceAdminActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isDeviceAdminActive) DisciplineGreen else DisciplineAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Device Administrator",
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextWhite,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = if (isDeviceAdminActive) "Active (Instant lockNow() enabled)" else "Inactive (Tap to activate)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDeviceAdminActive) DisciplineGreen else DisciplineAmber,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (!isDeviceAdminActive) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                                putExtra(
                                                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                                    SocialJailPolicyManager.getAdminComponent(context)
                                                )
                                                putExtra(
                                                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                                    "Social Jail requires Device Admin privilege to immediately secure and lock the screen for Device Lock focus sessions."
                                                )
                                            }
                                            adminLauncher.launch(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = DisciplineAmber,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        )
                                    ) {
                                        Text(text = "ACTIVATE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Device Owner Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isDeviceOwnerActive) Icons.Default.Shield else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (isDeviceOwnerActive) DisciplineGreen else SkyBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Hardcore Kiosk (Device Owner)",
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextWhite,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = if (isDeviceOwnerActive) "Device Owner active (Kiosk + Anti-uninstall)" else "Optional for hardware kiosk mode",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDeviceOwnerActive) DisciplineGreen else SteelGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (!isDeviceOwnerActive) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF162035),
                                        modifier = Modifier.clickable { showGuideDialog = true }
                                    ) {
                                        Text(
                                            text = "ADB SETUP",
                                            color = SkyBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Configuration Hero Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = JailCardSurface),
                        border = BorderStroke(1.dp, Color(0xFF381C26)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("device_lock_setup_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "CHOOSE LOCK DURATION",
                                style = MaterialTheme.typography.labelSmall,
                                color = SteelGray,
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Large Center Timer Preview
                            RichCircularTimerRing(
                                progress = 0.85f,
                                timeText = "${selectedDurationMinutes}m",
                                topStatusText = "Device Lock",
                                subtitleText = "Locks entire phone",
                                ringColor = LockCrimson,
                                size = 190.dp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Duration Presets
                            RichDurationPresetRow(
                                presets = presets,
                                selectedDurationMinutes = selectedDurationMinutes,
                                onSelectDuration = { mins -> selectedDurationMinutes = mins },
                                accentColor = LockCrimsonBright
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Expected End Time Tile
                            Surface(
                                color = Color(0xFF170C12),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF341720)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Unlock Time",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SteelGray
                                    )
                                    Text(
                                        text = calculatedEndTime,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LockCrimsonBright
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Initiate Lock Button
                            Button(
                                onClick = {
                                    if (!isDeviceAdminActive) {
                                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                            putExtra(
                                                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                                SocialJailPolicyManager.getAdminComponent(context)
                                            )
                                            putExtra(
                                                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                                "Social Jail requires Device Admin privilege to lock the screen."
                                            )
                                        }
                                        adminLauncher.launch(intent)
                                    } else {
                                        showConfirmDialog = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LockCrimson,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(Spacing.pillCorner),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("initiate_device_lock_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isDeviceAdminActive) "LOCK PHONE NOW" else "ENABLE ADMIN & LOCK",
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // Device Lock Telemetry & Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = JailCardSurface),
                        border = BorderStroke(1.dp, JailCardBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "TOTAL PROTECTED",
                                style = MaterialTheme.typography.labelSmall,
                                color = SteelGray,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = deviceLockStats.totalProtectedHoursFormatted,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Text(
                                text = "${deviceLockStats.completedSessionsCount} sessions",
                                style = MaterialTheme.typography.bodySmall,
                                color = DisciplineGreen,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = JailCardSurface),
                        border = BorderStroke(1.dp, JailCardBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "CURRENT STREAK",
                                style = MaterialTheme.typography.labelSmall,
                                color = SteelGray,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${deviceLockStats.currentStreakDays} Days",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Text(
                                text = "Longest: ${deviceLockStats.longestSessionMinutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = SkyBlue,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Past Device Lock Sessions History
            if (completedDeviceLocks.isNotEmpty()) {
                item {
                    Text(
                        text = "PAST DEVICE LOCK SESSIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SteelGray,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(completedDeviceLocks.take(5)) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = JailCardSurface),
                        border = BorderStroke(1.dp, JailCardBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = DisciplineGreen.copy(alpha = 0.15f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = DisciplineGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.goalText ?: "Full Phone Lock",
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextWhite,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${item.durationMinutes} min • ${TimeUtils.formatDate(item.endTime)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SteelGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Surface(
                                color = Color(0xFF0C2417),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "COMPLETED",
                                    color = DisciplineGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Dialogs
        if (showConfirmDialog) {
            DeviceLockConfirmDialog(
                initialDurationMinutes = selectedDurationMinutes,
                onDismiss = { showConfirmDialog = false },
                onConfirmLock = { minutes, goal ->
                    showConfirmDialog = false
                    onInitiateLock(minutes, goal)
                }
            )
        }

        if (showGuideDialog) {
            DeviceOwnerGuideDialog(
                onDismiss = { showGuideDialog = false }
            )
        }
    }
}
