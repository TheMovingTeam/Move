package io.github.azakidev.move.ui.components

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import io.github.azakidev.move.data.SheetStopModel
import io.github.azakidev.move.data.StopItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable @Preview
fun FavStopCarousel(
    sheetModel: SheetStopModel = SheetStopModel()
) {
    val stopItems = remember {
        listOf(
            StopItem(1, R.drawable.nathofjoy, "Them", 5),
            StopItem(4, R.drawable.heart_of_sea, "Painting", 4),
            StopItem(2, R.drawable.nathofjoy, "Them", 3),
            StopItem(3, R.drawable.heart_of_sea, "Place", 2),
            StopItem(0, R.drawable.nathofjoy, "Them", 1),
        )
    }

    HorizontalCenteredHeroCarousel(
        state = rememberCarouselState { stopItems.count() },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 16.dp),
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) { i ->
        val item = stopItems[i]
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
                painter = painterResource(id = item.imageResId),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 8.dp),
                        text = item.stopName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        modifier = Modifier
                            .padding(end = 16.dp, bottom = 8.dp),
                        text = item.timeLeft.toString() + " min.",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

        }

    }
}
