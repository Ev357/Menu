package entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import entities.Meal
import entities.Menu

data class MenuWithMeals(
    @Embedded val menu: Menu,
    @Relation(
        parentColumn = "dateString",
        entityColumn = "mealId"
    )
    val meals: List<Meal>
)
