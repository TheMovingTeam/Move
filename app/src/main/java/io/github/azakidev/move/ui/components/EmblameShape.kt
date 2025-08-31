package io.github.azakidev.move.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun shapeFromId(id:Int): Shape {
    return when(id % 24) {
        0  -> MaterialShapes.Cookie12Sided.toShape()
        1  -> MaterialShapes.Square.toShape()
        2  -> MaterialShapes.Arrow.toShape()
        3  -> MaterialShapes.Cookie9Sided.toShape()
        4  -> MaterialShapes.Pill.toShape()
        5  -> MaterialShapes.Flower.toShape()
        6  -> MaterialShapes.Slanted.toShape()
        7  -> MaterialShapes.Sunny.toShape()
        8  -> MaterialShapes.Diamond.toShape()
        9  -> MaterialShapes.Gem.toShape()
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

@Composable @Preview
fun ShapePreview() {
    Row {
        Column{
            (0..11).forEach { i ->
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .clip(shape = shapeFromId(i))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = i.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Column{
            (12..23).forEach { i ->
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .clip(shape = shapeFromId(i))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = i.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}