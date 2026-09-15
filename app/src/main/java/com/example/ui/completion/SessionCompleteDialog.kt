package com.example.ui.completion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.domain.model.GoalStatus
import com.example.domain.model.LockSession
import com.example.ui.theme.DisciplineAmber
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.JailCardBorder
import com.example.ui.theme.JailCardSurface
import com.example.ui.theme.JailDarkSurface
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.Spacing
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun SessionCompleteDialog(
    session: LockSession,
    blockedAttemptsCount: Int,
    onDismiss: () -> Unit,
    onSubmitReview: (goalStatus: GoalStatus, note: String) -> Unit = { _, _ -> }
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

    var selectedGoalStatus by remember { mutableStateOf(GoalStatus.COMPLETED) }
    var reflectionNote by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Spacing.cardCorner),
            color = JailDarkSurface,
            border = BorderStroke(1.dp, JailCardBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("session_complete_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F2618)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Complete",
                        tint = DisciplineGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SESSION COMPLETE",
                    style = MaterialTheme.typography.titleMedium,
                    color = DisciplineGreen,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = JailCardSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, JailCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Protected Time", color = SteelGray, style = MaterialTheme.typography.bodyMedium)
                            Text(durationText, color = TextWhite, fontWeight = FontWeight.Bold)
                        }

                        if (blockedAttemptsCount > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Blocked Attempts", color = SteelGray, style = MaterialTheme.typography.bodyMedium)
                                Text("$blockedAttemptsCount", color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (!session.goalText.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
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

                // POST-SESSION REVIEW (Feature 9)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "HOW DID IT GO?",
                    style = MaterialTheme.typography.labelSmall,
                    color = SteelLight,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        GoalStatus.COMPLETED to "✅ Done",
                        GoalStatus.PARTIAL to "⚡ Partial",
                        GoalStatus.NOT_COMPLETED to "❌ Missed"
                    ).forEach { (status, label) ->
                        val isSelected = selectedGoalStatus == status
                        Surface(
                            color = if (isSelected) Color(0xFF1B2232) else JailCardSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) DisciplineGreen else JailCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedGoalStatus = status }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) DisciplineGreen else SteelLight,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reflectionNote,
                    onValueChange = { reflectionNote = it },
                    placeholder = { Text("Add quick takeaway or reflection note...", color = SteelGray, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DisciplineGreen,
                        unfocusedBorderColor = JailCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = JailCardSurface,
                        unfocusedContainerColor = JailCardSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        onSubmitReview(selectedGoalStatus, reflectionNote.trim())
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DisciplineGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(Spacing.pillCorner),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Spacing.buttonHeight)
                        .testTag("session_complete_done_button")
                ) {
                    Text("SAVE & FINISH", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
