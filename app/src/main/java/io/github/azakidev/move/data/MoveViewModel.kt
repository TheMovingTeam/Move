package io.github.azakidev.move.data

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
import java.net.URL
import kotlinx.serialization.json.Json
import java.util.concurrent.LinkedBlockingDeque
import kotlin.collections.toMutableList
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

    init {
        viewModelScope.launch {
            _providerRepo.collect { savedUrl ->
                providerRepo.value = savedUrl
            }
        }
        viewModelScope.launch {
            // Wait for the first emission of savedProviders to ensure it's loaded from DataStore
            val initialSavedProviders = savedProviders.first()
            if (initialSavedProviders.isNotEmpty()) {
                // You might need to fetch the full provider list first if `fetchInfo` relies on it
                // and `savedProviders` only contains IDs.
                // Assuming fetchProviders() populates `_providers.value`
                fetchProviders() // Ensure provider list is available

                // Wait for providers to be fetched if necessary
                providers.first { it.isNotEmpty() || providerRepo.value.isEmpty() } // Ensure providers are loaded if repo is set
                fetchInfoForSavedProviders(initialSavedProviders)
            }
        }
    }

    fun fetchProviders() {
        // ... (implementation as before, ensure it uses providerRepo.value)
        // Make sure this populates _providers.value
        this@MoveViewModel._providers.value = listOf()
        val currentRepoUrl = providerRepo.value
        thread { // Consider replacing with viewModelScope.launch(Dispatchers.IO)
            val providerListJson = try { URL("${currentRepoUrl}/providers.json").readText() } catch (e: Exception) { return@thread }
            try {
                val response = Json.decodeFromString<ProviderListResponse>(providerListJson)
                val fetchedProviders = mutableListOf<ProviderItem>()
                // This part should ideally be parallelized with coroutines for better performance
                response.providers.forEach { providerName ->
                    try {
                        val providerMetadata = URL("${currentRepoUrl}/${providerName}/metadata.json").readText()
                        val providerItem = Json.decodeFromString<ProviderItem>(providerMetadata)
                        fetchedProviders.add(providerItem)
                    } catch (e: Exception) { /* handle individual error */ }
                }
                this@MoveViewModel._providers.value = fetchedProviders
                // After fetching all providers, trigger fetchInfo for currently saved ones
                viewModelScope.launch { fetchInfoForSavedProviders(savedProviders.value) }
            } catch (e: Exception) { /* handle parsing error */ }
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
    private fun fetchInfoForSingleProvider(provider: ProviderItem, repoUrl: String = providerRepo.value) {
        // Fetch Lines
        thread { // Consider viewModelScope.launch(Dispatchers.IO)
            val linesJson = try { URL("$repoUrl/${provider.name}/lines.json").readText() } catch (e: Exception) { return@thread }
            val response = try { Json.decodeFromString<LineResponse>(linesJson) } catch (e: Exception) { return@thread }
            val fetchedLines = response.lines.map { it.apply { this.provider = provider.id } }
            // Add only new lines to avoid duplicates and ensure distinctness
            _lines.value = (_lines.value + fetchedLines).distinctBy { Pair(it.id, it.provider) }

        }

        // Fetch Stops
        thread { // Consider viewModelScope.launch(Dispatchers.IO)
            val stopsJson = try { URL("$repoUrl/${provider.name}/stops.json").readText() } catch (e: Exception) { return@thread }
            val response = try { Json.decodeFromString<StopResponse>(stopsJson) } catch (e: Exception) { return@thread }
            val fetchedStops = response.stops.map { it.apply { this.provider = provider.id } }
            _stops.value = (_stops.value + fetchedStops).distinctBy { Pair(it.id, it.provider) }
        }
    }

    fun flushInfo() {
        _providers.value = emptyList()
        _lines.value = emptyList()
        _stops.value = emptyList()
    }

    fun setProviders(providers: List<ProviderItem>) {
        this._providers.value = providers
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
                _userStore.saveSavedProviders(currentSaved )
                // Optionally, flush info related to this provider
                // This logic might need refinement based on how you store/display lines & stops
                this@MoveViewModel._lines.value = _lines.value.filterNot { it.provider == providerId }
                this@MoveViewModel._stops.value = _stops.value.filterNot { it.provider == providerId }
                println(this@MoveViewModel.savedProviders.value)
                println(this@MoveViewModel.stops.value)
                println(this@MoveViewModel.lines.value)
            }
            fetchInfo()
            println(this@MoveViewModel.stops.value)
            println(this@MoveViewModel.lines.value)
        }
    }

    fun setStops(stops: List<StopItem>) {
        this._stops.value = stops
    }

    fun setLines(lines: List<LineItem>) {
        this._lines.value = lines
    }

    fun setFavStops(stops: List<Int>) {
        viewModelScope.launch {
            _userStore.saveFavouriteStops(stops)
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

    fun saveRepo(url: String) {
        if (tryRepo(url)) { // Assuming tryRepo validates and doesn't just block
            viewModelScope.launch {
                _userStore.saveProviderRepoUrl(url)
            }
        } else {
            // Handle invalid repo URL (e.g., show an error message)
            println("Attempted to save invalid repo URL: $url")
        }
    }

    fun fetchTimes(stopItem: StopItem) {
        val provider = providers.value.find { providerItem -> providerItem.id == stopItem.provider }
            ?: ProviderItem()
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
