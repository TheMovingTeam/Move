package io.github.azakidev.move.ui.components.map

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.data.items.Capabilities
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.ProviderItem
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
import kotlin.math.abs
import kotlin.math.pow

const val INTERPOLATED_SEGMENTS = 7

@Composable
@MaplibreComposable
fun AllLines(
    lines: List<LineItem>,
    stops: List<StopItem>,
    providers: List<ProviderItem>
) {
    //TODO: Remove check when fully implemented
    if (!BuildConfig.DEBUG) return
    // Return if no lines are found
    if (lines.isEmpty()) return

    lines.forEach { line ->
        val lineStops = line.stops.mapNotNull { stop ->
            stops.find { it.id == stop && it.provider == line.provider }
        }

        val provider = providers.find { it.id == line.provider }

        // Generate a path if none is present
        val geoJson = line.path ?: generatePath(lineStops, provider)
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
 * @param stops The list of [StopItem] in that [LineItem]
 * @param provider The [ProviderItem] correspondent to the line being generated
 */
fun generatePath(
    stops: List<StopItem>,
    provider: ProviderItem?
): String? {
    // TODO: Remove when good
    if (!BuildConfig.DEBUG) return null
    if (stops.isEmpty()) return null
    if (provider == null || !provider.capabilities.contains(Capabilities.GenPath)) return null

    val sortedStopLines = stops.sortByDistance()

    if (sortedStopLines.size < 2) return null // Need at least two points for a line

    val pathCoordinates = mutableListOf<Pair<Double, Double>>()

    // Generate Catmull-Rom spline points
    for (i in 0 until sortedStopLines.size - 1) {
        val p1 = sortedStopLines[i]
        val p2 = sortedStopLines[i + 1]

        val p0 = if (i == 0) p1 else sortedStopLines[i - 1]
        val p3 = if (i == sortedStopLines.size - 2) p2 else sortedStopLines[i + 2]

        // Add the start point of the segment
        pathCoordinates.add(Pair(p1.geoY!!, p1.geoX!!))

        var shouldInterpolate = true

        // Check for collinearity if there's a preceding point distinct from p1
        if (i > 0) {
            val x0 = p0.geoX!!
            val y0 = p0.geoY!!
            val x1 = p1.geoX
            val y1 = p1.geoY
            val x2 = p2.geoX!!
            val y2 = p2.geoY!!

            // Check for collinearity using cross product (p1-p0) x (p2-p1) == 0
            val crossProduct = (y1 - y0) * (x2 - x1) - (x1 - x0) * (y2 - y1)

            // Use a larger epsilon as a threshold
            if (abs(crossProduct) < 1.75e-6) {
                shouldInterpolate = false
            }
        }

        if (shouldInterpolate) {
            // Add interpolated points for the segment from p1 to p2 using Catmull-Rom
            for (j in 1..INTERPOLATED_SEGMENTS) {
                val t = j.toDouble() / (INTERPOLATED_SEGMENTS + 1).toDouble()
                pathCoordinates.add(catmullRomPoint(p0, p1, p2, p3, t))
            }
        }
    }

    // Add the last original stop point (P2 of the very last segment)
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

fun List<StopItem>.sortByDistance(): List<StopItem> {
    val sortedStopLines = mutableListOf<StopItem>()
    val unvisitedStops = this.toMutableSet()

    var currentStop = this.first()

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

    return sortedStopLines.toList()
}

// Helper function for Catmull-Rom interpolation
fun catmullRomPoint(p0: StopItem, p1: StopItem, p2: StopItem, p3: StopItem, t: Double): Pair<Double, Double> {
    val t2 = t * t
    val t3 = t2 * t

    val x = 0.5 * (
            (2 * p1.geoX!!) +
                    (-p0.geoX!! + p2.geoX!!) * t +
                    (2 * p0.geoX - 5 * p1.geoX + 4 * p2.geoX - p3.geoX!!) * t2 +
                    (-p0.geoX + 3 * p1.geoX - 3 * p2.geoX + p3.geoX) * t3
            )
    val y = 0.5 * (
            (2 * p1.geoY!!) +
                    (-p0.geoY!! + p2.geoY!!) * t +
                    (2 * p0.geoY - 5 * p1.geoY + 4 * p2.geoY - p3.geoY!!) * t2 +
                    (-p0.geoY + 3 * p1.geoY - 3 * p2.geoY + p3.geoY) * t3
            )
    return Pair(y, x) // geoJSON expects [longitude, latitude], which is [geoY, geoX]
}