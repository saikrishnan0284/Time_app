package com.apptime.ui.screens

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apptime.data.AlertType
import com.apptime.data.AppTheme
import com.apptime.ui.theme.getColorScheme
import com.apptime.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    onNavigateToAppManagement: () -> Unit
) {
    val context = LocalContext.current
    val theme by vm.theme.collectAsState()
    val excluded by vm.excludedPackages.collectAsState()
    val allApps by vm.allApps.collectAsState()
    val hasPermission by vm.hasUsagePermission.collectAsState()
    val serviceEnabled by vm.serviceEnabled.collectAsState()
    val defaultAlertType by vm.defaultAlertType.collectAsState()
    val defaultAlertUri by vm.defaultAlertUri.collectAsState()

    var showThemeSheet by remember { mutableStateOf(false) }
    var showAlertModeSheet by remember { mutableStateOf(false) }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            @Suppress("DEPRECATION")
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            vm.setDefaultAlert(defaultAlertType, uri?.toString())
        }
    }

    val songPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) vm.setDefaultAlert(defaultAlertType, uri.toString())
    }

    LaunchedEffect(Unit) { vm.refreshPermission() }

    if (showThemeSheet) {
        ModalBottomSheet(onDismissRequest = { showThemeSheet = false }) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp)) {
                Text(
                    "Choose Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                ThemeGrid(selectedTheme = theme, onSelect = { vm.setTheme(it); showThemeSheet = false })
            }
        }
    }

    if (showAlertModeSheet) {
        ModalBottomSheet(onDismissRequest = { showAlertModeSheet = false }) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp)) {
                Text(
                    "Default Alert Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                AlertType.values().forEach { alertType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val keepUri = if (alertType == defaultAlertType) defaultAlertUri else null
                                vm.setDefaultAlert(alertType, keepUri)
                                showAlertModeSheet = false
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = defaultAlertType == alertType,
                            onClick = {
                                val keepUri = if (alertType == defaultAlertType) defaultAlertUri else null
                                vm.setDefaultAlert(alertType, keepUri)
                                showAlertModeSheet = false
                            }
                        )
                        Column {
                            Text(alertType.displayName, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                when (alertType) {
                                    AlertType.RINGTONE    -> "Plays a ringtone when limit is reached"
                                    AlertType.SONG        -> "Plays your chosen music file"
                                    AlertType.VIBRATE     -> "Vibrates the phone silently"
                                    AlertType.NOTIFICATION -> "Shows a silent notification"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }

    LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
        item { CenterAlignedTopAppBar(title = { Text("Settings") }) }

        // ── MONITORING ──────────────────────────────────────────────────────────
        item { SettingsSectionHeader("Monitoring") }
        item {
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.Sync, null, tint = MaterialTheme.colorScheme.primary)
                },
                headlineContent = { Text("Background Monitoring") },
                supportingContent = {
                    Text(if (serviceEnabled) "Tracking app usage in the background" else "Monitoring is paused")
                },
                trailingContent = {
                    Switch(checked = serviceEnabled, onCheckedChange = { vm.setServiceEnabled(it) })
                }
            )
        }
        item {
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.Apps, null, tint = MaterialTheme.colorScheme.primary)
                },
                headlineContent = { Text("Manage Tracked Apps") },
                supportingContent = {
                    Text("${allApps.size - excluded.size} of ${allApps.size} apps tracked")
                },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                modifier = Modifier.clickable { onNavigateToAppManagement() }
            )
        }
        item { HorizontalDivider(Modifier.padding(horizontal = 16.dp)) }

        // ── DEFAULT ALERT ───────────────────────────────────────────────────────
        item { SettingsSectionHeader("Default Alert") }
        item {
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                },
                headlineContent = { Text("Alert Mode") },
                supportingContent = { Text(defaultAlertType.displayName) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                modifier = Modifier.clickable { showAlertModeSheet = true }
            )
        }
        if (defaultAlertType == AlertType.RINGTONE || defaultAlertType == AlertType.SONG) {
            item {
                val soundLabel = when {
                    defaultAlertUri != null -> {
                        if (defaultAlertType == AlertType.RINGTONE) {
                            runCatching {
                                RingtoneManager.getRingtone(context, Uri.parse(defaultAlertUri))
                                    ?.getTitle(context) ?: "Custom Sound"
                            }.getOrDefault("Custom Sound")
                        } else "Custom Song"
                    }
                    defaultAlertType == AlertType.RINGTONE -> "Default Ringtone"
                    else -> "No song selected"
                }
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.primary)
                    },
                    headlineContent = {
                        Text(if (defaultAlertType == AlertType.RINGTONE) "Ringtone" else "Song File")
                    },
                    supportingContent = { Text(soundLabel) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                    modifier = Modifier.clickable {
                        if (defaultAlertType == AlertType.RINGTONE) {
                            val current = defaultAlertUri?.let { Uri.parse(it) }
                                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            ringtonePickerLauncher.launch(
                                Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, current)
                                }
                            )
                        } else {
                            songPickerLauncher.launch("audio/*")
                        }
                    }
                )
            }
        }
        item { HorizontalDivider(Modifier.padding(horizontal = 16.dp)) }

        // ── APPEARANCE ──────────────────────────────────────────────────────────
        item { SettingsSectionHeader("Appearance") }
        item {
            val cs = getColorScheme(theme)
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary)
                },
                headlineContent = { Text("App Theme") },
                supportingContent = { Text(theme.displayName) },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(cs.background, cs.surface, cs.primary).forEach { color ->
                            Box(Modifier.size(14.dp).clip(CircleShape).background(color))
                        }
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, null)
                    }
                },
                modifier = Modifier.clickable { showThemeSheet = true }
            )
        }
        item { HorizontalDivider(Modifier.padding(horizontal = 16.dp)) }

        // ── PERMISSIONS ─────────────────────────────────────────────────────────
        item { SettingsSectionHeader("Permissions") }
        item {
            PermissionListItem(
                icon = Icons.Default.BarChart,
                label = "Usage Access",
                description = "Required to read app screen time",
                granted = hasPermission,
                onManage = {
                    context.startActivity(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                    vm.refreshPermission()
                }
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            item {
                val notifGranted = remember {
                    context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                            android.content.pm.PackageManager.PERMISSION_GRANTED
                }
                PermissionListItem(
                    icon = Icons.Default.NotificationsActive,
                    label = "Post Notifications",
                    description = "Required to show alarm alerts",
                    granted = notifGranted,
                    onManage = {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        )
                    }
                )
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun PermissionListItem(
    icon: ImageVector,
    label: String,
    description: String,
    granted: Boolean,
    onManage: () -> Unit
) {
    ListItem(
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        headlineContent = { Text(label) },
        supportingContent = { Text(description) },
        trailingContent = {
            if (granted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle, null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                    Text("Granted", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                }
            } else {
                FilledTonalButton(
                    onClick = onManage,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) { Text("Grant") }
            }
        }
    )
}

@Composable
private fun ThemeGrid(selectedTheme: AppTheme, onSelect: (AppTheme) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppTheme.values().toList().chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { appTheme ->
                    val cs = getColorScheme(appTheme)
                    val isSelected = selectedTheme == appTheme
                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelect(appTheme) },
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(cs.background, cs.surface, cs.primary).forEach { color ->
                                    Box(
                                        Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(color)
                                    )
                                }
                            }
                            Text(
                                appTheme.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle, null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
