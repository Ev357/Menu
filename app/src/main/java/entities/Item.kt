package entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

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
    val state: String = "unknown", // states: "unknown" | "taken" | "untaken" | "taken" | "not_allowed" | "over"
    val price: Int? = null,
    val startDispensingTime: LocalDate? = null,
    val endDispensingTime: LocalDate? = null,
    val endOrderTime: LocalDate? = null,
    val endCancelTime: LocalDate? = null,
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,
)
