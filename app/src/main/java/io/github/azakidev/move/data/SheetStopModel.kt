package io.github.azakidev.move.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.azakidev.move.R

class SheetStopModel: ViewModel() {
    var showBottomSheet by mutableStateOf(false)
    var sheetStop by mutableStateOf(StopItem(0, R.drawable.nathofjoy, "StopName", 0))
}