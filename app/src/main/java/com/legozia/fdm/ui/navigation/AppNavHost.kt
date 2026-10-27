package com.legozia.fdm.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.legozia.fdm.ui.screens.BrowserScreen
import com.legozia.fdm.ui.screens.DownloadsScreen
import com.legozia.fdm.ui.screens.SettingsScreen
import com.legozia.fdm.viewmodel.BrowserViewModel
import com.legozia.fdm.viewmodel.DownloadsViewModel
import com.legozia.fdm.viewmodel.SettingsViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    downloadsViewModel: DownloadsViewModel = viewModel(),
    browserViewModel: BrowserViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Downloads, Screen.Browser, Screen.Settings)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Downloads.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Downloads.route) {
                DownloadsScreen(viewModel = downloadsViewModel)
            }
            composable(Screen.Browser.route) {
                BrowserScreen(
                    browserViewModel = browserViewModel,
                    downloadsViewModel = downloadsViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
