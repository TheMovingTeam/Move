package io.github.azakidev.move.data

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.azakidev.move.parseTimes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URL
import java.util.concurrent.LinkedBlockingDeque
import kotlin.concurrent.thread

class MoveViewModel(application: Application) : AndroidViewModel(application) {

    private val _userStore = UserStore(application.applicationContext)
    private val _providerRepo: StateFlow<String> = _userStore.providerRepoUrlFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep active for 5s after last subscriber
            initialValue = "https://raw.githubusercontent.com/TheMovingTeam/Providers/refs/heads/main" // Initial fallback
        )
    var providerRepo: MutableState<String> = mutableStateOf(_providerRepo.value)
    private val _providers: MutableStateFlow<List<ProviderItem>> = MutableStateFlow(listOf())
    var providers = _providers.asStateFlow()
    val savedProviders: StateFlow<List<Int>> = _userStore.savedProvidersFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private val _lines: MutableStateFlow<List<LineItem>> = MutableStateFlow(listOf())
    var lines = _lines.asStateFlow()
    private val _stops: MutableStateFlow<List<StopItem>> = MutableStateFlow(listOf())
    var stops = _stops.asStateFlow()
    val favouriteStops: StateFlow<List<Int>> = _userStore.favouriteStopsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val lastStops: StateFlow<List<Int>> = _userStore.lastStopsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            _providerRepo.collect { savedUrl ->
                providerRepo.value = savedUrl
            }
        }
        viewModelScope.launch {
            val initialSavedProviders = savedProviders.first()
            if (initialSavedProviders.isNotEmpty()) {
                fetchProviders()

                // Wait for providers to be fetched if necessary
                providers.first { it.isNotEmpty() || providerRepo.value.isEmpty() } // Ensure providers are loaded if repo is set
                fetchInfoForSavedProviders(initialSavedProviders)
            }
        }
    }

    fun fetchProviders() {
        this@MoveViewModel._providers.value = listOf()
        val currentRepoUrl = providerRepo.value
        thread { // Consider replacing with viewModelScope.launch(Dispatchers.IO)
            val providerListJson = try {
                URL("${currentRepoUrl}/providers.json").readText()
            } catch (e: Exception) {
                println(e); return@thread
            }
            try {
                val response = Json.decodeFromString<ProviderListResponse>(providerListJson)
                val fetchedProviders = mutableListOf<ProviderItem>()
                // This part should ideally be parallelized with coroutines for better performance
                response.providers.forEach { providerName ->
                    try {
                        val providerMetadata =
                            URL("${currentRepoUrl}/${providerName}/metadata.json").readText()
                        val providerItem = Json.decodeFromString<ProviderItem>(providerMetadata)
                        fetchedProviders.add(providerItem)
                    } catch (e: Exception) {
                        println(e); }
                }
                this@MoveViewModel._providers.value = fetchedProviders
                // After fetching all providers, trigger fetchInfo for currently saved ones
                viewModelScope.launch { fetchInfoForSavedProviders(savedProviders.value) }
            } catch (e: Exception) {
                println(e); }
        }
    }

    // This function will fetch info for all providers currently in the savedProviders list.
    fun fetchInfo() {
        viewModelScope.launch { // Ensure it runs in a coroutine
            val currentSavedProviders = savedProviders.first() // Get current list from DataStore
            fetchInfoForSavedProviders(currentSavedProviders)
        }
    }

    // Updated fetchInfo to take a list of provider IDs
    private fun fetchInfoForSavedProviders(providerIds: List<Int>) {
        if (providers.value.isEmpty() && providerIds.isNotEmpty()) {
            println("Provider list is empty, cannot fetch info yet. Call fetchProviders first.")
            return
        }
        val currentRepoUrl = providerRepo.value
        providerIds.forEach { id ->
            providers.value.find { it.id == id }?.let { provider ->
                fetchInfoForSingleProvider(provider, currentRepoUrl)
            }
        }
    }

    // Extracted logic for fetching info for a single provider
    private fun fetchInfoForSingleProvider(
        provider: ProviderItem,
        repoUrl: String = providerRepo.value
    ) {
        // Fetch Lines
        thread { // Consider viewModelScope.launch(Dispatchers.IO)
            val linesJson = try {
                URL("$repoUrl/${provider.name}/lines.json").readText()
            } catch (e: Exception) {
                println(e); return@thread
            }
            val response = try {
                Json.decodeFromString<LineResponse>(linesJson)
            } catch (e: Exception) {
                println(e); return@thread
            }
            val fetchedLines = response.lines.map { it.apply { this.provider = provider.id } }
            // Add only new lines to avoid duplicates and ensure distinctness
            _lines.value = (_lines.value + fetchedLines).distinctBy { Pair(it.id, it.provider) }

        }
        // Fetch Stops
        thread { // Consider viewModelScope.launch(Dispatchers.IO)
            val stopsJson = try {
                URL("$repoUrl/${provider.name}/stops.json").readText()
            } catch (e: Exception) {
                println(e); return@thread
            }
            val response = try {
                Json.decodeFromString<StopResponse>(stopsJson)
            } catch (e: Exception) {
                println(e); return@thread
            }
            val fetchedStops = response.stops.map { it.apply { this.provider = provider.id } }
            _stops.value = (_stops.value + fetchedStops).distinctBy { Pair(it.id, it.provider) }
        }
    }

    fun flushInfo() {
        _providers.value = emptyList()
        _lines.value = emptyList()
        _stops.value = emptyList()
    }

    fun addSavedProvider(providerId: Int) {
        viewModelScope.launch {
            val currentSaved = savedProviders.value.toMutableList()
            if (!currentSaved.contains(providerId)) {
                currentSaved.add(providerId)
                _userStore.saveSavedProviders(currentSaved)
                println(this@MoveViewModel.savedProviders.value)
            }
            fetchInfo()
            println(this@MoveViewModel.stops.value)
            println(this@MoveViewModel.lines.value)
        }
    }

    fun removeSavedProvider(providerId: Int) {
        viewModelScope.launch {
            val currentSaved = savedProviders.value.toMutableList()
            if (currentSaved.remove(providerId)) {
                _userStore.saveSavedProviders(currentSaved)
                // Optionally, flush info related to this provider
                // This logic might need refinement based on how you store/display lines & stops
                this@MoveViewModel._lines.value =
                    _lines.value.filterNot { it.provider == providerId }
                this@MoveViewModel._stops.value =
                    _stops.value.filterNot { it.provider == providerId }
                println(this@MoveViewModel.savedProviders.value)
                println(this@MoveViewModel.stops.value)
                println(this@MoveViewModel.lines.value)
            }
            fetchInfo()
            println(this@MoveViewModel.stops.value)
            println(this@MoveViewModel.lines.value)
        }
    }

    fun addFavStop(stopId: Int) {
        viewModelScope.launch {
            val currentFavStops = favouriteStops.value.toMutableList()
            if (!currentFavStops.contains(stopId)) {
                currentFavStops.add(stopId)
                _userStore.saveFavouriteStops(currentFavStops)
            }
        }
    }

    fun removeFavStop(stopId: Int) {
        viewModelScope.launch {
            val currentFavStops = favouriteStops.value.toMutableList()
            if (currentFavStops.remove(stopId)) {
                _userStore.saveFavouriteStops(currentFavStops)
            }
        }
    }

    fun saveLastStop(stopId: Int) {
        viewModelScope.launch {
            val currentLastStops = lastStops.value.toMutableList()
            if (!currentLastStops.contains(stopId)) {
                currentLastStops.add(stopId)
                _userStore.saveLastStops(currentLastStops)
            } else {
                currentLastStops.remove(stopId)
                _userStore.saveLastStops(currentLastStops)
                currentLastStops.add(
                    index = currentLastStops.count(),
                    element = stopId
                )
                _userStore.saveLastStops(currentLastStops)
            }
            if (currentLastStops.count() > 5) {
                currentLastStops.removeAt(index = 0)
                _userStore.saveLastStops(currentLastStops)
            }
            println(currentLastStops)
        }
    }

    fun tryRepo(url: String): Boolean {
        val isValid = LinkedBlockingDeque<Boolean>()
        thread {
            val providerListJson =
                try {
                    URL("${url}/providers.json").readText()
                } catch (e: Exception) {
                    println(e)
                    isValid.add(false)
                    return@thread
                }
            try {
                Json.decodeFromString<ProviderListResponse>(providerListJson)
                isValid.add(true)
                return@thread
            } catch (e: Exception) {
                println(e)
                isValid.add(false)
                return@thread
            }
        }
        return isValid.take()
    }

    fun saveRepo(url: String) {
        if (tryRepo(url)) {
            viewModelScope.launch {
                _userStore.saveProviderRepoUrl(url)
            }
        } else {
            // Handle invalid repo URL (e.g., show an error message)
            Log.e("Error", "Attempted to save invalid repo URL: $url")
        }
    }

    fun fetchTimes(stopItem: StopItem, context: Context) {
        val provider = providers.value.find { providerItem -> providerItem.id == stopItem.provider }
            ?: ProviderItem()
        if (!(provider.capabilities.contains(Capabilities.Time) || provider.capabilities.contains(
                Capabilities.DoubleTime
            ))
        ) {
            return
        }
        val url = provider.timeSource.replace("@stop", stopItem.id.toString())
        thread {
            val response =
                try {
                    URL(url).readText()
                } catch (e: Exception) {
                    println(e)
                    return@thread
                }
            val times = parseTimes(response, provider) ?: emptyList()
            if (times.isNotEmpty()) {
                var count = 0
                val timeList = mutableListOf<LineTime>()
                stopItem.lines.forEach { i ->
                    timeList.add(LineTime(i, times[count]))
                    count++
                }
                stopItem.setTimeTable(timeList)
            }
            else {
                Toast.makeText(context, "Times couldn't be parsed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
