package io.github.azakidev.move.ui.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import io.github.azakidev.move.MainView
import io.github.azakidev.move.QrScanner
import io.github.azakidev.move.R
import io.github.azakidev.move.Settings
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.StopItem
import io.github.azakidev.move.listShape
import io.github.azakidev.move.ui.components.FavStopCarousel
import io.github.azakidev.move.ui.components.FavStopCarouselPreview
import io.github.azakidev.move.ui.components.StopEmblemRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomePage(
    model: MoveViewModel,
    sheetModel: SheetStopViewModel,
    backStack: NavBackStack<NavKey>
) {
    val unorderedLastStops = model.stops.collectAsState().value
        .filter { model.lastStops.collectAsState().value.contains(it.id) }
    val stopMap = unorderedLastStops.associateBy { it.id }
    val lastStops = model.lastStops.collectAsState().value
        .mapNotNull { id ->
            stopMap[id]
        }
        .reversed()
    HomePageView(
        backStack = backStack,
        favStopCarrousel = { FavStopCarousel(model, sheetModel) },
        lastStops = lastStops,
        lines = model.lines.collectAsState().value,
        onRecentOpen = { stopItem ->
            sheetModel.sheetStop = stopItem
            sheetModel.showBottomSheet = true
            model.saveLastStop(sheetModel.sheetStop.id)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomePageView(
    backStack: NavBackStack<NavKey>,
    lastStops: List<StopItem>,
    lines: List<LineItem>,
    onRecentOpen: (StopItem) -> Unit,
    favStopCarrousel: @Composable() () -> Unit
) {
    val greetings = stringArrayResource(R.array.greetings)
    greetings.shuffle()
    val greeting = rememberSaveable { greetings.first() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = greeting,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.displaySmallEmphasized,
                        fontWeight = FontWeight.Black
                    )
                },
                expandedHeight = TopAppBarDefaults.MediumAppBarCollapsedHeight,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                scrollBehavior = null,
                actions = {
                    IconButton(
                        onClick = {
                            backStack.add(Settings)
                        }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                })
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.size(64.dp),
                onClick = {
                    backStack.add(QrScanner)
                }
            ) {
                Icon(
                    modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                    imageVector = Icons.Rounded.QrCode,
                    contentDescription = null
                )
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .clip(shape = RoundedCornerShape(30.dp, 30.dp, 0.dp, 0.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
            ) {
                LazyColumn(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    item {
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            text = stringResource(id = R.string.favouriteStops),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        favStopCarrousel()
                    }
                    item {
                        Text(
                            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
                            text = stringResource(id = R.string.recentStops),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    item {
                        AnimatedContent(lastStops.count()) { count ->
                            when (count) {
                                0 -> {
                                    Box(
                                        modifier = Modifier
                                            .height(208.dp)
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.extraLarge)
                                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.noRecentStops),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                else -> {
                                    lastStops.forEach { stopItem ->
                                        val shape = listShape(lastStops.indexOf(stopItem), lastStops.count())
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 4.dp)
                                                .clip(shape = shape)
                                                .clickable(
                                                    onClick = { onRecentOpen(stopItem) }
                                                )
                                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                                .animateItem(
                                                    fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                                                    placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                                                    fadeOutSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
                                                ),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.62f)
                                                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
                                                text = stopItem.name
                                                    .replace("-", " - ")
                                                    .replace(".", ". ")
                                                    .replace("  ", " "),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                            StopEmblemRow(
                                                modifier = Modifier.padding(end = 12.dp),
                                                stopItem = stopItem,
                                                lines = lines
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
@Preview
fun HomePagePreview() {
    val backStack = rememberNavBackStack(MainView)
    val lastStops = listOf(
        StopItem(
            name = "A stop with a really really really long name",
            lines = listOf(1, 2, 3)
        )
    )
    val lineItems = listOf(
        LineItem(id = 1, emblem = "DL"),
        LineItem(id = 2, emblem = "TST"),
        LineItem(id = 3, emblem = "LONG"),
    )
    HomePageView(
        backStack = backStack,
        lastStops = lastStops,
        lines = lineItems,
        favStopCarrousel = { FavStopCarouselPreview() },
        onRecentOpen = {}
    )
}