package io.github.azakidev.move.ui.pages

import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices.PIXEL_FOLD
import androidx.compose.ui.tooling.preview.Devices.PIXEL_TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import io.github.azakidev.move.R
import io.github.azakidev.move.Settings
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.listShape
import io.github.azakidev.move.ui.components.LogoHero
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import java.util.Timer
import kotlin.concurrent.schedule

@Serializable
internal data object WelcomePage : NavKey

@Serializable
internal data object FeaturePage : NavKey

@Serializable
internal data object ProviderPage : NavKey

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingPage(model: MoveViewModel) {
    val backStack = rememberNavBackStack(WelcomePage)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            // Slide in from right when navigating forward
            slideInHorizontally(
                animationSpec = tween(500)
            ) { it } togetherWith slideOutHorizontally(
                animationSpec = tween(500)
            ) { -it }
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(
                animationSpec = tween(500)
            ) { -it } togetherWith slideOutHorizontally(
                animationSpec = tween(500)
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
                    onboardingIsComplete = model.onboardingStatus.collectAsState().value,
                    onAppReset = {},
                    onOnboardingReset = {}
                )
            }
        },
    )

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomePage(
    initialRevealed: Boolean = false,
    onNext: () -> Unit
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val revealed = rememberSaveable { mutableStateOf(initialRevealed) }
    val blur by animateFloatAsState(
        targetValue = if (revealed.value) 0f else 20f,
        label = "Blur",
        animationSpec = MotionScheme.expressive().slowEffectsSpec()
    )

    val infiniteTransition = rememberInfiniteTransition(label = "moveCookieRotate")
    val shapeAngle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing), repeatMode = RepeatMode.Restart
        ),
    )

    Timer().schedule(delay = 1000, action = {
        revealed.value = true
    })

    val modifier =
        if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
            Modifier.width((windowSizeClass.minWidthDp / 1.75).dp)
        } else {
            Modifier.fillMaxWidth()
        }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val uriHandler = LocalUriHandler.current
                Button(
                    modifier = modifier
                        .height(48.dp),
                    onClick = {
                        uriHandler.openUri("https://themovingteam.github.io/privacy/")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.privacyPolicy),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Button(
                    modifier = modifier
                        .height(48.dp),
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.purple_brand),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.start), fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }) { paddingValues ->
        val scale =
            if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
                1.25f
            } else {
                1f
            }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(25.dp)
                .zIndex(-1f)
        ) {
            val size = 200f * scale
            val padding = 40
            val color = colorResource(R.color.purple_shadow)
            Box(
                modifier = Modifier
                    .padding(padding.dp)
                    .offset((-(size + padding) / 2).dp, (-(size + padding) / 2).dp)
                    .size(size.dp)
                    .scale(scale * 2f)
                    .aspectRatio(1f)
                    .clip(MaterialShapes.Cookie12Sided.toShape((-shapeAngle.value / 2).toInt()))
                    .background(
                        Color(color.red, color.green, color.blue, .8f)
                    )
                    .align(Alignment.TopStart)
            ) {}
            Box(
                modifier = Modifier
                    .padding(padding.dp)
                    .offset(((size + padding) / 2).dp, ((size + padding) / 2).dp)
                    .size(size.dp)
                    .scale(scale * 2f)
                    .aspectRatio(1f)
                    .clip(MaterialShapes.Cookie12Sided.toShape((-shapeAngle.value / 3).toInt()))
                    .background(
                        Color(color.red, color.green, color.blue, .4f)
                    )
                    .align(Alignment.BottomEnd)
            ) {}
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column( horizontalAlignment = Alignment.CenterHorizontally) {
                LogoHero(
                    size = (208 * scale).toInt(),
                    shapeAngle = shapeAngle.value.toInt()
                )
                AnimatedVisibility(
                    modifier = Modifier
                        .padding(top = 36.dp)
                        .blur(blur.dp),
                    visible = revealed.value,
                    enter = fadeIn(
                        animationSpec = MotionScheme.expressive().slowEffectsSpec(),
                        initialAlpha = 0.25f
                    ) + expandVertically(
                        animationSpec = MotionScheme.expressive().slowSpatialSpec()
                    ),
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.welcomeTo),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraLight,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Light,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun WelcomePagePreview() {
    WelcomePage(
        initialRevealed = true,
        onNext = {}
    )
}

@Composable
@Preview(device = PIXEL_FOLD, showSystemUi = true)
fun WelcomeMediumPagePreview() {
    WelcomePage(
        initialRevealed = true,
        onNext = {}
    )
}

@Composable
@Preview(device = PIXEL_TABLET, showSystemUi = true)
fun WelcomeWidePagePreview() {
    WelcomePage(
        initialRevealed = true,
        onNext = {}
    )
}

@Composable
fun FeaturePage(
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val entryTitles = stringArrayResource(R.array.onboardingTitles)
    val entryDescriptions = stringArrayResource(R.array.onboardingDescriptions)
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Image(
                modifier = Modifier.matchParentSize(),
                painter = painterResource(R.drawable.banner),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
            Scaffold(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 64.dp)
                    .width((windowSizeClass.minWidthDp / 2).dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .shadow(4.dp),
                bottomBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 48.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = onBack,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back"
                            )
                        }
                        IconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = onNext,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next"
                            )
                        }
                    }
                }) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 12.dp),
                        text = stringResource(R.string.whatIsMove),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(entryTitles.count()) { i ->
                            ExplainingRow(
                                index = i + 1,
                                shape = listShape(i, entryTitles.count()),
                                modifier = Modifier.padding(horizontal = 8.dp),
                                title = entryTitles[i],
                                description = entryDescriptions[i]
                            )
                        }
                    }
                }
            }
        }
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back"
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = onNext,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next"
                        )
                    }
                }
            }) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                        .clip(MaterialTheme.shapes.large)
                        .aspectRatio(16 / 9f),
                    painter = painterResource(R.drawable.banner),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(vertical = (12 - 4).dp),
                    text = stringResource(R.string.whatIsMove),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(entryTitles.count()) { i ->
                        ExplainingRow(
                            index = i + 1,
                            shape = listShape(i, entryTitles.count()),
                            modifier = Modifier.padding(horizontal = 8.dp),
                            title = entryTitles[i],
                            description = entryDescriptions[i]
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun FeaturePagePreview() {
    FeaturePage(
        onBack = {},
        onNext = {}
    )
}

@Composable
@Preview(device = PIXEL_FOLD, showSystemUi = true)
fun FeatureMediumPagePreview() {
    FeaturePage(
        onBack = {},
        onNext = {}
    )
}

@Composable
@Preview(device = PIXEL_TABLET, showSystemUi = true)
fun FeatureWidePagePreview() {
    FeaturePage(
        onBack = {},
        onNext = {}
    )
}

@Composable
fun ExplainingRow(
    modifier: Modifier = Modifier,
    index: Int = 0,
    shape: Shape = MaterialTheme.shapes.small,
    title: String,
    description: String
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.background
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun ExplainingRowPreview() {
    val title = "Explaining goes here"
    val description = "This is like, the description that actually does the explaining and stuff."
    ExplainingRow(
        title = title,
        description = description
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderPage(
    model: MoveViewModel,
    onBack: () -> Unit = {},
    onEnd: () -> Unit = {},
    onSettings: () -> Unit
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
        if (i !in model.savedProviders.value) {
            icon.value = Icons.Default.Favorite
            model.addSavedProvider(i)
        } else {
            icon.value = Icons.Default.FavoriteBorder
            model.removeSavedProvider(i)
        }
    }
    ProviderContent(
        onBack = onBack,
        onEnd = onEnd,
        onSettings = onSettings,
        providerCount = model.savedProviders.collectAsState().value.count(),
        providerList = {
            ProvidersList(
                providerRepo = model.providerRepo.value,
                providers = model.providers.collectAsState().value,
                savedProviders = model.savedProviders.collectAsState().value,
                onFavoriteClick = onFavoriteClick,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderContent(
    onBack: () -> Unit,
    onEnd: () -> Unit,
    onSettings: () -> Unit,
    providerCount: Int,
    providerList: @Composable (() -> Unit)
) {
    val color = if (providerCount > 0) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val iconColor = if (providerCount > 0) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Image(
                modifier = Modifier.matchParentSize(),
                painter = painterResource(R.drawable.banner),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
            Scaffold(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 64.dp)
                    .width((windowSizeClass.minWidthDp / 2).dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .shadow(4.dp),
                topBar = {
                    TopAppBar(
                        scrollBehavior = null,
                        title = {
                            Text(
                                text = stringResource(R.string.chooseProvider),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = onSettings
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = stringResource(R.string.settings)
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 48.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = onBack,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                        IconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = onEnd,
                            enabled = providerCount > 0,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = color, contentColor = iconColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check, contentDescription = "Done"
                            )
                        }
                    }
                }) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    providerList()
                }
            }
        }
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            topBar = {
                TopAppBar(
                    scrollBehavior = null,
                    title = {
                        Text(
                            text = stringResource(R.string.chooseProvider),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = onSettings
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = stringResource(R.string.settings)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = onEnd,
                        enabled = providerCount > 0,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = color, contentColor = iconColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check, contentDescription = "Done"
                        )
                    }
                }
            }) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                providerList()
            }
        }
    }
}

@Composable
@Preview
fun ProviderPagePreview() {
    ProviderContent(
        providerCount = 0,
        onBack = {},
        onEnd = {},
        onSettings = {},
    ) {
        ProvidersListPreview()
    }
}

@Composable
@Preview(device = PIXEL_FOLD, showSystemUi = true)
fun ProviderMediumPagePreview() {
    ProviderContent(
        providerCount = 0,
        onBack = {},
        onEnd = {},
        onSettings = {},
    ) {
        ProvidersListPreview()
    }
}

@Composable
@Preview(device = PIXEL_TABLET, showSystemUi = true)
fun ProviderWidePagePreview() {
    ProviderContent(
        providerCount = 0,
        onBack = {},
        onEnd = {},
        onSettings = {},
    ) {
        ProvidersListPreview()
    }
}