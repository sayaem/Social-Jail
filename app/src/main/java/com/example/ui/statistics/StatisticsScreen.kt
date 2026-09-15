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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                    .padding(vertical = 12.dp),
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
                        text = "Discipline & Metrics",
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
                // Key metrics grid
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

                // Block attempts telemetry card
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
                                        text = "Most frequent: ${statistics.mostAttemptedAppName} (${statistics.mostAttemptedCount}x)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SteelLight
                                    )
                                }
                            }
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

                // Session History Section
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
                Text(
                    text = formatHoursMinutes(durationMillis),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = SteelGray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${session.blockedAppNames.size} apps locked: ${session.blockedAppNames.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = SteelLight,
                maxLines = 1
            )

            if (!session.goalText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Goal: \"${session.goalText}\"",
                    style = MaterialTheme.typography.labelSmall,
                    color = DisciplineGreen
                )
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
