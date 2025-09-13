package io.github.azakidev.move.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.Gson
import io.github.azakidev.move.data.StopItem

@Entity(
    tableName = "stops",
    foreignKeys = [ForeignKey(
        entity = ProviderEntity::class,
        parentColumns = ["id"],
        childColumns = ["providerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["providerId"]), Index(value = ["id", "providerId"], unique = true)]
)
data class StopEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0, // Auto-generated local primary key
    val id: Int, // Original ID from the Provider
    val providerId: Int, // Foreign key to ProviderEntity

    val name: String,

    val lines: List<Int>,

    val geoX: Float?,
    val geoY: Float?,

    val notifications: List<String>,
)

// Helper to convert to and from StopItem
fun StopEntity.toStopItem(): StopItem {
    return StopItem(
        id = this.id,
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
        name = this.name,
        geoX = this.geoX,
        geoY = this.geoY,
        providerId = this.provider,
        lines = this.lines,
        notifications = this.notifications
    )
}