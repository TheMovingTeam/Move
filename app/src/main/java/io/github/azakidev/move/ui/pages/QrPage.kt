package io.github.azakidev.move.ui.pages

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.github.azakidev.move.ui.MainView
import io.github.azakidev.move.R
import io.github.azakidev.move.data.items.Capabilities
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.ui.components.QrScannerViewFinder

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun QrPage(
    model: MoveViewModel, sheetModel: SheetStopViewModel, backStack: NavBackStack<NavKey>
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ), title = {
                    Text(stringResource(R.string.qrScan))
                }, navigationIcon = {
                    IconButton(
                        shape = IconButtonDefaults.standardShape,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = { backStack.removeLastOrNull() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                })
        }) { paddingValues ->
        val cameraPermissionState = rememberPermissionState(
            Manifest.permission.CAMERA
        )
        if (cameraPermissionState.status.isGranted) {
            if (model.providers.collectAsState().value.count { it.capabilities.contains(Capabilities.QrScan) } >= 1) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    val squareSize = 750f
                    val outlineSize = squareSize + 10f
                    val outlineColor = MaterialTheme.colorScheme.primary

                    QrScannerViewFinder(
                        modifier = Modifier
                            .matchParentSize()
                            .drawWithContent {
                                drawContent()
                                drawRect(Color.Black.copy(alpha = 0.85f))
                                drawRoundRect(
                                    color = outlineColor,
                                    size = Size(outlineSize, outlineSize),
                                    topLeft = Offset(
                                        x = (size.width - outlineSize) / 2,
                                        y = (size.height - outlineSize) / 2
                                    ),
                                    cornerRadius = CornerRadius(outlineSize / 8, outlineSize / 8),
                                )
                                drawRoundRect(
                                    color = Color(0xFFFFFFFF),
                                    size = Size(squareSize, squareSize),
                                    topLeft = Offset(
                                        x = (size.width - squareSize) / 2,
                                        y = (size.height - squareSize) / 2
                                    ),
                                    cornerRadius = CornerRadius(squareSize / 8, squareSize / 8),
                                    blendMode = BlendMode.DstOut,
                                )
                            },
                        providers = model.providers.collectAsState().value.filter {
                            model.savedProviders.collectAsState().value.contains(
                                it.id
                            )
                        },
                        callback = { response ->
                            val stopItem =
                                if (response.second.capabilities.contains(Capabilities.ComId)) {
                                    model.stops.value.find { it.comId == response.first }
                                        ?: StopItem()
                                } else {
                                    model.stops.value.find { it.id == response.first } ?: StopItem()
                                }
                            if (stopItem != StopItem()) {
                                if (backStack.last() != MainView) {
                                    backStack.removeLastOrNull()
                                }
                                sheetModel.sheetStop = stopItem
                                val stopKey = Pair(sheetModel.sheetStop.id, sheetModel.sheetStop.provider)
                                model.saveLastStop(stopKey)
                                sheetModel.showBottomSheet = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Stop not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            } else { //NoProviderPage
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.noQrProviders),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        } else { // NoPermissionPage
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.noCameraPermission)
                )
                SideEffect {
                    cameraPermissionState.launchPermissionRequest()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@Preview
fun QrPreview() {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ), title = {
                    Text(stringResource(R.string.qrScan))
                }, navigationIcon = {
                    IconButton(
                        shape = IconButtonDefaults.standardShape,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = { }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clip(
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            val squareSize = 750f
            val outlineSize = squareSize + 10f
            val outlineColor = MaterialTheme.colorScheme.primary

            QrScannerViewFinder(
                modifier = Modifier
                    .matchParentSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            color = Color.Black.copy(alpha = 0.8f),
                            size = size,
                        )
                        drawRoundRect(
                            color = outlineColor,
                            size = Size(outlineSize, outlineSize),
                            topLeft = Offset(
                                x = (size.width / 2) - (outlineSize / 2),
                                y = (size.height / 2) - (outlineSize / 2)
                            ),
                            cornerRadius = CornerRadius(outlineSize / 8, outlineSize / 8),
                        )
                        drawRoundRect(
                            color = Color(0xFFFFFFFF),
                            size = Size(squareSize, squareSize),
                            topLeft = Offset(
                                x = (size.width / 2) - (squareSize / 2),
                                y = (size.height / 2) - (squareSize / 2)
                            ),
                            cornerRadius = CornerRadius(squareSize / 8, squareSize / 8),
                            blendMode = BlendMode.DstOut,
                        )
                    },
                providers = emptyList(),
                callback = { }
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun NoPermissionPreview() {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ), title = {
                    Text(stringResource(R.string.qrScan))
                }, navigationIcon = {
                    IconButton(
                        shape = IconButtonDefaults.standardShape,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = { }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                })
        }) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.noCameraPermission)
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun NoQrPreview() {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ), title = {
                    Text(stringResource(R.string.qrScan))
                }, navigationIcon = {
                    IconButton(
                        shape = IconButtonDefaults.standardShape,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = { }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                })
        }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "No added providers support QR codes.",
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}