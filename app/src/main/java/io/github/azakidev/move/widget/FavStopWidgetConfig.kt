package io.github.azakidev.move.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.glance.state.GlanceStateDefinition
import java.io.File

object FavStopWidgetConfig : GlanceStateDefinition<Preferences> {
    val stopIdKey = intPreferencesKey("stop_id")
    val providerIdKey = intPreferencesKey("provider_id")

    override fun getLocation(
        context: Context,
        fileKey: String // In this case, fileKey is often ignored if using a single predefined DataStore
    ): File {
        return context.preferencesDataStoreFile("fav_stop_widget_$fileKey")
    }

    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            getLocation(context, fileKey)
        }
    }
}
