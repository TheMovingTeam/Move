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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.StopItem
import io.github.azakidev.move.listShape
import kotlin.math.exp

@Composable
fun LineEntry(line: LineItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EmblemShape(
            modifier = Modifier
                .padding(8.dp)
                .size(48.dp),
            line = line
        )
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = line.name.replace("-", " - ").replace("  ", " "),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
fun StopEntries(
    stops: List<StopItem>,
    lines: List<LineItem>,
    lineItem: LineItem,
    isExpanded: Boolean,
    onClick: (StopItem) -> Unit
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

    val fetchedStops = stops.filter { lineItem.stops.contains(it.id) }

    val map = fetchedStops.associateBy { stopItem -> stopItem.id }
    val sortedStops = lineItem.stops.mapNotNull { id ->
        map[id]
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
            sortedStops.forEach { stopItem ->
                val shape = listShape(count, sortedStops.count())
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = shape)
                        .clickable(
                            enabled = isExpanded,
                            onClick = { onClick(stopItem) }
                        )
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        modifier = Modifier
                            .padding( horizontal = 12.dp, vertical = 6.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .fillMaxWidth(.6f),
                            text = stopItem.name.replace("-", " - ").replace("  ", " "),
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        StopEmblemRow(
                            stopItem = stopItem,
                            lines = lines.filterNot { it.emblem == lineItem.emblem }
                        )
                    }
                }
                count++
            }
        }
    }
}

@Composable
fun LineRow(
    modifier: Modifier = Modifier,
    stops: List<StopItem>,
    lines: List<LineItem>,
    lineItem: LineItem,
    shape: Shape = MaterialTheme.shapes.large,
    expanded: MutableState<Boolean>,
    expandable: Boolean = true,
    background: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    onClick: (StopItem) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = background)
            .clickable(
                enabled = expandable,
                onClick = {
                    expanded.value = !expanded.value
                }
            ),
    ) {
        Column {
            LineEntry(line = lineItem)
            StopEntries(
                stops = stops,
                lines = lines,
                lineItem = lineItem,
                isExpanded = expanded.value,
                onClick = onClick
            )
        }
    }
}

@Preview
@Composable
fun LineRowPreview() {
    val stops = listOf(
            StopItem(id = 1, name = "A stop with a really really long name", lines = listOf(1, 2, 3)),
            StopItem(id = 2, name = "Stop 2"),
            StopItem(id = 3, name = "Stop 3")
        )
    val lines = listOf(
        LineItem(id = 1, name = "Line 1", emblem = "L1"),
        LineItem(id = 2, name = "Line 2", emblem = "L2"),
        LineItem(id = 3, name = "Line 3", emblem = "L3")
    )
    val expanded = remember { mutableStateOf(false) }
    LineRow(
        stops = stops,
        lines = lines,
        lineItem = LineItem(stops = listOf(1, 2, 3)),
        expanded = expanded,
        onClick = {}
    )
}

@Preview
@Composable
fun LineRowLongPreview() {
    val stops = listOf(
        StopItem(id = 1, name = "A stop with a really really long name", lines = listOf(1, 2, 3)),
        StopItem(id = 2, name = "Stop 2"),
        StopItem(id = 3, name = "Stop 3")
    )
    val lines = listOf(
        LineItem(id = 1, name = "Line 1", emblem = "L1"),
        LineItem(id = 2, name = "Line 2", emblem = "L2"),
        LineItem(id = 3, name = "Line 3", emblem = "L3")
    )
    val expanded = remember { mutableStateOf(false) }
    LineRow(
        stops = stops,
        lines = lines,
        lineItem = LineItem(
            name = "A line with an obscenely long name to which I would rather not read but I might need regardless",
            stops = listOf(1, 2, 3)
        ),
        expanded = expanded,
        onClick = {}
    )
}

@Preview
@Composable
fun LineRowExpandedPreview() {
    val stops = listOf(
        StopItem(id = 1, name = "A stop with a really really long name", lines = listOf(1, 2, 3)),
        StopItem(id = 2, name = "Stop 2"),
        StopItem(id = 3, name = "Stop 3")
    )
    val lines = listOf(
        LineItem(id = 1, name = "Line 1", emblem = "L1"),
        LineItem(id = 2, name = "Line 2", emblem = "L2"),
        LineItem(id = 3, name = "Line 3", emblem = "L3")
    )
    val expanded = remember { mutableStateOf(true) }
    LineRow(
        stops = stops,
        lines = lines,
        lineItem = LineItem(stops = listOf(1, 3, 2)),
        expanded = expanded,
        onClick = {}
    )
}
