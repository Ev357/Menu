package entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(indices = [Index(value = ["date"], unique = true)])
data class Menu(
    val date: LocalDate,
    @PrimaryKey(autoGenerate = true)
    val menuId: Long = 0,
)