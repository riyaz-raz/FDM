package com.legozia.fdm.ui.screens

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.legozia.fdm.ui.components.SniffedMediaBottomSheet
import com.legozia.fdm.viewmodel.BrowserViewModel
import com.legozia.fdm.viewmodel.DownloadsViewModel
import com.legozia.fdm.viewmodel.SettingsViewModel
import java.io.File

@Composable
fun BrowserScreen(
    browserViewModel: BrowserViewModel,
    downloadsViewModel: DownloadsViewModel,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settingsState by settingsViewModel.settings.collectAsState()
    val currentUrl by browserViewModel.currentUrl.collectAsState()
    val isLoading by browserViewModel.isLoading.collectAsState()
    val capturedMedia by browserViewModel.capturedMediaList.collectAsState()

    LaunchedEffect(settingsState.searchEngineUrl) {
        browserViewModel.syncSearchEngineHome(settingsState.searchEngineUrl)
    }

    val activeUrl = currentUrl ?: browserViewModel.getHomeUrl(settingsState.searchEngineUrl)
    var inputUrl by remember(activeUrl) { mutableStateOf(activeUrl) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var showMediaSheet by remember { mutableStateOf(false) }

    LaunchedEffect(activeUrl) {
        if (webViewInstance != null && webViewInstance?.url != activeUrl) {
            webViewInstance?.loadUrl(activeUrl)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Modern Address Bar
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { webViewInstance?.goBack() },
                        enabled = webViewInstance?.canGoBack() == true
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }

                    IconButton(
                        onClick = { webViewInstance?.goForward() },
                        enabled = webViewInstance?.canGoForward() == true
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
                    }

                    IconButton(
                        onClick = {
                            val homeUrl = browserViewModel.getHomeUrl(settingsState.searchEngineUrl)
                            browserViewModel.navigateTo(homeUrl, settingsState.searchEngineUrl)
                        }
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { webViewInstance?.reload() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reload")
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            browserViewModel.navigateTo(inputUrl, settingsState.searchEngineUrl)
                        }),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                }

                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        // Web View Container
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = settingsState.javaScriptEnabled
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                browserViewModel.onPageStarted()
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                browserViewModel.onPageFinished(view?.title, view?.url)
                            }

                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                request?.url?.toString()?.let { uri ->
                                    browserViewModel.onResourceIntercepted(uri)
                                }
                                return super.shouldInterceptRequest(view, request)
                            }
                        }

                        loadUrl(activeUrl)
                        webViewInstance = this
                    }
                },
                update = { webView ->
                    webView.settings.javaScriptEnabled = settingsState.javaScriptEnabled
                },
                modifier = Modifier.fillMaxSize()
            )

            // Animated Media Sniffer Pill FAB
            androidx.compose.animation.AnimatedVisibility(
                visible = capturedMedia.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { showMediaSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = null) },
                    text = { Text("${capturedMedia.size} Media Detected") },
                    shape = RoundedCornerShape(28.dp)
                )
            }
        }
    }

    if (showMediaSheet) {
        SniffedMediaBottomSheet(
            mediaList = capturedMedia,
            onDismiss = { showMediaSheet = false },
            onDownloadMedia = { media ->
                val targetDir = File(settingsState.downloadDirectory)
                targetDir.mkdirs()
                downloadsViewModel.addDownload(media.url, media.title, targetDir)
            }
        )
    }
}
