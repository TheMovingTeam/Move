package io.github.azakidev.move.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.data.MoveModel
import io.github.azakidev.move.data.SheetStopViewModel
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.LineTime
import io.github.azakidev.move.data.StopItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import java.util.Timer
import kotlin.collections.plus
import kotlin.concurrent.schedule

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StopPage(
    model: MoveModel,
    sheetModel: SheetStopViewModel,
    shouldFetch: Boolean = true
) {
    var icon by remember { mutableStateOf(Icons.Default.FavoriteBorder) }
    if (sheetModel.sheetStop.id in model.favouriteStops.collectAsState().value) {
        icon = Icons.Default.Favorite
    }

    var shouldLoad by remember { mutableStateOf(shouldFetch) }

    val timer = Timer().schedule(delay = 500, period = 15000, action = {
        model.fetchTimes(sheetModel.sheetStop)
    })
    if (shouldLoad) {
        timer.run()
    } else {
        timer.cancel()
    }

    Column {
        Box(
            modifier = Modifier
                .height(308.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(sheetModel.sheetStop.image),
                modifier = Modifier
                    .padding(bottom = 16.dp),
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
                Button(
                    modifier = Modifier
                        .padding(end = 12.dp, bottom = 16.dp)
                        .size(50.dp)
                        .align(Alignment.BottomEnd),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        if (sheetModel.sheetStop.id !in model.favouriteStops.value) {
                            model.addFavStop(sheetModel.sheetStop.id)
                            icon = Icons.Default.Favorite
                        } else {
                            model.removeFavStop(sheetModel.sheetStop.id)
                            icon = Icons.Default.FavoriteBorder
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            var count = 0
            sheetModel.sheetStop.lineTimes.collectAsState().value.forEach {
                val line =
                    model.lines.value.find { lineItem -> lineItem.id == it.lineId } ?: LineItem()

                val shape = when (count) {
                    0 -> {
                        RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = 2.dp,
                            bottomEnd = 2.dp,
                        )
                    }

                    line.stops.count() - 1 -> {
                        RoundedCornerShape(
                            topStart = 2.dp,
                            topEnd = 2.dp,
                            bottomStart = 8.dp,
                            bottomEnd = 8.dp
                        )
                    }

                    else -> {
                        MaterialTheme.shapes.extraSmall
                    }
                }
                count++

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = line.name
                    )
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
fun StopPagePreview() {
    val model = viewModel<MoveModel>()
    val sheetModel = viewModel<SheetStopViewModel>()
    model.setStops(
        listOf(
            StopItem(id = 1, name = "Stop 1", lines = listOf(1, 2, 3)),
        )
    )
    model.setLines(
        listOf(
            LineItem(id = 1)
        )
    )
    model.stops.collectAsState().value.first().setTimeTable(
        listOf(
            LineTime(1, 2)
        )
    )
    sheetModel.sheetStop = model.stops.collectAsState().value.first()
    StopPage(model, sheetModel, false)
}