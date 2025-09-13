package io.github.azakidev.move.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.Gson
import io.github.azakidev.move.data.LineItem

@Entity(
    tableName = "lines",
    foreignKeys = [ForeignKey(
        entity = ProviderEntity::class,
        parentColumns = ["id"],
        childColumns = ["providerId"],
        onDelete = ForeignKey.CASCADE // If a provider is deleted, its lines are also deleted
    )],
    indices = [Index(value = ["providerId"]), Index(value = ["id", "providerId"], unique = true)]
)
data class LineEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0, // Auto-generated local primary key
    val id: Int, // Original ID from the Provider
    val providerId: Int, // Foreign key to ProviderEntity

    val name: String,
    val emblem: String,
    val color: String?,

    val stops: List<Int>
    )

// Helper to convert to and from LineItem
fun LineEntity.toLineItem(): LineItem {
    return LineItem(
        id = this.id,
        name = this.name,
        emblem = this.emblem,
        color = this.color,
        provider = this.providerId,
        stops = this.stops
    )
}

fun LineItem.toLineEntity(): LineEntity {
    return LineEntity(
        id = this.id,
        name = this.name,
        emblem = this.emblem,
        color = this.color,
        providerId = this.provider,
        stops = this.stops
    )
}