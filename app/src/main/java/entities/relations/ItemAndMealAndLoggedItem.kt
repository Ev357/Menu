package entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import entities.Item
import entities.LoggedItem
import entities.Meal

data class ItemAndMealAndLoggedItem(
    @Embedded val item: Item,
    @Relation(
        parentColumn = "mealId",
        entityColumn = "mealId",
    )
    val meal: Meal,

    @Relation(
        parentColumn = "itemId",
        entityColumn = "itemId",
    )
    val loggedItem: LoggedItem?
)
