package com.evest.menu.presentation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import entities.Allergen
import entities.Meal
import entities.Menu
import entities.relations.MealAllergenCrossRef

@Database(
    entities = [
        Menu::class,
        Meal::class,
        Allergen::class,
        MealAllergenCrossRef::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class MenuDatabase : RoomDatabase() {
    abstract val dao: MenuDao

    companion object {
        @Volatile
        private var INSTANCE: MenuDatabase? = null

        fun getInstance(context: Context): MenuDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MenuDatabase::class.java,
                    "school_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
