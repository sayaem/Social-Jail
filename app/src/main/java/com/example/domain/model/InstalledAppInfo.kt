package com.example.domain.model

import android.graphics.drawable.Drawable

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val category: AppCategory = AppCategory.OTHER,
    val isEssential: Boolean = false
)

enum class AppCategory(val title: String) {
    SOCIAL("Social"),
    VIDEO("Video"),
    GAMES("Games"),
    BROWSERS("Browsers"),
    OTHER("Other")
}
