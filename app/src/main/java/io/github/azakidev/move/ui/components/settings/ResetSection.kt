package io.github.azakidev.move.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.common.RowButton
import io.github.azakidev.move.ui.listShape

@Composable
fun ResetSection(
    onAppReset: () -> Unit,
    onOnboardingReset: () -> Unit
) {
    Column(
        modifier = Modifier.padding(bottom = PADDING.div(2).dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = PADDING.dp),
            text = stringResource(R.string.reset),
            style = MaterialTheme.typography.titleMedium
        )
        val totalEntries = 2
        val shape = if (BuildConfig.DEBUG) listShape(0, totalEntries, 24.dp, 4.dp)
        else listShape(1, 1, 24.dp, 4.dp)
        RowButton(
            shape = shape,
            icon = Icons.Rounded.Delete,
            color = MaterialTheme.colorScheme.errorContainer,
            iconColor = MaterialTheme.colorScheme.onErrorContainer,
            description = stringResource(R.string.resetDesc),
            onClick = onAppReset
        )
        if (BuildConfig.DEBUG) {
            RowButton(
                shape = listShape(1, totalEntries, 24.dp, 4.dp),
                icon = Icons.Rounded.BugReport,
                color = MaterialTheme.colorScheme.errorContainer,
                iconColor = MaterialTheme.colorScheme.onErrorContainer,
                description = stringResource(R.string.resetOnboarding),
                onClick = onOnboardingReset
            )
        }
    }
}
