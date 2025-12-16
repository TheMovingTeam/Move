package io.github.azakidev.move.ui.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.ui.HERO_HEIGHT
import io.github.azakidev.move.ui.MainView
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.Settings
import io.github.azakidev.move.ui.components.FavStopCarousel
import io.github.azakidev.move.ui.components.FavStopCarouselPreview
import io.github.azakidev.move.ui.components.QrFAB
import io.github.azakidev.move.ui.components.StopRow
import io.github.azakidev.move.ui.listShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    model: MoveViewModel,
    sheetModel: SheetStopViewModel,
    backStack: NavBackStack<NavKey>,
    fabShouldAppear: Boolean = true,
) {
    val unorderedLastStops = model.stops.collectAsState().value
        .filter { model.lastStops.collectAsState().value.map{ stop -> stop.first }.contains(it.id) }
    val stopMap = unorderedLastStops.associateBy { it.id }
    val lastStops = model.lastStops.collectAsState().value
        .mapNotNull { id ->
            stopMap[id.first]
        }
        .reversed()

    HomePageView(
        modifier = modifier,
        backStack = backStack,
        lastStops = lastStops,
        lines = model.lines.collectAsState().value,
        onRecentOpen = { stopItem ->
            sheetModel.sheetStop = stopItem
            sheetModel.showBottomSheet = true
            val stopKey = Pair(sheetModel.sheetStop.id, sheetModel.sheetStop.provider)
            model.saveLastStop(stopKey)
        },
        favStopCarrousel = { FavStopCarousel(model, sheetModel) },
        fabShouldAppear
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomePageView(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey>,
    lastStops: List<StopItem>,
    lines: List<LineItem>,
    onRecentOpen: (StopItem) -> Unit,
    favStopCarrousel: @Composable () -> Unit,
    fabShouldAppear: Boolean = true
) {
    val greetings = stringArrayResource(R.array.greetings)
    greetings.shuffle()
    val greeting = rememberSaveable { greetings.first() }

    val fredokaFontFamily = FontFamily(
        Font(R.font.fredoka_medium, FontWeight.Medium),
        Font(R.font.fredoka_bold, FontWeight.Bold)
    )

    val sectionTitleModifier = Modifier.padding(start = PADDING.times(0.75).dp)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = greeting,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = fredokaFontFamily,
                        style = MaterialTheme.typography.displaySmallEmphasized,
                        fontWeight = FontWeight.Bold
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
            if (fabShouldAppear) {
                QrFAB(backStack = backStack)
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = 0.dp
                    )
                    .clip(
                        shape = RoundedCornerShape(
                            30.dp,
                            30.dp,
                            0.dp,
                            0.dp
                        )
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainer),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .padding(top = PADDING.dp)
                        .padding(horizontal = PADDING.dp),
                    verticalArrangement = Arrangement.spacedBy(PADDING.dp)
                ) {
                    item {
                        Text(
                            modifier = sectionTitleModifier,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                            text = stringResource(id = R.string.favouriteStops),
                        )
                    }
                    item {
                        favStopCarrousel()
                    }
                    item {
                        Text(
                            modifier = sectionTitleModifier,
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
                                            .height(HERO_HEIGHT.dp)
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
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
                                    ) {
                                        lastStops.forEach { stopItem ->
                                            val shape = listShape(
                                                lastStops.indexOf(stopItem),
                                                lastStops.count()
                                            )
                                            StopRow(
                                                shape = shape,
                                                stopItem = stopItem,
                                                lines = lines,
                                                onClick = { onRecentOpen(stopItem) }
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
fun HomePagePreview(
    modifier: Modifier = Modifier,
    fabShouldAppear: Boolean = true,
) {
    val backStack = rememberNavBackStack(MainView)
    val lastStops = listOf(
        StopItem(
            name = "A stop with a really really really long name",
            lines = listOf(1, 2, 3)
        ),
        StopItem(
            name = "A stop with a mildly long name",
            lines = listOf(2, 3)
        ),
        StopItem(
            name = "A stop",
            lines = listOf(1)
        )
    )
    val lineItems = listOf(
        LineItem(id = 1, emblem = "DL"),
        LineItem(id = 2, emblem = "TST"),
        LineItem(id = 3, emblem = "LONG"),
    )
    HomePageView(
        modifier = modifier,
        backStack = backStack,
        lastStops = lastStops,
        lines = lineItems,
        onRecentOpen = {},
        favStopCarrousel = { FavStopCarouselPreview() },
        fabShouldAppear = fabShouldAppear
    )
}