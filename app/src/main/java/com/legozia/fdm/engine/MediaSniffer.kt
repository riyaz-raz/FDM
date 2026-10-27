package com.legozia.fdm.engine

import android.util.Log
import com.legozia.fdm.model.SniffedMedia
import java.net.URI

class MediaSniffer {

    private val mediaExtensions = mapOf(
        "mp4" to ("video/mp4" to false),
        "mkv" to ("video/x-matroska" to false),
        "webm" to ("video/webm" to false),
        "mov" to ("video/quicktime" to false),
        "m3u8" to ("application/x-mpegURL" to true),
        "mpd" to ("application/dash+xml" to true),
        "mp3" to ("audio/mpeg" to false),
        "m4a" to ("audio/mp4" to false),
        "flac" to ("audio/flac" to false),
        "wav" to ("audio/wav" to false),
        "zip" to ("application/zip" to false),
        "pdf" to ("application/pdf" to false),
        "apk" to ("application/vnd.android.package-archive" to false)
    )

    fun inspectUrl(url: String, pageTitle: String? = null): SniffedMedia? {
        val cleanUrl = url.split("?").firstOrNull() ?: url
        val extension = cleanUrl.substringAfterLast('.', "").lowercase()

        val mediaInfo = mediaExtensions[extension]
        if (mediaInfo != null) {
            val (mimeType, isStream) = mediaInfo
            val derivedTitle = cleanUrl.substringAfterLast('/')
                .ifBlank { pageTitle ?: "media_file.$extension" }
            
            val quality = when {
                url.contains("1080") -> "1080p"
                url.contains("720") -> "720p"
                url.contains("480") -> "480p"
                isStream -> "Adaptive"
                else -> null
            }

            return SniffedMedia(
                url = url,
                title = derivedTitle,
                mimeType = mimeType,
                isStream = isStream,
                quality = quality
            )
        }
        return null
    }

    fun extractFilename(url: String, contentDisposition: String?): String {
        if (!contentDisposition.isNullOrBlank() && contentDisposition.contains("filename=")) {
            val match = Regex("filename=[\"']?([^\"';]+)[\"']?").find(contentDisposition)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return try {
            val path = URI(url).path
            val rawName = path.substringAfterLast('/')
            if (rawName.isNotBlank() && rawName.contains('.')) rawName else "download_${System.currentTimeMillis()}"
        } catch (e: Exception) {
            Log.e("EXTRACT_FILE_NAME", "extractFilename: $e" )
            "download_${System.currentTimeMillis()}"
        }
    }
}
