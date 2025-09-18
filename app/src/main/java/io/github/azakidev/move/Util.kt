package io.github.azakidev.move

import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.LineTime
import io.github.azakidev.move.data.ProviderItem
import io.github.azakidev.move.data.StopItem
import io.github.azakidev.move.data.providers.VectaliaResponse
import io.github.azakidev.move.data.providers.parseVectaliaResponse
import kotlinx.serialization.Serializable

@Serializable
internal data object MainView: NavKey
@Serializable
internal data object Settings: NavKey
@Serializable
internal data object Providers: NavKey
@Serializable
internal data object QrScanner: NavKey

fun parseTimes(response: String, provider: ProviderItem, stopItem: StopItem, lines: List<LineItem>): MutableList<LineTime>? {
    when (provider.name) {
        "DummyProvider" -> {
            val times = Regex("\\w+").findAll(response).toList().map { it.value.toInt() }
            if (times.isNotEmpty()) {
                var count = 0
                val timeList = mutableListOf<LineTime>()
                stopItem.lines.forEach { i ->
                    timeList.add(LineTime(i, times[count]))
                    count++
                }
                return timeList
            } else {
                return null
            }
        }
        "Vectalia Alicante" -> {
            val estimations: List<VectaliaResponse> = try {
                parseVectaliaResponse(response)
            } catch (e: Exception) {
                Log.e(
                    "MoveViewModel",
                    "Couldn't parse Vectalia times in ${e.message}",
                    e
                )
                return null
            }
            val timeList = mutableListOf<LineTime>()
            estimations.forEach { estimation ->
                val matchingLine = lines.find { line ->
                    line.name.equals(estimation.destination, ignoreCase = true) ||
                            // Add more sophisticated matching if needed, e.g., if destination might have extra details
                            // or if there's a specific mapping logic between API destination and your LineItem.name
                            line.emblem == estimation.lineEmblem // Fallback or additional check using emblem
                }
                println(matchingLine)
                if (matchingLine != null) {
                    if (estimation.firstEstimateSeconds != null) {
                        timeList.add(
                            LineTime(
                                lineId = matchingLine.id,
                                nextTimeFirst = estimation.firstEstimateSeconds.div(60),
                                nextTimeSecond = estimation.secondEstimateSeconds?.div(60)
                            )
                        )
                    }
                }
            }
            return timeList
        }
        else -> {
            return null
        }
    }
}

fun listShape(count: Int, total: Int): Shape {
    if (total == 1) {
        return RoundedCornerShape(
            8.dp
        )
    }
    return when (count) {
        0 -> {
            RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = 2.dp,
                bottomEnd = 2.dp,
            )
        }

        total - 1 -> {
            RoundedCornerShape(
                topStart = 2.dp,
                topEnd = 2.dp,
                bottomStart = 8.dp,
                bottomEnd = 8.dp
            )
        }

        else -> {
            RoundedCornerShape(
                4.dp
            )
        }
    }
}