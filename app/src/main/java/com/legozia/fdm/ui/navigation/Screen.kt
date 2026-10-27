package com.legozia.fdm.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Downloads : Screen("downloads", "Downloads", Icons.Default.Download)
    object Browser : Screen("browser", "Browser", Icons.Default.Language)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
