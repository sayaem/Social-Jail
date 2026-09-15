package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AppSelection : Screen("app_selection")
    object Profiles : Screen("profiles")
    object Schedule : Screen("schedule")
    object Statistics : Screen("statistics")
    object History : Screen("history")
    object Settings : Screen("settings")
    object Permissions : Screen("permissions")
}
