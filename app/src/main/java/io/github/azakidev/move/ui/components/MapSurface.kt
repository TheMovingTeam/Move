package io.github.azakidev.move.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import io.github.azakidev.move.R
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.StopItem
import kotlinx.coroutines.flow.StateFlow
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.SymbolPlacement
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.location.AndroidLocationProvider
import org.maplibre.compose.location.Location
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.MaplibreComposable

@Composable
fun MapSurface(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    cameraState: CameraState? = null,
    hasCompass: Boolean = false,
    interactable: Boolean = true,
    content: @Composable @MaplibreComposable (() -> Unit) = {}
) {
    val camera = cameraState ?: rememberCameraState()
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        MaplibreMap(
            modifier = modifier,
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
            options = MapOptions(
                ornamentOptions = OrnamentOptions(
                    padding = paddingValues,
                    isLogoEnabled = true,
                    logoAlignment = Alignment.BottomStart,
                    isScaleBarEnabled = false,
                    isCompassEnabled = hasCompass,
                    compassAlignment = Alignment.TopEnd,
                    isAttributionEnabled = false
                ),
                gestureOptions = if (interactable) GestureOptions.Standard else GestureOptions.AllDisabled
            ),
            cameraState = camera,
            content = content
        )
    }
}

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
                    },
                    "properties": {}
                }
            """.trimIndent()
        )
    )

    SymbolLayer(
        id = name,
        source = locationData,
        placement = const(SymbolPlacement.Point),
        iconColor = const(MaterialTheme.colorScheme.onErrorContainer),
        iconImage = image(rememberVectorPainter(Icons.Rounded.LocationOn))
    )
}

@Composable
@MaplibreComposable
fun AllStops(
    stops: List<StopItem>
) {
    var geoJson = """{ "type": "FeatureCollection", "features": [ """

    stops.forEach{
        geoJson += """
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [${it.geoY}, ${it.geoX}]
                    }
                }
            """.trimIndent()

        if (it != stops.last()) {
            geoJson += ", \n"
        }
    }

    geoJson = "${geoJson.removeSuffix(",")} ] }"

    val locationData = rememberGeoJsonSource(
        GeoJsonData.JsonString(geoJson)
    )

    SymbolLayer(
        id = "All stops",
        source = locationData,
        placement = const(SymbolPlacement.Point),
        iconColor = const(MaterialTheme.colorScheme.onErrorContainer),
        iconImage = image(rememberVectorPainter(Icons.Rounded.LocationOn))
    )
}

@Composable
@MaplibreComposable
fun AllLines(
    lines: List<LineItem>,
    stops: List<StopItem>
) {
    lines.forEach { line ->
        val stopLines = line.stops.mapNotNull { stop -> stops.find { it.id == stop && it.provider == line.provider } }

        var posArray = ""

        stopLines.forEach{
            posArray += "[${it.geoY}, ${it.geoX}]"

            if (it != stopLines.last()) {
                posArray += ",\n"
            }
        }

        val geoJson = """
         { 
            "type": "Feature",
            "geometry": {
               "type": "LineString",
               "coordinates": [
                   ${posArray.removeSuffix(",")}
               ]
            }
        }""".trimIndent()

        val locationData = rememberGeoJsonSource(
            GeoJsonData.JsonString(geoJson)
        )

        val color = when (line.color) {
            null -> MaterialTheme.colorScheme.primary
            else -> {
                Color(line.color.toColorInt())
            }
        }

        LineLayer(
            id = "line-${line.provider}-${line.id}",
            source = locationData,
            color = const(color)
        )
    }
}