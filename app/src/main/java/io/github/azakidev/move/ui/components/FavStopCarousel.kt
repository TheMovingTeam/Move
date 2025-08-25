package io.github.azakidev.move.ui.components

import android.annotation.SuppressLint
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
import io.github.azakidev.move.data.MoveModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.StopItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavStopCarousel(
    model: MoveModel,
    sheetModel: SheetStopViewModel
) {
    if (model.favouriteStops.count() != 0) {
        HorizontalCenteredHeroCarousel(
            state = rememberCarouselState { model.favouriteStops.count() },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 16.dp, bottom = 16.dp),
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) { i ->
            val item = model.stops.find { stopItem -> stopItem.stopId == model.favouriteStops[i] } ?: StopItem()
            Box(
                modifier = Modifier
                    .height(208.dp)
                    .maskClip(MaterialTheme.shapes.extraLarge)
                    .clickable(
                        enabled = true,
                        onClickLabel = null,
                        role = Role.Button,
                        onClick = {
                            sheetModel.sheetStop = item
                            sheetModel.showBottomSheet = true
                        }
                    ),
            ) {
                Image(
                    modifier = Modifier
                        .height(205.dp),
                    painter = painterResource(id = item.image),
                    contentDescription = item.stopName,
                    contentScale = ContentScale.Crop,
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
                        ),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 8.dp),
                        text = item.stopName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    item.lineTimes.forEach {
                        val line = model.lines.find { lineItem -> lineItem.lineId == it.lineId }
                        val lineName = line?.lineName ?: "DefaultLine"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(start = 16.dp, bottom = 8.dp),
                                text = lineName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                modifier = Modifier
                                    .padding(end = 16.dp, bottom = 8.dp),
                                text = it.nextTime.toString() + " min.",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    } else {
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
                text = stringResource(R.string.noStops),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Composable
@Preview
fun FavStopCarouselPreview() {
    val model: MoveModel = MoveModel()
    model.favouriteStops = listOf(1, 2, 3, 4, 5)
    val sheetModel: SheetStopViewModel = SheetStopViewModel()
    FavStopCarousel(model, sheetModel)
}