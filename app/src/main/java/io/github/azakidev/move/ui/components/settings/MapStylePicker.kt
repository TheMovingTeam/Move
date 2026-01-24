package io.github.azakidev.move.ui.components.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.data.items.MapStyle
import io.github.azakidev.move.ui.PADDING

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapStylePicker(
    savedMapStyle: MapStyle,
    currentMapStyle: MutableState<MapStyle>
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = PADDING.dp, topEnd = PADDING.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(top = PADDING.div(4).dp)
            .padding(horizontal = PADDING.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(PADDING.div(2).dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(MapStyle.entries.count()) {
            val style = MapStyle.entries[it]

            val roundness by animateDpAsState(
                targetValue = if(style == currentMapStyle.value) 12.dp else if (style == savedMapStyle) 18.dp else 24.dp,
                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
            )

            val outlineWidth by animateDpAsState(
                targetValue = if(style == currentMapStyle.value) 8.dp else if (style == savedMapStyle) 4.dp else 0.dp,
                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
            )

            Column(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(
                        onClick = { currentMapStyle.value = style }
                    )
                    .padding(PADDING.div(2).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
            ) {
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .clip(RoundedCornerShape(roundness))
                        .background(style.color)
                ) { }
                Text(
                    text = style.name,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable @Preview
fun MapStylePreview() {
    val savedMapStyle = MapStyle.Liberty
    val currentMapStyle = remember { mutableStateOf(MapStyle.Liberty) }

    MapStylePicker(
        savedMapStyle = savedMapStyle,
        currentMapStyle = currentMapStyle
    )
}