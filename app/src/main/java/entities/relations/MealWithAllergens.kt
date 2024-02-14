package entities.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import entities.Allergen
import entities.Meal

data class MealWithAllergens(
    @Embedded val meal: Meal,
    @Relation(
        parentColumn = "mealId",
        entityColumn = "id",
        associateBy = Junction(MealAllergenCrossRef::class)
    )
    val allergens: List<Allergen>
)
