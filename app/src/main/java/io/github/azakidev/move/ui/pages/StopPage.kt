package io.github.azakidev.move.ui.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.azakidev.move.R
import io.github.azakidev.move.data.Capabilities
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.LineTime
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.ProviderItem
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.StopItem
import io.github.azakidev.move.listShape
import io.github.azakidev.move.ui.components.EmblemShape
import java.util.Timer
import kotlin.concurrent.schedule

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StopPage(
    model: MoveViewModel,
    sheetModel: SheetStopViewModel,
//    nestedScroll: NestedScrollConnection
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
        model.providers.collectAsState().value.find { it.id == sheetModel.sheetStop.provider }
            ?: ProviderItem()
    val url = "${model.providerRepo.value}/${provider.name}/res/stop/${sheetModel.sheetStop.id}.png"

    Surface {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-12).dp)
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ){
            BottomSheetDefaults.DragHandle(
                color = Color.White
            )
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            StopBanner(
                imgUrl = url,
                icon = icon,
                onClick = onClick,
                sheetModel = sheetModel,
                shape = RoundedCornerShape(cornerRadius.value)
            )
            Column(
                modifier = Modifier
                    .padding(16.dp),
            ) {
                StopTimes(
                    lineItems = model.lines.collectAsState().value,
                    providers = model.providers.collectAsState().value,
                    sheetModel = sheetModel
                )
                if (provider.capabilities.contains(Capabilities.Notifications)) {
                    StopNotifications(
                        notifications = sheetModel.sheetStop.notifications
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun StopPagePreview() {
    Surface {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-12).dp)
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ){
            BottomSheetDefaults.DragHandle(
                color = Color.White
            )
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .clip(
                    RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp
                    )
                )
                .background(MaterialTheme.colorScheme.background)
        ) {
            StopBannerPreview()
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                StopTimesPreview()
                StopNotifications(
                    notifications = listOf(
                        "All your bases are mine"
                    )
                )
            }
        }
    }
}

    @OptIn(ExperimentalMaterial3Api::class)
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
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        ) {
            AsyncImage(
                modifier = Modifier.matchParentSize(),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imgUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.placeholder_banner),
                error = painterResource(R.drawable.banner),
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
                val style = when {
                    sheetModel.sheetStop.name.length > 20 -> MaterialTheme.typography.headlineMedium
                    sheetModel.sheetStop.name.length > 15 -> MaterialTheme.typography.headlineLarge
                    sheetModel.sheetStop.name.length > 10 -> MaterialTheme.typography.displaySmall
                    else -> MaterialTheme.typography.displayMedium
                }
                val width =
                    if (sheetModel.sheetStop.name.length > 10) .8f
                    else 1f
                Text(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(width)
                        .align(Alignment.BottomStart),
                    text = sheetModel.sheetStop.name
                        .replace("-", " - ")
                        .replace(".", ". ")
                        .replace("  ", " "),
                    style = style,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 16.dp)
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

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun StopTimes(
        modifier: Modifier = Modifier,
        lineItems: List<LineItem>,
        providers: List<ProviderItem>,
        sheetModel: SheetStopViewModel
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = stringResource(id = R.string.times),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
        )
        val lineTimes = sheetModel.sheetStop.lineTimes.collectAsState().value
        AnimatedContent(
            targetState = lineTimes.count(),
        ) { count ->
            when (count) {
                0 -> {
                    Box(
                        modifier = Modifier
                            .height(150.dp)
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                }
                else -> {
                    Column(
                        modifier = modifier,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        var count = 0
                        lineTimes
                            .sortedBy { it.nextTimeFirst }
                            .forEach {
                                val line =
                                    lineItems.find { lineItem -> lineItem.id == it.lineId } ?: LineItem()
                                val provider =
                                    providers.find { providerItem -> providerItem.id == line.provider }
                                        ?: ProviderItem()

                                val shape = listShape(count, lineTimes.count())
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
                                                modifier = Modifier.fillMaxWidth(.60f),
                                                text = line.name
                                                    .replace("-", " - ")
                                                    .replace(".", ". ")
                                                    .replace("  ", " "),
                                                overflow = TextOverflow.Ellipsis,
                                                maxLines = 2
                                            )
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val text =
                                                if (it.nextTimeFirst == 0) stringResource(R.string.soon) else it.nextTimeFirst.toString() + "m."
                                            Text(
                                                text = text
                                            )
                                            if (provider.capabilities.contains(Capabilities.DoubleTime) and (it.nextTimeSecond != null)) {
                                                Text(
                                                    text = "/"
                                                )
                                                Text(
                                                    text = it.nextTimeSecond.toString() + "m."
                                                )
                                            }
                                        }
                                    }
                                }

                            }
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
            LineItem(
                id = 1,
                emblem = "L1",
            ),
            LineItem(
                id = 2,
                emblem = "L2",
                name = "A really really really really long name for a line like it's actually massive given it goes nowhere"
            ),
            LineItem(
                id = 3,
                emblem = "L3",
                name = "Example line with a long-ish name"
            ),
        )
        val stop = StopItem(id = 1, name = "Example stop", lines = listOf(1))

        stop.setTimeTable(
            listOf(
                LineTime(1, 2, 18),
                LineTime(3, 4, 18),
                LineTime(2, 5, 18),
            )
        )

        sheetModel.sheetStop = stop

        StopTimes(
            modifier = modifier,
            lineItems = lineItems,
            providers = emptyList(),
            sheetModel = sheetModel
        )
    }

    @Composable
    fun StopNotifications(
        notifications: List<String>
    ) {
        Text(
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            text = stringResource(id = R.string.alerts),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
        )
        AnimatedContent(
            targetState = notifications.count(),
        ) { count ->
            when (count) {
                0 -> {
                    Box(
                        modifier = Modifier
                            .height(208.dp)
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.noAlerts),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                else -> {
                    var count = 0
                    Column {
                        notifications.forEach {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(listShape(count, notifications.count()))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                Text(
                                    modifier = Modifier.padding(16.dp),
                                    text = it
                                )
                            }
                            count++
                        }
                    }
                }
            }
        }
    }

    @Composable
    @Preview
    fun StopNotificationsPreview() {
        val notifications = listOf(
            "All your bases are mine"
        )
        StopNotifications(
            notifications = notifications
        )
    }

    @Composable
    @Preview
    fun StopNotificationsEmptyPreview() {
        val notifications = emptyList<String>()
        StopNotifications(
            notifications = notifications
        )
    }