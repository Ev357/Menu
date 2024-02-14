package com.evest.menu.presentation

sealed class Screen(val route: String) {
    data object MenuListScreen : Screen("menu_list_screen")
    data object SettingsScreen : Screen("settings_screen")
    data object MenuScreen : Screen("menu_screen")
    data object LoginScreen : Screen("login_screen")
    data object MealScreen : Screen("menu_details_screen")

    fun withArgs(vararg args: Any): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
