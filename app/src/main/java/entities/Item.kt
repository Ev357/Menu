package entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["type", "menuId", "mealId"], unique = true)])
data class Item(
    val type: String,
    val menuId: Long,
    val mealId: Long,
    val state: String = "unknown",
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,
)
