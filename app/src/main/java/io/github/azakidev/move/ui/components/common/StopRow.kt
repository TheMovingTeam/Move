package io.github.azakidev.move.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.fmt

@Composable
fun StopRow(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.background,
    stopItem: StopItem,
    lines: List<LineItem>,
    shape: Shape,
    onClick: (() -> Unit)?,
    clickable: Boolean = true,
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(background)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    enabled = clickable,
                    onClick = onClick
                )
            } else {
                Modifier
            }
        )
        .padding(
            vertical = PADDING.div(2).dp,
            horizontal = PADDING.dp
        )

    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val stopName = stopItem.name.fmt()

        val textMod =
            if (stopName.length >= 30) Modifier.weight(4f)
            else Modifier

        Text(
            modifier = textMod.padding(vertical = PADDING.div(4).dp),
            text = stopName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 16.sp,
        )
        Spacer(
            modifier = Modifier.width(PADDING.dp)
        )
        StopEmblemRow(
            modifier = Modifier.weight(1f),
            stopItem = stopItem,
            lines = lines
        )
    }
}

@Composable
@Preview
fun SearchResultPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
    ) {
        StopRow(
            stopItem = StopItem(
                lines = listOf(1)
            ),
            lines = listOf(
                LineItem(id = 1)
            ),
            shape = MaterialTheme.shapes.medium, onClick = {}
        )
        StopRow(
            stopItem = StopItem(
                name = "A stop with a many lines",
                lines = listOf(1, 2, 3)
            ),
            lines = listOf(
                LineItem(id = 1, emblem = "L1"),
                LineItem(id = 2, emblem = "L2"),
                LineItem(id = 3, emblem = "L3"),
            ),
            shape = MaterialTheme.shapes.medium, onClick = {}
        )
        StopRow(
            stopItem = StopItem(
                name = "A stop with a really really really long name",
                lines = listOf(1, 2, 3)
            ),
            lines = listOf(
                LineItem(id = 1, emblem = "L1"),
                LineItem(id = 2, emblem = "L2"),
                LineItem(id = 3, emblem = "L3"),
            ),
            shape = MaterialTheme.shapes.medium, onClick = {}
        )
    }
}