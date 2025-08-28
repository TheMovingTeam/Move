package io.github.azakidev.move.data

import androidx.annotation.DrawableRes
import io.github.azakidev.move.R
import kotlinx.serialization.Serializable

@Serializable
data class StopItem(
    val id: Int = 0,
    val name: String = "DefaultStop",
    @DrawableRes val image: Int = R.drawable.nathofjoy,
    val lines: Array<Int> = arrayOf(),

    var lineTimes: List<LineTime> = listOf(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StopItem

        if (id != other.id) return false
        if (image != other.image) return false
        if (name != other.name) return false
        if (!lines.contentEquals(other.lines)) return false
        if (lineTimes != other.lineTimes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + image
        result = 31 * result + name.hashCode()
        result = 31 * result + lines.contentHashCode()
        result = 31 * result + lineTimes.hashCode()
        return result
    }
}

@Serializable
data class StopResponse (
    val stops: Array<StopItem>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StopResponse

        return stops.contentEquals(other.stops)
    }

    override fun hashCode(): Int {
        return stops.contentHashCode()
    }
}