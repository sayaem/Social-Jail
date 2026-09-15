package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import com.example.domain.model.LockSession
import com.example.ui.common.AppLogoBadge
import com.example.ui.common.RichCircularTimerRing
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
import com.example.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActiveSessionScreen(
    session: LockSession,
    remainingMillis: Long,
    blockAttemptsCount: Int = 0
) {
    val scrollState = rememberScrollState()
    val endsAtFormatted = remember(session.endTime) {
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        format.format(Date(session.endTime))
    }
    
    val isNewSession = remember(session.id) {
        System.currentTimeMillis() - session.startTime < 3000L
    }
    var showActivationAnim by remember { mutableStateOf(isNewSession) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JailBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("active_session_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Lock Icon Badge
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2B0E11))
                    .border(1.5.dp, LockCrimson, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Session Active",
                    tint = LockCrimsonBright,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SOCIAL JAIL",
                style = MaterialTheme.typography.labelMedium,
                color = SteelGray,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = Color(0xFF261013),
                shape = RoundedCornerShape(Spacing.pillCorner),
                border = BorderStroke(1.dp, LockCrimson.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(LockCrimsonBright)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "SESSION ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = LockCrimsonBright,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Dominant Circular Countdown Display (Screenshot 4 Focus Timer)
            val totalDuration = (session.endTime - session.startTime).coerceAtLeast(1L)
            val progress = (remainingMillis.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

            RichCircularTimerRing(
                progress = progress,
                timeText = LockSession.formatCountdown(remainingMillis),
                topStatusText = "FOCUS SESSION",
                subtitleText = "Locked until $endsAtFormatted",
                ringColor = LockCrimsonBright,
                size = 240.dp,
                modifier = Modifier.testTag("active_session_countdown")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Impulse interception & Session Telemetry Row (Screenshot 4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color(0xFF131726),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF20263C)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "INTERCEPTED",
                            style = MaterialTheme.typography.labelSmall,
                            color = SteelGray,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$blockAttemptsCount attempts",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (blockAttemptsCount > 0) LockCrimsonBright else TextWhite
                        )
                    }
                }

                Surface(
                    color = Color(0xFF131726),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF20263C)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = SteelGray,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hardcore Locked",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = DisciplineGreen
                        )
                    }
                }
            }

            // Optional Intention / Goal Card
            if (!session.goalText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    color = JailCardSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, JailCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GOAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = SteelGray,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"${session.goalText}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Locked Applications Overview
            val appCount = session.blockedAppNames.size
            Surface(
                color = JailDarkSurface,
                shape = RoundedCornerShape(Spacing.cardCorner),
                border = BorderStroke(1.dp, JailCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$appCount APPLICATIONS LOCKED",
                            style = MaterialTheme.typography.labelSmall,
                            color = SteelGray,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (blockAttemptsCount > 0) {
                            Surface(
                                color = Color(0xFF261517),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$blockAttemptsCount blocked",
                                    color = LockCrimsonBright,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(session.blockedPackageNames.zip(session.blockedAppNames)) { (pkg, name) ->
                            Surface(
                                color = JailCardSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, JailCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppLogoBadge(packageName = pkg, appName = name, size = 22.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Reassuring Principle Message (No unlock buttons whatsoever)
            Surface(
                color = Color.Transparent,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\"Stay with the plan.\"",
                        style = MaterialTheme.typography.titleMedium,
                        color = SteelLight,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You already made the decision. Now you can stop thinking about it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showActivationAnim) {
            JailActivationOverlay(
                onAnimationFinished = { showActivationAnim = false }
            )
        }
    }
}

@Composable
fun JailActivationOverlay(onAnimationFinished: () -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenHeightPx = with(density) { (configuration.screenHeightDp + 200).dp.toPx() }
    val barCount = 6
    val barYOffsets = List(barCount) { remember { Animatable(-screenHeightPx) } }
    val overlayAlpha = remember { Animatable(1f) }
    val redFlashAlpha = remember { Animatable(0f) }
    val textScale = remember { Animatable(2f) }
    val textAlpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        barYOffsets.forEachIndexed { index, anim ->
            launch {
                delay(index * 40L)
                anim.animateTo(
                    0f, 
                    animationSpec = spring(
                        dampingRatio = 0.45f, 
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }
        
        delay((barCount * 40L) + 200L)
        
        launch {
            redFlashAlpha.animateTo(0.5f, tween(50))
            redFlashAlpha.animateTo(0f, tween(500))
        }
        
        launch {
            textAlpha.animateTo(1f, tween(100))
            textScale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
        }
        
        delay(1200)
        
        overlayAlpha.animateTo(0f, tween(500))
        onAnimationFinished()
    }

    if (overlayAlpha.value > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(overlayAlpha.value)
                .background(Color.Black.copy(alpha = 0.85f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                barYOffsets.forEach { anim ->
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .fillMaxHeight()
                            .graphicsLayer { 
                                translationY = anim.value
                            }
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF0F1115),
                                        Color(0xFF1E2129),
                                        Color(0xFF0F1115)
                                    )
                                )
                            )
                            .border(1.dp, Color.Black)
                    )
                }
            }
            
            if (redFlashAlpha.value > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LockCrimsonBright.copy(alpha = redFlashAlpha.value))
                )
            }
            
            if (textAlpha.value > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LOCKED",
                        style = MaterialTheme.typography.displayLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        color = LockCrimsonBright,
                        letterSpacing = 12.sp,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = textScale.value
                                scaleY = textScale.value
                                alpha = textAlpha.value
                            }
                    )
                }
            }
        }
    }
}
