package io.github.azakidev.move.ui.pages.panes.settings

import android.Manifest
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.items.MapStyle
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.map.LocationIndicator
import io.github.azakidev.move.ui.components.map.MapSurface
import io.github.azakidev.move.ui.components.settings.MapStylePicker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.location.AndroidLocationProvider
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.milliseconds

const val ZOOM = 14.0

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun MapSelectPage(
    model: MoveViewModel, backStack: NavBackStack<NavKey>, currentLocation: AndroidLocationProvider?
) {
    val savedMapStyle = MapStyle.entries.find { model.mapStyle.collectAsState().value == it.name }
        ?: MapStyle.Liberty
    val currentMapStyle = remember { mutableStateOf(savedMapStyle) }

    val coroutineScope = rememberCoroutineScope()

    val locationFinePermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val camera = rememberCameraState(
        firstPosition = CameraPosition(
            target = currentLocation?.location?.collectAsState()?.value?.position
                ?: Position(-0.490, 38.346), zoom = ZOOM
        )
    )

    LaunchedEffect(Unit) {
        if (currentLocation == null) {
            locationFinePermissionState.launchPermissionRequest()
        }
        coroutineScope.launch {
            delay(200)
            camera.animateTo(
                finalPosition = camera.position.copy(
                    target = currentLocation?.location?.value?.position ?: Position(
                        -0.490, 38.346
                    ),
                ),
                duration = 100.milliseconds,
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ), title = {
                    Text(
                        text = stringResource(R.string.mapStylePickerTitle)
                    )
                }, navigationIcon = {
                    IconButton(
                        shape = IconButtonDefaults.standardShape,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = {
                            backStack.removeLastOrNull()
                        }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        bottomBar = {
            MapStylePicker(
                savedMapStyle = savedMapStyle,
                currentMapStyle = currentMapStyle,
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(PADDING.div(2).dp)
        ) {
            MapSurface(
                modifier = Modifier.clip(MaterialTheme.shapes.large),
                style = currentMapStyle.value,
                cameraState = camera,
                interactable = false,
                content = {
                    LocationIndicator(
                        currentLocation = currentLocation
                    )
                }
            )

            AnimatedContent(
                modifier = Modifier.align(Alignment.BottomCenter),
                targetState = currentMapStyle.value != savedMapStyle,
                transitionSpec = {
                    (fadeIn(animationSpec = MotionScheme.expressive().fastEffectsSpec()) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = MotionScheme.expressive().fastSpatialSpec()
                    )).togetherWith(
                        fadeOut(
                            animationSpec = MotionScheme.expressive().fastEffectsSpec()
                        ) + scaleOut(
                            targetScale = 0.92f,
                            animationSpec = MotionScheme.expressive().fastSpatialSpec()
                        )
                    )
                }) {
                if (it) {
                    Button(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(PADDING.dp),
                        onClick = { model.saveMapStyle(currentMapStyle.value) },
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = stringResource(R.string.save)
                        )
                        Spacer(Modifier.padding(PADDING.div(2).dp))
                        Text(
                            text = stringResource(R.string.save)
                        )
                    }
                }
            }
        }
    }
}