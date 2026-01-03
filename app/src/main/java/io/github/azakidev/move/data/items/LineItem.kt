package io.github.azakidev.move.data.items

import kotlinx.serialization.Serializable

@Serializable
data class LineItem (
    val id: Int = 0,
    val provider: Int = 0,

    val name: String = "DefaultLine",
    val emblem: String = "DL",
    val color: String? = null,

    val stops: List<Int> = listOf()
)
@Serializable
data class LineResponse (
    val lines: List<LineItem>
)