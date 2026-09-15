package com.example.ui.profiles
import androidx.compose.foundation.layout.ExperimentalLayoutApi

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.foundation.layout.FlowRow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.InstalledAppInfo
import com.example.domain.model.Profile
import com.example.ui.common.AppLogoBadge
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.Spacing
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun ProfilesScreen(
    profiles: List<Profile>,
    activeProfile: Profile?,
    installedApps: List<InstalledAppInfo>,
    onSelectProfile: (Profile) -> Unit,
    onSaveProfile: (Profile) -> Unit,
    onDeleteProfile: (Long) -> Unit,
    onBack: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070A12),
                        Color(0xFF0A0D18),
                        Color(0xFF05060A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("profiles_screen")
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Focus Profiles",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tailored presets for study, deep work, or sleep",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkyBlueLight
                    )
                }
                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Profile",
                        tint = SkyBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(profiles, key = { it.id }) { profile ->
                    val isSelected = activeProfile?.id == profile.id || activeProfile?.name == profile.name
                    ProfileCard(
                        profile = profile,
                        isSelected = isSelected,
                        onSelect = { onSelectProfile(profile) },
                        onDelete = { onDeleteProfile(profile.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProfileDialog(
            installedApps = installedApps.filter { !it.isEssential },
            onDismiss = { showCreateDialog = false },
            onSave = { newProfile ->
                onSaveProfile(newProfile)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ProfileCard(
    profile: Profile,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val borderBrush = if (isSelected) {
        Brush.linearGradient(
            listOf(
                Color(0xFFA855F7).copy(alpha = 0.8f),
                Color(0xFF6366F1).copy(alpha = 0.6f),
                SkyBlue.copy(alpha = 0.4f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xFF1B2030),
                Color(0xFF151926)
            )
        )
    }

    Surface(
        color = if (isSelected) Color(0xFF141021) else Color(0xFF0C101A),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderBrush),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF151926))
                            .border(1.dp, if (isSelected) Color(0xFFA855F7).copy(alpha = 0.5f) else Color(0xFF22293D), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = profile.iconEmoji, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${profile.packageNames.size} apps • ${formatDuration(profile.defaultDurationMinutes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SteelGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Surface(
                            color = Color(0xFF1E1738),
                            shape = RoundedCornerShape(Spacing.pillCorner),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Color(0xFFDDD6FE),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onSelect,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF131726),
                                contentColor = SkyBlueLight
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Use", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (!profile.isPredefined) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Profile",
                                tint = SteelGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Apps summary pills
            if (profile.packageNames.isNotEmpty()) {
                val maxChips = 12
                val displayPackages = profile.packageNames.take(maxChips)
                val extraCount = profile.packageNames.size - maxChips
                
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    displayPackages.forEach { pkg ->
                        Surface(
                            color = Color(0xFF13161C),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = pkg.substringAfterLast('.'),
                                color = SteelLight,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (extraCount > 0) {
                        Surface(
                            color = Color(0xFF13161C),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "+$extraCount more",
                                color = SteelGray,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateProfileDialog(
    installedApps: List<InstalledAppInfo>,
    onDismiss: () -> Unit,
    onSave: (Profile) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("⚡") }
    var durationMinutes by remember { mutableIntStateOf(120) }
    var selectedPkgs by remember { mutableStateOf<Set<String>>(emptySet()) }

    val emojis = listOf("⚡", "📚", "💻", "🧠", "😴", "🛡️")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0C101A),
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1B2030),
                        Color(0xFF151926)
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth(0.96f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Focus Profile",
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
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Profile Name (e.g. Reading)", color = SteelGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyBlue,
                        unfocusedBorderColor = Color(0xFF1E2638),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = Color(0xFF05060A),
                        unfocusedContainerColor = Color(0xFF05060A)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Emoji picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojis.forEach { emoji ->
                        val isSel = selectedEmoji == emoji
                        Surface(
                            color = if (isSel) Color(0xFF131726) else Color(0xFF0C101A),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSel) SkyBlue else Color(0xFF1E2638)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedEmoji = emoji }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select apps to block (${selectedPkgs.size} selected):",
                    style = MaterialTheme.typography.bodySmall,
                    color = SteelGray
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = Color(0xFF0C101A),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E2638)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(6.dp)) {
                        items(installedApps) { app ->
                            val isSel = selectedPkgs.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPkgs = if (isSel) {
                                            selectedPkgs - app.packageName
                                        } else {
                                            selectedPkgs + app.packageName
                                        }
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppLogoBadge(packageName = app.packageName, appName = app.appName, size = 26.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = app.appName,
                                    color = TextWhite,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Checkbox(
                                    checked = isSel,
                                    onCheckedChange = { checked ->
                                        selectedPkgs = if (checked) {
                                            selectedPkgs + app.packageName
                                        } else {
                                            selectedPkgs - app.packageName
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = SkyBlue,
                                        uncheckedColor = SteelGray
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && selectedPkgs.isNotEmpty()) {
                            onSave(
                                Profile(
                                    name = name.trim(),
                                    iconEmoji = selectedEmoji,
                                    packageNames = selectedPkgs.toList(),
                                    defaultDurationMinutes = durationMinutes,
                                    isPredefined = false
                                )
                            )
                        }
                    },
                    enabled = name.isNotBlank() && selectedPkgs.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyBlue,
                        contentColor = Color(0xFF070A12),
                        disabledContainerColor = Color(0xFF131726),
                        disabledContentColor = SteelGray
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Spacing.buttonHeight)
                ) {
                    Text("Save Profile", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0 && m > 0) "${h}h ${m}m" else if (h > 0) "${h}h" else "${m}m"
}
