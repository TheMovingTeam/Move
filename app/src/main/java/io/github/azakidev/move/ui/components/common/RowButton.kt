package io.github.azakidev.move.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.ui.PADDING

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun RowButton(
    shape: Shape = MaterialTheme.shapes.largeIncreased,
    color: Color = MaterialTheme.colorScheme.secondaryFixed,
    iconColor: Color = MaterialTheme.colorScheme.onSecondaryFixed,
    icon: ImageVector = Icons.Rounded.BugReport,
    description: String = "Suggested action text",
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PADDING.div(2).dp)
            .clip(shape = shape)
            .clickable(
                onClick = onClick
            )
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = PADDING.div(2).dp, vertical = PADDING.times(0.75).dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PADDING.div(2).dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color)
                    .padding(PADDING.div(2).dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = description,
                    tint = iconColor
                )
            }
            Text(
                text = description
            )
        }
    }
}