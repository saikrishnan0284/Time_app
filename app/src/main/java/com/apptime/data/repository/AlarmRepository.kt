package com.apptime.data.repository

import com.apptime.data.db.AlarmDao
import com.apptime.data.db.AlarmEntity
import kotlinx.coroutines.flow.Flow

class AlarmRepository(private val dao: AlarmDao) {
    val allAlarms: Flow<List<AlarmEntity>> = dao.getAllAlarms()
    suspend fun insert(alarm: AlarmEntity): Long = dao.insertAlarm(alarm)
    suspend fun update(alarm: AlarmEntity) = dao.updateAlarm(alarm)
    suspend fun delete(alarm: AlarmEntity) = dao.deleteAlarm(alarm)
    suspend fun getActiveAlarms(): List<AlarmEntity> = dao.getActiveAlarms()
    suspend fun setTriggered(id: Int, ts: Long) = dao.setTriggered(id, ts)
    suspend fun resetPeriodAlarms(period: String) = dao.resetPeriodAlarms(period)
}
