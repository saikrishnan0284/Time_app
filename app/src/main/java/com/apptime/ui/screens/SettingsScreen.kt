package com.apptime.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.apptime.data.AppTheme
import com.apptime.ui.theme.getColorScheme
import com.apptime.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val context = LocalContext.current
    val theme by vm.theme.collectAsState()
    val excluded by vm.excludedPackages.collectAsState()
    val allApps by vm.allApps.collectAsState()
    val hasPermission by vm.hasUsagePermission.collectAsState()
    var showAppToggles by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.refreshPermission() }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CenterAlignedTopAppBar(title = { Text("Settings") })
        }

        // Permissions
        item {
            SectionCard(title = "Permissions", icon = Icons.Default.Security) {
                PermissionRow(
                    label = "Usage Access",
                    description = "Required to read app usage statistics",
                    granted = hasPermission,
                    onManage = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                        vm.refreshPermission()
                    }
                )
            }
        }

        // Theme
        item {
            SectionCard(title = "Theme", icon = Icons.Default.Palette) {
                ThemeGrid(selectedTheme = theme, onSelect = { vm.setTheme(it) })
            }
        }

        // Tracked apps header
        item {
            SectionCard(title = "Tracked Apps", icon = Icons.Default.Apps) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("App Visibility", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${allApps.size - excluded.size} of ${allApps.size} apps tracked",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    TextButton(onClick = { showAppToggles = !showAppToggles }) {
                        Text(if (showAppToggles) "Collapse" else "Manage")
                    }
                }
            }
        }

        // App toggles (expanded)
        if (showAppToggles) {
            items(allApps) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(app.appName, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = app.packageName !in excluded,
                        onCheckedChange = { tracked -> vm.toggleExcluded(app.packageName, !tracked) }
                    )
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun SectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
private fun PermissionRow(label: String, description: String, granted: Boolean, onManage: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        if (granted) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                Text("Granted", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
            }
        } else {
            FilledTonalButton(onClick = onManage) { Text("Grant") }
        }
    }
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
                        modifier = Modifier.weight(1f).clickable { onSelect(appTheme) },
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(cs.background, cs.surface, cs.primary).forEach { color ->
                                    Box(
                                        Modifier.size(18.dp).clip(RoundedCornerShape(4.dp))
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
