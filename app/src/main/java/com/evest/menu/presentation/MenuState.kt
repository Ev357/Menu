package com.evest.menu.presentation

import entities.Meal
import entities.relations.ItemAndMealAndLoggedItem
import entities.relations.MealWithAllergens
import entities.relations.MenuWithItems

data class MenuState(
    val menuWithItemsList: List<MenuWithItems> = emptyList(),
    val itemAndMealAndLoggedItemList: List<ItemAndMealAndLoggedItem> = emptyList(),
    val mealWithAllergensList: List<MealWithAllergens> = emptyList(),
    val mealList: List<Meal> = emptyList(),
    val isFetchingData: Boolean = false,
    val wereDataFetched: Boolean = false,
    val status: String = "initial",
)
