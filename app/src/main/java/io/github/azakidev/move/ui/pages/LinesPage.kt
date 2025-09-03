package io.github.azakidev.move.ui.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import io.github.azakidev.move.ui.components.LineRow
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.StopItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LinesPage(
    model: MoveViewModel, sheetModel: SheetStopViewModel
) {
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
            ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {}
        },
    ) { padding ->
        if (model.lines.collectAsState().value.count() != 0) {
            LineList(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                lineItems = model.lines.collectAsState().value,
                stopItems = model.stops.collectAsState().value,
                sheetModel = sheetModel
            )
        } else {
            EmptyLines(
                modifier = Modifier.fillMaxSize()
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
    sheetModel: SheetStopViewModel
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        var count = 0
        lineItems.forEach { item ->
            val expanded = remember { mutableStateOf(false) }
            val shape = when (count) {
                0 -> {
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 4.dp,
                    )
                }

                lineItems.count() - 1 -> {
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                }

                else -> {
                    MaterialTheme.shapes.extraSmall
                }
            }
            LineRow(
                stops = stopItems,
                sheetModel = sheetModel,
                lineItem = item,
                shape = shape,
                expanded = expanded,
            )
            count++
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ViewModelConstructorInComposable")
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
    val sheetModel = viewModel<SheetStopViewModel>()
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
            sheetModel = sheetModel
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
@Composable @Preview
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