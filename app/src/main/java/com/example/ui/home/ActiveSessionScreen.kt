package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActiveSessionScreen(
    session: LockSession,
    remainingMillis: Long,
    blockAttemptsCount: Int = 0
) {
    val scrollState = rememberScrollState()
    val endsAtFormatted = remember(session.endTime) {
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        format.format(Date(session.endTime))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JailBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("active_session_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Lock Icon Badge
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2B0E11))
                    .border(1.5.dp, LockCrimson, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Session Active",
                    tint = LockCrimsonBright,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SOCIAL JAIL",
                style = MaterialTheme.typography.labelMedium,
                color = SteelGray,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = Color(0xFF261013),
                shape = RoundedCornerShape(Spacing.pillCorner),
                border = BorderStroke(1.dp, LockCrimson.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(LockCrimsonBright)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "SESSION ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = LockCrimsonBright,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Dominant Countdown Display
            Text(
                text = LockSession.formatCountdown(remainingMillis),
                fontFamily = FontFamily.Monospace,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("active_session_countdown")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Locked until $endsAtFormatted",
                style = MaterialTheme.typography.bodyMedium,
                color = SteelLight
            )

            // Optional Intention / Goal Card
            if (!session.goalText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    color = JailCardSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, JailCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GOAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = SteelGray,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"${session.goalText}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Locked Applications Overview
            val appCount = session.blockedAppNames.size
            Surface(
                color = JailDarkSurface,
                shape = RoundedCornerShape(Spacing.cardCorner),
                border = BorderStroke(1.dp, JailCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$appCount APPLICATIONS LOCKED",
                            style = MaterialTheme.typography.labelSmall,
                            color = SteelGray,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (blockAttemptsCount > 0) {
                            Surface(
                                color = Color(0xFF261517),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$blockAttemptsCount blocked",
                                    color = LockCrimsonBright,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(session.blockedPackageNames.zip(session.blockedAppNames)) { (pkg, name) ->
                            Surface(
                                color = JailCardSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, JailCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppLogoBadge(packageName = pkg, appName = name, size = 22.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Reassuring Principle Message (No unlock buttons whatsoever)
            Surface(
                color = Color.Transparent,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\"Stay with the plan.\"",
                        style = MaterialTheme.typography.titleMedium,
                        color = SteelLight,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You already made the decision. Now you can stop thinking about it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
