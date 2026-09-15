package com.example.ui.blocking

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.AppLogoBadge
import com.example.ui.theme.JailBlack
import com.example.ui.theme.JailCardBorder
import com.example.ui.theme.JailDarkSurface
import com.example.ui.theme.LockCrimson
import com.example.ui.theme.LockCrimsonBright
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SteelGray
import com.example.ui.theme.SteelLight
import com.example.ui.theme.TextWhite
import com.example.util.TimeUtils
import kotlinx.coroutines.delay

class BlockingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure the blocking overlay appears over lockscreen/secure windows
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        // Ensure back press takes user straight to device launcher/home
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goToHomeScreen()
            }
        })

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val endTime = intent.getLongExtra(EXTRA_END_TIME, 0L)
        val goalText = intent.getStringExtra(EXTRA_GOAL_TEXT)

        val appName = try {
            val pm = packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        }

        setContent {
            MyApplicationTheme {
                BlockingScreen(
                    appName = appName,
                    packageName = packageName,
                    endTime = endTime,
                    goalText = goalText,
                    onGoHome = { goToHomeScreen() }
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        goToHomeScreen()
    }

    private fun goToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_blocked_pkg"
        const val EXTRA_END_TIME = "extra_end_time"
        const val EXTRA_GOAL_TEXT = "extra_goal_text"
        const val EXTRA_PROFILE_NAME = "extra_profile_name"
    }
}

@Composable
fun BlockingScreen(
    appName: String,
    packageName: String,
    endTime: Long,
    goalText: String? = null,
    onGoHome: () -> Unit
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val remaining = (endTime - currentTime).coerceAtLeast(0L)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF140505),
                        JailBlack,
                        Color(0xFF090A0D)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // High-impact App Logo & Lock Badge
            Box(
                modifier = Modifier.size(92.dp),
                contentAlignment = Alignment.Center
            ) {
                AppLogoBadge(
                    packageName = packageName,
                    appName = appName,
                    size = 80.dp
                )

                // Superimposed lock seal
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF1E0505))
                        .border(1.5.dp, LockCrimson, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = LockCrimsonBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${appName.uppercase()} IS LOCKED",
                style = MaterialTheme.typography.titleLarge,
                color = LockCrimsonBright,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Nice try. 😄",
                style = MaterialTheme.typography.bodyMedium,
                color = SteelLight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Countdown display
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = JailDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = TimeUtils.formatRemaining(remaining),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = TextWhite,
                        letterSpacing = 2.sp,
                        modifier = Modifier.testTag("blocking_remaining_timer")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "REMAINING",
                        style = MaterialTheme.typography.labelSmall,
                        color = SteelGray,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (endTime > 0L) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Your session ends at ${TimeUtils.formatTime(endTime)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SteelLight,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Goal reminder if present
            if (!goalText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    color = Color(0xFF171B24),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JailCardBorder),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Goal: \"$goalText\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Stay with the plan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SteelGray
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Stay with the plan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SteelGray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Non-bargaining return home action
            Button(
                onClick = onGoHome,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E2430),
                    contentColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(52.dp)
                    .testTag("go_home_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = TextWhite
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "RETURN HOME",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
