package io.github.azakidev.move.data.items

import androidx.compose.ui.graphics.Color

data class LineTime(
    val lineId: Int,
    var nextTimeFirst: Int,
    var nextTimeSecond: Int? = null,
    val destination: String? = null,
    val emblemOverride: String? = null,
    val colorOverride: Color? = null
)