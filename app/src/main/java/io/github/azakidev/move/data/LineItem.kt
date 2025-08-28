package io.github.azakidev.move.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.Serializable

@Serializable
data class LineItem (
    val id: Int = 0,

    val name: String = "DefaultLine",
    val emblem: String = "DL",

    val stops: List<Int> = listOf(),

    var expanded: MutableState<Boolean> = mutableStateOf(false),
    var placement:Int = -1
)

@Serializable
data class LineResponse (
    val lines: Array<LineItem>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LineResponse

        return lines.contentEquals(other.lines)
    }

    override fun hashCode(): Int {
        return lines.contentHashCode()
    }
}