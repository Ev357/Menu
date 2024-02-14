package com.evest.menu.presentation

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import entities.Allergen
import entities.Item
import entities.Meal
import entities.Menu
import entities.relations.MealAllergenCrossRef
import entities.relations.MenuWithItems
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MenuDao {
    // Upserts
    @Upsert
    suspend fun upsertMenu(menu: Menu): Long

    @Upsert
    suspend fun upsertItem(item: Item): Long

    @Upsert
    suspend fun upsertMeal(meal: Meal): Long

    @Upsert
    suspend fun upsertAllergen(allergen: Allergen): Long

    @Upsert
    suspend fun upsertMealAllergenCrossRef(crossRef: MealAllergenCrossRef)

    // Queries
    @Query("SELECT * FROM menu")
    fun getMenuWithItems(): Flow<List<MenuWithItems>>

    @Query("SELECT * FROM meal")
    fun getMeals(): Flow<List<Meal>>

    @Transaction
    @Query("SELECT * FROM menu WHERE date = :date")
    suspend fun getMenuByDate(date: LocalDate): Menu?

    @Transaction
    @Query("SELECT * FROM meal WHERE name = :name")
    suspend fun getMealByName(name: String): Meal?

    // Clear
    @Transaction
    @Query("DELETE FROM menu")
    suspend fun clearMenu()

    @Transaction
    @Query("DELETE FROM item")
    suspend fun clearItem()

    @Transaction
    @Query("DELETE FROM meal")
    suspend fun clearMeal()

    @Transaction
    @Query("DELETE FROM allergen")
    suspend fun clearAllergen()

    @Transaction
    @Query("DELETE FROM mealallergencrossref")
    suspend fun clearMealAllergenCrossRef()
}