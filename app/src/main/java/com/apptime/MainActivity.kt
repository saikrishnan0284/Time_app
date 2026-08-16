package com.apptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apptime.navigation.AppNavigation
import com.apptime.service.UsageMonitorService
import com.apptime.ui.theme.AppTimeTheme
import com.apptime.viewmodel.SettingsViewModel
import com.apptime.worker.ResetAlarmsWorker

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        UsageMonitorService.startService(this)
        ResetAlarmsWorker.schedule(this)
        setContent {
            val settingsVm: SettingsViewModel = viewModel()
            val theme by settingsVm.theme.collectAsState()
            AppTimeTheme(theme = theme) {
                AppNavigation(settingsVm = settingsVm)
            }
        }
    }
}
