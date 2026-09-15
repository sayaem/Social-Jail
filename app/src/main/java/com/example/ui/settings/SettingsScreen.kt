package com.example.ui.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.Spacing
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.PermissionStatus

@Composable
fun SettingsScreen(
    defaultDurationMinutes: Int,
    isGoalPromptEnabled: Boolean,
    permissionStatus: PermissionStatus,
    isDeviceAdmin: Boolean,
    isDeviceOwner: Boolean,
    onSetDefaultDuration: (Int) -> Unit,
    onSetGoalPromptEnabled: (Boolean) -> Unit,
    onOpenPermissions: () -> Unit,
    onActivateDeviceAdmin: () -> Unit,
    onClearHistory: () -> Unit,
    onClearStatistics: () -> Unit,
    onBack: () -> Unit
) {
    var showClearHistoryConfirm by remember { mutableStateOf(false) }
    var showClearStatsConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070A12),
                        Color(0xFF0A0D18),
                        Color(0xFF05060A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Integrity & Security",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Preferences and local device integrity",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkyBlueLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Section: Session Defaults
                item {
                    Text(
                        text = "SESSION CONFIGURATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBlueLight.copy(alpha = 0.9f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFF0C101A),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1B2030),
                                    Color(0xFF151926)
                                )
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Default Duration Picker
                            Text(
                                text = "Default Session Duration",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(30 to "30m", 60 to "1h", 120 to "2h", 240 to "4h").forEach { (mins, label) ->
                                    val isSel = defaultDurationMinutes == mins
                                    Surface(
                                        color = if (isSel) Color(0xFF131726) else Color(0xFF0C101A),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, if (isSel) SkyBlue.copy(alpha = 0.5f) else Color(0xFF222630)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSetDefaultDuration(mins) }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isSel) SkyBlueLight else SteelLight,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Goal Prompt Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Intention / Goal Prompt",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Ask what you are protecting time for before locking",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SteelGray
                                    )
                                }
                                Switch(
                                    checked = isGoalPromptEnabled,
                                    onCheckedChange = onSetGoalPromptEnabled,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = SkyBlue,
                                        uncheckedThumbColor = SteelGray,
                                        uncheckedTrackColor = Color(0xFF262A36)
                                    )
                                )
                            }
                        }
                    }
                }

                // Section: Enforcement & Accessibility Health
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ENFORCEMENT INTEGRITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBlueLight.copy(alpha = 0.9f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFF0C101A),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1B2030),
                                    Color(0xFF151926)
                                )
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPermissions() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Accessibility Enforcement",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (permissionStatus.isAccessibilityEnabled) {
                                        Surface(
                                            color = Color(0xFF0F2618),
                                            shape = RoundedCornerShape(Spacing.pillCorner)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = DisciplineGreen,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            color = Color(0xFF2E1216),
                                            shape = RoundedCornerShape(Spacing.pillCorner)
                                        ) {
                                            Text(
                                                text = "NEEDS SETUP",
                                                color = LockCrimsonBright,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Inspect and verify background permission integrity",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SteelGray
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SteelGray
                            )
                        }
                    }
                }

                // Section: Local Data Management
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DATA & STORAGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBlueLight.copy(alpha = 0.9f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFF0C101A),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1B2030),
                                    Color(0xFF151926)
                                )
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showClearHistoryConfirm = true }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Clear Completed Session Logs",
                                    color = TextWhite,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text("Clear", color = LockCrimsonBright, style = MaterialTheme.typography.labelSmall)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFF1E2638))
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showClearStatsConfirm = true }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reset Block Attempt Telemetry",
                                    color = TextWhite,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text("Reset", color = LockCrimsonBright, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Section: Anti-Uninstall & Device Management
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ANTI-UNINSTALL PROTECTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBlueLight.copy(alpha = 0.9f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF0C101A),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1B2030),
                                    Color(0xFF151926)
                                )
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Level 2: Device Admin
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { if (!isDeviceAdmin && !isDeviceOwner) onActivateDeviceAdmin() }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Level 2: Device Administrator",
                                        color = TextWhite,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Natively prevents uninstallation from the launcher. Can be deactivated manually in settings.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SteelGray
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                if (isDeviceOwner) {
                                    Text("OVERRIDDEN", color = SteelGray, style = MaterialTheme.typography.labelSmall)
                                } else if (isDeviceAdmin) {
                                    Surface(
                                        color = Color(0xFF0F2618),
                                        shape = RoundedCornerShape(Spacing.pillCorner)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            color = DisciplineGreen,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Text("ACTIVATE", color = SkyBlue, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFF1E2638))
                            )
                            
                            // Level 3: Device Owner
                            var showDOInstructions by remember { mutableStateOf(false) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDOInstructions = true }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Level 3: Device Owner",
                                        color = TextWhite,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Maximum enforcement. OS natively blocks uninstallation during lockdown. Requires ADB setup.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SteelGray
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                if (isDeviceOwner) {
                                    Surface(
                                        color = Color(0xFF0F2618),
                                        shape = RoundedCornerShape(Spacing.pillCorner)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            color = DisciplineGreen,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Text("SETUP", color = SteelLight, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            if (showDOInstructions) {
                                AlertDialog(
                                    onDismissRequest = { showDOInstructions = false },
                                    title = { Text("Device Owner Setup", color = TextWhite) },
                                    text = { 
                                        Column {
                                            Text("Android strictly forbids apps from making themselves uninstallable unless they are the Device Owner. To enable Level 3 protection:", color = SteelLight, style = MaterialTheme.typography.bodySmall)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("1. Remove ALL accounts from your device in Android Settings > Accounts.", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                                            Text("2. Connect via ADB.", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                                            Text("3. Run: adb shell dpm set-device-owner com.aistudio.socialjail.hxrk/com.example.service.JailDeviceAdminReceiver", color = DisciplineGreen, style = MaterialTheme.typography.bodySmall)
                                            Text("4. Re-add your accounts.", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Once active, the uninstallation button will be completely disabled at the OS level during a lockdown session.", color = SteelLight, style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { showDOInstructions = false }) {
                                            Text("Understood", color = SkyBlue)
                                        }
                                    },
                                    containerColor = Color(0xFF131726)
                                )
                            }
                        }
                    }
                }

                // Section: Strict Privacy Statement
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFF0D1016),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E232F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = DisciplineGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PRIVACY MANIFESTO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DisciplineGreen,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Social Jail runs entirely on-device.\n• No accounts\n• No analytics\n• No cloud sync\n• No telemetry\n• No personal data leaves this phone.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SteelLight,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }

    if (showClearHistoryConfirm) {
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirm = false },
            title = { Text("Clear History?", color = TextWhite) },
            text = { Text("Past completed session records will be deleted locally. Active locks are not affected.", color = SteelLight) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearHistoryConfirm = false
                    }
                ) {
                    Text("Clear", color = LockCrimsonBright)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryConfirm = false }) {
                    Text("Cancel", color = SteelGray)
                }
            },
            containerColor = Color(0xFF131726)
        )
    }

    if (showClearStatsConfirm) {
        AlertDialog(
            onDismissRequest = { showClearStatsConfirm = false },
            title = { Text("Reset Interception Counters?", color = TextWhite) },
            text = { Text("This will reset impulse launch attempt counters to zero.", color = SteelLight) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearStatistics()
                        showClearStatsConfirm = false
                    }
                ) {
                    Text("Reset", color = LockCrimsonBright)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearStatsConfirm = false }) {
                    Text("Cancel", color = SteelGray)
                }
            },
            containerColor = Color(0xFF131726)
        )
    }
}
