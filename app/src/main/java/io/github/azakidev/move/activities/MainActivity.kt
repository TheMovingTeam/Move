package io.github.azakidev.move.activities

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.items.toKey
import io.github.azakidev.move.widget.FavStopWidgetReceiver
import io.github.azakidev.move.ui.MainView
import io.github.azakidev.move.ui.Providers
import io.github.azakidev.move.ui.QrScanner
import io.github.azakidev.move.ui.Settings
import io.github.azakidev.move.ui.pages.ChangelogPage
import io.github.azakidev.move.ui.pages.HomePage
import io.github.azakidev.move.ui.pages.LargeScreenHome
import io.github.azakidev.move.ui.pages.LinesPage
import io.github.azakidev.move.ui.pages.MapPage
import io.github.azakidev.move.ui.pages.OnboardingPage
import io.github.azakidev.move.ui.pages.ProvidersPage
import io.github.azakidev.move.ui.pages.QrPage
import io.github.azakidev.move.ui.pages.SettingsPage
import io.github.azakidev.move.ui.pages.StopPage
import io.github.azakidev.move.ui.theme.MoveTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.maplibre.compose.location.AndroidLocationProvider
import org.maplibre.compose.location.DesiredAccuracy
import org.maplibre.compose.location.rememberAndroidLocationProvider
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        // Widget preview setup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            lifecycleScope.launch(Dispatchers.Default) {
                GlanceAppWidgetManager(this@MainActivity)
                    .setWidgetPreviews(FavStopWidgetReceiver::class)
            }
        }

        setContent {
            val model = viewModel<MoveViewModel>()
            model.fetchProviders()
            model.fetchInfoForProviders(model.savedProviders.collectAsState().value)

            val sheetState = rememberModalBottomSheetState()
            val sheetModel = viewModel<SheetStopViewModel>()

            val blur by animateFloatAsState(
                targetValue = if (sheetModel.showBottomSheet || model.shouldShowChangelog.value) 20f else 0f,
                label = "Blur",
                animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
            )

            val locationFinePermissionState = rememberPermissionState(
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            var currentLocation: AndroidLocationProvider? = null

            if (locationFinePermissionState.status.isGranted) {
                val location = rememberAndroidLocationProvider(
                    1.seconds,
                    DesiredAccuracy.Balanced,
                    minDistanceMeters = 50f,
                )
                currentLocation = location
            }

            MoveTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AnimatedContent(model.onboardingStatus.collectAsState().value) { state ->
                        when (state) {
                            true -> {
                                val backStack = rememberNavBackStack(MainView)
                                val context = LocalContext.current

                                NavDisplay(
                                    modifier = Modifier
                                        .blur(blur.dp),
                                    backStack = backStack,
                                    onBack = { backStack.removeLastOrNull() },
                                    transitionSpec = {
                                        // Slide in from right when navigating forward
                                        slideInHorizontally(initialOffsetX = { it }) togetherWith scaleOut(
                                            targetScale = 0.9f,
                                            transformOrigin = TransformOrigin(0f, 0.5f),
                                            animationSpec = MotionScheme.standard()
                                                .defaultSpatialSpec()
                                        )
                                    },
                                    popTransitionSpec = {
                                        // Slide in from left when navigating back
                                        scaleIn(
                                            initialScale = 0.9f,
                                            transformOrigin = TransformOrigin(0f, 0.5f),
                                            animationSpec = MotionScheme.standard()
                                                .defaultSpatialSpec()
                                        ) togetherWith slideOutHorizontally(
                                            animationSpec = MotionScheme.standard()
                                                .defaultSpatialSpec()
                                        ) { it }
                                    },
                                    predictivePopTransitionSpec = {
                                        // Slide in from left when navigating back
                                        scaleIn(
                                            initialScale = 0.9f,
                                            transformOrigin = TransformOrigin(0f, 0.5f),
                                            animationSpec = tween(200, easing = EaseInCirc)
                                        ) + fadeIn(
                                            initialAlpha = 0.2f,
                                            animationSpec = tween(200, easing = EaseInCirc)
                                        ) togetherWith scaleOut(
                                            targetScale = 0.9f,
                                            transformOrigin = TransformOrigin(0f, 0.5f),
                                            animationSpec = tween(200, easing = EaseInCirc)
                                        ) + slideOutHorizontally(
                                            animationSpec = tween(200, easing = EaseInCirc),
                                            targetOffsetX = { it }
                                        )

                                    },
                                    entryProvider = entryProvider {
                                        entry<MainView> {
                                            AppNavigator(
                                                model,
                                                sheetState,
                                                sheetModel,
                                                backStack,
                                                currentLocation
                                            )
                                        }
                                        entry<Providers> {
                                            ProvidersPage(model, backStack)
                                        }
                                        entry<Settings> {
                                            val invalidText =
                                                stringResource(R.string.providerInvalid)
                                            SettingsPage(
                                                model.providerRepo,
                                                backStack,
                                                onProviderReset = { url ->
                                                    if (URLUtil.isValidUrl(url) && model.tryRepo(url)) {
                                                        model.saveRepo(url)
                                                        model.flushInfo()
                                                    } else {
                                                        Toast
                                                            .makeText(
                                                                context,
                                                                invalidText,
                                                                Toast.LENGTH_SHORT
                                                            )
                                                            .show()
                                                    }
                                                },
                                                onboardingIsComplete = model.onboardingStatus.collectAsState().value,
                                                onAppReset = {
                                                    model.flushInfo()
                                                    model.saveOnboarding(false)
                                                },
                                                onOnboardingReset = {
                                                    model.saveOnboarding(false)
                                                },
                                                onChangeLogShow = {
                                                    backStack.removeLastOrNull()
                                                    model.shouldShowChangelog.value = true
                                                },
                                            )
                                        }
                                        entry<QrScanner> {
                                            QrPage(model, sheetModel, backStack)
                                        }
                                    },
                                )
                            }

                            false -> {
                                model.shouldShowChangelog.value = false
                                OnboardingPage(model)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class AppDestinations(
    @param:StringRes val label: Int,
    val icon: ImageVector,
    @param:StringRes val contentDescription: Int
) {
    HOME(R.string.home, Icons.Rounded.Home, R.string.home),
    LINES(R.string.lines, Icons.AutoMirrored.Rounded.ArrowForward, R.string.lines),
    MAP(R.string.map, Icons.Rounded.LocationOn, R.string.map)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavigator(
    model: MoveViewModel,
    sheetState: SheetState,
    sheetModel: SheetStopViewModel,
    backStack: NavBackStack<NavKey>,
    currentLocation: AndroidLocationProvider?
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    //TODO: Remove check when Map page is finished
    val presentDestinations =
        if (BuildConfig.DEBUG) AppDestinations.entries
        else AppDestinations.entries.filterNot { it == AppDestinations.MAP }

    val visibleDestinations =
        if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
            presentDestinations.filterNot { it == AppDestinations.LINES }
        } else {
            presentDestinations
        }

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    //TODO: Remove check when Map page is finished
    if (
        visibleDestinations.count() == 1 &&
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
        ) {
        LargeScreenHome(model, sheetModel, backStack)
    } else {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                visibleDestinations.forEach {
                    item(
                        icon = {
                            Icon(
                                imageVector = it.icon,
                                contentDescription = stringResource(it.contentDescription)
                            )
                        },
                        label = { Text(stringResource(it.label)) },
                        selected = it == currentDestination,
                        onClick = { currentDestination = it }
                    )
                }
            }
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    fadeIn(
                        animationSpec = MotionScheme.expressive().defaultEffectsSpec()
                    ) togetherWith fadeOut(
                        animationSpec = MotionScheme.expressive().defaultEffectsSpec()
                    )
                }
            ) { currentDestination ->
                when (currentDestination) {
                    AppDestinations.HOME -> {
                        if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
                            LargeScreenHome(model, sheetModel, backStack)
                        } else {
                            HomePage(
                                modifier = Modifier.padding(bottom = 0.dp),
                                model = model,
                                sheetModel = sheetModel,
                                backStack = backStack
                            )
                        }
                    }

                    AppDestinations.LINES -> LinesPage(
                        modifier = Modifier.padding(bottom = 0.dp),
                        model = model,
                        sheetModel = sheetModel
                    )

                    AppDestinations.MAP -> MapPage(
                        model = model,
                        sheetModel = sheetModel,
                        currentLocation = currentLocation
                    )
                }
            }
        }
    }

    if (sheetModel.showBottomSheet) {
        val nestedScroll = rememberNestedScrollInteropConnection()
        ModalBottomSheet(
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(nestedScroll),
            onDismissRequest = {
                sheetModel.showBottomSheet = false
                model.removeToFetchLoop(sheetModel.sheetStop.toKey())
            },
            containerColor = MaterialTheme.colorScheme.background,
            sheetState = sheetState,
            dragHandle = { },
        ) {
            StopPage(
                model = model,
                sheetModel = sheetModel,
                currentLocation = currentLocation
            )
        }
    }

    if (model.shouldShowChangelog.value) {
        val nestedScroll = rememberNestedScrollInteropConnection()
        ModalBottomSheet(
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(nestedScroll),
            onDismissRequest = {
                model.shouldShowChangelog.value = !model.shouldShowChangelog.value
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            sheetState = sheetState,
            dragHandle = { },
        ) {
            ChangelogPage()
        }
    }
}