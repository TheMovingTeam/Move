package io.github.azakidev.move.ui.components.search

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.data.items.toKey
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.common.LineRow
import io.github.azakidev.move.ui.components.common.StopRow
import io.github.azakidev.move.ui.fmtSearch
import io.github.azakidev.move.ui.listShape
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

    val resultModifier = Modifier
        .padding(
            horizontal = PADDING.dp, vertical = PADDING.div(4).dp
        )

    LazyColumn(
        modifier = Modifier.padding(horizontal = PADDING.div(2).dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PADDING.div(2).dp)
            ) {
                val sortedTags = filterTags.sortedBy { filter -> !filter.state.value }
                items(sortedTags.count()) {
                    val tag = sortedTags[it]
                    FilterChip(
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = MotionScheme.expressive().fastEffectsSpec(),
                                placementSpec = MotionScheme.expressive().fastSpatialSpec(),
                                fadeOutSpec = MotionScheme.expressive().fastEffectsSpec()
                            ),
                        selected = tag.state.value,
                        leadingIcon = {
                            Icon(
                                imageVector = tag.icon, contentDescription = null
                            )
                        },
                        label = { Text(stringResource(tag.label)) },
                        onClick = {
                            tag.state.value = !tag.state.value
                        }
                    )
                }
            }
        }
        if (query.isNotEmpty()) {
            if (stopResults.count() != 0) {
                item {
                    Text(
                        modifier = resultModifier
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
                    StopRow(
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
                            model.saveLastStop(sheetModel.sheetStop.toKey())
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
            if (lineResults.count() != 0) {
                item {
                    Text(
                        modifier = resultModifier
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
                            model.saveLastStop(stopItem.toKey())
                            textFieldState.edit {
                                delete(
                                    0, textFieldState.text.length
                                )
                            }
                            scope.launch { searchBarState.animateToCollapsed() }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
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