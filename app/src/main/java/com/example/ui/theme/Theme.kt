package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = LockCrimson,
  onPrimary = Color.White,
  primaryContainer = LockCrimsonDark,
  onPrimaryContainer = Color.White,
  secondary = SteelGray,
  onSecondary = JailBlack,
  secondaryContainer = JailCardSurface,
  onSecondaryContainer = TextWhite,
  tertiary = DisciplineAmber,
  onTertiary = JailBlack,
  background = JailBlack,
  onBackground = TextWhite,
  surface = JailDarkSurface,
  onSurface = TextWhite,
  surfaceVariant = JailCardSurface,
  onSurfaceVariant = TextMuted,
  outline = JailCardBorder,
  error = LockCrimsonBright
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}

