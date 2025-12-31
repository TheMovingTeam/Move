package io.github.azakidev.move.ui.components.map

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.StopItem
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.exponential
import org.maplibre.compose.expressions.dsl.interpolate
import org.maplibre.compose.expressions.dsl.zoom
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import kotlin.collections.forEach
import kotlin.math.pow

@Composable
@MaplibreComposable
fun AllLines(
    lines: List<LineItem>,
    stops: List<StopItem>
) {
    if (lines.isEmpty()) return
    //TODO: Remove check when fully implemented
    if (!BuildConfig.DEBUG) return
    lines.forEach { line ->
        val rawStopLines =
            line.stops
                .mapNotNull { stop -> stops.find { it.id == stop && it.provider == line.provider } }

        if (rawStopLines.isEmpty()) return@forEach

        val sortedStopLines = mutableListOf<StopItem>()
        val unvisitedStops = rawStopLines.toMutableSet()

        var currentStop = rawStopLines.first()

        sortedStopLines.add(currentStop)
        unvisitedStops.remove(currentStop)

        // Greedily find the next closest stop until all stops are added
        while (unvisitedStops.isNotEmpty()) {
            var closestStop: StopItem? = null
            var minDistanceSquared = Double.MAX_VALUE

            for (nextStopCandidate in unvisitedStops) {
                // GeoX and GeoY cannot be null here
                // Stops with null here get filtered off in MapSurface
                val dx = nextStopCandidate.geoY!! - currentStop.geoY!!
                val dy = nextStopCandidate.geoX!! - currentStop.geoX!!
                val distanceSquared = dx.pow(2) + dy.pow(2)

                if (distanceSquared < minDistanceSquared) {
                    minDistanceSquared = distanceSquared
                    closestStop = nextStopCandidate
                }
            }

            if (closestStop != null) {
                sortedStopLines.add(closestStop)
                unvisitedStops.remove(closestStop)
                currentStop = closestStop // Move to the newly added stop
            } else {
                // Break to prevent infinite loop.
                break
            }
        }

        var posArray = ""

        sortedStopLines.forEach {
            posArray += "[${it.geoY}, ${it.geoX}]"

            if (it != sortedStopLines.last()) {
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
            color = const(color),
            cap = const(LineCap.Round),
            join = const(LineJoin.Round),
            roundLimit = const(1.5f),
            width =
                interpolate(
                    type = exponential(1.2f),
                    input = zoom(),
                    4 to const(0.dp),
                    5 to const(0.4.dp),
                    6 to const(0.7.dp),
                    7 to const(1.75.dp),
                    20 to const(8.dp),
                ),
        )
    }
}