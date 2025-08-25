package io.github.azakidev.move.data

import androidx.annotation.DrawableRes
import io.github.azakidev.move.R

data class StopItem(
    val stopId: Int = 0,
    val stopName: String = "DefaultStop",
    @DrawableRes val image: Int = R.drawable.nathofjoy,

    var lineTimes: List<LineTime> = listOf(
        LineTime()
    ),
)