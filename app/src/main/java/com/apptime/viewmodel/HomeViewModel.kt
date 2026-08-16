package com.apptime.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apptime.data.AppUsage
import com.apptime.data.AppUsageHelper
import com.apptime.data.TimePeriod
import com.apptime.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val helper = AppUsageHelper(app)
    private val settings = SettingsDataStore(app)

    private val _selectedPeriod = MutableStateFlow(TimePeriod.TODAY)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    private val _usageList = MutableStateFlow<List<AppUsage>>(emptyList())
    val usageList: StateFlow<List<AppUsage>> = _usageList.asStateFlow()

    private val _totalMs = MutableStateFlow(0L)
    val totalMs: StateFlow<Long> = _totalMs.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_selectedPeriod, settings.excludedPackagesFlow) { period, excluded ->
                Pair(period, excluded)
            }.collectLatest { (period, excluded) ->
                loadUsage(period, excluded)
            }
        }
    }

    fun checkPermission() {
        _hasPermission.value = helper.hasUsagePermission()
    }

    fun selectPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
    }

    private suspend fun loadUsage(period: TimePeriod, excluded: Set<String>) {
        _isLoading.value = true
        val list = helper.getUsageStats(period, excluded)
        _usageList.value = list
        _totalMs.value = list.sumOf { it.totalTimeMs }
        _isLoading.value = false
    }

    fun refresh() {
        checkPermission()
        viewModelScope.launch {
            val excluded = settings.excludedPackagesFlow.first()
            loadUsage(_selectedPeriod.value, excluded)
        }
    }
}
