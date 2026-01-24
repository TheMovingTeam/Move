package io.github.azakidev.move.ui.pages.sheets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.items.Capabilities
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.LineTime
import io.github.azakidev.move.data.items.MapStyle
import io.github.azakidev.move.data.items.ProviderItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.data.items.StopKey
import io.github.azakidev.move.data.items.toKey
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.common.EmblemShape
import io.github.azakidev.move.ui.components.common.EmptyCard
import io.github.azakidev.move.ui.components.map.StopMap
import io.github.azakidev.move.ui.fmt
import io.github.azakidev.move.ui.listShape
import org.maplibre.compose.location.AndroidLocationProvider

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun StopPage(
    model: MoveViewModel,
    sheetModel: SheetStopViewModel,
    currentLocation: AndroidLocationProvider?
) {
    val stopKey = sheetModel.sheetStop.toKey()

    val roundness: Int =
        if (stopKey in model.favouriteStops.collectAsState().value) {
            25
        } else {
            50
        }

    val cornerRadius = animateIntAsState(
        targetValue = roundness,
        animationSpec = MotionScheme.expressive().fastSpatialSpec()
    )

    model.addToFetchLoop(stopKey)

    val onClick = {
        if (stopKey !in model.favouriteStops.value) {
            model.addFavStop(stopKey)
        } else {
            model.removeFavStop(stopKey)
        }
    }

    val provider =
        model.providers
            .collectAsState().value
            .find { it.id == sheetModel.sheetStop.provider }
            ?: ProviderItem()

    val url = "${model.providerRepo.collectAsState().value}/${provider.name}/res/stop/${sheetModel.sheetStop.id}.png"

    val lines = model.lines
        .collectAsState().value
        .filter { it.provider == provider.id }

    Surface {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-12).dp)
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ) {
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
                sheetModel = sheetModel,
                favStops = model.favouriteStops.collectAsState().value,
                shape = RoundedCornerShape(cornerRadius.value),
                onClick = onClick
            )
            Column(
                modifier = Modifier.padding(PADDING.dp),
            ) {
                if (provider.capabilities.contains(Capabilities.Time) || provider.capabilities.contains(
                        Capabilities.DoubleTime
                    )
                ) {
                    StopTimes(
                        lineItems = lines,
                        sheetModel = sheetModel
                    )
                }
                if (provider.capabilities.contains(Capabilities.Notifications)) {
                    StopNotifications(
                        notifications = sheetModel.sheetStop.notifications
                    )
                }
                if (provider.capabilities.contains(Capabilities.Geo)) {
                    val style = MapStyle.entries.find { model.mapStyle.collectAsState().value == it.name } ?: MapStyle.Liberty
                    StopMap(
                        sheetModel = sheetModel,
                        lines = lines.fastFilter { it.stops.contains(sheetModel.sheetStop.id) },
                        stops = model.stops.collectAsState().value,
                        providers = model.providers.collectAsState().value,
                        style = style,
                        currentLocation = currentLocation
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
        ) {
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
                modifier = Modifier.padding(PADDING.dp),
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StopBanner(
    imgUrl: String,
    favStops: List<StopKey>,
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
                            MaterialTheme.colorScheme.background
                        )
                    )
                ),
        ) {
            val width =
                if (sheetModel.sheetStop.name.length > 10) .8f
                else 1f
            Text(
                modifier = Modifier
                    .padding(PADDING.dp)
                    .fillMaxWidth(width)
                    .align(Alignment.BottomStart),
                text = sheetModel.sheetStop.name.fmt(),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = MaterialTheme.typography.titleLarge.fontSize,
                    maxFontSize = MaterialTheme.typography.displaySmall.fontSize
                ),
                lineHeight = 28.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                modifier = Modifier
                    .padding(horizontal = PADDING.times(0.75).dp, vertical = PADDING.dp)
                    .size(55.dp)
                    .align(Alignment.BottomEnd),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = shape,
                onClick = onClick
            ) {
                AnimatedContent(
                    targetState = sheetModel.sheetStop.toKey() in favStops,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = MotionScheme.expressive().fastEffectsSpec()
                        ) + scaleIn(
                            animationSpec = MotionScheme.expressive().fastSpatialSpec()
                        ) togetherWith fadeOut(
                            animationSpec = MotionScheme.expressive().fastEffectsSpec()
                        ) + scaleOut(
                            animationSpec = MotionScheme.expressive().fastSpatialSpec()
                        )
                    }
                ) { state ->
                    when (state) {
                        true -> {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = "Remove favourite stop",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        false -> {
                            Icon(
                                imageVector = Icons.Rounded.FavoriteBorder,
                                contentDescription = "Save favourite stop",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun StopBannerPreview() {
    val sheetModel = viewModel<SheetStopViewModel>()
    sheetModel.sheetStop = StopItem(
        name = "A stop with a really long name for testing purposes in case that a line decides to be obnoxious"
    )
    StopBanner(
        imgUrl = "",
        favStops = listOf(),
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
    sheetModel: SheetStopViewModel
) {
    Text(
        modifier = Modifier.padding(bottom = PADDING.div(2).dp),
        text = stringResource(id = R.string.times),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary,
    )
    val lineTimes = sheetModel.sheetStop.lineTimes.collectAsState().value
    AnimatedContent(
        targetState = lineTimes,
    ) { times ->
        when (times) {
            null -> {
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

            emptyList<LineTime>() -> {
                EmptyCard(stringResource(R.string.noTimes))
            }

            else -> {
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
                ) {
                    var count = 0
                    times
                        .sortedBy { it.nextTimeFirst }
                        .forEach {

                            val line = lineItems
                                .find { lineItem -> lineItem.id == it.lineId } ?: LineItem()

                            val shape = listShape(count, times.count())
                            count++

                            Box(
                                modifier = Modifier
                                    .clip(shape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .clip(shape),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(2.5f),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            PADDING.div(2).dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        EmblemShape(
                                            modifier = Modifier
                                                .padding(PADDING.div(2).dp)
                                                .size(48.dp),
                                            line = line,
                                            emblemOverride = it.emblemOverride
                                        )
                                        Text(
                                            text = (it.destination ?: line.name)
                                                .fmt()
                                                .replace(" - ", " > "),
                                            overflow = TextOverflow.Ellipsis,
                                            maxLines = 2
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = PADDING.dp),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            alignment = Alignment.End,
                                            space = PADDING.div(2).dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val text =
                                            if (it.nextTimeFirst == 0) stringResource(R.string.soon)
                                            else it.nextTimeFirst.toString() + stringResource(R.string.minuteMark)
                                        Text(
                                            text = text,
                                            maxLines = 1
                                        )
                                        if (it.nextTimeSecond != null) {
                                            Text(
                                                text = "/",
                                                maxLines = 1
                                            )
                                            Text(
                                                text = it.nextTimeSecond.toString() + stringResource(R.string.minuteMark),
                                                maxLines = 1
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
        sheetModel = sheetModel
    )
}

@Composable
fun StopNotifications(
    notifications: List<String>
) {
    Text(
        modifier = Modifier.padding(top = PADDING.dp, bottom = PADDING.div(2).dp),
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
                EmptyCard(stringResource(R.string.noAlerts))
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
                                modifier = Modifier.padding(PADDING.dp),
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