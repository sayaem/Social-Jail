package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueDeep
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBlueVibrant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Premium Splash Screen.
 * Features an authoritative, ultra-bold title in luminous sky blue,
 * stylish serif typography, atmospheric lighting, and clean branding.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val contentAlpha = remember { Animatable(0f) }
    val contentScale = remember { Animatable(0.93f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // 1. Emblem and Title emerge smoothly with subtle scale
        launch {
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
        launch {
            contentScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }

        delay(200L)

        // 2. Subtitle & accent divider appear cleanly
        subtitleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )

        // Hold momentarily for visual impact
        delay(1100L)

        // 3. Graceful exit fade
        exitAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        )
        onSplashFinished()
    }

    // Sky Blue gradient for the bold title
    val skyBlueTitleBrush = Brush.verticalGradient(
        colors = listOf(
            SkyBlueLight,
            SkyBlue,
            SkyBlueVibrant
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0C1929), // Ethereal sky-midnight core
                        Color(0xFF070C15), // Deep twilight
                        Color(0xFF030508)  // Solid deep black
                    ),
                    radius = 900f
                )
            )
            .alpha(exitAlpha.value)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onSplashFinished()
            }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Atmospheric Sky Blue Ambient Halo behind content
        Box(
            modifier = Modifier
                .size(340.dp)
                .alpha(contentAlpha.value * 0.45f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SkyBlue.copy(alpha = 0.28f),
                            SkyBlueDeep.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(contentScale.value)
                .alpha(contentAlpha.value)
        ) {
            // Stylish Minimalist Sky Blue Lock Crest
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x2838BDF8),
                                Color(0x100284C7)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                SkyBlue.copy(alpha = 0.8f),
                                SkyBlueDeep.copy(alpha = 0.3f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SkyBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Extra Bold Title in Radiant Sky Blue with Stylish Serif Typography
            Text(
                text = "SOCIAL JAIL",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 7.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    brush = skyBlueTitleBrush,
                    shadow = Shadow(
                        color = SkyBlue.copy(alpha = 0.55f),
                        offset = Offset(0f, 4f),
                        blurRadius = 24f
                    )
                ),
                modifier = Modifier.testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stylish Accent Divider with delicate sky blue geometry
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.alpha(subtitleAlpha.value)
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, SkyBlue.copy(alpha = 0.55f))
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(SkyBlue)
                )
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(SkyBlue.copy(alpha = 0.55f), Color.Transparent)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Refined Subtitle
            Text(
                text = "PURE DIGITAL DISCIPLINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 4.5.sp,
                color = SkyBlueLight.copy(alpha = 0.85f),
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
        }
    }
}
