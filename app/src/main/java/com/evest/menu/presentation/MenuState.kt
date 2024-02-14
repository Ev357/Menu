package com.evest.menu.presentation

import entities.Meal
import entities.relations.ItemAndMeal
import entities.relations.MealWithAllergens
import entities.relations.MenuWithItems

data class MenuState(
    val menuWithItemsList: List<MenuWithItems> = emptyList(),
    val itemAndMealList: List<ItemAndMeal> = emptyList(),
    val mealWithAllergensList: List<MealWithAllergens> = emptyList(),
    val mealList: List<Meal> = emptyList(),
    val isFetchingData: Boolean = false,
    val wereDataFetched: Boolean = false,
    val status: String = "initial",
)
