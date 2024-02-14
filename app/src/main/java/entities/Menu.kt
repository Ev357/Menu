package entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Menu(
    @PrimaryKey(autoGenerate = false)
    val dateString: String,
    val breakfastId: String? = null,
    val soupId: String? = null,
    val lunch1Id: String? = null,
    val lunch2Id: String? = null,
    val dinnerId: String? = null,
)