package io.github.azakidev.move.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.github.azakidev.move.parseTimes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URL
import kotlinx.serialization.json.Json
import java.util.concurrent.LinkedBlockingDeque
import kotlin.concurrent.thread

class MoveModel: ViewModel() {
    var providerRepo: MutableState<String> = mutableStateOf("https://raw.githubusercontent.com/TheMovingTeam/Providers/refs/heads/main")
    private val _providers: MutableStateFlow<List<ProviderItem>> = MutableStateFlow(listOf())
    var providers = _providers.asStateFlow()
    var savedProviders: List<Int> = mutableListOf(1)
    private val _lines: MutableStateFlow<List<LineItem>> = MutableStateFlow(listOf())
    var lines = _lines.asStateFlow()
    private val _stops: MutableStateFlow<List<StopItem>> = MutableStateFlow(listOf() )
    var stops = _stops.asStateFlow()
    private val _favouriteStops: MutableStateFlow<List<Int>> = MutableStateFlow(listOf(1, 3, 5))
    var favouriteStops = _favouriteStops.asStateFlow()

    fun fetchProviders() {
        this@MoveModel._providers.value = listOf()
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
                    this@MoveModel._providers.value += providerItem
                }
            }
        }
    }

    fun fetchInfo() {
        this.savedProviders.forEach { id ->
            val provider = providers.value.find { providerItem -> providerItem.id == id} ?: ProviderItem()
            thread {
                val providerListJson =
                    try {
                        URL("${providerRepo.value}/${provider.name}/lines.json").readText()
                    } catch (e: Exception) {
                        return@thread
                    }
                val response = Json.decodeFromString<LineResponse>(providerListJson)
                val lines = mutableListOf<LineItem>()
                response.lines.forEach { lineItem ->
                    lineItem.provider = id
                    lines += lineItem
                }
                this@MoveModel._lines.value += lines.subtract(this@MoveModel._lines.value)
            }

            thread {
                val providerListJson =
                    try {
                        URL("${providerRepo.value}/${provider.name}/stops.json").readText()
                    } catch (e: Exception) {
                        return@thread
                    }
                val response = Json.decodeFromString<StopResponse>(providerListJson)
                val stops = mutableListOf<StopItem>()
                response.stops.forEach { stopItem ->
                    stopItem.provider = id
                    stops += stopItem
                }
                this@MoveModel._stops.value += stops.subtract(this@MoveModel._stops.value)
            }
        }
    }

    fun flushInfo() {
        this._lines.value = listOf()
        this._stops.value = listOf()
    }

    fun setProviders(providers: List<ProviderItem>) {
        this._providers.value = providers
    }

    fun setStops(stops: List<StopItem>) {
        this._stops.value = stops
    }

    fun setLines(lines: List<LineItem>) {
        this._lines.value = lines
    }

    fun setFavStops(stops: List<Int>) {
        this._favouriteStops.value = stops
    }

    fun addFavStop(stop: Int) {
        this._favouriteStops.value += stop
    }

    fun removeFavStop(stop: Int) {
        this._favouriteStops.value -= stop
    }

    fun tryRepo(url: String): Boolean {
        val isValid = LinkedBlockingDeque<Boolean>()
        thread {
            val providerListJson =
                try {
                    URL("${url}/providers.json").readText()
                } catch (e: Exception) {
                    isValid.add(false)
                    return@thread
                }
            try {
                Json.decodeFromString<ProviderListResponse>(providerListJson)
                isValid.add(true)
                return@thread
            } catch (e: Exception) {
                isValid.add(false)
                return@thread
            }
        }
        return isValid.take()
    }

    fun fetchTimes(stopItem: StopItem) {
        val provider = providers.value.find { providerItem -> providerItem.id == stopItem.provider} ?: ProviderItem()
        if (!provider.capabilities.contains("Time")) {
            return
        }
        val url = provider.timeSource.replace("@stop", stopItem.id.toString())
        thread {
            val response =
                try {
                    URL(url).readText()
                } catch (e: Exception) {
                    return@thread
                }
            val times = parseTimes(response, provider)
            var count = 0
            val timeList = mutableListOf<LineTime>()
            stopItem.lines.forEach { i ->
                timeList.add(LineTime(i, times[count]))
                count++
            }
            stopItem.setTimeTable(timeList)
        }
    }
}
