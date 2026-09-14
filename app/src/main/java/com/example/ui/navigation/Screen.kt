package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateLock : Screen("create_lock")
    object Permissions : Screen("permissions")
    object History : Screen("history")
    object Settings : Screen("settings")
}
