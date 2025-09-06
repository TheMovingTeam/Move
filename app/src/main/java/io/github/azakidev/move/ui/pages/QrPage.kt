package io.github.azakidev.move.ui.pages

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import io.github.azakidev.move.data.Capabilities
import io.github.azakidev.move.data.MoveViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import io.github.azakidev.move.R
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.StopItem
import io.github.azakidev.move.ui.components.QrScanner

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrPage(
    model: MoveViewModel,
    sheetModel: SheetStopViewModel,
    backStack: NavBackStack
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Text(stringResource(R.string.qrScanFab))
                },
                navigationIcon = {
                    IconButton(
                        shape = IconButtonDefaults.standardShape,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = { backStack.removeLastOrNull() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        println(model.providers.collectAsState().value )
        if (model.providers.collectAsState().value.count { it.capabilities.contains(Capabilities.QrScan) } >= 1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(250.dp, 250.dp)
                        .clip(MaterialTheme.shapes.large)
                ) {
                    QrScanner(
                        modifier = Modifier.matchParentSize(),
                        providers = model.providers.collectAsState().value,
                        callback = { id ->
                            val stopItem = model.stops.value.find { it.id == id } ?: StopItem()
                            sheetModel.sheetStop = stopItem
                            backStack.removeLastOrNull()
                            sheetModel.showBottomSheet = true
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
}