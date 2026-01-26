package io.github.azakidev.move.ui.components.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.colorResource
import io.github.azakidev.move.R
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.items.StopItem
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.expressions.value.SymbolPlacement
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.util.MaplibreComposable

@Composable
@MaplibreComposable
fun AllStops(
    stops: List<StopItem>,
    sheetModel: SheetStopViewModel
) {
    if (stops.isEmpty()) return

    val sections = stops.joinToString {
        """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [${it.geoY}, ${it.geoX}]
                },
                "properties": {
                    "stopId": ${it.id},
                    "providerId": ${it.provider}
                }
            }
        """.trimIndent()
    }

    val geoJson = "{\"type\":\"FeatureCollection\",\"features\":[ ${sections.removeSuffix(",")} ] }"

    val locationData = rememberGeoJsonSource(
        GeoJsonData.JsonString(geoJson)
    )

    SymbolLayer(
        id = "All stops",
        source = locationData,
        placement = const(SymbolPlacement.Point),
        iconImage = image(
            value = rememberVectorPainter(Icons.Rounded.LocationOn),
            drawAsSdf = true,
        ),
        iconAnchor = const(SymbolAnchor.Bottom),
        iconColor = const(colorResource(R.color.purple_brand)),
        onClick = {
            if (it.isNotEmpty()) {
                val firstStop = it.first()
                val props = firstStop.properties

                if (props != null) {
                    val stopId = props["stopId"]
                    val providerId = props["providerId"]

                    if (stopId != null && providerId != null) {
                        val stop = stops.find { stop -> stop.id == stopId.toString().toInt() && stop.provider == providerId.toString().toInt()}
                        if (stop != null) {
                            sheetModel.sheetStop = stop
                            sheetModel.showBottomSheet = true
                        }
                    }
                }

                ClickResult.Consume
            }
            ClickResult.Pass
        }
    )
}