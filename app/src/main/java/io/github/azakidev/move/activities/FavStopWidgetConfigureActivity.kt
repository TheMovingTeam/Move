package io.github.azakidev.move.activities

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import io.github.azakidev.move.data.UserStore
import io.github.azakidev.move.data.db.MoveDatabase
import io.github.azakidev.move.data.db.dao.LineDao
import io.github.azakidev.move.data.db.dao.StopDao
import io.github.azakidev.move.data.db.entities.toLineItem
import io.github.azakidev.move.data.db.entities.toStopItem
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.data.items.StopKey
import io.github.azakidev.move.ui.pages.panes.widget.SelectStopPage
import io.github.azakidev.move.ui.theme.MoveTheme
import io.github.azakidev.move.widget.FavStopWidget
import io.github.azakidev.move.widget.FavStopWidgetConfig
import kotlinx.coroutines.launch

class FavStopWidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var userStore: UserStore
    private lateinit var database: MoveDatabase
    private lateinit var stopDao: StopDao
    private lateinit var lineDao: LineDao

    private var favouriteStops: List<StopKey> by mutableStateOf(emptyList())
    private var allStops: List<StopItem> by mutableStateOf(emptyList())
    private var allLines: List<LineItem> by mutableStateOf(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userStore = UserStore(applicationContext)
        database = MoveDatabase.getDatabase(applicationContext)
        stopDao = database.stopDao()
        lineDao = database.lineDao()

        lifecycleScope.launch {
            userStore.favouriteStopsFlow.collect {
                favouriteStops = it
            }
        }
        lifecycleScope.launch {
            stopDao.getAllStops().collect { entities ->
                allStops = entities.map { it.toStopItem() }
            }
        }
        lifecycleScope.launch {
            lineDao.getAllLines().collect { entities ->
                allLines = entities.map { it.toLineItem() }
            }
        }

        // Set the result to CANCELED. This will be changed to OK if the user
        // selects a stop.
        setResult(RESULT_CANCELED)

        // Find the widget id from the intent.
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If the widget id is invalid, finish the activity.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        enableEdgeToEdge()

        setContent {
            MoveTheme {
                SelectStopPage(
                    onStopSelected = { selectedStopId, selectedProviderId ->
                        // Store the selected stop ID and provider ID for this widget instance
                        val context = applicationContext
                        lifecycleScope.launch {
                            val glanceId =
                                GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                            updateAppWidgetState(context, FavStopWidgetConfig, glanceId) { prefs ->
                                prefs.toMutablePreferences().apply {
                                    set(intPreferencesKey("stop_id"), selectedStopId)
                                    set(intPreferencesKey("provider_id"), selectedProviderId)
                                }
                            }
                            FavStopWidget().update(context, glanceId)

                            // Return OK result and finish activity
                            val resultValue =
                                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            setResult(RESULT_OK, resultValue)
                            finish()
                        }
                    }, favouriteStops = favouriteStops, allStops = allStops, allLines = allLines
                )
            }
        }
    }
}