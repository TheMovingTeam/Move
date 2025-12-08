package io.github.azakidev.move.ui.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import io.github.azakidev.move.ui.MainView
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.Settings
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.ui.fmt
import io.github.azakidev.move.ui.listShape
import io.github.azakidev.move.ui.components.FavStopCarousel
import io.github.azakidev.move.ui.components.FavStopCarouselPreview
import io.github.azakidev.move.ui.components.QrFAB
import io.github.azakidev.move.ui.components.StopEmblemRow

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
        .filter { model.lastStops.collectAsState().value.contains(it.id) }
    val stopMap = unorderedLastStops.associateBy { it.id }
    val lastStops = model.lastStops.collectAsState().value
        .mapNotNull { id ->
            stopMap[id]
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
            model.saveLastStop(sheetModel.sheetStop.id)
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

    Scaffold(
        modifier = modifier.padding(bottom = 0.dp),
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
                    .padding(bottom = 0.dp)
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
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        lastStops.forEach { stopItem ->
                                            val shape = listShape(
                                                lastStops.indexOf(stopItem),
                                                lastStops.count()
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(shape = shape)
                                                    .clickable(
                                                        onClick = { onRecentOpen(stopItem) }
                                                    )
                                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val stopName = stopItem.name.fmt()
                                                val emblems =
                                                    lines.filter { it.provider == stopItem.provider }

                                                val textMod =
                                                    if (stopName.length >= 35 && emblems.count() > 2) Modifier.weight(
                                                        4f
                                                    )
                                                    else Modifier

                                                Text(
                                                    modifier = textMod.padding(vertical = 4.dp),
                                                    text = stopName,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontSize = 16.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(
                                                    modifier = Modifier.width(16.dp)
                                                )
                                                StopEmblemRow(
                                                    modifier = Modifier
                                                        .weight(1f),
                                                    stopItem = stopItem,
                                                    lines = lines.filter { it.provider == stopItem.provider }
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