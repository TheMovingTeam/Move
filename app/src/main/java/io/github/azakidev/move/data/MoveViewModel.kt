package io.github.azakidev.move.data

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.LogTags
import io.github.azakidev.move.data.db.MoveDatabase
import io.github.azakidev.move.data.db.toLineEntity
import io.github.azakidev.move.data.db.toLineItem
import io.github.azakidev.move.data.db.toProviderEntity
import io.github.azakidev.move.data.db.toProviderItem
import io.github.azakidev.move.data.db.toStopEntity
import io.github.azakidev.move.data.db.toStopItem
import io.github.azakidev.move.formRequest
import io.github.azakidev.move.parseTimes
import io.github.azakidev.move.trustSelfSignedCertsIfNeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.util.Timer
import java.util.concurrent.LinkedBlockingDeque
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

class MoveViewModel(application: Application) : AndroidViewModel(application) {
    private val _database = MoveDatabase.getDatabase(application.applicationContext)
    private val _providerDao = _database.providerDao()
    private val _lineDao = _database.lineDao()
    private val _stopDao = _database.stopDao()

    private val _userStore = UserStore(application.applicationContext)

    private val _providerRepo: StateFlow<String> = _userStore.providerRepoUrlFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep active for 5s after last subscriber
            initialValue = if (BuildConfig.APPLICATION_ID.contains("debug")) // Initial fallback
                "https://raw.githubusercontent.com/TheMovingTeam/Providers/refs/heads/testing"
            else "https://raw.githubusercontent.com/TheMovingTeam/Providers/refs/heads/main"
        )
    var providerRepo: MutableState<String> = mutableStateOf(_providerRepo.value)
    private val _providers: MutableStateFlow<List<ProviderItem>> = MutableStateFlow(emptyList())
    var providers = _providers.asStateFlow()
    val savedProviders: StateFlow<List<Int>> = _userStore.savedProvidersFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private val _lines: MutableStateFlow<List<LineItem>> = MutableStateFlow(emptyList())
    var lines = _lines.asStateFlow()
    private val _stops: MutableStateFlow<List<StopItem>> = MutableStateFlow(emptyList())
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

    val onboardingStatus: StateFlow<Boolean> = _userStore.onboardingFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _stopsToLoad: MutableList<Int> = mutableListOf()

    init {
        // Collect provider repo value DO NOT DELETE
        viewModelScope.launch {
            _providerRepo.collect { savedUrl ->
                providerRepo.value = savedUrl
            }
        }
        // Set up data from DB
        viewModelScope.launch {
            savedProviders.collect { savedProviderIds ->
                if (savedProviderIds.isNotEmpty()) {
                    // Load providers if not already loaded, then lines/stops
                    if (_providers.value.isEmpty() && providerRepo.value.isNotEmpty()) {
                        fetchProviders() // This will load from DB or fetch if necessary
                    }
                    // Wait for providers to be available if they were just fetched
                    providers.first { it.isNotEmpty() || providerRepo.value.isEmpty() }

                    // Observing lines and stops from DB, each must be in a coroutine to not obstruct each other
                    viewModelScope.launch(Dispatchers.IO) {
                        _lineDao.getLinesForProviders(savedProviderIds).collect { lineEntities ->
                            _lines.value = lineEntities.map { it.toLineItem() }
                        }
                    }

                    viewModelScope.launch(Dispatchers.IO) {
                        _stopDao.getStopsForProviders(savedProviderIds).collect { stopEntities ->
                            _stops.value = stopEntities.map { it.toStopItem() }
                        }
                    }
                    // Trigger a check/fetch for these saved providers
                    fetchInfoForProviders(savedProviderIds)
                } else {
                    _lines.value = emptyList()
                    _stops.value = emptyList()
                }
            }
        }
        // Initial load of providers from DB when ViewModel is created
        loadProvidersFromDb()
        // Initiate stop fetching
        startFetchLoop()
    }

    fun fetchProviders() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentRepoUrl = providerRepo.value
            if (currentRepoUrl.isEmpty()) {
                Log.w(
                    LogTags.MoveModel.name,
                    "Provider repo URL is empty, cannot fetch providers."
                )
                // Optionally load from DB if repo URL is not set but you have cached providers
                loadProvidersFromDb()
                return@launch
            }

            try {
                // 1. Fetch provider list (names) from remote
                val providerListJson = URL("${currentRepoUrl}/providers.json").readText()
                val providerNameResponse =
                    Json.decodeFromString<ProviderListResponse>(providerListJson)

                // 2. Get currently cached providers' metadata (ID and lastUpdated)
                val cachedProviders = _providerDao.getAllProviders().first() // Get current snapshot

                val freshProvidersFromRemote = mutableListOf<ProviderItem>()

                // 3. Compare and decide which providers need full metadata fetching
                providerNameResponse.providers.forEach { providerName ->
                    try {
                        val providerMetadataJson =
                            URL("${currentRepoUrl}/${providerName}/metadata.json").readText()
                        val remoteProviderItem =
                            Json.decodeFromString<ProviderItem>(
                                providerMetadataJson
                            )

                        val cachedProvider = cachedProviders.find { it.id == remoteProviderItem.id }

                        if (cachedProvider == null || remoteProviderItem.lastUpdated > cachedProvider.lastUpdated) {
                            // Needs fetching/updating or is new
                            freshProvidersFromRemote.add(remoteProviderItem)
                        } else {
                            // Cached version is up-to-date, add it to our list for UI (if not already there)
                            // This step ensures that even if not fetched, saved providers are loaded from DB
                            // _providers StateFlow should be updated with a mix of fresh and valid cached data.
                        }
                    } catch (e: Exception) {
                        Log.e(
                            LogTags.Networking.name,
                            "Error fetching metadata for $providerName: ${e.localizedMessage}",
                            e
                        )
                    }
                }

                // 4. Insert/Update fresh providers in DB
                if (freshProvidersFromRemote.isNotEmpty()) {
                    _providerDao.insertProviders(freshProvidersFromRemote.map { it.toProviderEntity() })
                }

                // 5. Update the _providers StateFlow by loading all (including just updated) from DB
                loadProvidersFromDb() // This will now include the fresh data

                // 6. Fetch info for saved providers (lines and stops)
                // This should also respect the lastUpdated timestamp for lines/stops if available
                fetchInfoForProviders(
                    savedProviders.value
                )

            } catch (e: Exception) {
                Log.e(
                    LogTags.MoveModel.name,
                    "Error fetching providers list: ${e.localizedMessage}", e
                )
                // Fallback to loading from DB if network fails
                loadProvidersFromDb()
            }
        }
    }

    private fun loadProvidersFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            _providerDao.getAllProviders().collect { entities ->
                _providers.value = entities.map { it.toProviderItem() }
            }
        }
    }

    fun fetchInfoForProviders(providerIds: List<Int>) {
        if (_providers.value.isEmpty() && providerIds.isNotEmpty()) {
            Log.w(
                LogTags.MoveModel.name,
                "Provider list is empty, attempting to load/fetch providers first."
            )
            // It's possible fetchProviders hasn't completed or loaded from DB yet.
            // This call might need to be smarter, perhaps waiting for providers.
            // For now, let's assume providers will be loaded.
        }
        val currentRepoUrl = providerRepo.value
        if (currentRepoUrl.isEmpty() && providerIds.isNotEmpty()) {
            Log.w(
                LogTags.MoveModel.name,
                "Repo URL is empty, cannot fetch new lines/stops data."
            )
            // Data will be loaded from DB via the collectors if available.
            return
        }

        providerIds.forEach { id ->
            val provider = _providers.value.find { it.id == id }
            if (provider == null) {
                Log.w(
                    LogTags.MoveModel.name,
                    "Provider with ID $id not found in current list for fetching info."
                )
                return@forEach
            }

            // Get the cached provider entity to check its lastUpdated timestamp
            // This is useful if fetchProviders decided not to re-fetch this provider's metadata
            viewModelScope.launch(Dispatchers.IO) {
                val cachedProviderEntity = _providerDao.getProviderById(id)

                // Fetch if:
                // 1. No cached lines/stops for this provider (check DAO).
                // 2. The provider's metadata was updated more recently than the last time we fetched lines/stops for it.
                val needsRemoteFetch =
                    (cachedProviderEntity != null && provider.lastUpdated > (cachedProviderEntity.lastUpdated)) // Simplified: if provider metadata is newer

                if (needsRemoteFetch && currentRepoUrl.isNotEmpty()) {
                    Log.d(
                        LogTags.MoveModel.name,
                        "Fetching lines/stops for provider ${provider.name} from remote."
                    )
                    fetchLinesForProvider(provider, currentRepoUrl)
                    fetchStopsForProvider(provider, currentRepoUrl)
                } else if (_stopDao.getStopsForProvider(provider.id)
                        .first()
                        .isEmpty()
                ) { // Force a fetch for the first fetch
                    Log.d(
                        LogTags.MoveModel.name,
                        "Fetching lines/stops for provider ${provider.name} from remote."
                    )
                    fetchLinesForProvider(provider, currentRepoUrl)
                    fetchStopsForProvider(provider, currentRepoUrl)
                } else {
                    Log.d(
                        LogTags.MoveModel.name,
                        "Lines/stops for provider ${provider.name} should be up-to-date or loaded from cache."
                    )
                }
            }
        }
    }

    private suspend fun fetchLinesForProvider(
        provider: ProviderItem,
        repoUrl: String
    ) {
        try {
            val linesJson = URL("$repoUrl/${provider.name}/lines.json").readText()
            val response = Json.decodeFromString<LineResponse>(linesJson)
            val fetchedLineItems = response.lines.map { it.apply { this.provider = provider.id } }

            val lineEntities = fetchedLineItems.map { it.toLineEntity() }
            _lineDao.deleteLinesForProvider(provider.id)
            _lineDao.insertLines(lineEntities)
            // The Flow from lineDao will automatically update _lines.value
        } catch (e: Exception) {
            Log.e(
                LogTags.MoveModel.name,
                "Error fetching lines for ${provider.name}: ${e.localizedMessage}",
                e
            )
        }
    }

    private suspend fun fetchStopsForProvider(
        provider: ProviderItem,
        repoUrl: String
    ) {
        try {
            val stopsJson = URL("$repoUrl/${provider.name}/stops.json").readText()
            val response = Json.decodeFromString<StopResponse>(stopsJson)
            val fetchedStopItems = response.stops.map { it.apply { this.provider = provider.id } }

            val stopEntities = fetchedStopItems.map { it.toStopEntity() }
            _stopDao.deleteStopsForProvider(provider.id)
            _stopDao.insertStops(stopEntities)
            // The Flow from stopDao will automatically update _stops.value
        } catch (e: Exception) {
            Log.e(
                LogTags.MoveModel.name,
                "Error fetching stops for ${provider.name}: ${e.localizedMessage}",
                e
            )
        }
    }

    fun addSavedProvider(providerId: Int) {
        viewModelScope.launch {
            val currentSaved = savedProviders.value.toMutableList()
            if (!currentSaved.contains(providerId)) {
                currentSaved.add(providerId)
                _userStore.saveSavedProviders(currentSaved)
            }
            fetchInfoForProviders(currentSaved)
        }
    }

    // When a saved provider is removed, also remove its lines and stops from the DB
    fun removeSavedProvider(providerId: Int) {

        val stopsToStopLoading =
            _stopsToLoad.filter { stops.value.find { stop -> stop.id == it && stop.provider == providerId } != null }

        val favStopsToKeep =
            favouriteStops.value.filterNot { stops.value.find { stop -> stop.id == it && stop.provider == providerId } != null }

        val lastStopsToKeep =
            lastStops.value.filterNot { stops.value.find { stop -> stop.id == it && stop.provider == providerId } != null }
        
        viewModelScope.launch {
            _stopsToLoad -= stopsToStopLoading
            _userStore.saveFavouriteStops(favStopsToKeep)
            _userStore.saveLastStops(lastStopsToKeep)
        }

        viewModelScope.launch(Dispatchers.IO) { // Perform DB ops on IO dispatcher
            val currentSaved = savedProviders.value.toMutableList()
            if (currentSaved.remove(providerId)) {
                _userStore.saveSavedProviders(currentSaved) // Update DataStore

                // Remove related data from local database
                _lineDao.deleteLinesForProvider(providerId)
                _stopDao.deleteStopsForProvider(providerId)

                Log.d(
                    LogTags.MoveModel.name,
                    "Removed provider $providerId and its data."
                )
            }
        }
    }

    fun flushInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            // Nuke all user settings
            favouriteStops.value.map {
                removeFavStop(it)
            }
            savedProviders.value.map {
                removeSavedProvider(it)
            }
            clearLastStops()
            _stopsToLoad.removeAll(_stopsToLoad)
            // Nuke all temporary data
            _providers.value = emptyList()
            _lines.value = emptyList()
            _stops.value = emptyList()
            // Nuke all entries from database
            _providerDao.clearAllProviders()
            _lineDao.clearAllLines()
            _stopDao.clearAllStops()
        }
    }

    fun addFavStop(stopId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentFavStops = favouriteStops.value.toMutableList()
            if (!currentFavStops.contains(stopId)) {
                currentFavStops.add(stopId)
                _userStore.saveFavouriteStops(currentFavStops)
            }
        }
    }

    fun removeFavStop(stopId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentFavStops = favouriteStops.value.toMutableList()
            if (currentFavStops.remove(stopId)) {
                _userStore.saveFavouriteStops(currentFavStops)
            }
        }
    }

    fun saveLastStop(stopId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentLastStops = lastStops.value.toMutableList()
            if (!currentLastStops.contains(stopId)) {
                currentLastStops.add(stopId)
                _userStore.saveLastStops(currentLastStops)
            } else if (currentLastStops.last() != stopId) {
                currentLastStops.remove(stopId)
                _userStore.saveLastStops(currentLastStops)
                currentLastStops += stopId
                _userStore.saveLastStops(currentLastStops)
            }
            if (currentLastStops.count() > 5) {
                currentLastStops.removeAt(index = 0)
                _userStore.saveLastStops(currentLastStops)
            }
        }
    }

    fun clearLastStops() {
        viewModelScope.launch(Dispatchers.IO) {
            _userStore.saveLastStops(emptyList())
        }
    }

    fun tryRepo(url: String): Boolean {
        val isValid = LinkedBlockingDeque<Boolean>()
        thread {
            try {
                val providerListJson = URL("${url}/providers.json").readText()
                Json.decodeFromString<ProviderListResponse>(providerListJson)
                isValid.add(true)
            } catch (e: Exception) {
                Log.e(
                    LogTags.MoveModel.name,
                    "The repo at $url could not be verified: ${e.message}",
                    e
                )
                isValid.add(false)
                return@thread
            }
        }
        return isValid.take()
    }

    fun saveRepo(url: String) {
        viewModelScope.launch {
            _userStore.saveProviderRepoUrl(url)
        }
    }

    fun fetchTimes(stopItem: StopItem) {
        val provider =
            providers.value.find { providerItem -> providerItem.id == stopItem.provider }
                ?: ProviderItem()
        if (!(provider.capabilities.contains(Capabilities.Time) || provider.capabilities.contains(
                Capabilities.DoubleTime
            ))
        ) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val url = provider.timeSource
                .replace("@stop", stopItem.id.toString())
                .replace("@comId", stopItem.comId.toString())

            val client = OkHttpClient.Builder()
                .trustSelfSignedCertsIfNeeded(provider)
                .build()
            val request = Request.Builder()

            try {
                val requestBuilt = request.formRequest(client, provider).url(url).build()

                val response = client
                    .newCall(requestBuilt)
                    .execute()

                val responseText = response.body!!.string()
                try {
                    val times =
                        parseTimes(responseText, provider, stopItem, lines.value) ?: emptyList()
                    if (times.isNotEmpty()) {
                        stopItem.setTimeTable(times)
                    }
                } catch (e: Exception) {
                    Log.e(
                        LogTags.MoveModel.name,
                        "Could not parse times for ${stopItem.name}: $e",
                        e
                    )
                    return@launch
                }
            } catch (e: Exception) {
                Log.e(
                    LogTags.Networking.name,
                    "Could not get times for ${stopItem.name}: $e",
                    e
                )
                return@launch
            }
        }
    }

    private fun startFetchLoop() {
        // Initial adding of favourite stops
        _stopsToLoad += favouriteStops.value
        // Start the timer
        Timer().schedule(
            delay = 1000,
            period = 15000,
            action = {
                Log.d(
                    LogTags.MoveModel.name,
                    "Stops to fetch: $_stopsToLoad"
                )
                _stopsToLoad.forEach { id ->
                    val stop = stops.value.find { stop -> stop.id == id }
                    if (stop != null) {
                        fetchTimes(stop)
                    }
                }
            }
        )
    }

    fun addToFetchLoop(stopId: Int) {
        if (!_stopsToLoad.contains(stopId)) { // Avoid duplicates
            _stopsToLoad.add(stopId)
        }
    }

    fun removeToFetchLoop(stopId: Int) {
        if (!favouriteStops.value.contains(stopId)) { // Only remove if it's NOT in favourites
            _stopsToLoad.remove(stopId)
        }
    }

    fun saveOnboarding(status: Boolean) {
        viewModelScope.launch {
            _userStore.saveOnboardingStatus(status)
        }
    }
}
