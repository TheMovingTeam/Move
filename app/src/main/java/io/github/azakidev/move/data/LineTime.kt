package io.github.azakidev.move.data

import kotlinx.serialization.Serializable

@Serializable
data class LineTime(
    val lineId: Int = 0,
    var nextTime: Int = 0
)