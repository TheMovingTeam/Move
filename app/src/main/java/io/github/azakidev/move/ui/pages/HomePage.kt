package io.github.azakidev.move.ui.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import io.github.azakidev.move.MainView
import io.github.azakidev.move.R
import io.github.azakidev.move.Settings
import io.github.azakidev.move.data.MoveModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.ui.components.FavStopCarousel
import io.github.azakidev.move.ui.components.HomeFabMenu

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomePage(
    model: MoveModel, sheetModel: SheetStopViewModel,
    backStack: NavBackStack
) {
    model.fetchInfo()
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
                actions = {
                    IconButton(
                        onClick = {
                            backStack.add(Settings)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            HomeFabMenu(backStack)
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
    val model = viewModel<MoveModel>()
    val sheetModel = viewModel<SheetStopViewModel>()
    val backStack = rememberNavBackStack(MainView)
    HomePage(model, sheetModel, backStack)
}