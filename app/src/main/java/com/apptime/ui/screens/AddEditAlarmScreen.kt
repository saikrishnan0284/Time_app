package com.apptime.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apptime.data.AlarmScope
import com.apptime.data.AlertType
import com.apptime.data.AppUsageHelper
import com.apptime.data.TimePeriod
import com.apptime.data.db.AlarmEntity
import com.apptime.viewmodel.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmScreen(alarmVm: AlarmViewModel, editId: Int?, onBack: () -> Unit) {
    val context = LocalContext.current
    val existing = editId?.let { alarmVm.getById(it) }

    var scope by remember {
        mutableStateOf(existing?.let { runCatching { AlarmScope.valueOf(it.scope) }.getOrDefault(AlarmScope.SPECIFIC_APP) } ?: AlarmScope.SPECIFIC_APP)
    }
    var selectedPackage by remember { mutableStateOf(existing?.packageName ?: "") }
    var selectedAppName by remember {
        mutableStateOf(existing?.appName ?: "")
    }
    var hours by remember { mutableStateOf(((existing?.thresholdMinutes ?: 60) / 60).toString()) }
    var minutes by remember { mutableStateOf(((existing?.thresholdMinutes ?: 60) % 60).toString()) }
    var period by remember {
        mutableStateOf(existing?.let { runCatching { TimePeriod.valueOf(it.period) }.getOrDefault(TimePeriod.TODAY) } ?: TimePeriod.TODAY)
    }
    var alertType by remember {
        mutableStateOf(existing?.let { runCatching { AlertType.valueOf(it.alertType) }.getOrDefault(AlertType.NOTIFICATION) } ?: AlertType.NOTIFICATION)
    }
    var alertUri by remember { mutableStateOf(existing?.alertUri) }
    var showAppPicker by remember { mutableStateOf(false) }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            @Suppress("DEPRECATION")
            alertUri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)?.toString()
        }
    }
    val songPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> alertUri = uri?.toString() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editId == null) "New Alarm" else "Edit Alarm") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Alarm Type
            SectionLabel("Alarm Type")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                AlarmScope.values().forEachIndexed { index, s ->
                    SegmentedButton(
                        selected = scope == s,
                        onClick = {
                            scope = s
                            if (s != AlarmScope.SPECIFIC_APP) {
                                selectedPackage = ""
                                selectedAppName = when (s) {
                                    AlarmScope.ANY_SINGLE_APP -> "Any Single App"
                                    AlarmScope.TOTAL_ALL_APPS -> "Total Screen Time"
                                    else -> ""
                                }
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, AlarmScope.values().size)
                    ) { Text(s.displayName, maxLines = 1) }
                }
            }

            // App picker
            if (scope == AlarmScope.SPECIFIC_APP) {
                SectionLabel("Select App")
                OutlinedCard(modifier = Modifier.fillMaxWidth().clickable { showAppPicker = true }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Apps, null, modifier = Modifier.padding(end = 12.dp))
                        Text(
                            if (selectedAppName.isNotEmpty()) selectedAppName else "Tap to select an app",
                            color = if (selectedAppName.isNotEmpty()) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Threshold
            SectionLabel("Usage Limit")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it.filter(Char::isDigit).take(2) },
                    label = { Text("Hours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Text("h", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter(Char::isDigit).take(2) },
                    label = { Text("Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Text("m", style = MaterialTheme.typography.titleLarge)
            }

            // Period
            SectionLabel("Period")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TimePeriod.values().forEachIndexed { index, p ->
                    SegmentedButton(
                        selected = period == p,
                        onClick = { period = p },
                        shape = SegmentedButtonDefaults.itemShape(index, TimePeriod.values().size)
                    ) { Text(p.displayName) }
                }
            }

            // Alert Mode
            SectionLabel("Alert Mode")
            AlertTypeSelector(selected = alertType, onSelect = { alertType = it; alertUri = null })

            // URI pickers
            if (alertType == AlertType.RINGTONE) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone")
                        }
                        ringtonePickerLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MusicNote, null, modifier = Modifier.padding(end = 8.dp))
                    Text(if (alertUri != null) "✓ Ringtone selected" else "Pick Ringtone")
                }
            }
            if (alertType == AlertType.SONG) {
                OutlinedButton(
                    onClick = { songPickerLauncher.launch("audio/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AudioFile, null, modifier = Modifier.padding(end = 8.dp))
                    Text(if (alertUri != null) "✓ Song selected" else "Pick Song from Storage")
                }
            }

            Spacer(Modifier.height(8.dp))

            val thresholdMinutes = (hours.toIntOrNull() ?: 0) * 60 + (minutes.toIntOrNull() ?: 0)
            val canSave = thresholdMinutes > 0 && (scope != AlarmScope.SPECIFIC_APP || selectedPackage.isNotEmpty())

            Button(
                onClick = {
                    val alarm = AlarmEntity(
                        id = existing?.id ?: 0,
                        scope = scope.name,
                        packageName = if (scope == AlarmScope.SPECIFIC_APP) selectedPackage else null,
                        appName = selectedAppName,
                        thresholdMinutes = thresholdMinutes,
                        period = period.name,
                        alertType = alertType.name,
                        alertUri = alertUri,
                        isEnabled = existing?.isEnabled ?: true
                    )
                    if (editId == null) alarmVm.insert(alarm) else alarmVm.update(alarm)
                    onBack()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (editId == null) "Create Alarm" else "Save Changes")
            }
        }
    }

    if (showAppPicker) {
        AppPickerDialog(
            context = context,
            onSelect = { pkg, name -> selectedPackage = pkg; selectedAppName = name; showAppPicker = false },
            onDismiss = { showAppPicker = false }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun AlertTypeSelector(selected: AlertType, onSelect: (AlertType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AlertType.values().forEach { type ->
            val icon = when (type) {
                AlertType.RINGTONE -> Icons.Default.VolumeUp
                AlertType.SONG -> Icons.Default.MusicNote
                AlertType.VIBRATE -> Icons.Default.Vibration
                AlertType.NOTIFICATION -> Icons.Default.Notifications
            }
            val isSelected = selected == type
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(type) },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(icon, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    Text(type.displayName, Modifier.weight(1f))
                    if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppPickerDialog(context: Context, onSelect: (String, String) -> Unit, onDismiss: () -> Unit) {
    val apps = remember { AppUsageHelper(context).getInstalledApps() }
    var search by remember { mutableStateOf("") }
    val filtered = apps.filter { search.isBlank() || it.appName.contains(search, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select App") },
        text = {
            Column {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search apps…") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true
                )
                Column(modifier = Modifier.height(320.dp).verticalScroll(rememberScrollState())) {
                    filtered.forEach { app ->
                        ListItem(
                            headlineContent = { Text(app.appName) },
                            modifier = Modifier.clickable { onSelect(app.packageName, app.appName) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
