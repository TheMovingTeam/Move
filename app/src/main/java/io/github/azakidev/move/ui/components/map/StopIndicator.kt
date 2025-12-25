package io.github.azakidev.move.ui.components.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.colorResource
import io.github.azakidev.move.R
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.expressions.value.SymbolPlacement
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable

@Composable
@MaplibreComposable
fun StopIndicator(
    lat: Double,
    lon: Double,
    name: String = "stop"
) {
    val locationData = rememberGeoJsonSource(
        GeoJsonData.JsonString(
            """
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [${lon}, ${lat}]
                    }
                }
            """.trimIndent()
        )
    )

    SymbolLayer(
        id = name,
        source = locationData,
        placement = const(SymbolPlacement.Point),
        iconImage = image(
            value = rememberVectorPainter(Icons.Rounded.LocationOn),
            drawAsSdf = true
        ),
        iconAnchor = const(SymbolAnchor.Bottom),
        iconColor = const(colorResource(R.color.purple_brand))
    )
}