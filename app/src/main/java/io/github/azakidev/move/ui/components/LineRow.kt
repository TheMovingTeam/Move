@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.azakidev.move.ui.components

import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
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
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.MoveModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.StopItem


@Composable
fun LineEntry(item: LineItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(48.dp)
                .clip(shape = MaterialShapes.Cookie9Sided.toShape())
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.lineEmblem,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = item.lineName,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
fun StopEntries(
    model: MoveModel,
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
                val stopItem = model.stops.find { stopItem -> stopItem.stopId == i } ?: StopItem()
                val shape = when (count) {
                    0 -> {
                        RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = 2.dp,
                            bottomEnd = 2.dp,
                        )
                    }

                    lineItem.stops.count() - 1 -> {
                        RoundedCornerShape(
                            topStart = 2.dp,
                            topEnd = 2.dp,
                            bottomStart = 8.dp,
                            bottomEnd = 8.dp
                        )
                    }

                    else -> {
                        MaterialTheme.shapes.extraSmall
                    }
                }
                Box(
                    modifier = Modifier
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
                        text = stopItem.stopName,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
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
    model: MoveModel,
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
            LineEntry(item = lineItem)
            StopEntries(
                model = model,
                sheetModel = sheetModel,
                lineItem = lineItem,
                isExpanded = expanded.value
            )
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun ExpandableContainerViewPreview() {
    val model = MoveModel()
    val sheetModel = SheetStopViewModel()
    val expanded = remember { mutableStateOf(true) }
    LineRow(
        model = model,
        sheetModel = sheetModel,
        lineItem = model.lines[0],
        expanded = expanded
    )
}
