package com.example.ui.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DisciplineAmber
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.JailBlack
import com.example.ui.theme.JailCardBorder
import com.example.ui.theme.JailCardSurface
import com.example.ui.theme.JailDarkSurface
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.PermissionStatus
import com.example.util.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    permissionStatus: PermissionStatus,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JailBlack)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Enforcement & Permissions",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
            },
            actions = {
                IconButton(onClick = onRefresh, modifier = Modifier.testTag("refresh_permissions_button")) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = SteelGray
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = JailDarkSurface)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    color = JailDarkSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Transparent & Local-First",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Social Jail operates 100% locally. Permissions are strictly used to detect foreground app launches and display the blocking screen. No keystrokes, personal messages, or analytics are collected.",
                            color = SteelGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // 1. Accessibility Service
            item {
                PermissionItemCard(
                    title = "Accessibility Service",
                    badgeText = "CRITICAL",
                    description = "Required to detect when a blocked app is launched and immediately display the Social Jail lock screen.",
                    isGranted = permissionStatus.isAccessibilityEnabled,
                    icon = Icons.Default.Security,
                    onOpenSettings = { PermissionUtils.openAccessibilitySettings(context) }
                )
            }

            // 2. Notifications
            item {
                PermissionItemCard(
                    title = "Notifications",
                    badgeText = "CRITICAL",
                    description = "Used to display active lock countdown and inform you when the session has completed.",
                    isGranted = permissionStatus.isNotificationEnabled,
                    icon = Icons.Default.Notifications,
                    onOpenSettings = { PermissionUtils.openNotificationSettings(context) }
                )
            }

            // 3. Usage Access
            item {
                PermissionItemCard(
                    title = "Usage Access",
                    badgeText = "RECOMMENDED",
                    description = "Acts as a secondary detection fallback to identify foreground applications.",
                    isGranted = permissionStatus.isUsageAccessEnabled,
                    icon = Icons.Default.QueryStats,
                    onOpenSettings = { PermissionUtils.openUsageAccessSettings(context) }
                )
            }

            // 4. Battery Optimization
            item {
                PermissionItemCard(
                    title = "Background Activity",
                    badgeText = "RECOMMENDED",
                    description = "Prevents aggressive Android battery management from killing enforcement during active sessions.",
                    isGranted = permissionStatus.isBatteryOptimizationIgnored,
                    icon = Icons.Default.BatteryAlert,
                    onOpenSettings = { PermissionUtils.openBatteryOptimizationSettings(context) }
                )
            }
        }
    }
}

@Composable
fun PermissionItemCard(
    title: String,
    badgeText: String,
    description: String,
    isGranted: Boolean,
    icon: ImageVector,
    onOpenSettings: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JailDarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) JailCardBorder else Color(0xFF422114)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) DisciplineGreen else DisciplineAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Surface(
                    color = if (isGranted) Color(0xFF0F2615) else Color(0xFF2E190A),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isGranted) DisciplineGreen else DisciplineAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGranted) "Enabled" else "Required",
                            color = if (isGranted) DisciplineGreen else DisciplineAmber,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                color = SteelGray,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) Color(0xFF1E2430) else LockCrimson,
                    contentColor = TextWhite
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isGranted) "Configure in Settings" else "OPEN SETTINGS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
