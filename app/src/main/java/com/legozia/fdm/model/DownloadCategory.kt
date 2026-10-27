package com.legozia.fdm.model

enum class DownloadCategory(val displayName: String) {
    ALL("All"),
    ACTIVE("Active"),
    VIDEO("Videos"),
    AUDIO("Music"),
    DOCUMENT("Documents"),
    ARCHIVE("Archives"),
    APK("Apps"),
    COMPLETED("Completed");

    companion object {
        fun fromMimeOrFilename(mimeType: String?, filename: String): DownloadCategory {
            val extension = filename.substringAfterLast('.', "").lowercase()
            return when {
                mimeType?.startsWith("video/") == true || extension in listOf("mp4", "mkv", "avi", "mov", "webm", "flv") -> VIDEO
                mimeType?.startsWith("audio/") == true || extension in listOf("mp3", "wav", "flac", "aac", "ogg", "m4a") -> AUDIO
                mimeType?.startsWith("image/") == true || extension in listOf("jpg", "jpeg", "png", "webp", "gif") -> DOCUMENT
                mimeType == "application/pdf" || extension in listOf("pdf", "doc", "docx", "txt", "epub") -> DOCUMENT
                extension in listOf("zip", "rar", "7z", "tar", "gz", "iso") -> ARCHIVE
                extension == "apk" -> APK
                else -> ALL
            }
        }
    }
}
