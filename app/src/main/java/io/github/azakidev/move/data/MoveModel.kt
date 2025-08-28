package io.github.azakidev.move.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import java.net.URL
import kotlinx.serialization.json.Json
import kotlin.concurrent.thread

class MoveModel: ViewModel() {
    var providerRepo: MutableState<String> = mutableStateOf("http://192.168.0.17:3000")
    var providers: List<ProviderItem> = mutableListOf()
    var savedProviders: List<Int> = mutableListOf(1)
    var lines: List<LineItem> = listOf()
    var stops: List<StopItem> = listOf()
    var favouriteStops: List<Int> = listOf()

    fun fetchProviders() {
        this@MoveModel.providers = listOf()
        thread {
            val providerListJson =
                try {
                    URL("${providerRepo.value}/providers.json").readText()
                } catch (e: Exception) {
                    return@thread
                }
            val response = Json.decodeFromString<ProviderListResponse>(providerListJson)

            response.providers.forEach { provider ->
                thread {
                    val providerMetadata =
                        try {
                            URL("${providerRepo.value}/${provider}/metadata.json").readText()
                        } catch (e: Exception) {
                            return@thread
                        }
                    val providerItem = Json.decodeFromString<ProviderItem>(providerMetadata)
                    this@MoveModel.providers += providerItem
                }
            }
        }
    }

    fun fetchInfo() {
        this@MoveModel.lines = listOf()
        this@MoveModel.stops = listOf()
        this.savedProviders.forEach { id ->
            val provider = providers.find { providerItem -> providerItem.id == id} ?: ProviderItem()
            thread {
                val providerListJson =
                    try {
                        URL("${providerRepo.value}/${provider.name}/lines.json").readText()
                    } catch (e: Exception) {
                        return@thread
                    }
                val response = Json.decodeFromString<LineResponse>(providerListJson)

                this@MoveModel.lines += response.lines
            }

            thread {
                val providerListJson =
                    try {
                        URL("${providerRepo.value}/${provider.name}/stops.json").readText()
                    } catch (e: Exception) {
                        return@thread
                    }
                val response = Json.decodeFromString<StopResponse>(providerListJson)

                response.stops.forEach { stopItem ->
                    stopItem.lines.forEach { n ->
                        stopItem.lineTimes += LineTime(lineId = n, nextTime = stopItem.id)
                        this@MoveModel.stops += stopItem
                    }
                }
            }
        }
    }
}
