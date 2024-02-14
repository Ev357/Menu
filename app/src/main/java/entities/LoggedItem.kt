package entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.LocalTime

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
    var isTaken: Boolean = false,
    val state: String = "normal", // states: "normal" | "not_allowed" | "over"
    val startDispensingTime: LocalTime,
    val endDispensingTime: LocalTime,
    val endOrderDateTime: LocalDateTime,
    val endCancelDateTime: LocalDateTime,
    val itemId: Long,
    @PrimaryKey(autoGenerate = true)
    val loggedItemId: Long = 0,
)
