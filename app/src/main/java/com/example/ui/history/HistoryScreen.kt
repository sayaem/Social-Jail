package com.example.ui.history

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
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
import com.example.domain.model.LockSession
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.JailBlack
import com.example.ui.theme.JailCardBorder
import com.example.ui.theme.JailDarkSurface
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.TimeUtils
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    completedSessions: List<LockSession>,
    onBackClick: () -> Unit
) {
    val totalLockedMillis = completedSessions.sumOf { it.endTime - it.startTime }
    val totalHours = TimeUnit.MILLISECONDS.toHours(totalLockedMillis)
    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(totalLockedMillis) % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JailBlack)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Lock History",
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
            // Summary Stats Card
            item {
                Surface(
                    color = JailDarkSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${totalHours}h ${totalMinutes}m",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Text(
                                text = "LOCKED TIME",
                                style = MaterialTheme.typography.labelSmall,
                                color = SteelGray,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${completedSessions.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Text(
                                text = "SESSIONS DONE",
                                style = MaterialTheme.typography.labelSmall,
                                color = SteelGray,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "COMPLETED SESSIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SteelGray,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
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
                            text = "No completed sessions yet. Start your first lock to build focus discipline.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                items(completedSessions, key = { it.id }) { session ->
                    val duration = session.endTime - session.startTime
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
                                Text(
                                    text = TimeUtils.formatDate(session.startTime),
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = DisciplineGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Completed",
                                        color = DisciplineGreen,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${TimeUtils.formatTime(session.startTime)} → ${TimeUtils.formatTime(session.endTime)} (${TimeUtils.formatRemainingShort(duration)})",
                                color = SteelLight,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Blocked ${session.blockedAppNames.size} apps: ${session.blockedAppNames.joinToString(", ")}",
                                color = SteelGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
