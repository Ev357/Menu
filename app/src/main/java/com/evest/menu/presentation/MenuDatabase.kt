package com.evest.menu.presentation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import entities.Allergen
import entities.Item
import entities.LoggedItem
import entities.Meal
import entities.Menu
import entities.relations.MealAllergenCrossRef
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Database(
    entities = [
        Menu::class,
        Item::class,
        Meal::class,
        Allergen::class,
        MealAllergenCrossRef::class,
        LoggedItem::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
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
                    "menu_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}

class Converters {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @TypeConverter
    fun fromString(string: String): LocalDate {
        return LocalDate.parse(string, dateFormatter)
    }

    @TypeConverter
    fun dateToString(date: LocalDate): String {
        return dateFormatter.format(date)
    }
}