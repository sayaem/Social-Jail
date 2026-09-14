package com.example.ui.settings

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RestartAlt
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.JailBlack
import com.example.ui.theme.JailCardBorder
import com.example.ui.theme.JailDarkSurface
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextWhite
import com.example.util.PermissionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isLockActive: Boolean,
    permissionStatus: PermissionStatus,
    onOpenPermissions: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JailBlack)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Settings & Protection",
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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = JailDarkSurface)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Lock Active Warning if currently locked
            if (isLockActive) {
                item {
                    Surface(
                        color = Color(0xFF260D0D),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, LockCrimson),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = LockCrimsonBright,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "🔒 Settings Locked",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Settings that could weaken enforcement are unavailable while a lock is active.",
                                    color = SteelLight,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ANTI-BYPASS ARCHITECTURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SteelGray,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                ProtectionFeatureCard(
                    title = "Reboot Survival",
                    status = "ACTIVE",
                    description = "Locks are saved with absolute Unix timestamps. If phone is restarted, BootReceiver immediately restores the active lock state.",
                    icon = Icons.Default.RestartAlt
                )
            }

            item {
                ProtectionFeatureCard(
                    title = "Time Tamper Resistance",
                    status = "ACTIVE",
                    description = "Cross-references system clock with elapsed monotonic hardware time to detect and resist manual system clock manipulation.",
                    icon = Icons.Default.Security
                )
            }

            item {
                ProtectionFeatureCard(
                    title = "Essential Apps Immunity",
                    status = "PROTECTED",
                    description = "Phone, Dialer, SMS, Emergency, Calculator, and Calendar are strictly protected from accidental blocking.",
                    icon = Icons.Default.Phone
                )
            }

            item {
                Text(
                    text = "DIAGNOSTICS & ENFORCEMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = SteelGray,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = JailDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "System Permissions",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (permissionStatus.isAllCriticalGranted) "All Good ✓" else "Needs Setup",
                                color = if (permissionStatus.isAllCriticalGranted) DisciplineGreen else Color(0xFFFFB74D),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenPermissions,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E2430),
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Review Permissions & Setup")
                        }
                    }
                }
            }

            item {
                Surface(
                    color = JailDarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Philosophy",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Social Jail is built to prevent impulsive self-bypass, not fight device owners. The goal is personal discipline and intentional digital focus.",
                            color = SteelGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProtectionFeatureCard(
    title: String,
    status: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JailDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
        shape = RoundedCornerShape(12.dp),
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
                        tint = LockCrimsonBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Surface(
                    color = Color(0xFF0F2615),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = status,
                        color = DisciplineGreen,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = SteelGray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
