package com.evest.menu.presentation

import androidx.compose.runtime.Immutable
import com.evest.menu.R

@Immutable
data class PreferenceOption(
    var name: String,
    var defaultValue: Any,
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
            PreferenceOption("display_breakfast", true, R.string.breakfast),
            PreferenceOption("display_soup", true, R.string.soup),
            PreferenceOption("display_lunch1", true, R.string.lunch1),
            PreferenceOption("display_lunch2", true, R.string.lunch2),
            PreferenceOption("display_dinner", true, R.string.dinner)
        )
    )

    data object ServerPreference : Preference(
        "server_preference",
        R.string.server,
        listOf(
            PreferenceOption(
                "server_address",
                "https://jidelnicek.roznovskastredni.cz",
                R.string.server_address
            ),
        )
    )
}
