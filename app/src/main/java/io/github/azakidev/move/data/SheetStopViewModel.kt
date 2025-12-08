package io.github.azakidev.move.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.azakidev.move.data.items.StopItem

class SheetStopViewModel: ViewModel() {
    var showBottomSheet by mutableStateOf(false)
    var sheetStop by mutableStateOf(StopItem())
}