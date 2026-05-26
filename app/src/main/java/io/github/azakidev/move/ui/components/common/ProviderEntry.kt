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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.azakidev.move.R
import io.github.azakidev.move.data.items.ProviderItem
import io.github.azakidev.move.ui.PADDING

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProviderEntry(
    modifier: Modifier = Modifier,
    provider: ProviderItem,
    shape: Shape,
    providerRepo: String,
    onClick: () -> Unit,
    savedProviders: List<Int>,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PADDING.div(2).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PADDING.div(2).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imgUrl = providerRepo + "/" + provider.name + "/res/provider.png"
                AsyncImage(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.large),
                    model = ImageRequest.Builder(LocalContext.current).data(imgUrl).crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.placeholder_provider),
                    error = painterResource(R.drawable.placeholder_provider),
                    contentScale = ContentScale.Crop,
                    contentDescription = provider.name,
                )
                Text(
                    modifier = Modifier.fillMaxWidth(.8f),
                    text = provider.name,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            IconButton(
                shape = CircleShape, colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ), onClick = onClick
            ) {
                AnimatedContent(
                    targetState = provider.id in savedProviders, transitionSpec = {
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
        if (provider.description.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .padding(horizontal = PADDING.dp)
                    .padding(bottom = PADDING.times(0.75).dp, top = PADDING.div(2).dp),
                text = provider.description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
@Preview
fun ProviderEntryPreview() {
    val provider = ProviderItem(
        name = "FictionalProvider",
        description = "This is a placeholder provider, if you see this it's probably in a preview"
    )

    val savedProviders = emptyList<Int>()

    ProviderEntry(
        modifier = Modifier,
        provider,
        MaterialTheme.shapes.medium,
        "",
        {},
        savedProviders
    )
}