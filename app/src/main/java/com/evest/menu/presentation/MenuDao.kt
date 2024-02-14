package com.evest.menu.presentation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import entities.Allergen
import entities.Meal
import entities.Menu
import entities.relations.MealAllergenCrossRef
import entities.relations.MenuAndMeal
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    @Upsert
    suspend fun upsertMenu(menu: Menu)

    @Insert
    suspend fun insertMeal(meal: Meal)

    @Upsert
    suspend fun upsertAllergen(allergen: Allergen)

    @Upsert
    suspend fun upsertMealAllergenCrossRef(crossRef: MealAllergenCrossRef)

    @Query("SELECT * FROM menu")
    fun getMenuAndMeal(): Flow<List<MenuAndMeal>>

    @Transaction
    @Query("SELECT * FROM meal WHERE name = :name")
    suspend fun getMealByName(name: String): Meal

    @Transaction
    @Query("DELETE FROM menu")
    suspend fun clearMenu()

    @Transaction
    @Query("DELETE FROM meal")
    suspend fun clearMeal()
}