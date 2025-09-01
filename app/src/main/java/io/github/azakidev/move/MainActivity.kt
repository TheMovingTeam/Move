package io.github.azakidev.move

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.azakidev.move.data.MoveModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.ui.pages.HomePage
import io.github.azakidev.move.ui.pages.LinesPage
import io.github.azakidev.move.ui.pages.MapPage
import io.github.azakidev.move.ui.pages.ProvidersPage
import io.github.azakidev.move.ui.pages.SettingsPage
import io.github.azakidev.move.ui.pages.StopPage
import io.github.azakidev.move.ui.theme.MoveTheme
import java.util.Timer
import kotlin.concurrent.schedule

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val model = viewModel<MoveModel>()
            model.fetchProviders()
            val timer = Timer().schedule(delay = 1000, action = {
                model.fetchInfo()
            })
            timer.run()

            val backStack = rememberNavBackStack(MainView)
            MoveTheme {
                Surface(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                ) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        transitionSpec = {
                            // Slide in from right when navigating forward
                            slideInHorizontally(initialOffsetX = { it }) togetherWith scaleOut(
                                targetScale = 0.9f,
                                transformOrigin = TransformOrigin(0f, 0.5f),
                                animationSpec = MotionScheme.standard().defaultSpatialSpec()
                            )
                        },
                        popTransitionSpec = {
                            // Slide in from left when navigating back
                            scaleIn(
                                initialScale = 0.9f,
                                transformOrigin = TransformOrigin(0f, 0.5f),
                                animationSpec = tween(500)
                            ) togetherWith slideOutHorizontally(
                                animationSpec = tween(500)
                            ) { it }
                        },
                        predictivePopTransitionSpec = {
                            // Slide in from left when navigating back
                            scaleIn(
                                initialScale = 0.9f,
                                transformOrigin = TransformOrigin(0f, 0.5f),
                                animationSpec = tween(200)
                            ) togetherWith scaleOut(
                                targetScale = 0.9f,
                                transformOrigin = TransformOrigin(0f, 0.5f),
                                animationSpec = tween(200)
                            ) + slideOutHorizontally(
                                animationSpec = tween(200, easing = EaseInCirc),
                                targetOffsetX = { it }
                            )

                        },
                        entryProvider = entryProvider {
                            entry<MainView> {
                                AppNavigator(model, backStack)
                            }
                            entry<Providers> {
                                ProvidersPage(model, backStack)
                            }
                            entry<Settings> {
                                SettingsPage(model, backStack)
                            }
                            entry<QrScanner> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "Under construction",
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

enum class AppDestinations(
    @StringRes val label: Int, val icon: ImageVector, @StringRes val contentDescription: Int
) {
    HOME(R.string.home, Icons.Default.Home, R.string.home),
    LINES(R.string.lines, Icons.AutoMirrored.Filled.ArrowForward, R.string.lines),
//    MAP(R.string.map, Icons.Default.LocationOn, R.string.map)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigator(
    model: MoveModel, backStack: NavBackStack
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    
    val sheetState = rememberModalBottomSheetState()
    val sheetModel = viewModel<SheetStopViewModel>()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = stringResource(it.contentDescription)
                        )
                    },
                    label = { Text(stringResource(it.label)) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it })
            }
        }) {
        when (currentDestination) {
            AppDestinations.HOME -> HomePage(model, sheetModel, backStack)
            AppDestinations.LINES -> LinesPage(model, sheetModel)
//            AppDestinations.MAP -> MapPage()
        }
    }

    if (sheetModel.showBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxHeight(),
            onDismissRequest = { sheetModel.showBottomSheet = false },
            sheetState = sheetState,
            dragHandle = { },
        ) {
        StopPage(model = model, sheetModel = sheetModel)
        }
    }
}

@Composable
@Preview
fun AppNavigatorPreview() {
    val model = viewModel<MoveModel>()
    val backStack = rememberNavBackStack(MainView)
    AppNavigator(model, backStack)
}