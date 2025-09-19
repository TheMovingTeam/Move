package io.github.azakidev.move.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastDistinctBy
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.StopItem

@Composable
fun EmblemShape(
    line: LineItem, modifier: Modifier = Modifier, textStyle: TextStyle = MaterialTheme.typography.titleLarge
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
            ) < 1.4f
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
            .clip(shape = shapeFromId(line.emblem.hashCode()))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        val text = if (line.emblem.length <= 3) line.emblem else line.emblem.substring(0..2)
        Text(
            text = text,
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = textStyle,
            color = textColor
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
        23 -> MaterialShapes.Diamond.toShape()
        else -> MaterialShapes.Circle.toShape()
    }
}

@Composable
@Preview
fun ShapePreview() {
    Row {
        Column {
            (0..11).forEach { i ->
                val line = LineItem(id = i, emblem = "L$i")
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .clip(shape = shapeFromId(i))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    val text = if (line.emblem.length <= 3) line.emblem else line.emblem.substring(0..2)
                    Text(
                        text = text,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Column {
            (12..23).forEach { i ->
                val line = LineItem(id = i, emblem = "L$i")
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .clip(shape = shapeFromId(i))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    val text = if (line.emblem.length <= 3) line.emblem else line.emblem.substring(0..2)
                    Text(
                        text = text,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Column {
            val line = LineItem(id = 1, emblem = "CAS")
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(48.dp)
                    .clip(shape = shapeFromId(line.emblem.hashCode()))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                val text = if (line.emblem.length <= 3) line.emblem else line.emblem.substring(0..2)
                Text(
                    text = text,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            val line2 = LineItem(id = 1, emblem = "TURI")
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(48.dp)
                    .clip(shape = shapeFromId(line2.emblem.hashCode()))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                val text = if (line2.emblem.length <= 3) line2.emblem else line2.emblem.substring(0..2)
                Text(
                    text = text,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun StopEmblemRow(
    modifier: Modifier = Modifier,
    stopItem: StopItem,
    lines: List<LineItem>
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            space = 4.dp,
            alignment = Alignment.End
        )
    ) {
        val lineItems = lines.filter { stopItem.lines.contains(it.id) }
        val distinctLines = lineItems.fastDistinctBy { line -> line.emblem }.sortedBy { it.emblem }
        if (distinctLines.count() <= 3) {
            distinctLines.forEach { line ->
                EmblemShape(
                    modifier = Modifier.size(36.dp),
                    line = line,
                    textStyle = MaterialTheme.typography.titleSmall
                )
            }
        } else {
            distinctLines.take(2).forEach { line ->
                EmblemShape(
                    modifier = Modifier.size(36.dp),
                    line = line,
                    textStyle = MaterialTheme.typography.titleSmall
                )
            }
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = null
            )
        }
    }
}