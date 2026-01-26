package io.github.azakidev.move.ui.components.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.data.items.MapStyle
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.MaplibreComposable

/**
 * A surface filling the max size available to its container displaying a map.
 *
 * @param modifier A [Modifier] for the map
 * @param paddingValues The optional insets on the container, used to inset the ornaments of the map
 * @param hasCompass Whether or not the map should display a compass component
 * @param interactable Whether or not the map should have it's gestures enabled
 * @param style The [MapStyle] of the map, falling back to Liberty
 * @param content The contents displayed on the map, mainly used for lines or stops
 */
@Composable
fun MapSurface(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    cameraState: CameraState? = null,
    hasCompass: Boolean = false,
    interactable: Boolean = true,
    style: MapStyle = MapStyle.Liberty,
    content: @Composable @MaplibreComposable (() -> Unit) = {}
) {
    val camera = cameraState ?: rememberCameraState()

    val style = when (style.url) {
        is String -> BaseStyle.Uri(style.url)
        is Int -> {
            val json = LocalResources.current.openRawResource(style.url).bufferedReader().readText()
            BaseStyle.Json(json)
        }
        else -> throw IllegalArgumentException("Unsupported type")
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        MaplibreMap(
            modifier = modifier,
            baseStyle = style,
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