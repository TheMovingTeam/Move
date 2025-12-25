package io.github.azakidev.move.ui.components.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
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