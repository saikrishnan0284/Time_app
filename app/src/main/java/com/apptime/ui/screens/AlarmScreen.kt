package com.apptime.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apptime.data.AlarmScope
import com.apptime.data.AlertType
import com.apptime.data.TimePeriod
import com.apptime.data.db.AlarmEntity
import com.apptime.viewmodel.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(vm: AlarmViewModel, onAddAlarm: () -> Unit, onEditAlarm: (Int) -> Unit) {
    val alarms by vm.alarms.collectAsState()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Alarms") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddAlarm,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Alarm") }
            )
        }
    ) { padding ->
        if (alarms.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AlarmOff, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text("No alarms set", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(
                        "Tap + to create one",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding(), bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { vm.toggleEnabled(alarm) },
                        onEdit = { onEditAlarm(alarm.id) },
                        onDelete = { vm.delete(alarm) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: AlarmEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val scope = runCatching { AlarmScope.valueOf(alarm.scope) }.getOrDefault(AlarmScope.SPECIFIC_APP)
    val period = runCatching { TimePeriod.valueOf(alarm.period) }.getOrDefault(TimePeriod.TODAY)
    val alertType = runCatching { AlertType.valueOf(alarm.alertType) }.getOrDefault(AlertType.NOTIFICATION)

    val h = alarm.thresholdMinutes / 60
    val m = alarm.thresholdMinutes % 60
    val thresholdStr = when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }

    val alertIcon = when (alertType) {
        AlertType.RINGTONE -> Icons.Default.VolumeUp
        AlertType.SONG -> Icons.Default.MusicNote
        AlertType.VIBRATE -> Icons.Default.Vibration
        AlertType.NOTIFICATION -> Icons.Default.Notifications
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(alarm.appName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    ScopeChip(scope)
                }
                Text(
                    "≥ $thresholdStr  ·  ${period.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(alertIcon, null, modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(
                        alertType.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Switch(checked = alarm.isEnabled, onCheckedChange = { onToggle() })
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ScopeChip(scope: AlarmScope) {
    val color = when (scope) {
        AlarmScope.SPECIFIC_APP -> MaterialTheme.colorScheme.primary
        AlarmScope.ANY_SINGLE_APP -> MaterialTheme.colorScheme.secondary
        AlarmScope.TOTAL_ALL_APPS -> MaterialTheme.colorScheme.tertiary
    }
    val label = when (scope) {
        AlarmScope.SPECIFIC_APP -> "App"
        AlarmScope.ANY_SINGLE_APP -> "Any App"
        AlarmScope.TOTAL_ALL_APPS -> "Total"
    }
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
