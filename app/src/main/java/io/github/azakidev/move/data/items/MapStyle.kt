package io.github.azakidev.move.data.items

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import io.github.azakidev.move.R


enum class MapStyle(
    val color: Brush,
    val url: Any
) {
    Liberty(
            color = Brush.linearGradient(listOf(
                Color(0xFFE7E4E1),
                Color(0xFF9EBDff)
            )),
            url = "https://tiles.openfreemap.org/styles/liberty"
    ),
    Bright(
        color = Brush.linearGradient(listOf(
            Color(0xFFffeeaa),
            Color(0xFFe9ac77)
        )),
        url = "https://tiles.openfreemap.org/styles/bright"
    ),
    Eclipse(
        color = Brush.linearGradient(listOf(
            Color(0xFFe19a55),
            Color(0xFF955c40),
        )),
        url = R.raw.eclipse
    ),
    Shadow(
        color = Brush.linearGradient(listOf(
            Color(0xFF3e3d52),
            Color(0xFF1e1d2a)
        )),
        url = R.raw.shadow
    )
}