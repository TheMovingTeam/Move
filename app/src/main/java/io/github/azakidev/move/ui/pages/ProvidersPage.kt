package io.github.azakidev.move.ui.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.ProviderItem
import io.github.azakidev.move.listShape
import io.github.azakidev.move.ui.components.ProvidersList
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Timer
import kotlin.concurrent.schedule

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProvidersPage(
    model: MoveViewModel,
    backStack: NavBackStack<NavKey>,
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

    val onFavoriteClick = { i: Int, icon: MutableStateFlow<ImageVector> ->
        if (!model.savedProviders.value.contains(i)) {
            icon.value = Icons.Default.Favorite
            model.addSavedProvider(i)
        } else {
            icon.value = Icons.Default.FavoriteBorder
            model.removeSavedProvider(i)
        }
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = stringResource(R.string.providerTitle)
                    )
                },
                navigationIcon = {
                    IconButton(
                        shape = IconButtonDefaults.standardShape,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
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
                .padding(paddingValues),
        ) {
            ProvidersList(
                providerRepo = model.providerRepo.value,
                providers = model.providers.collectAsState().value,
                savedProviders = model.savedProviders.collectAsState().value,
                onFavoriteClick = onFavoriteClick,
                scrollBehavior = scrollBehavior
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun ProvidersPagePreview() {
    val providers = listOf(
        ProviderItem(
            name = "FictionalProvider",
            description = "This is a placeholder provider, if you see this it's probably in a preview"
        ),
        ProviderItem(
            name = "PossibleProvider",
        ),
        ProviderItem(
            name = "A provider that happens to have a really long name that's kinda silly"
        )
    )
    val savedProviders = emptyList<Int>()
    val onFavoriteClick = { i: Int, icon: MutableStateFlow<ImageVector> -> }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = stringResource(R.string.providerTitle)
                    )
                },
                navigationIcon = {
                    IconButton(
                        shape = IconButtonDefaults.standardShape,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = {}
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
                .padding(paddingValues),
        ) {
            ProvidersList(
                providerRepo = "",
                providers = providers,
                savedProviders = savedProviders,
                onFavoriteClick = onFavoriteClick,
                scrollBehavior = scrollBehavior
            )
        }
    }
}