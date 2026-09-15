package com.example.ui.devicelock

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.common.RichDurationPresetRow
import com.example.ui.theme.DisciplineAmber
import com.example.ui.theme.JailCardSurface
import com.example.ui.theme.JailDarkSurface
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.Spacing
import com.example.ui.theme.SteelGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.TimeUtils

@Composable
fun DeviceLockConfirmDialog(
    initialDurationMinutes: Int = 30,
    onDismiss: () -> Unit,
    onConfirmLock: (durationMinutes: Int, goalText: String?) -> Unit
) {
    var selectedMinutes by remember { mutableIntStateOf(initialDurationMinutes) }
    var goalText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val presets = listOf(
        15 to "15m",
        30 to "30m",
        45 to "45m",
        60 to "1h",
        120 to "2h",
        240 to "4h"
    )

    val goalPresets = listOf(
        "📚 Study Session",
        "💻 Deep Work",
        "😴 Sleep & Rest",
        "🧠 Exam Prep",
        "🧘 Mindfulness",
        "🚫 Break Addiction"
    )

    val calculatedEndTime = remember<String>(selectedMinutes) {
        val end = System.currentTimeMillis() + (selectedMinutes * 60 * 1000L)
        TimeUtils.formatEndTime(end)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("device_lock_confirm_dialog"),
            color = JailDarkSurface,
            border = BorderStroke(1.2.dp, LockCrimson.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = LockCrimson.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = LockCrimsonBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "🔒 DEVICE LOCK",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                fontFamily = FontFamily.Serif,
                                color = TextWhite
                            )
                            Text(
                                text = "Full Phone Lockdown",
                                style = MaterialTheme.typography.bodySmall,
                                color = LockCrimsonBright
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = SteelGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Duration Selector
                Text(
                    text = "SELECT DURATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = SteelGray,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                RichDurationPresetRow(
                    presets = presets,
                    selectedDurationMinutes = selectedMinutes,
                    onSelectDuration = { mins -> selectedMinutes = mins },
                    accentColor = LockCrimsonBright
                )

                Spacer(modifier = Modifier.height(16.dp))

                // End Time Banner
                Surface(
                    color = Color(0xFF1B0D13),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B1520)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Locked Duration",
                                style = MaterialTheme.typography.labelSmall,
                                color = SteelGray
                            )
                            Text(
                                text = "$selectedMinutes minutes",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Ends At",
                                style = MaterialTheme.typography.labelSmall,
                                color = SteelGray
                            )
                            Text(
                                text = calculatedEndTime,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = LockCrimsonBright
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Goal Input & Presets
                Text(
                    text = "FOCUS GOAL / PURPOSE (OPTIONAL)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SteelGray,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    placeholder = { Text("e.g., Study Physics Chapter 4", color = SteelGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("device_lock_goal_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LockCrimsonBright,
                        unfocusedBorderColor = Color(0xFF262D42),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = LockCrimsonBright
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(goalPresets) { preset ->
                        Surface(
                            shape = RoundedCornerShape(Spacing.pillCorner),
                            color = if (goalText == preset) LockCrimson.copy(alpha = 0.3f) else JailCardSurface,
                            border = BorderStroke(
                                1.dp,
                                if (goalText == preset) LockCrimsonBright else Color(0xFF262D42)
                            ),
                            modifier = Modifier.clickable { goalText = preset }
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (goalText == preset) TextWhite else SteelGray,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Serious Commitment Warning Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF260D12)),
                    border = BorderStroke(1.dp, Color(0xFFDC2626)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "This will immediately lock your device. Social Jail cannot cancel an active lock until the time elapses. Emergency calls remain accessible.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button
                Button(
                    onClick = {
                        onConfirmLock(selectedMinutes, goalText.ifBlank { null })
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LockCrimson,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(Spacing.pillCorner),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_device_lock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LOCK PHONE NOW",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
