package com.example.ui.apps

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.InstalledAppInfo
import com.example.ui.common.AppLogoBadge
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

@Composable
fun AppSelectionScreen(
    installedApps: List<InstalledAppInfo>,
    selectedPackages: Set<String>,
    isLoading: Boolean,
    onToggleApp: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Social", "Entertainment", "Browser", "Games", "Other")

    val (selectableApps, immuneApps) = remember(installedApps) {
        installedApps.partition { !it.isEssential }
    }

    val filteredApps = remember(selectableApps, searchQuery, selectedCategory) {
        selectableApps.filter { app ->
            val matchesQuery = searchQuery.isBlank() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Social" -> isSocialApp(app.packageName)
                "Entertainment" -> isEntertainmentApp(app.packageName)
                "Browser" -> isBrowserApp(app.packageName)
                "Games" -> isGameApp(app.packageName)
                "Other" -> !isSocialApp(app.packageName) &&
                        !isEntertainmentApp(app.packageName) &&
                        !isBrowserApp(app.packageName) &&
                        !isGameApp(app.packageName)
                else -> true
            }

            matchesQuery && matchesCategory
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JailBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("app_selection_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        text = "Manage Locked Apps",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${selectedPackages.size} selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedPackages.isNotEmpty()) LockCrimsonBright else SteelGray
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps...", color = SteelGray) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = SteelGray)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = SteelGray)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LockCrimson,
                    unfocusedBorderColor = JailCardBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = JailCardSurface,
                    unfocusedContainerColor = JailCardSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        color = if (isSelected) Color(0xFF2E1216) else JailCardSurface,
                        shape = RoundedCornerShape(Spacing.pillCorner),
                        border = BorderStroke(1.dp, if (isSelected) LockCrimson else JailCardBorder),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) LockCrimsonBright else SteelLight,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Quick Actions: Select All / Deselect All
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredApps.size} apps found",
                    style = MaterialTheme.typography.bodySmall,
                    color = SteelGray
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Select All",
                        style = MaterialTheme.typography.labelMedium,
                        color = LockCrimsonBright,
                        modifier = Modifier
                            .clickable { onSelectAll() }
                            .padding(4.dp)
                    )
                    Text(
                        text = "Clear All",
                        style = MaterialTheme.typography.labelMedium,
                        color = SteelGray,
                        modifier = Modifier
                            .clickable { onDeselectAll() }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LockCrimson)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Apps List
                    items(filteredApps, key = { it.packageName }) { app ->
                        val isSelected = selectedPackages.contains(app.packageName)
                        Surface(
                            color = if (isSelected) Color(0xFF1F1215) else JailCardSurface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isSelected) LockCrimson.copy(alpha = 0.6f) else JailCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onToggleApp(app.packageName) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppLogoBadge(
                                    packageName = app.packageName,
                                    appName = app.appName,
                                    size = 38.dp
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SteelGray,
                                        maxLines = 1
                                    )
                                }
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { onToggleApp(app.packageName) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = LockCrimson,
                                        uncheckedColor = SteelGray,
                                        checkmarkColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Protected / Immune Apps Section
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Surface(
                            color = Color(0xFF0E1419),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Protected",
                                        tint = DisciplineGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PROTECTED FROM LOCKING",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DisciplineGreen,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "System utilities (Phone, Messages, Clock, Settings, Social Jail) are kept immune to guarantee safety and emergency reachability.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SteelGray,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }

        // Floating Save / Apply Bar
        Surface(
            color = JailDarkSurface.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, JailCardBorder),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${selectedPackages.size} apps",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "will be locked in sessions",
                        style = MaterialTheme.typography.bodySmall,
                        color = SteelGray
                    )
                }

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LockCrimson,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("app_selection_done_button")
                ) {
                    Text("Apply Selection", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Helpers for categorizing apps heuristically
private fun isSocialApp(pkg: String): Boolean {
    val p = pkg.lowercase()
    return p.contains("instagram") || p.contains("tiktok") || p.contains("facebook") ||
            p.contains("twitter") || p.contains("x.android") || p.contains("snapchat") ||
            p.contains("reddit") || p.contains("threads") || p.contains("whatsapp") ||
            p.contains("telegram") || p.contains("discord") || p.contains("messenger")
}

private fun isEntertainmentApp(pkg: String): Boolean {
    val p = pkg.lowercase()
    return p.contains("youtube") || p.contains("netflix") || p.contains("twitch") ||
            p.contains("spotify") || p.contains("hulu") || p.contains("disney") ||
            p.contains("primevideo") || p.contains("tiktok")
}

private fun isBrowserApp(pkg: String): Boolean {
    val p = pkg.lowercase()
    return p.contains("chrome") || p.contains("browser") || p.contains("firefox") ||
            p.contains("opera") || p.contains("edge") || p.contains("brave") || p.contains("duckduckgo")
}

private fun isGameApp(pkg: String): Boolean {
    val p = pkg.lowercase()
    return p.contains("game") || p.contains("supercell") || p.contains("roblox") ||
            p.contains("minecraft") || p.contains("pubg") || p.contains("genshin") ||
            p.contains("riotgames") || p.contains("king.com")
}
