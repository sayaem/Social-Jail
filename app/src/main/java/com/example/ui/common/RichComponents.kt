package com.example.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DisciplineAmber
import com.example.ui.theme.DisciplineGreen
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueDeep
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBlueVibrant
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * Rich Card with Glowing Gradient Border, matching the EchoVault style.
 * Dark indigo/black glass surface, vibrant border brush, rounded square icon container,
 * category capsule pill, tags, and right-aligned status badge with chevron.
 */
@Composable
fun RichGradientCard(
    title: String,
    category: String? = null,
    tags: List<String> = emptyList(),
    icon: ImageVector? = null,
    iconCustomContent: (@Composable () -> Unit)? = null,
    iconAccentColor: Color = SkyBlue,
    statusIcon: ImageVector? = null,
    statusColor: Color = SkyBlue,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    testTag: String = "rich_card"
) {
    // Elegant multi-stop gradient border: cyan-sky -> indigo -> soft violet
    val borderGradient = Brush.linearGradient(
        colors = listOf(
            SkyBlue.copy(alpha = 0.75f),
            Color(0xFF6366F1).copy(alpha = 0.5f),
            Color(0xFFA855F7).copy(alpha = 0.3f)
        )
    )

    Surface(
        color = Color(0xFF0C101A),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.2.dp, borderGradient),
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container on the left: rounded square with subtle glow and border
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color(0xFF131726))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(iconAccentColor.copy(alpha = 0.6f), Color(0xFF1E2640))
                        ),
                        shape = RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (iconCustomContent != null) {
                    iconCustomContent()
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconAccentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Center Column: Title, Category pill, Tags
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontFamily = FontFamily.Serif,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (category != null || tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (category != null) {
                            Surface(
                                color = Color(0xFF0C243B),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.8.dp, SkyBlue.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Category: ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SteelGray,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SkyBlueLight,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        tags.take(2).forEach { tag ->
                            Surface(
                                color = Color(0xFF151926),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SteelLight,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right side: Status indicator + Chevron arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (statusIcon != null) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SteelGray.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Circular Timer Ring Gauge inspired by the Focus Timer screenshots.
 * Displays a glowing circular track with active progress arc and large display digits.
 */
@Composable
fun RichCircularTimerRing(
    progress: Float, // 0f to 1f
    timeText: String,
    topStatusText: String = "Ready to focus",
    subtitleText: String = "Ready to start",
    ringColor: Color = SkyBlue,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "ring_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Glowing circular background halo
        Box(
            modifier = Modifier
                .size(size - 30.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ringColor.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Canvas for Circular Track and Glowing Progress Arc
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = 8.dp.toPx()
            val canvasSize = this.size
            val arcSize = Size(canvasSize.width - strokeWidth, canvasSize.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Background subtle ring track
            drawArc(
                color = Color(0xFF141926),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active glowing progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            ringColor.copy(alpha = 0.7f),
                            ringColor,
                            SkyBlueLight,
                            ringColor
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth + 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Inside the ring: Top pill, Large Countdown Digits, Bottom label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = topStatusText,
                style = MaterialTheme.typography.labelSmall,
                color = ringColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Large Countdown Digits
            Text(
                text = timeText,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = SteelGray,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Duration preset selector row: 15, 25, 30, 45, 60 (or custom durations),
 * with the active one highlighted in glowing accent color (matching Screenshot 3).
 */
@Composable
fun RichDurationPresetRow(
    presets: List<Pair<Int, String>>,
    selectedDurationMinutes: Int,
    onSelectDuration: (Int) -> Unit,
    accentColor: Color = SkyBlue,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEach { (mins, label) ->
            val isSelected = selectedDurationMinutes == mins
            Surface(
                color = if (isSelected) accentColor else Color(0xFF131724),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) accentColor else Color(0xFF242C40)
                ),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectDuration(mins) }
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) Color.White else SteelLight,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Rich Bottom Navigation Bar matching the dark, rich aesthetics in Screenshot 1, 3, 4.
 */
data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun RichBottomBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF070A12),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    SkyBlue.copy(alpha = 0.35f),
                    Color(0xFF6366F1).copy(alpha = 0.35f),
                    Color.Transparent
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onItemSelected(index) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag(item.testTag)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) SkyBlue.copy(alpha = 0.18f) else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) SkyBlue else SteelGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) SkyBlueLight else SteelGray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
