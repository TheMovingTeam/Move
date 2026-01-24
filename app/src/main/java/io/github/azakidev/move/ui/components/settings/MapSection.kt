package io.github.azakidev.move.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.MapSettings
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.common.RowButton
import io.github.azakidev.move.ui.listShape

const val MapSettingsEntries = 1

@Composable
fun MapSection(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey>
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = PADDING.dp, vertical = PADDING.div(2).dp),
            text = stringResource(R.string.map),
            style = MaterialTheme.typography.titleMedium
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
        ) {
            RowButton(
                icon = Icons.Rounded.Palette,
                shape = listShape(1, MapSettingsEntries),
                description = stringResource(R.string.mapStylePickerTitle),
                onClick = { backStack.add(MapSettings) }
            )
        }
    }
}