package com.evest.menu.presentation

sealed class MenuEvent {
    data class FetchMenu(val url: String, val initialFetch: Boolean = false) : MenuEvent()
    data object ClearDatabase : MenuEvent()
}
