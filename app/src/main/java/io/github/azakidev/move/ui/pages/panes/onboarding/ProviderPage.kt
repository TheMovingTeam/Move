package io.github.azakidev.move.ui.pages.panes.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.common.ProvidersList
import io.github.azakidev.move.ui.components.common.ProvidersListPreview
import java.util.Timer
import kotlin.concurrent.schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderPage(
    model: MoveViewModel,
    onBack: () -> Unit = {},
    onEnd: () -> Unit = {},
    onSettings: () -> Unit
) {
    var shouldLoad = model.providers.collectAsState().value.count() == 0

    val timer = Timer().schedule(delay = 1000, period = 5000, action = {
        if (model.providers.value.count() == 0) {
            model.fetchProviders()
        } else {
            Timer().schedule(delay = 500, action = {
                shouldLoad = false
            })
        }
    })

    if (shouldLoad) {
        timer.run()
    } else {
        timer.cancel()
    }

    ProviderContent(
        onBack = onBack,
        onEnd = onEnd,
        onSettings = onSettings,
        providerCount = model.savedProviders.collectAsState().value.count(),
        providerList = {
            ProvidersList(
                providerRepo = model.providerRepo.collectAsState().value,
                providers = model.providers.collectAsState().value,
                savedProviders = model.savedProviders.collectAsState().value,
                onFavoriteClick = {
                    if (it !in model.savedProviders.value) {
                        model.addSavedProvider(it)
                    } else {
                        model.removeSavedProvider(it)
                    }
                },
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProviderContent(
    onBack: () -> Unit,
    onEnd: () -> Unit,
    onSettings: () -> Unit,
    providerCount: Int,
    providerList: @Composable (() -> Unit)
) {
    val color = if (providerCount > 0) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val iconColor = if (providerCount > 0) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.4f)
                    .blur(60.dp),
                painter = painterResource(R.drawable.banner),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
            Scaffold(
                modifier = Modifier
                    .padding(horizontal = PADDING.dp)
                    .padding(top = PADDING.times(4).dp)
                    .width((windowSizeClass.minWidthDp / 2).dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.background),
                topBar = {
                    TopAppBar(
                        scrollBehavior = null,
                        title = {
                            Text(
                                text = stringResource(R.string.providerTitle),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        subtitle = {
                            Text(
                                text = stringResource(R.string.chooseProvider),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = onSettings
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = stringResource(R.string.settings)
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = PADDING.times(3).dp, top = PADDING.div(2).dp)
                            .padding(horizontal = PADDING.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = onBack,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                        IconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = onEnd,
                            enabled = providerCount > 0,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = color, contentColor = iconColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check, contentDescription = "Done"
                            )
                        }
                    }
                }) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    providerList()
                }
            }
        }
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            topBar = {
                TopAppBar(
                    scrollBehavior = null,
                    title = {
                        Text(
                            text = stringResource(R.string.providerTitle),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    subtitle = {
                        Text(
                            text = stringResource(R.string.chooseProvider),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = onSettings
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = stringResource(R.string.settings)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = PADDING.times(3).dp, top = PADDING.div(2).dp)
                        .padding(horizontal = PADDING.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = onEnd,
                        enabled = providerCount > 0,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = color, contentColor = iconColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check, contentDescription = "Done"
                        )
                    }
                }
            }) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                providerList()
            }
        }
    }
}

@Composable
@Preview
@Preview(device = Devices.PIXEL_FOLD, showSystemUi = true)
@Preview(device = Devices.PIXEL_TABLET, showSystemUi = true)
fun ProviderPagePreview() {
    ProviderContent(
        providerCount = 0,
        onBack = {},
        onEnd = {},
        onSettings = {},
    ) {
        ProvidersListPreview()
    }
}