package com.legozia.fdm.model

import java.util.UUID

data class DownloadItem(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val filename: String,
    val destinationPath: String,
    val totalBytes: Long = -1L,
    val downloadedBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val category: DownloadCategory = DownloadCategory.ALL,
    val mimeType: String? = null,
    val threadCount: Int = 8,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val progress: Float
        get() = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val etaSeconds: Long
        get() = if (speedBytesPerSec > 0 && totalBytes > downloadedBytes) {
            (totalBytes - downloadedBytes) / speedBytesPerSec
        } else -1L

    fun formatProgress(): String {
        return if (totalBytes > 0) {
            "${formatBytes(downloadedBytes)} / ${formatBytes(totalBytes)}"
        } else {
            formatBytes(downloadedBytes)
        }
    }

    fun formatSpeed(): String {
        return if (status == DownloadStatus.DOWNLOADING && speedBytesPerSec > 0) {
            "${formatBytes(speedBytesPerSec)}/s"
        } else {
            "--"
        }
    }

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
            return String.format("%.1f %s", value, units[digitGroups.coerceIn(0, units.size - 1)])
        }
    }
}
