package io.github.azakidev.move.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.location.AndroidLocationProvider
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable

@Composable
@MaplibreComposable
fun LocationIndicator(
    currentLocation: AndroidLocationProvider?
) {
    val location = currentLocation ?: return
    val position = location.location.collectAsState().value ?: return

    val locationData = rememberGeoJsonSource(
        GeoJsonData.JsonString(
            """
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [${position.position.longitude}, ${position.position.latitude}]
                    },
                    "properties": {}
                }
            """.trimIndent()
        )
    )

    CircleLayer(
        id = "position-shade",
        source = locationData,
        color = const(colorResource(R.color.purple_shadow).copy(alpha = 0.25f)),
        blur = const(0.5f),
        radius = const(30.dp)
    )

    CircleLayer(
        id = "position-outline",
        source = locationData,
        color = const(Color.White),
        radius = const(10.dp)
    )

    CircleLayer(
        id = "position",
        source = locationData,
        color = const(colorResource(R.color.purple_brand)),
        radius = const(8.dp)
    )
}