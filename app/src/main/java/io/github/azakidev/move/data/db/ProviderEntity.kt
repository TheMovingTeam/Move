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
    // Storing complex objects like lists of enums or custom objects might require TypeConverters
    // For simplicity, let's assume capabilities can be stored as a comma-separated string or use a TypeConverter
    val capabilities: String, // Or use a TypeConverter for List<Capabilities>
    val timeSource: String,
    // Similarly for timeFormat, consider a TypeConverter or store its properties directly
    val timeFormatType: String, // e.g., TimeType.IntArray.name
    val timeFormatRegex: String?,
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
        capabilities = this.capabilities.split(",").mapNotNull { enumName ->
            try {
                Capabilities.valueOf(enumName)
            } catch (e: IllegalArgumentException) {
                null // Handle cases where enum name might be invalid
            }
        },
        timeSource = this.timeSource,
        timeFormat = TimeFormat(
            type = io.github.azakidev.move.data.TimeType.valueOf(this.timeFormatType),
            regex = this.timeFormatRegex
        ),
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
        capabilities = this.capabilities.joinToString(",") { it.name },
        timeSource = this.timeSource,
        timeFormatType = this.timeFormat.type.name,
        timeFormatRegex = this.timeFormat.regex,
        qrFormat = this.qrFormat
    )
}