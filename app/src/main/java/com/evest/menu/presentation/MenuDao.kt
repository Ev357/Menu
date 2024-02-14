package com.evest.menu.presentation

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import entities.Allergen
import entities.Item
import entities.LoggedItem
import entities.Meal
import entities.Menu
import entities.relations.ItemAndMealAndLoggedItem
import entities.relations.MealAllergenCrossRef
import entities.relations.MealWithAllergens
import entities.relations.MenuWithItems
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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
    suspend fun upsertLoggedItem(meal: LoggedItem): Long

    @Upsert
    suspend fun upsertMealAllergenCrossRef(crossRef: MealAllergenCrossRef): Long

    // Queries
    @Transaction
    @Query("SELECT * FROM menu WHERE date(date) >= date('now')")
    fun getMenuWithItems(): Flow<List<MenuWithItems>>

    @Transaction
    @Query("SELECT * FROM meal")
    fun getMeals(): Flow<List<Meal>>

    @Transaction
    @Query("SELECT * FROM menu WHERE date = :date")
    suspend fun getMenu(date: LocalDate): Menu?

    @Transaction
    @Query("SELECT * FROM menu WHERE menuId = :menuId")
    suspend fun getMenuById(menuId: Long): Menu?

    @Transaction
    @Query("SELECT * FROM item WHERE menuId = :menuId")
    suspend fun getItemAndMealAndLoggedItemList(menuId: Long): List<ItemAndMealAndLoggedItem>

    @Transaction
    @Query("SELECT * FROM item WHERE menuId = :menuId AND type = :mealType")
    suspend fun getItem(menuId: Long, mealType: String): Item?

    @Transaction
    @Query("SELECT * FROM meal WHERE mealId IN (:mealIdList)")
    suspend fun getMealWithAllergens(mealIdList: List<Long>): List<MealWithAllergens>

    @Transaction
    @Query("SELECT * FROM meal WHERE name = :name")
    suspend fun getMeal(name: String): Meal?

    @Transaction
    @Query("UPDATE item SET mealId = :mealId WHERE menuId = :menuId AND type = :type")
    suspend fun updateMenuItem(menuId: Long, type: String, mealId: Long)

    @Transaction
    @Query(
        "UPDATE loggeditem SET state = :state, startDispensingTime = :startDispensingTime, endDispensingTime = :endDispensingTime, endOrderDateTime = :endOrderDateTime, endCancelDateTime = :endCancelDateTime, isTaken = :isTaken WHERE itemId = :itemId"
    )
    suspend fun updateLoggedItem(
        itemId: Long,
        state: String,
        startDispensingTime: LocalTime,
        endDispensingTime: LocalTime,
        endOrderDateTime: LocalDateTime,
        endCancelDateTime: LocalDateTime,
        isTaken: Boolean = false,
    )

    @Transaction
    @Query("DELETE FROM menu WHERE date(date) > date(:startDateString) AND date(date) < date(:endDateString) AND date NOT IN (:menuDateStringList)")
    suspend fun deleteRemovedMenu(
        startDateString: String,
        endDateString: String,
        menuDateStringList: List<String>
    )

    @Transaction
    @Query("DELETE FROM item WHERE menuId = :menuId AND type NOT IN (:mealTypeList)")
    suspend fun deleteRemovedItems(
        menuId: Long,
        mealTypeList: List<String>
    )

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

    @Transaction
    @Query("DELETE FROM loggeditem")
    suspend fun clearLoggedItem()
}