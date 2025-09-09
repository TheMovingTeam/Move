@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.azakidev.move.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.StopItem
import io.github.azakidev.move.getListShape


@Composable
fun LineEntry(line: LineItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EmblemShape(
            modifier = Modifier
                .padding(8.dp)
                .size(48.dp),
            line = line
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = line.name,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
fun StopEntries(
    stops: List<StopItem>,
    sheetModel: SheetStopViewModel,
    lineItem: LineItem,
    isExpanded: Boolean
) {
    // Opening Animation
    val expandTransition = remember {
        expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = tween(300)
        ) + fadeIn(
            animationSpec = tween(300)
        )
    }

    // Closing Animation
    val collapseTransition = remember {
        shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(300)
        ) + fadeOut(
            animationSpec = tween(300)
        )
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = expandTransition,
        exit = collapseTransition
    ) {
        Column(
            Modifier.padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            var count = 0
            lineItem.stops.forEach { i ->
                val stopItem = stops.find { stopItem -> stopItem.id == i } ?: StopItem()
                val shape = getListShape(count, lineItem.stops.count())
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = shape)
                        .clickable(
                            enabled = isExpanded,
                            onClick = {
                                sheetModel.sheetStop = stopItem
                                sheetModel.showBottomSheet = true
                            }
                        )
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Text(
                        text = stopItem.name,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(12.dp)
                    )
                }
                count++
            }
        }
    }
}

@Composable
fun LineRow(
    stops: List<StopItem>,
    sheetModel: SheetStopViewModel,
    lineItem: LineItem,
    shape: Shape = MaterialTheme.shapes.large,
    expanded: MutableState<Boolean>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape = shape)
            .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(
                onClick = {
                    expanded.value = !expanded.value
                }
            ),
    ) {
        Column {
            LineEntry(line = lineItem)
            StopEntries(
                stops = stops,
                sheetModel = sheetModel,
                lineItem = lineItem,
                isExpanded = expanded.value
            )
        }
    }
}

@Preview
@Composable
fun LineRowPreview() {
    val stops = listOf(
            StopItem(id = 1, name = "Stop 1"),
            StopItem(id = 2, name = "Stop 2"),
            StopItem(id = 3, name = "Stop 3")
        )
    val sheetModel = viewModel<SheetStopViewModel>()
    val expanded = remember { mutableStateOf(false) }
    LineRow(
        stops = stops,
        sheetModel = sheetModel,
        lineItem = LineItem(stops = listOf(1, 2, 3)),
        expanded = expanded
    )
}

@Preview
@Composable
fun LineRowExpandedPreview() {
    val stops = listOf(
        StopItem(id = 1, name = "Stop 1"),
        StopItem(id = 2, name = "Stop 2"),
        StopItem(id = 3, name = "Stop 3")
    )
    val sheetModel = viewModel<SheetStopViewModel>()
    val expanded = remember { mutableStateOf(true) }
    LineRow(
        stops = stops,
        sheetModel = sheetModel,
        lineItem = LineItem(stops = listOf(1, 2, 3)),
        expanded = expanded
    )
}
