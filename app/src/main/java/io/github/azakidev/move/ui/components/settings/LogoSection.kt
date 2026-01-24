package io.github.azakidev.move.ui.components.settings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.common.LogoHero

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