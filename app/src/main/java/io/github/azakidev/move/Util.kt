package io.github.azakidev.move

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.core.net.ParseException
import androidx.navigation3.runtime.NavKey
import io.github.azakidev.move.data.ProviderItem
import io.github.azakidev.move.data.TimeType
import kotlinx.serialization.Serializable

@Serializable
internal data object MainView: NavKey
@Serializable
internal data object Settings: NavKey
@Serializable
internal data object Providers: NavKey
@Serializable
internal data object QrScanner: NavKey

fun parseTimes(response: String, provider: ProviderItem): List<Int>? {
    when (provider.timeFormat.type) {
        TimeType.IntArray -> {
            val list = Regex("\\w+").findAll(response).toList().map { it.value.toInt() }
            return list
        }
        else -> {
            return null
        }
    }
}

fun getListShape(count: Int, total: Int): Shape {
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