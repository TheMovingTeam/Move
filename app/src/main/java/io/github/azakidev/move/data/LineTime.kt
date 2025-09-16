package io.github.azakidev.move.data

import kotlinx.serialization.Serializable

@Serializable
data class LineTime(
    val lineId: Int = 0,
    var nextTimeFirst: Int = 0,
    var nextTimeSecond: Int? = null
)