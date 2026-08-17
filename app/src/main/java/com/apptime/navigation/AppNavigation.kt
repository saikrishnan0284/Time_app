package com.apptime.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.apptime.ui.screens.*
import com.apptime.viewmodel.AlarmViewModel
import com.apptime.viewmodel.HomeViewModel
import com.apptime.viewmodel.SettingsViewModel

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val NAV_ITEMS = listOf(
    NavItem(Screen.Home.route, "Home", Icons.Default.Home),
    NavItem(Screen.Alarms.route, "Alarms", Icons.Default.Alarm),
    NavItem(Screen.Settings.route, "Settings", Icons.Default.Settings)
)

@Composable
fun AppNavigation(settingsVm: SettingsViewModel) {
    val navController = rememberNavController()
    val homeVm: HomeViewModel = viewModel()
    val alarmVm: AlarmViewModel = viewModel()

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val topLevelRoutes = NAV_ITEMS.map { it.route }

    Scaffold(
        bottomBar = {
            if (topLevelRoutes.any { currentRoute == it }) {
                NavigationBar {
                    NAV_ITEMS.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    vm = homeVm,
                    onOpenSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Alarms.route) {
                AlarmScreen(
                    vm = alarmVm,
                    onAddAlarm = { navController.navigate(Screen.AddAlarm.createRoute()) },
                    onEditAlarm = { id -> navController.navigate(Screen.AddAlarm.createRoute(id)) }
                )
            }
            composable(
                route = Screen.AddAlarm.route,
                arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
            ) { entry ->
                val id = entry.arguments?.getInt("id") ?: -1
                AddEditAlarmScreen(
                    alarmVm = alarmVm,
                    editId = if (id == -1) null else id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    vm = settingsVm,
                    onNavigateToAppManagement = { navController.navigate(Screen.AppManagement.route) }
                )
            }
            composable(Screen.AppManagement.route) {
                AppManagementScreen(vm = settingsVm, onBack = { navController.popBackStack() })
            }
        }
    }
}
