package com.apptime.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Alarms : Screen("alarms")
    object Settings : Screen("settings")
    object AppManagement : Screen("app_management")
    object AddAlarm : Screen("add_alarm?id={id}") {
        fun createRoute(id: Int = -1) = "add_alarm?id=$id"
    }
}
