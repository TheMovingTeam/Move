package io.github.azakidev.move.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.AddRoad
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.BuildConfig
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.PADDING
import io.github.azakidev.move.ui.components.common.trailingButton
import io.github.azakidev.move.ui.listShape
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProviderSection(
    state: TextFieldState,
    providerRepo: StateFlow<String>,
    onClick: (String) -> Unit,
    onBack: () -> Unit,
    isOnboardingComplete: Boolean
) {
    val trailingIcon = trailingButton(
        textState = state.text.toString(),
        defaultText = providerRepo.collectAsState().value,
        icon = Icons.Default.Save,
        onClick = {
            onClick(state.text.toString())
        }
    )

    val totalElements = if (isOnboardingComplete) 2 else 1

    Text(
        modifier = Modifier.padding(horizontal = PADDING.dp, vertical = PADDING.div(2).dp),
        text = stringResource(R.string.providerTitle),
        style = MaterialTheme.typography.titleMedium
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(PADDING.div(4).dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = PADDING.div(2).dp)
                .clip(shape = listShape(0, totalElements, 24.dp, 4.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.padding(start = 14.dp, top = 2.dp),
                    text = stringResource(R.string.providerSource),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = {
                        state.edit {
                            replace(
                                0,
                                state.text.length,
                                if (BuildConfig.DEBUG)
                                    "https://raw.githubusercontent.com/TheMovingTeam/Providers/refs/heads/testing"
                                else "https://raw.githubusercontent.com/TheMovingTeam/Providers/refs/heads/main"
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restore,
                        contentDescription = null
                    )
                }
            }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PADDING.div(2).dp),
                state = state,
                shape = MaterialTheme.shapes.large,
                lineLimits = TextFieldLineLimits.SingleLine,
                placeholder = {
                    Text(
                        text = stringResource(R.string.providerSource)
                    )
                },
                trailingIcon = trailingIcon
            )
        }
        if (isOnboardingComplete) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PADDING.div(2).dp)
                    .clip(shape = listShape(1, totalElements, 24.dp, 4.dp))
                    .clickable(
                        onClick = onBack
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Row(
                    modifier = Modifier
                        .padding(
                            horizontal = PADDING.div(2).dp,
                            vertical = PADDING.times(0.75).dp
                        )
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PADDING.div(2).dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryFixedDim)
                            .padding(PADDING.div(2).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddRoad,
                            contentDescription = stringResource(R.string.providerTitle),
                            tint = MaterialTheme.colorScheme.onSecondaryFixed
                        )
                    }
                    Text(
                        text = stringResource(R.string.savedProviders)
                    )
                }
            }
        }
    }
}