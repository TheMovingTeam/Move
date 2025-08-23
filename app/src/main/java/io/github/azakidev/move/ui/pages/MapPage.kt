package io.github.azakidev.move.ui.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import com.utsman.osmandcompose.CameraProperty
import com.utsman.osmandcompose.CameraState
import com.utsman.osmandcompose.DefaultMapProperties
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.ZoomButtonVisibility
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MapPage() {

    val context = LocalContext.current.applicationContext

    var cameraState by remember {
        mutableStateOf(
            CameraState(
                CameraProperty(
                    geoPoint = GeoPoint(0.0, 0.0),
                    zoom = 14.0
                )
            )
        )
    }

    LaunchedEffect(cameraState.zoom) {
        val zoom = cameraState.zoom
        val geoPoint = cameraState.geoPoint
        cameraState = CameraState(
            CameraProperty(
                geoPoint = geoPoint,
                zoom = zoom
            )
        )
    }

    var mapProperties by remember {
        mutableStateOf(DefaultMapProperties)
    }

    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
            placeholder = { Text("Where to go...") },
        )
    }

    SideEffect {
        mapProperties = mapProperties
            .copy(tileSources = TileSourceFactory.MAPNIK)
            .copy(isEnableRotationGesture = false)
            .copy(zoomButtonVisibility = ZoomButtonVisibility.NEVER)
            .copy(isFlingEnable = true)
            .copy(isAnimating = false)
            .copy(minZoomLevel = 5.0)
    }
    Scaffold (
        topBar = {
            AppBarWithSearch(
                state = searchBarState,
                inputField = inputField,
                colors = SearchBarDefaults.appBarWithSearchColors(
                    appBarContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = null
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(paddingValues),
            ) {
                Surface (
                    modifier = Modifier
                        .matchParentSize()
                        .clip(shape = RoundedCornerShape(50.dp, 50.dp, 0.dp, 0.dp))
                ) {
                    OpenStreetMap(
                        modifier = Modifier.matchParentSize(),
                        cameraState = cameraState,
                        properties = mapProperties
                    )
                }
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .size(64.dp),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    onClick = { Toast.makeText(context, "Unimplemented", Toast.LENGTH_SHORT ).show() },
                    shape = MaterialShapes.Clover8Leaf.toShape(),
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    content = {
                        Icon(
                            modifier = Modifier
                                .size(FloatingActionButtonDefaults.MediumIconSize),
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.search)
                        )
                    }
                )
            }
        }
    )

}

