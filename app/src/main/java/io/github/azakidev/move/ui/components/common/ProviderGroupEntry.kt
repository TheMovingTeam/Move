package io.github.azakidev.move.ui.components.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.azakidev.move.data.items.ProviderGroup
import io.github.azakidev.move.data.items.ProviderItem
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProviderGroupEntry(
    modifier: Modifier = Modifier,
    providerGroup: ProviderGroup,
    shape: Shape,
    providerRepo: String,
    savedProviders: List<Int>,
    onClick: () -> Unit,
) {
    val isAdded = remember { mutableStateOf(false) }

    LaunchedEffect(savedProviders) {
        isAdded.value = savedProviders.containsAll(providerGroup.providers.map { it.hashCode() })
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(PADDING.div(2).dp),

        verticalArrangement = Arrangement.spacedBy(PADDING.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Group Name
            Text(
                modifier = Modifier.fillMaxWidth(.8f),
                text = providerGroup.name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Light
            )
            // Add button
            IconButton(
                shape = CircleShape, colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ), onClick = onClick
            ) {
                AnimatedContent(
                    targetState = isAdded.value, transitionSpec = {
                        fadeIn(
                            animationSpec = MotionScheme.expressive().fastEffectsSpec()
                        ) + scaleIn(
                            animationSpec = MotionScheme.expressive().fastSpatialSpec()
                        ) togetherWith fadeOut(
                            animationSpec = MotionScheme.expressive().fastEffectsSpec()
                        ) + scaleOut(
                            animationSpec = MotionScheme.expressive().fastSpatialSpec()
                        )
                    }) { state ->
                    when (state) {
                        true -> {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = "Save provider"
                            )
                        }

                        false -> {
                            Icon(
                                imageVector = Icons.Rounded.FavoriteBorder,
                                contentDescription = "Save provider"
                            )
                        }
                    }
                }
            }
        }
        // Provider icons
        FlowRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PADDING.div(2).dp),
            maxLines = 1
        ) {
            providerGroup.providers.forEach { provider ->
                val imgUrl = "$providerRepo/$provider/res/provider.png"

                AsyncImage(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(MaterialTheme.shapes.large),
                    model = ImageRequest.Builder(LocalContext.current).data(imgUrl).crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.placeholder_provider),
                    error = painterResource(R.drawable.placeholder_provider),
                    contentScale = ContentScale.Crop,
                    contentDescription = provider,
                )
            }
        }
    }
}

@Composable
@Preview
fun ProviderGroupEntryPreview() {
    val providerGroup = ProviderGroup(
        "Dummy group",
        listOf(
            "FictionalProvider",
            "PossibleProvider",
            "A provider that happens to have a really long name that's kinda silly"
        )
    )
    val savedProviders = emptyList<Int>()

    ProviderGroupEntry(
        Modifier,
        providerGroup,
        MaterialTheme.shapes.medium,
        "",
        savedProviders,
    ) {}
}