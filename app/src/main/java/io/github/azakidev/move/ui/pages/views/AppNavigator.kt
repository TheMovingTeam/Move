package io.github.azakidev.move.ui.pages.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.window.core.layout.WindowSizeClass
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.data.items.toKey
import io.github.azakidev.move.ui.AppDestinations
import io.github.azakidev.move.ui.pages.panes.HomePage
import io.github.azakidev.move.ui.pages.panes.LinesPage
import io.github.azakidev.move.ui.pages.panes.MapPage
import io.github.azakidev.move.ui.pages.sheets.ChangelogPage
import io.github.azakidev.move.ui.pages.sheets.StopPage
import org.maplibre.compose.location.AndroidLocationProvider

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavigator(
    model: MoveViewModel,
    sheetState: SheetState,
    sheetModel: SheetStopViewModel,
    backStack: NavBackStack<NavKey>,
    currentDestination: MutableState<AppDestinations>,
    currentLocation: AndroidLocationProvider?
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val visibleDestinations =
        if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
            if (currentDestination.value == AppDestinations.LINES) {
                currentDestination.value = AppDestinations.HOME
            }
            AppDestinations.entries.filterNot { it == AppDestinations.LINES }
        } else {
            AppDestinations.entries
        }

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
                    selected = it == currentDestination.value,
                    onClick = { currentDestination.value = it }
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
            when (currentDestination.value) {
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

    if (model.shouldShowChangelog.collectAsState().value) {
        val nestedScroll = rememberNestedScrollInteropConnection()
        ModalBottomSheet(
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(nestedScroll),
            onDismissRequest = {
                model.toggleChangelog()
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            sheetState = sheetState,
            dragHandle = { },
        ) {
            ChangelogPage()
        }
    }
}