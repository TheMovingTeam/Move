package io.github.azakidev.move.ui.pages.panes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.data.items.toKey
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.common.LineRow
import io.github.azakidev.move.ui.components.search.SearchContents
import io.github.azakidev.move.ui.components.search.SearchInputField
import io.github.azakidev.move.ui.fmtSearch
import io.github.azakidev.move.ui.listShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LinesPage(
    modifier: Modifier = Modifier,
    model: MoveViewModel,
    sheetModel: SheetStopViewModel,
) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()

    val inputField = @Composable {
        SearchInputField(
            searchBarState,
            textFieldState
        )
    }

    val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()

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
        if (model.lines.collectAsState().value.count() != 0) {
            LineList(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .background(MaterialTheme.colorScheme.background),
                paddingValues = paddingValues,
                lineItems = model.lines.collectAsState().value,
                stopItems = model.stops.collectAsState().value,
                onClick = { stopItem ->
                    sheetModel.sheetStop = stopItem
                    sheetModel.showBottomSheet = true
                    model.saveLastStop(sheetModel.sheetStop.toKey())
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
    paddingValues: PaddingValues = PaddingValues(0.dp),
    lineItems: List<LineItem>,
    stopItems: List<StopItem>,
    onClick: (StopItem) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .consumeWindowInsets(paddingValues)
            .fillMaxHeight()
            .padding(horizontal = PADDING.times(0.75).dp),
        contentPadding = paddingValues,
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
fun LinesPagePreview(
    modifier: Modifier = Modifier,
) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()

    val inputField = @Composable { SearchInputField(searchBarState, textFieldState) }

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
            paddingValues = paddingValues,
            lineItems = lineItems,
            stopItems = emptyList(),
            onClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun EmptyLinesPreview() {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()

    val inputField = @Composable { SearchInputField(searchBarState, textFieldState) }

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
