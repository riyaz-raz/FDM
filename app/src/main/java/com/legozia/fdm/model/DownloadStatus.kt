package com.legozia.fdm.model

enum class DownloadStatus {
    QUEUED,
    CONNECTING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    ERROR,
    CANCELLED
}
