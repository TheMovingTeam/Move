package io.github.azakidev.move.ui.pages

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.utsman.osmandcompose.DefaultMapProperties
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.ZoomButtonVisibility
import com.utsman.osmandcompose.rememberCameraState
import io.github.azakidev.move.R
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun MapPage(
    fusedLocationProviderClient: FusedLocationProviderClient
) {
    val context = LocalContext.current.applicationContext

    val locationFinePermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    var lat = 0.0
    var lon = 0.0

    LaunchedEffect(key1 = null) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationFinePermissionState.launchPermissionRequest()
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                lat = location.latitude
                lon = location.longitude
            }
        }
    }

    val cameraState = rememberCameraState {
        geoPoint = GeoPoint(lat, lon)
        zoom = 20.0
    }

    LaunchedEffect(key1 = null) {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                lat = location.latitude
                lon = location.longitude
                cameraState.animateTo(
                    point = GeoPoint(lat, lon),
                    pZoom = 20.0
                )
            }
        }
    }

    var mapProperties by remember { mutableStateOf(DefaultMapProperties) }

    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
            placeholder = {
                Text(
                    text = stringResource(R.string.searchPlaceholder)
                )
            },
        )
    }

    SideEffect {
        mapProperties = mapProperties
            .copy(tileSources = TileSourceFactory.MAPNIK)
            .copy(isEnableRotationGesture = true)
            .copy(zoomButtonVisibility = ZoomButtonVisibility.NEVER)
            .copy(isFlingEnable = true)
            .copy(isAnimating = false)
            .copy(minZoomLevel = 5.0)
    }
    Scaffold(
        topBar = {
            AppBarWithSearch(
                modifier = Modifier.padding(bottom = 8.dp),
                state = searchBarState,
                inputField = inputField,
                colors = SearchBarDefaults.appBarWithSearchColors(
                    appBarContainerColor = Color.Transparent
                ),
                scrollBehavior = null
            )
            ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {}
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(8.dp),
                shape = FloatingActionButtonDefaults.largeShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                        location?.let {
                            lat = location.latitude
                            lon = location.longitude
                            cameraState.animateTo(
                                point = GeoPoint(lat, lon),
                                pZoom = 20.0
                            )
                        }
                    }
                }
            ) {
                Icon(
                    modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                    imageVector = Icons.Filled.GpsFixed,
                    contentDescription = stringResource(id = R.string.search)
                )
            }
        },
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            OpenStreetMap(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                properties = mapProperties,

            )
        }
    }
}

