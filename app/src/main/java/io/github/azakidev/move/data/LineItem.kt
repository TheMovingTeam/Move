package io.github.azakidev.move.data

data class LineItem (
    val lineId: Int = 0,

    val lineName: String = "DefaultLine",
    val lineEmblem: String = "D",

    val stops: List<Int> = listOf(1, 2, 3, 4, 5)
)