package io.github.azakidev.move.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.data.SheetStopModel


@Composable @Preview
fun StopPage(
    sheetModel: SheetStopModel = SheetStopModel()
) {
    Column {
        Box(
            modifier = Modifier
                .height(308.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(sheetModel.sheetStop.imageResId),
                modifier = Modifier
                    .matchParentSize()
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.FillWidth,
                contentDescription = sheetModel.sheetStop.stopName,
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
                    text = sheetModel.sheetStop.stopName,
                    style = MaterialTheme.typography.displayMedium
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Line" + " " + sheetModel.sheetStop.id.toString() + ":"
                )
                Text(
                    text = sheetModel.sheetStop.timeLeft.toString() + " " + "min."
                )
            }
        }
    }
}