package io.github.azakidev.move.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.HERO_HEIGHT
import io.github.azakidev.move.ui.PADDING

@Composable
fun EmptyCard(
    content: String
) {
    Box(
        modifier = Modifier
            .height(HERO_HEIGHT.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = content,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
@Preview
fun EmptyCardPreview(){
    Column(
        verticalArrangement = Arrangement.spacedBy(PADDING.div(2).dp)
    ) {
        EmptyCard(stringResource(R.string.noFavStops))
        EmptyCard(stringResource(R.string.noRecentStops))
        EmptyCard(stringResource(R.string.noTimes))
        EmptyCard(stringResource(R.string.noAlerts))
    }
}