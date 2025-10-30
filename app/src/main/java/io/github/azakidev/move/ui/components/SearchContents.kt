package io.github.azakidev.move.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.StopItem
import io.github.azakidev.move.fmt
import io.github.azakidev.move.fmtSearch
import io.github.azakidev.move.listShape
import kotlinx.coroutines.launch

data class FilterTag(
    val state: MutableState<Boolean>,
    val icon: ImageVector,
    @param:StringRes val label: Int,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchContents(
    model: MoveViewModel,
    sheetModel: SheetStopViewModel,
    textFieldState: TextFieldState,
    searchBarState: SearchBarState,
    query: String
) {
    val scope = rememberCoroutineScope()

    val stopResults = mutableListOf<StopItem>()
    val lineResults = mutableListOf<LineItem>()

    // Filter states
    val stopNameResultsShouldAppear = remember { mutableStateOf(false) }
    val stopComIdResultsShouldAppear = remember { mutableStateOf(false) }
    val lineNameResultsShouldAppear = remember { mutableStateOf(false) }
    val lineEmblemResultsShouldAppear = remember { mutableStateOf(false) }

    // Results for each filter
    val stopNameResults = model.stops.collectAsState().value.filter {
        it.name.fmtSearch().contains(query)
    }

    val stopComIdResults = model.stops.collectAsState().value.filter {
        it.comId != null && it.comId.toString().contains(query)
    }

    val lineNameResults = model.lines.collectAsState().value.filter {
        it.name.fmtSearch().contains(query)
    }

    val lineEmblemResults = model.lines.collectAsState().value.filter {
        it.emblem.lowercase().contains(query)
    }

    // Filter conditions
    if (stopNameResultsShouldAppear.value) {
        stopResults += stopNameResults
    }

    if (stopComIdResultsShouldAppear.value) {
        stopResults += stopComIdResults
    }

    if (lineNameResultsShouldAppear.value) {
        lineResults += lineNameResults
    }

    if (lineEmblemResultsShouldAppear.value) {
        lineResults += lineEmblemResults
    }

    // Show all if no filters are selected
    if (!(stopNameResultsShouldAppear.value || stopComIdResultsShouldAppear.value || lineNameResultsShouldAppear.value || lineEmblemResultsShouldAppear.value)) {
        stopResults += stopNameResults + stopComIdResults
        lineResults += lineNameResults + lineEmblemResults
    }

    // Filter chips to show
    val filterTags = listOf(
        FilterTag(
            stopNameResultsShouldAppear, Icons.Rounded.LocationOn, R.string.stopNameFilter
        ),
        FilterTag(
            stopComIdResultsShouldAppear, Icons.Rounded.Tag, R.string.stopComIdFilter
        ),
        FilterTag(
            lineNameResultsShouldAppear, Icons.Rounded.DirectionsBus, R.string.lineNameFilter
        ),
        FilterTag(
            lineEmblemResultsShouldAppear, Icons.Rounded.LocalOffer, R.string.lineEmblemFilter
        ),
    )

    LazyColumn(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterTags.forEach {
                    FilterChip(selected = it.state.value, leadingIcon = {
                        Icon(
                            imageVector = it.icon, contentDescription = null
                        )
                    }, label = { Text(stringResource(it.label)) }, onClick = {
                        it.state.value = !it.state.value
                    })
                }
            }
        }
        if (query.isNotEmpty()) {
            if (stopResults.count() != 0) {
                item {
                    Text(
                        modifier = Modifier
                            .padding(
                                horizontal = 16.dp, vertical = 4.dp
                            )
                            .animateItem(
                                fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                                placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                                fadeOutSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
                            ),
                        text = stringResource(id = R.string.stops),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                items(stopResults.count()) {
                    val shape = listShape(it, stopResults.count())
                    val result = stopResults[it]
                    SearchResultStop(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                                placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                                fadeOutSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
                            ),
                        stopItem = result,
                        lines = model.lines.collectAsState().value,
                        shape = shape,
                        onClick = {
                            sheetModel.sheetStop = result
                            sheetModel.showBottomSheet = true
                            model.saveLastStop(sheetModel.sheetStop.id)
                            textFieldState.edit {
                                delete(
                                    0, textFieldState.text.length
                                )
                            }
                            scope.launch { searchBarState.animateToCollapsed() }
                        })
                }
            }
            if (lineResults.count() != 0) {
                item {
                    Text(
                        modifier = Modifier
                            .padding(
                                horizontal = 16.dp, vertical = 4.dp
                            )
                            .animateItem(
                                fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                                placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                                fadeOutSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
                            ),
                        text = stringResource(id = R.string.lines),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                items(lineResults.count()) {
                    val shape = listShape(it, lineResults.count())
                    val expanded = remember { mutableStateOf(true) }
                    val result = lineResults[it]
                    LineRow(
                        modifier = Modifier.animateItem(
                            fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                            placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                            fadeOutSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
                        ),
                        lineItem = result,
                        lines = model.lines.collectAsState().value,
                        stops = model.stops.collectAsState().value,
                        shape = shape,
                        expandable = false,
                        expanded = expanded,
                        background = MaterialTheme.colorScheme.background,
                        onClick = { stopItem ->
                            sheetModel.sheetStop = stopItem
                            sheetModel.showBottomSheet = true
                            model.saveLastStop(sheetModel.sheetStop.id)
                            textFieldState.edit {
                                delete(
                                    0, textFieldState.text.length
                                )
                            }
                            scope.launch { searchBarState.animateToCollapsed() }
                        }
                    )
                }
            }

            if (stopResults.count() + lineResults.count() == 0) {
                item {
                    SearchNoResults(
                        modifier = Modifier.animateItem(
                            fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                            placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                            fadeOutSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
                        )
                    )
                }
            }
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
                modifier = Modifier.fillMaxWidth(0.65f),
                text = stopItem.name.fmt(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            StopEmblemRow(
                stopItem = stopItem, lines = lines
            )
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun SearchNoResults(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp, alignment = Alignment.CenterVertically
        )
    ) {
        Box(
            modifier = Modifier
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer
                ), contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier
                    .padding(24.dp)
                    .size(108.dp),
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(R.string.search),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Text(
            text = stringResource(R.string.noResults)
        )
    }
}

@Composable
@Preview
fun SearchResultPreview() {
    SearchResultStop(
        stopItem = StopItem(
            lines = listOf(1)
        ), lines = listOf(
            LineItem(id = 1)
        ), shape = MaterialTheme.shapes.medium, onClick = {})
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchInputField(
    searchBarState: SearchBarState, textFieldState: TextFieldState
) {
    val scope = rememberCoroutineScope()

    SearchBarDefaults.InputField(
        searchBarState = searchBarState,
        textFieldState = textFieldState,
        onSearch = {},
        placeholder = {
            Text(stringResource(R.string.searchPlaceholder))
        },
        leadingIcon = {
            val isCollapsed =
                searchBarState.currentValue == SearchBarValue.Collapsed && !searchBarState.isAnimating
            AnimatedContent(
                targetState = isCollapsed
            ) { isCollapsed ->
                when (isCollapsed) {
                    true -> {
                        Icon(
                            imageVector = Icons.Default.Search, contentDescription = null
                        )
                    }

                    false -> {
                        IconButton(
                            onClick = {
                                scope.launch { searchBarState.animateToCollapsed() }
                            }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            }
        },
        trailingIcon = {
            val enterTransition = remember {
                slideInHorizontally(
                    initialOffsetX = { it / 2 },
                    animationSpec = MotionScheme.expressive().defaultSpatialSpec()
                ) + fadeIn(
                    animationSpec = MotionScheme.expressive().defaultEffectsSpec()
                ) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = MotionScheme.expressive().defaultSpatialSpec()
                )
            }
            val exitTransition = remember {
                slideOutHorizontally(
                    targetOffsetX = { it / 2 },
                    animationSpec = MotionScheme.expressive().defaultSpatialSpec()
                ) + fadeOut(
                    animationSpec = MotionScheme.expressive().defaultEffectsSpec()
                ) + scaleOut(
                    targetScale = 0.8f,
                    animationSpec = MotionScheme.expressive().defaultSpatialSpec()
                )
            }
            AnimatedVisibility(
                visible = (searchBarState.currentValue != SearchBarValue.Collapsed && !searchBarState.isAnimating),
                enter = enterTransition,
                exit = exitTransition
            ) {
                IconButton(
                    onClick = {
                        textFieldState.edit { delete(0, textFieldState.text.length) }
                    }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Backspace,
                        contentDescription = "Back"
                    )
                }
            }
        })
}