@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.azakidev.move.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import io.github.azakidev.move.MainView
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveModel
import io.github.azakidev.move.data.ProviderItem
import java.util.Timer
import kotlin.concurrent.schedule
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersPage(
    model: MoveModel,
    backStack: NavBackStack,
) {

    var shouldLoad by rememberSaveable { mutableStateOf(model.providers.value.count() == 0) }

    val timer = Timer().schedule(delay = 1000, period = 5000, action = {
        if (model.providers.value.count() == 0) {
            model.fetchProviders()
            model.fetchInfo()
        } else {
            Timer().schedule(delay = 1000, action = {
                shouldLoad = false
            })
        }
    })

    if (shouldLoad) {
        timer.run()
    } else {
        timer.cancel()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.providerTitle)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            backStack.removeLastOrNull()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
        ) {
            if (!shouldLoad) {
                LazyColumn(
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    var count = 0
                    items(model.providers.value.count()) { i ->
                        var shape = when (count) {
                            0 -> {
                                RoundedCornerShape(
                                    topStart = 8.dp,
                                    topEnd = 8.dp,
                                    bottomStart = 2.dp,
                                    bottomEnd = 2.dp,
                                )
                            }

                            model.providers.collectAsState().value.count() - 1 -> {
                                RoundedCornerShape(
                                    topStart = 2.dp,
                                    topEnd = 2.dp,
                                    bottomStart = 8.dp,
                                    bottomEnd = 8.dp
                                )
                            }

                            else -> {
                                MaterialTheme.shapes.extraSmall
                            }
                        }
                        count++

                        if (model.providers.collectAsState().value.count() == 1) {
                            shape = MaterialTheme.shapes.medium
                        }

                        var icon by remember { mutableStateOf(Icons.Default.FavoriteBorder) }
                        if (model.providers.collectAsState().value[i].id in model.savedProviders) {
                            icon = Icons.Default.Favorite
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape = shape)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = model.providers.collectAsState().value[i].name,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(12.dp),
                            )
                            IconButton(
                                onClick = {
                                    if (model.providers.value[i].id !in model.savedProviders) {
                                        icon = Icons.Default.Favorite
                                        model.savedProviders += model.providers.value[i].id
                                    } else {
                                        icon = Icons.Default.FavoriteBorder
                                        model.savedProviders -= model.providers.value[i].id
                                    }
                                    model.fetchInfo()
                                }
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(
                        modifier = Modifier.size(86.dp),
                        polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun ProvidersPagePreview() {
    val model = viewModel<MoveModel>()
    model.setProviders(listOf(ProviderItem(), ProviderItem(), ProviderItem()))
    val backStack = rememberNavBackStack(MainView)
    ProvidersPage(model, backStack)
}