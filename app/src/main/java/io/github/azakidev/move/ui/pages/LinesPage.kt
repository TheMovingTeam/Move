package io.github.azakidev.move.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.StopItem
import io.github.azakidev.move.listShape
import io.github.azakidev.move.ui.components.LineRow
import io.github.azakidev.move.ui.components.StopEmblemRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LinesPage(
    model: MoveViewModel, sheetModel: SheetStopViewModel
) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()

    val results = model.stops.collectAsState().value.filter {
        it.name.lowercase().replace(" ", "")
            .contains(textFieldState.text.toString().lowercase().replace(" ", ""))
    }

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onSearch = {
                scope.launch { searchBarState.animateToCollapsed() }
                if (textFieldState.text.isNotEmpty()) {
                    textFieldState.edit { delete(0, textFieldState.text.length) }
                    sheetModel.sheetStop = results.first()
                    sheetModel.showBottomSheet = true
                }
            },
            placeholder = { Text("Where to go...") },
            leadingIcon = {
                if (searchBarState.currentValue == SearchBarValue.Collapsed && !searchBarState.isAnimating) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                } else {
                    IconButton(
                        onClick = {
                            textFieldState.edit { delete(0, textFieldState.text.length) }
                            scope.launch { searchBarState.animateToCollapsed() }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            }
        )
    }

    val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()

    Scaffold(
        topBar = {
            AppBarWithSearch(
                modifier = Modifier.padding(bottom = 8.dp),
                state = searchBarState,
                inputField = inputField,
                colors = SearchBarDefaults.appBarWithSearchColors(
                    appBarContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior
            )
            ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {
                LazyColumn(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (textFieldState.text.isNotEmpty()) {
                        var count = 0
                        items(results.count()) {
                            val shape = listShape(count, results.count())
                            count++
                            val result = results[it]
                            SearchResultStop(
                                modifier = Modifier.fillMaxWidth(),
                                stopItem = result,
                                lines = model.lines.collectAsState().value,
                                shape = shape,
                                onClick = {
                                    sheetModel.sheetStop = result
                                    sheetModel.showBottomSheet = true
                                    model.saveLastStop(sheetModel.sheetStop.id)
                                    textFieldState.edit { delete(0, textFieldState.text.length) }
                                    scope.launch { searchBarState.animateToCollapsed() }
                                }
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        if (model.lines.collectAsState().value.count() != 0) {
            LineList(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                lineItems = model.lines.collectAsState().value,
                stopItems = model.stops.collectAsState().value,
                onClick = { stopItem ->
                    sheetModel.sheetStop = stopItem
                    sheetModel.showBottomSheet = true
                    model.saveLastStop(sheetModel.sheetStop.id)
                }
            )
        } else {
            EmptyLines(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun SearchResultStop(
    modifier: Modifier = Modifier,
    stopItem: StopItem,
    lines: List<LineItem>,
    shape: Shape,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(0.65f),
                text = stopItem.name
                    .replace("-", " - ")
                    .replace(".", ". ")
                    .replace("  ", " "),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            StopEmblemRow(
                stopItem = stopItem,
                lines = lines
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineList(
    modifier: Modifier = Modifier,
    lineItems: List<LineItem>,
    stopItems: List<StopItem>,
    onClick: (StopItem) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(lineItems.count()) {
            val lineItem = lineItems.sortedBy { line -> line.emblem }[it]
            val expanded = rememberSaveable { mutableStateOf(false) }
            val shape = listShape(it, lineItems.count())
            LineRow(
                stops = stopItems,
                lineItem = lineItem,
                shape = shape,
                expanded = expanded,
                onClick = onClick
            )
        }
        item {
            Spacer(
                modifier = Modifier.height(4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun LinesPagePreview() {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
            placeholder = { Text("Where to go...") },
        )
    }
    val lineItems = listOf(
        LineItem(id = 1),
        LineItem(id = 2),
        LineItem(id = 3),
    )
    Scaffold(
        topBar = {
            AppBarWithSearch(
                modifier = Modifier.padding(bottom = 8.dp),
                state = searchBarState,
                inputField = inputField,
                colors = SearchBarDefaults.appBarWithSearchColors(
                    appBarContainerColor = Color.Transparent
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
            placeholder = { Text("Where to go...") },
        )
    }
    Scaffold(
        topBar = {
            AppBarWithSearch(
                modifier = Modifier.padding(bottom = 8.dp),
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

@Composable
@Preview
fun SearchResultPreview() {
    SearchResultStop(
        stopItem = StopItem(
            lines = listOf(1)
        ),
        lines = listOf(
            LineItem(id = 1)
        ),
        shape = MaterialTheme.shapes.medium,
        onClick = {}
    )
}