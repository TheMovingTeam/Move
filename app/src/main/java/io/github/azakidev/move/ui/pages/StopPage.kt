package io.github.azakidev.move.ui.pages

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.azakidev.move.R
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.LineTime
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.ProviderItem
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.StopItem
import io.github.azakidev.move.getListShape
import io.github.azakidev.move.ui.components.EmblemShape
import java.util.Timer
import kotlin.concurrent.schedule

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StopPage(
    model: MoveViewModel,
    sheetModel: SheetStopViewModel,
) {
    var icon by remember { mutableStateOf(Icons.Default.FavoriteBorder) }
    var roundness: Int

    if (sheetModel.sheetStop.id in model.favouriteStops.collectAsState().value) {
        icon = Icons.Default.Favorite
        roundness = 25
    } else {
        icon = Icons.Default.FavoriteBorder
        roundness = 50
    }

    val cornerRadius = animateIntAsState(targetValue = roundness)

    Timer().schedule(delay = 1000, period = 1000, action = {
        if (!sheetModel.showBottomSheet) {
            this.cancel()
        }
        if (!sheetModel.sheetStop.lineTimes.value.isEmpty()) {
            this.cancel()
        }
        model.fetchTimes(sheetModel.sheetStop)
    }).run()
    Timer().schedule(delay = 1000, period = 15000, action = {
        if (!sheetModel.showBottomSheet) {
            this.cancel()
        }
        model.fetchTimes(sheetModel.sheetStop)
    }).run()

    val onClick = {
        if (sheetModel.sheetStop.id !in model.favouriteStops.value) {
            model.addFavStop(sheetModel.sheetStop.id)
        } else {
            model.removeFavStop(sheetModel.sheetStop.id)
        }
    }

    val provider =
        model.providers.collectAsState().value.find { it -> it.id == sheetModel.sheetStop.id }
            ?: ProviderItem()
    val url = "${model.providerRepo.value}/${provider.name}/res/stop/${sheetModel.sheetStop.id}.png"

    Column {
        StopBanner(
            imgUrl = url,
            icon = icon,
            onClick = onClick,
            sheetModel = sheetModel,
            shape = RoundedCornerShape(cornerRadius.value)
        )
        StopTimes(
            modifier = Modifier.padding(16.dp),
            lineItems = model.lines.collectAsState().value,
            sheetModel = sheetModel
        )
    }
}

@Composable
fun StopBanner(
    imgUrl: String,
    icon: ImageVector,
    shape: Shape,
    onClick: () -> Unit,
    sheetModel: SheetStopViewModel
) {
    Box(
        modifier = Modifier
            .height(308.dp)
            .fillMaxWidth()
    ) {
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
            IconButton(
                modifier = Modifier
                    .padding(end = 12.dp, bottom = 16.dp)
                    .size(55.dp)
                    .align(Alignment.BottomEnd),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = shape,
                onClick = onClick
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
@Preview
fun StopBannerPreview() {
    val sheetModel = viewModel<SheetStopViewModel>()
    StopBanner(
        imgUrl = "",
        icon = Icons.Default.Favorite,
        shape = RoundedCornerShape(25),
        onClick = {},
        sheetModel = sheetModel,
    )
}

@Composable
fun StopTimes(
    modifier: Modifier = Modifier,
    lineItems: List<LineItem>,
    sheetModel: SheetStopViewModel
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        var count = 0
        sheetModel.sheetStop.lineTimes.collectAsState().value.forEach {
            val line =
                lineItems.find { lineItem -> lineItem.id == it.lineId } ?: LineItem()

            val shape = getListShape(count, sheetModel.sheetStop.lineTimes.collectAsState().value.count())
            count++

            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun StopTimesPreview(
    modifier: Modifier = Modifier,
) {
    val sheetModel = viewModel<SheetStopViewModel>()
    val lineItems = listOf(
        LineItem(id = 1),
        LineItem(id = 2),
        LineItem(id = 3),
    )
    val stop = StopItem(id = 1, name = "Stop 1", lines = listOf(1))

    stop.setTimeTable(
        listOf(
            LineTime(1, 2),
            LineTime(2, 4),
            LineTime(3, 5),
        )
    )

    sheetModel.sheetStop = stop

    StopTimes(
        modifier = modifier,
        lineItems = lineItems,
        sheetModel = sheetModel
    )
}

@Composable
@Preview
fun StopPagePreview() {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp
            ))
            .background(MaterialTheme.colorScheme.background)
    ) {
        StopBannerPreview()
        StopTimesPreview(
            modifier = Modifier.padding(16.dp)
        )
    }
}