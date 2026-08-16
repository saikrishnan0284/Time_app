package com.apptime.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scope: String,
    val packageName: String?,
    val appName: String,
    val thresholdMinutes: Int,
    val period: String,
    val alertType: String,
    val alertUri: String? = null,
    val isEnabled: Boolean = true,
    val lastTriggeredAt: Long = 0L
)
