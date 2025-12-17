package io.github.azakidev.move.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.azakidev.move.data.items.StopKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserStore(private val context: Context) {
    // Define a key for the providerRepo URL
    private object PreferencesKeys {
        val PROVIDER_REPO_URL = stringPreferencesKey("provider_repo_url")
        val FAVOURITE_STOPS_IDS = stringSetPreferencesKey("favourite_stops_ids")
        val LAST_STOPS_IDS = stringSetPreferencesKey("last_stops_ids")
        val SAVED_PROVIDERS_IDS = stringSetPreferencesKey("saved_providers_ids")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val LAST_OPENED_VERSION_CODE = intPreferencesKey("last_opened_version_code")
    }

    // Default URL if nothing is saved yet
    private val defaultRepoUrl = "https://raw.githubusercontent.com/TheMovingTeam/Providers/refs/heads/main"

    // Provider Repo
    val providerRepoUrlFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // No type safety for Preferences, so we use the safe call operator ?.
            // and provide a default value if the key is not found.
            preferences[PreferencesKeys.PROVIDER_REPO_URL] ?: defaultRepoUrl
        }

    suspend fun saveProviderRepoUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROVIDER_REPO_URL] = url
        }
    }

    // Saved Providers
    val savedProvidersFlow: Flow<List<Int>> = context.dataStore.data
        .map { preferences ->
            (preferences[PreferencesKeys.SAVED_PROVIDERS_IDS] ?: emptySet()).mapNotNull { it.toIntOrNull() }
        }

    suspend fun saveSavedProviders(providerIds: List<Int>) {
        val stringSet = providerIds.map { it.toString() }.toSet()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVED_PROVIDERS_IDS] = stringSet
        }
    }

    // Favourite Stops
    val favouriteStopsFlow: Flow<List<StopKey>> = context.dataStore.data
        .map { preferences ->
            // Read as Set<String>, then convert to List<Int>
            // Provide an empty set as default if not found
            (preferences[PreferencesKeys.FAVOURITE_STOPS_IDS] ?: emptySet()).map { pair ->
                val intPair = pair.split(",").mapNotNull{ digit -> digit.toIntOrNull() }
                StopKey(intPair.first(), intPair.last())
            }
        }

    /*
        stopPairs is a list of pairs where the first digit is the stopId and the second is the providerId
     */
    suspend fun saveFavouriteStops(stopKeys: List<StopKey>) {
        val stringSet = stopKeys.map { "${it.stopId},${it.providerId}" }.toSet()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FAVOURITE_STOPS_IDS] = stringSet
        }
    }

    // Favourite Stops
    val lastStopsFlow: Flow<List<StopKey>> = context.dataStore.data
        .map { preferences ->
            // Read as Set<String>, then convert to List<Int>
            // Provide an empty set as default if not found
            (preferences[PreferencesKeys.LAST_STOPS_IDS] ?: emptySet()).map { pair ->
                val intPair = pair.split(",").mapNotNull{ digit -> digit.toIntOrNull() }
                StopKey(intPair.first(), intPair.last())
            }
        }

    suspend fun saveLastStops(stopKeys: List<StopKey>) {
        val stringSet = stopKeys.map { "${it.stopId},${it.providerId}" }.toSet()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_STOPS_IDS] = stringSet
        }
    }

    // Onboarding status
    val onboardingFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // Read as Set<String>, then convert to List<Int>
            // Provide an empty set as default if not found
            (preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false)
        }

    suspend fun saveOnboardingStatus(status: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = status
        }
    }

    // Last opened version code
    val lastOpenedVersionCodeFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_OPENED_VERSION_CODE] ?: 0
        }

    suspend fun saveLastOpenedVersionCode(versionCode: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_OPENED_VERSION_CODE] = versionCode
        }
    }
}