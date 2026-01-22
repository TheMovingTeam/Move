package io.github.azakidev.move.ui.pages.panes

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.data.items.toKey
import io.github.azakidev.move.ui.MainView
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.Settings
import io.github.azakidev.move.ui.components.favStops.FavStopCarousel
import io.github.azakidev.move.ui.components.favStops.FavStopCarouselPreview
import io.github.azakidev.move.ui.components.qr.QrFAB
import io.github.azakidev.move.ui.components.common.StopRow
import io.github.azakidev.move.ui.components.common.EmptyCard
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
        .filter { model.lastStops.collectAsState().value.contains(it.toKey()) }
    val stopMap = unorderedLastStops.associateBy { it.id }
    val lastStops = model.lastStops.collectAsState().value
        .mapNotNull { id ->
            stopMap[id.stopId]
        }
        .reversed()

    HomePageContent(
        modifier = modifier,
        backStack = backStack,
        lastStops = lastStops,
        lines = model.lines.collectAsState().value,
        onRecentOpen = { stopItem ->
            sheetModel.sheetStop = stopItem
            sheetModel.showBottomSheet = true
            model.saveLastStop(stopItem.toKey())
        },
        favStopCarrousel = { FavStopCarousel(model, sheetModel) },
        fabShouldAppear
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomePageContent(
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
                        autoSize = TextAutoSize.StepBased(
                            MaterialTheme.typography.displaySmallEmphasized.fontSize.times(0.4),
                            MaterialTheme.typography.displaySmallEmphasized.fontSize
                        ),
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(paddingValues)
                    .padding(top = paddingValues.calculateTopPadding())
                    .clip(
                        shape = RoundedCornerShape(
                            30.dp,
                            30.dp,
                            0.dp,
                            0.dp
                        )
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                verticalArrangement = Arrangement.spacedBy(PADDING.dp),
                contentPadding = PaddingValues(
                    top = PADDING.dp,
                    start = PADDING.dp,
                    end = PADDING.dp,
                    bottom = PADDING.div(4).dp
                )
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
                                EmptyCard(stringResource(R.string.noRecentStops))
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
    HomePageContent(
        modifier = modifier,
        backStack = backStack,
        lastStops = lastStops,
        lines = lineItems,
        onRecentOpen = {},
        favStopCarrousel = { FavStopCarouselPreview() },
        fabShouldAppear = fabShouldAppear
    )
}
