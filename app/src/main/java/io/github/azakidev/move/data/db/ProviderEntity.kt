package io.github.azakidev.move.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.azakidev.move.data.Capabilities // Assuming these are simple enums
import io.github.azakidev.move.data.ProviderItem
import io.github.azakidev.move.data.TimeFormat

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val versionMajor: Int,
    val versionMinor: Int,
    val lastUpdated: Int, // Unix timestamp
    val capabilities: List<Capabilities>, // Or use a TypeConverter for List<Capabilities>
    val timeSource: String,
    // Similarly for timeFormat, consider a TypeConverter or store its properties directly
    val timeFormat: TimeFormat,
    val qrFormat: String
)

// Helper function to convert to and from ProviderItem (consider moving to a mapper class)
fun ProviderEntity.toProviderItem(): ProviderItem {
    // Implement conversion, including parsing capabilities string back to List<Capabilities>
    // and reconstructing TimeFormat
    return ProviderItem(
        id = this.id,
        name = this.name,
        description = this.description,
        versionMajor = this.versionMajor,
        versionMinor = this.versionMinor,
        lastUpdated = this.lastUpdated,
        capabilities = this.capabilities,
        timeSource = this.timeSource,
        timeFormat = this.timeFormat,
        qrFormat = this.qrFormat
    )
}

fun ProviderItem.toProviderEntity(): ProviderEntity {
    return ProviderEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        versionMajor = this.versionMajor,
        versionMinor = this.versionMinor,
        lastUpdated = this.lastUpdated,
        capabilities = this.capabilities,
        timeSource = this.timeSource,
        timeFormat = this.timeFormat,
        qrFormat = this.qrFormat
    )
}