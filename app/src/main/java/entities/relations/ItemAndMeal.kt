package entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import entities.Item
import entities.Meal

data class ItemAndMeal(
    @Embedded val item: Item,
    @Relation(
        parentColumn = "mealId",
        entityColumn = "mealId",
    )
    val meal: Meal,
)
