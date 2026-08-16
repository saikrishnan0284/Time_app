package com.apptime.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlarmEntity::class], version = 1, exportSchema = false)
abstract class AppTimeDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile private var INSTANCE: AppTimeDatabase? = null

        fun getInstance(context: Context): AppTimeDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppTimeDatabase::class.java, "apptime_db")
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
