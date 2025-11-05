package io.github.azakidev.move.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toColorLong
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastDistinctBy
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.StopItem

@Composable
fun EmblemShape(
    modifier: Modifier = Modifier,
    line: LineItem,
    emblemOverride: String? = null,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge
) {
    val color = when (line.color) {
        null -> MaterialTheme.colorScheme.primary
        else -> {
            Color(line.color.toColorInt())
        }
    }
    val textColor = if (color.value != MaterialTheme.colorScheme.primary.value) {
        if (ColorUtils.calculateContrast(
                Color.White.toColorLong().toColorInt(), color.toColorLong().toColorInt()
            ) < 1.85f
        ) {
            Color.Black
        } else {
            Color.White
        }
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = modifier
            .clip(shape = shapeFromId(line.emblem.hashCode() + line.provider.hashCode()))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        val emblem = emblemOverride ?: line.emblem
        val text = if (emblem.length <= 4) emblem else emblem.substring(0..3)

        Text(
            modifier = Modifier.padding(6.dp),
            text = text,
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = textStyle,
            fontWeight = FontWeight.Medium,
            color = textColor,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 0.sp,
                maxFontSize = textStyle.fontSize
            )
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun shapeFromId(id: Int): Shape {
    return when (id % 24) {
        0 -> MaterialShapes.Cookie12Sided.toShape()
        1 -> MaterialShapes.Square.toShape()
        2 -> MaterialShapes.Arrow.toShape()
        3 -> MaterialShapes.Cookie9Sided.toShape()
        4 -> MaterialShapes.Pill.toShape()
        5 -> MaterialShapes.Flower.toShape()
        6 -> MaterialShapes.Slanted.toShape()
        7 -> MaterialShapes.Sunny.toShape()
        8 -> MaterialShapes.Diamond.toShape(360)
        9 -> MaterialShapes.Gem.toShape(90)
        10 -> MaterialShapes.Cookie4Sided.toShape()
        11 -> MaterialShapes.Arch.toShape(45)
        12 -> MaterialShapes.Clover4Leaf.toShape()
        13 -> MaterialShapes.PuffyDiamond.toShape()
        14 -> MaterialShapes.Bun.toShape()
        15 -> MaterialShapes.Ghostish.toShape(90)
        16 -> MaterialShapes.SoftBurst.toShape()
        17 -> MaterialShapes.Circle.toShape()
        18 -> MaterialShapes.ClamShell.toShape()
        19 -> MaterialShapes.Pill.toShape(45)
        20 -> MaterialShapes.Clover8Leaf.toShape()
        21 -> MaterialShapes.Pentagon.toShape()
        22 -> MaterialShapes.PixelCircle.toShape()
        23 -> MaterialShapes.Oval.toShape(45)
        else -> MaterialShapes.Circle.toShape()
    }
}

@Composable
@Preview
fun ShapePreview() {
    FlowColumn(
        maxItemsInEachColumn = 12
    ) {
        (0..23).forEach { i ->
            val line = LineItem(id = i, emblem = "L$i")
            EmblemShape(
                modifier = Modifier
                    .padding(8.dp)
                    .size(48.dp)
                    .clip(shape = shapeFromId(i))
                    .background(MaterialTheme.colorScheme.primary),
                line = line
            )
        }
        val line = LineItem(id = 1, emblem = "CAS")
        EmblemShape(
            modifier = Modifier
                .padding(8.dp)
                .size(48.dp)
                .clip(shape = shapeFromId(line.emblem.hashCode()))
                .background(MaterialTheme.colorScheme.primary),
            line = line
        )
        val line2 = LineItem(id = 1, emblem = "TURI")
        EmblemShape(
            modifier = Modifier
                .padding(8.dp)
                .size(48.dp)
                .clip(shape = shapeFromId(line2.emblem.hashCode()))
                .background(MaterialTheme.colorScheme.primary),
            line = line2
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StopEmblemRow(
    modifier: Modifier = Modifier,
    stopItem: StopItem,
    lines: List<LineItem>
) {
    val lineItems =
        lines.filter { stopItem.lines.contains(it.id) && it.provider == stopItem.provider }
    val distinctLines = lineItems.fastDistinctBy { line -> line.emblem }.sortedBy { it.emblem }
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            space = 4.dp,
            alignment = Alignment.End
        ),
        maxLines = 1,
    ) {
        distinctLines.forEach { line ->
            EmblemShape(
                modifier = Modifier.size(36.dp),
                line = line,
                textStyle = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
@Preview
fun StopEmblemRowPreview() {
    val stopItem = StopItem(
        id = 1,
        name = "A Stop item with a very long name lmao",
        lines = (1..5).toList()
    )

    val lines = mutableListOf<LineItem>()

    lines += LineItem(
        id = 5,
        emblem = "555b"
    )

    stopItem.lines.take(4).forEach {
        lines += LineItem(
            id = it,
            emblem = "L$it"
        )
    }


    StopEmblemRow(
        stopItem = stopItem,
        lines = lines
    )
}