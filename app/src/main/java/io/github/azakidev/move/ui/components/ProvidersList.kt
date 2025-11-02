package io.github.azakidev.move.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.R
import io.github.azakidev.move.data.ProviderItem
import io.github.azakidev.move.listShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProvidersList(
    modifier: Modifier = Modifier,
    providerRepo: String,
    providers: List<ProviderItem>,
    savedProviders: List<Int>,
    onFavoriteClick: (Int) -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        val visibleProviders = if (!BuildConfig.APPLICATION_ID.contains("debug")) {
            providers.filterNot { it.name.contains("Dummy") }
        } else {
            providers
        }
        AnimatedContent(visibleProviders.count()) { count ->
            when (count) {
                0 -> {
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

                else -> {
                    val modifier =
                        if (scrollBehavior != null) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier
                    LazyColumn(
                        modifier = modifier
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(visibleProviders.count()) { i ->
                            val provider = visibleProviders.sortedBy { it.name }[i]
                            val shape = listShape(i, providers.count())

                            ProviderEntry(
                                modifier = Modifier
                                    .animateItem(
                                        fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                                        placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                                        fadeOutSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
                                    ),
                                shape = shape,
                                provider = provider,
                                savedProviders = savedProviders,
                                providerRepo = providerRepo,
                                onClick = { onFavoriteClick(provider.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun ProvidersListPreview() {
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    ProvidersList(
        providerRepo = "",
        providers = providers,
        savedProviders = savedProviders,
        onFavoriteClick = {},
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProviderEntry(
    modifier: Modifier = Modifier,
    provider: ProviderItem,
    shape: Shape,
    providerRepo: String,
    onClick: () -> Unit,
    savedProviders: List<Int>,
) {
    Column(
        modifier = modifier
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
                    placeholder = painterResource(R.drawable.placeholder_provider),
                    error = painterResource(R.drawable.placeholder_provider),
                    contentScale = ContentScale.Crop,
                    contentDescription = provider.name,
                )
                Text(
                    modifier = Modifier.fillMaxWidth(.8f),
                    text = provider.name,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            IconButton(
                shape = CircleShape,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                onClick = onClick
            ) {
                AnimatedContent(
                    targetState = provider.id in savedProviders,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = MotionScheme.expressive().fastEffectsSpec()
                        ) + scaleIn(
                            animationSpec = MotionScheme.expressive().fastSpatialSpec()
                        ) togetherWith fadeOut(
                            animationSpec = MotionScheme.expressive().fastEffectsSpec()
                        ) + scaleOut(
                            animationSpec = MotionScheme.expressive().fastSpatialSpec()
                        )
                    }
                ) { state ->
                    when (state) {
                        true -> {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = "Save provider"
                            )
                        }

                        false -> {
                            Icon(
                                imageVector = Icons.Rounded.FavoriteBorder,
                                contentDescription = "Save provider"
                            )
                        }
                    }
                }
            }
        }
        if (provider.description.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 8.dp),
                text = provider.description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}