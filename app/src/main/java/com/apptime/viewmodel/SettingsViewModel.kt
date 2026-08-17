package com.apptime.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apptime.data.AlertType
import com.apptime.data.AppTheme
import com.apptime.data.AppUsage
import com.apptime.data.AppUsageHelper
import com.apptime.data.datastore.SettingsDataStore
import com.apptime.service.UsageMonitorService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val settings = SettingsDataStore(app)
    private val helper = AppUsageHelper(app)

    val theme: StateFlow<AppTheme> = settings.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.DARK)

    val excludedPackages: StateFlow<Set<String>> = settings.excludedPackagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val serviceEnabled: StateFlow<Boolean> = settings.serviceEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val defaultAlertType: StateFlow<AlertType> = settings.defaultAlertTypeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlertType.NOTIFICATION)

    val defaultAlertUri: StateFlow<String?> = settings.defaultAlertUriFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _allApps = MutableStateFlow<List<AppUsage>>(emptyList())
    val allApps: StateFlow<List<AppUsage>> = _allApps.asStateFlow()

    private val _hasUsagePermission = MutableStateFlow(helper.hasUsagePermission())
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    init {
        viewModelScope.launch { _allApps.value = helper.getInstalledApps() }
    }

    fun setTheme(theme: AppTheme) = viewModelScope.launch { settings.setTheme(theme) }

    fun toggleExcluded(packageName: String, excluded: Boolean) = viewModelScope.launch {
        settings.toggleExcluded(packageName, excluded)
    }

    fun setServiceEnabled(enabled: Boolean) = viewModelScope.launch {
        settings.setServiceEnabled(enabled)
        val app = getApplication<Application>()
        if (enabled) UsageMonitorService.startService(app)
        else app.stopService(Intent(app, UsageMonitorService::class.java))
    }

    fun setDefaultAlert(type: AlertType, uri: String?) = viewModelScope.launch {
        settings.setDefaultAlert(type, uri)
    }

    fun refreshPermission() {
        _hasUsagePermission.value = helper.hasUsagePermission()
    }
}
