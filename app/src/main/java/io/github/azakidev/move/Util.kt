package io.github.azakidev.move

import androidx.navigation3.runtime.NavKey
import io.github.azakidev.move.data.ProviderItem
import io.github.azakidev.move.data.TimeType
import kotlinx.serialization.Serializable
import java.util.Collections

@Serializable
internal data object MainView: NavKey
@Serializable
internal data object Settings: NavKey
@Serializable
internal data object Providers: NavKey
@Serializable
internal data object QrScanner: NavKey

fun parseTimes(response: String, provider: ProviderItem): List<Int> {
    when (provider.timeFormat.type) {
        TimeType.IntArray -> {
            val list = Regex("\\w+").findAll(response).toList().map { it.value.toInt() }
            return list
        }
    }

}