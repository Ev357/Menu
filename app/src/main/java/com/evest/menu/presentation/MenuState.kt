package com.evest.menu.presentation

import entities.relations.MenuAndMeal

data class MenuState(
    val menuList: List<MenuAndMeal> = emptyList(),
    val isFetchingData: Boolean = false,
    val wereDataFetched: Boolean = false,
)
