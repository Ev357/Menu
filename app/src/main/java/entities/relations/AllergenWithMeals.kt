package entities.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import entities.Allergen
import entities.Meal

data class AllergenWithMeals(
    @Embedded val allergen: Allergen,
    @Relation(
        parentColumn = "id",
        entityColumn = "mealId",
        associateBy = Junction(MealAllergenCrossRef::class)
    )
    val meals: List<Meal>
)
