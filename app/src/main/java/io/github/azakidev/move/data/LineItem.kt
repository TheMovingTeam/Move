package io.github.azakidev.move.data

data class LineItem (
    val lineId: Int = 0,

    val lineName: String = "DefaultLine",
    val lineEmblem: String = "DL",

    val stops: List<Int> = listOf()
)