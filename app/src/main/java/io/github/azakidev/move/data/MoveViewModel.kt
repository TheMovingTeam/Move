package io.github.azakidev.move.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.azakidev.move.BuildConfig
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
import io.github.azakidev.move.data.items.MapStyle
import io.github.azakidev.move.data.items.ProviderItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.data.items.StopKey
import io.github.azakidev.move.data.items.StopResponse
import io.github.azakidev.move.data.items.toKey
import io.github.azakidev.move.utils.LogTags
import io.github.azakidev.move.utils.fetchProviderList
import io.github.azakidev.move.utils.fetchRemoteProviders
import io.github.azakidev.move.utils.fetchStopTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Timer
import kotlin.concurrent.schedule

class MoveViewModel(application: Application) : AndroidViewModel(application) {
    private val _database = MoveDatabase.getDatabase(application.applicationContext)
    private val _providerDao = _database.providerDao()
    private val _lineDao = _database.lineDao()
    private val _stopDao = _database.stopDao()

    private val _userStore = UserStore(application.applicationContext)

    val providerRepo: StateFlow<String>
        field = MutableStateFlow("")

    val providers: StateFlow<List<ProviderItem>>
        field = MutableStateFlow(emptyList())

    val lines: StateFlow<List<LineItem>>
        field = MutableStateFlow(emptyList())

    val stops: StateFlow<List<StopItem>>
        field = MutableStateFlow(emptyList())

    val savedProviders: StateFlow<List<Int>> = _userStore.savedProvidersFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
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

    val shouldShowChangelog: StateFlow<Boolean>
        field = MutableStateFlow(false)

    private val _lastOpenedVersionCode: StateFlow<Int> = _userStore.lastOpenedVersionCodeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = -1
        )

    private val _stopsToLoad: MutableList<StopKey> = mutableListOf()

    val mapStyle: StateFlow<String> = _userStore.mapStyle
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    init {
        // Collect provider repo value
        viewModelScope.launch {
            _userStore.providerRepoUrlFlow.collect { savedUrl ->
                providerRepo.value = savedUrl
            }
        }

        // Load up providers from the DB to avoid double fetches
        viewModelScope.launch(Dispatchers.IO) {
            _providerDao.getAllProviders().collect { entities ->
                providers.value = entities.map { it.toProviderItem() }
            }
        }

        // Set up data from DB
        viewModelScope.launch(Dispatchers.IO) {
            savedProviders.collect { savedProviderIds ->
                if (savedProviderIds.isNotEmpty()) {
                    // Load providers if not already loaded, then lines/stops
                    if (providers.value.isEmpty() && providerRepo.value.isNotEmpty()) {
                        fetchProviders()
                    }

                    // Wait for providers to be available if they were just fetched
                    providers.first { it.isNotEmpty() || providerRepo.value.isEmpty() }

                    // Observing lines and stops from DB, each must be in a coroutine to not obstruct each other
                    viewModelScope.launch(Dispatchers.IO) {
                        _lineDao.getLinesForProviders(savedProviderIds).collect { lineEntities ->
                            lines.value = lineEntities.map { it.toLineItem() }
                        }
                    }

                    viewModelScope.launch(Dispatchers.IO) {
                        _stopDao.getStopsForProviders(savedProviderIds).collect { stopEntities ->
                            stops.value = stopEntities.map { it.toStopItem() }
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            Timer().schedule(delay = 1000, period = 500, action = {
                viewModelScope.launch(Dispatchers.IO) {
                    _lastOpenedVersionCode.collect { ver ->
                        val currentVersion = BuildConfig.VERSION_CODE
                        // ver should be:
                        // -1 before it's collected
                        // 0 if the app hasn't been opened before
                        // The previous version number if the app has been updated since last opened
                        if (ver != -1) {
                            _userStore.saveLastOpenedVersionCode(currentVersion)
                            if (ver < 16) {
                                // Migrate to stop keys if still using the old system
                                migrateStopKeys()
                                // Migrate providers to hash based IDs
                                migrateProviders()
                            }
                            if (ver < currentVersion) {
                                shouldShowChangelog.value = true
                            }
                            this@schedule.cancel()
                            return@collect
                        }
                    }
                }
            }).run()

            // Initiate stop fetching
            startFetchLoop()
        }
    }

    // When migrating from the old singular Int version, the 1st and second value will always be the same because there's no "," to separate them
    // This ensures it's migrated properly
    fun migrateStopKeys() {
        viewModelScope.launch {
            val migratedFavStops = _userStore.favouriteStopsFlow.first().mapNotNull {
                if (it.stopId == it.providerId) {
                    stops.value.find { stopItem -> stopItem.id == it.stopId }?.toKey()
                } else it
            }

            if (migratedFavStops.isNotEmpty() && !migratedFavStops.zip(favouriteStops.value)
                    .all { (a, b) -> a.stopId == b.stopId && a.providerId == b.providerId }
            ) {
                Log.d(LogTags.MoveModel.name, "Migrating fav stops to StopKeys")
                _userStore.saveFavouriteStops(migratedFavStops)
            }

            val migratedLastStops = _userStore.lastStopsFlow.first().mapNotNull {
                if (it.stopId == it.providerId) {
                    stops.value.find { stopItem -> stopItem.id == it.stopId }?.toKey()
                } else it
            }

            if (migratedLastStops.isNotEmpty() && !migratedLastStops.zip(lastStops.value)
                    .all { (a, b) -> a.stopId == b.stopId && a.providerId == b.providerId }
            ) {
                Log.d(LogTags.MoveModel.name, "Migrating last stops to StopKeys")
                _userStore.saveLastStops(migratedLastStops)
            }
        }
    }

    fun migrateProviders() {
        Log.d(LogTags.MoveModel.name, "Migrating providers")

        val oldSavedProviders = savedProviders.value.toMutableList()
        val providerList = fetchProviderList(providerRepo.value)
        val oldFavStops = favouriteStops.value
        val oldLastStops = lastStops.value

        val newFavStops = mutableListOf<StopKey>()
        val newLastStops = mutableListOf<StopKey>()

        val newSavedProviders = oldSavedProviders.map { index ->
            val providerName = providerList.getOrNull(index)
            val hashCode = providerName.hashCode()

            if (providerName != null) {
                oldFavStops.filter { it.providerId == index }.forEach {
                    newFavStops += StopKey(it.stopId, hashCode)
                }

                oldLastStops.filter { it.providerId == index }.forEach {
                    newLastStops += StopKey(it.stopId, hashCode)
                }
            }

            hashCode
        }

        // Update the info in DB and DataStore
        viewModelScope.launch(Dispatchers.IO) {
            providers.value.forEach {
                _providerDao.replaceProviderId(it.id, it.name.hashCode())
            }
            _userStore.saveSavedProviders(newSavedProviders)
            _userStore.saveFavouriteStops(newFavStops)
            _userStore.saveLastStops(newLastStops)
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
                return@launch
            }

            try {
                Log.i(LogTags.MoveModel.name, "Fetching providers at $currentRepoUrl")

                val freshProvidersFromRemote = fetchRemoteProviders(
                    currentRepoUrl,
                    _providerDao.getAllProviders().first().map { it.toProviderItem() }
                )

                if (freshProvidersFromRemote.isNotEmpty()) {
                    _providerDao.insertProviders(freshProvidersFromRemote.map { it.toProviderEntity() })
                }

                fetchInfoForProviders(savedProviders.value)

            } catch (e: Exception) {
                Log.e(
                    LogTags.MoveModel.name,
                    "Error fetching providers list: ${e.localizedMessage}", e
                )
            }
        }
    }

    fun fetchInfoForProviders(providerIds: List<Int>) {
        if (providers.value.isEmpty() && providerIds.isNotEmpty()) {
            Log.w(
                LogTags.MoveModel.name,
                "Provider list is empty, attempting to load/fetch providers first."
            )
            // It's possible fetchProviders hasn't completed or loaded from DB yet.
            // This call might need to be smarter, perhaps waiting for providers.
            // For now, let's assume providers will be loaded.
        }
        if (providerRepo.value.isEmpty() && providerIds.isNotEmpty()) {
            Log.w(
                LogTags.MoveModel.name,
                "Repo URL is empty, cannot fetch new lines/stops data."
            )
            // Data will be loaded from DB via the collectors if available.
            return
        }

        val client = OkHttpClient()

        providerIds.forEach { id ->
            val provider = providers.value.find { it.id == id }
            if (provider == null) {
                Log.w(
                    LogTags.MoveModel.name,
                    "Provider with ID $id not found in current list for fetching info."
                )
                // Clear it from the DB if it can't be found
                viewModelScope.launch { _providerDao.deleteProviderById(id) }
                return@forEach
            }

            viewModelScope.launch(Dispatchers.IO) {
                // Get the cached provider entity to check its lastUpdated timestamp
                // This is useful if fetchProviders decided not to re-fetch this provider's metadata
                val cachedProviderEntity = _providerDao.getProviderById(id)

                // Fetch if:
                // 1. No cached lines/stops for this provider (check DAO).
                // 2. The provider's metadata was updated more recently than the last time we fetched lines/stops for it.
                val needsRemoteFetch =
                    (cachedProviderEntity != null && provider.lastUpdated > (cachedProviderEntity.lastUpdated))

                if (needsRemoteFetch && providerRepo.value.isNotEmpty()) {
                    Log.d(
                        LogTags.MoveModel.name,
                        "Fetching lines/stops for provider ${provider.name} from remote."
                    )
                    fetchLinesForProvider(
                        client,
                        provider,
                        providerRepo.value
                    )
                    fetchStopsForProvider(
                        client,
                        provider,
                        providerRepo.value
                    )
                } else if (_stopDao.getStopsForProvider(provider.id)
                        .first()
                        .isEmpty()
                ) { // Force a fetch for the first fetch
                    Log.d(
                        LogTags.MoveModel.name,
                        "Fetching lines/stops for provider ${provider.name} from remote."
                    )
                    fetchLinesForProvider(
                        client,
                        provider,
                        providerRepo.value
                    )
                    fetchStopsForProvider(
                        client,
                        provider,
                        providerRepo.value
                    )
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
        client: OkHttpClient,
        provider: ProviderItem,
        currentRepoUrl: String
    ) {
        val linesRequest =
            Request.Builder()
                .get()
                .url("${currentRepoUrl}/${provider.name}/lines.json")
                .build()

        try {
            client.newCall(linesRequest).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val response = Json.decodeFromString<LineResponse>(response.body!!.string())

                val fetchedLines =
                    response.lines.map { it.copy(provider = provider.id).toLineEntity() }

                _lineDao.deleteLinesForProvider(provider.id)
                _lineDao.insertLines(fetchedLines)
            }
        } catch (e: IOException) {
            Log.e(LogTags.Networking.name, e.message, e)
        }
    }

    private suspend fun fetchStopsForProvider(
        client: OkHttpClient,
        provider: ProviderItem,
        currentRepoUrl: String
    ) {
        val stopsRequest =
            Request.Builder()
                .get()
                .url("${currentRepoUrl}/${provider.name}/stops.json")
                .build()

        try {
            client.newCall(stopsRequest).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val response = Json.decodeFromString<StopResponse>(response.body!!.string())

                val fetchedStops =
                    response.stops.map { it.copy(provider = provider.id).toStopEntity() }

                _stopDao.deleteStopsForProvider(provider.id)
                _stopDao.insertStops(fetchedStops)
            }
        } catch (e: IOException) {
            Log.e(LogTags.Networking.name, e.message, e)
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

        viewModelScope.launch(Dispatchers.IO) {
            val currentSaved = savedProviders.value.toMutableList()
            if (currentSaved.remove(providerId)) {
                _userStore.saveSavedProviders(currentSaved)

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
            favouriteStops.value.forEach { removeFavStop(it) }
            savedProviders.value.forEach { removeSavedProvider(it) }
            clearLastStops()
            _stopsToLoad.removeAll(_stopsToLoad)
            // Nuke all temporary data
            providers.value = emptyList()
            lines.value = emptyList()
            stops.value = emptyList()
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
            delay = 2500,
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

    fun toggleChangelog(
        value: Boolean? = null
    ) {
        if (value == null) shouldShowChangelog.value = !shouldShowChangelog.value
        else shouldShowChangelog.value = value
    }

    fun saveMapStyle(mapStyle: MapStyle) {
        viewModelScope.launch {
            _userStore.saveMapStyle(mapStyle.name)
        }
    }
}
