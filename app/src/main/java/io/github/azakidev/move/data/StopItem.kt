package io.github.azakidev.move.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class StopItem(
    val id: Int = 0,
    val name: String = "DefaultStop",
    var provider: Int = 0,

    val geoX: Float? = null,
    val geoY: Float? = null,

    val lines: List<Int> = emptyList(),

) {
    private var _lineTimes: MutableStateFlow<List<LineTime>> = MutableStateFlow(listOf())
    val lineTimes = _lineTimes.asStateFlow()
    fun setTimeTable(times: List<LineTime>) {
        _lineTimes.value = times
    }
}
@Serializable
data class StopResponse (
    val stops: List<StopItem>
)