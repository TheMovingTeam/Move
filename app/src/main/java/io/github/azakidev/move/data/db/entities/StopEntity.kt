package io.github.azakidev.move.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.azakidev.move.data.items.StopItem

@Entity(
    tableName = "stops",
    foreignKeys = [ForeignKey(
        entity = ProviderEntity::class,
        parentColumns = ["id"],
        childColumns = ["providerId"],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["providerId"]), Index(value = ["id", "providerId"], unique = true)]
)
data class StopEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0, // Auto-generated local primary key
    val id: Int, // Original ID from the Provider
    val comId: Int?, // Commercial ID from the Provider, in case it differs
    val providerId: Int, // Foreign key to ProviderEntity

    val name: String,

    val lines: List<Int>,

    val geoX: Double?,
    val geoY: Double?,

    val notifications: List<String>,
)

// Helper to convert to and from StopItem
fun StopEntity.toStopItem(): StopItem {
    return StopItem(
        id = this.id,
        comId = this.comId,
        name = this.name,
        geoX = this.geoX,
        geoY = this.geoY,
        provider = this.providerId,
        lines = this.lines,
        notifications = this.notifications
    )
}

fun StopItem.toStopEntity(): StopEntity {
    return StopEntity(
        id = this.id,
        comId = this.comId,
        name = this.name,
        geoX = this.geoX,
        geoY = this.geoY,
        providerId = this.provider,
        lines = this.lines,
        notifications = this.notifications
    )
}