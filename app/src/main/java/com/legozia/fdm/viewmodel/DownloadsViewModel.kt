package com.legozia.fdm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legozia.fdm.engine.DownloadEngine
import com.legozia.fdm.model.DownloadCategory
import com.legozia.fdm.model.DownloadItem
import com.legozia.fdm.model.DownloadStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.io.File

class DownloadsViewModel(
    private val engine: DownloadEngine = DownloadEngine()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(DownloadCategory.ALL)
    val selectedCategory = _selectedCategory.asStateFlow()

    val filteredDownloads: StateFlow<List<DownloadItem>> = combine(
        engine.downloads,
        _searchQuery,
        _selectedCategory
    ) { downloads, query, category ->
        downloads.filter { item ->
            val matchesQuery = query.isBlank() || item.filename.contains(query, ignoreCase = true)
            val matchesCategory = when (category) {
                DownloadCategory.ALL -> true
                DownloadCategory.ACTIVE -> item.status in listOf(DownloadStatus.DOWNLOADING, DownloadStatus.CONNECTING, DownloadStatus.QUEUED)
                DownloadCategory.COMPLETED -> item.status == DownloadStatus.COMPLETED
                else -> item.category == category
            }
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDownloadSpeed: StateFlow<Long> = engine.downloads.combine(_selectedCategory) { list, _ ->
        list.filter { it.status == DownloadStatus.DOWNLOADING }.sumOf { it.speedBytesPerSec }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: DownloadCategory) {
        _selectedCategory.value = category
    }

    fun addDownload(url: String, filename: String, destinationDir: File, threads: Int = 8) {
        engine.enqueueDownload(url, filename, destinationDir, threads)
    }

    fun pauseDownload(id: String) = engine.pauseDownload(id)
    fun resumeDownload(id: String) = engine.resumeDownload(id)
    fun cancelDownload(id: String, deleteFile: Boolean = false) = engine.cancelDownload(id, deleteFile)
    fun retryDownload(id: String) = engine.retryDownload(id)
}
