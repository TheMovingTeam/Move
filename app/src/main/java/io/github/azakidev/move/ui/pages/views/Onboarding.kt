package io.github.azakidev.move.ui.pages.views

import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.Settings
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.ui.pages.panes.settings.SettingsPage
import io.github.azakidev.move.ui.pages.panes.onboarding.FeaturePage
import io.github.azakidev.move.ui.pages.panes.onboarding.ProviderPage
import io.github.azakidev.move.ui.pages.panes.onboarding.WelcomePage
import kotlinx.serialization.Serializable

@Serializable
internal data object WelcomePage : NavKey

@Serializable
internal data object FeaturePage : NavKey

@Serializable
internal data object ProviderPage : NavKey

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Onboarding(model: MoveViewModel) {
    val backStack = rememberNavBackStack(WelcomePage)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            // Slide in from right when navigating forward
            slideInHorizontally(
                animationSpec = MotionScheme.standard().defaultSpatialSpec()
            ) { it } togetherWith slideOutHorizontally(
                animationSpec = MotionScheme.standard().defaultSpatialSpec()
            ) { -it }
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(
                animationSpec = MotionScheme.standard().defaultSpatialSpec()
            ) { -it } togetherWith slideOutHorizontally(
                animationSpec = MotionScheme.standard().defaultSpatialSpec()
            ) { it }
        },
        predictivePopTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(
                animationSpec = tween(200, easing = EaseInCirc)
            ) { -it } togetherWith slideOutHorizontally(
                animationSpec = tween(200, easing = EaseInCirc), targetOffsetX = { it })

        },
        entryProvider = entryProvider {
            entry<WelcomePage> {
                WelcomePage(
                    onNext = {
                        backStack.add(FeaturePage)
                    })
            }
            entry<FeaturePage> {
                FeaturePage(onBack = {
                    backStack.removeLastOrNull()
                }, onNext = {
                    backStack.add(ProviderPage)
                })
            }
            entry<ProviderPage> {
                ProviderPage(
                    model = model,
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                    onEnd = {
                        model.saveOnboarding(true)
                    },
                    onSettings = {
                        backStack.add(Settings)
                    }
                )
            }
            entry<Settings> {
                val context = LocalContext.current
                val invalidText = stringResource(R.string.providerInvalid)
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
                    isOnboardingComplete = model.onboardingStatus.collectAsState().value,
                    onAppReset = {},
                    onOnboardingReset = {},
                    onChangeLogShow = {}
                )
            }
        },
    )

}