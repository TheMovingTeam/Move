package io.github.azakidev.move.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.azakidev.move.R
import io.github.azakidev.move.ui.QrScanner

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QrFAB(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey>
) {
    FloatingActionButton(
        modifier = modifier.size(64.dp),
        onClick = {
            backStack.add(QrScanner)
        }
    ) {
        Icon(
            modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
            imageVector = Icons.Rounded.QrCode,
            contentDescription = stringResource(R.string.qrScan)
        )
    }
}