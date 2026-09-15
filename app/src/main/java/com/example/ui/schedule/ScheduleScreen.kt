package com.example.ui.schedule

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.Profile
import com.example.domain.model.Schedule
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
import java.util.Locale

@Composable
fun ScheduleScreen(
    schedules: List<Schedule>,
    profiles: List<Profile>,
    onToggleSchedule: (Long, Boolean) -> Unit,
    onSaveSchedule: (Schedule) -> Unit,
    onDeleteSchedule: (Long) -> Unit,
    onBack: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JailBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("schedule_screen")
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Scheduled Locks",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Automate self-discipline during recurring hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = SteelGray
                    )
                }
                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Schedule",
                        tint = LockCrimsonBright
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (schedules.isEmpty()) {
                Surface(
                    color = JailCardSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, JailCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = SteelGray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No schedules set",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set up recurring locks for sleep or deep work blocks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SteelGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onToggle = { onToggleSchedule(schedule.id, it) },
                            onDelete = { onDeleteSchedule(schedule.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateScheduleDialog(
            profiles = profiles,
            onDismiss = { showCreateDialog = false },
            onSave = { newSchedule ->
                onSaveSchedule(newSchedule)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ScheduleCard(
    schedule: Schedule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Surface(
        color = if (schedule.isEnabled) JailCardSurface else Color(0xFF101216),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (schedule.isEnabled) JailCardBorder else Color(0xFF1E2129)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = schedule.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (schedule.isEnabled) TextWhite else SteelGray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${formatTime12Hour(schedule.startHour, schedule.startMinute)} • ${schedule.durationMinutes / 60}h lock",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (schedule.isEnabled) LockCrimsonBright else SteelGray
                    )
                }

                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = LockCrimson,
                        uncheckedThumbColor = SteelGray,
                        uncheckedTrackColor = Color(0xFF262A36)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    dayLabels.forEachIndexed { index, label ->
                        val dayNum = index + 1 // 1..7
                        val isActiveDay = schedule.daysOfWeek.contains(dayNum)
                        Surface(
                            color = if (isActiveDay) (if (schedule.isEnabled) Color(0xFF2E1216) else Color(0xFF1F222B)) else Color.Transparent,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, if (isActiveDay) LockCrimson.copy(alpha = 0.5f) else Color(0xFF2A2E3B)),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    color = if (isActiveDay) TextWhite else SteelGray,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isActiveDay) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = SteelGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateScheduleDialog(
    profiles: List<Profile>,
    onDismiss: () -> Unit,
    onSave: (Schedule) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedProfile by remember { mutableStateOf(profiles.firstOrNull()) }
    var startHour by remember { mutableIntStateOf(22) } // default 10 PM
    var startMinute by remember { mutableIntStateOf(0) }
    var durationMinutes by remember { mutableIntStateOf(480) } // default 8 hours (sleep)
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) } // all week

    val dayNames = listOf(
        1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = JailDarkSurface,
            border = BorderStroke(1.dp, JailCardBorder),
            modifier = Modifier.fillMaxWidth(0.96f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SteelGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Night Sleep Lock", color = SteelGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LockCrimson,
                        unfocusedBorderColor = JailCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = JailCardSurface,
                        unfocusedContainerColor = JailCardSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Profile to lock:", style = MaterialTheme.typography.bodySmall, color = SteelGray)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profiles.forEach { p ->
                        val isSel = selectedProfile?.id == p.id
                        Surface(
                            color = if (isSel) Color(0xFF2E1216) else JailCardSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSel) LockCrimson else JailCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedProfile = p }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(text = p.iconEmoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = p.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) LockCrimsonBright else SteelLight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Repeat on days:", style = MaterialTheme.typography.bodySmall, color = SteelGray)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayNames.forEach { (dayNum, label) ->
                        val isSel = selectedDays.contains(dayNum)
                        Surface(
                            color = if (isSel) Color(0xFF2E1216) else JailCardSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (isSel) LockCrimson else JailCardBorder),
                            modifier = Modifier.clickable {
                                selectedDays = if (isSel) selectedDays - dayNum else selectedDays + dayNum
                            }
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) LockCrimsonBright else SteelGray,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val profile = selectedProfile ?: return@Button
                        if (title.isNotBlank() && selectedDays.isNotEmpty()) {
                            onSave(
                                Schedule(
                                    title = title.trim(),
                                    profileId = profile.id,
                                    profileName = profile.name,
                                    daysOfWeek = selectedDays,
                                    startHour = startHour,
                                    startMinute = startMinute,
                                    durationMinutes = durationMinutes,
                                    blockedPackageNames = profile.packageNames,
                                    isEnabled = true
                                )
                            )
                        }
                    },
                    enabled = title.isNotBlank() && selectedDays.isNotEmpty() && selectedProfile != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LockCrimson,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Spacing.buttonHeight)
                ) {
                    Text("Save Schedule", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatTime12Hour(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val displayHour = if (hour % 12 == 0) 12 else hour % 12
    return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
}
