package io.github.azakidev.move.data

import androidx.annotation.DrawableRes

data class StopItem(
    val id: Int,
    @DrawableRes val imageResId: Int,
    val stopName: String,
    var timeLeft: Int,
)