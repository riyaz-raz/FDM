package com.legozia.fdm

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.legozia.fdm.theme.ModernIDMTheme
import com.legozia.fdm.ui.navigation.AppNavHost
import com.legozia.fdm.viewmodel.DownloadsViewModel
import com.legozia.fdm.viewmodel.SettingsViewModel
import java.io.File

class MainActivity : ComponentActivity() {

    private val downloadsViewModel: DownloadsViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            val settings by settingsViewModel.settings.collectAsState()
            ModernIDMTheme(darkTheme = settings.darkTheme) {
                AppNavHost(
                    downloadsViewModel = downloadsViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIncomingIntent(it) }
    }

    private fun handleIncomingIntent(intent: Intent) {
        val action = intent.action
        val data = intent.dataString
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)

        val targetUrl = when (action) {
            Intent.ACTION_VIEW if !data.isNullOrBlank() -> data
            Intent.ACTION_SEND if !sharedText.isNullOrBlank() -> sharedText
            else -> null
        }

        targetUrl?.let { url ->
            val targetDir = File(settingsViewModel.settings.value.downloadDirectory)
            targetDir.mkdirs()
            val filename = url.substringAfterLast('/').substringBefore('?').ifBlank { "download_${System.currentTimeMillis()}" }
            downloadsViewModel.addDownload(url.trim(), filename, targetDir)
        }
    }
}
