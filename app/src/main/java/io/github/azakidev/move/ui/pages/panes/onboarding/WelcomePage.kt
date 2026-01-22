package io.github.azakidev.move.ui.pages.panes.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.window.core.layout.WindowSizeClass
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.common.LogoHero
import java.util.Timer
import kotlin.concurrent.schedule

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

    val fredokaFontFamily = FontFamily(
        Font(R.font.fredoka_bold, FontWeight.Bold)
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = PADDING.times(3).dp)
                    .padding(horizontal = PADDING.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
            ) {
                val uriHandler = LocalUriHandler.current
                Button(
                    modifier = modifier
                        .height(48.dp),
                    onClick = {
                        uriHandler.openUri("https://movetransit.app/privacy/")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PADDING.times(0.75).dp),
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
                        horizontalArrangement = Arrangement.spacedBy(PADDING.times(0.75).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.start),
                            fontWeight = FontWeight.SemiBold
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
        Box( // Backdrop
            modifier = Modifier
                .fillMaxSize()
                .blur(25.dp)
                .zIndex(-1f)
        ) {
            val size = 200f * scale
            val padding = 40

            val colorLight = colorResource(R.color.purple_brand)
            val colorShadow = colorResource(R.color.purple_shadow)

            Box(
                modifier = Modifier
                    .padding(padding.dp)
                    .offset((-(size + padding) / 2).dp, (-(size + padding) / 2).dp)
                    .size(size.dp)
                    .scale(scale * 2f)
                    .aspectRatio(1f)
                    .clip(MaterialShapes.Cookie12Sided.toShape((-shapeAngle.value / 2).toInt()))
                    .background(
                        Color(colorShadow.red, colorShadow.green, colorShadow.blue, .8f)
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
                        Color(colorShadow.red, colorShadow.green, colorShadow.blue, .4f)
                    )
                    .align(Alignment.BottomEnd)
            ) {}
            Box(
                modifier = Modifier
                    .padding(padding.dp)
                    .offset((-(size + padding) / 2).dp, ((size + padding) / 2).dp)
                    .size(size.dp)
                    .scale(scale * 1.5f)
                    .aspectRatio(1f)
                    .blur(40.dp)
                    .clip(CircleShape)
                    .background(
                        Color(colorLight.red, colorLight.green, colorLight.blue, .2f)
                    )
                    .align(Alignment.BottomStart)
            ) {}
            Box(
                modifier = Modifier
                    .padding(padding.dp)
                    .offset(((size + padding) / 2).dp, (-(size + padding) / 1.8).dp)
                    .size(size.dp)
                    .scale(scale * 2.5f)
                    .aspectRatio(1f)
                    .blur(40.dp)
                    .clip(CircleShape)
                    .background(
                        Color(colorLight.red, colorLight.green, colorLight.blue, .3f)
                    )
                    .align(Alignment.TopEnd)
            ) {}
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                        modifier = Modifier.Companion,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.welcomeTo),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraLight,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fredokaFontFamily,
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
@Preview(device = Devices.PIXEL_FOLD, showSystemUi = true)
@Preview(device = Devices.PIXEL_TABLET, showSystemUi = true)
fun WelcomePagePreview() {
    WelcomePage(
        initialRevealed = true,
        onNext = {}
    )
}