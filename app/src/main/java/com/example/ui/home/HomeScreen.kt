package com.example.ui.home

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LockSession
import com.example.domain.model.Profile
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.JailBlack
import com.example.ui.theme.JailCardBorder
import com.example.ui.theme.JailCardSurface
import com.example.ui.theme.JailDarkSurface
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.Spacing
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.PermissionStatus

@Composable
fun HomeScreen(
    activeSession: LockSession?,
    remainingMillis: Long,
    selectedPackagesCount: Int,
    selectedProfile: Profile?,
    defaultDurationMinutes: Int,
    permissionStatus: PermissionStatus,
    diagnosticReport: com.example.util.SocialJailDiagnostics.DiagnosticReport? = null,
    onStartSessionClick: () -> Unit,
    onQuickJailClick: (minutes: Int) -> Unit,
    onManageAppsClick: () -> Unit,
    onProfilesClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPermissionsClick: () -> Unit
) {
    val isLockActive = activeSession != null && remainingMillis > 0

    if (isLockActive && activeSession != null) {
        ActiveSessionScreen(
            session = activeSession,
            remainingMillis = remainingMillis
        )
        return
    }

    // Inactive "READY TO LOCK" Screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JailBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("home_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Top Brand & Status Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SOCIAL JAIL",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.5.sp,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Self-imposed digital discipline",
                            style = MaterialTheme.typography.bodySmall,
                            color = SteelGray
                        )
                    }

                    // Status Indicator
                    Surface(
                        color = Color(0xFF0F2618),
                        shape = RoundedCornerShape(Spacing.pillCorner),
                        border = BorderStroke(1.dp, DisciplineGreen.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(DisciplineGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = DisciplineGreen,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Diagnostic Alert Banner if accessibility was turned off
            if (diagnosticReport != null && (!diagnosticReport.isServiceRunning || !diagnosticReport.isSettingsEnabled)) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1010)),
                        border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPermissionsClick() }
                            .testTag("diagnostic_alert_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Accessibility Service Inactive",
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Tap to enable Social Jail in Android Accessibility settings.",
                                    color = TextWhite,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            } else if (!permissionStatus.isAllCriticalGranted) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF261808)),
                        border = BorderStroke(1.dp, Color(0xFFE65100)),
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
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Permissions Required",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Setup Accessibility to allow hardcore enforcement.",
                                    color = Color(0xFFFFCC80),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // PRIMARY HERO CARD: "Ready to Lock"
            item {
                Surface(
                    color = JailCardSurface,
                    shape = RoundedCornerShape(Spacing.cardCorner),
                    border = BorderStroke(1.dp, JailCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ready_to_lock_card")
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = "READY TO LOCK",
                            style = MaterialTheme.typography.labelSmall,
                            color = SteelGray,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Selected Apps & Profile status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedPackagesCount == 0) "No apps selected" else "$selectedPackagesCount apps selected",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            if (selectedProfile != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFF26181A),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, LockCrimson.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "${selectedProfile.iconEmoji} ${selectedProfile.name}",
                                        color = LockCrimsonBright,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Default duration ${formatHoursLabel(defaultDurationMinutes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SteelLight
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // One Obvious Large Primary Action Button
                        Button(
                            onClick = onStartSessionClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LockCrimson,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Spacing.buttonHeight)
                                .testTag("start_session_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "START SESSION",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Quick Jail Row: 30m, 1h, 2h, 4h
                        Text(
                            text = "QUICK JAIL",
                            style = MaterialTheme.typography.labelSmall,
                            color = SteelGray,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(30 to "30m", 60 to "1h", 120 to "2h", 240 to "4h").forEach { (mins, label) ->
                                Surface(
                                    color = Color(0xFF14171E),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF242936)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onQuickJailClick(mins) }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextWhite,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Secondary Navigation Options
            item {
                Text(
                    text = "DISCIPLINE CONTROLS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SteelGray,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                SecondaryNavCard(
                    title = "Manage Apps",
                    subtitle = if (selectedPackagesCount > 0) "$selectedPackagesCount apps selected" else "Select which apps to lock",
                    icon = Icons.Default.Apps,
                    onClick = onManageAppsClick,
                    testTag = "nav_manage_apps"
                )
            }

            item {
                SecondaryNavCard(
                    title = "Profiles",
                    subtitle = "Presets for Study, Deep Work, and Sleep",
                    icon = Icons.Default.Tune,
                    onClick = onProfilesClick,
                    testTag = "nav_profiles"
                )
            }

            item {
                SecondaryNavCard(
                    title = "Schedule",
                    subtitle = "Recurring locks for night sleep and work blocks",
                    icon = Icons.Default.Schedule,
                    onClick = onScheduleClick,
                    testTag = "nav_schedule"
                )
            }

            item {
                SecondaryNavCard(
                    title = "Statistics & History",
                    subtitle = "Protected time, streaks, and impulse intercepts",
                    icon = Icons.Default.BarChart,
                    onClick = onStatisticsClick,
                    testTag = "nav_statistics"
                )
            }

            item {
                SecondaryNavCard(
                    title = "Settings",
                    subtitle = "Defaults, enforcement integrity, and privacy statement",
                    icon = Icons.Default.Settings,
                    onClick = onSettingsClick,
                    testTag = "nav_settings"
                )
            }

            item { Spacer(modifier = Modifier.height(28.dp)) }
        }
    }
}

@Composable
private fun SecondaryNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = JailDarkSurface,
        shape = RoundedCornerShape(Spacing.cardCorner),
        border = BorderStroke(1.dp, JailCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B1E28)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SteelLight,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SteelGray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SteelGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatHoursLabel(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> if (h == 1) "1 hour" else "$h hours"
        else -> "$m minutes"
    }
}
