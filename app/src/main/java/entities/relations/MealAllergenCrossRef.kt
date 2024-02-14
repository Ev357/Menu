package entities.relations

import androidx.room.Entity

@Entity(primaryKeys = ["mealId", "allergenId"])
data class MealAllergenCrossRef(
    val mealId: Long,
    val allergenId: Int
)
