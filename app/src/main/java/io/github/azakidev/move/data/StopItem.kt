package io.github.azakidev.move.data

import androidx.annotation.DrawableRes
import io.github.azakidev.move.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class StopItem(
    val id: Int = 0,
    val name: String = "DefaultStop",
    var provider: Int = 0,

    @DrawableRes val image: Int = R.drawable.nathofjoy,
    val lines: List<Int> = listOf(),

    private var _lineTimes: MutableStateFlow<List<LineTime>> = MutableStateFlow(listOf()),
) {
    val lineTimes = _lineTimes.asStateFlow()
    fun setTimeTable(times: List<LineTime>) {
        _lineTimes.value = times
    }
}
@Serializable
data class StopResponse (
    val stops: List<StopItem>
)