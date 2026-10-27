package com.legozia.fdm.model

import java.util.UUID

data class SniffedMedia(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String,
    val mimeType: String,
    val fileSizeBytes: Long = -1L,
    val quality: String? = null,
    val isStream: Boolean = false
) {
    val formattedSize: String
        get() = if (fileSizeBytes > 0) DownloadItem.formatBytes(fileSizeBytes) else if (isStream) "Live Stream / HLS" else "Unknown Size"
}
