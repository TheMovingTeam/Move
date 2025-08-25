package io.github.azakidev.move

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.azakidev.move.data.MoveModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.ui.pages.HomePage
import io.github.azakidev.move.ui.pages.LinesPage
import io.github.azakidev.move.ui.pages.MapPage
import io.github.azakidev.move.ui.pages.StopPage
import io.github.azakidev.move.ui.theme.MoveTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoveTheme {
                AppNavigator()
            }
        }
    }
}

enum class AppDestinations(
    @StringRes val label: Int,
    val icon: ImageVector,
    @StringRes val contentDescription: Int
) {
    HOME(R.string.home, Icons.Default.Home, R.string.home),
    LINES(R.string.lines, Icons.AutoMirrored.Filled.ArrowForward, R.string.lines),
    MAP(R.string.map, Icons.Default.LocationOn, R.string.map)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AppNavigator() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    val model = viewModel<MoveModel>()

    val sheetState = rememberModalBottomSheetState()
    val sheetModel = viewModel<SheetStopViewModel>()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            stringResource(it.contentDescription)
                        )
                    },
                    label = { Text(stringResource(it.label)) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        when (currentDestination) {
            AppDestinations.HOME -> HomePage(model, sheetModel)
            AppDestinations.LINES -> LinesPage(model, sheetModel)
            AppDestinations.MAP -> MapPage()
        }
    }

    if (sheetModel.showBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxHeight(),
            onDismissRequest = { sheetModel.showBottomSheet = false },
            sheetState = sheetState,
            dragHandle = { },
            content = {
                StopPage(model, sheetModel)
            }
        )
    }
}