package com.evest.menu.presentation

import androidx.compose.runtime.Immutable
import com.evest.menu.R

@Immutable
data class PreferenceOption(
    var name: String,
    var label: Int
)

@Immutable
sealed class Preference(
    val name: String,
    val label: Int,
    val options: List<PreferenceOption>
) {
    data object MenuPreference : Preference(
        "menu_preference",
        R.string.menu,
        listOf(
            PreferenceOption("display_breakfast", R.string.breakfast),
            PreferenceOption("display_soup", R.string.soup),
            PreferenceOption("display_lunch1", R.string.lunch1),
            PreferenceOption("display_lunch2", R.string.lunch2),
            PreferenceOption("display_dinner", R.string.dinner)
        )
    )
}
