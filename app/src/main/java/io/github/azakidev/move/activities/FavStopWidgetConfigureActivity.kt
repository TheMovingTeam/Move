package io.github.azakidev.move.activities

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import io.github.azakidev.move.R
import io.github.azakidev.move.data.UserStore
import io.github.azakidev.move.data.db.MoveDatabase
import io.github.azakidev.move.data.db.dao.LineDao
import io.github.azakidev.move.data.db.dao.StopDao
import io.github.azakidev.move.data.db.entities.toLineItem
import io.github.azakidev.move.data.db.entities.toStopItem
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.data.items.StopKey
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.StopRow
import io.github.azakidev.move.ui.components.trailingButton
import io.github.azakidev.move.ui.fmtSearch
import io.github.azakidev.move.ui.listShape
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
    favouriteStops: List<StopKey>,
    allStops: List<StopItem>,
    allLines: List<LineItem>
) {
    val textFieldState = rememberTextFieldState()

    val favStopItems = allStops.filter { stop -> favouriteStops.map { it.stopId }.contains(stop.id) }

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

    val trailingIcon = trailingButton(
        textState = textFieldState.text.toString(),
        icon = Icons.AutoMirrored.Rounded.Backspace,
        onClick = {
            textFieldState.clearText()
        }
    )

    val fredokaFontFamily = FontFamily(
        Font(R.font.fredoka_medium, FontWeight.Medium),
        Font(R.font.fredoka_bold, FontWeight.Bold)
    )

    val titleModifier = Modifier.padding(start = PADDING.dp)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.favStopWidgetSelectorTitle),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = fredokaFontFamily,
                        style = MaterialTheme.typography.displaySmallEmphasized,
                        fontWeight = FontWeight.Bold
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
                .padding(horizontal = PADDING.div(2).dp),
            verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
        ) {
            item {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = PADDING.dp)
                        .padding(horizontal = PADDING.div(3).dp),
                    shape = MaterialTheme.shapes.large,
                    leadingIcon = {
                        Icon(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(PADDING.div(3).dp),
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = trailingIcon,
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
                Spacer(modifier = Modifier.height(PADDING.div(4).dp))
            }
            if (filteredFavStops.count() >= 1) {
                item {
                    Text(
                        modifier = titleModifier,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        text = stringResource(R.string.favouriteStops)
                    )
                }
                items(filteredFavStops.count()) {
                    val shape = listShape(it, filteredFavStops.count())
                    val result = filteredFavStops[it]

                    StopRow(
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
                        modifier = titleModifier,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        text = stringResource(R.string.allStops)
                    )
                }
                items(filteredStops.count()) {
                    val shape = listShape(it, filteredStops.count())
                    val result = filteredStops[it]
                    StopRow(
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
                            .height(PADDING.div(2).dp)
                    )
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSelectStopScreen() {

    val favStops = listOf(StopKey(1, 1))

    val stops = listOf(
        StopItem(id = 1, name = "A stop with a really really long name", lines = listOf(1, 2)),
        StopItem(id = 2, name = "Stop 2", lines = listOf(2)),
        StopItem(id = 3, name = "Stop 3", lines = listOf(1, 2))
    )
    val lines = listOf(
        LineItem(id = 1, name = "Line 1", emblem = "L1", stops = (1..3).toList()),
        LineItem(
            id = 2,
            name = "A line with an obscenely long name to which I would rather not read but I might need regardless",
            emblem = "ELNL",
            stops = listOf(1, 2, 3)
        )
    )

    MoveTheme {
        SelectStopScreen(
            favouriteStops = favStops,
            allStops = stops,
            allLines = lines
        )
    }
}