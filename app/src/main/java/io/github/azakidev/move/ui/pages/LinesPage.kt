package io.github.azakidev.move.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.LineRow
import io.github.azakidev.move.ui.components.SearchContents
import io.github.azakidev.move.ui.components.SearchInputField
import io.github.azakidev.move.ui.fmtSearch
import io.github.azakidev.move.ui.listShape
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LinesPage(
    modifier: Modifier = Modifier,
    model: MoveViewModel,
    sheetModel: SheetStopViewModel,
    appBarCanScroll: Boolean = true,
) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()

    val inputField = @Composable {
        SearchInputField(
            searchBarState,
            textFieldState
        )
    }

    val scrollBehavior = if (appBarCanScroll) {
        SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    } else {
        null
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            AppBarWithSearch(
                modifier = Modifier.padding(bottom = PADDING.div(2).dp),
                state = searchBarState,
                inputField = inputField,
                colors = SearchBarDefaults.appBarWithSearchColors(
                    appBarContainerColor = Color.Transparent,
                    scrolledAppBarContainerColor = Color.Transparent,
                ),
                scrollBehavior = scrollBehavior
            )
            ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {
                SearchContents(
                    model,
                    sheetModel,
                    textFieldState,
                    searchBarState,
                    textFieldState.text.toString().fmtSearch()
                )
            }
        },
    ) { paddingValues ->
        val modifier = if (scrollBehavior != null) {
            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        } else {
            Modifier
        }
        if (model.lines.collectAsState().value.count() != 0) {
            LineList(
                modifier = modifier
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = 0.dp
                    )
                    .background(MaterialTheme.colorScheme.background),
                lineItems = model.lines.collectAsState().value,
                stopItems = model.stops.collectAsState().value,
                onClick = { stopItem ->
                    sheetModel.sheetStop = stopItem
                    sheetModel.showBottomSheet = true
                    val stopKey = Pair(sheetModel.sheetStop.id, sheetModel.sheetStop.provider)
                    model.saveLastStop(stopKey)
                }
            )
        } else {
            EmptyLines(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LineList(
    modifier: Modifier = Modifier,
    lineItems: List<LineItem>,
    stopItems: List<StopItem>,
    onClick: (StopItem) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = PADDING.times(0.75).dp),
        verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp),
    ) {
        items(lineItems.count()) {
            val lineItem = lineItems
                .sortedBy { line -> line.emblem }
                .sortedBy { line ->
                    val emblem =
                        if (!line.emblem.first().isDigit()) line.emblem.drop(1) else line.emblem
                    emblem
                        .removePrefix("L")
                        .takeWhile { c -> c.isDigit() }
                        .toIntOrNull() ?: Int.MAX_VALUE
                }[it]
            val expanded = rememberSaveable { mutableStateOf(false) }
            val shape = listShape(it, lineItems.count())
            LineRow(
                modifier = Modifier
                    .animateItem(
                        fadeInSpec = MotionScheme.expressive().defaultEffectsSpec(),
                        placementSpec = MotionScheme.expressive().defaultSpatialSpec(),
                        fadeOutSpec = MotionScheme.expressive().defaultEffectsSpec()
                    ),
                stops = stopItems,
                lines = lineItems,
                lineItem = lineItem,
                shape = shape,
                expanded = expanded,
                onClick = onClick
            )
        }
        item {
            Spacer(
                modifier = Modifier.height(PADDING.div(4).dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun LinesPagePreview(
    modifier: Modifier = Modifier,
) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
            placeholder = {
                Text(
                    text = stringResource(R.string.searchPlaceholder)
                )
            },
        )
    }
    val lineItems = listOf(
        LineItem(id = 1),
        LineItem(id = 2),
        LineItem(id = 3),
    )
    Scaffold(
        modifier = modifier,
        topBar = {
            AppBarWithSearch(
                modifier = Modifier.padding(bottom = PADDING.div(2).dp),
                state = searchBarState,
                inputField = inputField,
                colors = SearchBarDefaults.appBarWithSearchColors(
                    appBarContainerColor = Color.Transparent,
                    scrolledAppBarContainerColor = Color.Transparent,
                ),
            )
            ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {}
        },
    ) { paddingValues ->
        LineList(
            modifier = Modifier.padding(paddingValues),
            lineItems = lineItems,
            stopItems = emptyList(),
            onClick = {}
        )
    }
}

@Composable
fun EmptyLines(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.noLines),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun EmptyLinesPreview() {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
            placeholder = {
                Text(
                    text = stringResource(R.string.searchPlaceholder)
                )
            },
        )
    }
    Scaffold(
        topBar = {
            AppBarWithSearch(
                modifier = Modifier.padding(bottom = PADDING.div(2).dp),
                state = searchBarState,
                inputField = inputField,
                colors = SearchBarDefaults.appBarWithSearchColors(
                    appBarContainerColor = Color.Transparent
                ),
            )
            ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {}
        },
    ) { paddingValues ->
        EmptyLines(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}
