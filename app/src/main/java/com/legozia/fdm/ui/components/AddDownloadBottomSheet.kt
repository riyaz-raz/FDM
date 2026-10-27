package com.legozia.fdm.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.legozia.fdm.engine.MediaSniffer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDownloadBottomSheet(
    onDismiss: () -> Unit,
    onStartDownload: (url: String, filename: String, dirPath: String, threads: Int) -> Unit,
    initialUrl: String = "",
    initialDirectory: String = "/storage/emulated/0/Download/FDM",
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sniffer = remember { MediaSniffer() }

    var url by remember { mutableStateOf(initialUrl) }
    var filename by remember {
        mutableStateOf(if (initialUrl.isNotBlank()) sniffer.extractFilename(initialUrl, null) else "")
    }
    var downloadDir by remember { mutableStateOf(initialDirectory) }
    var threadsCount by remember { mutableFloatStateOf(8f) }
    var showAdvanced by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "New Download",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // URL input
            OutlinedTextField(
                value = url,
                onValueChange = {
                    url = it
                    if (filename.isBlank() || filename.startsWith("download_")) {
                        filename = sniffer.extractFilename(it, null)
                    }
                },
                label = { Text("Download Link / URL") },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filename input
            OutlinedTextField(
                value = filename,
                onValueChange = { filename = it },
                label = { Text("File Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Collapsible Advanced Section
            TextButton(
                onClick = { showAdvanced = !showAdvanced },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(if (showAdvanced) "Hide Advanced Settings" else "Advanced Settings")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = showAdvanced) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = downloadDir,
                        onValueChange = { downloadDir = it },
                        label = { Text("Save Path") },
                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Connection Threads: ${threadsCount.toInt()} parts",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = threadsCount,
                        onValueChange = { threadsCount = it },
                        valueRange = 1f..32f,
                        steps = 30
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (url.isNotBlank()) {
                            val finalName = filename.ifBlank { sniffer.extractFilename(url, null) }
                            onStartDownload(url.trim(), finalName.trim(), downloadDir.trim(), threadsCount.toInt())
                            onDismiss()
                        }
                    },
                    enabled = url.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Start Download")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
