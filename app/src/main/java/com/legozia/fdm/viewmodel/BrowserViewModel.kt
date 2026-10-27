package com.legozia.fdm.viewmodel

import androidx.lifecycle.ViewModel
import com.legozia.fdm.engine.MediaSniffer
import com.legozia.fdm.model.SniffedMedia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URI

class BrowserViewModel(
    private val sniffer: MediaSniffer = MediaSniffer()
) : ViewModel() {

    private val _currentUrl = MutableStateFlow<String?>(null)
    val currentUrl: StateFlow<String?> = _currentUrl.asStateFlow()

    private val _pageTitle = MutableStateFlow("")
    val pageTitle: StateFlow<String> = _pageTitle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _capturedMediaList = MutableStateFlow<List<SniffedMedia>>(emptyList())
    val capturedMediaList: StateFlow<List<SniffedMedia>> = _capturedMediaList.asStateFlow()

    private var hasUserNavigated = false

    fun getHomeUrl(searchEngineUrl: String): String {
        return try {
            val uri = URI(searchEngineUrl)
            val scheme = uri.scheme ?: "https"
            val host = uri.host ?: "duckduckgo.com"
            "$scheme://$host"
        } catch (e: Exception) {
            "https://duckduckgo.com"
        }
    }

    fun syncSearchEngineHome(searchEngineUrl: String) {
        if (!hasUserNavigated || _currentUrl.value == null) {
            _currentUrl.value = getHomeUrl(searchEngineUrl)
        }
    }

    fun navigateTo(url: String, searchEngineUrl: String = "https://duckduckgo.com/?q=") {
        hasUserNavigated = true
        val trimmed = url.trim()
        val formatted = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            if (trimmed.contains(".") && !trimmed.contains(" ")) {
                "https://$trimmed"
            } else {
                val encodedQuery = java.net.URLEncoder.encode(trimmed, "UTF-8")
                if (searchEngineUrl.contains("%s")) {
                    searchEngineUrl.replace("%s", encodedQuery)
                } else {
                    "$searchEngineUrl$encodedQuery"
                }
            }
        } else {
            trimmed
        }
        _currentUrl.value = formatted
        _capturedMediaList.value = emptyList() // Clear sniffed media on new page
    }

    fun onPageStarted() {
        _isLoading.value = true
    }

    fun onPageFinished(title: String?, loadedUrl: String? = null) {
        _isLoading.value = false
        if (title != null) _pageTitle.value = title
        if (!loadedUrl.isNullOrBlank() && hasUserNavigated) {
            _currentUrl.value = loadedUrl
        }
    }

    fun onResourceIntercepted(resourceUri: String) {
        val sniffed = sniffer.inspectUrl(resourceUri, _pageTitle.value)
        if (sniffed != null) {
            val exists = _capturedMediaList.value.any { it.url == sniffed.url }
            if (!exists) {
                _capturedMediaList.value = _capturedMediaList.value + sniffed
            }
        }
    }

    fun clearCapturedMedia() {
        _capturedMediaList.value = emptyList()
    }
}
