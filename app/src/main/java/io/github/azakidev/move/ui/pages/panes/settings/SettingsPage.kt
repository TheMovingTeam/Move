package io.github.azakidev.move.ui.pages.panes.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices.PIXEL_FOLD
import androidx.compose.ui.tooling.preview.Devices.PIXEL_TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.window.core.layout.WindowSizeClass
import io.github.azakidev.move.ui.MainView
import io.github.azakidev.move.ui.Providers
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.settings.AboutSection
import io.github.azakidev.move.ui.components.settings.LogoSection
import io.github.azakidev.move.ui.components.settings.MapSection
import io.github.azakidev.move.ui.components.settings.ProviderSection
import io.github.azakidev.move.ui.components.settings.ResetSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsPage(
    providerRepo: StateFlow<String>,
    backStack: NavBackStack<NavKey>,
    onProviderReset: (String) -> Unit,
    isOnboardingComplete: Boolean,
    onAppReset: () -> Unit,
    onOnboardingReset: () -> Unit,
    onChangeLogShow: () -> Unit,
) {
    val state = rememberTextFieldState(
        initialText = providerRepo.collectAsState().value,
    )
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val gridCells =
        if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            StaggeredGridCells.Fixed(2)
        } else {
            StaggeredGridCells.Adaptive((WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND / 2).dp)
        }

    val scrollBehavior =
        if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            null
        } else {
            TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        }

    val modifier = if (scrollBehavior != null) {
        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    } else {
        Modifier
    }

    Scaffold(
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Text(
                        text = stringResource(R.string.settings)
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
        }
    ) { paddingValues ->
        LazyVerticalStaggeredGrid(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                )
                .consumeWindowInsets(paddingValues)
                .clip(shape = RoundedCornerShape(30.dp, 30.dp, 0.dp, 0.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
            columns = gridCells,
            verticalItemSpacing = PADDING.div(2).dp,
            contentPadding = PaddingValues(
                top = PADDING.div(4).dp,
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                bottom = paddingValues.calculateBottomPadding()
            )
        ) {
            item {
                LogoSection()
            }
            item {
                AboutSection(
                    onChangeLogShow = onChangeLogShow,
                    isOnboardingComplete = isOnboardingComplete
                )
            }
            item {
                ProviderSection(
                    state,
                    providerRepo,
                    onClick = onProviderReset,
                    onBack = {
                        backStack.add(Providers)
                    },
                    isOnboardingComplete
                )
            }
            if (isOnboardingComplete) {
                item {
                    MapSection(backStack = backStack)
                }

                item {
                    ResetSection(
                        onAppReset = onAppReset,
                        onOnboardingReset = onOnboardingReset
                    )
                }
            }
            item {
                Spacer(
                    Modifier.height(PADDING.div(2).dp)
                )
            }
        }
    }
}

@Composable
@Preview
@Preview(device = PIXEL_FOLD, showSystemUi = true)
@Preview(device = PIXEL_TABLET, showSystemUi = true)
fun SettingsPagePreview() {
    val providerRepo = MutableStateFlow("").asStateFlow()
    val backStack = rememberNavBackStack(MainView)
    SettingsPage(
        providerRepo = providerRepo,
        backStack = backStack,
        onProviderReset = {},
        isOnboardingComplete = true,
        onAppReset = {},
        onOnboardingReset = {},
        onChangeLogShow = {}
    )
}