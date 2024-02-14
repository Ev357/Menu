package entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    indices = [Index(value = ["itemId"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = Item::class,
        parentColumns = ["itemId"],
        childColumns = ["itemId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class LoggedItem(
    val state: String = "untaken", // states: "taken" | "untaken" | "taken" | "not_allowed" | "over"
    val price: Int,
    val startDispensingTime: LocalDate,
    val endDispensingTime: LocalDate,
    val endOrderTime: LocalDate,
    val endCancelTime: LocalDate,
    val itemId: Long,
    @PrimaryKey(autoGenerate = true)
    val loggedItemId: Long = 0,
)
