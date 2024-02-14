package com.evest.menu.presentation

sealed class MenuEvent {
    data class FetchMenu(val initialFetch: Boolean = false) : MenuEvent()
    data object ClearDatabase : MenuEvent()
    data class OpenAlert(val actionName: String, val titleText: String, val message: String) :
        MenuEvent()
}
