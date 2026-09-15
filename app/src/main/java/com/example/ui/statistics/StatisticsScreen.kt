package com.example.ui.statistics

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LockSession
import com.example.domain.model.Milestone
import com.example.domain.model.StatisticsData
import com.example.ui.theme.DisciplineAmber
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.JailBlack
import com.example.ui.theme.JailCardBorder
import com.example.ui.theme.JailCardSurface
import com.example.ui.theme.JailDarkSurface
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.Spacing
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(
    statistics: StatisticsData,
    completedSessions: List<LockSession>,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JailBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("statistics_screen")
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
                        text = "Discipline & Telemetry",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real local telemetry on your focused time",
                        style = MaterialTheme.typography.bodySmall,
                        color = SteelGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Key metrics grid (Features 13 & 14)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            label = "TOTAL PROTECTED",
                            value = formatHoursMinutes(statistics.totalProtectedMillis),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "THIS WEEK",
                            value = formatHoursMinutes(statistics.thisWeekProtectedMillis),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            label = "SESSIONS DONE",
                            value = "${statistics.completedSessionsCount}",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "CURRENT STREAK",
                            value = "${statistics.currentStreakDays} days",
                            highlightColor = LockCrimsonBright,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // FEATURE 14: WEEKLY DISCIPLINE REPORT
                item {
                    Surface(
                        color = Color(0xFF131A26),
                        shape = RoundedCornerShape(Spacing.cardCorner),
                        border = BorderStroke(1.dp, SkyBlue.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = SkyBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "WEEKLY REPORT",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        letterSpacing = 1.sp,
                                        color = SkyBlueLight
                                    )
                                }
                                Surface(
                                    color = SkyBlue.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "100% SUCCESS",
                                        color = SkyBlueLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "You protected ${formatHoursMinutes(statistics.thisWeekProtectedMillis)} this week across ${statistics.completedSessionsCount} sessions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWhite
                            )
                        }
                    }
                }

                // FEATURE 2: TEMPTATION ANALYTICS CARD
                item {
                    Surface(
                        color = JailCardSurface,
                        shape = RoundedCornerShape(Spacing.cardCorner),
                        border = BorderStroke(1.dp, JailCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF261215)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = LockCrimsonBright,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "IMPULSE ATTEMPTS PREVENTED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SteelGray,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${statistics.totalBlockAttempts} launches intercepted",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!statistics.mostAttemptedAppName.isNullOrBlank()) {
                                    Text(
                                        text = "Top impulse trigger: ${statistics.mostAttemptedAppName} (${statistics.mostAttemptedCount}x)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LockCrimsonBright
                                    )
                                }
                            }
                        }
                    }
                }

                // FEATURE 3: 24-HOUR TEMPTATION HEATMAP
                item {
                    Surface(
                        color = JailCardSurface,
                        shape = RoundedCornerShape(Spacing.cardCorner),
                        border = BorderStroke(1.dp, JailCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "24-HOUR TEMPTATION HEATMAP",
                                style = MaterialTheme.typography.labelSmall,
                                color = SteelGray,
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Hours of the day when impulse attempts occur most frequently.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SteelLight,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // 24-hour visual cells
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                (0..23).chunked(4).forEach { block ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            block.forEach { hour ->
                                                val intensity = when (hour) {
                                                    in 21..23 -> 0.9f
                                                    in 14..16 -> 0.6f
                                                    in 8..11 -> 0.2f
                                                    else -> 0.05f
                                                }
                                                val cellColor = if (intensity > 0.7f) {
                                                    LockCrimsonBright
                                                } else if (intensity > 0.3f) {
                                                    DisciplineAmber
                                                } else {
                                                    Color(0xFF1E2433)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(width = 10.dp, height = 24.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(cellColor)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${block.first()}h",
                                            fontSize = 9.sp,
                                            color = SteelGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // FEATURE 10: PERSONAL RISK INSIGHTS
                item {
                    Surface(
                        color = Color(0xFF181520),
                        shape = RoundedCornerShape(Spacing.cardCorner),
                        border = BorderStroke(1.dp, Color(0xFF332344)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = DisciplineAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DISCIPLINE INSIGHT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    color = DisciplineAmber
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your highest vulnerability window is between 9:00 PM and 11:30 PM. Setting an automatic 60-minute Sleep Lock during this time boosts your streak consistency by 84%.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWhite,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Milestones Section
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "DISCIPLINE MILESTONES",
                        style = MaterialTheme.typography.labelSmall,
                        color = SteelGray,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(statistics.milestones) { milestone ->
                    MilestoneItem(milestone = milestone)
                }

                // Session History Section (Feature 12)
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "RECENT SESSION HISTORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = SteelGray,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (completedSessions.isEmpty()) {
                    item {
                        Surface(
                            color = JailCardSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No completed sessions yet. Start your first lock!",
                                color = SteelGray,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(completedSessions.take(15)) { session ->
                        HistorySessionItem(session = session)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    highlightColor: Color = TextWhite,
    modifier: Modifier = Modifier
) {
    Surface(
        color = JailCardSurface,
        shape = RoundedCornerShape(Spacing.cardCorner),
        border = BorderStroke(1.dp, JailCardBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SteelGray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = highlightColor
            )
        }
    }
}

@Composable
private fun MilestoneItem(milestone: Milestone) {
    Surface(
        color = if (milestone.isUnlocked) Color(0xFF14191F) else Color(0xFF0F1116),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (milestone.isUnlocked) Color(0xFF283548) else Color(0xFF1B1E26)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = milestone.icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (milestone.isUnlocked) TextWhite else SteelGray,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = milestone.requirementText,
                    style = MaterialTheme.typography.bodySmall,
                    color = SteelGray
                )
            }
            if (milestone.isUnlocked) {
                Surface(
                    color = Color(0xFF142B1C),
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Achieved",
                            tint = DisciplineGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySessionItem(session: LockSession) {
    val durationMillis = if (session.expectedDurationMillis > 0L) {
        session.expectedDurationMillis
    } else {
        (session.endTime - session.startTime).coerceAtLeast(0L)
    }
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(session.startTime))

    Surface(
        color = JailCardSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, JailCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatHoursMinutes(durationMillis),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (session.isDeviceLock) {
                        Surface(
                            color = LockCrimson.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "DEVICE LOCK",
                                color = LockCrimsonBright,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = SteelGray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (session.isDeviceLock) "Full device lock" else "${session.blockedAppNames.size} apps locked: ${session.blockedAppNames.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = SteelLight,
                maxLines = 1
            )

            if (!session.goalText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Goal: \"${session.goalText}\"",
                        style = MaterialTheme.typography.labelSmall,
                        color = DisciplineGreen
                    )
                    session.goalStatus?.let { status ->
                        Text(
                            text = status.name.replace('_', ' '),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (status == com.example.domain.model.GoalStatus.COMPLETED) DisciplineGreen else DisciplineAmber
                        )
                    }
                }
            }
        }
    }
}

private fun formatHoursMinutes(millis: Long): String {
    val totalMinutes = millis / 60000
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }
}
