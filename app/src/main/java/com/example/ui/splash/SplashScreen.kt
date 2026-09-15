package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JailBlack
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

/**
 * Animated 3.5s clean splash screen displaying Social Jail branding,
 * breathing crimson aura, dynamic status logs, and smooth exit.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animation drivers
    val logoScale = remember { Animatable(0.7f) }
    val logoAlpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("INITIALIZING CORE PROTOCOLS...") }

    // Infinite pulse for crimson energy ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    // Sequence orchestrator (~3.8s total duration)
    LaunchedEffect(Unit) {
        // Phase 1: Logo enters (0 - 600ms)
        logoAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        logoScale.animateTo(1f, tween(600, easing = FastOutSlowInEasing))

        // Phase 2: Text content reveals (600 - 1200ms)
        contentAlpha.animateTo(1f, tween(600))
        currentProgress = 0.25f
        statusText = "CALIBRATING ZERO-BARGAINING RULES..."

        delay(900L)
        // Phase 3: Armed background services (1200 - 2400ms)
        currentProgress = 0.65f
        statusText = "ARMING ACCESSIBILITY ENFORCEMENT..."

        delay(1000L)
        // Phase 4: Verification complete (2400 - 3400ms)
        currentProgress = 1.0f
        statusText = "ENFORCEMENT ENGINE ACTIVE"

        delay(600L)
        // Phase 5: Smooth exit (3400 - 3800ms)
        exitAlpha.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF200707),
                        JailBlack,
                        Color(0xFF060709)
                    ),
                    radius = 1200f
                )
            )
            .alpha(exitAlpha.value)
            .testTag("splash_screen")
    ) {
        // Subtle ambient laser grid lines
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.08f)) {
            val step = 40.dp.toPx()
            for (x in 0..(size.width / step).toInt()) {
                drawLine(
                    color = Color.White,
                    start = Offset(x * step, 0f),
                    end = Offset(x * step, size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(size.height / step).toInt()) {
                drawLine(
                    color = Color.White,
                    start = Offset(0f, y * step),
                    end = Offset(size.width, y * step),
                    strokeWidth = 1f
                )
            }
        }

        // Top skip control for convenience
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, end = 24.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                color = Color(0x331F2937),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSplashFinished() }
            ) {
                Text(
                    text = "SKIP",
                    color = SteelLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // Center Emblem & Typography
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emblem with pulsing crimson aura
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                // Expanding pulse glow
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Color(0x33DC2626))
                )

                // Rotating radar/reticle ring
                Canvas(modifier = Modifier.size(124.dp)) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                LockCrimson.copy(alpha = 0.1f),
                                LockCrimsonBright.copy(alpha = 0.9f),
                                LockCrimson.copy(alpha = 0.1f)
                            )
                        ),
                        radius = size.width / 2f - 4f,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner shield badge
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2C0A0A),
                                    Color(0xFF130404)
                                )
                            )
                        )
                        .border(2.dp, LockCrimson, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Social Jail Emblem",
                        tint = LockCrimsonBright,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Title with letter spacing
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(contentAlpha.value)
            ) {
                Text(
                    text = "SOCIAL JAIL",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    color = TextWhite,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ZERO-BARGAINING DIGITAL DISCIPLINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = LockCrimsonBright,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Sleek Progress Bar & Dynamic Diagnostic Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .alpha(contentAlpha.value)
            ) {
                LinearProgressIndicator(
                    progress = { currentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = LockCrimsonBright,
                    trackColor = Color(0xFF1F2430),
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                    color = SteelGray,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom version footer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "v1.0 • UNCOMPROMISING DIGITAL FORTRESS",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF475569),
                letterSpacing = 1.5.sp,
                modifier = Modifier.alpha(contentAlpha.value)
            )
        }
    }
}
