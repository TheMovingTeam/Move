package io.github.azakidev.move.ui.pages.panes.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.listShape

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
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.4f)
                    .blur(60.dp),
                painter = painterResource(R.drawable.banner),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
            Scaffold(
                modifier = Modifier
                    .padding(horizontal = PADDING.dp)
                    .padding(top = PADDING.times(4).dp)
                    .width((windowSizeClass.minWidthDp * 0.55).dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .shadow(4.dp),
                bottomBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = PADDING.times(3).dp, top = PADDING.div(2).dp)
                            .padding(horizontal = PADDING.dp),
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
                    verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = PADDING.times(0.75).dp),
                        text = stringResource(R.string.whatIsMove),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
                    ) {
                        items(entryTitles.count()) { i ->
                            ExplainingRow(
                                index = i + 1,
                                shape = listShape(i, entryTitles.count()),
                                modifier = Modifier.padding(horizontal = PADDING.div(2).dp),
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
                        .padding(bottom = PADDING.times(3).dp, top = PADDING.div(2).dp)
                        .padding(horizontal = PADDING.dp),
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
                verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = PADDING.div(2).dp)
                        .padding(horizontal = PADDING.div(2).dp)
                        .clip(MaterialTheme.shapes.large)
                        .aspectRatio(16 / 9f),
                    painter = painterResource(R.drawable.banner),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(vertical = PADDING.div(4).dp),
                    text = stringResource(R.string.whatIsMove),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
                ) {
                    items(entryTitles.count()) { i ->
                        ExplainingRow(
                            index = i + 1,
                            shape = listShape(i, entryTitles.count()),
                            modifier = Modifier.padding(horizontal = PADDING.div(2).dp),
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
            modifier = Modifier.padding(
                horizontal = PADDING.div(2).dp,
                vertical = PADDING.times(0.75).dp
            ),
            horizontalArrangement = Arrangement.spacedBy(PADDING.times(0.75).dp),
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
@Preview(device = Devices.PIXEL_FOLD, showSystemUi = true)
@Preview(device = Devices.PIXEL_TABLET, showSystemUi = true)
fun FeaturePagePreview() {
    FeaturePage(
        onBack = {},
        onNext = {}
    )
}