package com.example.ui.completion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.LockSession
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.JailCardBorder
import com.example.ui.theme.JailCardSurface
import com.example.ui.theme.JailDarkSurface
import com.example.ui.theme.Spacing
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun SessionCompleteDialog(
    session: LockSession,
    blockedAttemptsCount: Int,
    onDismiss: () -> Unit
) {
    val durationMillis = if (session.expectedDurationMillis > 0L) {
        session.expectedDurationMillis
    } else {
        (session.endTime - session.startTime).coerceAtLeast(0L)
    }
    val durationHours = durationMillis / 3600000
    val durationMinutes = (durationMillis % 3600000) / 60000
    val durationText = when {
        durationHours > 0 && durationMinutes > 0 -> "${durationHours}h ${durationMinutes}m"
        durationHours > 0 -> if (durationHours == 1L) "1 hour" else "$durationHours hours"
        else -> "$durationMinutes minutes"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Spacing.cardCorner),
            color = JailDarkSurface,
            border = BorderStroke(1.dp, JailCardBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("session_complete_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F2618)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Complete",
                        tint = DisciplineGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SESSION COMPLETE",
                    style = MaterialTheme.typography.titleMedium,
                    color = DisciplineGreen,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    color = JailCardSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, JailCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Protected Time", color = SteelGray, style = MaterialTheme.typography.bodyMedium)
                            Text(durationText, color = TextWhite, fontWeight = FontWeight.Bold)
                        }

                        if (blockedAttemptsCount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Blocked Attempts", color = SteelGray, style = MaterialTheme.typography.bodyMedium)
                                Text("$blockedAttemptsCount", color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (!session.goalText.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Goal", color = SteelGray, style = MaterialTheme.typography.bodyMedium)
                                Text("\"${session.goalText}\"", color = TextWhite, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Nice work.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SteelLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF262A36),
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Spacing.buttonHeight)
                        .testTag("session_complete_done_button")
                ) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
