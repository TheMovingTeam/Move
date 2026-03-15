package io.github.azakidev.move.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import io.github.azakidev.move.R
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.MapStyle
import io.github.azakidev.move.data.items.ProviderItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.ui.HERO_HEIGHT
import io.github.azakidev.move.ui.PADDING
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.location.AndroidLocationProvider
import org.maplibre.spatialk.geojson.Position

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StopMap(
    sheetModel: SheetStopViewModel,
    lines: List<LineItem>,
    stops: List<StopItem>,
    providers: List<ProviderItem>,
    style: MapStyle,
    currentLocation: AndroidLocationProvider?,
) {
    val stopItem = sheetModel.sheetStop

    if (stopItem.geoX != null && stopItem.geoY != null) {
        val camera =
            rememberCameraState(
                firstPosition =
                    CameraPosition(
                        target = Position(
                            latitude = stopItem.geoX,
                            longitude = stopItem.geoY
                        ),
                        zoom = 15.0
                    )
            )
        Text(
            modifier = Modifier.padding(top = PADDING.dp, bottom = PADDING.div(2).dp),
            text = stringResource(id = R.string.map),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
        )
        Box(
            modifier = Modifier
                .height(HERO_HEIGHT.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            MapSurface(
                cameraState = camera,
                style = style,
                interactable = false
            ) {
                AllLines(
                    lines = lines,
                    stops = stops,
                    providers = providers
                )
                StopIndicator(
                    stopItem.geoX,
                    stopItem.geoY
                )
                LocationIndicator(
                    currentLocation = currentLocation
                )
            }
        }
    }
}