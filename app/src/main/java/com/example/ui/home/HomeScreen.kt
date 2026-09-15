package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LockSession
import com.example.ui.common.AppLogoBadge
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.JailBlack
import com.example.ui.theme.JailCardBorder
import com.example.ui.theme.JailCardSurface
import com.example.ui.theme.JailDarkSurface
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.PermissionStatus
import com.example.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    activeSession: LockSession?,
    scheduledSessions: List<LockSession>,
    completedSessions: List<LockSession>,
    remainingMillis: Long,
    permissionStatus: PermissionStatus,
    onCreateLockClick: () -> Unit,
    onPermissionsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCancelScheduled: (Long) -> Unit
) {
    val isLockActive = activeSession != null && remainingMillis > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JailBlack)
    ) {
        // App Bar
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isLockActive) LockCrimsonBright else TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SOCIAL JAIL",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = TextWhite,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onHistoryClick,
                    modifier = Modifier.testTag("history_nav_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Lock History",
                        tint = SteelGray
                    )
                }
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.testTag("settings_nav_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings & Diagnostics",
                        tint = SteelGray
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = JailDarkSurface
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Permission Warning Banner if missing critical enforcement
            if (!permissionStatus.isAllCriticalGranted) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF261808)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE65100)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPermissionsClick() }
                            .testTag("permission_warning_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color(0xFFFFB74D),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enforcement Service Required",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Accessibility access is needed to block selected apps.",
                                    color = Color(0xFFFFCC80),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SETUP",
                                color = Color(0xFFFFB74D),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Status Indicator Header
            item {
                Surface(
                    color = JailDarkSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = SteelGray,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isLockActive) LockCrimsonBright else DisciplineGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLockActive) "🔴 LOCK ACTIVE" else "🟢 No active lock",
                                fontWeight = FontWeight.Bold,
                                color = if (isLockActive) LockCrimsonBright else DisciplineGreen,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Active Lock Dashboard OR Create Lock CTA
            item {
                if (isLockActive && activeSession != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = JailCardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, LockCrimson),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_lock_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TIME REMAINING",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SteelGray,
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ends ${TimeUtils.formatTime(activeSession.endTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SteelLight
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = TimeUtils.formatRemaining(remainingMillis),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = TextWhite,
                                letterSpacing = 2.sp,
                                modifier = Modifier.testTag("home_active_timer")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Blocked apps count and chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Blocked: ${activeSession.blockedAppNames.size} apps",
                                    color = SteelLight,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                activeSession.blockedAppNames.forEachIndexed { index, appName ->
                                    val pkg = activeSession.blockedPackageNames.getOrNull(index) ?: ""
                                    Surface(
                                        color = Color(0xFF261010),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5A1E1E))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AppLogoBadge(
                                                packageName = pkg,
                                                appName = appName,
                                                size = 18.dp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = appName,
                                                color = TextWhite,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Anti-bargaining definitive note
                            Surface(
                                color = Color(0xFF14171E),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = SteelGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Hardcore Mode active. No cancel, edit, or bypass permitted.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SteelGray
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Create Lock Button
                    Button(
                        onClick = onCreateLockClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LockCrimson,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("create_lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "CREATE LOCK",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Upcoming / Scheduled Locks
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UPCOMING SCHEDULE",
                        style = MaterialTheme.typography.labelSmall,
                        color = SteelGray,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (scheduledSessions.isNotEmpty()) {
                        Text(
                            text = "${scheduledSessions.size} scheduled",
                            style = MaterialTheme.typography.labelSmall,
                            color = SteelGray
                        )
                    }
                }
            }

            if (scheduledSessions.isEmpty()) {
                item {
                    Surface(
                        color = JailDarkSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No upcoming locks scheduled.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(scheduledSessions, key = { it.id }) { scheduled ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = JailDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${TimeUtils.formatTime(scheduled.startTime)} → ${TimeUtils.formatTime(scheduled.endTime)}",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${TimeUtils.formatDate(scheduled.startTime)} • ${scheduled.blockedAppNames.size} apps",
                                    color = SteelGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            // User can cancel a scheduled session BEFORE it becomes active
                            IconButton(
                                onClick = { onCancelScheduled(scheduled.id) },
                                modifier = Modifier.testTag("cancel_scheduled_${scheduled.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Scheduled Lock",
                                    tint = SteelGray
                                )
                            }
                        }
                    }
                }
            }

            // Quick History preview
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT SESSIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SteelGray,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (completedSessions.isNotEmpty()) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelSmall,
                            color = LockCrimsonBright,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onHistoryClick() }
                                .padding(4.dp)
                        )
                    }
                }
            }

            if (completedSessions.isEmpty()) {
                item {
                    Surface(
                        color = JailDarkSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No completed lock sessions yet.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(completedSessions.take(3), key = { it.id }) { session ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = JailDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${TimeUtils.formatTime(session.startTime)} → ${TimeUtils.formatTime(session.endTime)}",
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${TimeUtils.formatDate(session.startTime)} • ${session.blockedAppNames.size} apps",
                                    color = SteelGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = DisciplineGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Completed",
                                    color = DisciplineGreen,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
