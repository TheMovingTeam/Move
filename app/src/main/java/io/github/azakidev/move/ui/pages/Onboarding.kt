package io.github.azakidev.move.ui.pages

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.listShape
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
                ProviderPage(model = model, onBack = {
                    backStack.removeLastOrNull()
                }, onEnd = {
                    model.saveOnboarding(true)
                })
            }
        },
    )

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun WelcomePage(
    onNext: () -> Unit = {}
) {
    val revealed = rememberSaveable { mutableStateOf(false) }
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

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth(),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(208.dp)
                        .clip(MaterialShapes.Cookie12Sided.toShape(shapeAngle.value.toInt()))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    colorResource(R.color.purple_brand),
                                    colorResource(R.color.purple_shadow)
                                )
                            )
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(128.dp),
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
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
fun FeaturePage(
    onBack: () -> Unit = {}, onNext: () -> Unit = {}
) {
    val entries = listOf(
        "Explaining goes here",
        "And here",
        "Explaining also goes here",
    )
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, start = 16.dp, end = 16.dp),
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
                    .height(228.dp),
                painter = painterResource(R.drawable.placeholderstop),
                contentScale = ContentScale.Crop,
                contentDescription = "Feature banner"
            )
            Text(
                modifier = Modifier.padding(12.dp),
                text = stringResource(R.string.whatIsMove),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            entries.forEach {
                ExplainingRow(
                    index = entries.indexOf(it) + 1,
                    shape = listShape(entries.indexOf(it), entries.count()),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    description = it
                )
            }
        }
    }
}

@Composable
@Preview
fun ExplainingRow(
    modifier: Modifier = Modifier,
    index: Int = 0,
    shape: Shape = MaterialTheme.shapes.small,
    description: String = "Explaining goes here"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.background
                    ), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = description, color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderPage(
    model: MoveViewModel, onBack: () -> Unit = {}, onEnd: () -> Unit = {}
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
    ProviderContent(
        onBack = onBack,
        onEnd = onEnd,
        providerCount = model.savedProviders.collectAsState().value.count(),
        providerList = {
            ProvidersList(
                providerRepo = model.providerRepo.value,
                providers = model.providers.collectAsState().value,
                savedProviders = model.savedProviders.collectAsState().value,
                onFavoriteClick = onFavoriteClick
            )
        })
}

@Composable
fun ProviderContent(
    onBack: () -> Unit = {},
    onEnd: () -> Unit = {},
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
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, start = 16.dp, end = 16.dp),
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(12.dp),
                text = stringResource(R.string.chooseProvider),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            providerList()
        }
    }
}

@Composable
@Preview
fun ProviderPagePreview() {
    ProviderContent(
        providerCount = 0
    ) {
        ProvidersListPreview()
    }
}