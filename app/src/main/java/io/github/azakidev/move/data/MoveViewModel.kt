package io.github.azakidev.move.data

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.utils.LogTags
import io.github.azakidev.move.data.db.MoveDatabase
import io.github.azakidev.move.data.db.entities.toLineEntity
import io.github.azakidev.move.data.db.entities.toLineItem
import io.github.azakidev.move.data.db.entities.toProviderEntity
import io.github.azakidev.move.data.db.entities.toProviderItem
import io.github.azakidev.move.data.db.entities.toStopEntity
import io.github.azakidev.move.data.db.entities.toStopItem
import io.github.azakidev.move.data.items.Capabilities
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.LineResponse
import io.github.azakidev.move.data.items.ProviderItem
import io.github.azakidev.move.data.items.ProviderListResponse
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.data.items.StopKey
import io.github.azakidev.move.data.items.StopResponse
import io.github.azakidev.move.data.items.toKey
import io.github.azakidev.move.utils.fetchStopTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
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
    val favouriteStops: StateFlow<List<StopKey>> = _userStore.favouriteStopsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val lastStops: StateFlow<List<StopKey>> = _userStore.lastStopsFlow
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

    val shouldShowChangelog = mutableStateOf(false)

    private val _lastOpenedVersionCode: StateFlow<Int> = _userStore.lastOpenedVersionCodeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = -1
        )

    private val _stopsToLoad: MutableList<StopKey> = mutableListOf()

    init {
        // Collect provider repo value
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

        // When migrating from the old singular Int version, the 1st and second value will always be the same because there's no "," to separate them
        // This ensures it's migrated properly
        viewModelScope.launch {
            val migratedFavStops = _userStore.favouriteStopsFlow.first().mapNotNull {
                if (it.stopId == it.providerId) {
                    _stops.value.find { stopItem -> stopItem.id == it.stopId }?.toKey()
                } else it
            }

            if (migratedFavStops.isNotEmpty() && migratedFavStops != favouriteStops) {
                _userStore.saveFavouriteStops(migratedFavStops)
            }

            val migratedLastStops = _userStore.lastStopsFlow.first().mapNotNull {
                if (it.stopId == it.providerId) {
                    _stops.value.find { stopItem -> stopItem.id == it.stopId }?.toKey()
                } else it
            }

            if (migratedLastStops.isNotEmpty() && migratedLastStops != lastStops) {
                _userStore.saveLastStops(migratedLastStops)
            }
        }

        // Initiate stop fetching
        startFetchLoop()

        viewModelScope.launch {
            Timer().schedule(delay = 500, period = 500, action = {
                viewModelScope.launch {
                    _lastOpenedVersionCode.collect { ver ->
                        val currentVersion = BuildConfig.VERSION_CODE
                        // ver should be:
                        // -1 before it's properly collected
                        // 0 if the app hasn't been opened before
                        // The previous version number if the app has been updated
                        if (ver != -1) {
                            _userStore.saveLastOpenedVersionCode(currentVersion)
                            if (currentVersion > ver) {
                                shouldShowChangelog.value = true
                            }
                            this@schedule.cancel()
                            return@collect
                        }
                    }
                }
            }).run()
        }
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

                if (freshProvidersFromRemote.isNotEmpty()) {
                    _providerDao.insertProviders(freshProvidersFromRemote.map { it.toProviderEntity() })
                }

                loadProvidersFromDb()

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
                    (cachedProviderEntity != null && provider.lastUpdated > (cachedProviderEntity.lastUpdated))

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
            _stopsToLoad.filter { it.providerId == providerId }

        val favStopsToKeep =
            favouriteStops.value.filterNot { it.providerId == providerId }

        val lastStopsToKeep =
            lastStops.value.filterNot { it.providerId == providerId }

        viewModelScope.launch {
            _stopsToLoad -= stopsToStopLoading.toSet()
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

    fun addFavStop(stopKey: StopKey) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentFavStops = favouriteStops.value.toMutableList()
            if (!currentFavStops.contains(stopKey)) {
                currentFavStops.add(stopKey)
                _userStore.saveFavouriteStops(currentFavStops)
            }
        }
    }

    fun removeFavStop(stopKey: StopKey) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentFavStops = favouriteStops.value.toMutableList()
            if (currentFavStops.remove(stopKey)) {
                _userStore.saveFavouriteStops(currentFavStops)
            }
        }
    }

    fun saveLastStop(stopKey: StopKey) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentLastStops = lastStops.value.toMutableList()
            if (!currentLastStops.contains(stopKey)) {
                currentLastStops.add(stopKey)
                _userStore.saveLastStops(currentLastStops)
            } else if (currentLastStops.last() != stopKey) {
                currentLastStops.remove(stopKey)
                _userStore.saveLastStops(currentLastStops)
                currentLastStops += stopKey
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
        val provider = providers.value.find { providerItem -> providerItem.id == stopItem.provider }

        if (provider == null) {
            Log.w(LogTags.Networking.name, "Provider cannot be null")
            return
        }

        if (!(provider.capabilities.contains(Capabilities.Time))) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            fetchStopTime(
                provider,
                stopItem,
                lines.value
            )
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
                _stopsToLoad.forEach {
                    val stop = stops.value.find { stop -> stop.id == it.stopId }
                    if (stop != null) {
                        fetchTimes(stop)
                    }
                }
            }
        )
    }

    fun addToFetchLoop(stopKey: StopKey) {
        if (!_stopsToLoad.contains(stopKey)) { // Avoid duplicates

            val stopItem =
                stops.value.find { it.id == stopKey.stopId && it.provider == stopKey.providerId }
            if (stopItem != null) {
                fetchTimes(stopItem) // Force first fetch
            }

            _stopsToLoad.add(stopKey)
        }
    }

    fun removeToFetchLoop(stopKey: StopKey) {
        if (!favouriteStops.value.contains(stopKey)) { // Only remove if it's NOT in favourites
            _stopsToLoad.remove(stopKey)
        }
    }

    fun saveOnboarding(status: Boolean) {
        viewModelScope.launch {
            _userStore.saveOnboardingStatus(status)
        }
    }
}
