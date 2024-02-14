package entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Meal(
    val name: String,
    @PrimaryKey(autoGenerate = true)
    val mealId: Long = 0,
)