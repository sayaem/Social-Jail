package com.example.ui.common

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

/**
 * Renders an app's authentic launcher icon or a polished, branded logo badge
 * if the installed icon is not available (such as for test/demo environments).
 */
@Composable
fun AppLogoBadge(
    packageName: String,
    appName: String,
    iconDrawable: Drawable? = null,
    size: Dp = 42.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Try to retrieve icon from Drawable parameter or from PackageManager
    val resolvedBitmap = remember(packageName, iconDrawable) {
        if (iconDrawable != null) {
            try {
                iconDrawable.toBitmap(96, 96, Bitmap.Config.ARGB_8888).asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else {
            try {
                val pm = context.packageManager
                val drawable = pm.getApplicationIcon(packageName)
                drawable.toBitmap(96, 96, Bitmap.Config.ARGB_8888).asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    if (resolvedBitmap != null) {
        Image(
            bitmap = resolvedBitmap,
            contentDescription = "$appName logo",
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.24f))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(size * 0.24f))
        )
    } else {
        // High-fidelity fallback branded badges
        BrandFallbackLogo(
            packageName = packageName,
            appName = appName,
            logoSize = size,
            modifier = modifier
        )
    }
}

@Composable
private fun BrandFallbackLogo(
    packageName: String,
    appName: String,
    logoSize: Dp,
    modifier: Modifier
) {
    val cornerRadius = logoSize * 0.24f
    val shape = RoundedCornerShape(cornerRadius)

    when {
        packageName.contains("instagram", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(logoSize)
                    .clip(shape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF833AB4),
                                Color(0xFFFD1D1D),
                                Color(0xFFFCB045)
                            )
                        )
                    )
                    .border(1.dp, Color(0x44FFFFFF), shape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(logoSize * 0.6f)) {
                    val strokeW = 2.5f * (logoSize.value / 42f)
                    val canvasW = size.width
                    val canvasH = size.height
                    // Camera rounded outer rect
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(canvasW * 0.1f, canvasH * 0.1f),
                        size = androidx.compose.ui.geometry.Size(canvasW * 0.8f, canvasH * 0.8f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                        style = Stroke(width = strokeW)
                    )
                    // Lens circle
                    drawCircle(
                        color = Color.White,
                        radius = canvasW * 0.22f,
                        center = center,
                        style = Stroke(width = strokeW)
                    )
                    // Flash dot
                    drawCircle(
                        color = Color.White,
                        radius = canvasW * 0.05f,
                        center = Offset(canvasW * 0.7f, canvasH * 0.26f)
                    )
                }
            }
        }

        packageName.contains("youtube", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(logoSize)
                    .clip(shape)
                    .background(Color(0xFFFF0000))
                    .border(1.dp, Color(0x44FFFFFF), shape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(logoSize * 0.65f)
                )
            }
        }

        packageName.contains("tiktok", ignoreCase = true) || packageName.contains("musically", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(logoSize)
                    .clip(shape)
                    .background(Color(0xFF010101))
                    .border(1.dp, Color(0xFF00F2FE), shape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♪",
                    color = Color(0xFF00F2FE),
                    fontSize = (logoSize.value * 0.52f).sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        packageName.contains("facebook", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(logoSize)
                    .clip(shape)
                    .background(Color(0xFF1877F2))
                    .border(1.dp, Color(0x44FFFFFF), shape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "f",
                    color = Color.White,
                    fontSize = (logoSize.value * 0.65f).sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        packageName.contains("twitter", ignoreCase = true) || appName.equals("X", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(logoSize)
                    .clip(shape)
                    .background(Color(0xFF0B0E14))
                    .border(1.dp, Color(0xFF334155), shape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "𝕏",
                    color = Color.White,
                    fontSize = (logoSize.value * 0.55f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        packageName.contains("reddit", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(logoSize)
                    .clip(shape)
                    .background(Color(0xFFFF4500))
                    .border(1.dp, Color(0x44FFFFFF), shape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "r/",
                    color = Color.White,
                    fontSize = (logoSize.value * 0.45f).sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        packageName.contains("chrome", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(logoSize)
                    .clip(shape)
                    .background(Color(0xFF1F2937))
                    .border(2.dp, Color(0xFF4285F4), shape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = Color(0xFF34A853),
                    modifier = Modifier.size(logoSize * 0.6f)
                )
            }
        }

        packageName.contains("netflix", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(logoSize)
                    .clip(shape)
                    .background(Color(0xFF111111))
                    .border(1.dp, Color(0xFFE50914), shape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    color = Color(0xFFE50914),
                    fontSize = (logoSize.value * 0.62f).sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        packageName.contains("snapchat", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(logoSize)
                    .clip(shape)
                    .background(Color(0xFFFFFC00))
                    .border(1.dp, Color(0xFFD4D000), shape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👻",
                    fontSize = (logoSize.value * 0.48f).sp
                )
            }
        }

        else -> {
            // Distinctive gradient tile generated with app initials
            val initial = appName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            val hash = packageName.hashCode()
            val gradientPair = getBrandPalette(hash)

            Box(
                modifier = modifier
                    .size(logoSize)
                    .clip(shape)
                    .background(Brush.linearGradient(gradientPair))
                    .border(1.dp, Color(0x33FFFFFF), shape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontSize = (logoSize.value * 0.5f).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

private fun getBrandPalette(hash: Int): List<Color> {
    val palettes = listOf(
        listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
        listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
        listOf(Color(0xFFEC4899), Color(0xFFBE185D)),
        listOf(Color(0xFF10B981), Color(0xFF047857)),
        listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
        listOf(Color(0xFF6366F1), Color(0xFF4338CA)),
        listOf(Color(0xFF06B6D4), Color(0xFF0E7490)),
        listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
    )
    val index = kotlin.math.abs(hash) % palettes.size
    return palettes[index]
}
