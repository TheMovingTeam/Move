package io.github.azakidev.move.ui.pages

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.ProviderItem
import io.github.azakidev.move.listShape
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
        if (model.providers.value[i].id !in model.savedProviders.value) {
            icon.value = Icons.Default.Favorite
            model.addSavedProvider(model.providers.value[i].id)
        } else {
            icon.value = Icons.Default.FavoriteBorder
            model.removeSavedProvider(model.providers.value[i].id)
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProvidersList(
    modifier: Modifier = Modifier,
    providerRepo: String,
    providers: List<ProviderItem>,
    savedProviders: List<Int>,
    onFavoriteClick: (Int, MutableStateFlow<ImageVector>) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) {
        if (providers.count() != 0) {
            LazyColumn(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(providers.count()) { i ->
                    val provider = providers[i]
                    val shape = listShape(i, providers.count())
                    val iconMut = remember { MutableStateFlow(Icons.Default.FavoriteBorder) }
                    var icon = iconMut.collectAsState().value
                    if (provider.id in savedProviders) {
                        icon = Icons.Default.Favorite
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape = shape)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val imgUrl =
                                    providerRepo + "/" + provider.name + "/res/provider.png"
                                AsyncImage(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(MaterialTheme.shapes.large),
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imgUrl)
                                        .crossfade(true)
                                        .build(),
                                    placeholder = painterResource(R.drawable.placeholderstop),
                                    error = painterResource(R.drawable.placeholderstop),
                                    contentScale = ContentScale.Crop,
                                    contentDescription = provider.name,
                                )
                                Text(
                                    text = provider.name,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            }
                            IconButton(
                                shape = CircleShape,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                onClick = { onFavoriteClick(i, iconMut) }
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null
                                )
                            }
                        }
                        if (provider.description.isNotEmpty()) {
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                text = provider.description,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable @Preview
fun ProvidersListPreview() {
    val providers = listOf(
        ProviderItem(
            description = "This is a placeholder provider, if you see this it's probably in a preview"
        ),
        ProviderItem(),
        ProviderItem()
    )
    val savedProviders = emptyList<Int>()
    val onFavoriteClick = { i: Int, icon: MutableStateFlow<ImageVector> -> }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    ProvidersList(
        providerRepo = "",
        providers = providers,
        savedProviders = savedProviders,
        onFavoriteClick = onFavoriteClick,
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun ProvidersPagePreview() {
    val providers = listOf(
        ProviderItem(
            description = "This is a placeholder provider, if you see this it's probably in a preview"
        ),
        ProviderItem(),
        ProviderItem()
    )
    val savedProviders = emptyList<Int>()
    val onFavoriteClick = { i: Int, icon: MutableStateFlow<ImageVector> -> }
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