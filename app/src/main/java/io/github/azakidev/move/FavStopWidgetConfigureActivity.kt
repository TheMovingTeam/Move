package io.github.azakidev.move

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import io.github.azakidev.move.widget.FavStopWidget
import io.github.azakidev.move.widget.FavStopWidgetConfig
import androidx.lifecycle.lifecycleScope
import io.github.azakidev.move.ui.theme.MoveTheme
import kotlinx.coroutines.launch
import io.github.azakidev.move.data.UserStore
import io.github.azakidev.move.data.db.MoveDatabase
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.data.items.LineItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import io.github.azakidev.move.data.db.entities.toLineItem
import io.github.azakidev.move.data.db.entities.toStopItem
import io.github.azakidev.move.ui.components.SearchResultStop
import io.github.azakidev.move.ui.fmtSearch
import io.github.azakidev.move.ui.listShape

class FavStopWidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var userStore: UserStore
    private lateinit var database: MoveDatabase
    private lateinit var stopDao: io.github.azakidev.move.data.db.dao.StopDao
    private lateinit var lineDao: io.github.azakidev.move.data.db.dao.LineDao

    private var favouriteStops: List<Int> by mutableStateOf(emptyList())
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
                SelectStopScreen(
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SelectStopScreen(
    onStopSelected: (Int, Int) -> Unit = { _, _ -> },
    favouriteStops: List<Int>,
    allStops: List<StopItem>,
    allLines: List<LineItem>
) {
    val textFieldState = rememberTextFieldState()

    val favStopItems = allStops.filter { stop -> favouriteStops.contains(stop.id) }

    val filteredFavStops = favStopItems.filter { stop ->
        (stop.name.fmtSearch().contains(textFieldState.text.toString().fmtSearch())
                || stop.comId.toString().contains(textFieldState.text.toString().fmtSearch()))
                || textFieldState.text.isEmpty()
    }
    val filteredStops = allStops.filter { stop ->
        (stop.name.fmtSearch().contains(textFieldState.text.toString().fmtSearch())
                || stop.comId.toString().contains(textFieldState.text.toString().fmtSearch()))
                || textFieldState.text.isEmpty()
    }.filterNot { favStopItems.contains(it) }.sortedBy { it.name }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.favStopWidgetSelectorTitle),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                })
        }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateRightPadding(LayoutDirection.Ltr),
                    bottom = 0.dp
                )
                .fillMaxSize()
                .clip(shape = RoundedCornerShape(30.dp, 30.dp, 0.dp, 0.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .padding(horizontal = 8.dp),
                    shape = MaterialTheme.shapes.large,
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search, contentDescription = null
                        )
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.searchPlaceholder)
                        )
                    },
                    state = textFieldState,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (filteredFavStops.count() >= 1) {
                item {
                    Text(
                        text = stringResource(R.string.favouriteStops)
                    )
                }
                items(filteredFavStops.count()) {
                    val shape = listShape(it, filteredFavStops.count())
                    val result = filteredFavStops[it]

                    SearchResultStop(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                                placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                                fadeOutSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
                            ),
                        stopItem = result,
                        lines = allLines,
                        shape = shape,
                        onClick = { onStopSelected(result.id, result.provider) })
                }
            }
            if (filteredStops.count() >= 1) {
                item {
                    Text(
                        text = stringResource(R.string.allStops)
                    )
                }
                items(filteredStops.count()) {
                    val shape = listShape(it, filteredStops.count())
                    val result = filteredStops[it]
                    SearchResultStop(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                                placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                                fadeOutSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
                            ),
                        stopItem = result,
                        lines = allLines,
                        shape = shape,
                        onClick = { onStopSelected(result.id, result.provider) })
                }
                item {
                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSelectStopScreen() {
    MoveTheme {
        SelectStopScreen(
            favouriteStops = emptyList(), allStops = emptyList(), allLines = emptyList()
        )
    }
}