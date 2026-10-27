package com.legozia.fdm.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.legozia.fdm.model.DownloadCategory
import com.legozia.fdm.model.DownloadItem
import com.legozia.fdm.model.DownloadStatus
import com.legozia.fdm.theme.StatusCompleted
import com.legozia.fdm.theme.StatusDownloading
import com.legozia.fdm.theme.StatusError
import com.legozia.fdm.theme.StatusPaused

@Composable
fun DownloadCard(
    item: DownloadItem,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(targetValue = item.progress, label = "downloadProgress")
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(text = "Stop Download") },
            text = { Text(text = "Are you sure you want to stop and remove \"${item.filename}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        onCancel()
                    }
                ) {
                    Text("Stop", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon Badge
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = getCategoryContainerColor(item.category)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getCategoryIcon(item.category),
                            contentDescription = null,
                            tint = getCategoryIconColor(item.category),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.filename,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.formatProgress(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (item.status) {
                        DownloadStatus.DOWNLOADING, DownloadStatus.CONNECTING -> {
                            IconButton(onClick = onPause) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        DownloadStatus.PAUSED, DownloadStatus.QUEUED -> {
                            IconButton(onClick = onResume) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        DownloadStatus.ERROR -> {
                            IconButton(onClick = onRetry) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = StatusError)
                            }
                        }

                        DownloadStatus.COMPLETED -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = StatusCompleted,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        else -> Unit
                    }

                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Progress Bar
            AnimatedVisibility(
                visible = item.status in listOf(DownloadStatus.DOWNLOADING, DownloadStatus.CONNECTING, DownloadStatus.PAUSED)
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = when (item.status) {
                            DownloadStatus.PAUSED -> StatusPaused
                            else -> StatusDownloading
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Speed & ETA metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (item.status == DownloadStatus.DOWNLOADING) "⚡ ${item.formatSpeed()}" else item.status.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        if (item.etaSeconds > 0) {
                            Text(
                                text = "ETA: ${formatEta(item.etaSeconds)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Error info banner
            if (item.status == DownloadStatus.ERROR && !item.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = StatusError, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.errorMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusError,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun formatEta(seconds: Long): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}

@Composable
private fun getCategoryIcon(category: DownloadCategory): ImageVector {
    return when (category) {
        DownloadCategory.VIDEO -> Icons.Default.VideoFile
        DownloadCategory.AUDIO -> Icons.Default.AudioFile
        DownloadCategory.ARCHIVE -> Icons.Default.Archive
        DownloadCategory.APK -> Icons.Default.Android
        DownloadCategory.DOCUMENT -> Icons.Default.Description
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}

@Composable
private fun getCategoryContainerColor(category: DownloadCategory) = when (category) {
    DownloadCategory.VIDEO -> MaterialTheme.colorScheme.primaryContainer
    DownloadCategory.AUDIO -> MaterialTheme.colorScheme.secondaryContainer
    DownloadCategory.ARCHIVE -> MaterialTheme.colorScheme.tertiaryContainer
    else -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun getCategoryIconColor(category: DownloadCategory) = when (category) {
    DownloadCategory.VIDEO -> MaterialTheme.colorScheme.onPrimaryContainer
    DownloadCategory.AUDIO -> MaterialTheme.colorScheme.onSecondaryContainer
    DownloadCategory.ARCHIVE -> MaterialTheme.colorScheme.onTertiaryContainer
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
