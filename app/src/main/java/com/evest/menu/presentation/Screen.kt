package com.evest.menu.presentation

sealed class Screen(val route: String) {
    data object MenuScreen : Screen("menu_screen")
    data object SettingsScreen : Screen("settings_screen")
}
