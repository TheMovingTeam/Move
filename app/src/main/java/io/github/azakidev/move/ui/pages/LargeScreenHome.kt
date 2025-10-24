package io.github.azakidev.move.ui.pages

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices.PIXEL_FOLD
import androidx.compose.ui.tooling.preview.Devices.PIXEL_TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.azakidev.move.data.MoveViewModel
import io.github.azakidev.move.data.SheetStopViewModel

@Composable
fun LargeScreenHome(
    model: MoveViewModel,
    sheetModel: SheetStopViewModel,
    backStack: NavBackStack<NavKey>
) {
    Row {
        HomePage(
            modifier = Modifier
                .fillMaxWidth(.5f)
                .padding(start = 8.dp),
            model, sheetModel, backStack
        )
        LinesPage(Modifier.fillMaxWidth(),
            model, sheetModel, appBarCanScroll = false
        )
    }
}

@Composable
@Preview(device = PIXEL_FOLD, showSystemUi = true)
@Preview(device = PIXEL_TABLET, showSystemUi = true)
fun LargeScreenHomePagePreview(
) {
    Row {
        HomePagePreview(
            modifier = Modifier
                .fillMaxWidth(.5f)
                .padding(start = 8.dp)
        )
        LinesPagePreview(
            modifier = Modifier.fillMaxWidth()
        )
    }
}