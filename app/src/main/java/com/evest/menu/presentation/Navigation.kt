package com.evest.menu.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController

@Composable
fun Navigation() {
    val navController = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Screen.MenuListScreen.route
    ) {
        composable(Screen.MenuListScreen.route) {
            MenuListScreen(navController)
        }
        composable(Screen.SettingsScreen.route) {
            SettingsScreen(navController)
        }
        composable(
            Screen.MenuScreen.route + "/{menuId}",
            listOf(
                navArgument("menuId") {
                    type = NavType.LongType
                }
            )
        ) { entry ->
            entry.arguments?.let { MenuScreen(it.getLong("menuId")) }
        }
        composable(Screen.LoginScreen.route) {
            LoginScreen(navController)
        }
    }
}