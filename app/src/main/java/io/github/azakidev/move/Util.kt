package io.github.azakidev.move

import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import androidx.navigation3.runtime.NavKey
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.LineTime
import io.github.azakidev.move.data.ProviderItem
import io.github.azakidev.move.data.StopItem
import io.github.azakidev.move.data.providers.parseFGVResponse
import io.github.azakidev.move.data.providers.parseVectaliaTimes
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
internal data object MainView : NavKey

@Serializable
internal data object Settings : NavKey

@Serializable
internal data object Providers : NavKey

@Serializable
internal data object QrScanner : NavKey

fun parseTimes(
    response: String,
    provider: ProviderItem,
    stopItem: StopItem,
    lines: List<LineItem>
): List<LineTime>? {
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
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines)
            } catch (e: Exception) {
                Log.e(
                    "MoveViewModel",
                    "Couldn't parse Vectalia times in ${e.message}",
                    e
                )
                return null
            }
            return estimations
        }

        "Vectalia Albacete" -> {
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines)
            } catch (e: Exception) {
                Log.e(
                    "MoveViewModel",
                    "Couldn't parse Vectalia times in ${e.message}",
                    e
                )
                return null
            }
            return estimations
        }

        "Vectalia Cáceres" -> {
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines)
            } catch (e: Exception) {
                Log.e(
                    "MoveViewModel",
                    "Couldn't parse Vectalia times in ${e.message}",
                    e
                )
                return null
            }
            return estimations
        }

        "Vectalia Alcoi" -> {
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines)
            } catch (e: Exception) {
                Log.e(
                    "MoveViewModel",
                    "Couldn't parse Vectalia times in ${e.message}",
                    e
                )
                return null
            }
            return estimations
        }

        "Vectalia Mérida" -> {
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines)
            } catch (e: Exception) {
                Log.e(
                    "MoveViewModel",
                    "Couldn't parse Vectalia times in ${e.message}",
                    e
                )
                return null
            }
            return estimations
        }

        "Tram Alacant" -> {
            val estimations: List<LineTime> = try {
                parseFGVResponse(response)
            } catch (e: Exception) {
                Log.e(
                    "MoveViewModel",
                    "Couldn't parse Tram Alacant times in ${e.message}",
                    e
                )
                return null
            }
            return estimations
        }

        "Metrovalencia" -> {
            val estimations: List<LineTime> = try {
                parseFGVResponse(response)
            } catch (e: Exception) {
                Log.e(
                    "MoveViewModel",
                    "Couldn't parse Metrovalencia times in ${e.message}",
                    e
                )
                return null
            }
            return estimations
        }

        else -> {
            return null
        }
    }
}

fun listShape(
    count: Int,
    total: Int,
    roundingLarge: Dp = 12.dp,
    roundingSmall: Dp = 4.dp
): Shape {
    if (total == 1) {
        return RoundedCornerShape(
            roundingLarge
        )
    }
    return when (count) {
        0 -> {
            RoundedCornerShape(
                topStart = roundingLarge,
                topEnd = roundingLarge,
                bottomStart = roundingSmall,
                bottomEnd = roundingSmall,
            )
        }

        total - 1 -> {
            RoundedCornerShape(
                topStart = roundingSmall,
                topEnd = roundingSmall,
                bottomStart = roundingLarge,
                bottomEnd = roundingLarge
            )
        }

        else -> {
            RoundedCornerShape(
                roundingSmall
            )
        }
    }
}

fun String.fmt(): String {
    return this
        .lowercase()
        .replace("-", " - ")
        .replace("–", " - ")
        .replace("—", " - ")
        .replace(">", " > ")
        .replace("(", " ( ")
        .replace(".", ". ")
        .replace("'", "' ")
        .replace("\"", "")
        .replace("_", " ")
        .replace("c/", "C/")
        .replace("C/", "C/ ")
        .replace("avda", "av.")
        .replace("- obres", "( obres )")
        .replace("..", ".")
        .replace("  ", " ")
        .replace("- >", ">")
        .split(' ')
        .map { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
        .fastJoinToString(" ")
        .replace("' ", "'")
        .replace("( ", "(")
        .replace(" )", ")")
}

fun String.fmtSearch(): String {
    return this
        .lowercase()
        .toList()
        .filterNot { listOf('-', ' ', '(', ')', '.').contains(it) }
        .joinToString("")
}