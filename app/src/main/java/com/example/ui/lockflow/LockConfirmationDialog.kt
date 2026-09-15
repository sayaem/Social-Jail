package com.example.ui.lockflow

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.InstalledAppInfo
import com.example.domain.model.Profile
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LockConfirmationDialog(
    selectedPackages: List<String>,
    allApps: List<InstalledAppInfo>,
    initialDurationMinutes: Int = 120,
    activeProfile: Profile? = null,
    isGoalPromptEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onConfirmLock: (durationMinutes: Int, goalText: String?) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var durationMinutes by remember { mutableIntStateOf(initialDurationMinutes) }
    var selectedGoalCategory by remember { mutableStateOf("Study") }
    var customGoalText by remember { mutableStateOf("") }

    val appMap = remember(allApps) { allApps.associateBy { it.packageName } }
    val lockedApps = remember(selectedPackages, allApps) {
        selectedPackages.map { pkg ->
            appMap[pkg] ?: InstalledAppInfo(
                packageName = pkg,
                appName = pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() },
                icon = null
            )
        }
    }

    val maxSteps = if (isGoalPromptEnabled) 4 else 3

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(Spacing.cardCorner),
            color = JailDarkSurface,
            border = BorderStroke(1.dp, JailCardBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("lock_confirmation_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStep > 1) {
                        IconButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SteelLight
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(36.dp))
                    }

                    Text(
                        text = "STEP $currentStep OF $maxSteps",
                        style = MaterialTheme.typography.labelSmall,
                        color = SteelGray,
                        letterSpacing = 1.5.sp
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = SteelLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "StepContent"
                ) { step ->
                    when (step) {
                        1 -> StepReviewApps(lockedApps = lockedApps)
                        2 -> StepSelectDuration(
                            currentMinutes = durationMinutes,
                            onMinutesSelected = { durationMinutes = it }
                        )
                        3 -> {
                            if (isGoalPromptEnabled) {
                                StepIntention(
                                    selectedCategory = selectedGoalCategory,
                                    onCategorySelected = { selectedGoalCategory = it },
                                    customText = customGoalText,
                                    onCustomTextChanged = { customGoalText = it }
                                )
                            } else {
                                StepFinalConfirmation(
                                    appCount = lockedApps.size,
                                    durationMinutes = durationMinutes,
                                    goalText = null
                                )
                            }
                        }
                        4 -> {
                            val resolvedGoal = if (customGoalText.isNotBlank()) {
                                customGoalText.trim()
                            } else {
                                selectedGoalCategory
                            }
                            StepFinalConfirmation(
                                appCount = lockedApps.size,
                                durationMinutes = durationMinutes,
                                goalText = resolvedGoal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Button
                if (currentStep < maxSteps) {
                    Button(
                        onClick = { currentStep++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF262A36),
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Spacing.buttonHeight)
                            .testTag("step_next_button")
                    ) {
                        Text("Continue", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    val finalGoal = if (isGoalPromptEnabled) {
                        if (customGoalText.isNotBlank()) customGoalText.trim() else selectedGoalCategory
                    } else null

                    Button(
                        onClick = {
                            onConfirmLock(durationMinutes, finalGoal)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LockCrimson,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Spacing.buttonHeight)
                            .testTag("confirm_lock_final_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LOCK FOR ${formatDurationLabel(durationMinutes)}",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepReviewApps(lockedApps: List<InstalledAppInfo>) {
    Column {
        Text(
            text = "You're about to lock",
            style = MaterialTheme.typography.titleLarge,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${lockedApps.size} applications will be blocked immediately upon locking.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = JailCardSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, JailCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(8.dp)) {
                items(lockedApps) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppLogoBadge(packageName = app.packageName, appName = app.appName, size = 32.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = app.appName,
                            color = TextWhite,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepSelectDuration(
    currentMinutes: Int,
    onMinutesSelected: (Int) -> Unit
) {
    val presets = listOf(
        30 to "30 minutes",
        60 to "1 hour",
        120 to "2 hours",
        240 to "4 hours",
        480 to "8 hours"
    )

    Column {
        Text(
            text = "Select Duration",
            style = MaterialTheme.typography.titleLarge,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Session duration cannot be shortened or cancelled once started.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { (mins, label) ->
                val isSelected = currentMinutes == mins
                Surface(
                    color = if (isSelected) Color(0xFF261013) else JailCardSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isSelected) LockCrimson else JailCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMinutesSelected(mins) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) LockCrimsonBright else TextWhite,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = LockCrimsonBright,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIntention(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    customText: String,
    onCustomTextChanged: (String) -> Unit
) {
    val categories = listOf(
        "Study" to "📚",
        "Coding" to "💻",
        "Sleep" to "😴",
        "Focus" to "🧠",
        "Custom" to "✍️"
    )

    Column {
        Text(
            text = "What are you protecting this time for?",
            style = MaterialTheme.typography.titleLarge,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Your intention will be subtly displayed during the lock to reinforce your decision.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (cat, emoji) ->
                val isSelected = selectedCategory == cat
                Surface(
                    color = if (isSelected) Color(0xFF241416) else JailCardSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) LockCrimson else JailCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCategorySelected(cat) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) LockCrimsonBright else SteelLight
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = customText,
            onValueChange = onCustomTextChanged,
            placeholder = { Text("e.g. Finish Physics chapter 4", color = SteelGray) },
            label = { Text("Specific Goal (Optional)", color = SteelLight) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LockCrimson,
                unfocusedBorderColor = JailCardBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = JailCardSurface,
                unfocusedContainerColor = JailCardSurface
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StepFinalConfirmation(
    appCount: Int,
    durationMinutes: Int,
    goalText: String?
) {
    val now = System.currentTimeMillis()
    val endTime = now + (durationMinutes * 60 * 1000L)
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val endsAtFormatted = timeFormat.format(Date(endTime))

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E0F12)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = LockCrimsonBright,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "FINAL CONFIRMATION",
            style = MaterialTheme.typography.titleMedium,
            color = LockCrimsonBright,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                    Text("Apps Locked", color = SteelGray, style = MaterialTheme.typography.bodyMedium)
                    Text("$appCount apps", color = TextWhite, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Duration", color = SteelGray, style = MaterialTheme.typography.bodyMedium)
                    Text(formatDurationLabel(durationMinutes), color = TextWhite, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ends At", color = SteelGray, style = MaterialTheme.typography.bodyMedium)
                    Text(endsAtFormatted, color = LockCrimsonBright, fontWeight = FontWeight.Bold)
                }
                if (!goalText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Goal", color = SteelGray, style = MaterialTheme.typography.bodyMedium)
                        Text("\"$goalText\"", color = TextWhite, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = Color(0xFF1E1012),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFF4A181C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Cancellation will not be available while the session is active.",
                    color = Color(0xFFFCA5A5),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

private fun formatDurationLabel(minutes: Int): String {
    val hours = minutes / 60
    val remainingMins = minutes % 60
    return when {
        hours > 0 && remainingMins > 0 -> "${hours}h ${remainingMins}m"
        hours > 0 -> if (hours == 1) "1 HOUR" else "$hours HOURS"
        else -> "$minutes MINUTES"
    }
}
