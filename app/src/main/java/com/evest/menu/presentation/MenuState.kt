package com.evest.menu.presentation

import entities.Meal
import entities.relations.ItemAndRelations
import entities.relations.MealWithAllergens
import entities.relations.MenuWithItems

data class MenuState(
    val menuWithItemsList: List<MenuWithItems> = emptyList(),
    val itemAndMealAndLoggedItemList: List<ItemAndRelations> = emptyList(),
    val mealWithAllergensList: List<MealWithAllergens> = emptyList(),
    val mealList: List<Meal> = emptyList(),
    val itemAndRelations: ItemAndRelations? = null,
    val mealWithAllergens: MealWithAllergens? = null,
    val isFetchingData: Boolean = false,
    val wereDataFetched: Boolean = false,
    val status: String = "initial",
)
