package entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import entities.Item
import entities.Menu

data class MenuWithItems(
    @Embedded val menu: Menu,
    @Relation(
        parentColumn = "menuId",
        entityColumn = "menuId",
    )
    val items: List<Item>,
)
