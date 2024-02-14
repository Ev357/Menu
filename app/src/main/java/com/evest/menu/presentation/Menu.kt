package com.evest.menu.presentation

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class Allergen(
    var id: Int,
    var name: String,
)

@Immutable
data class MenuItem(
    var name: String,
    var allergens: List<Allergen>
)

@Immutable
data class Menu(
    var date: LocalDate,
    var breakfast: MenuItem?,
    var soup: MenuItem?,
    var lunch1: MenuItem?,
    var lunch2: MenuItem?,
    var dinner: MenuItem?
)
