package io.github.azakidev.move.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import androidx.navigation3.runtime.NavKey
import io.github.azakidev.move.R
import kotlinx.serialization.Serializable
import java.util.Locale
import kotlin.streams.toList

// Common values

const val PADDING = 16
const val HERO_HEIGHT = 208

// App locations
@Serializable
internal data object MainView : NavKey

@Serializable
internal data object Settings : NavKey

@Serializable
internal data object Providers : NavKey

@Serializable
internal data object QrScanner : NavKey


fun listShape(
    count: Int,
    total: Int,
    roundingLarge: Dp = 24.dp,
    roundingSmall: Dp = 4.dp
): Shape {
    if (total == 1) {
        return RoundedCornerShape(
            roundingLarge
        )
    }
    return when (count) {
        0 -> {
            RoundedCornerShape(
                topStart = roundingLarge,
                topEnd = roundingLarge,
                bottomStart = roundingSmall,
                bottomEnd = roundingSmall,
            )
        }

        total - 1 -> {
            RoundedCornerShape(
                topStart = roundingSmall,
                topEnd = roundingSmall,
                bottomStart = roundingLarge,
                bottomEnd = roundingLarge
            )
        }

        else -> {
            RoundedCornerShape(
                roundingSmall
            )
        }
    }
}

fun String.fmt(): String {
    return this
        .lowercase()
        .replace("-", " - ")
        .replace("–", " - ")
        .replace("—", " - ")
        .replace(">", " > ")
        .replace("(", " ( ")
        .replace("/", " / ")
        .replace(".", ". ")
        .replace("'", "' ")
        .replace("\"", "")
        .replace("_", " ")
        .replace("[", " ")
        .replace("]", " ")
        .replace("avda", "av.")
        .replace("- obres", "( obres )")
        .replace("..", ".")
        .replace("  ", " ")
        .replace("- >", ">")
        .split(' ')
        .map { word ->
            if (word.uppercase().chars().allMatch { "MDCLXVI".chars().toList().contains(it) }
            ) { // Check if it's a roman numeral
                word.uppercase()
            } else {
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
        }
        .fastJoinToString(" ")
        .replace("c/", "C/")
        .replace("C/", "C/ ")
        .replace("' ", "'")
        .replace("( ", "(")
        .replace(" )", ")")
        .replace(" / ", "/")
}

fun String.fmtSearch(): String {
    return this
        .lowercase()
        .toList()
        .filterNot { listOf('-', ' ', '(', ')', '.').contains(it) }
        .joinToString("")
}

@Composable
fun PaddingValues.copy(
    top: Dp = this.calculateTopPadding(),
    start: Dp = this.calculateStartPadding(LocalLayoutDirection.current),
    end: Dp = this.calculateEndPadding(LocalLayoutDirection.current),
    bottom: Dp = this.calculateBottomPadding(),
): PaddingValues {
    return PaddingValues(
        top = top,
        start = start,
        end = end,
        bottom = bottom
    )
}

enum class AppDestinations(
    @param:StringRes val label: Int,
    val icon: ImageVector,
    @param:StringRes val contentDescription: Int
) {
    HOME(R.string.home, Icons.Rounded.Home, R.string.home),
    LINES(R.string.lines, Icons.AutoMirrored.Rounded.ArrowForward, R.string.lines),
    MAP(R.string.map, Icons.Rounded.LocationOn, R.string.map)
}