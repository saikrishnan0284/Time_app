package com.apptime.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.apptime.data.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_EXCLUDED = stringSetPreferencesKey("excluded_packages")
    }

    val themeFlow: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        runCatching { AppTheme.valueOf(prefs[KEY_THEME] ?: AppTheme.DARK.name) }.getOrDefault(AppTheme.DARK)
    }

    val excludedPackagesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_EXCLUDED] ?: emptySet()
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[KEY_THEME] = theme.name }
    }

    suspend fun toggleExcluded(packageName: String, excluded: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_EXCLUDED]?.toMutableSet() ?: mutableSetOf()
            if (excluded) current.add(packageName) else current.remove(packageName)
            prefs[KEY_EXCLUDED] = current
        }
    }
}
