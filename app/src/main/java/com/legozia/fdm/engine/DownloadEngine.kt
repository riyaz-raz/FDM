package com.legozia.fdm.engine

import com.legozia.fdm.model.DownloadCategory
import com.legozia.fdm.model.DownloadItem
import com.legozia.fdm.model.DownloadStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds

class DownloadEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val chunkDownloader: ChunkDownloader = ChunkDownloader()
) {

    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()

    private val runningJobs = ConcurrentHashMap<String, Job>()
    private val speedTrackers = ConcurrentHashMap<String, AtomicLong>()

    init {
        // Speed calculation ticker
        scope.launch {
            while (isActive) {
                delay(1000.milliseconds)
                updateSpeeds()
            }
        }
    }

    fun enqueueDownload(
        url: String,
        filename: String,
        destinationDir: File,
        threadCount: Int = 8
    ): String {
        val targetFile = File(destinationDir, filename)
        val category = DownloadCategory.fromMimeOrFilename(null, filename)

        val item = DownloadItem(
            url = url,
            filename = filename,
            destinationPath = targetFile.absolutePath,
            category = category,
            threadCount = threadCount,
            status = DownloadStatus.QUEUED
        )

        _downloads.value += item
        startDownload(item.id)
        return item.id
    }

    fun startDownload(id: String) {
        val currentItem = _downloads.value.find { it.id == id } ?: return
        if (currentItem.status == DownloadStatus.DOWNLOADING) return

        updateItem(id) { it.copy(status = DownloadStatus.CONNECTING) }

        val job = scope.launch {
            try {
                val (contentLength, _) = chunkDownloader.getRemoteFileInfo(currentItem.url)
                val targetFile = File(currentItem.destinationPath)
                targetFile.parentFile?.mkdirs()

                val startOffset = if (targetFile.exists()) targetFile.length() else 0L

                updateItem(id) {
                    it.copy(
                        totalBytes = contentLength,
                        downloadedBytes = startOffset,
                        status = DownloadStatus.DOWNLOADING
                    )
                }

                val speedCounter = AtomicLong(0)
                speedTrackers[id] = speedCounter

                chunkDownloader.downloadChunk(
                    url = currentItem.url,
                    outputFile = targetFile,
                    startByte = startOffset,
                    endByte = contentLength
                ) { bytesRead ->
                    speedCounter.addAndGet(bytesRead.toLong())
                    updateItem(id) { item ->
                        item.copy(downloadedBytes = item.downloadedBytes + bytesRead)
                    }
                }

                ensureActive()

                updateItem(id) {
                    it.copy(
                        status = DownloadStatus.COMPLETED,
                        downloadedBytes = it.totalBytes,
                        speedBytesPerSec = 0
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                if (isActive) {
                    updateItem(id) {
                        it.copy(
                            status = DownloadStatus.ERROR,
                            errorMessage = e.localizedMessage ?: "Download failed",
                            speedBytesPerSec = 0
                        )
                    }
                }
            } finally {
                runningJobs.remove(id)
                speedTrackers.remove(id)
            }
        }

        runningJobs[id] = job
    }

    fun pauseDownload(id: String) {
        runningJobs[id]?.cancel()
        runningJobs.remove(id)
        speedTrackers.remove(id)
        updateItem(id) { it.copy(status = DownloadStatus.PAUSED, speedBytesPerSec = 0) }
    }

    fun resumeDownload(id: String) {
        startDownload(id)
    }

    fun cancelDownload(id: String, deleteFile: Boolean = false) {
        pauseDownload(id)
        val item = _downloads.value.find { it.id == id }
        if (deleteFile && item != null) {
            File(item.destinationPath).delete()
        }
        _downloads.value = _downloads.value.filterNot { it.id == id }
    }

    fun retryDownload(id: String) {
        startDownload(id)
    }

    private fun updateSpeeds() {
        _downloads.value = _downloads.value.map { item ->
            if (item.status == DownloadStatus.DOWNLOADING) {
                val counter = speedTrackers[item.id]
                val speed = counter?.getAndSet(0L) ?: 0L
                item.copy(speedBytesPerSec = speed)
            } else {
                item.copy(speedBytesPerSec = 0L)
            }
        }
    }

    private fun updateItem(id: String, transform: (DownloadItem) -> DownloadItem) {
        _downloads.value = _downloads.value.map {
            if (it.id == id) transform(it) else it
        }
    }
}
