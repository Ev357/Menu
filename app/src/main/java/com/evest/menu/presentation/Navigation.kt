package com.evest.menu.presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController

@Composable
fun Navigation() {
    val navController = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Screen.MenuScreen.route
    ) {
        composable(Screen.MenuScreen.route) {
            MenuScreen(navController)
        }
        composable(Screen.SettingsScreen.route) {
            SettingsScreen()
        }
    }
}