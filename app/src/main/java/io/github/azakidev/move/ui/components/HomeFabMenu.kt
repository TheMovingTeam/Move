package io.github.azakidev.move.ui.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import io.github.azakidev.move.MainView
import io.github.azakidev.move.Providers
import io.github.azakidev.move.QrScanner
import io.github.azakidev.move.R

data class FabEntry(
    val icon: ImageVector,
    @StringRes val label: Int
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeFabMenu(
    backStack: NavBackStack,
    initialState: Boolean = false
) {
    val listState = rememberLazyListState()
    val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    var fabMenuExpanded by rememberSaveable { mutableStateOf(initialState) }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = initialState }

    val context = LocalContext.current.applicationContext

    val items = listOf(
        FabEntry(Icons.Filled.AddRoad, R.string.providerTitle),
        FabEntry(Icons.Filled.QrCode, R.string.qrScan),
    )

    FloatingActionButtonMenu(
        expanded = fabMenuExpanded,
        button = {
            ToggleFloatingActionButton(
                modifier = Modifier
                    .semantics {
                        traversalIndex = -1f
                        stateDescription = if (fabMenuExpanded) "Expanded" else "Collapsed"
                        contentDescription = "Toggle menu"
                    }
                    .animateFloatingActionButton(
                        visible = fabVisible || fabMenuExpanded,
                        alignment = Alignment.BottomEnd,
                    ),
                checked = fabMenuExpanded,
                onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.search),
                )
            }
        },
    ) {
        items.forEachIndexed { i, item ->
            FloatingActionButtonMenuItem(
                modifier =
                    Modifier.semantics {
                        isTraversalGroup = true
                        // Add a custom a11y action to allow closing the menu when focusing
                        // the last menu item, since the close button comes before the first
                        // menu item in the traversal order.
                        if (i == items.size - 1) {
                            customActions =
                                listOf(
                                    CustomAccessibilityAction(
                                        label = "Close menu",
                                        action = {
                                            fabMenuExpanded = false
                                            true
                                        },
                                    )
                                )
                        }
                    },
                onClick = {
                    fabMenuExpanded = false
                    when (item.label) {
                        R.string.providerTitle -> backStack.add(Providers)
                        R.string.qrScan -> backStack.add(QrScanner)
                        else -> Toast
                            .makeText(context, "Unimplemented", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                icon = { Icon(item.icon, contentDescription = null) },
                text = { Text(text = stringResource(item.label)) },
            )
        }
    }
}

@Composable @Preview
fun HomeFabMenuPreview() {
    val backStack = rememberNavBackStack(MainView)
    HomeFabMenu(backStack)
}

@Composable @Preview
fun HomeFabMenuExpandedPreview() {
    val backStack = rememberNavBackStack(MainView)
    HomeFabMenu(backStack, true)
}