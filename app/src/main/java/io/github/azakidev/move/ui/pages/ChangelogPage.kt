@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package io.github.azakidev.move.ui.pages

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.listShape
import io.github.azakidev.move.ui.components.LogoHero
import io.github.azakidev.move.ui.components.RowButton

@Composable @Preview
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
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
        var count = 0
        changelogEntry.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(horizontal = 8.dp)
                    .clip(listShape(count, changelogEntry.count()))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(MaterialShapes.Cookie6Sided.toShape(count * 30))
                        .background(MaterialTheme.colorScheme.secondaryFixedDim)
                )
                Text(
                    text = it,
                )
            }
            count++
        }

        Spacer(
            modifier = Modifier
        )

        val uriHandler = LocalUriHandler.current
        RowButton(
            icon = Icons.Rounded.Link,
            description = stringResource(R.string.changelogHistory),
            onClick = {
                uriHandler.openUri("https://movetransit.app/changelog/")
            }
        )
    }
}