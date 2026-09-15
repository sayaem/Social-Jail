package com.example.ui.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.domain.model.AppCategory
import com.example.domain.model.InstalledAppInfo
import com.example.domain.model.LockMode
import com.example.ui.common.AppLogoBadge
import com.example.ui.theme.DisciplineAmber
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.JailBlack
import com.example.ui.theme.JailCardBorder
import com.example.ui.theme.JailCardSurface
import com.example.ui.theme.JailDarkSurface
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.PermissionStatus
import com.example.util.TimeUtils
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateLockScreen(
    installedApps: List<InstalledAppInfo>,
    isLoadingApps: Boolean,
    permissionStatus: PermissionStatus,
    onBackClick: () -> Unit,
    onOpenPermissions: () -> Unit,
    onStartLock: suspend (packages: List<String>, appNames: List<String>, durationMillis: Long, mode: LockMode) -> Result<Long>,
    onScheduleLock: suspend (packages: List<String>, appNames: List<String>, startTime: Long, endTime: Long, mode: LockMode) -> Result<Long>,
    onSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableIntStateOf(1) } // 1: Apps, 2: Duration, 3: Schedule, 4: Confirm

    // App Selection state
    var selectedPackageSet by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<AppCategory?>(null) }

    // Duration preset: 15m, 30m, 1h, 2h, 4h, 8h, custom
    val durationOptions = listOf(
        15 * 60 * 1000L to "15 minutes",
        30 * 60 * 1000L to "30 minutes",
        60 * 60 * 1000L to "1 hour",
        2 * 60 * 60 * 1000L to "2 hours",
        4 * 60 * 60 * 1000L to "4 hours",
        8 * 60 * 60 * 1000L to "8 hours"
    )
    var selectedDurationMillis by remember { mutableLongStateOf(60 * 60 * 1000L) }
    var isCustomDuration by remember { mutableStateOf(false) }
    var customHoursText by remember { mutableStateOf("3") }
    var customMinutesText by remember { mutableStateOf("0") }

    // Schedule: Immediate vs Scheduled
    var isImmediateStart by remember { mutableStateOf(true) }
    var scheduleHoursFromNow by remember { mutableIntStateOf(2) } // default 2 hours later

    // Mode
    var lockMode by remember { mutableStateOf(LockMode.HARDCORE) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val nonEssentialApps = installedApps.filter { !it.isEssential }
    val filteredApps = nonEssentialApps.filter { app ->
        val matchesQuery = searchQuery.isBlank() ||
                app.appName.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == null || app.category == selectedCategoryFilter
        matchesQuery && matchesCategory
    }

    val selectedAppsList = installedApps.filter { selectedPackageSet.contains(it.packageName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JailBlack)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = when (currentStep) {
                        1 -> "Select Apps"
                        2 -> "Lock Duration"
                        3 -> "Schedule"
                        else -> "Confirm Lock"
                    },
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (currentStep > 1) currentStep-- else onBackClick()
                    },
                    modifier = Modifier.testTag("create_lock_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
            },
            actions = {
                Text(
                    text = "Step $currentStep/4",
                    color = SteelGray,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 16.dp)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = JailDarkSurface)
        )

        // Error message banner
        if (errorMessage != null) {
            Surface(
                color = Color(0xFF330B0B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = LockCrimsonBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = TextWhite,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (currentStep) {
                1 -> {
                    // STEP 1: Select Apps
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Category Shortcut Chips
                        Text(
                            text = "CATEGORY SHORTCUTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = SteelGray,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                AppCategory.SOCIAL to "Social",
                                AppCategory.VIDEO to "Video",
                                AppCategory.GAMES to "Games",
                                AppCategory.BROWSERS to "Browsers"
                            ).forEach { (category, title) ->
                                val categoryApps = nonEssentialApps.filter { it.category == category }
                                val allCategorySelected = categoryApps.isNotEmpty() &&
                                        categoryApps.all { selectedPackageSet.contains(it.packageName) }

                                FilterChip(
                                    selected = allCategorySelected,
                                    onClick = {
                                        val newSet = selectedPackageSet.toMutableSet()
                                        if (allCategorySelected) {
                                            categoryApps.forEach { newSet.remove(it.packageName) }
                                        } else {
                                            categoryApps.forEach { newSet.add(it.packageName) }
                                        }
                                        selectedPackageSet = newSet
                                    },
                                    label = { Text(title) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LockCrimson,
                                        selectedLabelColor = TextWhite,
                                        containerColor = JailCardSurface,
                                        labelColor = SteelLight
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = JailCardBorder,
                                        selectedBorderColor = LockCrimson,
                                        enabled = true,
                                        selected = allCategorySelected
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search installed apps...", color = SteelGray) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = SteelGray)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SteelGray)
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LockCrimson,
                                unfocusedBorderColor = JailCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = JailDarkSurface,
                                unfocusedContainerColor = JailDarkSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("app_search_field")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selected: ${selectedPackageSet.size} apps",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selectedPackageSet.isNotEmpty()) LockCrimsonBright else SteelGray,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (selectedPackageSet.isNotEmpty()) {
                                Text(
                                    text = "Clear All",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SteelGray,
                                    modifier = Modifier
                                        .clickable { selectedPackageSet = emptySet() }
                                        .padding(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isLoadingApps) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = LockCrimson)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(filteredApps, key = { it.packageName }) { app ->
                                    val isSelected = selectedPackageSet.contains(app.packageName)
                                    val isImmune = app.isEssential
                                    Surface(
                                        color = if (isSelected) Color(0xFF241012) else JailDarkSurface,
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) LockCrimson else JailCardBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = !isImmune) {
                                                val newSet = selectedPackageSet.toMutableSet()
                                                if (isSelected) newSet.remove(app.packageName) else newSet.add(app.packageName)
                                                selectedPackageSet = newSet
                                            }
                                            .testTag("app_item_${app.packageName}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isSelected,
                                                enabled = !isImmune,
                                                onCheckedChange = { checked ->
                                                    if (!isImmune) {
                                                        val newSet = selectedPackageSet.toMutableSet()
                                                        if (checked) newSet.add(app.packageName) else newSet.remove(app.packageName)
                                                        selectedPackageSet = newSet
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = LockCrimson,
                                                    uncheckedColor = SteelGray,
                                                    checkmarkColor = TextWhite
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            AppLogoBadge(
                                                packageName = app.packageName,
                                                appName = app.appName,
                                                iconDrawable = app.icon,
                                                size = 40.dp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = app.appName,
                                                    color = TextWhite,
                                                    fontWeight = FontWeight.SemiBold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = app.packageName,
                                                    color = SteelGray,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                            Surface(
                                                color = if (isImmune) Color(0xFF1B2E1E) else Color(0xFF1F2430),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = if (isImmune) "ESSENTIAL" else app.category.title,
                                                    color = if (isImmune) com.example.ui.theme.DisciplineGreen else SteelLight,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // STEP 2: Select Duration
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "PRESET DURATIONS",
                                style = MaterialTheme.typography.labelSmall,
                                color = SteelGray,
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(durationOptions) { (duration, label) ->
                            val isSelected = !isCustomDuration && selectedDurationMillis == duration
                            Surface(
                                color = if (isSelected) Color(0xFF241012) else JailDarkSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) LockCrimson else JailCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isCustomDuration = false
                                        selectedDurationMillis = duration
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            isCustomDuration = false
                                            selectedDurationMillis = duration
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = LockCrimson)
                                    )
                                }
                            }
                        }

                        // Custom Duration Option
                        item {
                            Surface(
                                color = if (isCustomDuration) Color(0xFF241012) else JailDarkSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCustomDuration) LockCrimson else JailCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isCustomDuration = true }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Custom Duration",
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        RadioButton(
                                            selected = isCustomDuration,
                                            onClick = { isCustomDuration = true },
                                            colors = RadioButtonDefaults.colors(selectedColor = LockCrimson)
                                        )
                                    }

                                    if (isCustomDuration) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = customHoursText,
                                                onValueChange = { customHoursText = it.filter { ch -> ch.isDigit() } },
                                                label = { Text("Hours") },
                                                modifier = Modifier.weight(1f),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = LockCrimson,
                                                    unfocusedBorderColor = JailCardBorder,
                                                    focusedTextColor = TextWhite,
                                                    unfocusedTextColor = TextWhite
                                                )
                                            )
                                            OutlinedTextField(
                                                value = customMinutesText,
                                                onValueChange = { customMinutesText = it.filter { ch -> ch.isDigit() } },
                                                label = { Text("Minutes") },
                                                modifier = Modifier.weight(1f),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = LockCrimson,
                                                    unfocusedBorderColor = JailCardBorder,
                                                    focusedTextColor = TextWhite,
                                                    unfocusedTextColor = TextWhite
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // STEP 3: Schedule
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "WHEN SHOULD THE LOCK BEGIN?",
                                style = MaterialTheme.typography.labelSmall,
                                color = SteelGray,
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            Surface(
                                color = if (isImmediateStart) Color(0xFF241012) else JailDarkSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isImmediateStart) LockCrimson else JailCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isImmediateStart = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Start Immediately",
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "Lock becomes active as soon as you confirm.",
                                            color = SteelGray,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    RadioButton(
                                        selected = isImmediateStart,
                                        onClick = { isImmediateStart = true },
                                        colors = RadioButtonDefaults.colors(selectedColor = LockCrimson)
                                    )
                                }
                            }
                        }

                        item {
                            Surface(
                                color = if (!isImmediateStart) Color(0xFF241012) else JailDarkSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (!isImmediateStart) LockCrimson else JailCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isImmediateStart = false }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Schedule for Later",
                                                color = TextWhite,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = "Upcoming lock starts automatically at specified time.",
                                                color = SteelGray,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        RadioButton(
                                            selected = !isImmediateStart,
                                            onClick = { isImmediateStart = false },
                                            colors = RadioButtonDefaults.colors(selectedColor = LockCrimson)
                                        )
                                    }

                                    if (!isImmediateStart) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "Start in:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SteelLight
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            listOf(1 to "1 hr", 2 to "2 hrs", 4 to "4 hrs", 8 to "Tonight (8h)").forEach { (hrs, label) ->
                                                val isSelected = scheduleHoursFromNow == hrs
                                                Surface(
                                                    color = if (isSelected) LockCrimson else Color(0xFF1F2430),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .clickable { scheduleHoursFromNow = hrs }
                                                ) {
                                                    Text(
                                                        text = label,
                                                        color = TextWhite,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Discipline Mode (Hardcore vs Normal)
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "DISCIPLINE LEVEL",
                                style = MaterialTheme.typography.labelSmall,
                                color = SteelGray,
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            Surface(
                                color = JailDarkSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = LockCrimsonBright,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Hardcore Mode (Recommended)",
                                                color = TextWhite,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Zero unlock buttons, zero pause, zero edit once active.",
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
                4 -> {
                    // STEP 4: Serious Confirmation
                    val effectiveDuration = if (isCustomDuration) {
                        val h = customHoursText.toLongOrNull() ?: 0L
                        val m = customMinutesText.toLongOrNull() ?: 0L
                        (h * 60 + m) * 60 * 1000L
                    } else {
                        selectedDurationMillis
                    }

                    val now = System.currentTimeMillis()
                    val startTime = if (isImmediateStart) now else now + (scheduleHoursFromNow * 60 * 60 * 1000L)
                    val endTime = startTime + effectiveDuration

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E0F11))
                                    .border(2.dp, LockCrimson, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = LockCrimsonBright,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        item {
                            Text(
                                text = "🔒 LOCK SESSION CONFIRMATION",
                                color = LockCrimsonBright,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = JailCardSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "Selected Apps (${selectedAppsList.size}):",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SteelGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        selectedAppsList.forEach { app ->
                                            Surface(
                                                color = Color(0xFF261010),
                                                shape = RoundedCornerShape(6.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5A1E1E))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    AppLogoBadge(
                                                        packageName = app.packageName,
                                                        appName = app.appName,
                                                        iconDrawable = app.icon,
                                                        size = 18.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = app.appName,
                                                        color = TextWhite,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Duration:", color = SteelGray, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            TimeUtils.formatRemainingShort(effectiveDuration),
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Starts:", color = SteelGray, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            if (isImmediateStart) "Immediately" else TimeUtils.formatDateTime(startTime),
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Ends:", color = SteelGray, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            TimeUtils.formatDateTime(endTime),
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        // Serious warning
                        item {
                            Surface(
                                color = Color(0xFF240E10),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LockCrimson),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "⚠️ NO TAKEBACKS",
                                        color = LockCrimsonBright,
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Once started, this lock CANNOT be cancelled, paused, or shortened from Social Jail. Are you sure?",
                                        color = TextWhite,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        if (!permissionStatus.isAccessibilityEnabled) {
                            item {
                                Surface(
                                    color = Color(0xFF261808),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DisciplineAmber),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Enforcement Permission Needed",
                                            color = DisciplineAmber,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Accessibility service must be enabled to block apps.",
                                            color = SteelLight,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = onOpenPermissions,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Open Settings", color = DisciplineAmber)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Bar
        Surface(
            color = JailDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SteelLight),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("step_back_button")
                    ) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        when (currentStep) {
                            1 -> {
                                if (selectedPackageSet.isEmpty()) {
                                    errorMessage = "Please select at least one app to lock."
                                } else {
                                    errorMessage = null
                                    currentStep = 2
                                }
                            }
                            2 -> {
                                currentStep = 3
                            }
                            3 -> {
                                currentStep = 4
                            }
                            4 -> {
                                // Execute lock creation
                                val effectiveDuration = if (isCustomDuration) {
                                    val h = customHoursText.toLongOrNull() ?: 0L
                                    val m = customMinutesText.toLongOrNull() ?: 0L
                                    (h * 60 + m) * 60 * 1000L
                                } else {
                                    selectedDurationMillis
                                }

                                if (effectiveDuration < 60 * 1000L) {
                                    errorMessage = "Lock duration must be at least 1 minute."
                                    return@Button
                                }

                                isSubmitting = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val pkgs = selectedAppsList.map { it.packageName }
                                    val names = selectedAppsList.map { it.appName }

                                    val result = if (isImmediateStart) {
                                        onStartLock(pkgs, names, effectiveDuration, lockMode)
                                    } else {
                                        val now = System.currentTimeMillis()
                                        val start = now + (scheduleHoursFromNow * 60 * 60 * 1000L)
                                        val end = start + effectiveDuration
                                        onScheduleLock(pkgs, names, start, end, lockMode)
                                    }

                                    isSubmitting = false
                                    if (result.isSuccess) {
                                        onSuccess()
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to start lock."
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentStep == 4) LockCrimson else Color(0xFF1E2430),
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("step_next_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = TextWhite,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = when (currentStep) {
                                4 -> if (isImmediateStart) "START LOCK 🔒" else "SCHEDULE LOCK ⏰"
                                else -> "Next"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
