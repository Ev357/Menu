package entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Allergen(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val name: String,
)
