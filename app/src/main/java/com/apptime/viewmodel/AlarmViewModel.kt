package com.apptime.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apptime.data.db.AlarmEntity
import com.apptime.data.db.AppTimeDatabase
import com.apptime.data.repository.AlarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AlarmRepository(AppTimeDatabase.getInstance(app).alarmDao())

    val alarms: StateFlow<List<AlarmEntity>> = repo.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insert(alarm: AlarmEntity) = viewModelScope.launch { repo.insert(alarm) }
    fun update(alarm: AlarmEntity) = viewModelScope.launch { repo.update(alarm) }
    fun delete(alarm: AlarmEntity) = viewModelScope.launch { repo.delete(alarm) }
    fun toggleEnabled(alarm: AlarmEntity) = viewModelScope.launch {
        repo.update(alarm.copy(isEnabled = !alarm.isEnabled))
    }
    fun getById(id: Int): AlarmEntity? = alarms.value.find { it.id == id }
}
