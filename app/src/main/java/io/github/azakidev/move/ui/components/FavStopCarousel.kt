package io.github.azakidev.move.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.StopItem
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.LineTime
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavStopCarousel(
    model: MoveViewModel,
    sheetModel: SheetStopViewModel
) {
    val favStops = MutableStateFlow<List<StopItem>>(listOf())

    model.favouriteStops.collectAsState().value.forEach {
        val stop = model.stops.value.find { stopItem -> it == stopItem.id } ?: StopItem()

        if (model.savedProviders.contains(stop.provider)) {
            favStops.value += stop

            slowTimer(model, stop).run()

            if (stop.lineTimes.value.isEmpty()) {
                fastTimer(model, stop).run()
            }
        }
    }

    val reloadTimer = Timer().schedule(delay = 500, period = 5000, action = {
        model.favouriteStops.value.forEach {
            val stop = model.stops.value.find { stopItem -> it == stopItem.id } ?: StopItem()

            if (model.savedProviders.contains(stop.provider)) {
                favStops.value += stop

                val timer = slowTimer(model, stop)
                timer.run()

                if (favStops.value.count() != 0) {
                    timer.cancel()
                }

                if (stop.lineTimes.value.isEmpty()) {
                    fastTimer(model, stop).run()
                }
            }
        }
        if (favStops.value.count() != 0) {
            this.cancel()
        }
    })

    if (favStops.collectAsState().value.count() != 0) {
        reloadTimer.cancel()
        HorizontalCenteredHeroCarousel(
            state = rememberCarouselState { favStops.value.count() },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 16.dp, bottom = 16.dp),
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) { i ->
            val item = favStops.collectAsState().value[i]
            Box(
                modifier = Modifier
                    .height(208.dp)
                    .maskClip(MaterialTheme.shapes.extraLarge)
                    .clickable(
                        enabled = true,
                        onClickLabel = null,
                        role = Role.Button,
                        onClick = {
                            sheetModel.sheetStop = item
                            sheetModel.showBottomSheet = true
                        }
                    ),
            ) {
                Image(
                    modifier = Modifier
                        .height(205.dp),
                    painter = painterResource(id = item.image),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            )
                        ),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 8.dp),
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    item.lineTimes.collectAsState().value.forEach {
                        val line = model.lines.value.find { lineItem -> lineItem.id == it.lineId }
                        val lineName = line?.name ?: "DefaultLine"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(start = 16.dp, bottom = 8.dp),
                                text = lineName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                modifier = Modifier
                                    .padding(end = 16.dp, bottom = 8.dp),
                                text = it.nextTime.toString() + " min.",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    } else {
        reloadTimer.run()
        Box(
            modifier = Modifier
                .height(208.dp)
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.noStops),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun slowTimer(
    model: MoveViewModel,
    stopItem: StopItem
): TimerTask {
    return Timer().schedule(
        delay = 1000,
        period = 30000,
        action = {
            model.fetchTimes(stopItem)
        }
    )
}

fun fastTimer(
    model: MoveViewModel,
    stop: StopItem
): TimerTask {
    return Timer().schedule(
        delay = 1000,
        period = 5000,
        action = {
            model.fetchTimes(stop)
            if (!stop.lineTimes.value.isEmpty()) {
                this.cancel()
            }
        }
    )
}

@Composable
@Preview
fun FavStopCarouselPreview() {
    val model = viewModel<MoveViewModel>()
    model.setStops(
        listOf(
            StopItem(id = 1, name = "Stop 1", provider = 1),
            StopItem(id = 2, name = "Stop 2", provider = 1),
        )
    )
    model.setFavStops(
        listOf(1, 2)
    )
    val sheetModel = viewModel<SheetStopViewModel>()
    FavStopCarousel(model, sheetModel)
}


@Composable
@Preview
fun FavStopCarouselEmptyPreview() {
    val model = viewModel<MoveViewModel>()
    model.setStops(
        listOf(
            StopItem(id = 1, name = "Stop 1", lines = listOf(1, 2)),
            StopItem(id = 2, name = "Stop 2", lines = listOf(2, 3)),
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
            LineTime(2, 4)
        )
    )
    model.stops.collectAsState().value.last().setTimeTable(
        listOf(
            LineTime(2, 2),
            LineTime(3, 4)
        )
    )
    model.setFavStops(listOf(1, 2))
    val sheetModel = viewModel<SheetStopViewModel>()
    FavStopCarousel(model, sheetModel)
}