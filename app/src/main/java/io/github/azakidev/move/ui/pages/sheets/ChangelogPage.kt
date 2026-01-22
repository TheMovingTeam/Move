@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package io.github.azakidev.move.ui.pages.sheets

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.listShape
import io.github.azakidev.move.ui.components.common.LogoHero
import io.github.azakidev.move.ui.components.common.RowButton

@Composable
@Preview
fun ChangelogPage() {
    val infiniteTransition = rememberInfiniteTransition(label = "moveCookieRotate")
    val shapeAngle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing), repeatMode = RepeatMode.Restart
        ),
    )

    val changelogEntry = stringArrayResource(R.array.changelogItems)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PADDING.div(2).dp)
            .padding(bottom = PADDING.div(2).dp),
        verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-12).dp)
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ) {
            BottomSheetDefaults.DragHandle(
                color = Color.White
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LogoHero(
                size = 108,
                shapeAngle = shapeAngle.value.toInt()
            )
            val appVersion =
                if (BuildConfig.DEBUG) BuildConfig.VERSION_NAME + "_BETA"
                else BuildConfig.VERSION_NAME
            val titleString = stringResource(R.string.changelogVersion)
                .replace("{app}", stringResource(R.string.app_name))
                .replace("{ver}", appVersion)
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = titleString,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = stringResource(R.string.changelogFlavor),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        var count = 0
        changelogEntry.forEach {
            val isBreaking = it.contains("[BREAKING]")

            val color =
                if (isBreaking) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceContainer

            val badgeColor =
                if (isBreaking) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.secondaryFixedDim

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(horizontal = PADDING.div(2).dp)
                    .clip(listShape(count, changelogEntry.count(), roundingLarge = PADDING.dp))
                    .background(color)
                    .padding(PADDING.div(2).dp),
                horizontalArrangement = Arrangement.spacedBy(PADDING.div(2).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = PADDING.div(2).dp)
                        .size(PADDING.dp)
                        .clip(MaterialShapes.Cookie6Sided.toShape(count * 30))
                        .background(badgeColor)
                )
                Text(
                    modifier = Modifier.padding(PADDING.div(2).dp),
                    text = it
                        .removePrefix("[BREAKING]"),
                )
            }
            count++
        }

        Spacer(
            modifier = Modifier.height(PADDING.div(2).dp)
        )

        Text(
            modifier = Modifier.padding(start = PADDING.times(0.75).dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
            text = stringResource(R.string.changelogHistory),
        )

        val uriHandler = LocalUriHandler.current
        RowButton(
            icon = Icons.Rounded.Link,
            description = stringResource(R.string.showChangeLog),
            onClick = {
                uriHandler.openUri("https://movetransit.app/changelog/")
            }
        )
    }
}