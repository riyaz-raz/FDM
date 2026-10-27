package com.legozia.fdm.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsState(
    val maxConcurrentDownloads: Int = 3,
    val maxChunksPerDownload: Int = 8,
    val downloadDirectory: String = "/storage/emulated/0/Download/FDM",
    val wifiOnly: Boolean = false,
    val darkTheme: Boolean = true,
    val autoResumeOnError: Boolean = true,
    val adBlockerEnabled: Boolean = true,
    val javaScriptEnabled: Boolean = true,
    val searchEngineName: String = "DuckDuckGo",
    val searchEngineUrl: String = "https://duckduckgo.com/?q=",
    val customSearchEngineUrl: String = "https://search.brave.com/search?q="
)

class SettingsViewModel : ViewModel() {
    private val _settings = MutableStateFlow(SettingsState())
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    fun updateMaxConcurrent(count: Int) {
        _settings.value = _settings.value.copy(maxConcurrentDownloads = count)
    }

    fun updateMaxChunks(count: Int) {
        _settings.value = _settings.value.copy(maxChunksPerDownload = count)
    }

    fun toggleWifiOnly(enabled: Boolean) {
        _settings.value = _settings.value.copy(wifiOnly = enabled)
    }

    fun toggleDarkTheme(enabled: Boolean) {
        _settings.value = _settings.value.copy(darkTheme = enabled)
    }

    fun toggleAdBlocker(enabled: Boolean) {
        _settings.value = _settings.value.copy(adBlockerEnabled = enabled)
    }

    fun toggleJavaScript(enabled: Boolean) {
        _settings.value = _settings.value.copy(javaScriptEnabled = enabled)
    }

    fun updateDownloadDir(path: String) {
        _settings.value = _settings.value.copy(downloadDirectory = path)
    }

    fun updateSearchEngine(name: String, url: String) {
        _settings.value = _settings.value.copy(
            searchEngineName = name,
            searchEngineUrl = if (name == "Custom") _settings.value.customSearchEngineUrl else url
        )
    }

    fun updateCustomSearchUrl(url: String) {
        _settings.value = _settings.value.copy(
            customSearchEngineUrl = url,
            searchEngineUrl = if (_settings.value.searchEngineName == "Custom") url else _settings.value.searchEngineUrl
        )
    }
}
