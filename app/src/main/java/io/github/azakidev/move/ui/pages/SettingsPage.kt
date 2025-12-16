package io.github.azakidev.move.ui.pages

import androidx.annotation.StringRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.AddRoad
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices.PIXEL_FOLD
import androidx.compose.ui.tooling.preview.Devices.PIXEL_TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.window.core.layout.WindowSizeClass
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.ui.MainView
import io.github.azakidev.move.ui.Providers
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.listShape
import io.github.azakidev.move.ui.components.LogoHero
import io.github.azakidev.move.ui.components.RowButton
import io.github.azakidev.move.ui.components.trailingButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsPage(
    providerRepo: MutableState<String>,
    backStack: NavBackStack<NavKey>,
    onProviderReset: (String) -> Unit,
    onboardingIsComplete: Boolean,
    onAppReset: () -> Unit,
    onOnboardingReset: () -> Unit,
    onChangeLogShow: () -> Unit,
) {
    val state = rememberTextFieldState(
        initialText = providerRepo.value,
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
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = 0.dp
                )
                .clip(shape = RoundedCornerShape(30.dp, 30.dp, 0.dp, 0.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
            columns = gridCells,
            verticalItemSpacing = PADDING.div(2).dp
        ) {
            item {
                LogoSection()
            }
            item {
                AboutSection(
                    onChangeLogShow = onChangeLogShow
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
                    onboardingIsComplete
                )
            }
            if (onboardingIsComplete) {
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
    val providerRepo = remember { mutableStateOf("") }
    val backStack = rememberNavBackStack(MainView)
    SettingsPage(
        providerRepo = providerRepo,
        backStack = backStack,
        onProviderReset = {},
        onboardingIsComplete = true,
        onAppReset = {},
        onOnboardingReset = {},
        onChangeLogShow = {}
    )
}

@Composable
fun LogoSection(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "moveCookieRotate")
    val shapeAngle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing), repeatMode = RepeatMode.Restart
        ),
    )
    Column(
        modifier = modifier
            .padding(top = PADDING.times(0.75).dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
    ) {
        LogoHero(
            size = 128,
            shapeAngle = shapeAngle.value.toInt()
        )
        val appName =
            if (BuildConfig.DEBUG) stringResource(R.string.app_name) + " " + BuildConfig.VERSION_NAME + "_BETA"
            else stringResource(R.string.app_name) + " " + BuildConfig.VERSION_NAME

        val fredokaFontFamily = FontFamily(
            Font(R.font.fredoka_medium, FontWeight.Medium),
            Font(R.font.fredoka_bold, FontWeight.Bold)
        )

        Text(
            modifier = Modifier.padding(top = PADDING.div(2).dp),
            text = appName,
            fontFamily = fredokaFontFamily,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

data class AboutElement(
    val icon: ImageVector,
    @param:StringRes val description: Int,
    val link: String
)

@Composable
fun AboutSection(
    modifier: Modifier = Modifier,
    onChangeLogShow: () -> Unit
) {
    val elements = listOf(
        AboutElement(
            icon = Icons.Rounded.BugReport,
            description = R.string.bugReport,
            link = "mailto:support@movetransit.app"
        ),
        AboutElement(
            icon = Icons.Rounded.AlternateEmail,
            description = R.string.socialMedia,
            link = "https://twitter.com/movetransit"
        ),
        AboutElement(
            icon = Icons.Rounded.Info,
            description = R.string.privacyPolicy,
            link = "https://movetransit.app/privacy/"
        ),
    )

    val uriHandler = LocalUriHandler.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = PADDING.dp, vertical = PADDING.div(2).dp),
            text = stringResource(R.string.about),
            style = MaterialTheme.typography.titleMedium
        )
        elements.forEach {
            RowButton(
                shape = listShape(elements.indexOf(it), elements.count() + 1, 24.dp, 4.dp),
                icon = it.icon,
                description = stringResource(it.description),
                onClick = {
                    uriHandler.openUri(it.link)
                }
            )
        }
        RowButton(
            shape = listShape(elements.count(), elements.count() + 1, 24.dp, 4.dp),
            icon = Icons.Rounded.NewReleases,
            description = stringResource(R.string.showChangeLog),
            onClick = onChangeLogShow
        )
    }
}

@Composable
fun ResetSection(
    onAppReset: () -> Unit,
    onOnboardingReset: () -> Unit
) {
    Column(
        modifier = Modifier.padding(bottom = PADDING.div(2).dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = PADDING.dp),
            text = stringResource(R.string.reset),
            style = MaterialTheme.typography.titleMedium
        )
        val totalEntries = 2
        val shape = if (BuildConfig.DEBUG) listShape(0, totalEntries, 24.dp, 4.dp)
        else listShape(1, 1, 24.dp, 4.dp)
        RowButton(
            shape = shape,
            icon = Icons.Rounded.Delete,
            color = MaterialTheme.colorScheme.errorContainer,
            iconColor = MaterialTheme.colorScheme.onErrorContainer,
            description = stringResource(R.string.resetDesc),
            onClick = onAppReset
        )
        if (BuildConfig.DEBUG) {
            RowButton(
                shape = listShape(1, totalEntries, 24.dp, 4.dp),
                icon = Icons.Rounded.BugReport,
                color = MaterialTheme.colorScheme.errorContainer,
                iconColor = MaterialTheme.colorScheme.onErrorContainer,
                description = stringResource(R.string.resetOnboarding),
                onClick = onOnboardingReset
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProviderSection(
    state: TextFieldState,
    providerRepo: MutableState<String>,
    onClick: (String) -> Unit,
    onBack: () -> Unit,
    onboardingIsComplete: Boolean
) {

    val trailingIcon = trailingButton(
        textState = state.text.toString(),
        defaultText = providerRepo.value,
        icon = Icons.Default.Save,
        onClick = {
            onClick(state.text.toString())
        }
    )

    Text(
        modifier = Modifier.padding(horizontal = PADDING.dp, vertical = PADDING.div(2).dp),
        text = stringResource(R.string.providerTitle),
        style = MaterialTheme.typography.titleMedium
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = PADDING.div(2).dp)
                .clip(shape = listShape(0, 2, 24.dp, 4.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.padding(start = 14.dp, top = 2.dp),
                    text = stringResource(R.string.providerSource),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = {
                        state.edit {
                            replace(
                                0,
                                state.text.length,
                                "https://raw.githubusercontent.com/TheMovingTeam/Providers/refs/heads/main"
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restore,
                        contentDescription = null
                    )
                }
            }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PADDING.div(2).dp),
                state = state,
                shape = MaterialTheme.shapes.large,
                lineLimits = TextFieldLineLimits.SingleLine,
                placeholder = {
                    Text(
                        text = stringResource(R.string.providerSource)
                    )
                },
                trailingIcon = trailingIcon
            )
        }
        if (onboardingIsComplete) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PADDING.div(2).dp)
                    .clip(shape = listShape(1, 2, 24.dp, 4.dp))
                    .clickable(
                        onClick = onBack
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Row(
                    modifier = Modifier
                        .padding(
                            horizontal = PADDING.div(2).dp,
                            vertical = PADDING.times(0.75).dp
                        )
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PADDING.div(2).dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryFixedDim)
                            .padding(PADDING.div(2).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddRoad,
                            contentDescription = stringResource(R.string.providerTitle),
                            tint = MaterialTheme.colorScheme.onSecondaryFixed
                        )
                    }
                    Text(
                        text = stringResource(R.string.savedProviders)
                    )
                }
            }
        }
    }
}
