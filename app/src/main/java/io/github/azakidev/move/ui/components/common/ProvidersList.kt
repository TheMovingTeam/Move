package io.github.azakidev.move.ui.components.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.data.items.ProviderGroup
import io.github.azakidev.move.data.items.ProviderItem
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.listShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProvidersList(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    providerRepo: String,
    providers: List<ProviderItem>,
    providerGroups: List<ProviderGroup>,
    savedProviders: List<Int>,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onFavoriteProviderClick: (Int) -> Unit = {},
    onFavoriteGroupClick: (ProviderGroup) -> Unit = {}
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
                            .consumeWindowInsets(paddingValues)
                            .padding(bottom = PADDING.div(2).dp)
                            .padding(horizontal = PADDING.div(2).dp),
                        verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp),
                        contentPadding = paddingValues
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
                                onClick = { onFavoriteProviderClick(provider.id) }
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
    val providerGroup = ProviderGroup(
        "Dummy group",
        listOf(
            "FictionalProvider",
            "PossibleProvider",
            "A provider that happens to have a really long name that's kinda silly"
        )
    )
    val savedProviders = emptyList<Int>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    ProvidersList(
        providerRepo = "",
        providers = providers,
        providerGroups = listOf(providerGroup),
        savedProviders = savedProviders,
        scrollBehavior = scrollBehavior,
        onFavoriteProviderClick = {},
        onFavoriteGroupClick = {}
    )
}