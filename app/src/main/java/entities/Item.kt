package entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [Index(value = ["type", "menuId"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = Menu::class,
        parentColumns = ["menuId"],
        childColumns = ["menuId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Item(
    val type: String,
    val menuId: Long,
    val mealId: Long,
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,
)
