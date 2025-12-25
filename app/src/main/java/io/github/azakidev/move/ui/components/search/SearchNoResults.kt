package io.github.azakidev.move.ui.components.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.PADDING

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun SearchNoResults(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = PADDING.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        Box(
            modifier = Modifier
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer
                ), contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier
                    .padding(24.dp)
                    .size(108.dp),
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Text(
            text = stringResource(R.string.noResults)
        )
    }
}
