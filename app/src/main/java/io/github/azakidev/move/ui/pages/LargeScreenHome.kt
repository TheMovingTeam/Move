package io.github.azakidev.move.ui.pages

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9
import androidx.compose.ui.tooling.preview.Devices.PIXEL_FOLD
import androidx.compose.ui.tooling.preview.Devices.PIXEL_TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel
import io.github.azakidev.move.ui.components.QrFAB

const val FAB_PADDING = 8

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LargeScreenHome(
    model: MoveViewModel,
    sheetModel: SheetStopViewModel,
    backStack: NavBackStack<NavKey>
) {
    Scaffold(
        floatingActionButton = {
            QrFAB(
                modifier = Modifier.padding(end = FAB_PADDING.dp),
                backStack
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier.padding(
                top = 0.dp,
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                bottom = 0.dp
            )
        ) {
            HomePage(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, bottom = 0.dp),
                fabShouldAppear = false,
                model = model, sheetModel = sheetModel, backStack = backStack
            )
            LinesPage(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 0.dp),
                appBarCanScroll = false,
                model = model, sheetModel = sheetModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview(device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape", showSystemUi = true)
@Preview(device = PIXEL_FOLD, showSystemUi = true)
@Preview(device = PIXEL_TABLET, showSystemUi = true)
fun LargeScreenHomePagePreview(
) {
    Scaffold(
        floatingActionButton = {
            QrFAB(
                modifier = Modifier.padding(end = FAB_PADDING.dp),
                backStack = rememberNavBackStack()
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 0.dp,
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = 0.dp
                ),
        ) {
            HomePagePreview(
                modifier = Modifier
                    .fillMaxWidth(.5f)
                    .padding(0.dp),
                fabShouldAppear = false
            )
            LinesPagePreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            )
        }
    }
}