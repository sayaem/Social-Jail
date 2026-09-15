package com.example.ui.home
import androidx.compose.ui.text.style.TextOverflow


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LockSession
import com.example.domain.model.Profile
import com.example.ui.common.RichCircularTimerRing
import com.example.ui.common.RichDurationPresetRow
import com.example.ui.common.RichGradientCard
import com.example.ui.theme.DisciplineAmber
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.JailBlack
import com.example.ui.theme.JailCardBorder
import com.example.ui.theme.JailCardSurface
import com.example.ui.theme.JailDarkSurface
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueDeep
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBlueVibrant
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

    var selectedMinutes by remember(defaultDurationMinutes) {
        mutableIntStateOf(defaultDurationMinutes.coerceAtLeast(15))
    }
    var showCustomTimeDialog by remember { mutableStateOf(false) }

    val presets = listOf(
        15 to "15m",
        30 to "30m",
        60 to "1h",
        120 to "2h",
        -1 to "..."
    )

    // Formatted duration string for the center ring (e.g. "25:00" or "02:00:00")
    val timeDisplay = remember(selectedMinutes) {
        val h = selectedMinutes / 60
        val m = selectedMinutes % 60
        if (h > 0) {
            String.format("%02d:%02d:00", h, m)
        } else {
            String.format("%02d:00", m)
        }
    }

    // Inactive "READY TO LOCK" Screen with Rich UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070A12), // Midnight top
                        Color(0xFF0A0D18), // Rich indigo center
                        Color(0xFF05060A)  // Dark base
                    )
                )
            )
            .statusBarsPadding()
            .testTag("home_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // TOP BRAND HEADER (matching EchoVault serif title & status)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Social Jail",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Serif,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Focus on your work",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkyBlueLight.copy(alpha = 0.8f)
                        )
                    }

                    // Status Indicator
                    Surface(
                        color = Color(0xFF0C1F16),
                        shape = RoundedCornerShape(Spacing.pillCorner),
                        border = BorderStroke(1.dp, DisciplineGreen.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
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
                        shape = RoundedCornerShape(14.dp),
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
                        shape = RoundedCornerShape(14.dp),
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

            // PRIMARY HERO FOCUS CARD with Circular Ring (Screenshots 2, 3, 4)
            item {
                Surface(
                    color = Color(0xFF0D111E),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(
                        1.2.dp,
                        Brush.linearGradient(
                            listOf(
                                SkyBlue.copy(alpha = 0.7f),
                                Color(0xFF6366F1).copy(alpha = 0.5f),
                                Color(0xFFA855F7).copy(alpha = 0.3f)
                            )
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ready_to_lock_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top completed session chip (Screenshot 2: "0 sessions completed")
                        Surface(
                            color = Color(0xFF1E1738),
                            shape = RoundedCornerShape(Spacing.pillCorner),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedPackagesCount == 0) "No apps in jail" else "$selectedPackagesCount apps locked",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFDDD6FE),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Circular Focus Timer Ring (Screenshots 2, 3, 4)
                        RichCircularTimerRing(
                            progress = 0.75f,
                            timeText = timeDisplay,
                            topStatusText = "Focus Time",
                            subtitleText = "Ready to lock",
                            ringColor = SkyBlue,
                            size = 210.dp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Duration Preset Selector Row: 15m, 25m, 30m, 1h, 2h (Screenshot 3)
                        RichDurationPresetRow(
                            presets = presets,
                            selectedDurationMinutes = selectedMinutes,
                            onSelectDuration = { mins ->
                                if (mins == -1) {
                                    showCustomTimeDialog = true
                                } else {
                                    selectedMinutes = mins
                                    onQuickJailClick(mins)
                                }
                            },
                            accentColor = SkyBlue
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large Rounded Start Button (Screenshot 3 "Start")
                        Button(
                            onClick = {
                                onQuickJailClick(selectedMinutes)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LockCrimson,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(Spacing.pillCorner),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
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
                                letterSpacing = 1.2.sp,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Project / Target Card at bottom of Hero (matching Screenshot 2: "Project: No project selected")
                        Surface(
                            color = Color(0xFF131726),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFF232A40)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProfilesClick() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1C2238)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = SkyBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Target Profile",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SteelGray,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = selectedProfile?.let { "${it.iconEmoji} ${it.name}" } ?: "Default Preset",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = SteelGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // DISCIPLINE VAULTS SECTION HEADER (EchoVault Style)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DISCIPLINE VAULTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBlueLight.copy(alpha = 0.9f),
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$selectedPackagesCount Apps Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = SteelGray,
                        fontSize = 11.sp
                    )
                }
            }

            // 1. Manage Apps Vault Card (EchoVault glowing gradient card style!)
            item {
                RichGradientCard(
                    title = "App Vault & Restrictions",
                    category = "Enforcement",
                    tags = listOf("social", "$selectedPackagesCount locked"),
                    icon = Icons.Default.Apps,
                    iconAccentColor = SkyBlue,
                    statusIcon = Icons.Default.Lock,
                    statusColor = SkyBlue,
                    onClick = onManageAppsClick,
                    testTag = "nav_manage_apps"
                )
            }

            // 2. Focus Profiles Card
            item {
                RichGradientCard(
                    title = "Focus Profiles & Presets",
                    category = "Presets",
                    tags = listOf(selectedProfile?.name ?: "Study", "Deep Work"),
                    icon = Icons.Default.Tune,
                    iconAccentColor = Color(0xFFA855F7),
                    statusIcon = Icons.Default.Schedule,
                    statusColor = DisciplineAmber,
                    onClick = onProfilesClick,
                    testTag = "nav_profiles"
                )
            }

            // 3. Scheduled Locks Card
            item {
                RichGradientCard(
                    title = "Recurring Alarms & Schedule",
                    category = "Schedule",
                    tags = listOf("night lock", "daily routine"),
                    icon = Icons.Default.Schedule,
                    iconAccentColor = DisciplineGreen,
                    statusIcon = Icons.Default.Shield,
                    statusColor = DisciplineGreen,
                    onClick = onScheduleClick,
                    testTag = "nav_schedule"
                )
            }

            // 4. Discipline Telemetry Card
            item {
                RichGradientCard(
                    title = "Telemetry & Impulse Stats",
                    category = "Analytics",
                    tags = listOf("streaks", "intercepts"),
                    icon = Icons.Default.BarChart,
                    iconAccentColor = SkyBlueVibrant,
                    statusIcon = Icons.Default.BarChart,
                    statusColor = SkyBlue,
                    onClick = onStatisticsClick,
                    testTag = "nav_statistics"
                )
            }

            // 5. Settings Card
            item {
                RichGradientCard(
                    title = "Integrity & Anti-Bypass",
                    category = "Security",
                    tags = listOf("strict mode", "privacy"),
                    icon = Icons.Default.Settings,
                    iconAccentColor = SteelLight,
                    statusIcon = Icons.Default.Settings,
                    statusColor = SteelLight,
                    onClick = onSettingsClick,
                    testTag = "nav_settings"
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        if (showCustomTimeDialog) {
            CustomTimePickerDialog(
                initialMinutes = selectedMinutes,
                onDismiss = { showCustomTimeDialog = false },
                onConfirm = { mins ->
                    showCustomTimeDialog = false
                    selectedMinutes = mins
                    onQuickJailClick(mins)
                }
            )
        }
    }
}
