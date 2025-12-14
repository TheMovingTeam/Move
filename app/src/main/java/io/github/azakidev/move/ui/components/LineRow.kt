@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.azakidev.move.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
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
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.fmt
import io.github.azakidev.move.ui.listShape
import kotlin.math.round

@Composable
fun LineEntry(line: LineItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EmblemShape(
            modifier = Modifier
                .padding(PADDING.div(2).dp)
                .size(48.dp),
            line = line
        )
        Text(
            modifier = Modifier.padding(horizontal = PADDING.div(2).dp),
            text = line.name
                .fmt()
                .replace(" - ", " > "),
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
            animationSpec = MotionScheme.expressive().defaultSpatialSpec()
        ) + fadeIn(
            animationSpec = MotionScheme.expressive().defaultEffectsSpec()
        )
    }

    // Closing Animation
    val collapseTransition = remember {
        shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = MotionScheme.expressive().defaultSpatialSpec()
        ) + fadeOut(
            animationSpec = MotionScheme.expressive().defaultEffectsSpec()
        )
    }

    // Sort stop as they appear in the line
    val map = stops.associateBy { stopItem -> stopItem.id }
    val sortedStops = lineItem.stops.mapNotNull { id ->
        map[id]
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = expandTransition,
        exit = collapseTransition
    ) {
        Column(
            Modifier
                .padding(horizontal = PADDING.times(0.75).dp)
                .padding(
                    top = PADDING.times(0.75).div(2).dp,
                    bottom = PADDING.times(0.75).dp
                ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            var count = 0
            sortedStops.forEach { stopItem ->
                val shape = listShape(
                    count,
                    sortedStops.count(),
                    roundingLarge = (24 - PADDING.times(0.75)).dp
                )
                StopRow(
                    modifier = Modifier.animateEnterExit(
                        enter = expandTransition,
                        exit = collapseTransition,
                    ),
                    shape = shape,
                    background = MaterialTheme.colorScheme.surfaceContainerLow,
                    stopItem = stopItem,
                    lines = lines.filter { (it.emblem != lineItem.emblem) && (it.provider == lineItem.provider) },
                    onClick = { onClick(stopItem) },
                    clickable = isExpanded
                )
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
                stops = stops.filter { lineItem.stops.contains(it.id) && it.provider == lineItem.provider },
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
        LineItem(id = 1, name = "Line 1", emblem = "L1", stops = (1..3).toList()),
        LineItem(
            id = 2,
            name = "A line with an obscenely long name to which I would rather not read but I might need regardless",
            emblem = "ELNL",
            stops = listOf(1, 2, 3)
        )
    )
    val notExpanded = remember { mutableStateOf(false) }
    val expanded = remember { mutableStateOf(true) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LineRow(
            stops = stops,
            lines = lines,
            lineItem = lines[0],
            expanded = notExpanded,
            onClick = {}
        )

        LineRow(
            stops = stops,
            lines = lines,
            lineItem = lines[1],
            expanded = notExpanded,
            onClick = {}
        )

        LineRow(
            stops = stops,
            lines = lines,
            lineItem = lines[0],
            expanded = expanded,
            onClick = {}
        )
    }

}
