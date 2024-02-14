package com.evest.menu.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class PreferenceOption(
    var name: String,
    var label: String
)

@Immutable
sealed class Preference(
    val name: String,
    val label: String,
    val options: List<PreferenceOption>
) {
    data object MenuPreference : Preference(
        "menu_preference",
        "Menu",
        listOf(
            PreferenceOption("display_breakfast", "Breakfast"),
            PreferenceOption("display_soup", "Soup"),
            PreferenceOption("display_lunch1", "Lunch1"),
            PreferenceOption("display_lunch2", "Lunch2"),
            PreferenceOption("display_dinner", "Dinner")
        ) // TODO add translation
    )
}
