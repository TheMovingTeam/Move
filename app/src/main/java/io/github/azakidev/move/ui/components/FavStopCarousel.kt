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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.LineTime
import io.github.azakidev.move.data.ProviderItem
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

        if (model.savedProviders.value.contains(stop.provider)) {
            favStops.value += stop

            slowTimer(model, stop).run()

            if (stop.lineTimes.value.isEmpty()) {
                fastTimer(model, stop).run()
            }
        }
    }

    val reloadTimer = Timer().schedule(delay = 1000, period = 1000, action = {
        model.favouriteStops.value.forEach {
            val stop = model.stops.value.find { stopItem -> it == stopItem.id } ?: StopItem()
            if (model.savedProviders.value.contains(stop.provider)) {
                favStops.value += stop
                val timer = slowTimer(model, stop)
                timer.run()
                if (favStops.value.count() != 0) { timer.cancel() }
                if (stop.lineTimes.value.isEmpty()) { fastTimer(model, stop).run() }
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
            val stopItem = favStops.collectAsState().value[i]
            val provider =
                model.providers.collectAsState().value.find { it -> it.id == stopItem.id }
                    ?: ProviderItem()
            val url = "${model.providerRepo.value}/${provider.name}/res/stop/${stopItem.id}.png"
            HeroCarrouselItem(
                modifier = Modifier
                    .height(208.dp)
                    .maskClip(MaterialTheme.shapes.extraLarge)
                    .clickable(
                        enabled = true,
                        onClickLabel = null,
                        role = Role.Button,
                        onClick = {
                            sheetModel.sheetStop = stopItem
                            sheetModel.showBottomSheet = true
                        }
                    ),
                stopItem = stopItem,
                lineItems = model.lines.collectAsState().value,
                imgUrl = url,
                sheetModel = sheetModel
            )
        }
    } else {
        if (model.favouriteStops.collectAsState().value.count() != 0) {
            reloadTimer.run()
        }
        EmptyCarrousel()
    }
}

fun slowTimer(
    model: MoveViewModel,
    stopItem: StopItem,
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
    stopItem: StopItem,
): TimerTask {
    return Timer().schedule(
        delay = 1000,
        period = 5000,
        action = {
            model.fetchTimes(stopItem)
            if (!stopItem.lineTimes.value.isEmpty()) {
                this.cancel()
            }
        }
    )
}

@Composable
fun HeroCarrouselItem(
    modifier: Modifier,
    stopItem: StopItem,
    lineItems: List<LineItem>,
    imgUrl: String,
    sheetModel: SheetStopViewModel
) {
    Box(
        modifier = modifier
    ) {
        Image(
            modifier = Modifier
                .height(205.dp),
            painter = painterResource(R.drawable.placeholderstop),
            contentDescription = stopItem.name,
            contentScale = ContentScale.Crop,
        )
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imgUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.placeholderstop),
            error = painterResource(R.drawable.placeholderstop),
            contentScale = ContentScale.Crop,
            contentDescription = sheetModel.sheetStop.name,
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
                )
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(
                space = 4.dp,
                alignment = Alignment.Bottom
            ),
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 12.dp, bottom = 4.dp),
                text = stopItem.name
                    .replace("-", " - ")
                    .replace(".", ". ")
                    .replace("  ", " "),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            stopItem.lineTimes.collectAsState().value.forEach {
                val line = lineItems.find { lineItem -> lineItem.id == it.lineId } ?: LineItem()
                val lineName = line.name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(.85f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EmblemShape(
                            modifier = Modifier.size(26.dp),
                            line = line,
                            textStyle = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = lineName
                                .replace("-", " - ")
                                .replace(".", ". ")
                                .replace("  ", " "),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = it.nextTimeFirst.toString() + "m.",
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun EmptyCarrousel() {
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
            text = stringResource(R.string.noFavStops),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun FavStopCarouselPreview() {
    val stops = listOf(
        StopItem(id = 1, name = "Stop 1", provider = 1),
        StopItem(id = 2, name = "Stop 2", provider = 1),
        StopItem(id = 3, name = "Stop 3", provider = 1),
    )
    stops.first().setTimeTable(
        listOf(
            LineTime(1, 20, 18),
            LineTime(2, 4, 18),
            LineTime(3, 5, 18),
        )
    )
    val lines = listOf(
        LineItem(
            id = 1,
            name = "A line with a really long name I don't really like"
        )
    )
    val sheetModel = viewModel<SheetStopViewModel>()

    HorizontalCenteredHeroCarousel(
        state = rememberCarouselState { stops.count() },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 16.dp),
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) { i ->
        val stopItem = stops[i]
        val url = ""
        HeroCarrouselItem(
            modifier = Modifier
                .height(208.dp)
                .maskClip(MaterialTheme.shapes.extraLarge)
                .clickable(
                    enabled = true,
                    onClickLabel = null,
                    role = Role.Button,
                    onClick = {
                        sheetModel.sheetStop = stopItem
                        sheetModel.showBottomSheet = true
                    }
                ),
            stopItem = stopItem,
            lineItems = lines,
            imgUrl = url,
            sheetModel = sheetModel
        )
    }
}