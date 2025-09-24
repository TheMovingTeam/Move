package io.github.azakidev.move.ui.pages

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.utsman.osmandcompose.CameraProperty
import com.utsman.osmandcompose.CameraState
import com.utsman.osmandcompose.DefaultMapProperties
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.ZoomButtonVisibility
import io.github.azakidev.move.R
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MapPage() {
    val context = LocalContext.current.applicationContext
    var cameraState by remember {
        mutableStateOf(
            CameraState(
                CameraProperty(
                    geoPoint = GeoPoint(0.0, 0.0), zoom = 14.0
                )
            )
        )
    }
    LaunchedEffect(cameraState.zoom) {
        val zoom = cameraState.zoom
        val geoPoint = cameraState.geoPoint
        cameraState = CameraState(
            CameraProperty(
                geoPoint = geoPoint, zoom = zoom
            )
        )
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
        mapProperties = mapProperties.copy(tileSources = TileSourceFactory.MAPNIK)
            .copy(isEnableRotationGesture = false)
            .copy(zoomButtonVisibility = ZoomButtonVisibility.NEVER).copy(isFlingEnable = true)
            .copy(isAnimating = false).copy(minZoomLevel = 5.0)
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
                onClick = { Toast.makeText(context, "Unimplemented", Toast.LENGTH_SHORT).show() },
                shape = FloatingActionButtonDefaults.largeShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
                properties = mapProperties
            )
        }
    }
}

