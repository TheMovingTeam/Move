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

const val GEN_PATH = true

@Composable
@MaplibreComposable
fun AllLines(
    lines: List<LineItem>,
    stops: List<StopItem>
) {
    //TODO: Remove check when fully implemented
    if (!BuildConfig.DEBUG) return
    // Return if no lines are found
    if (lines.isEmpty()) return

    lines.forEach { line ->
        val lineStops = line.stops.mapNotNull { stop ->
            stops.find { it.id == stop && it.provider == line.provider }
        }

        // Generate a path if none is present
        val geoJson = line.path ?: generatePath(lineStops)
        if (geoJson == null) return@forEach

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

/**
 * Generate a rough geoJSON string path for a given [LineItem] for displaying in a map
 *
 * @param stops: The list of [StopItem] in that [LineItem]
 */
fun generatePath(
    stops: List<StopItem>
): String? {

    if (!GEN_PATH) return null
    if (stops.isEmpty()) return null

    val sortedStopLines = mutableListOf<StopItem>()
    val unvisitedStops = stops.toMutableSet()

    var currentStop = stops.first()

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

    // Add interpolation points
    val pathCoordinates = mutableListOf<Pair<Double, Double>>()
    val interpolationPointsPerSegment = 3 // Number of points to add between each pair of original stops

    for (i in 0 until sortedStopLines.size - 1) {
        val startStop = sortedStopLines[i]
        val endStop = sortedStopLines[i + 1]

        pathCoordinates.add(Pair(startStop.geoY!!, startStop.geoX!!)) // Add the start stop

        // Add interpolated points
        for (j in 1..interpolationPointsPerSegment) {
            val t = j.toDouble() / (interpolationPointsPerSegment + 1).toDouble()
            val interpolatedX = startStop.geoX + t * (endStop.geoX!! - startStop.geoX)
            val interpolatedY = startStop.geoY + t * (endStop.geoY!! - startStop.geoY)
            pathCoordinates.add(Pair(interpolatedY, interpolatedX))
        }
    }

    // Add the last stop if the list is not empty
    if (sortedStopLines.isNotEmpty()) {
        pathCoordinates.add(Pair(sortedStopLines.last().geoY!!, sortedStopLines.last().geoX!!))
    }

    var posArray = ""
    pathCoordinates.forEachIndexed { index, coordinate ->
        posArray += "[${coordinate.first}, ${coordinate.second}]"
        if (index != pathCoordinates.lastIndex) {
            posArray += ","
        }
    }

    return """
             {
                "type": "Feature",
                "geometry": {
                   "type": "LineString",
                   "coordinates": [
                       $posArray
                   ]
                }
            }""".trimIndent()
}