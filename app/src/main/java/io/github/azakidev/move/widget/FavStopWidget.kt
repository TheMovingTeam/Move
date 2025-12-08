package io.github.azakidev.move.widget

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toColorLong
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.github.azakidev.move.MainActivity
import io.github.azakidev.move.R
import io.github.azakidev.move.data.db.MoveDatabase
import io.github.azakidev.move.data.db.entities.toLineItem
import io.github.azakidev.move.data.db.entities.toProviderItem
import io.github.azakidev.move.data.db.entities.toStopItem
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.LineTime
import io.github.azakidev.move.data.items.ProviderItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.widget.FavStopWidgetConfig.providerIdKey
import io.github.azakidev.move.widget.FavStopWidgetConfig.stopIdKey
import io.github.azakidev.move.ui.fmt
import io.github.azakidev.move.utils.fetchStopTime
import io.github.azakidev.move.worker.FavStopUpdaterWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.collections.map

const val CORNER_RADIUS = 24
const val PADDING = 8

const val SMALL = 80
const val BIG = 210

enum class Sizes(val size: DpSize) {
    SMALL_SQUARE(DpSize(SMALL.dp, SMALL.dp)),
    HORIZONTAL_RECTANGLE(DpSize(BIG.dp, SMALL.dp)),

    VERTICAL_RECTANGLE(DpSize(SMALL.dp, BIG.dp)),
    BIG_SQUARE(DpSize(BIG.dp, BIG.dp))
}

class FavStopWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FavStopWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        // Cancel any ongoing work for the deleted widgets
        appWidgetIds.forEach { appWidgetId ->
            WorkManager.getInstance(context).cancelUniqueWork("favStopUpdate_$appWidgetId")
        }
    }
}

class FavStopWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            Sizes.SMALL_SQUARE.size,
            Sizes.HORIZONTAL_RECTANGLE.size,
            Sizes.VERTICAL_RECTANGLE.size,
            Sizes.BIG_SQUARE.size
        )
    )

    override val stateDefinition = FavStopWidgetConfig

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val frameworkAppWidgetId =
            GlanceAppWidgetManager(context).getAppWidgetId(id)


        provideContent {
            val stopId = currentState(stopIdKey)
            val providerId = currentState(providerIdKey)
            val coroutineScope = rememberCoroutineScope()

            var stopItemState by remember { mutableStateOf<StopItem?>(null) }
            var providerItemState by remember { mutableStateOf<ProviderItem?>(null) }
            var linesState by remember { mutableStateOf<List<LineItem>>(emptyList()) }

            LaunchedEffect(stopId, providerId) {
                if (stopId == null || providerId == null) {
                    // Clear the state if configuration is incomplete
                    stopItemState = null
                    linesState = emptyList()
                    return@LaunchedEffect
                }

                launch(Dispatchers.IO) {
                    val stopEntity = MoveDatabase
                        .getDatabase(context)
                        .stopDao()
                        .getStopById(stopId)

                    val lineEntities = MoveDatabase
                        .getDatabase(context)
                        .lineDao()
                        .getLinesForProvider(providerId)


                    stopItemState = stopEntity?.toStopItem() ?: StopItem()
                    linesState = lineEntities.map { it.toLineItem() }

                    // Schedule periodic updates for this widget
                    scheduleUpdate(context, frameworkAppWidgetId, stopId)
                    if (stopItemState != null && linesState.isNotEmpty()) {
                        val providerItem =
                            MoveDatabase.getDatabase(context)
                                .providerDao()
                                .getProviderById(stopItemState!!.provider)

                        if (providerItem != null) {
                            providerItemState = providerItem.toProviderItem()

                            fetchStopTime(providerItemState!!, stopItemState!!, linesState)
                        }
                    }
                }
            }

            val stopItem = stopItemState
            val lines = linesState
            val provider = providerItemState

            GlanceTheme {
                if (stopItem != null && stopItem != StopItem()) {
                    val sortedLineTimes = stopItem.lineTimes.collectAsState().value
                        ?.sortedBy { it.nextTimeFirst }
                        ?.take(6) ?: emptyList()

                    FavStopWidgetContent(
                        stopItem,
                        lines,
                        sortedLineTimes,
                        onRefresh = {
                            coroutineScope.launch(Dispatchers.IO) {
                                if (provider != null) {
                                    fetchStopTime(
                                        provider,
                                        stopItem,
                                        lines
                                    )
                                }
                            }
                        })
                } else {
                    // Display a message if stop not found
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(
                                GlanceTheme.colors.background
                            )
                            .cornerRadius(CORNER_RADIUS.dp)
                            .padding(PADDING.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (stopItem == null) {
                            Text(
                                text = context.resources.getString(R.string.favStopWidgetLoading),
                                style = TextStyle(color = GlanceTheme.colors.onBackground)
                            )
                        } else {
                            Column {
                                Text(
                                    text = context.resources.getString(R.string.favStopWidgetError),
                                    style = TextStyle(color = GlanceTheme.colors.onBackground)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        super.providePreview(context, widgetCategory)

        val dummyStop = StopItem(
            id = 1,
            name = context.resources.getString(R.string.favStopPreviewStop),
            provider = 1,
        )

        val lineTimes = listOf(
            LineTime(
                lineId = 1,
                nextTimeFirst = 1
            ),
            LineTime(
                lineId = 2,
                nextTimeFirst = 6,
                nextTimeSecond = 4
            ),
            LineTime(
                lineId = 3,
                nextTimeFirst = 7
            )
        )

        val dummyLines = listOf(
            LineItem(id = 1, name = "P. Line 1", emblem = "L01"),
            LineItem(id = 2, name = "P. Line 2", emblem = "L02"),
            LineItem(id = 3, name = "P. Line 3", emblem = "L03")
        )

        provideContent {
            GlanceTheme {
                FavStopWidgetContent(
                    dummyStop,
                    dummyLines,
                    lineTimes,
                    forceWideLayout = true,
                    onRefresh = {}
                )
            }
        }
    }

    private fun scheduleUpdate(context: Context, appWidgetId: Int, stopId: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putInt(FavStopUpdaterWorker.KEY_APP_WIDGET_ID, appWidgetId)
            .putInt(FavStopUpdaterWorker.KEY_STOP_ID, stopId)
            .build()

        val updateRequest = PeriodicWorkRequestBuilder<FavStopUpdaterWorker>(
            15,
            TimeUnit.MINUTES,
            5,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag("favStopWorker_$appWidgetId")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "favStopUpdate_$appWidgetId",
            ExistingPeriodicWorkPolicy.UPDATE, // Replace existing work if it exists
            updateRequest
        )
    }
}

@Composable
fun FavStopWidgetContent(
    stopItem: StopItem,
    lineItems: List<LineItem>,
    lineTimes: List<LineTime>,
    forceWideLayout: Boolean = false,
    onRefresh: () -> Unit,
) {
    val size = LocalSize.current

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                GlanceTheme.colors.background
            )
            .cornerRadius(CORNER_RADIUS.dp)
            .padding(PADDING.dp)
            .clickable(
                onClick = actionStartActivity<MainActivity>()
            ),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                modifier = GlanceModifier.padding(
                    start = (PADDING / 2).dp,
                    end = (22 + (PADDING / 2)).dp
                ),
                text = stopItem.name.fmt(),
                maxLines = 1,
                style = TextStyle(color = GlanceTheme.colors.onBackground)
            )
            Spacer(
                GlanceModifier.size(PADDING.dp)
            )


            if (size.width >= Sizes.HORIZONTAL_RECTANGLE.size.width || forceWideLayout)
            // Horizontal rectangle
                Row(
                    modifier = GlanceModifier.fillMaxSize(),
                ) {
                    FirstTime(
                        modifier = GlanceModifier.defaultWeight(),
                        lineItems,
                        lineTimes
                    )
                    MoreTimes(
                        modifier = GlanceModifier.defaultWeight(),
                        lineItems,
                        lineTimes.take(5)
                    )
                }
            else {
                if (size.height >= Sizes.VERTICAL_RECTANGLE.size.height) {
                    // Vertical rectangle
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                    ) {
                        FirstTime(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            lineItems,
                            lineTimes
                        )
                        MoreTimes(
                            modifier = GlanceModifier.fillMaxWidth(),
                            lineItems,
                            lineTimes.take(4)
                        )
                    }
                } else {
                    FirstTime(
                        modifier = GlanceModifier.fillMaxSize(),
                        lineItems,
                        lineTimes.take(1)
                    )
                }
            }
        }
        CircleIconButton(
            modifier = GlanceModifier.size(24.dp),
            imageProvider = ImageProvider(
                R.drawable.refresh
            ),
            contentDescription = "Refresh",
            onClick = onRefresh,
        )
    }
}

@Composable
fun FirstTime(modifier: GlanceModifier, lineItems: List<LineItem>, lineTimes: List<LineTime>) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius((CORNER_RADIUS - PADDING).dp)
            .padding((PADDING / 1).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = context.resources.getString(R.string.nextUp),
            style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer)
        )
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val firstTime = lineTimes.firstOrNull()
            if (firstTime != null) {
                val line = lineItems.firstOrNull { it.id == firstTime.lineId }
                if (line != null) {

                    val color = when (line.color) {
                        null -> GlanceTheme.colors.tertiary.getColor(context)
                        else -> Color(line.color.toColorInt())
                    }

                    val textColor =
                        if (color.value != MaterialTheme.colorScheme.primary.value) {
                            if (ColorUtils.calculateContrast(
                                    Color.White.toColorLong().toColorInt(),
                                    color.toColorLong().toColorInt()
                                ) < 1.85f
                            ) {
                                GlanceTheme.colors.onPrimary
                            } else {
                                GlanceTheme.colors.onBackground
                            }
                        } else {
                            GlanceTheme.colors.onTertiary
                        }

                    val text =
                        if (firstTime.nextTimeFirst == 0) context.resources.getString(R.string.soon)
                        else firstTime.nextTimeFirst.toString()

                    val spacing =
                        if (firstTime.nextTimeFirst == 0) 0.05.sp
                        else 0.1.sp

                    GlanceText(
                        text = text,
                        fontSize = 64.sp,
                        font = R.font.fredoka_bold,
                        color = GlanceTheme.colors.onPrimaryContainer.getColor(context),
                        letterSpacing = spacing
                    )
                    Spacer(
                        GlanceModifier.size(PADDING.dp)
                    )
                    GlanceText(
                        modifier = GlanceModifier
                            .padding(
                                vertical = (PADDING / 1.65).dp,
                                horizontal = (PADDING * 1.65).dp
                            )
                            .background(color)
                            .cornerRadius((CORNER_RADIUS * 2).dp),
                        text = line.emblem,
                        color = textColor.getColor(context),
                        font = R.font.fredoka_medium,
                        fontSize = 14.sp
                    )
                }
            } else {
                GlanceText(
                    text = "-",
                    fontSize = 64.sp,
                    font = R.font.fredoka_bold,
                    color = GlanceTheme.colors.onPrimaryContainer.getColor(context)
                )
            }
        }
    }
}

@Composable
fun MoreTimes(modifier: GlanceModifier, lineItems: List<LineItem>, lineTimes: List<LineTime>) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .padding((PADDING / 2).dp)
            .padding(horizontal = (PADDING / 2).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            style = TextStyle(
                color = GlanceTheme.colors.onBackground
            ),
            text = context.resources.getString(R.string.comingUp)
        )
        lineTimes.drop(1).forEach { lineTime ->
            val line = lineItems.firstOrNull { it.id == lineTime.lineId }
            if (line != null) {

                val color = when (line.color) {
                    null -> GlanceTheme.colors.tertiary.getColor(context)
                    else -> Color(line.color.toColorInt())
                }

                val textColor =
                    if (color.value != MaterialTheme.colorScheme.primary.value) {
                        if (ColorUtils.calculateContrast(
                                Color.White.toColorLong().toColorInt(),
                                color.toColorLong().toColorInt()
                            ) < 1.85f
                        ) {
                            GlanceTheme.colors.onPrimary
                        } else {
                            GlanceTheme.colors.onBackground
                        }
                    } else {
                        GlanceTheme.colors.onTertiary
                    }

                val text =
                    if (lineTime.nextTimeFirst == 0) context.resources.getString(R.string.soon)
                    else lineTime.nextTimeFirst.toString()


                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier
                            .padding(bottom = (PADDING / 2).dp)
                    ) {
                        GlanceText(
                            modifier = GlanceModifier
                                .background(color)
                                .padding(
                                    vertical = (PADDING / 1.65).dp,
                                    horizontal = (PADDING / 1.35).dp
                                )
                                .cornerRadius((CORNER_RADIUS / 3).dp),
                            text = line.emblem,
                            color = textColor.getColor(context),
                            font = R.font.fredoka_medium,
                            fontSize = 14.sp,
                        )
                    }
                    Text(
                        modifier = GlanceModifier
                            .padding(horizontal = (PADDING / 4).dp)
                            .defaultWeight(),
                        text = lineTime.destination?.fmt() ?: line.name.fmt(),
                        maxLines = 1,
                        style = TextStyle(
                            color = GlanceTheme.colors.onBackground,
                            textAlign = TextAlign.End,
                        )
                    )
                    Text(
                        modifier = GlanceModifier.defaultWeight(),
                        text = "$text m.",
                        maxLines = 1,
                        style = TextStyle(
                            color = GlanceTheme.colors.onBackground,
                            textAlign = TextAlign.End,
                        )
                    )
                }
            }
        }
    }
}

@Suppress("unused")
@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview(widthDp = 400, heightDp = 200)
@Preview(widthDp = 200, heightDp = 300)
@Preview(widthDp = 200, heightDp = 200)
fun FavStopWidgetPreview() {

    val dummyStop = StopItem(
        id = 1,
        name = "Preview Stop",
        provider = 1,
    )

    val lineTimes = listOf(
        LineTime(
            lineId = 1,
            nextTimeFirst = 1,
            nextTimeSecond = 3
        ),
        LineTime(
            lineId = 2,
            nextTimeFirst = 2,
            nextTimeSecond = 4
        ),
        LineTime(
            lineId = 3,
            nextTimeFirst = 3,
            nextTimeSecond = 5
        )
    )

    val dummyLines = listOf(
        LineItem(id = 1, name = "P-Line 1", emblem = "PL1"),
        LineItem(id = 2, name = "P-Line 2", emblem = "PL2"),
        LineItem(id = 3, name = "P-Line 3", emblem = "PL3")
    )

    GlanceTheme {
        Box(
            modifier = GlanceModifier.fillMaxSize().background(Color.Black)
        ) {
            FavStopWidgetContent(
                dummyStop,
                dummyLines,
                lineTimes,
                onRefresh = {}
            )
        }
    }
}