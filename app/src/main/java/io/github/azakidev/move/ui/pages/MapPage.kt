package io.github.azakidev.move.ui.pages

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.window.core.layout.WindowSizeClass
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import io.github.azakidev.move.R
import io.github.azakidev.move.data.items.Capabilities
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.fmtSearch
import io.github.azakidev.move.ui.components.AllLines
import io.github.azakidev.move.ui.components.AllStops
import io.github.azakidev.move.ui.components.LocationIndicator
import io.github.azakidev.move.ui.components.MapSurface
import io.github.azakidev.move.ui.components.SearchContents
import io.github.azakidev.move.ui.components.SearchInputField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.location.AndroidLocationProvider
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.milliseconds

const val ZOOM: Double = 16.5

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun MapPage(
    model: MoveViewModel,
    sheetModel: SheetStopViewModel,
    currentLocation: AndroidLocationProvider?,
) {
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

    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()

    val inputField = @Composable {
        SearchInputField(
            searchBarState, textFieldState
        )
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val topSafe = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }

    val fill =
        if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                    MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                    Color.Transparent
                )
            )
        }

    val mapModifier =
        if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
            if (windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)) {
                Modifier
                    .zIndex(1f)
                    .padding(top = topSafe)
                    .clip(RoundedCornerShape(12.dp, 0.dp, 0.dp, 0.dp))
            } else {
                Modifier
                    .zIndex(1f)
                    .padding(top = topSafe)
                    .clip(RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp))
            }
        } else {
            Modifier.zIndex(1f)
        }

    val geoProviders =
        model.providers.collectAsState().value.filter { it.capabilities.contains(Capabilities.Geo) }
            .map { it.id }

    Scaffold(
        topBar = {
            AppBarWithSearch(
                modifier = Modifier.padding(bottom = PADDING.div(2).dp),
                state = searchBarState,
                inputField = inputField,
                colors = SearchBarDefaults.appBarWithSearchColors(
                    appBarContainerColor = Color.Transparent,
                    scrolledAppBarContainerColor = Color.Transparent,
                ),
            )
            ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {
                SearchContents(
                    model,
                    sheetModel,
                    textFieldState,
                    searchBarState,
                    textFieldState.text.toString().fmtSearch()
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(PADDING.div(2).dp),
                shape = FloatingActionButtonDefaults.largeShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    coroutineScope.launch {
                        camera.animateTo(
                            finalPosition = camera.position.copy(
                                target = currentLocation?.location?.value?.position ?: Position(
                                    -0.490, 38.346
                                ),
                            ),
                            duration = 250.milliseconds,
                        )
                    }
                }) {
                Icon(
                    modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                    imageVector = Icons.Filled.GpsFixed,
                    contentDescription = stringResource(id = R.string.search)
                )
            }
        },
    ) { paddingValues ->
        val verticalPadding = if (windowSizeClass.isWidthAtLeastBreakpoint(
                WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
            )
        ) topSafe
        else topSafe.times(1.2f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(verticalPadding)
                .zIndex(2f)
                .background(
                    brush = fill
                )
        ) {}

        MapSurface(
            modifier = mapModifier,
            paddingValues = paddingValues,
            cameraState = camera,
            hasCompass = true,
            content = {
                LocationIndicator(
                    currentLocation = currentLocation
                )
                AllLines(
                    model.lines.collectAsState().value, model.stops.collectAsState().value
                )
                AllStops(model.stops.collectAsState().value.filter { it.provider in geoProviders }
                    .filter { it.geoX != null && it.geoY != null })
            })
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape",
    showSystemUi = true
)
@Preview(showSystemUi = true)
fun MapPagePreview() {
    Scaffold(
        topBar = {
            AppBarWithSearch(
                modifier = Modifier.padding(bottom = PADDING.div(2).dp),
                state = rememberSearchBarState(),
                inputField = {
                    SearchBarDefaults.InputField(
                        textFieldState = rememberTextFieldState(),
                        searchBarState = rememberSearchBarState(),
                        onSearch = {},
                    )
                },
                colors = SearchBarDefaults.appBarWithSearchColors(
                    appBarContainerColor = Color.Transparent,
                    scrolledAppBarContainerColor = Color.Transparent,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(PADDING.div(2).dp),
                shape = FloatingActionButtonDefaults.largeShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = { }) {
                Icon(
                    modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                    imageVector = Icons.Filled.GpsFixed,
                    contentDescription = stringResource(id = R.string.search)
                )
            }
        },
    ) { paddingValues ->
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

        val fill =
            if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color(0f, 0f, 0f, 0.8f), Color(0f, 0f, 0f, 0.5f), Color.Transparent
                    )
                )
            }
        val size = with(LocalDensity.current) {
            WindowInsets.statusBars.getTop(this).toDp()
        }

        val verticalPadding = if (windowSizeClass.isWidthAtLeastBreakpoint(
                WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
            )
        ) size.div(10)
        else size.times(1.2f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .zIndex(2f)
                .background(brush = fill)
        ) {}
        MapSurface(
            paddingValues = paddingValues
        )
    }
}