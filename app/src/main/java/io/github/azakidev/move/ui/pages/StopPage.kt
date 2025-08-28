package io.github.azakidev.move.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun StopPage(
    model: MoveModel = MoveModel(),
    sheetModel: SheetStopViewModel = SheetStopViewModel()
) {
    var icon by remember { mutableStateOf(Icons.Default.FavoriteBorder) }
    if (sheetModel.sheetStop.id in model.favouriteStops) {
        icon = Icons.Default.Favorite
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
                        if (sheetModel.sheetStop.id !in model.favouriteStops) {
                            model.favouriteStops += sheetModel.sheetStop.id
                            icon = Icons.Default.Favorite
                        } else {
                            model.favouriteStops -= sheetModel.sheetStop.id
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
            sheetModel.sheetStop.lineTimes.forEach {
                val line = model.lines.find { lineItem -> lineItem.id == it.lineId }
                val lineName = line?.name ?: "DefaultLine"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = lineName
                    )
                    Text(
                        text = it.nextTime.toString() + " " + "min."
                    )
                }
            }
        }
    }
}