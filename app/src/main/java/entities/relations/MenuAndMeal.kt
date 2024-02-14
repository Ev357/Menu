package entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import entities.Meal
import entities.Menu

data class MenuAndMeal(
    @Embedded val menu: Menu,
    @Relation(
        parentColumn = "breakfastId",
        entityColumn = "mealId"
    )
    val breakfast: Meal?,

    @Relation(
        parentColumn = "soupId",
        entityColumn = "mealId"
    )
    val soup: Meal?,

    @Relation(
        parentColumn = "lunch1Id",
        entityColumn = "mealId"
    )
    val lunch1: Meal?,

    @Relation(
        parentColumn = "lunch2Id",
        entityColumn = "mealId"
    )
    val lunch2: Meal?,

    @Relation(
        parentColumn = "dinnerId",
        entityColumn = "mealId"
    )
    val dinner: Meal?,
)