package com.example.ui.lockflow

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JailBlack
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LockingCountdownOverlay(
    onFinished: () -> Unit
) {
    var count by remember { mutableIntStateOf(3) }

    LaunchedEffect(Unit) {
        delay(600L)
        count = 2
        delay(700L)
        count = 1
        delay(700L)
        count = 0 // Locked state
        delay(800L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JailBlack.copy(alpha = 0.96f))
            .testTag("locking_countdown_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "LOCKING",
                style = MaterialTheme.typography.labelLarge,
                color = TextMuted,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.8f)) with
                            (fadeOut() + scaleOut(targetScale = 1.15f))
                },
                label = "CountdownAnimation"
            ) { targetCount ->
                if (targetCount > 0) {
                    Text(
                        text = "$targetCount",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 84.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = LockCrimsonBright,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "LOCKED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp,
                            color = LockCrimson
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Decision made. Standing by.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}
