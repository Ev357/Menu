package entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Allergen(
    @PrimaryKey(autoGenerate = false)
    val allergenId: Int,
    val description: String,
)
