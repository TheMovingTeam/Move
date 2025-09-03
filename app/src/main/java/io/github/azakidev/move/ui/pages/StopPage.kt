package io.github.azakidev.move.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.LineTime
import io.github.azakidev.move.data.StopItem
import io.github.azakidev.move.ui.components.EmblemShape
import java.util.Timer
import kotlin.concurrent.schedule

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StopPage(
    model: MoveViewModel,
    sheetModel: SheetStopViewModel,
    shouldFetch: Boolean = true
) {
    var icon by remember { mutableStateOf(Icons.Default.FavoriteBorder) }
    if (sheetModel.sheetStop.id in model.favouriteStops.collectAsState().value) {
        icon = Icons.Default.Favorite
    }

    var shouldLoad by remember { mutableStateOf(shouldFetch) }

    val fastTimer = Timer().schedule(delay = 1000, period = 1000, action = {
        model.fetchTimes(sheetModel.sheetStop)
    })
    val slowTimer = Timer().schedule(delay = 1000, period = 15000, action = {
        model.fetchTimes(sheetModel.sheetStop)
    })

    if (shouldLoad) {
        if (sheetModel.sheetStop.lineTimes.collectAsState().value.isEmpty()) {
            fastTimer.run()
        } else {
            fastTimer.cancel()
            slowTimer.run()
        }
    } else {
        fastTimer.cancel()
        slowTimer.cancel()
    }

    Column {
        Box(
            modifier = Modifier
                .height(308.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(sheetModel.sheetStop.image),
                modifier = Modifier
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Crop,
                contentDescription = sheetModel.sheetStop.name,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surfaceContainer
                            )
                        )
                    ),
            ) {
                Text(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomStart),
                    text = sheetModel.sheetStop.name,
                    style = MaterialTheme.typography.displayMedium
                )
                Button(
                    modifier = Modifier
                        .padding(end = 12.dp, bottom = 16.dp)
                        .size(50.dp)
                        .align(Alignment.BottomEnd),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        if (sheetModel.sheetStop.id !in model.favouriteStops.value) {
                            model.addFavStop(sheetModel.sheetStop.id)
                            icon = Icons.Default.Favorite
                        } else {
                            model.removeFavStop(sheetModel.sheetStop.id)
                            icon = Icons.Default.FavoriteBorder
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            var count = 0
            sheetModel.sheetStop.lineTimes.collectAsState().value.forEach {
                val line =
                    model.lines.value.find { lineItem -> lineItem.id == it.lineId } ?: LineItem()

                var shape = when (count) {
                    0 -> {
                        RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = 2.dp,
                            bottomEnd = 2.dp,
                        )
                    }

                    sheetModel.sheetStop.lineTimes.collectAsState().value.count() - 1 -> {
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
                count++

                if (sheetModel.sheetStop.lineTimes.collectAsState().value.count() == 1) {
                    shape = RoundedCornerShape(8.dp)
                }

                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end =16.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clip(shape),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EmblemShape(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(48.dp),
                                line = line
                            )
                            Text(
                                text = line.name
                            )
                        }
                        Text(
                            text = it.nextTime.toString() + " " + "min."
                        )
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun StopPagePreview() {
    val model = viewModel<MoveViewModel>()
    val sheetModel = viewModel<SheetStopViewModel>()
    model.setStops(
        listOf(
            StopItem(id = 1, name = "Stop 1", lines = listOf(1, 2, 3)),
        )
    )
    model.setLines(
        listOf(
            LineItem(id = 1),
        )
    )
    model.stops.collectAsState().value.first().setTimeTable(
        listOf(
            LineTime(1, 2),
        )
    )
    sheetModel.sheetStop = model.stops.collectAsState().value.first()
    StopPage(model, sheetModel, false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun StopPageMultiplePreview() {
    val model = viewModel<MoveViewModel>()
    val sheetModel = viewModel<SheetStopViewModel>()
    model.setStops(
        listOf(
            StopItem(id = 1, name = "Stop 1", lines = listOf(1, 2, 3)),
        )
    )
    model.setLines(
        listOf(
            LineItem(id = 1),
            LineItem(id = 2),
            LineItem(id = 3),
        )
    )
    model.stops.collectAsState().value.first().setTimeTable(
        listOf(
            LineTime(1, 2),
            LineTime(2, 4),
            LineTime(3, 5),
        )
    )
    sheetModel.sheetStop = model.stops.collectAsState().value.first()
    StopPage(model, sheetModel, false)
}