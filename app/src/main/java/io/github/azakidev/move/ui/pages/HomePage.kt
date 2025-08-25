package io.github.azakidev.move.ui.pages

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.azakidev.move.R
import io.github.azakidev.move.data.MoveModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.ui.components.FavStopCarousel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomePage(
    model: MoveModel, sheetModel: SheetStopViewModel
) {
    val context = LocalContext.current.applicationContext

    val listState = rememberLazyListState()
    val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    val items = listOf(
            Icons.Filled.AddRoad to "Provider",
            Icons.Filled.QrCode to "Scan",
        )

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Going somewhere?",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.displaySmallEmphasized,
                        fontWeight = FontWeight.Black
                    )
                },
                expandedHeight = TopAppBarDefaults.MediumAppBarCollapsedHeight,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                scrollBehavior = null,
            )
        },
        floatingActionButton = {
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
                            Toast.makeText(context, "Unimplemented", Toast.LENGTH_SHORT).show()
                                  },
                        icon = { Icon(item.first, contentDescription = null) },
                        text = { Text(text = item.second) },
                    )
                }
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .clip(shape = RoundedCornerShape(30.dp, 30.dp, 0.dp, 0.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = stringResource(id = R.string.favouriteStops),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    FavStopCarousel(model, sheetModel)
                }
            }
        },
    )
}

@SuppressLint("ViewModelConstructorInComposable")
@Composable
@Preview
fun HomePagePreview() {
    val model = MoveModel()
    val sheetModel = SheetStopViewModel()
    HomePage(model, sheetModel)
}