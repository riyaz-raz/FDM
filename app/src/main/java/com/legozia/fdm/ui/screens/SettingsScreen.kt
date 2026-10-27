package com.legozia.fdm.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.legozia.fdm.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "App and engine preferences",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Performance & Engine
        SettingsSectionHeader(title = "Download Performance")

        SettingsCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Connection Threads", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${settings.maxChunksPerDownload} parts per download",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = settings.maxChunksPerDownload.toFloat(),
                    onValueChange = { viewModel.updateMaxChunks(it.toInt()) },
                    valueRange = 1f..32f,
                    steps = 30
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Network & Search
        SettingsSectionHeader(title = "Network & Search Engine")

        SettingsCard {
            Column {
                SettingsSwitchRow(
                    icon = Icons.Default.Wifi,
                    title = "Download over Wi-Fi only",
                    subtitle = "Pause tasks automatically when on mobile data",
                    checked = settings.wifiOnly,
                    onCheckedChange = { viewModel.toggleWifiOnly(it) }
                )

                SettingsSwitchRow(
                    icon = Icons.Default.Block,
                    title = "Built-in Ad-Blocker",
                    subtitle = "Block ads and trackers in the integrated browser",
                    checked = settings.adBlockerEnabled,
                    onCheckedChange = { viewModel.toggleAdBlocker(it) }
                )

                SettingsSwitchRow(
                    icon = Icons.Default.Code,
                    title = "Enable JavaScript",
                    subtitle = "Allow web pages to run scripts in the built-in browser",
                    checked = settings.javaScriptEnabled,
                    onCheckedChange = { viewModel.toggleJavaScript(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Default Search Engine
        SettingsSectionHeader(title = "Search Engine")

        SettingsCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Default Search Engine", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Select default engine or enter custom search URL",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val searchEngines = listOf(
                    "DuckDuckGo" to "https://duckduckgo.com/?q=",
                    "Google" to "https://www.google.com/search?q=",
                    "Bing" to "https://www.bing.com/search?q=",
                    "Yahoo" to "https://search.yahoo.com/search?p=",
                    "Startpage" to "https://www.startpage.com/sp/search?query=",
                    "Ecosia" to "https://www.ecosia.org/search?q=",
                    "Custom" to settings.customSearchEngineUrl
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    searchEngines.forEach { (name, url) ->
                        val isSelected = settings.searchEngineName == name
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateSearchEngine(name, url) },
                            label = { Text(name, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                AnimatedVisibility(visible = settings.searchEngineName == "Custom") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = settings.customSearchEngineUrl,
                            onValueChange = { viewModel.updateCustomSearchUrl(it) },
                            label = { Text("Custom Search URL") },
                            placeholder = { Text("https://search.brave.com/search?q=") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Use %s as query placeholder or end with search parameter (e.g. ?q=)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Storage
        SettingsSectionHeader(title = "Storage & Path")

        var showDirDialog by remember { mutableStateOf(false) }
        val folderPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            uri?.let {
                val rawPath = it.path?.substringAfter("primary:")?.let { p -> "/storage/emulated/0/$p" } ?: it.path ?: it.toString()
                viewModel.updateDownloadDir(rawPath)
            }
        }

        if (showDirDialog) {
            var tempPath by remember { mutableStateOf(settings.downloadDirectory) }
            AlertDialog(
                onDismissRequest = { showDirDialog = false },
                title = { Text("Change Download Directory") },
                text = {
                    Column {
                        Text("Enter target path or choose a preset location:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = tempPath,
                            onValueChange = { tempPath = it },
                            label = { Text("Directory Path") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Presets:", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        val presets = listOf(
                            "Public Downloads" to "/storage/emulated/0/Download",
                            "FDM Downloads" to "/storage/emulated/0/Download/FDM",
                            "Documents" to "/storage/emulated/0/Documents",
                            "Movies" to "/storage/emulated/0/Movies"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presets.forEach { (name, path) ->
                                FilterChip(
                                    selected = tempPath == path,
                                    onClick = { tempPath = path },
                                    label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (tempPath.isNotBlank()) {
                                viewModel.updateDownloadDir(tempPath.trim())
                            }
                            showDirDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDirDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDirDialog = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Download Directory", style = MaterialTheme.typography.titleSmall)
                    Text(
                        settings.downloadDirectory,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { folderPickerLauncher.launch(null) }) {
                    Icon(Icons.Default.FolderOpen, contentDescription = "Choose Folder", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Appearance
        SettingsSectionHeader(title = "Appearance")

        SettingsCard {
            SettingsSwitchRow(
                icon = Icons.Default.DarkMode,
                title = "Dark Theme",
                subtitle = "Use sleek dark mode with Material You accent colors",
                checked = settings.darkTheme,
                onCheckedChange = { viewModel.toggleDarkTheme(it) }
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
