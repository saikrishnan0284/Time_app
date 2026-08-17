package com.apptime.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.apptime.data.AlertType
import com.apptime.data.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_EXCLUDED = stringSetPreferencesKey("excluded_packages")
        private val KEY_SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
        private val KEY_DEFAULT_ALERT = stringPreferencesKey("default_alert")
        private val KEY_DEFAULT_ALERT_URI = stringPreferencesKey("default_alert_uri")
    }

    val themeFlow: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        runCatching { AppTheme.valueOf(prefs[KEY_THEME] ?: AppTheme.DARK.name) }.getOrDefault(AppTheme.DARK)
    }

    val excludedPackagesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_EXCLUDED] ?: emptySet()
    }

    val serviceEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SERVICE_ENABLED] ?: true
    }

    val defaultAlertTypeFlow: Flow<AlertType> = context.dataStore.data.map { prefs ->
        runCatching { AlertType.valueOf(prefs[KEY_DEFAULT_ALERT] ?: AlertType.NOTIFICATION.name) }
            .getOrDefault(AlertType.NOTIFICATION)
    }

    val defaultAlertUriFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_ALERT_URI]
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

    suspend fun setServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SERVICE_ENABLED] = enabled }
    }

    suspend fun setDefaultAlert(type: AlertType, uri: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_ALERT] = type.name
            if (uri != null) prefs[KEY_DEFAULT_ALERT_URI] = uri
            else prefs.remove(KEY_DEFAULT_ALERT_URI)
        }
    }
}
